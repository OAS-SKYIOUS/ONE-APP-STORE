// androidApp/src/main/java/com/yourcompany/appstore/android/ui/DiscoverViewModel.kt
package io.github.skyious.oas.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.skyious.oas.data.IndexRepository
import io.github.skyious.oas.data.SettingsRepository
import io.github.skyious.oas.data.model.AppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.asStateFlow

class DiscoverViewModel(app: Application) : AndroidViewModel(app) {
    private val settingsRepo = SettingsRepository(app)
    private val indexRepo = IndexRepository(app, settingsRepo)

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps = _apps.asStateFlow()

    fun loadApps(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            val list = indexRepo.getApps(forceRefresh)
            _apps.value = list
            _isLoading.value = false
        }
    }
}
