package io.github.skyious.oas.fdroid.domain.repository

import io.github.skyious.oas.fdroid.domain.model.App
import io.github.skyious.oas.fdroid.domain.model.Repo
import kotlinx.coroutines.flow.Flow

interface FdroidRepository {
    suspend fun init()
    suspend fun updateRepository(repo: Repo)
    fun getApps(): Flow<List<App>>
    fun getApp(packageName: String): Flow<App?>
    fun getRepositories(): Flow<List<Repo>>
    suspend fun addRepository(repo: Repo)
    suspend fun deleteRepository(repo: Repo)
}
