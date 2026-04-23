package com.cnpen.smartcampus.di

import android.content.pm.ApplicationInfo
import android.content.Context
import android.util.Log
import com.cnpen.smartcampus.data.local.ThemePreferenceRepository
import com.cnpen.smartcampus.data.repository.CampusRepository
import com.cnpen.smartcampus.data.repository.FirestoreCampusRepository
import com.cnpen.smartcampus.data.repository.FakeCampusRepository
import com.cnpen.smartcampus.data.route.AMapWebRoutePlanningService
import com.cnpen.smartcampus.data.route.InMemoryRoutePlanRepository
import com.cnpen.smartcampus.data.route.RoutePlanRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class DefaultAppContainer(
    private val context: Context
) : AppContainer {
    private companion object {
        const val TAG = "NUIST_POI_SEED"
    }

    override val themePreferenceRepository: ThemePreferenceRepository by lazy {
        ThemePreferenceRepository(context.applicationContext)
    }

    override val routePlanRepository: RoutePlanRepository by lazy {
        InMemoryRoutePlanRepository(
            routePlanningService = AMapWebRoutePlanningService()
        )
    }

    override val campusRepository: CampusRepository by lazy {
        if (isFirebaseConfigured()) {
            val debugSeedEnabled = isDebuggableBuild()
            Log.d(
                TAG,
                "Creating FirestoreCampusRepository: firebaseConfigured=true, debuggableBuild=$debugSeedEnabled"
            )
            FirestoreCampusRepository(
                firestore = FirebaseFirestore.getInstance(),
                enableDebugSeed = debugSeedEnabled
            )
        } else {
            Log.d(TAG, "Creating FakeCampusRepository: firebaseConfigured=false")
            FakeCampusRepository()
        }
    }

    private fun isFirebaseConfigured(): Boolean =
        runCatching { FirebaseApp.initializeApp(context) != null }
            .getOrDefault(false)

    private fun isDebuggableBuild(): Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
}
