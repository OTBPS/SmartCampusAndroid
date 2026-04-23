package com.cnpen.smartcampus.data.route

import com.cnpen.smartcampus.data.model.RouteEntrySource
import com.cnpen.smartcampus.data.model.RouteMode
import com.cnpen.smartcampus.data.model.RoutePickerContext
import com.cnpen.smartcampus.data.model.RoutePlanState
import com.cnpen.smartcampus.data.model.RoutePlannerPanelMode
import com.cnpen.smartcampus.data.model.RoutePoint
import kotlinx.coroutines.flow.StateFlow

interface RoutePlanRepository {
    fun observeRoutePlanState(): StateFlow<RoutePlanState>
    fun getCurrentRoutePlanState(): RoutePlanState
    fun startRoutePlanning(entrySource: RouteEntrySource)
    fun exitRoutePlanning()
    fun setOrigin(routePoint: RoutePoint)
    fun setDestination(routePoint: RoutePoint)
    fun addWaypoint(routePoint: RoutePoint)
    fun replaceWaypoint(index: Int, routePoint: RoutePoint)
    fun removeWaypoint(index: Int)
    fun swapOriginAndDestination()
    fun clearAllPoints()
    fun setRouteMode(routeMode: RouteMode)
    fun setCurrentLocationStatus(status: String?)
    fun setCurrentLocationLoading(isLoading: Boolean)
    fun setCurrentLocationAsOrigin(latitude: Double, longitude: Double)
    fun openPicker(pickerContext: RoutePickerContext)
    fun closePicker()
    fun updatePickerQuery(query: String)
    fun applyPickedPoint(routePoint: RoutePoint)
    fun selectAlternative(index: Int)
    fun setPlannerPanelMode(mode: RoutePlannerPanelMode)
    suspend fun calculateRoute(collapsePlannerOnSuccess: Boolean = false)
    fun clearError()
}
