package io.github.skyious.oas.fdroid.domain.usecase

import io.github.skyious.oas.fdroid.domain.model.Repo
import io.github.skyious.oas.fdroid.domain.repository.FdroidRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get a flow of all configured repositories.
 *
 * @param fdroidRepository The repository providing the repo data.
 */
class GetRepositories(private val fdroidRepository: FdroidRepository) {
    operator fun invoke(): Flow<List<Repo>> {
        return fdroidRepository.getRepositories()
    }
}
