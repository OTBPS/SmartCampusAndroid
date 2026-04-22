package com.cnpen.smartcampus.ui.home

import com.cnpen.smartcampus.data.model.Poi

data class HomeUiState(
    val title: String = "Smart Campus Navigation",
    val subtitle: String = "Find places quickly across campus",
    val quickSearchHint: String = "Search for library, cafeteria, service hall...",
    val recommendedPois: List<Poi> = emptyList(),
    val favoritesCount: Int = 0
)
