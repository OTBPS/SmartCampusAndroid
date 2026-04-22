package com.cnpen.smartcampus.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cnpen.smartcampus.data.repository.CampusRepositoryProvider
import com.cnpen.smartcampus.navigation.AppDestination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class PoiDetailViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val repository = CampusRepositoryProvider.repository
    private val poiId: String? = savedStateHandle[AppDestination.PoiDetail.POI_ID_ARG]

    val uiState: StateFlow<PoiDetailUiState> = combine(
        repository.observePois(),
        repository.observeFavoriteIds()
    ) { pois, favoriteIds ->
        val currentPoiId = poiId
        if (currentPoiId == null) {
            PoiDetailUiState(errorMessage = "Missing place ID.")
        } else {
            val poi = pois.firstOrNull { it.id == currentPoiId }
            if (poi == null) {
                PoiDetailUiState(errorMessage = "Place not found.")
            } else {
                PoiDetailUiState(
                    poi = poi,
                    isFavorite = favoriteIds.contains(poi.id)
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PoiDetailUiState()
    )

    fun onToggleFavorite() {
        val currentPoi = uiState.value.poi ?: return
        repository.toggleFavorite(currentPoi.id)
    }

    fun onPrepareMapFocus() {
        val currentPoi = uiState.value.poi ?: return
        repository.setSelectedMapPoi(currentPoi.id)
    }
}
