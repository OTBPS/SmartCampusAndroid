package com.cnpen.smartcampus.data.model

data class RouteAlternative(
    val id: String,
    val mode: RouteMode,
    val distanceMeters: Float,
    val durationSeconds: Long,
    val polylinePoints: List<RouteCoordinate>,
    val stepCount: Int,
    val instructionSummary: String?
)
