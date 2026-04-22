package com.cnpen.smartcampus.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val destination: AppDestination,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(AppDestination.Home, Icons.Filled.Home),
    BottomNavItem(AppDestination.Search, Icons.Filled.Search),
    BottomNavItem(AppDestination.Map, Icons.Filled.Map),
    BottomNavItem(AppDestination.Favorites, Icons.Filled.Favorite),
    BottomNavItem(AppDestination.Assistant, Icons.Filled.SmartToy)
)
