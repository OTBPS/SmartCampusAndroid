package com.cnpen.smartcampus.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cnpen.smartcampus.data.model.Poi
import com.cnpen.smartcampus.data.model.RouteEntrySource
import com.cnpen.smartcampus.data.model.RoutePoint
import com.cnpen.smartcampus.data.model.RoutePointType
import com.cnpen.smartcampus.data.repository.CampusRepository
import com.cnpen.smartcampus.data.route.RoutePlanRepository
import com.cnpen.smartcampus.navigation.AppDestination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PoiDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CampusRepository,
    private val routePlanRepository: RoutePlanRepository
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

    fun onDirections() {
        val currentPoi = uiState.value.poi ?: return
        repository.setSelectedMapPoi(currentPoi.id)
        routePlanRepository.startRoutePlanning(RouteEntrySource.POI_DETAIL)
        routePlanRepository.setDestination(currentPoi.toRoutePoint(RoutePointType.DESTINATION))
        if (routePlanRepository.getCurrentRoutePlanState().canCalculate) {
            viewModelScope.launch {
                routePlanRepository.calculateRoute()
            }
        }
    }

    fun clearError() {
        repository.clearError()
    }
}

private fun Poi.toRoutePoint(type: RoutePointType): RoutePoint =
    RoutePoint(
        id = "${id}_${type.name.lowercase()}",
        poiId = id,
        label = name,
        subtitle = building,
        latitude = latitude,
        longitude = longitude,
        type = type
    )
