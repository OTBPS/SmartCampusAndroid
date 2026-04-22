package com.cnpen.smartcampus.di

import android.content.pm.ApplicationInfo
import android.content.Context
import com.cnpen.smartcampus.data.repository.CampusRepository
import com.cnpen.smartcampus.data.repository.FirestoreCampusRepository
import com.cnpen.smartcampus.data.repository.FakeCampusRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class DefaultAppContainer(
    private val context: Context
) : AppContainer {
    override val campusRepository: CampusRepository by lazy {
        if (isFirebaseConfigured()) {
            FirestoreCampusRepository(
                firestore = FirebaseFirestore.getInstance(),
                enableDebugSeed = isDebuggableBuild()
            )
        } else {
            FakeCampusRepository()
        }
    }

    private fun isFirebaseConfigured(): Boolean =
        runCatching { FirebaseApp.initializeApp(context) != null }
            .getOrDefault(false)

    private fun isDebuggableBuild(): Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
}
