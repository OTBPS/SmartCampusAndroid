package com.cnpen.smartcampus.data.repository

import com.cnpen.smartcampus.data.model.Poi
import com.cnpen.smartcampus.data.model.PoiCategory

object FakeCampusDataSource {
    val poiList: List<Poi> = listOf(
        Poi(
            id = "poi_library",
            name = "Main Library",
            category = PoiCategory.ACADEMIC,
            description = "Central library with quiet study zones and digital resources.",
            latitude = 22.3020,
            longitude = 114.1775,
            imageUrl = null,
            building = "Library Building",
            keywords = listOf("books", "study", "research", "quiet"),
            popularity = 98,
            updatedAt = 1_713_728_000_000
        ),
        Poi(
            id = "poi_teaching_a",
            name = "Teaching Building A",
            category = PoiCategory.ACADEMIC,
            description = "Main lecture rooms and faculty classrooms for daily teaching.",
            latitude = 22.3014,
            longitude = 114.1780,
            imageUrl = null,
            building = "Academic Zone A",
            keywords = listOf("classroom", "lecture", "faculty", "academic"),
            popularity = 91,
            updatedAt = 1_713_600_000_000
        ),
        Poi(
            id = "poi_cafeteria",
            name = "Cafeteria Center",
            category = PoiCategory.FOOD,
            description = "Student dining center with local and international options.",
            latitude = 22.3010,
            longitude = 114.1769,
            imageUrl = null,
            building = "Student Dining Complex",
            keywords = listOf("food", "lunch", "dinner", "coffee"),
            popularity = 95,
            updatedAt = 1_713_900_000_000
        ),
        Poi(
            id = "poi_dorm3",
            name = "Dormitory 3",
            category = PoiCategory.DORM,
            description = "Residential hall with shared study lounge and laundry area.",
            latitude = 22.3002,
            longitude = 114.1788,
            imageUrl = null,
            building = "Residential Block 3",
            keywords = listOf("dorm", "residence", "living", "hall"),
            popularity = 80,
            updatedAt = 1_713_300_000_000
        ),
        Poi(
            id = "poi_service_hall",
            name = "Student Service Hall",
            category = PoiCategory.SERVICE,
            description = "One-stop center for ID services, enrollment support, and inquiries.",
            latitude = 22.3026,
            longitude = 114.1762,
            imageUrl = null,
            building = "Administration Wing",
            keywords = listOf("service", "id card", "support", "admin"),
            popularity = 89,
            updatedAt = 1_713_850_000_000
        ),
        Poi(
            id = "poi_sports_center",
            name = "Sports Center",
            category = PoiCategory.SPORTS,
            description = "Indoor courts and fitness facilities for sports and wellness.",
            latitude = 22.2998,
            longitude = 114.1772,
            imageUrl = null,
            building = "Athletics Complex",
            keywords = listOf("gym", "fitness", "basketball", "sports"),
            popularity = 84,
            updatedAt = 1_713_500_000_000
        )
    )
}
