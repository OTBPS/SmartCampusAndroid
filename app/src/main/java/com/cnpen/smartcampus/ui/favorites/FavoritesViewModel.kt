package com.cnpen.smartcampus.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cnpen.smartcampus.data.model.Poi
import com.cnpen.smartcampus.data.model.RouteEntrySource
import com.cnpen.smartcampus.data.model.RoutePoint
import com.cnpen.smartcampus.data.model.RoutePointType
import com.cnpen.smartcampus.data.repository.CampusRepository
import com.cnpen.smartcampus.data.route.RoutePlanRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: CampusRepository,
    private val routePlanRepository: RoutePlanRepository
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

    fun setPoiAsOrigin(poi: Poi) {
        routePlanRepository.startRoutePlanning(RouteEntrySource.FAVORITES)
        routePlanRepository.setOrigin(poi.toRoutePoint(RoutePointType.ORIGIN))
        repository.setSelectedMapPoi(poi.id)
        autoCalculateIfPossible()
    }

    fun setPoiAsDestination(poi: Poi) {
        routePlanRepository.startRoutePlanning(RouteEntrySource.FAVORITES)
        routePlanRepository.setDestination(poi.toRoutePoint(RoutePointType.DESTINATION))
        repository.setSelectedMapPoi(poi.id)
        autoCalculateIfPossible()
    }

    fun addPoiAsWaypoint(poi: Poi) {
        routePlanRepository.startRoutePlanning(RouteEntrySource.FAVORITES)
        routePlanRepository.addWaypoint(
            poi.toRoutePoint(
                type = RoutePointType.WAYPOINT,
                uniqueSuffix = System.currentTimeMillis().toString()
            )
        )
        repository.setSelectedMapPoi(poi.id)
        autoCalculateIfPossible()
    }

    private fun autoCalculateIfPossible() {
        if (!routePlanRepository.getCurrentRoutePlanState().canCalculate) return
        viewModelScope.launch {
            routePlanRepository.calculateRoute()
        }
    }
}

private fun Poi.toRoutePoint(
    type: RoutePointType,
    uniqueSuffix: String = ""
): RoutePoint =
    RoutePoint(
        id = buildString {
            append(id)
            append('_')
            append(type.name.lowercase())
            if (uniqueSuffix.isNotBlank()) {
                append('_')
                append(uniqueSuffix)
            }
        },
        poiId = id,
        label = name,
        subtitle = building,
        latitude = latitude,
        longitude = longitude,
        type = type
    )
