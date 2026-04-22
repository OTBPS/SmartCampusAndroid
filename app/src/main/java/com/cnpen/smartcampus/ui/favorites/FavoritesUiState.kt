package com.cnpen.smartcampus.ui.favorites

import com.cnpen.smartcampus.data.model.Poi

data class FavoritesUiState(
    val favorites: List<Poi> = emptyList()
) {
    val isEmpty: Boolean = favorites.isEmpty()
}
