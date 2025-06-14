//package io.github.skyious.oas.data
//
//// Unused import, can be removed if not used elsewhere for `add` on a Composable
//// import androidx.compose.foundation.layout.add
//import android.util.Log
//// import android.util.Log.e // Log.e can be called directly via Log.e
//import io.github.skyious.oas.data.model.AppInfo
//import io.github.skyious.oas.data.model.SourceType
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.ResponseBody // Import ResponseBody
//import org.json.JSONException
//import org.json.JSONObject
//import java.io.BufferedInputStream
//import java.io.IOException
//import java.util.concurrent.TimeUnit
//import java.util.zip.ZipEntry
//import java.util.zip.ZipInputStream
//
//object FdroidUtils {
//    // Consider defining timeouts for this client if it's used for large downloads
//    private val client = OkHttpClient.Builder()
//        .connectTimeout(30, TimeUnit.SECONDS)
//        .readTimeout(30, TimeUnit.SECONDS)
//        .build()
//
//    private const val DEBUG_PACKAGE_NAME = "com.wirelessalien.zipxtract"
//
//    suspend fun fetchFdroidApps(indexFileUrl: String): List<AppInfo> {
//        Log.d("FdroidUtils", "Attempting to fetch and process F-Droid index from: $indexFileUrl")
//        val appInfoList = mutableListOf<AppInfo>() // Initialize here for broader scope if needed, or inside the success path
//
//        try {
//            val request = Request.Builder().url(indexFileUrl).build()
//            // In coroutines, it's better to use an async call if the library supports it directly
//            // or wrap execute() in withContext(Dispatchers.IO) if not already on a background thread.
//            // However, since fetchFdroidApps is a suspend function, OkHttp's execute() will
//            // be run on the dispatcher this coroutine is using (likely Dispatchers.IO if called from a ViewModel).
//            val response = client.newCall(request).execute()
//
//            if (!response.isSuccessful) {
//                Log.w("FdroidUtils", "Failed to download index: $indexFileUrl, HTTP code: ${response.code}")
//                response.close() // Ensure response is closed on failure too
//                return emptyList()
//            }
//
//            // Obtain responseBody HERE
//            val responseBody: ResponseBody? = response.body
//
//            if (responseBody == null) {
//                Log.w("FdroidUtils", "Response body is null for $indexFileUrl")
//                response.close() // Ensure response is closed
//                return emptyList()
//            }
//
//            // Now use responseBody within its scope
//            responseBody.use { rb -> // .use will auto-close the responseBody
//                if (indexFileUrl.endsWith(".jar", ignoreCase = true)) {
//                    Log.d("FdroidUtils", "Processing as JAR file: $indexFileUrl")
//                    // appInfoList is already defined above
//                    try {
//                        rb.byteStream().use { jarByteStream ->
//                            ZipInputStream(BufferedInputStream(jarByteStream)).use { zipInputStream ->
//                                var ze: ZipEntry?
//                                while (zipInputStream.nextEntry.also { ze = it } != null) {
//                                    val entry = ze ?: continue
//                                    if (entry.name == "index-v1.json") {
//                                        Log.d("FdroidUtils", "Found index-v1.json in JAR")
//                                        val indexJsonString = zipInputStream.bufferedReader().readText()
//
//                                        // --------------- LOG SNIPPET IF DEBUG PACKAGE IS IN THE RAW STRING ---------------
//                                        if (indexJsonString.contains(DEBUG_PACKAGE_NAME)) {
//                                            Log.d("FdroidUtils_DebugPkg", "DEBUG_PACKAGE_NAME '$DEBUG_PACKAGE_NAME' found in raw indexJsonString.")
//                                            Log.d("FdroidUtils_DebugPkg", "Full index-v1.json content (or part containing the debug package):")
//                                            val startIdx = indexJsonString.indexOf(DEBUG_PACKAGE_NAME)
//                                            val subStringAroundPackage = if (startIdx != -1) {
//                                                val effectiveStart = (startIdx - 500).coerceAtLeast(0)
//                                                val effectiveEnd = (startIdx + 2000).coerceAtMost(indexJsonString.length)
//                                                indexJsonString.substring(effectiveStart, effectiveEnd)
//                                            } else {
//                                                "DEBUG_PACKAGE_NAME not found in indexJsonString snippet search after initial check."
//                                            }
//                                            Log.d("FdroidUtils_DebugPkg", "...JSON around $DEBUG_PACKAGE_NAME...\n$subStringAroundPackage\n...END JSON SNIPPET...")
//                                        }
//                                        // --------------- END LOG SNIPPET -------------------------------------
//
//                                        try {
//                                            val rootJson = JSONObject(indexJsonString)
//                                            Log.d("FdroidUtils", "Successfully parsed indexJsonString into rootJson.")
//
//                                            val rootKeys = mutableListOf<String>()
//                                            rootJson.keys().forEach { key -> rootKeys.add(key) }
//                                            Log.d("FdroidUtils", "Keys in rootJson: ${rootKeys.joinToString()}")
//
//                                            if (rootJson.has("repo")) {
//                                                Log.d("FdroidUtils", "rootJson has 'repo' object: ${rootJson.optJSONObject("repo")?.toString(2)}")
//                                            }
//
//                                            val appsArray = rootJson.optJSONArray("apps")
//                                            if (appsArray == null) {
//                                                Log.e("FdroidUtils", "'apps' JSONArray is NULL in rootJson. Cannot proceed with app parsing.")
//                                                Log.e("FdroidUtils", "Structure of rootJson (first 2000 chars):\n${rootJson.toString(2).take(2000)}")
//                                                // No need to return emptyList() here yet, as we need to break from the while loop first
//                                                // and then the function will return the current appInfoList (which would be empty)
//                                            } else {
//                                                Log.d("FdroidUtils", "'apps' JSONArray found with length: ${appsArray.length()}")
//                                                val repoBaseUrl = indexFileUrl.substringBeforeLast('/') + "/"
//
//                                                Log.d("FdroidUtils", "Starting to iterate through appsArray (length: ${appsArray.length()})")
//                                                for (i in 0 until appsArray.length()) {
//                                                    val appObject = appsArray.getJSONObject(i)
//                                                    val packageName = appObject.optString("packageName")
//
//                                                    val isDebugTargetPackage = packageName == DEBUG_PACKAGE_NAME
//                                                    if (isDebugTargetPackage) {
//                                                        Log.d("FdroidUtils_DebugPkg", "--- Iterating: Found DEBUG_PACKAGE_NAME: $packageName ---")
//                                                        Log.d("FdroidUtils_DebugPkg", "Full appObject JSON for $packageName:\n${appObject.toString(4)}")
//                                                        val keysInAppObject = mutableListOf<String>()
//                                                        appObject.keys().forEach { key -> keysInAppObject.add(key) }
//                                                        Log.d("FdroidUtils_DebugPkg", "Keys available in appObject for $packageName: ${keysInAppObject.joinToString()}")
//                                                    }
//
//                                                    // TODO: UNCOMMENT AND COMPLETE YOUR AppInfo CREATION LOGIC HERE
//                                                    // Example:
//
//                                                    val name = appObject.optString("name") // This needs proper localized name extraction
//                                                    val apkName = appObject.optString("apkName") // This needs robust extraction (from packages or template)
//                                                    if (packageName.isNotEmpty() && name.isNotEmpty() && apkName.isNotEmpty()) {
//                                                        val appInfo = AppInfo(
//                                                            id = packageName,
//                                                            name = name,
//                                                            packageName = packageName,
//                                                            // ... other fields ...
//                                                            source = SourceType.FDROID
//                                                        )
//                                                        appInfoList.add(appInfo)
//                                                    }
//
//
//                                                }
//                                                Log.d("FdroidUtils", "Finished iterating through appsArray.")
//                                            }
//                                        } catch (e: JSONException) {
//                                            Log.e("FdroidUtils", "Failed to parse index-v1.json or specific appObject", e)
//                                            if (indexJsonString.contains(DEBUG_PACKAGE_NAME)) {
//                                                Log.e("FdroidUtils_DebugPkg", "JSONException occurred while processing index/app for $DEBUG_PACKAGE_NAME", e)
//                                            }
//                                        }
//                                        break // Found and processed index-v1.json, exit while loop
//                                    }
//                                }
//                            }
//                        }
//                    } catch (e: IOException) {
//                        Log.e("FdroidUtils", "IOException during JAR processing for $indexFileUrl", e)
//                        // This will return the (likely empty) appInfoList
//                    }
//                    Log.d("FdroidUtils", "Finished processing JAR for $indexFileUrl, found ${appInfoList.size} apps.")
//                    return appInfoList // Return apps found from JAR
//                } else if (indexFileUrl.endsWith(".json", ignoreCase = true)) {
//                    Log.d("FdroidUtils", "Processing as plain JSON file: $indexFileUrl (Not fully implemented in this snippet)")
//                    // TODO: Implement parsing for plain .json index files if needed
//                    // val indexJsonString = rb.source().readString(Charsets.UTF_8)
//                    // ... then parse indexJsonString similar to how it's done for the JAR's index-v1.json ...
//                    return emptyList() // Placeholder
//                } else {
//                    Log.w("FdroidUtils", "Unsupported file type for index: $indexFileUrl")
//                    return emptyList()
//                }
//            } // responseBody.use {} closes the responseBody
//        } catch (e: IOException) {
//            Log.e("FdroidUtils", "IOException during F-Droid fetch for $indexFileUrl", e)
//            return emptyList()
//        } catch (e: Exception) { // Catch any other unexpected errors
//            Log.e("FdroidUtils", "Generic exception during F-Droid fetch for $indexFileUrl", e)
//            return emptyList()
//        }
//        // Fallback, though paths above should return earlier
//        return appInfoList
//    }
//}
//
//// Ensure you are using a configured OkHttpClient if needed, e.g.:
//// private val client = OkHttpClient.Builder()
////    .connectTimeout(15, TimeUnit.SECONDS) // Longer for potentially large index files
////    .readTimeout(15, TimeUnit.SECONDS)
////    .build()
//
//

