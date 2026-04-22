package com.cnpen.smartcampus.di

import com.cnpen.smartcampus.data.repository.CampusRepository

interface AppContainer {
    val campusRepository: CampusRepository
}
