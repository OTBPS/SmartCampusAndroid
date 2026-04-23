package com.cnpen.smartcampus.data.model

data class RoutePoint(
    val id: String,
    val poiId: String? = null,
    val label: String,
    val subtitle: String? = null,
    val latitude: Double,
    val longitude: Double,
    val type: RoutePointType
)