//
//import android.util.Log
//import io.github.skyious.oas.data.model.AppInfo
//import io.github.skyious.oas.data.model.SourceType
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import org.json.JSONException
//import org.json.JSONObject
//import java.io.BufferedInputStream
//import java.io.IOException
//import java.util.concurrent.TimeUnit
//import java.util.zip.ZipEntry
//import java.util.zip.ZipInputStream
//
//object FdroidUtils {
//    private val client = OkHttpClient()
//
//    /**
//     * Given an F-Droid repo base URL (e.g. https://f-droid.org/repo),
//     * download index.jar and return a list of AppInfo.
//     */
//    // Inside FdroidUtils.kt
//// suspend fun fetchFdroidApps(repoFileUrl: String): List<AppInfo> { // Renamed for clarity
//    suspend fun fetchFdroidApps(indexFileUrl: String): List<AppInfo> {
//        Log.d("FdroidUtils", "Attempting to fetch and process F-Droid index from: $indexFileUrl")
//        val client = OkHttpClient() // Or your preferred HTTP client
//
//        try {
//            val request = Request.Builder().url(indexFileUrl).build()
//            val response = client.newCall(request).execute() // Use async version in coroutines
//
//            if (!response.isSuccessful) {
//                Log.w("FdroidUtils", "Failed to download index: $indexFileUrl, HTTP code: ${response.code}")
//                return emptyList()
//            }
//
//            val responseBody = response.body ?: return emptyList()
//
//            // === Key Change Here: Process the downloaded responseBody directly ===
//            // It's either a JSON string or bytes of a JAR file
//
//            // Inside FdroidUtils.fetchFdroidApps, in the .jar block:
//
//            if (indexFileUrl.endsWith(".jar", ignoreCase = true)) {
//                Log.d("FdroidUtils", "Processing as JAR file: $indexFileUrl")
//                val appInfoList = mutableListOf<AppInfo>()
//                try {
//                    responseBody.byteStream().use { jarByteStream ->
//                        ZipInputStream(BufferedInputStream(jarByteStream)).use { zipInputStream ->
//                            var ze: ZipEntry?
//                            while (zipInputStream.nextEntry.also { ze = it } != null) {
//                                val entry = ze ?: continue
//                                if (entry.name == "index-v1.json") {
//                                    Log.d("FdroidUtils", "Found index-v1.json in JAR")
//                                    // Read the content of index-v1.json
//                                    // For simplicity, reading to string. For large files, stream processing is better.
//                                    val indexJsonString = zipInputStream.bufferedReader().readText()
//
//                                    // --- PARSE JSON HERE ---
//                                    // Example using org.json:
//                                    try {
//                                        val rootJson = JSONObject(indexJsonString)
//                                        val repoBaseUrl = indexFileUrl.substringBeforeLast('/') + "/" // e.g., "https://f-droid.org/repo/"
//                                        val appsArray = rootJson.optJSONArray("apps")
//                                        if (appsArray != null) {
//                                            for (i in 0 until appsArray.length()) {
//
//
//                                                // Inside your loop through appsArray:
//                                                val appObject = appsArray.getJSONObject(i)
//                                                val packageName = appObject.optString("packageName")
//
//                                                // --- Revised Name Extraction ---
//                                                var name = ""
//                                                val localizedObject = appObject.optJSONObject("localized")
//                                                if (localizedObject != null) {
//                                                    // Try for "en-US" first as a common default
//                                                    val enUsDetails = localizedObject.optJSONObject("en-US")
//                                                    if (enUsDetails != null) {
//                                                        name = enUsDetails.optString("name")
//                                                    }
//                                                    // Fallback: If "en-US" name is empty, you could try to get the first available name
//                                                    // from any locale, or use packageName as a last resort.
//                                                    if (name.isEmpty()) {
//                                                        val localeKeys = localizedObject.keys()
//                                                        while (localeKeys.hasNext()) {
//                                                            val localeKey = localeKeys.next()
//                                                            val localeDetails = localizedObject.optJSONObject(localeKey)
//                                                            if (localeDetails != null) {
//                                                                val localeName = localeDetails.optString("name")
//                                                                if (localeName.isNotEmpty()) {
//                                                                    name = localeName
//                                                                    break // Found a name
//                                                                }
//                                                            }
//                                                        }
//                                                    }
//                                                }
//                                                // Last resort if no localized name is found at all
//                                                if (name.isEmpty()) {
//                                                    // name = packageName // Or handle as an error/skip if name is strictly required
//                                                    Log.w("FdroidUtils", "App '$packageName' has no 'name' in localized section. Consider a fallback.")
//                                                }
//                                                // --- End Revised Name Extraction ---
//
//                                                val summary = appObject.optString("summary") // This might also be better taken from localized section
//                                                val iconPath = appObject.optString("icon")
//                                                val suggestedVersionCode = appObject.optString("suggestedVersionCode") // e.g., "60"
//
//                                                // --- apkName Extraction (Still relies on the 'packages' object from the PARENT JSON) ---
//                                                // This part of the logic remains the same as previously advised,
//                                                // assuming 'appObject' here is an element from the "apps" array which
//                                                // ALSO contains a "packages" JSONObject.
//                                                var apkName = ""
//                                                if (suggestedVersionCode.isNotEmpty()) {
//                                                    val packagesObject = appObject.optJSONObject("packages") // THIS IS CRITICAL
//                                                    if (packagesObject != null) {
//                                                        val versionPackageDetails = packagesObject.optJSONObject(suggestedVersionCode)
//                                                        if (versionPackageDetails != null) {
//                                                            apkName = versionPackageDetails.optString("apkName")
//                                                        } else {
//                                                            Log.w("FdroidUtils", "No package details found for suggestedVersionCode '$suggestedVersionCode' in app '$packageName'")
//                                                        }
//                                                    } else {
//                                                        // This is the problematic case if the snippet you provided is the *entire* appObject
//                                                        // and there's no "packages" field alongside "localized", "icon", etc.
//                                                        // This would mean the overall structure of index-v1.json is different than assumed,
//                                                        // or the 'appObject' you're working with is incomplete.
//                                                        Log.w("FdroidUtils", "App '$packageName' is missing the 'packages' object entirely.")
//                                                    }
//                                                }
//                                                // ---
//
//                                                val author = appObject.optString("authorName", appObject.optString("authorEmail", "Unknown"))
//
//
//                                                if (packageName.isNotEmpty() && name.isNotEmpty() && apkName.isNotEmpty()) {
//                                                    val iconUrl = if (iconPath.isNotEmpty()) repoBaseUrl + iconPath else ""
//                                                    val downloadUrl = if (apkName.isNotEmpty()) repoBaseUrl + apkName else ""
//
//                                                    appInfoList.add(
//                                                        AppInfo(
//                                                            id = packageName,
//                                                            name = name,
//                                                            packageName = packageName,
//                                                            author = author,
//                                                            summary = summary, // Consider localized summary as well
//                                                            description = appObject.optString("description"),
//                                                            logoUrl = iconUrl,
//                                                            downloadUrl = downloadUrl,
//                                                            version = appObject.optString("suggestedVersionName", suggestedVersionCode), // Use suggestedVersionName if available
//                                                            source = SourceType.FDROID, // Corrected
//                                                            // ... other fields
//                                                        )
//                                                    )
//                                                } else {
//                                                    Log.w("FdroidUtils", "Skipping app due to missing essential fields. Name: '$name', Package: '$packageName', APKName: '$apkName', SuggestedVersionCode: '$suggestedVersionCode'")
//                                                }
//                                            }
//                                        }
//                                    } catch (e: JSONException) {
//                                        Log.e("FdroidUtils", "Failed to parse index-v1.json", e)
//                                    }
//                                    // --- END JSON PARSING ---
//                                    break // Found the index, no need to check other entries
//                                }
//                            }
//                        }
//                    }
//                } catch (e: IOException) {
//                    Log.e("FdroidUtils", "IOException during JAR processing for $indexFileUrl", e)
//                    return emptyList() // Return empty on error
//                }
//                Log.d("FdroidUtils", "Finished processing JAR for $indexFileUrl, found ${appInfoList.size} apps.")
//                return appInfoList
//
//            } else if (indexFileUrl.endsWith(".json", ignoreCase = true)) {
//                // ... your existing JSON processing logic (which also needs implementation)
//                Log.d("FdroidUtils", "Processing as JSON file: $indexFileUrl")
//                val jsonString = responseBody.string() // Or responseBody.charStream() for streaming
//                // Similar parsing logic as above, but directly on jsonString
//                Log.w("FdroidUtils", "JSON processing not fully implemented in this example yet.")
//                return emptyList() // Placeholder
//            } else {
//                Log.w("FdroidUtils", "Unsupported file type for index: $indexFileUrl")
//                return emptyList()
//            }
//
//        } catch (e: IOException) {
//            Log.e("FdroidUtils", "IOException during F-Droid fetch for $indexFileUrl", e)
//            return emptyList()
//        } catch (e: Exception) { // Catch other potential exceptions during parsing etc.
//            Log.e("FdroidUtils", "Generic exception during F-Droid fetch for $indexFileUrl", e)
//            return emptyList()
//        }
//    }
//
//}
//
//
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
