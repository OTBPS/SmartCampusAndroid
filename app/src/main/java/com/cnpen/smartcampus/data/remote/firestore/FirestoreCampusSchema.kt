package com.cnpen.smartcampus.data.remote.firestore

object FirestoreCampusSchema {
    const val POIS_COLLECTION = "pois"
    const val FAVORITES_COLLECTION = "favorites"

    object PoiFields {
        const val ID = "id"
        const val NAME = "name"
        const val CATEGORY = "category"
        const val DESCRIPTION = "description"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val IMAGE_URL = "imageUrl"
        const val BUILDING = "building"
        const val KEYWORDS = "keywords"
        const val POPULARITY = "popularity"
        const val UPDATED_AT = "updatedAt"
    }

    object FavoriteFields {
        const val POI_ID = "poiId"
        const val UPDATED_AT = "updatedAt"
    }
}
