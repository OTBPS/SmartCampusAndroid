package com.cnpen.smartcampus.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cnpen.smartcampus.data.model.Poi
import com.cnpen.smartcampus.data.model.RouteEntrySource
import com.cnpen.smartcampus.data.model.RouteMode
import com.cnpen.smartcampus.data.model.RoutePickerContext
import com.cnpen.smartcampus.data.model.RoutePlannerPanelMode
import com.cnpen.smartcampus.data.model.RoutePoint
import com.cnpen.smartcampus.data.model.RoutePointType
import com.cnpen.smartcampus.data.repository.CampusRepository
import com.cnpen.smartcampus.data.route.RoutePlanRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapViewModel(
    private val campusRepository: CampusRepository,
    private val routePlanRepository: RoutePlanRepository
) : ViewModel() {
    val uiState: StateFlow<MapUiState> = combine(
        campusRepository.observePois(),
        campusRepository.observeFavoriteIds(),
        campusRepository.observeSelectedMapPoiId(),
        routePlanRepository.observeRoutePlanState()
    ) { pois, favoriteIds, selectedPoiId, routePlanState ->
        val selectedPoi = selectedPoiId?.let { id -> pois.firstOrNull { it.id == id } }
        val fallbackCenter = if (pois.isEmpty()) {
            22.3020 to 114.1775
        } else {
            val latitude = pois.map { it.latitude }.average()
            val longitude = pois.map { it.longitude }.average()
            latitude to longitude
        }

        val flowState = when {
            routePlanState.pickerContext != null -> MapFlowState.PLACE_PICKER
            routePlanState.isRoutePlanningActive && routePlanState.selectedAlternative != null -> {
                MapFlowState.ROUTE_RESULT_FOCUS
            }

            routePlanState.isRoutePlanningActive -> MapFlowState.ROUTE_EDITOR
            selectedPoi != null -> MapFlowState.POI_DETAIL
            else -> MapFlowState.MAP_EXPLORE
        }

        val pickerLabel = routePlanState.pickerContext.toPickerLabel()
        val pickerResults = buildPickerResults(
            query = routePlanState.pickerQuery,
            allPois = pois
        )
        val pickerFavorites = pois.filter { favoriteIds.contains(it.id) }
        val pickerRecentPoints = routePlanState.recentRoutePoints

        val focusMessage = when {
            routePlanState.selectedAlternative != null -> "Route is drawn on the map."
            selectedPoi == null -> "No destination selected. Showing campus overview."
            else -> "Map is focused on the selected destination."
        }
        val destinationHint = if (selectedPoi == null) {
            "Choose a place from Search or open Directions from Place Detail."
        } else {
            "Chosen destination: ${selectedPoi.name} (${selectedPoi.category.label})"
        }

        MapUiState(
            selectedPoi = selectedPoi,
            focusStatus = focusMessage,
            destinationHint = destinationHint,
            fallbackCenterLatitude = fallbackCenter.first,
            fallbackCenterLongitude = fallbackCenter.second,
            flowState = flowState,
            pickerContextLabel = pickerLabel,
            pickerSearchResults = pickerResults,
            pickerFavoritePois = pickerFavorites,
            pickerRecentPoints = pickerRecentPoints,
            routePlan = RoutePlanUiState(
                origin = routePlanState.origin,
                destination = routePlanState.destination,
                waypoints = routePlanState.waypoints,
                routeMode = routePlanState.routeMode,
                alternatives = routePlanState.alternatives,
                selectedAlternativeIndex = routePlanState.selectedAlternativeIndex,
                plannerPanelMode = routePlanState.plannerPanelMode,
                isLoading = routePlanState.isLoading,
                errorMessage = routePlanState.errorMessage,
                currentLocationStatus = routePlanState.currentLocationStatus,
                isCurrentLocationLoading = routePlanState.isCurrentLocationLoading,
                canCalculateRoute = routePlanState.canCalculate,
                isRoutePlanningActive = routePlanState.isRoutePlanningActive,
                pickerContext = routePlanState.pickerContext,
                pickerQuery = routePlanState.pickerQuery,
                recentRoutePoints = routePlanState.recentRoutePoints,
                entrySource = routePlanState.entrySource
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MapUiState()
    )

    fun onStartRoutePlanningFromSelectedPoi() {
        val poi = uiState.value.selectedPoi ?: return
        routePlanRepository.startRoutePlanning(RouteEntrySource.POI_DETAIL)
        routePlanRepository.setDestination(poi.toRoutePoint(RoutePointType.DESTINATION))
        attemptAutoRecalculate()
    }

    fun onStartRoutePlanning(entrySource: RouteEntrySource = RouteEntrySource.MAP_EXPLORE) {
        routePlanRepository.startRoutePlanning(entrySource)
    }

    fun onRouteBack() {
        val fromPoiDetail = uiState.value.routePlan.entrySource == RouteEntrySource.POI_DETAIL
        routePlanRepository.exitRoutePlanning()
        if (!fromPoiDetail) {
            campusRepository.setSelectedMapPoi(null)
        }
    }

    fun onDismissPoiDetail() {
        campusRepository.setSelectedMapPoi(null)
    }

    fun setRouteMode(routeMode: RouteMode) {
        routePlanRepository.setRouteMode(routeMode)
        attemptAutoRecalculate()
    }

    fun swapOriginAndDestination() {
        routePlanRepository.swapOriginAndDestination()
        attemptAutoRecalculate()
    }

    fun clearRoutePoints() {
        routePlanRepository.clearAllPoints()
    }

    fun clearRouteError() {
        routePlanRepository.clearError()
    }

    fun onSelectAlternative(index: Int) {
        routePlanRepository.selectAlternative(index)
    }

    fun calculateRoute() {
        viewModelScope.launch {
            routePlanRepository.calculateRoute(collapsePlannerOnSuccess = true)
        }
    }

    fun expandRoutePlannerPanel() {
        routePlanRepository.setPlannerPanelMode(RoutePlannerPanelMode.EXPANDED)
    }

    fun collapseRoutePlannerPanel() {
        routePlanRepository.setPlannerPanelMode(RoutePlannerPanelMode.COLLAPSED)
    }

    fun openOriginPicker() {
        routePlanRepository.startRoutePlanning(
            if (uiState.value.routePlan.isRoutePlanningActive) {
                uiState.value.routePlan.entrySource
            } else {
                RouteEntrySource.MAP_EXPLORE
            }
        )
        routePlanRepository.openPicker(RoutePickerContext(RoutePointType.ORIGIN))
    }

    fun openDestinationPicker() {
        routePlanRepository.startRoutePlanning(
            if (uiState.value.routePlan.isRoutePlanningActive) {
                uiState.value.routePlan.entrySource
            } else {
                RouteEntrySource.MAP_EXPLORE
            }
        )
        routePlanRepository.openPicker(RoutePickerContext(RoutePointType.DESTINATION))
    }

    fun openAddWaypointPicker() {
        routePlanRepository.startRoutePlanning(uiState.value.routePlan.entrySource)
        routePlanRepository.openPicker(RoutePickerContext(RoutePointType.WAYPOINT))
    }

    fun openReplaceWaypointPicker(index: Int) {
        routePlanRepository.startRoutePlanning(uiState.value.routePlan.entrySource)
        routePlanRepository.openPicker(
            RoutePickerContext(
                pointType = RoutePointType.WAYPOINT,
                waypointIndex = index
            )
        )
    }

    fun removeWaypoint(index: Int) {
        routePlanRepository.removeWaypoint(index)
        attemptAutoRecalculate()
    }

    fun closePicker() {
        routePlanRepository.closePicker()
    }

    fun onPickerQueryChange(value: String) {
        routePlanRepository.updatePickerQuery(value)
    }

    fun onPickPoiFromPicker(poiId: String) {
        val poi = campusRepository.getPoiById(poiId) ?: return
        campusRepository.setSelectedMapPoi(poi.id)
        routePlanRepository.applyPickedPoint(
            poi.toRoutePoint(
                type = routePointTypeFromPickerContext(),
                uniqueSuffix = System.currentTimeMillis().toString()
            )
        )
        attemptAutoRecalculate()
    }

    fun onPickRecentPoint(routePoint: RoutePoint) {
        routePlanRepository.applyPickedPoint(
            routePoint.copy(
                type = routePointTypeFromPickerContext()
            )
        )
        routePoint.poiId?.let(campusRepository::setSelectedMapPoi)
        attemptAutoRecalculate()
    }

    fun onSetSelectedPoiAsWaypoint() {
        val poi = uiState.value.selectedPoi ?: return
        routePlanRepository.addWaypoint(
            poi.toRoutePoint(
                type = RoutePointType.WAYPOINT,
                uniqueSuffix = System.currentTimeMillis().toString()
            )
        )
        routePlanRepository.startRoutePlanning(RouteEntrySource.MAP_EXPLORE)
        attemptAutoRecalculate()
    }

    fun startCurrentLocationAsOrigin() {
        routePlanRepository.setCurrentLocationLoading(true)
    }

    fun setCurrentLocationAsOrigin(latitude: Double, longitude: Double) {
        routePlanRepository.setCurrentLocationAsOrigin(latitude, longitude)
        routePlanRepository.startRoutePlanning(
            if (uiState.value.routePlan.isRoutePlanningActive) {
                uiState.value.routePlan.entrySource
            } else {
                RouteEntrySource.MAP_EXPLORE
            }
        )
        attemptAutoRecalculate()
    }

    fun updateCurrentLocationStatus(status: String?) {
        routePlanRepository.setCurrentLocationStatus(status)
    }

    fun finishCurrentLocationRequest() {
        routePlanRepository.setCurrentLocationLoading(false)
    }

    private fun routePointTypeFromPickerContext(): RoutePointType {
        return uiState.value.routePlan.pickerContext?.pointType ?: RoutePointType.WAYPOINT
    }

    private fun attemptAutoRecalculate() {
        if (!routePlanRepository.getCurrentRoutePlanState().canCalculate) return
        viewModelScope.launch {
            routePlanRepository.calculateRoute(collapsePlannerOnSuccess = false)
        }
    }
}

private fun buildPickerResults(
    query: String,
    allPois: List<Poi>
): List<Poi> {
    val normalized = query.trim().lowercase()
    if (normalized.isBlank()) {
        return allPois.sortedByDescending { it.popularity }.take(10)
    }
    return allPois.filter { poi ->
        poi.name.lowercase().contains(normalized) ||
            poi.description.lowercase().contains(normalized) ||
            poi.building.lowercase().contains(normalized) ||
            poi.keywords.any { keyword -> keyword.lowercase().contains(normalized) }
    }.sortedByDescending { it.popularity }
}

private fun RoutePickerContext?.toPickerLabel(): String =
    when (this?.pointType) {
        RoutePointType.ORIGIN -> "Search starting point"
        RoutePointType.DESTINATION -> "Search destination"
        RoutePointType.WAYPOINT -> if (waypointIndex == null) "Search waypoint" else "Replace waypoint"
        null -> "Search place"
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
