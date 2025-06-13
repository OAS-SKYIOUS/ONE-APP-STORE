package io.github.skyious.oas.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import io.github.skyious.oas.data.model.AppInfo

class SharedViewModel : ViewModel() {
    private val _selectedApp = MutableStateFlow<AppInfo?>(null)
    val selectedApp = _selectedApp.asStateFlow()

    fun selectApp(app: AppInfo) {
        _selectedApp.value = app
    }
}
