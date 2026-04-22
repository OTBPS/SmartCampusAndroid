package com.cnpen.smartcampus.di

import com.cnpen.smartcampus.data.repository.CampusRepository
import com.cnpen.smartcampus.data.repository.FakeCampusRepository

class DefaultAppContainer : AppContainer {
    override val campusRepository: CampusRepository by lazy {
        FakeCampusRepository()
    }
}
