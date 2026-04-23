package com.cnpen.smartcampus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cnpen.smartcampus.di.SmartCampusViewModelFactory
import com.cnpen.smartcampus.navigation.SmartCampusNavHost
import com.cnpen.smartcampus.ui.theme.SmartCampusTheme
import com.cnpen.smartcampus.ui.theme.ThemeSettingsViewModel

@Composable
fun SmartCampusApp() {
    val context = LocalContext.current
    val appContainer = remember(context) {
        (context.applicationContext as SmartCampusApplication).container
    }
    val viewModelFactory = remember(appContainer) {
        SmartCampusViewModelFactory(appContainer).factory
    }
    val themeSettingsViewModel: ThemeSettingsViewModel = viewModel(factory = viewModelFactory)
    val themeUiState by themeSettingsViewModel.uiState.collectAsStateWithLifecycle()

    SmartCampusTheme(themeMode = themeUiState.selectedThemeMode) {
        SmartCampusNavHost(
            selectedThemeMode = themeUiState.selectedThemeMode,
            onThemeModeSelected = themeSettingsViewModel::onThemeModeSelected
        )
    }
}
