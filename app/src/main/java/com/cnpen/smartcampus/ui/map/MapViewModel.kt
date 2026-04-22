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
        val focusMessage = status ?: if (selectedPoi == null) {
            "No destination focus is set yet."
        } else {
            "Selected destination is prepared for future Amap marker focus."
        }
        val destinationHint = if (selectedPoi == null) {
            "Choose a place from Search or open View on Map from Place Detail."
        } else {
            "Chosen destination: ${selectedPoi.name} (${selectedPoi.category.label})"
        }
        MapUiState(
            selectedPoi = selectedPoi,
            focusStatus = focusMessage,
            destinationHint = destinationHint
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
