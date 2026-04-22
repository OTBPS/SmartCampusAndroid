package com.cnpen.smartcampus.ui.search

import com.cnpen.smartcampus.data.model.Poi
import com.cnpen.smartcampus.data.model.PoiCategory
import com.cnpen.smartcampus.data.model.SearchSortOption

enum class SearchDisplayState {
    DEFAULT,
    FILTERED,
    NO_RESULTS
}

data class SearchResultUiModel(
    val poi: Poi,
    val isFavorite: Boolean
)

data class SearchUiState(
    val query: String = "",
    val selectedCategory: PoiCategory? = null,
    val selectedSort: SearchSortOption = SearchSortOption.POPULARITY,
    val results: List<SearchResultUiModel> = emptyList(),
    val resultCount: Int = 0,
    val displayState: SearchDisplayState = SearchDisplayState.DEFAULT,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val dataSourceLabel: String = "Local fake data"
) {
    val hasActiveFilters: Boolean
        get() = query.isNotBlank() || selectedCategory != null || selectedSort != SearchSortOption.POPULARITY
}
