package com.cnpen.smartcampus.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cnpen.smartcampus.data.repository.CampusRepositoryProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class FavoritesViewModel : ViewModel() {
    private val repository = CampusRepositoryProvider.repository

    val uiState: StateFlow<FavoritesUiState> = combine(
        repository.observePois(),
        repository.observeFavoriteIds()
    ) { pois, favoriteIds ->
        val favorites = pois.filter { favoriteIds.contains(it.id) }
        FavoritesUiState(favorites = favorites)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FavoritesUiState()
    )
}
