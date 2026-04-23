package com.cnpen.smartcampus.data.repository

import android.util.Log
import com.cnpen.smartcampus.data.mapper.toPoiOrNull
import com.cnpen.smartcampus.data.model.Poi
import com.cnpen.smartcampus.data.remote.firestore.DebugFirestorePoiSeeder
import com.cnpen.smartcampus.data.remote.firestore.DebugFirestorePoiSeedData
import com.cnpen.smartcampus.data.remote.firestore.FirestoreCampusSchema
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FirestoreCampusRepository(
    private val firestore: FirebaseFirestore,
    private val fallbackPois: List<Poi> = FakeCampusDataSource.poiList,
    private val debugSeedPois: List<Poi> = DebugFirestorePoiSeedData.poiList,
    private val enableDebugSeed: Boolean = false
) : CampusRepository {
    private companion object {
        const val TAG = "NUIST_POI_SEED"
    }

    private val poiState = MutableStateFlow(emptyList<Poi>())
    private val favoriteIdsState = MutableStateFlow(emptySet<String>())
    private val selectedMapPoiIdState = MutableStateFlow<String?>(null)
    private val isLoadingState = MutableStateFlow(true)
    private val errorMessageState = MutableStateFlow<String?>(null)
    private val dataSourceLabelState = MutableStateFlow("Loading Firestore data...")

    private var poiLoaded = false
    private var favoritesLoaded = false

    private var poiListener: ListenerRegistration? = null
    private var favoritesListener: ListenerRegistration? = null

    init {
        Log.d(
            TAG,
            "FirestoreCampusRepository init: enableDebugSeed=$enableDebugSeed, debugSeedPoisSize=${debugSeedPois.size}, poisCollection=${FirestoreCampusSchema.POIS_COLLECTION}"
        )
        if (debugSeedPois.size != 10) {
            Log.w(TAG, "Unexpected debug seed dataset size: ${debugSeedPois.size} (expected 10)")
        }

        DebugFirestorePoiSeeder.seedPoisIfEmpty(
            firestore = firestore,
            seedPois = debugSeedPois,
            enabled = enableDebugSeed,
            onError = { message ->
                errorMessageState.value = message
            }
        )
        startListeners()
    }

    private fun startListeners() {
        poiListener?.remove()
        favoritesListener?.remove()

        poiListener = firestore.collection(FirestoreCampusSchema.POIS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onPoiListenerError(error.message)
                    markPoisLoaded()
                    return@addSnapshotListener
                }

                val mappedPois = snapshot
                    ?.documents
                    ?.mapNotNull { it.toPoiOrNull() }
                    .orEmpty()
                    .sortedByDescending { it.popularity }

                if (mappedPois.isEmpty()) {
                    poiState.value = fallbackPois
                    dataSourceLabelState.value = "Local fallback POI data (Firestore pois is empty)."
                } else {
                    poiState.value = mappedPois
                    dataSourceLabelState.value = "Firestore"
                }

                ensureSelectedPoiIsValid()
                markPoisLoaded()
            }

        favoritesListener = firestore.collection(FirestoreCampusSchema.FAVORITES_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFavoriteListenerError(error.message)
                    markFavoritesLoaded()
                    return@addSnapshotListener
                }

                val favoriteIds = snapshot
                    ?.documents
                    ?.mapNotNull { document ->
                        document.getString(FirestoreCampusSchema.FavoriteFields.POI_ID)
                            ?.trim()
                            ?.takeIf { it.isNotEmpty() }
                            ?: document.id.takeIf { it.isNotBlank() }
                    }
                    .orEmpty()
                    .toSet()

                favoriteIdsState.value = favoriteIds
                markFavoritesLoaded()
            }
    }

    private fun markPoisLoaded() {
        poiLoaded = true
        updateLoadingState()
    }

    private fun markFavoritesLoaded() {
        favoritesLoaded = true
        updateLoadingState()
    }

    private fun updateLoadingState() {
        isLoadingState.value = !(poiLoaded && favoritesLoaded)
    }

    private fun onPoiListenerError(errorMessage: String?) {
        if (poiState.value.isEmpty()) {
            poiState.value = fallbackPois
            dataSourceLabelState.value = "Local fallback POI data"
        }
        this.errorMessageState.value = errorMessage ?: "Unable to load campus places from Firestore."
    }

    private fun onFavoriteListenerError(errorMessage: String?) {
        this.errorMessageState.value = errorMessage ?: "Unable to load favorites from Firestore."
    }

    private fun ensureSelectedPoiIsValid() {
        val current = selectedMapPoiIdState.value
        val currentList = poiState.value
        val hasCurrent = current == null || currentList.any { it.id == current }
        if (!hasCurrent) {
            selectedMapPoiIdState.value = null
        }
    }

    override fun observePois(): StateFlow<List<Poi>> = poiState.asStateFlow()

    override fun observeFavoriteIds(): StateFlow<Set<String>> = favoriteIdsState.asStateFlow()

    override fun observeSelectedMapPoiId(): StateFlow<String?> = selectedMapPoiIdState.asStateFlow()

    override fun observeIsLoading(): StateFlow<Boolean> = isLoadingState.asStateFlow()

    override fun observeErrorMessage(): StateFlow<String?> = errorMessageState.asStateFlow()

    override fun observeDataSourceLabel(): StateFlow<String> = dataSourceLabelState.asStateFlow()

    override fun getPoiById(poiId: String): Poi? = poiState.value.firstOrNull { it.id == poiId }

    override fun addFavorite(poiId: String) {
        if (poiId.isBlank()) return
        favoriteIdsState.update { old -> old + poiId }
        firestore.collection(FirestoreCampusSchema.FAVORITES_COLLECTION)
            .document(poiId)
            .set(
                mapOf(
                    FirestoreCampusSchema.FavoriteFields.POI_ID to poiId,
                    FirestoreCampusSchema.FavoriteFields.UPDATED_AT to FieldValue.serverTimestamp()
                )
            )
            .addOnFailureListener { throwable ->
                errorMessageState.value = throwable.message ?: "Failed to add favorite."
            }
    }

    override fun removeFavorite(poiId: String) {
        if (poiId.isBlank()) return
        favoriteIdsState.update { old -> old - poiId }
        firestore.collection(FirestoreCampusSchema.FAVORITES_COLLECTION)
            .document(poiId)
            .delete()
            .addOnFailureListener { throwable ->
                errorMessageState.value = throwable.message ?: "Failed to remove favorite."
            }
    }

    override fun toggleFavorite(poiId: String) {
        if (favoriteIdsState.value.contains(poiId)) {
            removeFavorite(poiId)
        } else {
            addFavorite(poiId)
        }
    }

    override fun setSelectedMapPoi(poiId: String?) {
        selectedMapPoiIdState.value = poiId
    }

    override fun clearError() {
        errorMessageState.value = null
    }
}
