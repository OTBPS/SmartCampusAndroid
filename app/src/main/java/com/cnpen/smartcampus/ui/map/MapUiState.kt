package com.cnpen.smartcampus.ui.map

import com.cnpen.smartcampus.data.model.Poi

data class MapUiState(
    val title: String = "Campus Map",
    val mapPlaceholderText: String = "Future Amap map area",
    val selectedPoi: Poi? = null,
    val focusStatus: String = "Map focus is ready for future marker integration."
)
