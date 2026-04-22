package com.cnpen.smartcampus.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cnpen.smartcampus.ui.assistant.AssistantScreen
import com.cnpen.smartcampus.ui.detail.PoiDetailScreen
import com.cnpen.smartcampus.ui.favorites.FavoritesScreen
import com.cnpen.smartcampus.ui.home.HomeScreen
import com.cnpen.smartcampus.ui.map.MapScreen
import com.cnpen.smartcampus.ui.search.SearchScreen

@Composable
fun SmartCampusNavHost(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val shouldShowBottomBar = currentRoute != AppDestination.PoiDetail.route

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.destination.route,
                            onClick = {
                                navController.navigate(item.destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.destination.label
                                )
                            },
                            label = {
                                Text(text = item.destination.label)
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Home.route,
            modifier = Modifier
        ) {
            composable(AppDestination.Home.route) {
                HomeScreen(
                    onQuickSearchClick = { navController.navigate(AppDestination.Search.route) },
                    onQuickMapClick = { navController.navigate(AppDestination.Map.route) },
                    onQuickFavoritesClick = { navController.navigate(AppDestination.Favorites.route) },
                    onQuickAssistantClick = { navController.navigate(AppDestination.Assistant.route) },
                    onPoiClick = { poiId ->
                        navController.navigate(AppDestination.PoiDetail.routeFor(poiId))
                    },
                    contentPadding = innerPadding
                )
            }
            composable(AppDestination.Search.route) {
                SearchScreen(
                    onPoiClick = { poiId ->
                        navController.navigate(AppDestination.PoiDetail.routeFor(poiId))
                    },
                    contentPadding = innerPadding
                )
            }
            composable(AppDestination.Map.route) {
                MapScreen(contentPadding = innerPadding)
            }
            composable(AppDestination.Favorites.route) {
                FavoritesScreen(
                    onPoiClick = { poiId ->
                        navController.navigate(AppDestination.PoiDetail.routeFor(poiId))
                    },
                    onExploreSearchClick = {
                        navController.navigate(AppDestination.Search.route) {
                            launchSingleTop = true
                        }
                    },
                    contentPadding = innerPadding
                )
            }
            composable(AppDestination.Assistant.route) {
                AssistantScreen(contentPadding = innerPadding)
            }
            composable(
                route = AppDestination.PoiDetail.route,
                arguments = listOf(
                    navArgument(AppDestination.PoiDetail.POI_ID_ARG) {
                        type = NavType.StringType
                    }
                )
            ) {
                PoiDetailScreen(
                    onBackClick = { navController.navigateUp() },
                    onViewOnMap = {
                        navController.navigate(AppDestination.Map.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}
