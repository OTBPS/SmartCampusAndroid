package com.cnpen.smartcampus.ui.search

import com.cnpen.smartcampus.data.model.Poi
import com.cnpen.smartcampus.data.model.PoiCategory
import com.cnpen.smartcampus.data.model.SearchSortOption

data class SearchUiState(
    val query: String = "",
    val selectedCategory: PoiCategory? = null,
    val selectedSort: SearchSortOption = SearchSortOption.POPULARITY,
    val results: List<Poi> = emptyList()
)
