package com.cnpen.smartcampus.data.route

import com.cnpen.smartcampus.data.model.RouteAlternative
import com.cnpen.smartcampus.data.model.RouteMode
import com.cnpen.smartcampus.data.model.RoutePoint

sealed interface RouteServiceResult {
    data class Success(
        val alternatives: List<RouteAlternative>
    ) : RouteServiceResult

    data class Failure(
        val message: String,
        val errorCode: Int? = null
    ) : RouteServiceResult
}

interface RoutePlanningService {
    suspend fun calculateRoute(
        origin: RoutePoint,
        destination: RoutePoint,
        waypoints: List<RoutePoint>,
        routeMode: RouteMode
    ): RouteServiceResult
}
