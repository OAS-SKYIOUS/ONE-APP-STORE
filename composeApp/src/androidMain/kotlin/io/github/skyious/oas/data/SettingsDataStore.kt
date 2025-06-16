package io.github.skyious.oas.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// Name of the DataStore file
private const val SETTINGS_DATASTORE_NAME = "settings_datastore"

// Extension property to get DataStore<Preferences> instance
val Context.dataStore by preferencesDataStore(name = SETTINGS_DATASTORE_NAME)

// Preference keys
// Default refresh interval in milliseconds (1 day)
private const val DEFAULT_REFRESH_INTERVAL = 24 * 60 * 60 * 1000L

object SettingsKeys {
    val ALLOW_OTHER_SOURCES = booleanPreferencesKey("allow_other_sources")
    val CUSTOM_SOURCE_URLS = stringPreferencesKey("custom_source_urls")
    val INCLUDE_FDROID_SOURCES = booleanPreferencesKey("include_fdroid_sources")
    val LAST_REFRESH_TIMESTAMP = longPreferencesKey("last_refresh_timestamp")
    val REFRESH_INTERVAL = longPreferencesKey("refresh_interval")
    // We'll store URLs separated by newline "\n". If empty or missing, treat as empty list.
}
