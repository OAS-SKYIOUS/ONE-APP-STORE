package io.github.skyious.oas.data // Adapt to your actual package name

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.zip.ZipInputStream
import io.github.skyious.oas.data.model.AppInfo // Adapt to your actual AppInfo model path
import io.github.skyious.oas.data.model.FDroidRepo
import java.io.ByteArrayInputStream
import java.util.jar.JarInputStream
import kotlin.text.Charsets

object FdroidUtils {

    private const val TAG = "FdroidUtils"
    private const val DEBUG_PACKAGE_NAME = "com.wirelessalien.zipxtract" // For specific package debugging

    suspend fun fetchFdroidApps(repoUrl: String): List<AppInfo> {
        Log.d(TAG, "fetchAndParseFdroidIndex called with URL: $repoUrl")
        return withContext(Dispatchers.IO) {
            val appInfoList = mutableListOf<AppInfo>()
            try {
                // Determine if the URL is a direct index file or a base repo URL
                val indexJsonString = if (repoUrl.endsWith(".jar") || repoUrl.endsWith(".json")) {
                    Log.d(TAG, "Processing direct index file URL: $repoUrl")
                    fetchIndexDirectly(repoUrl)
                } else {
                    Log.d(TAG, "Processing base repo URL: $repoUrl, attempting to find index file.")
                    // Try common index file names for a base repository URL
                    val indexFilesToTry = listOf("index-v1.jar", "index.jar", "index-v1.json", "index.json")
                    var foundIndexJson: String? = null
                    for (indexFile in indexFilesToTry) {
                        val fullIndexPath = if (repoUrl.endsWith("/")) "$repoUrl$indexFile" else "$repoUrl/$indexFile"
                        Log.d(TAG, "Attempting to fetch index: $fullIndexPath")
                        try {
                            foundIndexJson = fetchIndexDirectly(fullIndexPath)
                            if (foundIndexJson != null) {
                                Log.i(TAG, "Successfully fetched and processed index: $fullIndexPath")
                                break
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to fetch or process $fullIndexPath: ${e.message}")
                        }
                    }
                    foundIndexJson
                }

                if (indexJsonString == null) {
                    Log.e(TAG, "Could not retrieve index JSON from $repoUrl or its sub-paths.")
                    return@withContext emptyList()
                }

                Log.d(TAG, "Successfully obtained indexJsonString, length: ${indexJsonString.length}")
                val rootJson = JSONObject(indexJsonString)
                Log.d(TAG, "Successfully parsed indexJsonString into rootJson.")

                val repoObject = rootJson.optJSONObject("repo")
                val repoAddress = repoObject?.optString("address", repoUrl) ?: repoUrl
                val repoBaseUrl = if (repoAddress.endsWith("/")) repoAddress else "$repoAddress/"
                Log.i(TAG, "Using repoBaseUrl: $repoBaseUrl")


                val appsArray = rootJson.optJSONArray("apps")
                if (appsArray == null) {
                    Log.e(TAG, "'apps' JSONArray not found in the index.")
                    return@withContext emptyList()
                }
                Log.d(TAG, "'apps' JSONArray found with length: ${appsArray.length()}")

                val packagesObject = rootJson.optJSONObject("packages")
                val packagesMap = mutableMapOf<String, MutableList<String>>()
                if (packagesObject != null) {
                    for (packageName in packagesObject.keys()) {
                        val packageArray = packagesObject.optJSONArray(packageName)
                        if (packageArray != null) {
                            val packageList = mutableListOf<String>()
                            for (i in 0 until packageArray.length()) {
                                packageList.add(packageArray.optString(i, ""))
                            }
                            packagesMap[packageName] = packageList
                        }
                    }
                }

                for (i in 0 until appsArray.length()) {
                    val appObject = appsArray.getJSONObject(i)
                    val packageName = appObject.optString("packageName", null) ?: continue // Skip if no package name

                    val isDebugTargetPackage = packageName == DEBUG_PACKAGE_NAME

                    if (isDebugTargetPackage) {
                        Log.d("FdroidUtils_DebugPkg", "Processing target package: $packageName")
                        Log.d("FdroidUtils_DebugPkg", "AppObject JSON for $packageName:\n${appObject.toString(2)}")
                    }

                    val localizedObject = appObject.optJSONObject("localized")
                    var appName: String? = null
                    var summary: String? = null
                    var description: String? = null
                    var iconUrl: String? = null
                    val imageList = mutableListOf<String>()

                    if (localizedObject != null) {
                        // Prioritize "en-US", then "en", then the first available locale
                        val preferredLocales = listOf("en-US", "en")
                        var targetLocaleKey: String? = null

                        for (locale in preferredLocales) {
                            if (localizedObject.has(locale)) {
                                targetLocaleKey = locale
                                break
                            }
                        }
                        if (targetLocaleKey == null && localizedObject.keys().hasNext()) {
                            targetLocaleKey = localizedObject.keys().next() // Fallback to the first available locale
                        }

                        if (targetLocaleKey != null) {
                            val localeDetails = localizedObject.optJSONObject(targetLocaleKey)
                            if (localeDetails != null) {
                                appName = localeDetails.optString("name", packageName) // Fallback to package name if name is missing
                                summary = localeDetails.optString("summary", null)
                                description = localeDetails.optString("description", null)

                                val iconFileName = localeDetails.optString("icon", null)
                                if (iconFileName != null) {
                                    val encodedIconFileName = URLEncoder.encode(iconFileName, "UTF-8")
                                    iconUrl = "$repoBaseUrl$packageName/$targetLocaleKey/$encodedIconFileName"
                                    if (isDebugTargetPackage) {
                                        Log.d("FdroidUtils_DebugPkg", "Icon for $packageName: file=$iconFileName, locale=$targetLocaleKey, url=$iconUrl")
                                    }
                                } else {
                                    if (isDebugTargetPackage) {
                                        Log.w("FdroidUtils_DebugPkg", "No 'icon' field in localeDetails for $packageName, locale $targetLocaleKey")
                                    }
                                }

                                // Screenshots (assuming 'triple' or 'featureGraphic' might be used for general images)
                                val screenshotKeys = listOf("phoneScreenshots", "sevenInchScreenshots", "tenInchScreenshots", "tvScreenshots", "wearScreenshots", "featureGraphic", "promoGraphic", "tvBanner")
                                for (key in screenshotKeys) {
                                    val screenshotsArray = localeDetails.optJSONArray(key)
                                    if (screenshotsArray != null) {
                                        for (j in 0 until screenshotsArray.length()) {
                                            val screenshotFileName = screenshotsArray.optString(j, null)
                                            if (screenshotFileName != null) {
                                                val encodedScreenshotFileName = URLEncoder.encode(screenshotFileName, "UTF-8")
                                                val screenshotUrl = "$repoBaseUrl$packageName/$targetLocaleKey/$encodedScreenshotFileName"
                                                imageList.add(screenshotUrl)
                                                if (isDebugTargetPackage) {
                                                    Log.d("FdroidUtils_DebugPkg", "Screenshot for $packageName: file=$screenshotFileName, key=$key, url=$screenshotUrl")
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (isDebugTargetPackage) {
                                    Log.w("FdroidUtils_DebugPkg", "localeDetails is null for $packageName, targetLocaleKey: $targetLocaleKey")
                                }
                            }
                        } else {
                            if (isDebugTargetPackage) {
                                Log.w("FdroidUtils_DebugPkg", "No suitable targetLocaleKey found for $packageName. LocalizedObject: ${localizedObject.toString(2)}")
                            }
                        }
                    } else {
                        // Fallback if no localizedObject at all
                        appName = appObject.optString("name", packageName) // Use global name or package name
                        summary = appObject.optString("summary", null)
                        description = appObject.optString("description", null)
                        val globalIcon = appObject.optString("icon", null)
                        if (globalIcon != null) {
                            // Global icons usually don't have locale folder, often relative to package
                            // or could be a full URL. F-Droid spec usually has icons under localized.
                            // This is a basic fallback, might need more specific logic if global icons are used differently in your sources.
                            if (globalIcon.startsWith("http://") || globalIcon.startsWith("https://")) {
                                iconUrl = globalIcon
                            } else {
                                val encodedGlobalIcon = URLEncoder.encode(globalIcon, "UTF-8")
                                iconUrl = "$repoBaseUrl$packageName/$encodedGlobalIcon" // Guessing path structure
                            }
                            if (isDebugTargetPackage) {
                                Log.d("FdroidUtils_DebugPkg", "Using global icon for $packageName: file=$globalIcon, url=$iconUrl")
                            }
                        }
                        if (isDebugTargetPackage) {
                            Log.w("FdroidUtils_DebugPkg", "No 'localized' object found for $packageName. Falling back to global fields.")
                        }
                    }

                    // If appName is still null after all attempts, use package name
                    if (appName == null) {
                        appName = packageName
                    }

                    // --- Debugging for a specific failing package ---
                    if (iconUrl == null && packageName == "some.failing.package.name") { // Replace with actual failing package name
                        Log.w("FdroidUtils_IconDebug", "Icon is null for $packageName. AppObject JSON:\n${appObject.toString(2)}")
                        Log.w("FdroidUtils_IconDebug", "Localized object for $packageName: ${appObject.optJSONObject("localized")?.toString(2)}")
                    }
                    // --- End Debugging ---


                    val packageList = packagesMap[packageName] ?: emptyList()
                    var latestVersionName: String? = null
                    var downloadUrl: String? = null

                    if (packageList.isNotEmpty()) {
                        // Assuming the first package in the array is the most recent/relevant one
                        // More sophisticated logic might be needed to pick the "best" package
                        val latestPackage = packageList.first() // Or loop to find highest versionCode
                        latestVersionName = latestPackage
                        val apkName = latestPackage
                        if (apkName != null) {
                            val encodedApkName = URLEncoder.encode(apkName, "UTF-8")
                            downloadUrl = "$repoBaseUrl$encodedApkName" // APKs are usually at the repo root + apkName
                        }
                    }

                    val appInfo = AppInfo(
                        id = packageName, // Using packageName as a unique ID
                        packageName = packageName,
                        name = appName ?: packageName, // Ensure name is never null
                        author = repoObject?.optString("name", "Unknown Source"), // F-Droid repo name as author
                        summary = summary,
                        description = description,
                        logoUrl = iconUrl,
                        images = imageList.takeIf { it.isNotEmpty() },
                        downloadUrl = downloadUrl,
                        version = latestVersionName,
                        source = repoUrl, // The URL of the F-Droid repo itself
                        // configUrl, changelog, otherFields, metadataUrl might not be directly available
                        // in basic F-Droid index structure and would need other sources or be left null.
                        configUrl = null,
                        changelog = null,
                        otherFields = null,
                        metadataUrl = null // Could point to a web representation if available
                    )
                    appInfoList.add(appInfo)

                    if (isDebugTargetPackage || (iconUrl == null && packageName == "some.failing.package.name")) {
                        Log.i("FdroidUtils_DebugPkg", "Processed AppInfo for $packageName: $appInfo")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error processing F-Droid index from $repoUrl", e)
                // Return empty list on any major processing error
                return@withContext emptyList()
            }
            Log.i(TAG, "Finished parsing F-Droid index. Found ${appInfoList.size} apps from $repoUrl.")
            appInfoList
        }
    }

    private fun fetchIndexDirectly(fullIndexPath: String): String? {
        return try {
            val urlConnection = URL(fullIndexPath).openConnection() as HttpURLConnection
            urlConnection.connectTimeout = 15000 // 15 seconds
            urlConnection.readTimeout = 15000  // 15 seconds
            Log.d(TAG, "Connecting to: $fullIndexPath")

            if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Successfully connected to $fullIndexPath (HTTP_OK)")
                val inputStream = urlConnection.inputStream
                when {
                    fullIndexPath.endsWith(".jar") -> {
                        Log.d(TAG, "Processing as JAR file: $fullIndexPath")
                        ZipInputStream(inputStream).use { zipInput ->
                            var entry = zipInput.nextEntry
                            while (entry != null) {
                                if (entry.name.equals("index-v1.json", ignoreCase = true) || entry.name.equals("index.json", ignoreCase = true)) {
                                    Log.i(TAG, "Found ${entry.name} in JAR from $fullIndexPath")
                                    val bufferedReader = BufferedReader(InputStreamReader(zipInput))
                                    return bufferedReader.readText()
                                }
                                zipInput.closeEntry()
                                entry = zipInput.nextEntry
                            }
                        }
                        Log.w(TAG, "index-v1.json or index.json not found in JAR: $fullIndexPath")
                        null // JSON not found in JAR
                    }
                    fullIndexPath.endsWith(".json") -> {
                        Log.d(TAG, "Processing as JSON file: $fullIndexPath")
                        BufferedReader(InputStreamReader(inputStream)).use { reader ->
                            reader.readText()
                        }
                    }
                    else -> {
                        Log.w(TAG, "Unsupported file type for index: $fullIndexPath")
                        null
                    }
                }
            } else {
                Log.e(TAG, "HTTP error ${urlConnection.responseCode} while fetching $fullIndexPath")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching or processing index from $fullIndexPath", e)
            null
        }
    }

    fun extractEntryJsonFromJar(jarBytes: ByteArray): String? {
        Log.d("FDroidUtils_Debug", "Starting extraction from JAR bytes (size: ${jarBytes.size})")
        ZipInputStream(ByteArrayInputStream(jarBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                Log.d("FDroidUtils_Debug", "Found JAR entry: ${entry.name}") // Log every entry
                if (entry.name == "entry.json") {
                    Log.i("FDroidUtils_Debug", "Found 'entry.json'. Reading content.")
                    val content = zis.bufferedReader().readText()
                    Log.d("FDroidUtils_Debug", "Successfully extracted 'entry.json' (size: ${content.length})")
                    return content
                }
                entry = zis.nextEntry
            }
        }
        Log.w("FDroidUtils_Debug", "entry.json not found in JAR.")
        return null
    }

    /**
     * Extracts the `index-v1.json` file from the raw bytes of an F-Droid `index-v1.jar` file.
     * The official F-Droid repo now serves a JAR containing this JSON file instead of the old XML file.
     */
    fun extractIndexV1JsonFromJar(jarBytes: ByteArray): String? {
        Log.d("FDroidUtils_Debug", "Attempting to extract index-v1.json from JAR bytes.")
        JarInputStream(ByteArrayInputStream(jarBytes)).use { jis ->
            var ze = jis.nextJarEntry
            while (ze != null) {
                Log.v("FDroidUtils_Debug", "Found JAR entry: ${ze.name}")
                if (ze.name == "index-v1.json") {
                    Log.i("FDroidUtils_Debug", "Found index-v1.json, extracting content.")
                    return jis.bufferedReader(Charsets.UTF_8).readText()
                }
                ze = jis.nextJarEntry
            }
        }
        Log.w("FDroidUtils_Debug", "index-v1.json not found in JAR.")
        return null
    }

    // Deprecated: The official F-Droid repo no longer provides index.xml in its v1 JAR.
    // Kept for reference or compatibility with very old/custom repos.
    /*
    fun extractIndexXmlFromJar(jarBytes: ByteArray): String? {
        Log.d("FDroidUtils_Debug", "Attempting to extract index.xml from JAR bytes.")
        JarInputStream(ByteArrayInputStream(jarBytes)).use { jis ->
            var ze = jis.nextJarEntry
            while (ze != null) {
                Log.d("FDroidUtils_Debug", "Found JAR entry: ${ze.name}")
                if (ze.name == "index.xml") {
                    Log.i("FDroidUtils_Debug", "Found index.xml, extracting content.")
                    return jis.bufferedReader(Charsets.UTF_8).readText()
                }
                ze = jis.nextJarEntry
            }
        }
        Log.w("FDroidUtils_Debug", "index.xml not found in JAR.")
        return null
    }
    */

}