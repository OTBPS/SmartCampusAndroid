package com.cnpen.smartcampus.data.remote.firestore

import com.cnpen.smartcampus.data.model.Poi
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object DebugFirestorePoiSeeder {

    fun seedPoisIfEmpty(
        firestore: FirebaseFirestore,
        seedPois: List<Poi>,
        enabled: Boolean,
        onError: (String) -> Unit = {}
    ) {
        if (!enabled || seedPois.isEmpty()) return

        val poiCollection = firestore.collection(FirestoreCampusSchema.POIS_COLLECTION)
        poiCollection.limit(1).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) return@addOnSuccessListener

                val batch = firestore.batch()
                seedPois.forEach { poi ->
                    val documentRef = poiCollection.document(poi.id)
                    batch.set(documentRef, poi.toFirestoreSeedMap())
                }
                batch.commit()
                    .addOnFailureListener { error ->
                        onError(error.message ?: "Failed to seed Firestore POIs.")
                    }
            }
            .addOnFailureListener { error ->
                onError(error.message ?: "Unable to check Firestore POI seed state.")
            }
    }

    private fun Poi.toFirestoreSeedMap(): Map<String, Any?> =
        mapOf(
            FirestoreCampusSchema.PoiFields.ID to id,
            FirestoreCampusSchema.PoiFields.NAME to name,
            FirestoreCampusSchema.PoiFields.CATEGORY to category.name,
            FirestoreCampusSchema.PoiFields.DESCRIPTION to description,
            FirestoreCampusSchema.PoiFields.LATITUDE to latitude,
            FirestoreCampusSchema.PoiFields.LONGITUDE to longitude,
            FirestoreCampusSchema.PoiFields.IMAGE_URL to imageUrl,
            FirestoreCampusSchema.PoiFields.BUILDING to building,
            FirestoreCampusSchema.PoiFields.KEYWORDS to keywords,
            FirestoreCampusSchema.PoiFields.POPULARITY to popularity,
            FirestoreCampusSchema.PoiFields.UPDATED_AT to FieldValue.serverTimestamp()
        )
}
