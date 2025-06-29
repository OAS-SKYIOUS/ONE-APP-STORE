package io.github.skyious.oas.fdroid.data.repository

import io.github.skyious.oas.fdroid.domain.model.App
import io.github.skyious.oas.fdroid.domain.model.Repo
import io.github.skyious.oas.fdroid.data.model.remote.IndexV2
import io.github.skyious.oas.fdroid.data.model.remote.toDomain
import io.github.skyious.oas.fdroid.domain.repository.FdroidRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Concrete implementation of the FdroidRepository.
 * This class handles fetching data from a remote F-Droid repository,
 * parsing it, and providing it to the application.
 *
 * @param httpClient The Ktor client used for network operations.
 */
class FdroidRepositoryImpl(private val httpClient: HttpClient) : FdroidRepository {

    // In-memory cache for repositories. A database would be used in a full implementation.
    private val _repos = MutableStateFlow<List<Repo>>(emptyList())

    // In-memory cache for apps. A database would be used in a full implementation.
    private val _apps = MutableStateFlow<List<App>>(emptyList())

    override suspend fun init() {
        // TODO: Initialize repositories from a local data source if necessary.
    }

    override suspend fun updateRepository(repo: Repo) {
        try {
            // F-Droid repositories store their index in a specific JSON file.
            val indexUrl = "${repo.address}/index-v2.json"
            val index = httpClient.get(indexUrl).body<IndexV2>()

            // Map the remote models to our domain models.
            val newApps = index.apps.map { it.toDomain(repo.address) }

            // Update the in-memory cache. This will notify all collectors.
            _apps.value = newApps
        } catch (e: Exception) {
            // In a real app, this would be handled with a more robust error reporting system.
            println("Error updating repository ${repo.name}: ${e.message}")
        }
    }

    override fun getApps(): Flow<List<App>> = _apps.asStateFlow()

    override fun getApp(packageName: String): Flow<App?> {
        return getApps().map { apps ->
            apps.find { it.packageName == packageName }
        }
    }

    override fun getRepositories(): Flow<List<Repo>> = _repos.asStateFlow()

    override suspend fun addRepository(repo: Repo) {
        _repos.value = _repos.value + repo
    }

    override suspend fun deleteRepository(repo: Repo) {
        _repos.value = _repos.value.filter { it.id != repo.id }
    }
}
