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
import kotlinx.coroutines.launch

class DetailViewModel(app: Application) : AndroidViewModel(app) {
    private val settingsRepo = SettingsRepository(app)
    private val indexRepo = IndexRepository(app, settingsRepo, settingsRepo)

    private val _detail = MutableStateFlow<AppDetail?>(null)
    val detail: StateFlow<AppDetail?> = _detail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadDetail(appInfo: AppInfo) {
        viewModelScope.launch {
            _isLoading.value = true
            val d = indexRepo.fetchAppDetail(appInfo)
            _detail.value = d
            _isLoading.value = false
        }
    }
}
