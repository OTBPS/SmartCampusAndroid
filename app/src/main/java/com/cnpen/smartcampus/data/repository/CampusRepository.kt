package com.cnpen.smartcampus.data.repository

import com.cnpen.smartcampus.data.model.Poi
import kotlinx.coroutines.flow.StateFlow

interface CampusRepository {
    fun observePois(): StateFlow<List<Poi>>
    fun observeFavoriteIds(): StateFlow<Set<String>>
    fun observeSelectedMapPoiId(): StateFlow<String?>
    fun getPoiById(poiId: String): Poi?
    fun addFavorite(poiId: String)
    fun removeFavorite(poiId: String)
    fun toggleFavorite(poiId: String)
    fun setSelectedMapPoi(poiId: String?)
}
