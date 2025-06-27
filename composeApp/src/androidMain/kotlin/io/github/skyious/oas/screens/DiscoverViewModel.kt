package io.github.skyious.oas.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.skyious.oas.data.IndexRepository
import io.github.skyious.oas.data.model.AppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DiscoverUiState {
    data class Success(val apps: List<AppInfo>) : DiscoverUiState
    object Error : DiscoverUiState
    object Loading : DiscoverUiState
}

class DiscoverViewModel(
    private val indexRepository: IndexRepository,
    private val category: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiscoverUiState>(DiscoverUiState.Loading)
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    init {
        loadApps(forceRefresh = false)
    }

    fun loadApps(forceRefresh: Boolean) {
        _uiState.value = DiscoverUiState.Loading
        viewModelScope.launch {
            try {
                var apps = indexRepository.getApps(forceRefresh)
                if (category != null) {
                    apps = apps.filter { it.categories?.contains(category) ?: false }
                }
                _uiState.value = DiscoverUiState.Success(apps)
            } catch (e: Exception) {
                _uiState.value = DiscoverUiState.Error
            }
        }
    }
}
