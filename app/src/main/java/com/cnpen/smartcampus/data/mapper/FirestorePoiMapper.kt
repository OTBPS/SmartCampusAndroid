package com.cnpen.smartcampus.data.mapper

import com.cnpen.smartcampus.data.model.Poi
import com.cnpen.smartcampus.data.model.PoiCategory
import com.cnpen.smartcampus.data.remote.firestore.FirestoreCampusSchema
import com.google.firebase.firestore.DocumentSnapshot

fun DocumentSnapshot.toPoiOrNull(): Poi? {
    val id = getString(FirestoreCampusSchema.PoiFields.ID)
        ?.trim()
        ?.ifBlank { this.id }
        ?: this.id
    val name = getString(FirestoreCampusSchema.PoiFields.NAME)?.trim().orEmpty()
    if (id.isBlank() || name.isBlank()) return null

    val categoryRaw = getString(FirestoreCampusSchema.PoiFields.CATEGORY)
    val category = parsePoiCategory(categoryRaw)

    val description = getString(FirestoreCampusSchema.PoiFields.DESCRIPTION)
        ?.trim()
        ?.ifBlank { "No description available." }
        ?: "No description available."
    val latitude = getDouble(FirestoreCampusSchema.PoiFields.LATITUDE) ?: 0.0
    val longitude = getDouble(FirestoreCampusSchema.PoiFields.LONGITUDE) ?: 0.0
    val imageUrl = getString(FirestoreCampusSchema.PoiFields.IMAGE_URL)?.trim()?.takeIf { it.isNotEmpty() }
    val building = getString(FirestoreCampusSchema.PoiFields.BUILDING)
        ?.trim()
        ?.ifBlank { "Unknown building" }
        ?: "Unknown building"

    val keywords = when (val rawKeywords = get(FirestoreCampusSchema.PoiFields.KEYWORDS)) {
        is List<*> -> rawKeywords.mapNotNull { it?.toString()?.trim()?.takeIf(String::isNotEmpty) }
        else -> emptyList()
    }

    val popularity = (getLong(FirestoreCampusSchema.PoiFields.POPULARITY) ?: 0L).toInt()
    val updatedAt = getTimestamp(FirestoreCampusSchema.PoiFields.UPDATED_AT)?.toDate()?.time
        ?: getLong(FirestoreCampusSchema.PoiFields.UPDATED_AT)
        ?: System.currentTimeMillis()

    return Poi(
        id = id,
        name = name,
        category = category,
        description = description,
        latitude = latitude,
        longitude = longitude,
        imageUrl = imageUrl,
        building = building,
        keywords = keywords,
        popularity = popularity,
        updatedAt = updatedAt
    )
}

private fun parsePoiCategory(raw: String?): PoiCategory {
    if (raw.isNullOrBlank()) return PoiCategory.SERVICE
    PoiCategory.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }?.let { return it }
    PoiCategory.entries.firstOrNull { it.label.equals(raw, ignoreCase = true) }?.let { return it }
    return PoiCategory.SERVICE
}
