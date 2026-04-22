package com.cnpen.smartcampus.data.repository

import com.cnpen.smartcampus.data.model.Poi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeCampusRepository : CampusRepository {
    private val poiState = MutableStateFlow(FakeCampusDataSource.poiList)
    private val favoriteIdsState = MutableStateFlow(
        setOf(
            "poi_library",
            "poi_cafeteria"
        )
    )
    private val selectedMapPoiIdState = MutableStateFlow<String?>("poi_library")

    override fun observePois(): StateFlow<List<Poi>> = poiState.asStateFlow()

    override fun observeFavoriteIds(): StateFlow<Set<String>> = favoriteIdsState.asStateFlow()

    override fun observeSelectedMapPoiId(): StateFlow<String?> = selectedMapPoiIdState.asStateFlow()

    override fun getPoiById(poiId: String): Poi? = poiState.value.firstOrNull { it.id == poiId }

    override fun addFavorite(poiId: String) {
        favoriteIdsState.update { old -> old + poiId }
    }

    override fun removeFavorite(poiId: String) {
        favoriteIdsState.update { old -> old - poiId }
    }

    override fun toggleFavorite(poiId: String) {
        favoriteIdsState.update { old ->
            if (old.contains(poiId)) old - poiId else old + poiId
        }
    }

    override fun setSelectedMapPoi(poiId: String?) {
        selectedMapPoiIdState.value = poiId
    }
}
