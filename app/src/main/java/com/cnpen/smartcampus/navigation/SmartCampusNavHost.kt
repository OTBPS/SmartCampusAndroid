package com.cnpen.smartcampus.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cnpen.smartcampus.SmartCampusApplication
import com.cnpen.smartcampus.di.SmartCampusViewModelFactory
import com.cnpen.smartcampus.ui.assistant.AssistantScreen
import com.cnpen.smartcampus.ui.assistant.AssistantViewModel
import com.cnpen.smartcampus.ui.detail.PoiDetailScreen
import com.cnpen.smartcampus.ui.detail.PoiDetailViewModel
import com.cnpen.smartcampus.ui.favorites.FavoritesScreen
import com.cnpen.smartcampus.ui.favorites.FavoritesViewModel
import com.cnpen.smartcampus.ui.home.HomeScreen
import com.cnpen.smartcampus.ui.home.HomeViewModel
import com.cnpen.smartcampus.ui.map.MapScreen
import com.cnpen.smartcampus.ui.map.MapViewModel
import com.cnpen.smartcampus.ui.search.SearchScreen
import com.cnpen.smartcampus.ui.search.SearchViewModel

@Composable
fun SmartCampusNavHost(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appContainer = remember(context) {
        (context.applicationContext as SmartCampusApplication).container
    }
    val viewModelFactory = remember(appContainer) {
        SmartCampusViewModelFactory(appContainer).factory
    }
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
                val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
                HomeScreen(
                    onQuickSearchClick = { navController.navigate(AppDestination.Search.route) },
                    onQuickMapClick = { navController.navigate(AppDestination.Map.route) },
                    onQuickFavoritesClick = { navController.navigate(AppDestination.Favorites.route) },
                    onQuickAssistantClick = { navController.navigate(AppDestination.Assistant.route) },
                    onPoiClick = { poiId ->
                        navController.navigate(AppDestination.PoiDetail.routeFor(poiId))
                    },
                    contentPadding = innerPadding,
                    viewModel = homeViewModel
                )
            }
            composable(AppDestination.Search.route) {
                val searchViewModel: SearchViewModel = viewModel(factory = viewModelFactory)
                SearchScreen(
                    onPoiClick = { poiId ->
                        navController.navigate(AppDestination.PoiDetail.routeFor(poiId))
                    },
                    onOpenMapWithPoi = { _ ->
                        navController.navigate(AppDestination.Map.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    contentPadding = innerPadding,
                    viewModel = searchViewModel
                )
            }
            composable(AppDestination.Map.route) {
                val mapViewModel: MapViewModel = viewModel(factory = viewModelFactory)
                MapScreen(
                    contentPadding = innerPadding,
                    onViewDetailClick = { poiId ->
                        navController.navigate(AppDestination.PoiDetail.routeFor(poiId))
                    },
                    viewModel = mapViewModel
                )
            }
            composable(AppDestination.Favorites.route) {
                val favoritesViewModel: FavoritesViewModel = viewModel(factory = viewModelFactory)
                FavoritesScreen(
                    onPoiClick = { poiId ->
                        navController.navigate(AppDestination.PoiDetail.routeFor(poiId))
                    },
                    onExploreSearchClick = {
                        navController.navigate(AppDestination.Search.route) {
                            launchSingleTop = true
                        }
                    },
                    contentPadding = innerPadding,
                    viewModel = favoritesViewModel
                )
            }
            composable(AppDestination.Assistant.route) {
                val assistantViewModel: AssistantViewModel = viewModel(factory = viewModelFactory)
                AssistantScreen(
                    contentPadding = innerPadding,
                    viewModel = assistantViewModel
                )
            }
            composable(
                route = AppDestination.PoiDetail.route,
                arguments = listOf(
                    navArgument(AppDestination.PoiDetail.POI_ID_ARG) {
                        type = NavType.StringType
                    }
                )
            ) {
                val detailViewModel: PoiDetailViewModel = viewModel(factory = viewModelFactory)
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
                    },
                    viewModel = detailViewModel
                )
            }
        }
    }
}
