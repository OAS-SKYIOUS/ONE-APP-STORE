package io.github.skyious.oas.data


import android.util.Log
import io.github.skyious.oas.data.model.AppInfo
import io.github.skyious.oas.data.model.SourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.yaml.snakeyaml.Yaml
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

object FdroidUtils {
    private val client = OkHttpClient()

    /**
     * Given an F-Droid repo base URL (e.g. https://f-droid.org/repo),
     * download index.jar and return a list of AppInfo.
     */
    suspend fun fetchFdroidApps(repoUrl: String): List<AppInfo> = withContext(Dispatchers.IO) {
        val result = mutableListOf<AppInfo>()
        try {
            val indexUrl = repoUrl.trimEnd('/') + "/index.jar"
            val req = Request.Builder().url(indexUrl).build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.w("FdroidUtils", "index.jar not found or error: $indexUrl, code=${resp.code}")
                    return@withContext emptyList()
                }
                val bytes = resp.body?.bytes() ?: return@withContext emptyList()
                ZipInputStream(BufferedInputStream(ByteArrayInputStream(bytes))).use { zip ->
                    var entry = zip.nextEntry
                    val yaml = Yaml()
                    while (entry != null) {
                        if (!entry.isDirectory && entry.name.startsWith("metadata/") &&
                            (entry.name.endsWith(".yml") || entry.name.endsWith(".yaml"))
                        ) {
                            val text = zip.readBytes().toString(Charsets.UTF_8)
                            try {
                                @Suppress("UNCHECKED_CAST")
                                val map = yaml.load<Any>(text)
                                if (map is Map<*, *>) {
                                    val name = map["Name"]?.toString()?.takeIf { it.isNotBlank() }
                                    val pkg  = map["PackageName"]?.toString()?.takeIf { it.isNotBlank() }
                                    val icon = map["IconName"]?.toString()?.takeIf { it.isNotBlank() }
                                    val apkName = map["DownloadName"]?.toString()?.takeIf { it.isNotBlank() }
                                    if (name != null && pkg != null && icon != null && apkName != null) {
                                        val logoUrl = "$repoUrl/icon?name=$icon"
                                        val downloadUrl = "$repoUrl/$apkName"
                                        result += AppInfo(
                                            name = name,
                                            author = pkg,
                                            logoUrl = logoUrl,
                                            downloadUrl = downloadUrl,
                                            metadataUrl = indexUrl,
                                            source = SourceType.CUSTOM
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                // skip this entry
                            }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("FdroidUtils", "Error parsing F‑Droid repo $repoUrl: ${e.message}")
        }
        result
    }

}


// FdroidUtils.kt
private val client = OkHttpClient.Builder()
    .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
    .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
    .build()
