package io.github.skyious.oas.fdroid.domain.usecase

import io.github.skyious.oas.fdroid.domain.model.Repo
import io.github.skyious.oas.fdroid.domain.repository.FdroidRepository

/**
 * Use case to trigger an update of a specific repository.
 *
 * @param fdroidRepository The repository to be updated.
 */
class UpdateRepository(private val fdroidRepository: FdroidRepository) {
    suspend operator fun invoke(repo: Repo) {
        fdroidRepository.updateRepository(repo)
    }
}
