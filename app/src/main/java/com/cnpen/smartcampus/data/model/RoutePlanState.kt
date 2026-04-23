package com.cnpen.smartcampus.data.model

data class RoutePlanState(
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
    val isRoutePlanningActive: Boolean = false,
    val pickerContext: RoutePickerContext? = null,
    val pickerQuery: String = "",
    val recentRoutePoints: List<RoutePoint> = emptyList(),
    val entrySource: RouteEntrySource = RouteEntrySource.UNKNOWN
) {
    val canCalculate: Boolean
        get() = origin != null && destination != null

    val selectedAlternative: RouteAlternative?
        get() = alternatives.getOrNull(selectedAlternativeIndex)
}
