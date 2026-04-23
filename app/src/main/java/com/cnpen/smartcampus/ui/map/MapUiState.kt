package com.cnpen.smartcampus.ui.map

import com.cnpen.smartcampus.data.model.Poi
import com.cnpen.smartcampus.data.model.RoutePoint

data class MapUiState(
    val title: String = "Campus Map",
    val selectedPoi: Poi? = null,
    val focusStatus: String = "Map is ready for destination focus.",
    val destinationHint: String = "Choose a destination from Search or Place Detail.",
    val fallbackCenterLatitude: Double = 22.3020,
    val fallbackCenterLongitude: Double = 114.1775,
    val fallbackZoom: Float = 15.5f,
    val flowState: MapFlowState = MapFlowState.MAP_EXPLORE,
    val routePlan: RoutePlanUiState = RoutePlanUiState(),
    val pickerContextLabel: String = "",
    val pickerSearchResults: List<Poi> = emptyList(),
    val pickerFavoritePois: List<Poi> = emptyList(),
    val pickerRecentPoints: List<RoutePoint> = emptyList()
)
