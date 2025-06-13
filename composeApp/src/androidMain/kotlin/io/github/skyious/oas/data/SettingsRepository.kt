package io.github.skyious.oas.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.distinctUntilChanged

class SettingsRepository(private val context: Context) {

    private val dataStore = context.dataStore

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
}
