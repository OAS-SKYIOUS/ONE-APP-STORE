package io.github.skyious.oas

import io.github.skyious.oas.data.model.AppInfo
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.FileSystem
import okio.Path
import io.ktor.client.call.body

class AppRepository(
    private val httpClient: HttpClient,
    private val fileSystem: FileSystem,
    private val cacheFile: Path,
    private val defaultIndexUrl: String
) {
    /** Returns the cached list if present, else fetches & caches first. */
    fun getApps(): Flow<List<AppInfo>> = flow {
        // 1. If cache exists, read & parse it immediately:
        if (fileSystem.exists(cacheFile)) {
            val text = fileSystem.read(cacheFile) { readUtf8() }
            emit(parseCsv(text))
        }
        // 2. Always try to fetch fresh from defaultIndexUrl:
        try {
            val response = httpClient.get(defaultIndexUrl)
            val csv = response.body<String>()
            fileSystem.write(cacheFile) { writeUtf8(csv) }
            emit(parseCsv(csv))
        } catch (e: Exception) {
            // swallow or emit an error state
        }
    }

    private fun parseCsv(csv: String): List<AppInfo> {
        return csv
            .lineSequence()
            .drop(1) // assume first line is header
            .mapNotNull { line ->
                val cols = line.split(',')
                if (cols.size >= 4) {
                    AppInfo(
                        name = cols[0].trim(),
                        author = cols[1].trim(),
                        logoUrl = cols[2].trim(),
                        configUrl = cols[3].trim()
                    )
                } else null
            }
            .toList()
    }
}
