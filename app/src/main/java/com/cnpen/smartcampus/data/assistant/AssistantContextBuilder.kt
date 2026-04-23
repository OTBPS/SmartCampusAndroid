package com.cnpen.smartcampus.data.assistant

import com.cnpen.smartcampus.data.model.Poi
import com.cnpen.smartcampus.data.repository.CampusRepository
import com.cnpen.smartcampus.data.route.RoutePlanRepository

class AssistantContextBuilder(
    private val campusRepository: CampusRepository,
    private val routePlanRepository: RoutePlanRepository
) {
    fun build(userQuery: String): AssistantContextSnapshot {
        val allPois = campusRepository.observePois().value
        val favoriteIds = campusRepository.observeFavoriteIds().value
        val selectedPoiId = campusRepository.observeSelectedMapPoiId().value
        val routeState = routePlanRepository.getCurrentRoutePlanState()

        val selectedPoi = allPois.firstOrNull { it.id == selectedPoiId }?.toContextPoi()
        val favorites = allPois
            .filter { favoriteIds.contains(it.id) }
            .sortedWith(compareByDescending<Poi> { it.popularity }.thenBy { it.name.lowercase() })
            .take(MAX_FAVORITE_CONTEXT_SIZE)
            .map { poi -> poi.toContextPoi() }
        val relevantPois = allPois
            .findRelevantPois(userQuery)
            .ensureSelectedPoiFirst(selectedPoiId)
            .take(MAX_RELEVANT_POI_SIZE)
            .map { poi -> poi.toContextPoi() }

        val routeContext = if (!routeState.isRoutePlanningActive) {
            null
        } else {
            AssistantRouteContext(
                mode = routeState.routeMode.label,
                originLabel = routeState.origin?.label,
                destinationLabel = routeState.destination?.label,
                distanceMeters = routeState.selectedAlternative?.distanceMeters,
                durationSeconds = routeState.selectedAlternative?.durationSeconds
            )
        }

        return AssistantContextSnapshot(
            selectedPoi = selectedPoi,
            route = routeContext,
            favorites = favorites,
            relevantPois = relevantPois,
            dataSourceLabel = campusRepository.observeDataSourceLabel().value
        )
    }

    private fun Poi.toContextPoi(): AssistantPoiContext =
        AssistantPoiContext(
            id = id,
            name = name,
            category = category.label,
            building = building,
            description = description
        )

    private fun List<Poi>.findRelevantPois(query: String): List<Poi> {
        val normalized = query.trim().lowercase()
        if (normalized.isBlank()) {
            return sortedWith(
                compareByDescending<Poi> { it.popularity }.thenBy { it.name.lowercase() }
            )
        }
        val tokens = normalized.split(Regex("\\s+")).filter { it.isNotBlank() }
        return filter { poi ->
            val fields = buildList {
                add(poi.name.lowercase())
                add(poi.description.lowercase())
                add(poi.building.lowercase())
                addAll(poi.keywords.map { it.lowercase() })
            }
            tokens.any { token -> fields.any { it.contains(token) } }
        }.sortedWith(compareByDescending<Poi> { it.popularity }.thenBy { it.name.lowercase() })
    }

    private fun List<Poi>.ensureSelectedPoiFirst(selectedPoiId: String?): List<Poi> {
        val selectedId = selectedPoiId ?: return this
        val selected = firstOrNull { it.id == selectedId } ?: return this
        return listOf(selected) + filterNot { it.id == selectedId }
    }

    private companion object {
        const val MAX_FAVORITE_CONTEXT_SIZE = 5
        const val MAX_RELEVANT_POI_SIZE = 6
    }
}
