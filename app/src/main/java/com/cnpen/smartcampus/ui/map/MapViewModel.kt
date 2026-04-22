package com.cnpen.smartcampus.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cnpen.smartcampus.data.repository.CampusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MapViewModel(
    private val repository: CampusRepository
) : ViewModel() {
    private val locationStatus = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MapUiState> = combine(
        repository.observePois(),
        repository.observeSelectedMapPoiId(),
        locationStatus
    ) { pois, selectedPoiId, status ->
        val selectedPoi = selectedPoiId?.let { id -> pois.firstOrNull { it.id == id } }
        val fallbackCenter = if (pois.isEmpty()) {
            22.3020 to 114.1775
        } else {
            val latitude = pois.map { it.latitude }.average()
            val longitude = pois.map { it.longitude }.average()
            latitude to longitude
        }
        val focusMessage = status ?: if (selectedPoi == null) {
            "No destination selected. Showing campus overview."
        } else {
            "Map is focused on the selected destination."
        }
        val destinationHint = if (selectedPoi == null) {
            "Choose a place from Search or open View on Map from Place Detail."
        } else {
            "Chosen destination: ${selectedPoi.name} (${selectedPoi.category.label})"
        }
        MapUiState(
            selectedPoi = selectedPoi,
            focusStatus = focusMessage,
            destinationHint = destinationHint,
            fallbackCenterLatitude = fallbackCenter.first,
            fallbackCenterLongitude = fallbackCenter.second
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MapUiState()
    )

    fun onLocateClick() {
        locationStatus.value = "Location action triggered. Future Amap current-location behavior will be connected here."
    }
}
