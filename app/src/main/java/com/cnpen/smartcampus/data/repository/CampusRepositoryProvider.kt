package com.cnpen.smartcampus.data.repository

object CampusRepositoryProvider {
    val repository: CampusRepository by lazy {
        FakeCampusRepository()
    }
}
