package com.cnpen.smartcampus.data.remote.firestore

import android.util.Log
import com.cnpen.smartcampus.data.model.Poi
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object DebugFirestorePoiSeeder {
    private const val TAG = "NUIST_POI_SEED"

    fun seedPoisIfEmpty(
        firestore: FirebaseFirestore,
        seedPois: List<Poi>,
        enabled: Boolean,
        onError: (String) -> Unit = {}
    ) {
        Log.d(
            TAG,
            "seedPoisIfEmpty called: enabled=$enabled, seedListSize=${seedPois.size}, collection=${FirestoreCampusSchema.POIS_COLLECTION}"
        )

        if (!enabled) {
            Log.d(TAG, "Skip seeding: enabled=false")
            return
        }

        if (seedPois.isEmpty()) {
            Log.w(TAG, "Skip seeding: seedPois is empty")
            return
        }

        val poiCollection = firestore.collection(FirestoreCampusSchema.POIS_COLLECTION)
        poiCollection.limit(1).get()
            .addOnSuccessListener { snapshot ->
                Log.d(
                    TAG,
                    "Collection empty check: collection=${FirestoreCampusSchema.POIS_COLLECTION}, snapshotSize=${snapshot.size()}, isEmpty=${snapshot.isEmpty}"
                )

                if (!snapshot.isEmpty) {
                    Log.d(TAG, "Skip commit: collection is not empty")
                    return@addOnSuccessListener
                }

                val batch = firestore.batch()
                seedPois.forEach { poi ->
                    val documentRef = poiCollection.document(poi.id)
                    batch.set(documentRef, poi.toFirestoreSeedMap())
                }
                batch.commit()
                    .addOnSuccessListener {
                        Log.i(
                            TAG,
                            "Seed commit success: collection=${FirestoreCampusSchema.POIS_COLLECTION}, insertedCount=${seedPois.size}"
                        )
                    }
                    .addOnFailureListener { error ->
                        Log.e(
                            TAG,
                            "Seed commit failure: collection=${FirestoreCampusSchema.POIS_COLLECTION}",
                            error
                        )
                        onError(error.message ?: "Failed to seed Firestore POIs.")
                    }
            }
            .addOnFailureListener { error ->
                Log.e(
                    TAG,
                    "Collection empty check failure: collection=${FirestoreCampusSchema.POIS_COLLECTION}",
                    error
                )
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
