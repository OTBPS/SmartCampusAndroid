package com.cnpen.smartcampus.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cnpen.smartcampus.data.repository.CampusRepository
import com.cnpen.smartcampus.navigation.AppDestination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class PoiDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CampusRepository
) : ViewModel() {
    private val poiId: String? = savedStateHandle[AppDestination.PoiDetail.POI_ID_ARG]

    val uiState: StateFlow<PoiDetailUiState> = combine(
        repository.observePois(),
        repository.observeFavoriteIds(),
        repository.observeIsLoading(),
        repository.observeErrorMessage(),
        repository.observeDataSourceLabel()
    ) { pois, favoriteIds, isLoading, errorMessage, dataSourceLabel ->
        val currentPoiId = poiId
        if (currentPoiId == null) {
            PoiDetailUiState(
                errorMessage = "Missing place ID.",
                isLoading = isLoading,
                dataSourceLabel = dataSourceLabel
            )
        } else {
            val poi = pois.firstOrNull { it.id == currentPoiId }
            if (poi == null) {
                PoiDetailUiState(
                    errorMessage = errorMessage ?: if (isLoading) null else "Place not found.",
                    isLoading = isLoading,
                    dataSourceLabel = dataSourceLabel
                )
            } else {
                PoiDetailUiState(
                    poi = poi,
                    isFavorite = favoriteIds.contains(poi.id),
                    errorMessage = errorMessage,
                    isLoading = isLoading,
                    dataSourceLabel = dataSourceLabel
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

    fun clearError() {
        repository.clearError()
    }
}
