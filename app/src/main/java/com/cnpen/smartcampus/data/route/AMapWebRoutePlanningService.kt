package com.cnpen.smartcampus.data.route

import android.net.Uri
import android.util.Log
import com.cnpen.smartcampus.BuildConfig
import com.cnpen.smartcampus.data.model.RouteAlternative
import com.cnpen.smartcampus.data.model.RouteCoordinate
import com.cnpen.smartcampus.data.model.RouteMode
import com.cnpen.smartcampus.data.model.RoutePoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class AMapWebRoutePlanningService : RoutePlanningService {

    override suspend fun calculateRoute(
        origin: RoutePoint,
        destination: RoutePoint,
        waypoints: List<RoutePoint>,
        routeMode: RouteMode
    ): RouteServiceResult = withContext(Dispatchers.IO) {
        val webApiKey = BuildConfig.AMAP_WEB_API_KEY.trim()
        if (webApiKey.isBlank()) {
            return@withContext RouteServiceResult.Failure(
                message = "AMap Web Service key is missing. Set AMAP_WEB_API_KEY in local.properties."
            )
        }

        runCatching {
            if (routeMode == RouteMode.WALKING && waypoints.isNotEmpty()) {
                calculateWalkingRouteWithWaypoints(
                    key = webApiKey,
                    origin = origin,
                    destination = destination,
                    waypoints = waypoints
                )
            } else {
                val url = buildRouteUrl(
                    key = webApiKey,
                    origin = origin,
                    destination = destination,
                    waypoints = waypoints,
                    routeMode = routeMode
                ) ?: return@runCatching RouteServiceResult.Failure(
                    message = "Unsupported route mode."
                )
                parseRouteResponse(
                    response = requestJson(url),
                    routeMode = routeMode
                )
            }
        }.fold(
            onSuccess = { result ->
                result
            },
            onFailure = { throwable ->
                Log.e(TAG, "AMap Web route request failed.", throwable)
                RouteServiceResult.Failure(
                    message = throwable.message ?: "Route request failed."
                )
            }
        )
    }

    private fun buildRouteUrl(
        key: String,
        origin: RoutePoint,
        destination: RoutePoint,
        waypoints: List<RoutePoint>,
        routeMode: RouteMode
    ): String? {
        val endpoint = when (routeMode) {
            RouteMode.DRIVING -> "https://restapi.amap.com/v3/direction/driving"
            RouteMode.WALKING -> "https://restapi.amap.com/v3/direction/walking"
        }

        val originParam = "${origin.longitude},${origin.latitude}"
        val destinationParam = "${destination.longitude},${destination.latitude}"

        val uriBuilder = Uri.parse(endpoint).buildUpon()
            .appendQueryParameter("key", key)
            .appendQueryParameter("origin", originParam)
            .appendQueryParameter("destination", destinationParam)

        if (routeMode == RouteMode.DRIVING) {
            uriBuilder.appendQueryParameter("extensions", "all")
            uriBuilder.appendQueryParameter("strategy", "0")
            if (waypoints.isNotEmpty()) {
                val waypointParam = waypoints.joinToString(";") {
                    "${it.longitude},${it.latitude}"
                }
                uriBuilder.appendQueryParameter("waypoints", waypointParam)
            }
        }

        return uriBuilder.build().toString()
    }

    private fun requestJson(url: String): JSONObject {
        Log.d(TAG, "AMap Web route URL requested.")
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            doInput = true
        }
        return connection.useAndParseJson()
    }

    private fun HttpURLConnection.useAndParseJson(): JSONObject {
        return try {
            val code = responseCode
            val stream = if (code in 200..299) inputStream else errorStream
            val body = stream?.bufferedText().orEmpty()
            if (body.isBlank()) {
                throw IllegalStateException("AMap response body is empty. httpCode=$code")
            }
            JSONObject(body)
        } finally {
            disconnect()
        }
    }

    private fun parseRouteResponse(
        response: JSONObject,
        routeMode: RouteMode
    ): RouteServiceResult {
        val status = response.optString("status")
        val info = response.optString("info")
        val infoCode = response.optString("infocode")
        Log.d(
            TAG,
            "AMap Web route response: mode=${routeMode.name}, status=$status, info=$info, infocode=$infoCode"
        )
        if (status != "1") {
            return RouteServiceResult.Failure(
                message = buildString {
                    append("AMap route API failed")
                    if (info.isNotBlank()) append(": ").append(info)
                },
                errorCode = infoCode.toIntOrNull()
            )
        }

        val route = response.optJSONObject("route")
            ?: return RouteServiceResult.Failure("AMap route API returned no route data.")
        val paths = route.optJSONArray("paths")
        Log.d(TAG, "AMap Web route paths count=${paths?.length() ?: 0}")
        if (paths == null || paths.length() == 0) {
            return RouteServiceResult.Failure(
                message = when (routeMode) {
                    RouteMode.DRIVING -> "No driving route found."
                    RouteMode.WALKING -> "No walking route found."
                }
            )
        }

        val alternatives = mutableListOf<RouteAlternative>()
        for (index in 0 until paths.length()) {
            val path = paths.optJSONObject(index) ?: continue
            val polylinePoints = parsePathPolyline(path)
            if (polylinePoints.isEmpty()) continue

            val distance = path.optString("distance").toFloatOrNull() ?: 0f
            val duration = path.optString("duration").toLongOrNull() ?: 0L
            val steps = path.optJSONArray("steps")
            val stepCount = steps?.length() ?: 0
            val firstInstruction = steps?.optJSONObject(0)?.optString("instruction")
                ?.takeIf { it.isNotBlank() }

            alternatives += RouteAlternative(
                id = "${routeMode.name.lowercase()}_$index",
                mode = routeMode,
                distanceMeters = distance,
                durationSeconds = duration,
                polylinePoints = polylinePoints,
                stepCount = stepCount,
                instructionSummary = firstInstruction
            )
        }

        if (alternatives.isEmpty()) {
            return RouteServiceResult.Failure("AMap route data contains no drawable polyline.")
        }
        return RouteServiceResult.Success(alternatives)
    }

    private fun calculateWalkingRouteWithWaypoints(
        key: String,
        origin: RoutePoint,
        destination: RoutePoint,
        waypoints: List<RoutePoint>
    ): RouteServiceResult {
        val segments = listOf(origin) + waypoints + destination
        if (segments.size < 2) {
            return RouteServiceResult.Failure("Walking route requires origin and destination.")
        }

        val mergedPolyline = mutableListOf<RouteCoordinate>()
        var totalDistance = 0f
        var totalDuration = 0L
        var totalSteps = 0
        val instructionSnippets = mutableListOf<String>()

        for (index in 0 until segments.lastIndex) {
            val segmentOrigin = segments[index]
            val segmentDestination = segments[index + 1]
            val url = buildRouteUrl(
                key = key,
                origin = segmentOrigin,
                destination = segmentDestination,
                waypoints = emptyList(),
                routeMode = RouteMode.WALKING
            ) ?: return RouteServiceResult.Failure("Unsupported walking route segment.")

            val segmentResult = parseRouteResponse(
                response = requestJson(url),
                routeMode = RouteMode.WALKING
            )

            when (segmentResult) {
                is RouteServiceResult.Failure -> {
                    return RouteServiceResult.Failure(
                        message = "Walking segment ${index + 1} failed: ${segmentResult.message}",
                        errorCode = segmentResult.errorCode
                    )
                }

                is RouteServiceResult.Success -> {
                    val firstAlternative = segmentResult.alternatives.firstOrNull()
                        ?: return RouteServiceResult.Failure(
                            "Walking segment ${index + 1} returned no route."
                        )

                    totalDistance += firstAlternative.distanceMeters
                    totalDuration += firstAlternative.durationSeconds
                    totalSteps += firstAlternative.stepCount
                    appendDistinctPolyline(
                        target = mergedPolyline,
                        additional = firstAlternative.polylinePoints
                    )
                    firstAlternative.instructionSummary
                        ?.takeIf { it.isNotBlank() }
                        ?.let(instructionSnippets::add)
                }
            }
        }

        if (mergedPolyline.size < 2) {
            return RouteServiceResult.Failure("Walking route with waypoints has no drawable polyline.")
        }

        val summary = buildString {
            if (instructionSnippets.isNotEmpty()) {
                append(instructionSnippets.first())
            } else {
                append("Walking route")
            }
            append(" via ")
            append(waypoints.size)
            append(if (waypoints.size == 1) " stop." else " stops.")
        }

        return RouteServiceResult.Success(
            alternatives = listOf(
                RouteAlternative(
                    id = "walking_waypoints_0",
                    mode = RouteMode.WALKING,
                    distanceMeters = totalDistance,
                    durationSeconds = totalDuration,
                    polylinePoints = mergedPolyline,
                    stepCount = totalSteps,
                    instructionSummary = summary
                )
            )
        )
    }

    private fun appendDistinctPolyline(
        target: MutableList<RouteCoordinate>,
        additional: List<RouteCoordinate>
    ) {
        for (point in additional) {
            val last = target.lastOrNull()
            if (last == null || last.latitude != point.latitude || last.longitude != point.longitude) {
                target += point
            }
        }
    }

    private fun parsePathPolyline(path: JSONObject): List<RouteCoordinate> {
        val steps = path.optJSONArray("steps") ?: return emptyList()
        val points = mutableListOf<RouteCoordinate>()
        for (i in 0 until steps.length()) {
            val step = steps.optJSONObject(i) ?: continue
            val polylineText = step.optString("polyline")
            if (polylineText.isBlank()) continue
            val stepPoints = parsePolyline(polylineText)
            for (point in stepPoints) {
                val last = points.lastOrNull()
                if (last == null || last.latitude != point.latitude || last.longitude != point.longitude) {
                    points += point
                }
            }
        }
        return points
    }

    private fun parsePolyline(polyline: String): List<RouteCoordinate> {
        return polyline.split(';')
            .mapNotNull { lngLat ->
                val parts = lngLat.split(',')
                if (parts.size != 2) return@mapNotNull null
                val lng = parts[0].toDoubleOrNull() ?: return@mapNotNull null
                val lat = parts[1].toDoubleOrNull() ?: return@mapNotNull null
                RouteCoordinate(latitude = lat, longitude = lng)
            }
    }

    private fun java.io.InputStream.bufferedText(): String =
        BufferedReader(InputStreamReader(this)).use { it.readText() }

    private companion object {
        const val TAG = "AMAP_ROUTE_WEB"
        const val CONNECT_TIMEOUT_MS = 10_000
        const val READ_TIMEOUT_MS = 15_000
    }
}
