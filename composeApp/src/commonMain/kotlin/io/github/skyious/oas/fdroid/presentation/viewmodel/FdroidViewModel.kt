package io.github.skyious.oas.fdroid.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.skyious.oas.fdroid.domain.model.Repo
import io.github.skyious.oas.fdroid.domain.usecase.GetApps
import io.github.skyious.oas.fdroid.domain.usecase.GetRepositories
import io.github.skyious.oas.fdroid.domain.usecase.UpdateRepository
import io.github.skyious.oas.fdroid.presentation.state.FdroidScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel for the F-Droid screen.
 * It orchestrates data flow from the use cases to the UI and handles user events.
 *
 * @param getApps Use case to get the list of applications.
 * @param getRepositories Use case to get the list of repositories.
 * @param updateRepository Use case to refresh a repository.
 */
class FdroidViewModel(
    private val getApps: GetApps,
    private val getRepositories: GetRepositories,
    private val updateRepository: UpdateRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<FdroidScreenState>(FdroidScreenState.Loading)
    val state = _state.asStateFlow()

    init {
        loadContent()
    }

    private fun loadContent() {
        viewModelScope.launch {
            // Combine flows from both use cases to create a single state object.
            combine(getRepositories(), getApps()) { repos, apps ->
                FdroidScreenState.Success(apps, repos)
            }.catch { throwable ->
                _state.value = FdroidScreenState.Error(throwable.message ?: "An unknown error occurred")
            }.collect { successState ->
                _state.value = successState
            }
        }
    }

    /**
     * Handles user events from the UI.
     */
    fun onEvent(event: FdroidEvent) {
        when (event) {
            is FdroidEvent.RefreshRepository -> {
                viewModelScope.launch {
                    _state.value = FdroidScreenState.Loading
                    updateRepository(event.repo)
                    // The flow will automatically emit the new state upon completion.
                }
            }
        }
    }
}

/**
 * Defines the events that can be sent from the UI to the ViewModel.
 */
sealed interface FdroidEvent {
    data class RefreshRepository(val repo: Repo) : FdroidEvent
}