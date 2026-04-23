package com.cnpen.smartcampus.di

import com.cnpen.smartcampus.data.local.ThemePreferenceRepository
import com.cnpen.smartcampus.data.repository.CampusRepository
import com.cnpen.smartcampus.data.route.RoutePlanRepository

interface AppContainer {
    val campusRepository: CampusRepository
    val themePreferenceRepository: ThemePreferenceRepository
    val routePlanRepository: RoutePlanRepository
}
