package com.cnpen.smartcampus.ui.detail

import com.cnpen.smartcampus.data.model.Poi

data class PoiDetailUiState(
    val poi: Poi? = null,
    val isFavorite: Boolean = false,
    val errorMessage: String? = null
)
