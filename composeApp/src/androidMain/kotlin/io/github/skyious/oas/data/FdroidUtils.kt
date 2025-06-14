package io.github.skyious.oas.utils // Assuming your package name from the logs

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.BufferedInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import io.github.skyious.oas.data.model.AppInfo // Make sure this import is correct for your AppInfo model
import io.github.skyious.oas.data.model.SourceType.FDROID

// It's good practice to have the client as a member if used multiple times or if needs specific config
// For simplicity in this function, we can create it locally or pass it as a parameter.
// Let's assume you have a client defined elsewhere or you'll create one.
// For this example, I'll create one locally. If you have a shared client, use that.
private val client = OkHttpClient() // Or pass/inject your existing OkHttpClient

// For easier debugging of a specific package
private const val DEBUG_PACKAGE_NAME = "com.wirelessalien.zipxtract" // Change if you want to debug another package

suspend fun fetchFdroidApps(indexFileUrl: String): List<AppInfo> {
    Log.d("FdroidUtils", "Attempting to fetch and process F-Droid index from: $indexFileUrl")
    val appInfoList = mutableListOf<AppInfo>()

    try {
        val request = Request.Builder().url(indexFileUrl).build()
        // In a real suspend function, you'd use client.newCall(request).await() from a library like Ktor or Retrofit's suspend extensions.
        // For OkHttp's execute(), you'd typically wrap this in withContext(Dispatchers.IO)
        val response = client.newCall(request).execute() // Blocking call, ensure it's on a background thread

        if (!response.isSuccessful) {
            Log.w("FdroidUtils", "Failed to download index: $indexFileUrl, HTTP code: ${response.code}")
            return emptyList()
        }

        response.body?.use { responseBody -> // Ensures responseBody is closed
            // Example: "https://f-droid.org/repo/index-v1.jar" -> "https://f-droid.org/repo/"
            val repoBaseUrl = indexFileUrl.substringBeforeLast('/', "") + "/"
            Log.d("FdroidUtils", "Derived repoBaseUrl: $repoBaseUrl")


            if (indexFileUrl.endsWith(".jar", ignoreCase = true)) {
                Log.d("FdroidUtils", "Processing as JAR file: $indexFileUrl")
                responseBody.byteStream().use { jarByteStream ->
                    ZipInputStream(BufferedInputStream(jarByteStream)).use { zipInputStream ->
                        var ze: ZipEntry?
                        while (zipInputStream.nextEntry.also { ze = it } != null) {
                            val entry = ze ?: continue
                            if (entry.name == "index-v1.json") {
                                Log.d("FdroidUtils", "Found index-v1.json in JAR")
                                val indexJsonString = zipInputStream.bufferedReader().readText()
                                zipInputStream.closeEntry() // Close current entry

                                if (indexJsonString.contains(DEBUG_PACKAGE_NAME)) {
                                    Log.d("FdroidUtils_DebugPkg", "DEBUG_PACKAGE_NAME '$DEBUG_PACKAGE_NAME' found in raw indexJsonString.")
                                    val snippetStart = Math.max(0, indexJsonString.indexOf(DEBUG_PACKAGE_NAME) - 500)
                                    val snippetEnd = Math.min(indexJsonString.length, indexJsonString.indexOf(DEBUG_PACKAGE_NAME) + 500)
                                    Log.d("FdroidUtils_DebugPkg", "Relevant JSON snippet:\n${indexJsonString.substring(snippetStart, snippetEnd)}")
                                }

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

                                        if (packageName.isNullOrEmpty()) {
                                            Log.w("FdroidUtils", "Skipping app with missing or empty packageName. JSON (first 500 chars): ${appObject.toString(2).take(500)}")
                                            continue
                                        }

                                        if (isDebugTargetPackage) {
                                            Log.d("FdroidUtils_DebugPkg", "--- Iterating: Found DEBUG_PACKAGE_NAME: $packageName ---")
                                            Log.d("FdroidUtils_DebugPkg", "Full appObject JSON for $packageName:\n${appObject.toString(2)}")
                                            Log.d("FdroidUtils_DebugPkg", "Keys available in appObject for $packageName: ${appObject.keys().asSequence().joinToString()}")
                                        }

                                        var appName = appObject.optString("name", packageName) // Fallback to packageName if no name
                                        val authorName = appObject.optString("authorName", "Unknown Author")
                                        val sourceCodeUrl = appObject.optString("sourceCode", null)
                                        val issueTrackerUrl = appObject.optString("issueTracker", null)
                                        val license = appObject.optString("license", null)
                                        val addedTimestamp = appObject.optLong("added", 0L)
                                        val lastUpdatedTimestamp = appObject.optLong("lastUpdated", 0L)
                                        val suggestedVersionName = appObject.optString("suggestedVersionName", null)
                                        // val suggestedVersionCode = appObject.optString("suggestedVersionCode", null) // String or Int? Check F-Droid spec. Assuming String from your log.
                                        val suggestedVersionCodeString = appObject.optString("suggestedVersionCode", null)


                                        var iconUrl: String? = null
                                        var summary: String? = appObject.optString("summary", null) // Global summary as fallback
                                        var description: String? = appObject.optString("description", null) // Global description as fallback
                                        var whatsNew: String? = null
                                        val images = mutableListOf<String>()

                                        val localizedObject = appObject.optJSONObject("localized")
                                        var targetLocaleKey: String? = null

                                        if (localizedObject != null) {
                                            val locales = localizedObject.keys()
                                            val preferredLocale = "en-US"
                                            if (localizedObject.has(preferredLocale)) {
                                                targetLocaleKey = preferredLocale
                                            } else if (locales.hasNext()) {
                                                targetLocaleKey = locales.next() // Fallback to first available
                                            }

                                            if (targetLocaleKey != null) {
                                                val localeDetails = localizedObject.optJSONObject(targetLocaleKey)
                                                if (localeDetails != null) {
                                                    appName = localeDetails.optString("name", appName) // Override with localized if present
                                                    summary = localeDetails.optString("summary", summary)
                                                    description = localeDetails.optString("description", description)
                                                    whatsNew = localeDetails.optString("whatsNew", null)

                                                    val iconFileName = localeDetails.optString("icon", null)
                                                    if (!iconFileName.isNullOrEmpty()) {
                                                        iconUrl = repoBaseUrl + packageName + "/" + targetLocaleKey + "/" + iconFileName
                                                        if (isDebugTargetPackage) {
                                                            Log.d("FdroidUtils_DebugPkg", "Icon for $packageName: file=$iconFileName, locale=$targetLocaleKey, url=$iconUrl")
                                                        }
                                                    } else {
                                                        if (isDebugTargetPackage) {
                                                            Log.w("FdroidUtils_DebugPkg", "No icon filename for $packageName in locale $targetLocaleKey")
                                                        }
                                                    }

                                                    val phoneScreenshotsArray = localeDetails.optJSONArray("phoneScreenshots")
                                                    if (phoneScreenshotsArray != null) {
                                                        for (j in 0 until phoneScreenshotsArray.length()) {
                                                            val screenshotFileName = phoneScreenshotsArray.optString(j)
                                                            if (!screenshotFileName.isNullOrEmpty()) {
                                                                val screenshotUrl = repoBaseUrl + packageName + "/" + targetLocaleKey + "/" + screenshotFileName
                                                                images.add(screenshotUrl)
                                                                if (isDebugTargetPackage) {
                                                                    Log.d("FdroidUtils_DebugPkg", "Screenshot for $packageName: file=$screenshotFileName, locale=$targetLocaleKey, url=$screenshotUrl")
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Fallback for appName if it's still empty (e.g., no localized and no global "name" field)
                                        if (appName.isEmpty() || appName == packageName && appObject.has("name")) {
                                            // If appName is still the packageName, but a global "name" existed, prefer that.
                                            // This handles cases where localized name might be missing but global isn't.
                                            appName = appObject.optString("name", packageName)
                                        }
                                        if (appName.isEmpty()) appName = packageName // Absolute fallback

                                        // Placeholder for Download URL - this is complex and needs the 'packages' object from rootJson
                                        var downloadUrl: String? = null
                                        val binariesString = appObject.optString("binaries", null)
                                        if (binariesString != null && suggestedVersionName != null && binariesString.contains("%v")) {
                                            downloadUrl = binariesString.replace("%v", suggestedVersionName)
                                            if (isDebugTargetPackage) Log.d("FdroidUtils_DebugPkg", "Package $packageName has binary template, constructed basic downloadUrl: $downloadUrl (version: $suggestedVersionName). For robust URL, parse 'packages' object.")
                                        } else if (binariesString != null && !binariesString.contains("%v")) {
                                            downloadUrl = binariesString // Direct URL
                                            if (isDebugTargetPackage) Log.d("FdroidUtils_DebugPkg", "Package $packageName has direct binary URL: $downloadUrl")
                                        }
                                        else {
                                            if (isDebugTargetPackage) Log.w("FdroidUtils_DebugPkg", "Package $packageName has no 'binaries' template or no 'suggestedVersionName'. Proper parsing of 'packages' object needed for APK URL.")
                                        }
                                        // For a more robust download URL, you'd parse rootJson.optJSONObject("packages").optJSONObject(packageName)
                                        // then look at its versions, find the one matching suggestedVersionCode, and get its apkName.
                                        // Then construct: repoBaseUrl + apkName

                                        val categories = mutableListOf<String>()
                                        val categoriesArray = appObject.optJSONArray("categories")
                                        if (categoriesArray != null) {
                                            for (j in 0 until categoriesArray.length()) {
                                                categories.add(categoriesArray.getString(j))
                                            }
                                        }

                                        val appInfo = AppInfo(
                                            id = packageName, // Usually packageName is a good unique ID
                                            name = appName,
                                            packageName = packageName,
                                            author = authorName,
                                            summary = summary,
                                            description = description,
                                            logoUrl = iconUrl,
                                            downloadUrl = downloadUrl, // Placeholder
                                            version = suggestedVersionName,
                                            source = FDROID, // Assuming you have an enum for source
                                            images = images,
                                            changelog = whatsNew, // Using whatsNew as changelog, common in F-Droid
                                            sourceCodeUrl = sourceCodeUrl,
                                            websiteUrl = issueTrackerUrl, // issueTracker often serves as project homepage/website
                                            license = license,
                                            lastUpdated = lastUpdatedTimestamp,
                                            dateAdded = addedTimestamp,
                                            categories = categories,
                                            suggestedVersionCode = suggestedVersionCodeString
                                            // Add any other fields your AppInfo model expects
                                        )
                                        appInfoList.add(appInfo)
                                        if (isDebugTargetPackage) {
                                            Log.i("FdroidUtils_DebugPkg", "Successfully created and added AppInfo for $packageName: $appInfo")
                                        }
                                    }
                                    Log.d("FdroidUtils", "Finished iterating through appsArray.")
                                } else {
                                    Log.w("FdroidUtils", "'apps' JSONArray not found in rootJson or is null.")
                                }
                                Log.d("FdroidUtils", "Finished processing index-v1.json from JAR.")
                                break // Found and processed index-v1.json, exit while loop
                            }
                        }
                    }
                }
                Log.d("FdroidUtils", "Finished processing JAR for $indexFileUrl, found ${appInfoList.size} apps.")
            } else if (indexFileUrl.endsWith(".json", ignoreCase = true)) {
                // TODO: Implement direct JSON parsing (similar to the logic inside JAR processing but without ZipInputStream)
                // val indexJsonString = responseBody.source().readString(Charsets.UTF_8)
                // ... then parse 'rootJson' and 'appsArray' as above ...
                Log.w("FdroidUtils", "Direct .json file processing is not fully implemented yet for $indexFileUrl")
            } else {
                Log.w("FdroidUtils", "Unsupported file type for index: $indexFileUrl. URL must end with .jar or .json (for F-Droid index).")
                return emptyList()
            }
        } ?: run {
            Log.w("FdroidUtils", "Response body was null for $indexFileUrl")
            return emptyList()
        }

    } catch (e: Exception) {
        Log.e("FdroidUtils", "Error fetching or processing F-Droid index from $indexFileUrl", e)
        // Optionally, rethrow or return a specific error state
    }

    Log.d("FdroidUtils", "Fetched ${appInfoList.size} apps from F-Droid URL: $indexFileUrl")
    return appInfoList
}