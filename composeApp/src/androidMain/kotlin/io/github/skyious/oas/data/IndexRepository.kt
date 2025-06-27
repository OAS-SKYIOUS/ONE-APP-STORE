package io.github.skyious.oas.data

import android.content.Context
import android.util.Log
import io.github.skyious.oas.data.model.AppDetail
import io.github.skyious.oas.data.model.AppInfo
import io.github.skyious.oas.data.model.FDroidRepo
import io.github.skyious.oas.data.model.SourceType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.InputStream
import java.net.URI
import java.util.zip.ZipInputStream

class IndexRepository(
    private val context: Context,
    private val settingsRepo: SettingsRepository
) {
    private val okHttpClient = OkHttpClient()
    private val fdroidMutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }
    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    // Caching properties
    private val CACHE_EXPIRATION_MS = 24 * 60 * 60 * 1000L // 24 hours
    private var appListCache: List<AppInfo>? = null
    private val appListMutex = Mutex()

    // Cache files
    private val defaultCacheFile = File(context.cacheDir, "index.csv")
    private val fdroidCacheDir = File(context.cacheDir, "fdroid")

    //region F-Droid Data Classes
    @Serializable
    private data class FdroidRepoInfo(
        val timestamp: Long
    )

    @Serializable
    private data class FdroidLocalizedApp(
        val name: String? = null,
        val summary: String? = null,
        val description: String? = null,
        val icon: String? = null,
        val phoneScreenshots: List<String> = emptyList(),
        val whatsNew: String? = null
    )

    @Serializable
    private data class FdroidPackageV1(
        @SerialName("versionName") val versionName: String? = null,
        @SerialName("versionCode") val versionCode: Long? = null,
        @SerialName("apkName") val apkName: String? = null,
        @SerialName("hash") val hash: String? = null,
        @SerialName("size") val size: Long? = null,
        @SerialName("whatsNew") val whatsNew: JsonElement? = null, // Can be object or string
        val icon: String? = null
    )

    @Serializable
    private data class FdroidAppV1(
        @SerialName("packageName") val packageName: String,
        @SerialName("icon") val icon: String? = null,
        val localized: Map<String, FdroidLocalizedApp> = emptyMap(),
        val categories: List<String> = emptyList(),
        val authorName: String? = null,
        val authorEmail: String? = null,
        val license: String? = null,
        val sourceCode: String? = null,
        val issueTracker: String? = null,
        val translation: String? = null,
        val webSite: String? = null,
        @SerialName("added") val added: Long,
        @SerialName("lastUpdated") val lastUpdated: Long
    )

    @Serializable
    private data class FdroidIndexV1(
        val repo: FdroidRepoInfo,
        val apps: List<FdroidAppV1> = emptyList(),
        val packages: Map<String, List<FdroidPackageV1>> = emptyMap()
    )
    //endregion

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val repoAddress = "https://f-droid.org/repo"
    private val repoName = "F-Droid"

    init {
        coroutineScope.launch {
            loadInitialData()
        }
    }

    private suspend fun loadInitialData() {
        // Load apps from cache on startup. This will also populate categories.
        getApps(forceRefresh = false)
    }

    suspend fun getApps(forceRefresh: Boolean = false): List<AppInfo> = appListMutex.withLock {
        if (forceRefresh) {
            appListCache = null // Invalidate memory cache
            _categories.value = emptyList()
        }

        // Return from memory cache if available
        appListCache?.let { return it }

        // Load from sources if memory cache is empty
        val fdroidApps = getFdroidApps(forceRefresh)
        val defaultApps = getDefaultApps(forceRefresh)
        val customApps = getCustomApps(forceRefresh)
        val combinedApps = (fdroidApps + defaultApps + customApps).distinctBy { it.id }

        appListCache = combinedApps // Populate memory cache
        return combinedApps
    }

    suspend fun forceRefresh() {
        getApps(forceRefresh = true)
    }

    private suspend fun getFdroidApps(forceRefresh: Boolean): List<AppInfo> = fdroidMutex.withLock {
        val includeFdroid = settingsRepo.includeFDroidFlow.first()
        if (!includeFdroid) return@withLock emptyList()

        // Use a default, hardcoded F-Droid repo list as it's not provided by SettingsRepository
        val enabledRepos = listOf(
            FDroidRepo(name = "FDroid", address = "https://f-droid.org/repo")
        )

        return coroutineScope {
            enabledRepos.map {
                async { fetchAndParseFdroidRepo(it, forceRefresh) }
            }.awaitAll().flatten()
        }
    }

    private suspend fun fetchAndParseFdroidRepo(repo: FDroidRepo, forceRefresh: Boolean): List<AppInfo> {
        if (!fdroidCacheDir.exists()) fdroidCacheDir.mkdirs()
        val cacheFile = File(fdroidCacheDir, "${repo.name}-index.json")

        val isCacheValid = cacheFile.exists() && cacheFile.length() > 0 && (System.currentTimeMillis() - cacheFile.lastModified() < CACHE_EXPIRATION_MS)

        val jsonString = if (forceRefresh || !isCacheValid) {
            val jarBytes = downloadRaw(repo.address + "/index-v1.jar")
            if (jarBytes == null) {
                Log.e("FDROID", "Failed to download index-v1.jar for ${repo.name}, using stale cache if available.")
                return if (cacheFile.exists()) parseFdroidIndexV1Json(cacheFile.readText(), repo) else emptyList()
            }
            val extractedJson = extractJsonFromJar(jarBytes.inputStream())
            if (extractedJson != null) {
                cacheFile.writeText(extractedJson)
                extractedJson
            } else {
                Log.e("FDROID", "Failed to extract index-v1.json from jar for ${repo.name}, using stale cache if available.")
                if (cacheFile.exists()) cacheFile.readText() else ""
            }
        } else {
            cacheFile.readText()
        }

        if (jsonString.isBlank()) return emptyList()
        return parseFdroidIndexV1Json(jsonString, repo)
    }

    private fun extractJsonFromJar(inputStream: InputStream): String? {
        ZipInputStream(inputStream).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (entry.name == "index-v1.json") {
                    return zis.bufferedReader().readText()
                }
                entry = zis.nextEntry
            }
        }
        return null
    }

    private fun parseFdroidIndexV1Json(jsonString: String, repo: FDroidRepo): List<AppInfo> {
        val index = try {
            json.decodeFromString<FdroidIndexV1>(jsonString)
        } catch (e: Exception) {
            Log.e("FDROID_MAPPER", "Failed to decode F-Droid index-v1 JSON", e)
            return emptyList()
        }

        val allCategories = index.apps.map { it.categories }.flatten().distinct()
        _categories.update { (it + allCategories).distinct().sorted() }

        return index.apps.mapNotNull { fdroidApp ->
            val appPackages = index.packages[fdroidApp.packageName]
            if (appPackages.isNullOrEmpty()) return@mapNotNull null

            val bestPackage = appPackages.maxByOrNull { it.versionCode ?: -1L }
            if (bestPackage == null || bestPackage.apkName == null) return@mapNotNull null

            val bestLocale = when {
                "en-US" in fdroidApp.localized.keys -> "en-US"
                else -> fdroidApp.localized.keys.firstOrNull()
            }
            val localizedApp = bestLocale?.let { fdroidApp.localized[it] }

            val iconName = localizedApp?.icon ?: fdroidApp.icon ?: bestPackage.icon
            var iconUrl = iconName?.let { name ->
                if (name.contains('/')) {
                    resolveUrl(repo.address, name)
                } else {
                    resolveUrl(repo.address, "icons-640/$name")
                }
            }

            if (iconUrl == null && bestLocale != null) {
                iconUrl = resolveUrl(repo.address, "${fdroidApp.packageName}/$bestLocale/icon.png")
            }

            Log.d("FDROID_URL_DEBUG", "App: ${fdroidApp.packageName}, Author: ${fdroidApp.authorName ?: fdroidApp.authorEmail}, IconName: $iconName, Final URL: $iconUrl")

            val screenshotUrls = if (bestLocale != null && localizedApp?.phoneScreenshots != null) {
                localizedApp.phoneScreenshots.mapNotNull { screenshotName ->
                    resolveUrl(repo.address, "$bestLocale/phoneScreenshots/$screenshotName")
                }
            } else {
                emptyList()
            }

            val changelog = when (val whatsNew = bestPackage.whatsNew) {
                is JsonObject -> whatsNew["en-US"]?.jsonPrimitive?.content ?: ""
                is JsonPrimitive -> whatsNew.content
                else -> ""
            }

            val appInfo = AppInfo(
                id = fdroidApp.packageName,
                name = localizedApp?.name ?: fdroidApp.packageName,
                packageName = fdroidApp.packageName,
                author = fdroidApp.authorName ?: fdroidApp.authorEmail,
                summary = localizedApp?.summary,
                description = localizedApp?.description,
                logoUrl = iconUrl,
                downloadUrl = bestPackage.apkName?.let { "${repo.address}/$it" },
                version = bestPackage.versionName,
                source = SourceType.FDROID.name,
                images = screenshotUrls,
                changelog = localizedApp?.whatsNew, // Use whatsNew from localized data
                sourceCodeUrl = fdroidApp.sourceCode,
                websiteUrl = fdroidApp.webSite,
                license = fdroidApp.license,
                lastUpdated = fdroidApp.lastUpdated,
                dateAdded = fdroidApp.added,
                categories = fdroidApp.categories,
                suggestedVersionCode = bestPackage.versionCode?.toString(),
                apkName = bestPackage.apkName
            )
            appInfo
        }.filterNotNull()
    }

    // --- Default & Custom Source Handling ---
    private suspend fun getDefaultApps(forceRefresh: Boolean): List<AppInfo> {
        val isCacheValid = defaultCacheFile.exists() && (System.currentTimeMillis() - defaultCacheFile.lastModified() < CACHE_EXPIRATION_MS)

        val csvText = if (forceRefresh || !isCacheValid) {
            downloadAndCacheDefaultCsv()
        } else {
            defaultCacheFile.readText()
        }

        if (csvText.isNullOrBlank()) {
            // Fallback to stale cache if download failed or result is empty
            if (defaultCacheFile.exists()) {
                return parseCsv(defaultCacheFile.readText(), SourceType.DEFAULT)
            }
            return emptyList()
        }

        return parseCsv(csvText, SourceType.DEFAULT)
    }

    private suspend fun getCustomApps(forceRefresh: Boolean): List<AppInfo> {
        val allowOtherSources = settingsRepo.allowOtherSourcesFlow.first()
        if (!allowOtherSources) return emptyList()
        val urls = settingsRepo.customSourceUrlsFlow.first()
        return fetchAllCustomSources(urls)
    }

    private suspend fun downloadAndCacheDefaultCsv(): String? {
        return try {
            val text = fetchRawText("https://raw.githubusercontent.com/SKYIOUS/index-repo-oneappstore/refs/heads/main/apps.one")
            if (text?.isNotBlank() == true) {
                defaultCacheFile.writeText(text)
            }
            text
        } catch (e: Exception) {
            Log.e("IndexRepo", "Failed to download default CSV", e)
            null
        }
    }

    private fun parseCsv(csv: String, sourceType: SourceType): List<AppInfo> {
        val delimiter = if (csv.contains("|")) "\\|" else ","
        return csv.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .mapNotNull { line ->
                val parts = line.split(Regex(delimiter))
                if (parts.size >= 4) {
                    val name = parts[0].trim()
                    AppInfo(
                        id = "${sourceType.name}_$name",
                        name = name,
                        author = parts[1].trim(),
                        logoUrl = parts[2].trim(),
                        configUrl = parts[3].trim(),
                        source = sourceType.name
                    )
                } else null
            }.toList()
    }

    private suspend fun fetchAllCustomSources(urls: List<String>): List<AppInfo> = coroutineScope {
        urls.map { url ->
            async {
                try {
                    val text = fetchRawText(url) ?: ""
                    parseCsv(text, SourceType.CUSTOM)
                } catch (e: Exception) {
                    Log.e("IndexRepo_Custom", "Failed to fetch custom source: $url", e)
                    emptyList<AppInfo>()
                }
            }
        }.awaitAll().flatten()
    }

    // --- Network & Utility Helpers ---
    private suspend fun fetchRawText(url: String): String? {
        return downloadRaw(url)?.toString(Charsets.UTF_8)
    }

    private suspend fun downloadRaw(url: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.bytes()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun resolveUrl(base: String, path: String?): String? {
        if (path.isNullOrBlank()) return null
        val uri = try { URI(path) } catch (e: Exception) { null }
        if (uri?.isAbsolute == true) {
            return path // It's already a full URL
        }

        // It's a relative path, so join it with the base URL.
        val normalizedBase = if (base.endsWith('/')) base else "$base/"
        return normalizedBase + path.removePrefix("/")
    }

    suspend fun fetchAppDetail(appId: String): AppDetail? = withContext(Dispatchers.IO) {
        // First, find the app in the existing app list to get its full AppInfo.
        val appInfo = getApps().find { it.id == appId || it.packageName == appId } ?: return@withContext null

        // If the source is F-Droid, we already have all the details in AppInfo.
        if (appInfo.source == SourceType.FDROID.name) {
            return@withContext AppDetail(
                id = appInfo.id,
                name = appInfo.name,
                packageName = appInfo.packageName,
                author = appInfo.author,
                summary = appInfo.summary,
                description = appInfo.description,
                logoUrl = appInfo.logoUrl,
                downloadUrl = appInfo.downloadUrl ?: "", // AppDetail requires non-null
                version = appInfo.version,
                source = SourceType.FDROID,
                images = appInfo.images ?: emptyList(),
                changelog = appInfo.changelog,
                otherFields = mapOf(
                    "sourceCodeUrl" to appInfo.sourceCodeUrl,
                    "websiteUrl" to appInfo.websiteUrl,
                    "license" to appInfo.license,
                    "lastUpdated" to appInfo.lastUpdated,
                    "dateAdded" to appInfo.dateAdded,
                    "categories" to appInfo.categories
                ).filterValues { it != null }
            )
        }

        // For other sources, fetch the YAML file from metadataUrl.
        val detailUrl = appInfo.metadataUrl ?: return@withContext null

        return@withContext try {
            val yamlContent = okHttpClient.newCall(Request.Builder().url(detailUrl).build()).execute().body?.string()
            if (yamlContent != null) {
                parseYamlToAppDetail(yamlContent)
            } else {
                Log.e("IndexRepo_Detail", "YAML content was null for $appId")
                null
            }
        } catch (e: Exception) {
            Log.e("IndexRepo_Detail", "Failed to fetch or parse app detail for $appId", e)
            null
        }
    }

    fun parseYamlToAppDetail(yamlContent: String): AppDetail {
        val yaml = Yaml()
        val data = yaml.load<Map<String, Any>>(yamlContent)

        // Extract main fields, converting keys to lowercase for case-insensitivity
        val lowerCaseData = data.mapKeys { it.key.lowercase(java.util.Locale.getDefault()) }

        val name = lowerCaseData["name"] as? String
        val author = lowerCaseData["author"] as? String
        val summary = lowerCaseData["summary"] as? String
        val description = lowerCaseData["description"] as? String
        val logoUrl = lowerCaseData["logourl"] as? String
        val downloadUrl = lowerCaseData["downloadurl"] as? String ?: ""
        val version = lowerCaseData["version"] as? String
        val changelog = lowerCaseData["changelog"] as? String
        val images = (lowerCaseData["images"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

        // Collect all other fields into the 'otherFields' map
        val otherFields = lowerCaseData.filter { (key, _) -> key !in AppDetail.MAIN_KEYS }

        return AppDetail(
            name = name,
            author = author,
            summary = summary,
            description = description,
            logoUrl = logoUrl,
            downloadUrl = downloadUrl,
            version = version,
            images = images,
            changelog = changelog,
            otherFields = otherFields
        )
    }
}
