package com.cnpen.smartcampus.data.remote.firestore

import com.cnpen.smartcampus.data.model.Poi
import com.cnpen.smartcampus.data.model.PoiCategory

object DebugFirestorePoiSeedData {

    private const val DEFAULT_UPDATED_AT = 1_777_680_000_000L // 2026-04-23T00:00:00Z (epoch ms)

    val poiList: List<Poi> = listOf(
        Poi(
            id = "poi_main_library",
            name = "Main Library",
            category = PoiCategory.ACADEMIC,
            description = "Central university library with study spaces and core academic resources.",
            latitude = 32.2039,
            longitude = 118.7162,
            imageUrl = null,
            building = "Library Building",
            keywords = listOf("library", "main library", "study", "图书馆", "南京信息工程大学图书馆"),
            popularity = 95,
            updatedAt = DEFAULT_UPDATED_AT
        ),
        Poi(
            id = "poi_mingde_building",
            name = "Mingde Building",
            category = PoiCategory.ACADEMIC,
            description = "Academic teaching building used for classes and smart classroom activities.",
            latitude = 32.2067,
            longitude = 118.7199,
            imageUrl = null,
            building = "Mingde Building",
            keywords = listOf("mingde", "teaching building", "classrooms", "明德楼"),
            popularity = 90,
            updatedAt = DEFAULT_UPDATED_AT
        ),
        Poi(
            id = "poi_wende_building",
            name = "Wende Building",
            category = PoiCategory.ACADEMIC,
            description = "Major teaching and exam venue on campus.",
            latitude = 32.2061,
            longitude = 118.7215,
            imageUrl = null,
            building = "Wende Building",
            keywords = listOf("wende", "teaching", "exam venue", "文德楼"),
            popularity = 88,
            updatedAt = DEFAULT_UPDATED_AT
        ),
        Poi(
            id = "poi_qixiang_building",
            name = "Qixiang (Meteorology) Building",
            category = PoiCategory.ACADEMIC,
            description = "Academic and administrative building associated with meteorology-focused units.",
            latitude = 32.2051,
            longitude = 118.7234,
            imageUrl = null,
            building = "Qixiang Building",
            keywords = listOf("qixiang", "meteorology", "academic", "气象楼"),
            popularity = 85,
            updatedAt = DEFAULT_UPDATED_AT
        ),
        Poi(
            id = "poi_yuejiang_building",
            name = "Yuejiang Building",
            category = PoiCategory.ACADEMIC,
            description = "Academic building used by teaching and research units.",
            latitude = 32.2047,
            longitude = 118.7183,
            imageUrl = null,
            building = "Yuejiang Building",
            keywords = listOf("yuejiang", "teaching", "research", "阅江楼"),
            popularity = 82,
            updatedAt = DEFAULT_UPDATED_AT
        ),
        Poi(
            id = "poi_lanjiang_building",
            name = "Lanjiang Building",
            category = PoiCategory.ACADEMIC,
            description = "Academic building in the west-side campus zone.",
            latitude = 32.197967,
            longitude = 118.708502,
            imageUrl = null,
            building = "Lanjiang Building",
            keywords = listOf("lanjiang", "west campus", "academic", "揽江楼"),
            popularity = 76,
            updatedAt = DEFAULT_UPDATED_AT
        ),
        Poi(
            id = "poi_student_activity_center",
            name = "Student Activity Center",
            category = PoiCategory.SERVICE,
            description = "Campus activity and events center with student-facing services.",
            latitude = 32.2033,
            longitude = 118.7217,
            imageUrl = null,
            building = "Student Activity Center",
            keywords = listOf("activity center", "events", "student services", "大学生活动中心"),
            popularity = 86,
            updatedAt = DEFAULT_UPDATED_AT
        ),
        Poi(
            id = "poi_east_garden_gymnasium",
            name = "East Garden Gymnasium",
            category = PoiCategory.SPORTS,
            description = "Indoor sports venue used for physical education and events.",
            latitude = 32.208,
            longitude = 118.7261,
            imageUrl = null,
            building = "East Garden Gymnasium",
            keywords = listOf("gymnasium", "sports hall", "east garden", "体育馆", "东苑"),
            popularity = 83,
            updatedAt = DEFAULT_UPDATED_AT
        ),
        Poi(
            id = "poi_zhongyuan_main_stadium",
            name = "Zhongyuan Main Stadium",
            category = PoiCategory.SPORTS,
            description = "Main track-and-field stadium for large ceremonies and sports events.",
            latitude = 32.1989,
            longitude = 118.719,
            imageUrl = null,
            building = "Zhongyuan Main Stadium",
            keywords = listOf("stadium", "track field", "zhongyuan", "中苑主田径场"),
            popularity = 91,
            updatedAt = DEFAULT_UPDATED_AT
        ),
        Poi(
            id = "poi_great_hall",
            name = "Great Hall",
            category = PoiCategory.SERVICE,
            description = "Large campus hall used for ceremonies, lectures, and cultural events.",
            latitude = 32.2037,
            longitude = 118.7199,
            imageUrl = null,
            building = "Great Hall",
            keywords = listOf("great hall", "ceremony hall", "lecture venue", "大礼堂"),
            popularity = 84,
            updatedAt = DEFAULT_UPDATED_AT
        )
    )
}
