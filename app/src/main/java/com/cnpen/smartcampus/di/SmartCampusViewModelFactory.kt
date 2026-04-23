package com.cnpen.smartcampus.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cnpen.smartcampus.ui.assistant.AssistantViewModel
import com.cnpen.smartcampus.ui.detail.PoiDetailViewModel
import com.cnpen.smartcampus.ui.favorites.FavoritesViewModel
import com.cnpen.smartcampus.ui.home.HomeViewModel
import com.cnpen.smartcampus.ui.map.MapViewModel
import com.cnpen.smartcampus.ui.search.SearchViewModel
import com.cnpen.smartcampus.ui.theme.ThemeSettingsViewModel

class SmartCampusViewModelFactory(
    appContainer: AppContainer
) {
    private val repository = appContainer.campusRepository
    private val themePreferenceRepository = appContainer.themePreferenceRepository
    private val routePlanRepository = appContainer.routePlanRepository

    val factory: ViewModelProvider.Factory = viewModelFactory {
        initializer { HomeViewModel(repository) }
        initializer { SearchViewModel(repository) }
        initializer { MapViewModel(repository, routePlanRepository) }
        initializer { FavoritesViewModel(repository, routePlanRepository) }
        initializer { AssistantViewModel() }
        initializer { PoiDetailViewModel(createSavedStateHandle(), repository, routePlanRepository) }
        initializer { ThemeSettingsViewModel(themePreferenceRepository) }
    }
}
