package io.github.skyious.oas.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class SettingsRepository(private val context: Context) {
    private val dataStore = context.dataStore
    
    companion object {
        const val DEFAULT_REFRESH_INTERVAL = 24 * 60 * 60 * 1000L // 1 day
        private val REFRESH_INTERVAL_OPTIONS = mapOf(
            "1 hour" to TimeUnit.HOURS.toMillis(1),
            "6 hours" to TimeUnit.HOURS.toMillis(6),
            "12 hours" to TimeUnit.HOURS.toMillis(12),
            "1 day" to TimeUnit.DAYS.toMillis(1),
            "1 week" to TimeUnit.DAYS.toMillis(7)
        )
        
        fun getRefreshIntervalOptions(): Map<String, Long> = REFRESH_INTERVAL_OPTIONS
    }

    val includeFDroidFlow: Flow<Boolean> =
        dataStore.data.map { it[SettingsKeys.INCLUDE_FDROID_SOURCES] ?: true } // default true
            .distinctUntilChanged()

    suspend fun setIncludeFDroidSources(include: Boolean) {
        dataStore.edit { prefs ->
            prefs[SettingsKeys.INCLUDE_FDROID_SOURCES] = include
        }
    }

    /** Flow of whether custom sources are allowed */
    val allowOtherSourcesFlow: Flow<Boolean> =
        dataStore.data
            .map { prefs ->
                prefs[SettingsKeys.ALLOW_OTHER_SOURCES] ?: false
            }
            .distinctUntilChanged()

    /** Flow of list of custom source URLs (split by newline) */
    val customSourceUrlsFlow: Flow<List<String>> =
        dataStore.data
            .map { prefs ->
                val raw = prefs[SettingsKeys.CUSTOM_SOURCE_URLS].orEmpty()
                raw.lines()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            }
            .distinctUntilChanged()

    /** Update allowOtherSources */
    suspend fun setAllowOtherSources(allow: Boolean) {
        dataStore.edit { prefs ->
            prefs[SettingsKeys.ALLOW_OTHER_SOURCES] = allow
            // If disabling custom sources, we could optionally clear URLs or keep them for later re-enable
            // e.g., if (!allow) prefs[SettingsKeys.CUSTOM_SOURCE_URLS] = ""
        }
    }

    /** Add a new source URL (if not already present) */
    suspend fun addCustomSourceUrl(url: String) {
        dataStore.edit { prefs ->
            val raw = prefs[SettingsKeys.CUSTOM_SOURCE_URLS].orEmpty()
            val list = raw.lines().map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
            if (!list.contains(url)) {
                list.add(url)
                // join with newline
                prefs[SettingsKeys.CUSTOM_SOURCE_URLS] = list.joinToString("\n")
            }
        }
    }

    /** Remove an existing source URL */
    suspend fun removeCustomSourceUrl(url: String) {
        dataStore.edit { prefs ->
            val raw = prefs[SettingsKeys.CUSTOM_SOURCE_URLS].orEmpty()
            val list = raw.lines().map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
            if (list.remove(url)) {
                prefs[SettingsKeys.CUSTOM_SOURCE_URLS] = list.joinToString("\n")
            }
        }
    }
    
    // Refresh interval settings
    val refreshIntervalFlow: Flow<Long> =
        dataStore.data
            .map { it[SettingsKeys.REFRESH_INTERVAL] ?: DEFAULT_REFRESH_INTERVAL }
            .distinctUntilChanged()
    
    suspend fun setRefreshInterval(intervalMs: Long) {
        dataStore.edit { prefs ->
            prefs[SettingsKeys.REFRESH_INTERVAL] = intervalMs
        }
    }
    
    suspend fun getLastRefreshTimestamp(): Long {
        return dataStore.data.first()[SettingsKeys.LAST_REFRESH_TIMESTAMP] ?: 0L
    }
    
    suspend fun updateLastRefreshTimestamp() {
        dataStore.edit { prefs ->
            prefs[SettingsKeys.LAST_REFRESH_TIMESTAMP] = System.currentTimeMillis()
        }
    }
    
    suspend fun shouldRefreshData(): Boolean {
        val lastRefresh = getLastRefreshTimestamp()
        val refreshInterval = dataStore.data.first()[SettingsKeys.REFRESH_INTERVAL] ?: DEFAULT_REFRESH_INTERVAL
        return (System.currentTimeMillis() - lastRefresh) >= refreshInterval
    }
}
