package com.cnpen.smartcampus.data.model

data class Poi(
    val id: String,
    val name: String,
    val category: PoiCategory,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String?,
    val building: String,
    val keywords: List<String>,
    val popularity: Int,
    val updatedAt: Long
)
