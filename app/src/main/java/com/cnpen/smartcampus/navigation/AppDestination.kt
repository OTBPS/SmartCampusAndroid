package com.cnpen.smartcampus.navigation

sealed class AppDestination(
    val route: String,
    val label: String
) {
    data object Home : AppDestination("home", "Home")
    data object Search : AppDestination("search", "Search")
    data object Map : AppDestination("map", "Map")
    data object Favorites : AppDestination("favorites", "Favorites")
    data object Assistant : AppDestination("assistant", "Assistant")

    data object PoiDetail : AppDestination("detail/{poiId}", "Detail") {
        const val POI_ID_ARG = "poiId"
        fun routeFor(poiId: String): String = "detail/$poiId"
    }
}
