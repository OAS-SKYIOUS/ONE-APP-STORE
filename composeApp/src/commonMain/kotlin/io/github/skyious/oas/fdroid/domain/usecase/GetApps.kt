package io.github.skyious.oas.fdroid.domain.usecase

import io.github.skyious.oas.fdroid.domain.model.App
import io.github.skyious.oas.fdroid.domain.repository.FdroidRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get a flow of all applications from the repository.
 *
 * @param fdroidRepository The repository providing the application data.
 */
class GetApps(private val fdroidRepository: FdroidRepository) {
    operator fun invoke(): Flow<List<App>> {
        return fdroidRepository.getApps()
    }
}
