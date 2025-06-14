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

    // DiscoverViewModel.kt
    // DiscoverViewModel.kt
    fun loadApps(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get ALL apps, including F-Droid if enabled in settings, directly from getApps()
                val allApps = indexRepo.getApps(forceRefresh)
                _apps.value = allApps
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("DiscoverViewModel", "Error loading apps", e)
                _apps.value = emptyList() // Or some error state
            } finally {
                _isLoading.value = false
            }
        }
    }
}