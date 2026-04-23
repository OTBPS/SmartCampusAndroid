package com.cnpen.smartcampus.data.model

data class RoutePickerContext(
    val pointType: RoutePointType,
    val waypointIndex: Int? = null
)
