package com.cnpen.smartcampus.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cnpen.smartcampus.data.model.Poi
import com.cnpen.smartcampus.data.model.PoiCategory
import com.cnpen.smartcampus.data.model.RouteEntrySource
import com.cnpen.smartcampus.data.model.RoutePoint
import com.cnpen.smartcampus.data.model.RoutePointType
import com.cnpen.smartcampus.data.model.SearchSortOption
import com.cnpen.smartcampus.data.repository.CampusRepository
import com.cnpen.smartcampus.data.route.RoutePlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: CampusRepository,
    private val routePlanRepository: RoutePlanRepository
) : ViewModel() {
    private val queryState = MutableStateFlow("")
    private val categoryState = MutableStateFlow<PoiCategory?>(null)
    private val sortState = MutableStateFlow(SearchSortOption.POPULARITY)

    private val filteredState = combine(
        repository.observePois(),
        repository.observeFavoriteIds(),
        queryState,
        categoryState,
        sortState
    ) { pois, favoriteIds, query, category, sort ->
        val filtered = pois
            .filterByQuery(query)
            .filterByCategory(category)
            .sortedByOption(sort)

        val displayState = when {
            filtered.isEmpty() && (query.isNotBlank() || category != null) -> SearchDisplayState.NO_RESULTS
            query.isBlank() && category == null -> SearchDisplayState.DEFAULT
            else -> SearchDisplayState.FILTERED
        }

        SearchUiState(
            query = query,
            selectedCategory = category,
            selectedSort = sort,
            results = filtered.map { poi ->
                SearchResultUiModel(
                    poi = poi,
                    isFavorite = favoriteIds.contains(poi.id)
                )
            },
            resultCount = filtered.size,
            displayState = displayState
        )
    }

    val uiState: StateFlow<SearchUiState> = combine(
        filteredState,
        repository.observeIsLoading(),
        repository.observeErrorMessage(),
        repository.observeDataSourceLabel()
    ) { filteredUiState, isLoading, errorMessage, dataSourceLabel ->
        filteredUiState.copy(
            isLoading = isLoading,
            errorMessage = errorMessage,
            dataSourceLabel = dataSourceLabel
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchUiState()
    )

    fun onQueryChange(value: String) {
        queryState.value = value
    }

    fun onSelectCategory(category: PoiCategory?) {
        categoryState.value = category
    }

    fun onSelectSort(option: SearchSortOption) {
        sortState.value = option
    }

    fun clearQuery() {
        queryState.update { "" }
    }

    fun resetFilters() {
        queryState.value = ""
        categoryState.value = null
        sortState.value = SearchSortOption.POPULARITY
    }

    fun onToggleFavorite(poiId: String) {
        repository.toggleFavorite(poiId)
    }

    fun onOpenOnMap(poiId: String) {
        repository.setSelectedMapPoi(poiId)
        val selectedPoi = repository.getPoiById(poiId) ?: return
        val routeState = routePlanRepository.getCurrentRoutePlanState()
        if (!routeState.isRoutePlanningActive) return

        routePlanRepository.startRoutePlanning(RouteEntrySource.SEARCH)
        routePlanRepository.setDestination(selectedPoi.toDestinationRoutePoint())
        if (routePlanRepository.getCurrentRoutePlanState().canCalculate) {
            viewModelScope.launch {
                routePlanRepository.calculateRoute()
            }
        }
    }

    fun clearError() {
        repository.clearError()
    }
}

private fun List<Poi>.filterByQuery(query: String): List<Poi> {
    val normalized = query.trim().lowercase()
    if (normalized.isBlank()) return this
    return filter { poi ->
        poi.name.lowercase().contains(normalized) ||
            poi.description.lowercase().contains(normalized) ||
            poi.building.lowercase().contains(normalized) ||
            poi.keywords.any { it.lowercase().contains(normalized) }
    }
}

private fun List<Poi>.filterByCategory(category: PoiCategory?): List<Poi> {
    if (category == null) return this
    return filter { it.category == category }
}

private fun List<Poi>.sortedByOption(sort: SearchSortOption): List<Poi> =
    when (sort) {
        SearchSortOption.NAME_ASC -> sortedBy { it.name.lowercase() }
        SearchSortOption.NAME_DESC -> sortedByDescending { it.name.lowercase() }
        SearchSortOption.CATEGORY -> sortedWith(compareBy({ it.category.label }, { it.name.lowercase() }))
        SearchSortOption.POPULARITY -> sortedByDescending { it.popularity }
    }

private fun Poi.toDestinationRoutePoint(): RoutePoint =
    RoutePoint(
        id = "${id}_${RoutePointType.DESTINATION.name.lowercase()}",
        poiId = id,
        label = name,
        subtitle = building,
        latitude = latitude,
        longitude = longitude,
        type = RoutePointType.DESTINATION
    )
