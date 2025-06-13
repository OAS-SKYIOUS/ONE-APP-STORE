package io.github.skyious.oas.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// Name of the DataStore file
private const val SETTINGS_DATASTORE_NAME = "settings_datastore"

// Extension property to get DataStore<Preferences> instance
val Context.dataStore by preferencesDataStore(name = SETTINGS_DATASTORE_NAME)

// Preference keys
object SettingsKeys {
    val ALLOW_OTHER_SOURCES = booleanPreferencesKey("allow_other_sources")
    val CUSTOM_SOURCE_URLS = stringPreferencesKey("custom_source_urls")
    val INCLUDE_FDROID_SOURCES = booleanPreferencesKey("include_fdroid_sources")
    // We'll store URLs separated by newline "\n". If empty or missing, treat as empty list.
}
