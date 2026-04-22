package com.cnpen.smartcampus.ui.favorites

import com.cnpen.smartcampus.data.model.Poi

data class FavoritesUiState(
    val favorites: List<Poi> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val dataSourceLabel: String = "Local fake data"
) {
    val isEmpty: Boolean = favorites.isEmpty()
}
