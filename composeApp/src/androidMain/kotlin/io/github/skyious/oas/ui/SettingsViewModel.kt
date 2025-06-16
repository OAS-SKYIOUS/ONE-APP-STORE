package io.github.skyious.oas.ui


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.skyious.oas.data.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SettingsRepository(application)


    val includeFDroidSources: StateFlow<Boolean> =
        repo.includeFDroidFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun onIncludeFDroidChanged(include: Boolean) {
        viewModelScope.launch { repo.setIncludeFDroidSources(include) }
    }




    /** Expose as StateFlow for Compose to collect */
    val allowOtherSources: StateFlow<Boolean> =
        repo.allowOtherSourcesFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val customSourceUrls: StateFlow<List<String>> =
        repo.customSourceUrlsFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Called when user toggles the switch */
    fun onAllowOtherSourcesChanged(newValue: Boolean) {
        viewModelScope.launch {
            repo.setAllowOtherSources(newValue)
        }
    }

    /** Called when user adds a new URL */
    fun addCustomSource(url: String) {
        viewModelScope.launch {
            repo.addCustomSourceUrl(url)
        }
    }

    /** Called when user removes a URL */
    fun removeCustomSource(url: String) {
        viewModelScope.launch {
            repo.removeCustomSourceUrl(url)
        }
    }
    
    // Refresh interval flow
    val refreshIntervalFlow: Flow<Long> = repo.refreshIntervalFlow
    
    // Set new refresh interval
    fun setRefreshInterval(intervalMs: Long) {
        viewModelScope.launch {
            repo.setRefreshInterval(intervalMs)
        }
    }
}
