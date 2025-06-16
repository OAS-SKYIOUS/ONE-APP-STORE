package io.github.skyious.oas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.skyious.oas.data.IndexRepository
import io.github.skyious.oas.data.SettingsRepository
import io.github.skyious.oas.data.model.AppDetail
import io.github.skyious.oas.data.model.AppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data class Success(val appDetail: AppDetail) : DetailUiState
    data class Error(val message: String) : DetailUiState
    object Loading : DetailUiState
}

class DetailViewModel(app: Application) : AndroidViewModel(app) {
    // This is not ideal, should be using DI. For now, this is okay.
    private val settingsRepo = SettingsRepository(app)
    private val indexRepo = IndexRepository(app, settingsRepo, settingsRepo)

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadDetail(appInfo: AppInfo) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                val detail = indexRepo.fetchAppDetail(appInfo)
                if (detail != null) {
                    _uiState.value = DetailUiState.Success(detail)
                } else {
                    _uiState.value = DetailUiState.Error("Could not load app details.")
                }
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }
}
