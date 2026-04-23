package com.cnpen.smartcampus.data.route

import com.cnpen.smartcampus.data.model.RouteEntrySource
import com.cnpen.smartcampus.data.model.RouteMode
import com.cnpen.smartcampus.data.model.RoutePickerContext
import com.cnpen.smartcampus.data.model.RoutePlanState
import com.cnpen.smartcampus.data.model.RoutePlannerPanelMode
import com.cnpen.smartcampus.data.model.RoutePoint
import com.cnpen.smartcampus.data.model.RoutePointType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryRoutePlanRepository(
    private val routePlanningService: RoutePlanningService
) : RoutePlanRepository {
    private val routePlanState = MutableStateFlow(RoutePlanState())

    override fun observeRoutePlanState(): StateFlow<RoutePlanState> = routePlanState.asStateFlow()

    override fun getCurrentRoutePlanState(): RoutePlanState = routePlanState.value

    override fun startRoutePlanning(entrySource: RouteEntrySource) {
        routePlanState.update { old ->
            old.copy(
                isRoutePlanningActive = true,
                entrySource = entrySource,
                plannerPanelMode = RoutePlannerPanelMode.EXPANDED
            )
        }
    }

    override fun exitRoutePlanning() {
        routePlanState.update { old ->
                old.copy(
                    isRoutePlanningActive = false,
                    plannerPanelMode = RoutePlannerPanelMode.EXPANDED,
                    pickerContext = null,
                    pickerQuery = "",
                    errorMessage = null,
                currentLocationStatus = null,
                isCurrentLocationLoading = false
            )
        }
    }

    override fun setOrigin(routePoint: RoutePoint) {
        routePlanState.update { old ->
            invalidateCalculatedRoute(
                old.copy(
                    origin = routePoint.copy(type = RoutePointType.ORIGIN)
                )
            )
        }
    }

    override fun setDestination(routePoint: RoutePoint) {
        routePlanState.update { old ->
            invalidateCalculatedRoute(
                old.copy(
                    destination = routePoint.copy(type = RoutePointType.DESTINATION)
                )
            )
        }
    }

    override fun addWaypoint(routePoint: RoutePoint) {
        routePlanState.update { old ->
            invalidateCalculatedRoute(
                old.copy(
                    waypoints = old.waypoints + routePoint.copy(type = RoutePointType.WAYPOINT)
                )
            )
        }
    }

    override fun replaceWaypoint(index: Int, routePoint: RoutePoint) {
        routePlanState.update { old ->
            if (index !in old.waypoints.indices) return@update old
            invalidateCalculatedRoute(
                old.copy(
                    waypoints = old.waypoints.toMutableList().apply {
                        set(index, routePoint.copy(type = RoutePointType.WAYPOINT))
                    }
                )
            )
        }
    }

    override fun removeWaypoint(index: Int) {
        routePlanState.update { old ->
            if (index !in old.waypoints.indices) return@update old
            invalidateCalculatedRoute(
                old.copy(
                    waypoints = old.waypoints.toMutableList().apply {
                        removeAt(index)
                    }
                )
            )
        }
    }

    override fun swapOriginAndDestination() {
        routePlanState.update { old ->
            invalidateCalculatedRoute(
                old.copy(
                    origin = old.destination?.copy(type = RoutePointType.ORIGIN),
                    destination = old.origin?.copy(type = RoutePointType.DESTINATION)
                )
            )
        }
    }

    override fun clearAllPoints() {
        routePlanState.update { old ->
            old.copy(
                origin = null,
                destination = null,
                waypoints = emptyList(),
                alternatives = emptyList(),
                selectedAlternativeIndex = -1,
                plannerPanelMode = RoutePlannerPanelMode.EXPANDED,
                errorMessage = null
            )
        }
    }

    override fun setRouteMode(routeMode: RouteMode) {
        routePlanState.update { old ->
            if (old.routeMode == routeMode) return@update old
            invalidateCalculatedRoute(
                old.copy(routeMode = routeMode)
            )
        }
    }

    override fun setCurrentLocationStatus(status: String?) {
        routePlanState.update { old ->
            old.copy(currentLocationStatus = status)
        }
    }

    override fun setCurrentLocationLoading(isLoading: Boolean) {
        routePlanState.update { old ->
            old.copy(isCurrentLocationLoading = isLoading)
        }
    }

    override fun setCurrentLocationAsOrigin(latitude: Double, longitude: Double) {
        setOrigin(
            RoutePoint(
                id = "current_location_origin",
                poiId = null,
                label = "Current Location",
                subtitle = "Device location",
                latitude = latitude,
                longitude = longitude,
                type = RoutePointType.ORIGIN
            )
        )
        setCurrentLocationStatus("Current location set as origin.")
        setCurrentLocationLoading(false)
    }

    override fun openPicker(pickerContext: RoutePickerContext) {
        routePlanState.update { old ->
            old.copy(
                pickerContext = pickerContext,
                pickerQuery = "",
                isRoutePlanningActive = true
            )
        }
    }

    override fun closePicker() {
        routePlanState.update { old ->
            old.copy(
                pickerContext = null,
                pickerQuery = ""
            )
        }
    }

    override fun updatePickerQuery(query: String) {
        routePlanState.update { old ->
            old.copy(pickerQuery = query)
        }
    }

    override fun applyPickedPoint(routePoint: RoutePoint) {
        val snapshot = routePlanState.value
        val pickerContext = snapshot.pickerContext ?: return
        when (pickerContext.pointType) {
            RoutePointType.ORIGIN -> setOrigin(routePoint.copy(type = RoutePointType.ORIGIN))
            RoutePointType.DESTINATION -> setDestination(routePoint.copy(type = RoutePointType.DESTINATION))
            RoutePointType.WAYPOINT -> {
                val replaceIndex = pickerContext.waypointIndex
                if (replaceIndex == null) {
                    addWaypoint(routePoint.copy(type = RoutePointType.WAYPOINT))
                } else {
                    replaceWaypoint(
                        index = replaceIndex,
                        routePoint = routePoint.copy(type = RoutePointType.WAYPOINT)
                    )
                }
            }
        }
        routePlanState.update { old ->
            old.copy(
                pickerContext = null,
                pickerQuery = "",
                recentRoutePoints = rememberRecentRoutePoints(old.recentRoutePoints, routePoint),
                isRoutePlanningActive = true
            )
        }
    }

    override fun selectAlternative(index: Int) {
        routePlanState.update { old ->
            if (index !in old.alternatives.indices) return@update old
            old.copy(
                selectedAlternativeIndex = index,
                errorMessage = null
            )
        }
    }

    override fun setPlannerPanelMode(mode: RoutePlannerPanelMode) {
        routePlanState.update { old ->
            if (old.plannerPanelMode == mode) return@update old
            old.copy(plannerPanelMode = mode)
        }
    }

    override suspend fun calculateRoute(collapsePlannerOnSuccess: Boolean) {
        val snapshot = routePlanState.value
        val origin = snapshot.origin
        val destination = snapshot.destination
        if (origin == null || destination == null) {
            routePlanState.update { old ->
                old.copy(
                    errorMessage = "Set both origin and destination before route calculation."
                )
            }
            return
        }

        routePlanState.update { old ->
            old.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        when (
            val result = routePlanningService.calculateRoute(
                origin = origin,
                destination = destination,
                waypoints = snapshot.waypoints,
                routeMode = snapshot.routeMode
            )
        ) {
            is RouteServiceResult.Success -> {
                routePlanState.update { old ->
                    old.copy(
                        isLoading = false,
                        alternatives = result.alternatives,
                        selectedAlternativeIndex = result.alternatives.indices.firstOrNull() ?: -1,
                        plannerPanelMode = if (collapsePlannerOnSuccess && result.alternatives.isNotEmpty()) {
                            RoutePlannerPanelMode.COLLAPSED
                        } else if (result.alternatives.isEmpty()) {
                            RoutePlannerPanelMode.EXPANDED
                        } else {
                            old.plannerPanelMode
                        },
                        errorMessage = null
                    )
                }
            }

            is RouteServiceResult.Failure -> {
                routePlanState.update { old ->
                    old.copy(
                        isLoading = false,
                        alternatives = emptyList(),
                        selectedAlternativeIndex = -1,
                        plannerPanelMode = RoutePlannerPanelMode.EXPANDED,
                        errorMessage = buildString {
                            append(result.message)
                            if (result.errorCode != null) {
                                append(" (code: ")
                                append(result.errorCode)
                                append(')')
                            }
                        }
                    )
                }
            }
        }
    }

    override fun clearError() {
        routePlanState.update { old ->
            old.copy(errorMessage = null)
        }
    }
}

private fun invalidateCalculatedRoute(state: RoutePlanState): RoutePlanState =
    state.copy(
        alternatives = emptyList(),
        selectedAlternativeIndex = -1,
        plannerPanelMode = RoutePlannerPanelMode.EXPANDED,
        errorMessage = null
    )

private fun rememberRecentRoutePoints(
    previous: List<RoutePoint>,
    selected: RoutePoint
): List<RoutePoint> {
    val withoutDuplicate = previous.filterNot { point ->
        point.poiId != null && point.poiId == selected.poiId &&
            point.latitude == selected.latitude &&
            point.longitude == selected.longitude
    }
    return listOf(
        selected.copy(type = RoutePointType.WAYPOINT)
    ) + withoutDuplicate.take(5)
}
