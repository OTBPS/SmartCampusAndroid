package com.cnpen.smartcampus.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cnpen.smartcampus.data.repository.CampusRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val repository: CampusRepository
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        repository.observePois(),
        repository.observeFavoriteIds()
    ) { pois, favoriteIds ->
        HomeUiState(
            recommendedPois = pois.sortedByDescending { it.popularity }.take(4),
            favoritesCount = favoriteIds.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )
}
