package com.cnpen.smartcampus.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cnpen.smartcampus.data.repository.CampusRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class FavoritesViewModel(
    private val repository: CampusRepository
) : ViewModel() {
    val uiState: StateFlow<FavoritesUiState> = combine(
        repository.observePois(),
        repository.observeFavoriteIds(),
        repository.observeIsLoading(),
        repository.observeErrorMessage(),
        repository.observeDataSourceLabel()
    ) { pois, favoriteIds, isLoading, errorMessage, dataSourceLabel ->
        val favorites = pois.filter { favoriteIds.contains(it.id) }
        FavoritesUiState(
            favorites = favorites,
            isLoading = isLoading,
            errorMessage = errorMessage,
            dataSourceLabel = dataSourceLabel
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FavoritesUiState()
    )

    fun onRemoveFavorite(poiId: String) {
        repository.removeFavorite(poiId)
    }

    fun clearError() {
        repository.clearError()
    }
}
