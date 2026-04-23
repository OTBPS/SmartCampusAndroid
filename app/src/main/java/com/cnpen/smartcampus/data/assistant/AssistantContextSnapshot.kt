package com.cnpen.smartcampus.data.assistant

import org.json.JSONArray
import org.json.JSONObject

data class AssistantPoiContext(
    val id: String,
    val name: String,
    val category: String,
    val building: String,
    val description: String
)

data class AssistantRouteContext(
    val mode: String,
    val originLabel: String?,
    val destinationLabel: String?,
    val distanceMeters: Float?,
    val durationSeconds: Long?
)

data class AssistantContextSnapshot(
    val selectedPoi: AssistantPoiContext?,
    val route: AssistantRouteContext?,
    val favorites: List<AssistantPoiContext>,
    val relevantPois: List<AssistantPoiContext>,
    val dataSourceLabel: String
) {
    fun buildContextHint(): String {
        val sections = mutableListOf<String>()
        if (selectedPoi != null) {
            sections += "selected place"
        }
        if (route != null) {
            sections += "current route"
        }
        if (favorites.isNotEmpty()) {
            sections += "favorites"
        }
        return if (sections.isEmpty()) {
            "Using campus POI data."
        } else {
            "Using ${sections.joinToString(", ")}."
        }
    }

    fun toJsonString(): String {
        val root = JSONObject()
            .put("dataSourceLabel", dataSourceLabel)
            .put("selectedPoi", selectedPoi?.toJson())
            .put("route", route?.toJson())
            .put("favorites", favorites.toJsonArray())
            .put("relevantPois", relevantPois.toJsonArray())
        return root.toString(2)
    }
}

private fun AssistantPoiContext.toJson(): JSONObject =
    JSONObject()
        .put("id", id)
        .put("name", name)
        .put("category", category)
        .put("building", building)
        .put("description", description)

private fun AssistantRouteContext.toJson(): JSONObject =
    JSONObject()
        .put("mode", mode)
        .put("originLabel", originLabel)
        .put("destinationLabel", destinationLabel)
        .put("distanceMeters", distanceMeters)
        .put("durationSeconds", durationSeconds)

private fun List<AssistantPoiContext>.toJsonArray(): JSONArray {
    val array = JSONArray()
    forEach { poi ->
        array.put(poi.toJson())
    }
    return array
}
