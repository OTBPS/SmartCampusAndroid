package com.cnpen.smartcampus.ui.map

import com.cnpen.smartcampus.data.model.RouteAlternative
import com.cnpen.smartcampus.data.model.RouteEntrySource
import com.cnpen.smartcampus.data.model.RouteMode
import com.cnpen.smartcampus.data.model.RoutePickerContext
import com.cnpen.smartcampus.data.model.RoutePlannerPanelMode
import com.cnpen.smartcampus.data.model.RoutePoint

data class RoutePlanUiState(
    val origin: RoutePoint? = null,
    val destination: RoutePoint? = null,
    val waypoints: List<RoutePoint> = emptyList(),
    val routeMode: RouteMode = RouteMode.WALKING,
    val alternatives: List<RouteAlternative> = emptyList(),
    val selectedAlternativeIndex: Int = -1,
    val plannerPanelMode: RoutePlannerPanelMode = RoutePlannerPanelMode.EXPANDED,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentLocationStatus: String? = null,
    val isCurrentLocationLoading: Boolean = false,
    val canCalculateRoute: Boolean = false,
    val isRoutePlanningActive: Boolean = false,
    val pickerContext: RoutePickerContext? = null,
    val pickerQuery: String = "",
    val recentRoutePoints: List<RoutePoint> = emptyList(),
    val entrySource: RouteEntrySource = RouteEntrySource.UNKNOWN
) {
    val selectedAlternative: RouteAlternative?
        get() = alternatives.getOrNull(selectedAlternativeIndex)
}
