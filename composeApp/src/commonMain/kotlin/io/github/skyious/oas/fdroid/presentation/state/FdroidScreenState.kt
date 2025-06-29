package io.github.skyious.oas.fdroid.presentation.state

import io.github.skyious.oas.fdroid.domain.model.App
import io.github.skyious.oas.fdroid.domain.model.Repo

/**
 * Represents the different states for the F-Droid screen.
 * This sealed class ensures that we handle all possible UI states.
 */
sealed interface FdroidScreenState {
    /** The screen is currently loading data. */
    data object Loading : FdroidScreenState

    /** Data has been successfully loaded. */
    data class Success(
        val apps: List<App>,
        val repos: List<Repo>
    ) : FdroidScreenState

    /** An error occurred while loading data. */
    data class Error(val message: String) : FdroidScreenState
}
