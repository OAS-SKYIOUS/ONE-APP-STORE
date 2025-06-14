//// FdroidUtils.kt
//private val client = OkHttpClient.Builder()
//    .connectTimeout(5, TimeUnit.SECONDS)
//    .readTimeout(5, TimeUnit.SECONDS)
//    .build()
//


package io.github.skyious.oas.utils // Assuming a common package for utils

import android.util.Log
import io.github.skyious.oas.data.model.AppInfo
import io.github.skyious.oas.data.model.SourceType // Ensure this import is correct
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.concurrent.TimeUnit // If you re-add client configuration

object FdroidUtils {

    // You can configure your OkHttpClient here if needed, e.g., with timeouts
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Increased timeout for potentially large index files
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // For easier debugging of a specific package
    private const val DEBUG_PACKAGE_NAME = "com.wirelessalien.zipxtract" // Change if you want to debug another package

    suspend fun fetchFdroidApps(indexFileUrl: String): List<AppInfo> {
        Log.d("FdroidUtils", "Attempting to fetch and process F-Droid index from: $indexFileUrl")
        val appInfoList = mutableListOf<AppInfo>()

        try {
            val request = Request.Builder().url(indexFileUrl).build()
            // Use the class member client
            val response = client.newCall(request).execute() // In a real coroutine, use .await() with an async client call

            if (!response.isSuccessful) {
                Log.w("FdroidUtils", "Failed to download index: $indexFileUrl, HTTP code: ${response.code}")
                return emptyList()
            }

            response.body?.use { responseBody -> // Ensures responseBody is closed
                val repoBaseUrl = indexFileUrl.substringBeforeLast('/', "") + "/" // e.g., "https://f-droid.org/repo/"

                if (indexFileUrl.endsWith(".jar", ignoreCase = true)) {
                    Log.d("FdroidUtils", "Processing as JAR file: $indexFileUrl")
                    try {
                        responseBody.byteStream().use { jarByteStream ->
                            ZipInputStream(BufferedInputStream(jarByteStream)).use { zipInputStream ->
                                var ze: ZipEntry?
                                while (zipInputStream.nextEntry.also { ze = it } != null) {
                                    val entry = ze ?: continue
                                    if (entry.name == "index-v1.json") {
                                        Log.d("FdroidUtils", "Found index-v1.json in JAR")
                                        val indexJsonString = zipInputStream.bufferedReader().readText()
                                        zipInputStream.closeEntry() // Close current entry after reading

                                        // For debugging: Log the raw JSON if it contains the debug package
                                        if (indexJsonString.contains(DEBUG_PACKAGE_NAME)) {
                                            Log.d("FdroidUtils_DebugPkg", "DEBUG_PACKAGE_NAME '$DEBUG_PACKAGE_NAME' found in raw indexJsonString.")
                                            // Log only a snippet to avoid flooding Logcat for very large files
                                            val snippetStart = maxOf(0, indexJsonString.indexOf(DEBUG_PACKAGE_NAME) - 500)
                                            val snippetEnd = minOf(indexJsonString.length, indexJsonString.indexOf(DEBUG_PACKAGE_NAME) + 500)
                                            Log.d("FdroidUtils_DebugPkg", "Relevant JSON snippet:\n${indexJsonString.substring(snippetStart, snippetEnd)}")
                                        }

                                        try {
                                            val rootJson = JSONObject(indexJsonString)
                                            Log.d("FdroidUtils", "Successfully parsed indexJsonString into rootJson.")
                                            Log.d("FdroidUtils", "Keys in rootJson: ${rootJson.keys().asSequence().joinToString()}")

                                            val appsArray = rootJson.optJSONArray("apps")
                                            if (appsArray != null) {
                                                Log.d("FdroidUtils", "'apps' JSONArray found with length: ${appsArray.length()}")
                                                Log.d("FdroidUtils", "Starting to iterate through appsArray (length: ${appsArray.length()})")

                                                for (i in 0 until appsArray.length()) {
                                                    val appObject = appsArray.getJSONObject(i)
                                                    val packageName = appObject.optString("packageName")
                                                    val isDebugTargetPackage = packageName == DEBUG_PACKAGE_NAME

                                                    if (isDebugTargetPackage) {
                                                        Log.d("FdroidUtils_DebugPkg", "--- Iterating: Found DEBUG_PACKAGE_NAME: $packageName ---")
                                                        Log.d("FdroidUtils_DebugPkg", "Full appObject JSON for $packageName:\n${appObject.toString(2)}")
                                                        Log.d("FdroidUtils_DebugPkg", "Keys available in appObject for $packageName: ${appObject.keys().asSequence().joinToString()}")
                                                    }

                                                    if (packageName.isNullOrEmpty()) {
                                                        Log.w("FdroidUtils", "Skipping app with missing or empty packageName in appObject (first 500 chars): ${appObject.toString(2).take(500)}")
                                                        continue
                                                    }

                                                    // --- Name Extraction (Localized) ---
                                                    var appName = ""
                                                    val localizedObject = appObject.optJSONObject("localized")
                                                    if (localizedObject != null) {
                                                        val enUsDetails = localizedObject.optJSONObject("en-US")
                                                        if (enUsDetails != null) {
                                                            appName = enUsDetails.optString("name")
                                                        }
                                                        if (appName.isEmpty()) { // Fallback to any other locale's name
                                                            localizedObject.keys().asSequence().forEach { localeKey ->
                                                                val localeDetails = localizedObject.optJSONObject(localeKey)
                                                                if (localeDetails != null) {
                                                                    val localeName = localeDetails.optString("name")
                                                                    if (localeName.isNotEmpty()) {
                                                                        appName = localeName
                                                                        return@forEach // Found a name, exit loop
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                    if (appName.isEmpty()) { // Last resort if no localized name
                                                        appName = packageName // Or handle as an error/skip
                                                        if (isDebugTargetPackage) Log.w("FdroidUtils_DebugPkg", "App '$packageName' has no 'name' in localized section, using packageName.")
                                                    }

                                                    // --- Icon URL Construction ---
                                                    var iconFileName: String? = null
                                                    var iconUrl: String? = null
                                                    if (localizedObject != null) {
                                                        // Prioritize en-US, then first available
                                                        val enUsIcon = localizedObject.optJSONObject("en-US")?.optString("icon")
                                                        if (!enUsIcon.isNullOrEmpty()) {
                                                            iconFileName = enUsIcon
                                                        } else {
                                                            localizedObject.keys().asSequence().firstNotNullOfOrNull { localeKey ->
                                                                localizedObject.optJSONObject(localeKey)?.optString("icon")?.takeIf { it.isNotEmpty() }
                                                            }?.let { iconFileName = it }
                                                        }
                                                        if (!iconFileName.isNullOrEmpty()) {
                                                            // Try with "icons/" subdirectory
                                                            iconUrl = repoBaseUrl + "icons/" + iconFileName // MODIFIED LINE
                                                            if (isDebugTargetPackage) Log.d("FdroidUtils_DebugPkg", "Icon for $packageName: file=$iconFileName, url=$iconUrl (tried with /icons/ path)")
                                                        } else {
                                                            if (isDebugTargetPackage) Log.w("FdroidUtils_DebugPkg", "No icon filename found for $packageName in localized data.")
                                                        }
                                                    }


                                                    // --- Summary/Description ---
                                                    var summary: String? = null
                                                    var description: String? = null
                                                    if (localizedObject != null) {
                                                        val enUsDetails = localizedObject.optJSONObject("en-US")
                                                        if (enUsDetails != null) {
                                                            summary = enUsDetails.optString("summary").takeIf { it.isNotEmpty() }
                                                            description = enUsDetails.optString("description").takeIf { it.isNotEmpty() }
                                                        }
                                                        // Add fallback for summary/description from other locales if needed (similar to name/icon)
                                                    }

                                                    // --- APK Download URL & Name (Placeholder - Needs full implementation) ---
                                                    // This is the most complex part as it often requires looking up the suggestedVersionCode
                                                    // in the top-level "packages" object of the index-v1.json to get the actual apkName.
                                                    // The "binaries" field in appObject is often a template.
                                                    var finalDownloadUrl: String? = null
                                                    var apkFileName: String? = null
                                                    val suggestedVersionCode = appObject.optString("suggestedVersionCode")

                                                    // Example of how you *might* start to approach this, but this is simplified:
                                                    // You would typically need to access rootJson.optJSONObject("packages")
                                                    // then rootJson.optJSONObject("packages").optJSONObject(packageName)
                                                    // then find the entry for suggestedVersionCode.
                                                    // For now, we'll leave it null or try a very naive approach from 'binaries' if it's a direct URL
                                                    val binariesUrlTemplate = appObject.optString("binaries")
                                                    if (binariesUrlTemplate.isNotEmpty() && !binariesUrlTemplate.contains("%")) { // If it looks like a direct URL
                                                        finalDownloadUrl = binariesUrlTemplate
                                                        apkFileName = binariesUrlTemplate.substringAfterLast('/')
                                                    } else if (binariesUrlTemplate.isNotEmpty() && suggestedVersionCode.isNotEmpty()) {
                                                        // Attempt basic template replacement (likely needs more robust version name logic)
                                                        // val suggestedVersionName = appObject.optString("suggestedVersionName", suggestedVersionCode)
                                                        // finalDownloadUrl = binariesUrlTemplate.replace("%v", suggestedVersionName)
                                                        // apkFileName = finalDownloadUrl.substringAfterLast('/')
                                                        if (isDebugTargetPackage) Log.w("FdroidUtils_DebugPkg", "Package $packageName has binary template '$binariesUrlTemplate', proper parsing of 'packages' object is needed.")
                                                    }


                                                    // --- Other fields ---
                                                    val author = appObject.optString("authorName").ifEmpty { null }
                                                    val sourceCodeUrl = appObject.optString("sourceCode").ifEmpty { null }
                                                    val issueTrackerUrl = appObject.optString("issueTracker").ifEmpty { null }
                                                    // F-Droid 'webBaseUrl' from repo object + packageName can form a website URL
                                                    // val repoObject = rootJson.optJSONObject("repo")
                                                    // val webBaseUrl = repoObject?.optString("webBaseUrl")
                                                    // val websiteUrl = if (!webBaseUrl.isNullOrEmpty()) "$webBaseUrl/$packageName" else issueTrackerUrl
                                                    val websiteUrl = issueTrackerUrl // Simpler for now

                                                    val version = appObject.optString("suggestedVersionName", suggestedVersionCode).ifEmpty { null }
                                                    val license = appObject.optString("license").ifEmpty { null }
                                                    val lastUpdatedTimestamp = appObject.optLong("lastUpdated", 0L).takeIf { it > 0 }
                                                    val addedTimestamp = appObject.optLong("added", 0L).takeIf { it > 0 }

                                                    val categoriesJsonArray = appObject.optJSONArray("categories")
                                                    val categoriesList = mutableListOf<String>()
                                                    if (categoriesJsonArray != null) {
                                                        for (j in 0 until categoriesJsonArray.length()) {
                                                            categoriesList.add(categoriesJsonArray.getString(j))
                                                        }
                                                    }

                                                    // --- Create AppInfo Object ---
                                                    if (packageName.isNotEmpty() && appName.isNotEmpty()) {
                                                        val appInfo = AppInfo(
                                                            id = packageName,
                                                            name = appName,
                                                            packageName = packageName,
                                                            author = author,
                                                            summary = summary,
                                                            description = description,
                                                            logoUrl = iconUrl, // Assuming your AppInfo uses logoUrl
                                                            downloadUrl = finalDownloadUrl,
                                                            apkName = apkFileName,
                                                            version = version,
                                                            source = SourceType.FDROID,
                                                            sourceCodeUrl = sourceCodeUrl,
                                                            websiteUrl = websiteUrl,
                                                            license = license,
                                                            lastUpdated = lastUpdatedTimestamp,
                                                            dateAdded = addedTimestamp,
                                                            categories = categoriesList.ifEmpty { null }
                                                        )
                                                        appInfoList.add(appInfo)

                                                        if (isDebugTargetPackage) {
                                                            Log.i("FdroidUtils_DebugPkg", "Successfully created and added AppInfo for $packageName: $appInfo")
                                                        }
                                                    } else {
                                                        if (isDebugTargetPackage) {
                                                            Log.w("FdroidUtils_DebugPkg", "Skipped AppInfo creation for $packageName due to missing name or packageName. Name: '$appName', PackageName: '$packageName'")
                                                        } else {
                                                            Log.w("FdroidUtils", "Skipped AppInfo creation for an app due to missing essential fields. AppObject (partial): ${appObject.toString(2).take(300)}")
                                                        }
                                                    }
                                                } // end for loop
                                                Log.d("FdroidUtils", "Finished iterating through appsArray.")
                                            } else {
                                                Log.w("FdroidUtils", "'apps' JSONArray not found or is null in rootJson.")
                                            }
                                        } catch (e: JSONException) {
                                            Log.e("FdroidUtils", "Failed to parse index-v1.json string for $indexFileUrl", e)
                                        }
                                        break // Found and processed index-v1.json, exit while loop
                                    }
                                }
                            }
                        }
                    } catch (e: IOException) {
                        Log.e("FdroidUtils", "IOException during JAR processing for $indexFileUrl", e)
                        // This will return the (likely empty) appInfoList
                    }
                    Log.d("FdroidUtils", "Finished processing JAR for $indexFileUrl, found ${appInfoList.size} apps.")
                    return appInfoList // Return apps found from JAR

                } else if (indexFileUrl.endsWith(".json", ignoreCase = true)) {
                    Log.d("FdroidUtils", "Processing as plain JSON file: $indexFileUrl")
                    // If the URL directly points to an index-v1.json (or similar)
                    try {
                        val indexJsonString = responseBody.source().readUtf8()
                        // TODO: Implement parsing for plain .json index files if needed
                        // The parsing logic would be very similar to the one inside the JAR processing block,
                        // starting from `val rootJson = JSONObject(indexJsonString)`
                        // You'd pass `repoBaseUrl` and `indexJsonString` to a shared parsing function.
                        Log.w("FdroidUtils", "Plain JSON file parsing is not fully implemented here. The string was read but not processed into AppInfo objects.")
                        // For now, returns empty for direct JSON.
                        return emptyList() // Placeholder
                    } catch (e: IOException) {
                        Log.e("FdroidUtils", "IOException reading plain JSON file $indexFileUrl", e)
                        return emptyList()
                    }

                } else {
                    Log.w("FdroidUtils", "Unsupported file type for index: $indexFileUrl. URL must end with .jar or .json (for F-Droid index).")
                    return emptyList()
                }
            } ?: run {
                Log.w("FdroidUtils", "Response body was null for $indexFileUrl")
                return emptyList()
            }
        } catch (e: IOException) {
            Log.e("FdroidUtils", "IOException during F-Droid fetch for $indexFileUrl", e)
            return emptyList()
        } catch (e: Exception) { // Catch any other unexpected errors (like network issues, malformed URLs)
            Log.e("FdroidUtils", "Generic exception during F-Droid fetch for $indexFileUrl", e)
            return emptyList()
        }
    }
}
