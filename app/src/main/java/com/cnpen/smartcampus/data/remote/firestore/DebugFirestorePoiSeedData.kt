package com.cnpen.smartcampus.data.remote.firestore

import com.cnpen.smartcampus.data.model.Poi
import com.cnpen.smartcampus.data.model.PoiCategory

object DebugFirestorePoiSeedData {

    private const val DEFAULT_UPDATED_AT = 1_777_680_000_000L // 2026-04-23T00:00:00Z (epoch ms)

    val poiList: List<Poi> = listOf(
        poi(
            id = "poi_main_library",
            name = "Main Library",
            category = PoiCategory.ACADEMIC,
            description = "Central university library with major study spaces and academic resources.",
            latitude = 32.203020,
            longitude = 118.713497,
            building = "Library Building",
            keywords = listOf("library", "main library", "study", "reading", "books"),
            popularity = 95
        ),
        poi(
            id = "poi_mingde_building",
            name = "Mingde Building",
            category = PoiCategory.ACADEMIC,
            description = "Core teaching building used for lectures and classroom sessions.",
            latitude = 32.206700,
            longitude = 118.719900,
            building = "Mingde Building",
            keywords = listOf("mingde", "teaching", "classroom", "lecture"),
            popularity = 92
        ),
        poi(
            id = "poi_wende_building",
            name = "Wende Building",
            category = PoiCategory.ACADEMIC,
            description = "Important teaching and exam venue in the central campus zone.",
            latitude = 32.203025,
            longitude = 118.720825,
            building = "Wende Building",
            keywords = listOf("wende", "teaching", "exam", "classroom"),
            popularity = 91
        ),
        poi(
            id = "poi_qixiang_building",
            name = "Qixiang Building",
            category = PoiCategory.ACADEMIC,
            description = "Meteorology-focused teaching and research building.",
            latitude = 32.204614,
            longitude = 118.722521,
            building = "Qixiang Building",
            keywords = listOf("qixiang", "meteorology", "atmospheric science"),
            popularity = 89
        ),
        poi(
            id = "poi_yuejiang_building",
            name = "Yuejiang Building",
            category = PoiCategory.ACADEMIC,
            description = "Academic building for classes, seminars, and faculty offices.",
            latitude = 32.204700,
            longitude = 118.718300,
            building = "Yuejiang Building",
            keywords = listOf("yuejiang", "teaching", "seminar"),
            popularity = 85
        ),
        poi(
            id = "poi_lanjiang_building",
            name = "Lanjiang Building",
            category = PoiCategory.ACADEMIC,
            description = "Academic building in the west-side campus area.",
            latitude = 32.197967,
            longitude = 118.708502,
            building = "Lanjiang Building",
            keywords = listOf("lanjiang", "west campus", "academic"),
            popularity = 81
        ),
        poi(
            id = "poi_student_activity_center",
            name = "Student Activity Center",
            category = PoiCategory.SERVICE,
            description = "Student-facing venue for events, clubs, and activity services.",
            latitude = 32.202669,
            longitude = 118.718253,
            building = "Student Activity Center",
            keywords = listOf("student activity", "events", "clubs", "services"),
            popularity = 88
        ),
        poi(
            id = "poi_east_garden_gymnasium",
            name = "East Garden Gymnasium",
            category = PoiCategory.SPORTS,
            description = "Indoor sports hall used for physical education and campus events.",
            latitude = 32.205998,
            longitude = 118.724767,
            building = "East Garden Gymnasium",
            keywords = listOf("gymnasium", "sports hall", "indoor sports"),
            popularity = 87
        ),
        poi(
            id = "poi_zhongyuan_main_stadium",
            name = "Zhongyuan Main Stadium",
            category = PoiCategory.SPORTS,
            description = "Main track-and-field stadium for sports meets and ceremonies.",
            latitude = 32.200601,
            longitude = 118.712322,
            building = "Zhongyuan Main Stadium",
            keywords = listOf("stadium", "track", "field", "sports"),
            popularity = 90
        ),
        poi(
            id = "poi_great_hall",
            name = "Great Hall",
            category = PoiCategory.SERVICE,
            description = "Large ceremony and conference hall for university events.",
            latitude = 32.202989,
            longitude = 118.717281,
            building = "Great Hall",
            keywords = listOf("great hall", "ceremony", "conference", "events"),
            popularity = 86
        ),
        poi(
            id = "poi_school_of_atmospheric_sciences",
            name = "School of Atmospheric Sciences",
            category = PoiCategory.ACADEMIC,
            description = "Major school focused on atmospheric science education and research.",
            latitude = 32.204614,
            longitude = 118.722521,
            building = "Atmospheric Sciences Complex",
            keywords = listOf("atmospheric sciences", "school", "meteorology"),
            popularity = 84
        ),
        poi(
            id = "poi_school_of_marine_sciences",
            name = "School of Marine Sciences",
            category = PoiCategory.ACADEMIC,
            description = "Academic unit dedicated to marine science teaching and projects.",
            latitude = 32.204580,
            longitude = 118.720730,
            building = "Marine Sciences Building",
            keywords = listOf("marine sciences", "school", "ocean"),
            popularity = 83
        ),
        poi(
            id = "poi_school_of_automation",
            name = "School of Automation",
            category = PoiCategory.ACADEMIC,
            description = "Teaching and research building for automation-related majors.",
            latitude = 32.202772,
            longitude = 118.711254,
            building = "Automation Building",
            keywords = listOf("automation", "engineering", "school"),
            popularity = 83
        ),
        poi(
            id = "poi_school_of_geography_science",
            name = "School of Geography Science",
            category = PoiCategory.ACADEMIC,
            description = "Academic unit for geography science and spatial studies.",
            latitude = 32.205794,
            longitude = 118.716476,
            building = "Geography Science Building",
            keywords = listOf("geography science", "school", "spatial studies"),
            popularity = 82
        ),
        poi(
            id = "poi_school_of_remote_sensing",
            name = "School of Remote Sensing and Surveying",
            category = PoiCategory.ACADEMIC,
            description = "College area for remote sensing and surveying education.",
            latitude = 32.205782,
            longitude = 118.717600,
            building = "Remote Sensing Building",
            keywords = listOf("remote sensing", "surveying", "mapping"),
            popularity = 82
        ),
        poi(
            id = "poi_school_of_physics_optoelectronics",
            name = "School of Physics and Optoelectronics",
            category = PoiCategory.ACADEMIC,
            description = "Academic unit for physics and optoelectronic engineering disciplines.",
            latitude = 32.205319,
            longitude = 118.718601,
            building = "Physics and Optoelectronics Building",
            keywords = listOf("physics", "optoelectronics", "school"),
            popularity = 80
        ),
        poi(
            id = "poi_school_of_chemistry_materials",
            name = "School of Chemistry and Materials",
            category = PoiCategory.ACADEMIC,
            description = "College building for chemistry and materials engineering courses.",
            latitude = 32.201718,
            longitude = 118.711529,
            building = "Chemistry and Materials Building",
            keywords = listOf("chemistry", "materials", "school"),
            popularity = 80
        ),
        poi(
            id = "poi_school_of_environmental_engineering",
            name = "School of Environmental Science and Engineering",
            category = PoiCategory.ACADEMIC,
            description = "Research and teaching unit for environmental science and engineering.",
            latitude = 32.203179,
            longitude = 118.712375,
            building = "Environmental Engineering Building",
            keywords = listOf("environmental science", "engineering", "school"),
            popularity = 79
        ),
        poi(
            id = "poi_school_of_business",
            name = "School of Business",
            category = PoiCategory.ACADEMIC,
            description = "Business education building for management and economics subjects.",
            latitude = 32.201807,
            longitude = 118.711371,
            building = "Business School Building",
            keywords = listOf("business", "management", "economics"),
            popularity = 78
        ),
        poi(
            id = "poi_school_of_emergency_management",
            name = "School of Emergency Management",
            category = PoiCategory.ACADEMIC,
            description = "Academic unit for emergency management and safety studies.",
            latitude = 32.202423,
            longitude = 118.712458,
            building = "Emergency Management Building",
            keywords = listOf("emergency management", "safety", "school"),
            popularity = 76
        ),
        poi(
            id = "poi_school_of_language_culture",
            name = "School of Language and Culture",
            category = PoiCategory.ACADEMIC,
            description = "Teaching unit for language and culture programs.",
            latitude = 32.204583,
            longitude = 118.720725,
            building = "Language and Culture Building",
            keywords = listOf("language", "culture", "school"),
            popularity = 75
        ),
        poi(
            id = "poi_school_of_art",
            name = "School of Art",
            category = PoiCategory.ACADEMIC,
            description = "Campus school area for art education and studios.",
            latitude = 32.201666,
            longitude = 118.711262,
            building = "Art School Building",
            keywords = listOf("art", "design", "school"),
            popularity = 74
        ),
        poi(
            id = "poi_reading_college",
            name = "Reading College",
            category = PoiCategory.ACADEMIC,
            description = "International cooperative college for undergraduate programs.",
            latitude = 32.204688,
            longitude = 118.724799,
            building = "Reading College Building",
            keywords = listOf("reading college", "international", "undergraduate"),
            popularity = 78
        ),
        poi(
            id = "poi_international_education_college",
            name = "International Education College",
            category = PoiCategory.ACADEMIC,
            description = "College supporting international students and exchange programs.",
            latitude = 32.204439,
            longitude = 118.725106,
            building = "International Education Building",
            keywords = listOf("international education", "exchange", "college"),
            popularity = 77
        ),
        poi(
            id = "poi_applied_technology_college",
            name = "Applied Technology College",
            category = PoiCategory.ACADEMIC,
            description = "Applied technology teaching area with practical learning spaces.",
            latitude = 32.200325,
            longitude = 118.709325,
            building = "Applied Technology Building",
            keywords = listOf("applied technology", "practice", "college"),
            popularity = 75
        ),
        poi(
            id = "poi_water_resources_school",
            name = "School of Hydrology and Water Resources",
            category = PoiCategory.ACADEMIC,
            description = "Academic unit focused on hydrology and water resource engineering.",
            latitude = 32.203775,
            longitude = 118.721125,
            building = "Hydrology and Water Resources Building",
            keywords = listOf("hydrology", "water resources", "school"),
            popularity = 77
        ),
        poi(
            id = "poi_integrated_circuit_school",
            name = "Integrated Circuit School",
            category = PoiCategory.ACADEMIC,
            description = "Teaching and research unit for integrated circuit disciplines.",
            latitude = 32.202696,
            longitude = 118.712311,
            building = "Integrated Circuit Building",
            keywords = listOf("integrated circuit", "microelectronics", "school"),
            popularity = 76
        ),
        poi(
            id = "poi_campus_history_museum",
            name = "Campus History Museum",
            category = PoiCategory.SERVICE,
            description = "University history exhibition venue for visitors and students.",
            latitude = 32.202952,
            longitude = 118.713871,
            building = "Campus History Museum",
            keywords = listOf("history museum", "campus history", "exhibition"),
            popularity = 73
        ),
        poi(
            id = "poi_information_science_building",
            name = "Information Science Building",
            category = PoiCategory.ACADEMIC,
            description = "Information science and technology teaching and office building.",
            latitude = 32.199807,
            longitude = 118.709706,
            building = "Information Science Building",
            keywords = listOf("information science", "technology", "building"),
            popularity = 79
        ),
        poi(
            id = "poi_information_center",
            name = "Information Center",
            category = PoiCategory.SERVICE,
            description = "Campus information support center for digital services.",
            latitude = 32.204863,
            longitude = 118.717168,
            building = "Information Center",
            keywords = listOf("information center", "digital services", "support"),
            popularity = 77
        ),
        poi(
            id = "poi_modern_education_tech_center",
            name = "Modern Education Technology Center",
            category = PoiCategory.SERVICE,
            description = "Center for educational technology support and teaching systems.",
            latitude = 32.204898,
            longitude = 118.717209,
            building = "Modern Education Technology Center",
            keywords = listOf("education technology", "teaching support", "center"),
            popularity = 74
        ),
        poi(
            id = "poi_high_performance_computing_platform",
            name = "High Performance Computing Platform",
            category = PoiCategory.ACADEMIC,
            description = "Computing platform supporting scientific research workloads.",
            latitude = 32.204925,
            longitude = 118.717156,
            building = "High Performance Computing Platform",
            keywords = listOf("high performance computing", "research computing", "hpc"),
            popularity = 73
        ),
        poi(
            id = "poi_student_affairs_center",
            name = "Student Affairs and Development Center",
            category = PoiCategory.SERVICE,
            description = "Student support center for academic and development services.",
            latitude = 32.204572,
            longitude = 118.717658,
            building = "Student Affairs and Development Center",
            keywords = listOf("student affairs", "development", "support center"),
            popularity = 80
        ),
        poi(
            id = "poi_student_affairs_office",
            name = "Student Affairs Office",
            category = PoiCategory.SERVICE,
            description = "Administrative office handling student-related operations.",
            latitude = 32.204530,
            longitude = 118.717883,
            building = "Student Affairs Office",
            keywords = listOf("student affairs", "administration", "office"),
            popularity = 72
        ),
        poi(
            id = "poi_general_affairs_office",
            name = "General Affairs Office",
            category = PoiCategory.SERVICE,
            description = "Campus operations office for facilities and service coordination.",
            latitude = 32.203386,
            longitude = 118.717798,
            building = "General Affairs Office",
            keywords = listOf("general affairs", "operations", "office"),
            popularity = 71
        ),
        poi(
            id = "poi_finance_office",
            name = "Finance Office",
            category = PoiCategory.SERVICE,
            description = "Administrative finance office for campus financial services.",
            latitude = 32.207412,
            longitude = 118.724375,
            building = "Finance Office",
            keywords = listOf("finance", "office", "administration"),
            popularity = 70
        ),
        poi(
            id = "poi_procurement_bidding_office",
            name = "Procurement and Bidding Office",
            category = PoiCategory.SERVICE,
            description = "Office managing procurement and bidding procedures.",
            latitude = 32.203375,
            longitude = 118.717821,
            building = "Procurement and Bidding Office",
            keywords = listOf("procurement", "bidding", "office"),
            popularity = 68
        ),
        poi(
            id = "poi_ecological_research_institute",
            name = "Ecological Research Institute",
            category = PoiCategory.ACADEMIC,
            description = "Research institute focused on ecology and environmental studies.",
            latitude = 32.202652,
            longitude = 118.718304,
            building = "Ecological Research Institute",
            keywords = listOf("ecology", "research institute", "environment"),
            popularity = 72
        ),
        poi(
            id = "poi_radar_technology_institute",
            name = "Radar Technology Research Institute",
            category = PoiCategory.ACADEMIC,
            description = "Specialized research institute for radar technology.",
            latitude = 32.199447,
            longitude = 118.711789,
            building = "Radar Technology Research Institute",
            keywords = listOf("radar", "research", "institute"),
            popularity = 71
        ),
        poi(
            id = "poi_faculty_dining_hall",
            name = "Faculty Dining Hall",
            category = PoiCategory.FOOD,
            description = "Dining hall serving faculty and staff meals.",
            latitude = 32.204726,
            longitude = 118.716415,
            building = "Faculty Dining Hall",
            keywords = listOf("faculty dining", "canteen", "food"),
            popularity = 78
        ),
        poi(
            id = "poi_campus_canteen",
            name = "Campus Canteen",
            category = PoiCategory.FOOD,
            description = "General campus canteen with everyday meal options.",
            latitude = 32.206471,
            longitude = 118.719952,
            building = "Campus Canteen",
            keywords = listOf("canteen", "food", "dining"),
            popularity = 79
        ),
        poi(
            id = "poi_east_garden_residential_area",
            name = "East Garden Residential Area",
            category = PoiCategory.DORM,
            description = "Primary student residential area in the east campus zone.",
            latitude = 32.205516,
            longitude = 118.721242,
            building = "East Garden Dormitory Area",
            keywords = listOf("east garden", "dormitory", "residential"),
            popularity = 82
        ),
        poi(
            id = "poi_zhongyuan_residential_area",
            name = "Zhongyuan Residential Area",
            category = PoiCategory.DORM,
            description = "Student dormitory area located in the central campus section.",
            latitude = 32.203710,
            longitude = 118.718674,
            building = "Zhongyuan Dormitory Area",
            keywords = listOf("zhongyuan", "dormitory", "residential"),
            popularity = 82
        ),
        poi(
            id = "poi_qinyuan_residential_area",
            name = "Qinyuan Residential Area",
            category = PoiCategory.DORM,
            description = "Student residence area with multiple dormitory buildings.",
            latitude = 32.201146,
            longitude = 118.713284,
            building = "Qinyuan Dormitory Area",
            keywords = listOf("qinyuan", "dormitory", "residential"),
            popularity = 80
        ),
        poi(
            id = "poi_huiyuan_residential_area",
            name = "Huiyuan Residential Area",
            category = PoiCategory.DORM,
            description = "Residential zone for student accommodation near east campus.",
            latitude = 32.205553,
            longitude = 118.720061,
            building = "Huiyuan Dormitory Area",
            keywords = listOf("huiyuan", "dormitory", "residential"),
            popularity = 79
        ),
        poi(
            id = "poi_shuoyuan_residential_area",
            name = "Shuoyuan Residential Area",
            category = PoiCategory.DORM,
            description = "Graduate-oriented residential area with campus housing.",
            latitude = 32.206097,
            longitude = 118.721929,
            building = "Shuoyuan Dormitory Area",
            keywords = listOf("shuoyuan", "graduate dormitory", "residential"),
            popularity = 77
        ),
        poi(
            id = "poi_talent_apartments",
            name = "Talent Apartments",
            category = PoiCategory.DORM,
            description = "Campus apartment complex for faculty and talent programs.",
            latitude = 32.205758,
            longitude = 118.713552,
            building = "Talent Apartments",
            keywords = listOf("talent apartments", "housing", "residential"),
            popularity = 72
        ),
        poi(
            id = "poi_swan_lake",
            name = "Swan Lake",
            category = PoiCategory.SERVICE,
            description = "Campus lake landmark and common meeting point.",
            latitude = 32.201980,
            longitude = 118.710121,
            building = "Swan Lake",
            keywords = listOf("swan lake", "landmark", "meeting point"),
            popularity = 83
        ),
        poi(
            id = "poi_academician_grove",
            name = "Academician Grove",
            category = PoiCategory.SERVICE,
            description = "Campus landmark grove area and outdoor walking space.",
            latitude = 32.200318,
            longitude = 118.708124,
            building = "Academician Grove",
            keywords = listOf("academician grove", "landmark", "outdoor"),
            popularity = 69
        ),
        poi(
            id = "poi_binhai_office_zone",
            name = "Binjiang Office Zone",
            category = PoiCategory.SERVICE,
            description = "Administrative office zone in the west campus section.",
            latitude = 32.199525,
            longitude = 118.708625,
            building = "Binjiang Office Zone",
            keywords = listOf("binjiang office", "administration", "west campus"),
            popularity = 66
        )
    )

    private fun poi(
        id: String,
        name: String,
        category: PoiCategory,
        description: String,
        latitude: Double,
        longitude: Double,
        building: String,
        keywords: List<String>,
        popularity: Int
    ): Poi =
        Poi(
            id = id,
            name = name,
            category = category,
            description = description,
            latitude = latitude,
            longitude = longitude,
            imageUrl = null,
            building = building,
            keywords = keywords,
            popularity = popularity,
            updatedAt = DEFAULT_UPDATED_AT
        )
}
