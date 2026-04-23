package com.cnpen.smartcampus.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cnpen.smartcampus.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.themeDataStore by preferencesDataStore(name = "theme_preferences")

class ThemePreferenceRepository(
    private val context: Context
) {
    private val themeModeKey: Preferences.Key<String> = stringPreferencesKey("theme_mode")

    val themeModeFlow: Flow<ThemeMode> = context.themeDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[themeModeKey]
                ?.let(::toThemeMode)
                ?: ThemeMode.SYSTEM
        }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.themeDataStore.edit { preferences ->
            preferences[themeModeKey] = themeMode.name
        }
    }

    private fun toThemeMode(rawValue: String): ThemeMode =
        ThemeMode.entries.firstOrNull { it.name == rawValue } ?: ThemeMode.SYSTEM
}
