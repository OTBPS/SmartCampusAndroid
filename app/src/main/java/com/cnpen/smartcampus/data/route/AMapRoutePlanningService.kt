package com.cnpen.smartcampus.data.route

import android.content.Context
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.route.DrivePathV2
import com.amap.api.services.route.DriveRouteResultV2
import com.amap.api.services.route.RouteSearchV2
import com.amap.api.services.route.WalkPath
import com.amap.api.services.route.WalkRouteResultV2
import com.cnpen.smartcampus.data.model.RouteAlternative
import com.cnpen.smartcampus.data.model.RouteCoordinate
import com.cnpen.smartcampus.data.model.RouteMode
import com.cnpen.smartcampus.data.model.RoutePoint
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AMapRoutePlanningService(
    private val context: Context
) : RoutePlanningService {

    override suspend fun calculateRoute(
        origin: RoutePoint,
        destination: RoutePoint,
        waypoints: List<RoutePoint>,
        routeMode: RouteMode
    ): RouteServiceResult {
        val routeSearch = try {
            RouteSearchV2(context)
        } catch (exception: AMapException) {
            return RouteServiceResult.Failure(
                message = exception.errorMessage ?: "Unable to initialize route search service.",
                errorCode = exception.errorCode
            )
        } catch (throwable: Throwable) {
            return RouteServiceResult.Failure(
                message = throwable.message ?: "Unable to initialize route search service."
            )
        }
        return when (routeMode) {
            RouteMode.DRIVING -> calculateDrivingRoute(
                routeSearch = routeSearch,
                origin = origin,
                destination = destination,
                waypoints = waypoints
            )

            RouteMode.WALKING -> calculateWalkingRoute(
                routeSearch = routeSearch,
                origin = origin,
                destination = destination
            )
        }
    }

    private suspend fun calculateDrivingRoute(
        routeSearch: RouteSearchV2,
        origin: RoutePoint,
        destination: RoutePoint,
        waypoints: List<RoutePoint>
    ): RouteServiceResult = suspendCancellableCoroutine { continuation ->
        routeSearch.setRouteSearchListener(
            object : RouteSearchV2.OnRouteSearchListener {
                override fun onDriveRouteSearched(result: DriveRouteResultV2?, errorCode: Int) {
                    if (!continuation.isActive) return
                    if (errorCode != AMapException.CODE_AMAP_SUCCESS || result == null) {
                        continuation.resume(
                            RouteServiceResult.Failure(
                                message = "Driving route calculation failed.",
                                errorCode = errorCode
                            )
                        )
                        return
                    }
                    val alternatives = result.paths
                        .orEmpty()
                        .mapIndexedNotNull { index, path ->
                            path.toRouteAlternative(
                                index = index,
                                mode = RouteMode.DRIVING
                            )
                        }
                    if (alternatives.isEmpty()) {
                        continuation.resume(
                            RouteServiceResult.Failure("No driving route found.")
                        )
                    } else {
                        continuation.resume(RouteServiceResult.Success(alternatives))
                    }
                }

                override fun onWalkRouteSearched(result: WalkRouteResultV2?, errorCode: Int) = Unit
                override fun onBusRouteSearched(
                    busRouteResult: com.amap.api.services.route.BusRouteResultV2?,
                    errorCode: Int
                ) = Unit

                override fun onRideRouteSearched(
                    rideRouteResult: com.amap.api.services.route.RideRouteResultV2?,
                    errorCode: Int
                ) = Unit
            }
        )

        runCatching {
            val fromAndTo = RouteSearchV2.FromAndTo(
                origin.toLatLonPoint(),
                destination.toLatLonPoint()
            )
            val waypointPoints = waypoints.map { it.toLatLonPoint() }
            val query = RouteSearchV2.DriveRouteQuery(
                fromAndTo,
                RouteSearchV2.DrivingStrategy.DEFAULT,
                waypointPoints,
                null,
                null
            )
            routeSearch.calculateDriveRouteAsyn(query)
        }.onFailure { throwable ->
            if (continuation.isActive) {
                continuation.resume(
                    RouteServiceResult.Failure(
                        message = throwable.message ?: "Driving route request failed."
                    )
                )
            }
        }
    }

    private suspend fun calculateWalkingRoute(
        routeSearch: RouteSearchV2,
        origin: RoutePoint,
        destination: RoutePoint
    ): RouteServiceResult = suspendCancellableCoroutine { continuation ->
        routeSearch.setRouteSearchListener(
            object : RouteSearchV2.OnRouteSearchListener {
                override fun onWalkRouteSearched(result: WalkRouteResultV2?, errorCode: Int) {
                    if (!continuation.isActive) return
                    if (errorCode != AMapException.CODE_AMAP_SUCCESS || result == null) {
                        continuation.resume(
                            RouteServiceResult.Failure(
                                message = "Walking route calculation failed.",
                                errorCode = errorCode
                            )
                        )
                        return
                    }
                    val alternatives = result.paths
                        .orEmpty()
                        .mapIndexedNotNull { index, path ->
                            path.toRouteAlternative(
                                index = index,
                                mode = RouteMode.WALKING
                            )
                        }
                    if (alternatives.isEmpty()) {
                        continuation.resume(
                            RouteServiceResult.Failure("No walking route found.")
                        )
                    } else {
                        continuation.resume(RouteServiceResult.Success(alternatives))
                    }
                }

                override fun onDriveRouteSearched(result: DriveRouteResultV2?, errorCode: Int) = Unit
                override fun onBusRouteSearched(
                    busRouteResult: com.amap.api.services.route.BusRouteResultV2?,
                    errorCode: Int
                ) = Unit

                override fun onRideRouteSearched(
                    rideRouteResult: com.amap.api.services.route.RideRouteResultV2?,
                    errorCode: Int
                ) = Unit
            }
        )

        runCatching {
            val query = RouteSearchV2.WalkRouteQuery(
                RouteSearchV2.FromAndTo(
                    origin.toLatLonPoint(),
                    destination.toLatLonPoint()
                )
            )
            routeSearch.calculateWalkRouteAsyn(query)
        }.onFailure { throwable ->
            if (continuation.isActive) {
                continuation.resume(
                    RouteServiceResult.Failure(
                        message = throwable.message ?: "Walking route request failed."
                    )
                )
            }
        }
    }
}

private fun RoutePoint.toLatLonPoint(): LatLonPoint =
    LatLonPoint(latitude, longitude)

private fun DrivePathV2.toRouteAlternative(
    index: Int,
    mode: RouteMode
): RouteAlternative? {
    val polyline = polyline
        .orEmpty()
        .map { point -> RouteCoordinate(point.latitude, point.longitude) }
    if (polyline.isEmpty()) return null
    val steps = steps.orEmpty()
    return RouteAlternative(
        id = "drive_$index",
        mode = mode,
        distanceMeters = distance,
        durationSeconds = duration.toLong(),
        polylinePoints = polyline,
        stepCount = steps.size,
        instructionSummary = steps.firstOrNull()?.instruction
    )
}

private fun WalkPath.toRouteAlternative(
    index: Int,
    mode: RouteMode
): RouteAlternative? {
    val polyline = polyline
        .orEmpty()
        .map { point -> RouteCoordinate(point.latitude, point.longitude) }
    if (polyline.isEmpty()) return null
    val steps = steps.orEmpty()
    return RouteAlternative(
        id = "walk_$index",
        mode = mode,
        distanceMeters = distance,
        durationSeconds = duration.toLong(),
        polylinePoints = polyline,
        stepCount = steps.size,
        instructionSummary = steps.firstOrNull()?.instruction
    )
}
