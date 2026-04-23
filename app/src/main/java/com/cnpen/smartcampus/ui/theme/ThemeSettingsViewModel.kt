package com.cnpen.smartcampus.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cnpen.smartcampus.data.local.ThemePreferenceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeSettingsViewModel(
    private val themePreferenceRepository: ThemePreferenceRepository
) : ViewModel() {
    val uiState: StateFlow<ThemeSettingsUiState> = themePreferenceRepository.themeModeFlow
        .map { mode ->
            ThemeSettingsUiState(selectedThemeMode = mode)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeSettingsUiState()
        )

    fun onThemeModeSelected(themeMode: ThemeMode) {
        viewModelScope.launch {
            themePreferenceRepository.setThemeMode(themeMode)
        }
    }
}
