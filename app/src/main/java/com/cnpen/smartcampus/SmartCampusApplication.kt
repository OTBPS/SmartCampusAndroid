package com.cnpen.smartcampus

import android.app.Application
import com.amap.api.maps.MapsInitializer
import com.cnpen.smartcampus.di.AppContainer
import com.cnpen.smartcampus.di.DefaultAppContainer

class SmartCampusApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)
        container = DefaultAppContainer(this)
    }
}
