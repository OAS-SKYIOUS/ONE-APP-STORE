// androidApp/src/main/java/com/yourcompany/appstore/android/ui/DiscoverViewModel.kt
package io.github.skyious.oas.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.skyious.oas.data.IndexRepository
import io.github.skyious.oas.data.SettingsRepository
import io.github.skyious.oas.data.model.AppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

class DiscoverViewModel(app: Application) : AndroidViewModel(app) {
    private val settingsRepo = SettingsRepository(app)
    // Assuming IndexRepository constructor takes two SettingsRepository instances for different purposes
    private val indexRepo = IndexRepository(app, settingsRepo, settingsRepo)

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps = _apps.asStateFlow()
    private val _progress = MutableStateFlow(0f)
    val progress = _progress.asStateFlow()

    private val _progressMessage = MutableStateFlow("Loading...")
    val progressMessage = _progressMessage.asStateFlow()


    fun loadApps(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _progress.value = 0f
            _progressMessage.value = "Starting app list refresh..."

            try {
                // Get all apps in one go since the repository handles the caching
                _progressMessage.value = "Loading apps..."
                val allApps = indexRepo.getApps(forceRefresh)
                _progress.value = 0.7f

                _progressMessage.value = "Finalizing..."
                _apps.value = allApps
                _progress.value = 1f

            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("DiscoverViewModel", "Error loading apps", e)
                _apps.value = emptyList()
                _progressMessage.value = "Error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }

        }
    }
}
