package com.cnpen.smartcampus.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cnpen.smartcampus.data.repository.CampusRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MapViewModel : ViewModel() {
    private val repository = CampusRepositoryProvider.repository
    private val locationStatus = MutableStateFlow("Tap the location button to center future map focus.")

    val uiState: StateFlow<MapUiState> = combine(
        repository.observePois(),
        repository.observeSelectedMapPoiId(),
        locationStatus
    ) { pois, selectedPoiId, status ->
        val selectedPoi = selectedPoiId?.let { id -> pois.firstOrNull { it.id == id } }
        MapUiState(
            selectedPoi = selectedPoi,
            focusStatus = status
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MapUiState()
    )

    fun onLocateClick() {
        locationStatus.value = "Location placeholder triggered. Amap current-location mode will be connected in a future round."
    }
}
