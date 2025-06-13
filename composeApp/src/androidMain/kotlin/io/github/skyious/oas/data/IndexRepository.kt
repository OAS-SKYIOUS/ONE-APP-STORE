package io.github.skyious.oas.data

import android.content.Context
import android.util.Log
import io.github.skyious.oas.data.model.AppDetail
import io.github.skyious.oas.data.model.AppInfo
import io.github.skyious.oas.data.model.SourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.yaml.snakeyaml.Yaml
import java.io.File

class IndexRepository(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val settingsRepo: SettingsRepository
) {
    private val client = OkHttpClient()

    // Default CSV URL (replace with your actual default index URL)
    private val defaultIndexUrl = "https://raw.githubusercontent.com/SKYIOUS/index-repo-oneappstore/refs/heads/main/apps.one"
    private val defaultCacheFile = File(context.filesDir, "index.csv")

    // We may cache custom merged index too, e.g. in a file; omitted here or you can add.

    /**
     * Main entry: get merged list of AppInfo.
     * If forceRefresh is true, re-fetch default CSV and custom sources.
     * Otherwise, read default CSV cache; optionally read custom cache or re-fetch based on policy.
     */
    suspend fun getApps(forceRefresh: Boolean = false): List<AppInfo> = withContext(Dispatchers.IO) {
        // 1. Load default apps
        val defaultCsvText = if (forceRefresh || !defaultCacheFile.exists()) {
            downloadAndCacheDefaultCsv()
        } else {
            defaultCacheFile.readText()
        }
        val defaultApps = parseCsv(defaultCsvText)

        // 2. Check settings for custom sources
        val allowOther = settingsRepository.allowOtherSourcesFlow.first()
        val customApps = if (allowOther) {
            val urls = settingsRepository.customSourceUrlsFlow.first()
            fetchAllCustomSources(urls)
        } else {
            emptyList()
        }

        // 3) load F-Droid if toggled on
        val fdroid = if (settingsRepo.includeFDroidFlow.first()) {
            fetchAllCustomSources(FdroidRepos.ALL)
        } else emptyList()

        // 3. Merge
        val merged = defaultApps + customApps + fdroid
        // TODO: Optionally cache merged index locally.
        merged
    }

    /** Download default CSV and cache it */
    private fun downloadAndCacheDefaultCsv(): String {
        return try {
            val request = Request.Builder().url(defaultIndexUrl).build()
            client.newCall(request).execute().use { resp ->
                val body = resp.body?.string() ?: ""
                defaultCacheFile.writeText(body)
                Log.d("IndexRepo", "Downloaded default CSV, size=${body.length}")
                body
            }
        } catch (e: Exception) {
            Log.e("IndexRepo", "Failed to download default CSV", e)
            ""
        }
    }

    /** Parse CSV or pipe-delimited as before */
    private fun parseCsv(csv: String): List<AppInfo> {
        val delimiter = if (csv.contains("|")) "\\|" else ","
        return csv
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .mapNotNull { line ->
                val parts = line.split(Regex(delimiter))
                if (parts.size >= 4) {
                    AppInfo(
                        name = parts[0].trim(),
                        author = parts[1].trim(),
                        logoUrl = parts[2].trim(),
                        configUrl = parts[3].trim()
                    )
                } else {
                    Log.w("IndexRepo", "Skipping line (unexpected columns): $line")
                    null
                }
            }
            .toList()
    }

    /** For each custom repo URL, fetch apps and accumulate */
    private fun fetchAllCustomSources(urls: List<String>): List<AppInfo> {
        val result = mutableListOf<AppInfo>()
        for (repoUrl in urls) {
            try {
                val apps = fetchFromSingleCustomRepo(repoUrl)
                result += apps
            } catch (e: Exception) {
                Log.e("IndexRepo", "Error fetching custom source $repoUrl", e)
            }
        }
        return result
    }

    /**
     * Fetch apps from a single custom GitHub repo.
     * Conventions:
     * - Repo URL: e.g., https://github.com/username/repo
     * - Default branch: main
     * - listinginfo at: listinginfo/listinginfo.yaml or listinginfo/listinfo.yaml
     * - apps folder at: apps/
     * - Inside each app folder: pick first .yaml/.yml file as metadata
     * - Metadata YAML: map of keys to values; required keys (name, author, downloadUrl, logoUrl) should appear;
     *   which keys to read is determined by listinginfo.yaml mapping positions → key names.
     */
    private fun fetchFromSingleCustomRepo(repoUrl: String): List<AppInfo> {
        val ownerRepo = GitHubUtils.parseOwnerRepo(repoUrl)
        if (ownerRepo == null) {
            Log.w("IndexRepo", "Invalid GitHub URL: $repoUrl")
            return emptyList()
        }
        val (owner, repo) = ownerRepo
        val branch = "main" // or allow customizing later

        // 1. Fetch listinginfo.yaml
        val listingMap = fetchListingInfo(owner, repo, branch) ?: run {
            Log.w("IndexRepo", "No valid listinginfo.yaml in $repoUrl")
            return emptyList()
        }
        // listingMap: Map<Int, String> mapping positions → metadata keys

        // 2. List app folders under 'apps/'
        val appFolders = listGitHubDirectory(owner, repo, "apps", branch)
        if (appFolders.isEmpty()) {
            Log.w("IndexRepo", "'apps/' folder empty or inaccessible in $repoUrl")
            return emptyList()
        }

        val apps = mutableListOf<AppInfo>()
        for (folderName in appFolders) {
            try {
                val metadataMap = fetchAppMetadata(owner, repo, branch, folderName)
                if (metadataMap != null) {
                    // Extract required fields: positions → keys
                    val nameKey = listingMap[1]
                    val authorKey = listingMap[2]
                    val downloadKey = listingMap[3]
                    val logoKey = listingMap[4]
                    if (nameKey != null && authorKey != null && downloadKey != null && logoKey != null) {
                        val nameVal = metadataMap[nameKey]?.toString()?.trim()
                        val authorVal = metadataMap[authorKey]?.toString()?.trim()
                        val downloadVal = metadataMap[downloadKey]?.toString()?.trim()
                        val logoVal = metadataMap[logoKey]?.toString()?.trim()
                        if (!nameVal.isNullOrEmpty() && !authorVal.isNullOrEmpty()
                            && !downloadVal.isNullOrEmpty() && !logoVal.isNullOrEmpty()
                        ) {
                            apps += AppInfo(
                                name = nameVal,
                                author = authorVal,
                                logoUrl = logoVal,
                                configUrl = downloadVal
                            )
                        } else {
                            Log.w("IndexRepo", "Missing required fields in $folderName of $repoUrl")
                        }
                    } else {
                        Log.w("IndexRepo", "listinginfo.yaml does not specify positions 1-4 in $repoUrl")
                    }
                }
            } catch (e: Exception) {
                Log.e("IndexRepo", "Error processing app folder $folderName in $repoUrl", e)
            }
        }
        return apps
    }

    /** Fetch listinginfo.yaml (or listinfo.yaml) and parse into Map<Int, String> */
    private fun fetchListingInfo(owner: String, repo: String, branch: String): Map<Int, String>? {
        val rawBase = GitHubUtils.rawBase(owner, repo, branch)
        // Try both possible file names
        val candidates = listOf("listinginfo/listinginfo.yaml", "listinginfo/listinginfo.yml",
            "listinginfo/listinfo.yaml", "listinginfo/listinfo.yml")
        for (path in candidates) {
            val url = rawBase + path
            val yamlText = fetchRawText(url)
            if (yamlText != null) {
                val mapping = parseListingYaml(yamlText)
                if (!mapping.isNullOrEmpty()) {
                    return mapping
                }
            }
        }
        return null
    }

    /** Fetch text from a raw URL; return null if non-200 or error */
    private fun fetchRawText(url: String): String? {
        return try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { resp ->
                if (resp.isSuccessful) {
                    resp.body?.string()
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("IndexRepo", "Failed to fetch raw text from $url", e)
            null
        }
    }

    /** Parse listinginfo YAML into Map<Int, String> */
    @Suppress("UNCHECKED_CAST")
    private fun parseListingYaml(yamlText: String): Map<Int, String>? {
        return try {
            val yaml = Yaml()
            val data = yaml.load<Any>(yamlText)
            if (data is Map<*, *>) {
                // Keys are probably Int or String representing an integer
                val result = mutableMapOf<Int, String>()
                for ((k, v) in data) {
                    val keyInt = when (k) {
                        is Int -> k
                        is String -> k.toIntOrNull()
                        else -> null
                    }
                    val valueStr = v?.toString()
                    if (keyInt != null && !valueStr.isNullOrBlank()) {
                        result[keyInt] = valueStr
                    }
                }
                result.toSortedMap() // sorted by key if needed
            } else {
                Log.w("IndexRepo", "listinginfo YAML root is not a map")
                null
            }
        } catch (e: Exception) {
            Log.e("IndexRepo", "Failed to parse listinginfo YAML", e)
            null
        }
    }

    /**
     * List directory contents via GitHub API.
     * Returns list of names of subdirectories under given path that have type "dir".
     */
    private fun listGitHubDirectory(owner: String, repo: String, path: String, branch: String): List<String> {
        val url = GitHubUtils.apiContentsUrl(owner, repo, path, branch)
        return try {
            val request = Request.Builder()
                .url(url)
                // Optionally set a User-Agent header to avoid GitHub API rejecting:
                .header("User-Agent", "MyAppStoreApp")
                .build()
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.w("IndexRepo", "GitHub API list contents failed: $url, code=${resp.code}")
                    return emptyList()
                }
                val body = resp.body?.string() ?: return emptyList()
                // Parse JSON array
                val jsonArray = JSONArray(body)
                val dirs = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    val type = item.optString("type")
                    val name = item.optString("name")
                    if (type == "dir" && name.isNotEmpty()) {
                        dirs += name
                    }
                }
                dirs
            }
        } catch (e: Exception) {
            Log.e("IndexRepo", "Error listing GitHub directory $owner/$repo/$path", e)
            emptyList()
        }
    }

    /**
     * For a given app folder under apps/, fetch its metadata YAML and parse into Map<String, Any>.
     * Returns null if no suitable YAML found or parse fails.
     */
    @Suppress("UNCHECKED_CAST")
    private fun fetchAppMetadata(owner: String, repo: String, branch: String, folderName: String): Map<String, Any>? {
        // 1. List files in this folder via GitHub API
        val contents = listGitHubDirectoryContents(owner, repo, "apps/$folderName", branch)
        if (contents.isEmpty()) {
            Log.w("IndexRepo", "No contents in apps/$folderName")
            return null
        }
        // 2. Find first YAML file: *.yaml or *.yml
        val yamlFileName = contents.firstOrNull { it.endsWith(".yaml") || it.endsWith(".yml") }
        if (yamlFileName.isNullOrEmpty()) {
            Log.w("IndexRepo", "No YAML metadata file in apps/$folderName")
            return null
        }
        // 3. Fetch raw YAML text
        val rawUrl = GitHubUtils.rawBase(owner, repo, branch) + "apps/$folderName/$yamlFileName"
        val yamlText = fetchRawText(rawUrl) ?: return null
        // 4. Parse into Map<String, Any>
        return try {
            val yaml = Yaml()
            val data = yaml.load<Any>(yamlText)
            if (data is Map<*, *>) {
                // Convert keys to String, values to Any
                val map = mutableMapOf<String, Any>()
                for ((k, v) in data) {
                    val keyStr = k?.toString()
                    if (!keyStr.isNullOrBlank() && v != null) {
                        map[keyStr] = v
                    }
                }
                map
            } else {
                Log.w("IndexRepo", "App metadata YAML root is not a map in $folderName")
                null
            }
        } catch (e: Exception) {
            Log.e("IndexRepo", "Failed to parse app metadata YAML in $folderName", e)
            null
        }
    }

    /**
     * List directory contents (names) via GitHub API, both files and directories, but here we need file names.
     */
    private fun listGitHubDirectoryContents(owner: String, repo: String, path: String, branch: String): List<String> {
        val url = GitHubUtils.apiContentsUrl(owner, repo, path, branch)
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "MyAppStoreApp")
                .build()
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.w("IndexRepo", "GitHub API list contents failed: $url, code=${resp.code}")
                    return emptyList()
                }
                val body = resp.body?.string() ?: return emptyList()
                val jsonArray = JSONArray(body)
                val names = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    val name = item.optString("name")
                    if (name.isNotEmpty()) {
                        names += name
                    }
                }
                names
            }
        } catch (e: Exception) {
            Log.e("IndexRepo", "Error listing GitHub directory contents $owner/$repo/$path", e)
            emptyList()
        }
    }



    // In IndexRepository or separate AppDetailRepository
    suspend fun fetchAppDetail(appInfo: AppInfo): AppDetail? = withContext(Dispatchers.IO) {
        try {
            val yamlText: String? = when (appInfo.source) {
                SourceType.DEFAULT -> {
                    // configUrl should be non-null
                    val url = appInfo.configUrl ?: return@withContext null
                    fetchRawText(url)
                }
                SourceType.CUSTOM -> {
                    val metaUrl = appInfo.metadataUrl ?: return@withContext null
                    fetchRawText(metaUrl)
                }
            }
            if (yamlText.isNullOrBlank()) return@withContext null

            // Parse YAML into Map<String,Any>
            val yaml = org.yaml.snakeyaml.Yaml()
            val loaded = yaml.load<Any>(yamlText)
            if (loaded !is Map<*, *>) return@withContext null

            // Convert to Map<String,Any>
            val metaMap = mutableMapOf<String, Any>()
            for ((k, v) in loaded) {
                val key = k?.toString()?.trim() ?: continue
                if (v != null) {
                    metaMap[key] = v
                }
            }

            // Extract fields by key names; since different repos may use different keys, we rely on listinginfo for custom,
            // but here for default we assume standard keys in config.one YAML (you define).
            // For default, decide on the expected keys, e.g.: "name", "packageName", "version", "description", "downloadUrl", "logoUrl", "images", "changelog", "author"
            // You can adjust these to match your config.one structure.
            val name = metaMap["name"]?.toString()
                ?: metaMap["appName"]?.toString()
                ?: metaMap["packageName"]?.toString()  // fallback
            val packageName = metaMap["packageName"]?.toString()
            val version = metaMap["version"]?.toString()
            val description = metaMap["description"]?.toString()
            val downloadUrl = metaMap["downloadUrl"]?.toString()
            // maybe config.one uses "download_link" or similar; you can check metaMap keys or use listinginfo for custom
            val logoUrl = metaMap["logoUrl"]?.toString() ?: appInfo.logoUrl
            val author = metaMap["author"]?.toString() ?: appInfo.author

            // images: could be a list under key "images" or "screenshots"
            val images: List<String> = when (val imgs = metaMap["images"]) {
                is List<*> -> imgs.mapNotNull { it?.toString() }
                is String -> listOf(imgs)  // maybe a single URL
                else -> emptyList()
            }
            // changelog: perhaps a string or list. If list, join lines.
            val changelog: String? = when (val ch = metaMap["changelog"]) {
                is List<*> -> ch.mapNotNull { it?.toString() }.joinToString("\n")
                is String -> ch
                else -> null
            }

            // Build AppDetail
            AppDetail(
                name = name,
                packageName = packageName,
                version = version,
                description = description,
                downloadUrl = downloadUrl,
                logoUrl = logoUrl,
                images = images,
                changelog = changelog,
                author = author,
                otherFields = metaMap.filterKeys { key ->
                    // exclude keys already extracted
                    val lower = key.lowercase()
                    lower !in setOf("name", "appname", "packagename", "version", "description", "downloadurl", "logoUrl".lowercase(),
                        "author", "images", "screenshots", "changelog")
                }
            )
        } catch (e: Exception) {
            Log.e("IndexRepo", "Error fetching/parsing AppDetail for ${appInfo.name}", e)
            null
        }
    }

}
