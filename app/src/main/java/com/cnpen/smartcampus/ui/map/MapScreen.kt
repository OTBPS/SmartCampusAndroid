package com.cnpen.smartcampus.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.Polyline
import com.amap.api.maps.model.PolylineOptions
import com.cnpen.smartcampus.data.model.Poi
import com.cnpen.smartcampus.data.model.RouteMode
import com.cnpen.smartcampus.data.model.RoutePlannerPanelMode
import com.cnpen.smartcampus.data.model.RoutePoint
import com.cnpen.smartcampus.data.model.RoutePointType
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private const val MAP_ZOOM_SELECTED_POI = 17f
private const val MAP_ZOOM_CURRENT_LOCATION = 17f
private const val MAP_LOAD_TIMEOUT_MS = 8_000L

private sealed interface MapRuntimeStatus {
    data object Initializing : MapRuntimeStatus
    data object Ready : MapRuntimeStatus
    data class Error(
        val message: String,
        val technicalDetail: String? = null
    ) : MapRuntimeStatus
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    contentPadding: PaddingValues,
    onOpenSearchClick: () -> Unit,
    onViewDetailClick: (String) -> Unit,
    viewModel: MapViewModel
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val routePlan = uiState.routePlan
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isDebugBuild = remember(context) {
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
    val preflightIssue = remember(context) { checkAmapPreflightIssue(context) }

    var hasLocationPermission by remember { mutableStateOf(hasAnyLocationPermission(context)) }
    var pendingUseCurrentLocationAsOrigin by rememberSaveable { mutableStateOf(false) }
    var routeResultExpanded by rememberSaveable { mutableStateOf(false) }

    var mapStatus by remember(preflightIssue) {
        mutableStateOf<MapRuntimeStatus>(
            if (preflightIssue == null) {
                MapRuntimeStatus.Initializing
            } else {
                MapRuntimeStatus.Error(
                    message = "Map failed to initialize.",
                    technicalDetail = preflightIssue
                )
            }
        )
    }

    val mapView = remember(preflightIssue) {
        if (preflightIssue != null) {
            null
        } else {
            runCatching {
                MapView(context).apply { onCreate(Bundle()) }
            }.getOrNull()
        }
    }

    var aMap by remember { mutableStateOf<AMap?>(null) }
    var selectedPoiMarker by remember { mutableStateOf<Marker?>(null) }
    var originMarker by remember { mutableStateOf<Marker?>(null) }
    var destinationMarker by remember { mutableStateOf<Marker?>(null) }
    val waypointMarkers = remember { mutableStateListOf<Marker>() }
    val routePolylines = remember { mutableStateListOf<Polyline>() }
    var focusedPoiId by remember { mutableStateOf<String?>(null) }
    var focusedRouteId by remember { mutableStateOf<String?>(null) }
    var centeredFallback by remember { mutableStateOf(false) }

    fun tryUseCurrentLocationAsOrigin() {
        val map = aMap
        if (map == null) {
            viewModel.updateCurrentLocationStatus("Map is not ready yet.")
            viewModel.finishCurrentLocationRequest()
            return
        }
        val currentLocation = map.myLocation
        if (currentLocation == null) {
            viewModel.updateCurrentLocationStatus("Current location is not available yet.")
            viewModel.finishCurrentLocationRequest()
            return
        }
        viewModel.setCurrentLocationAsOrigin(
            latitude = currentLocation.latitude,
            longitude = currentLocation.longitude
        )
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(currentLocation.latitude, currentLocation.longitude),
                MAP_ZOOM_CURRENT_LOCATION
            )
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted || hasAnyLocationPermission(context)
        if (hasLocationPermission) {
            viewModel.updateCurrentLocationStatus(null)
            if (pendingUseCurrentLocationAsOrigin) {
                pendingUseCurrentLocationAsOrigin = false
                tryUseCurrentLocationAsOrigin()
            }
        } else {
            pendingUseCurrentLocationAsOrigin = false
            viewModel.updateCurrentLocationStatus(
                "Location permission denied. Current location cannot be used as route origin."
            )
            viewModel.finishCurrentLocationRequest()
        }
    }

    BindMapLifecycle(mapView = mapView, lifecycleOwner = lifecycleOwner)

    LaunchedEffect(mapView, preflightIssue) {
        if (preflightIssue == null && mapView == null) {
            mapStatus = MapRuntimeStatus.Error(
                message = "Map failed to initialize.",
                technicalDetail = "MapView creation failed unexpectedly."
            )
        }
    }

    LaunchedEffect(mapView) {
        if (mapView == null) return@LaunchedEffect
        try {
            val map = mapView.map
            aMap = map
            map.uiSettings.isZoomControlsEnabled = false
            map.uiSettings.isScaleControlsEnabled = true
            map.uiSettings.isRotateGesturesEnabled = true
            map.setOnMapLoadedListener {
                mapStatus = MapRuntimeStatus.Ready
            }
        } catch (throwable: Throwable) {
            mapStatus = MapRuntimeStatus.Error(
                message = "Map failed to initialize.",
                technicalDetail = throwable.message ?: "Unexpected map initialization error."
            )
        }
    }

    LaunchedEffect(mapStatus, mapView) {
        if (mapView == null || mapStatus !is MapRuntimeStatus.Initializing) return@LaunchedEffect
        delay(MAP_LOAD_TIMEOUT_MS)
        if (mapStatus is MapRuntimeStatus.Initializing) {
            mapStatus = MapRuntimeStatus.Error(
                message = "Map failed to initialize.",
                technicalDetail = "Map load timed out. Check AMap API key, package name, SHA1, and network."
            )
        }
    }

    LaunchedEffect(aMap, hasLocationPermission) {
        val map = aMap ?: return@LaunchedEffect
        runCatching {
            map.isMyLocationEnabled = hasLocationPermission
        }.onFailure {
            viewModel.updateCurrentLocationStatus("Unable to enable current location.")
        }
    }

    LaunchedEffect(
        aMap,
        uiState.selectedPoi,
        uiState.fallbackCenterLatitude,
        uiState.fallbackCenterLongitude,
        uiState.fallbackZoom,
        routePlan.origin,
        routePlan.destination,
        routePlan.waypoints,
        routePlan.selectedAlternative,
        routePlan.selectedAlternativeIndex,
        routePlan.alternatives
    ) {
        val map = aMap ?: return@LaunchedEffect

        selectedPoiMarker?.remove()
        selectedPoiMarker = null
        originMarker?.remove()
        originMarker = null
        destinationMarker?.remove()
        destinationMarker = null
        waypointMarkers.forEach { it.remove() }
        waypointMarkers.clear()
        routePolylines.forEach { it.remove() }
        routePolylines.clear()

        routePlan.origin?.let { origin ->
            originMarker = map.addMarker(
                MarkerOptions()
                    .position(origin.toLatLng())
                    .title("Origin")
                    .snippet(origin.subtitle ?: origin.label)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
        }
        routePlan.destination?.let { destination ->
            destinationMarker = map.addMarker(
                MarkerOptions()
                    .position(destination.toLatLng())
                    .title("Destination")
                    .snippet(destination.subtitle ?: destination.label)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
        }
        routePlan.waypoints.forEachIndexed { index, waypoint ->
            map.addMarker(
                MarkerOptions()
                    .position(waypoint.toLatLng())
                    .title("Waypoint ${index + 1}")
                    .snippet(waypoint.subtitle ?: waypoint.label)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
            )?.let(waypointMarkers::add)
        }

        val selectedIndex = routePlan.selectedAlternativeIndex
        routePlan.alternatives.forEachIndexed { index, alternative ->
            val points = alternative.polylinePoints.map { LatLng(it.latitude, it.longitude) }
            if (points.size < 2) return@forEachIndexed
            map.addPolyline(
                PolylineOptions()
                    .addAll(points)
                    .width(if (index == selectedIndex) 15f else 10f)
                    .color(
                        if (index == selectedIndex) {
                            0xFF2563EB.toInt()
                        } else {
                            0xFF94A3B8.toInt()
                        }
                    )
                    .zIndex(if (index == selectedIndex) 10f else 5f)
            )?.let(routePolylines::add)
        }

        val selectedRoute = routePlan.selectedAlternative
        if (selectedRoute != null && selectedRoute.polylinePoints.isNotEmpty()) {
            val routeKey = "${selectedRoute.id}_${routePlan.selectedAlternativeIndex}"
            if (focusedRouteId != routeKey) {
                val boundsBuilder = LatLngBounds.builder()
                selectedRoute.polylinePoints.forEach { point ->
                    boundsBuilder.include(LatLng(point.latitude, point.longitude))
                }
                routePlan.origin?.let { boundsBuilder.include(it.toLatLng()) }
                routePlan.destination?.let { boundsBuilder.include(it.toLatLng()) }
                routePlan.waypoints.forEach { boundsBuilder.include(it.toLatLng()) }
                runCatching {
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120))
                }
                focusedRouteId = routeKey
            }
            focusedPoiId = null
            centeredFallback = false
            return@LaunchedEffect
        }

        focusedRouteId = null
        val selectedPoi = uiState.selectedPoi
        if (selectedPoi != null && routePlan.destination == null) {
            val target = LatLng(selectedPoi.latitude, selectedPoi.longitude)
            selectedPoiMarker = map.addMarker(
                MarkerOptions()
                    .position(target)
                    .title(selectedPoi.name)
                    .snippet(selectedPoi.building)
            )
            selectedPoiMarker?.showInfoWindow()
            if (focusedPoiId != selectedPoi.id) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(target, MAP_ZOOM_SELECTED_POI))
                focusedPoiId = selectedPoi.id
            }
            centeredFallback = false
        } else {
            focusedPoiId = null
            if (!centeredFallback) {
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            uiState.fallbackCenterLatitude,
                            uiState.fallbackCenterLongitude
                        ),
                        uiState.fallbackZoom
                    )
                )
                centeredFallback = true
            }
        }
    }

    val handleBackFromMapFlow: () -> Unit = {
        when {
            uiState.flowState == MapFlowState.PLACE_PICKER -> {
                viewModel.closePicker()
            }

            routeResultExpanded -> {
                routeResultExpanded = false
            }

            routePlan.isRoutePlanningActive -> {
                viewModel.onRouteBack()
            }
        }
    }

    BackHandler(
        enabled = uiState.flowState == MapFlowState.PLACE_PICKER ||
            routeResultExpanded ||
            routePlan.isRoutePlanningActive,
        onBack = handleBackFromMapFlow
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.startCurrentLocationAsOrigin()
                    if (!hasLocationPermission) {
                        pendingUseCurrentLocationAsOrigin = true
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    } else {
                        tryUseCurrentLocationAsOrigin()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = "Use current location as origin"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            if (mapView == null) {
                MapErrorCard(
                    message = "Map failed to initialize.",
                    technicalDetail = preflightIssue,
                    isDebugBuild = isDebugBuild
                )
            } else {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize()
                )
            }

            when (val status = mapStatus) {
                MapRuntimeStatus.Initializing -> {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                    ) {
                        Text(
                            text = "Initializing campus map...",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                MapRuntimeStatus.Ready -> Unit

                is MapRuntimeStatus.Error -> {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = status.message,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Check AMap API key, package name, and SHA1 binding.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isDebugBuild && !status.technicalDetail.isNullOrBlank()) {
                                Text(
                                    text = "Debug: ${status.technicalDetail}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (routePlan.isRoutePlanningActive) {
                RouteEditorTopPanel(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    routePlan = routePlan,
                    onBack = viewModel::onRouteBack,
                    onOriginClick = viewModel::openOriginPicker,
                    onDestinationClick = viewModel::openDestinationPicker,
                    onAddWaypoint = viewModel::openAddWaypointPicker,
                    onEditWaypoint = viewModel::openReplaceWaypointPicker,
                    onRemoveWaypoint = viewModel::removeWaypoint,
                    onSwap = viewModel::swapOriginAndDestination,
                    onModeChange = viewModel::setRouteMode,
                    onCalculateRoute = viewModel::calculateRoute,
                    onExpandPanel = viewModel::expandRoutePlannerPanel,
                    onCollapsePanel = viewModel::collapseRoutePlannerPanel,
                    onUseCurrentLocation = {
                        viewModel.startCurrentLocationAsOrigin()
                        if (!hasLocationPermission) {
                            pendingUseCurrentLocationAsOrigin = true
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else {
                            tryUseCurrentLocationAsOrigin()
                        }
                    },
                    onClearRoute = viewModel::clearRoutePoints,
                    onDismissError = viewModel::clearRouteError
                )
            } else {
                MapSearchTopBar(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    onOpenSearchClick = onOpenSearchClick
                )
            }

            if (uiState.flowState == MapFlowState.POI_DETAIL && uiState.selectedPoi != null) {
                SelectedPoiCard(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 8.dp, vertical = 16.dp),
                    poi = uiState.selectedPoi,
                    onDirections = viewModel::onStartRoutePlanningFromSelectedPoi,
                    onViewDetail = { onViewDetailClick(uiState.selectedPoi.id) },
                    onDismiss = viewModel::onDismissPoiDetail
                )
            }

            if (routePlan.selectedAlternative != null && routePlan.isRoutePlanningActive) {
                RouteResultBottomPanel(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(
                            horizontal = 8.dp,
                            vertical = if (uiState.flowState == MapFlowState.POI_DETAIL) 180.dp else 92.dp
                        ),
                    routePlanUiState = routePlan,
                    expanded = routeResultExpanded,
                    onToggleExpanded = { routeResultExpanded = !routeResultExpanded },
                    onSelectAlternative = viewModel::onSelectAlternative
                )
            }

            if (uiState.flowState == MapFlowState.PLACE_PICKER) {
                PlacePickerOverlay(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    contextLabel = uiState.pickerContextLabel,
                    query = routePlan.pickerQuery,
                    favorites = uiState.pickerFavoritePois,
                    recentPoints = uiState.pickerRecentPoints,
                    searchResults = uiState.pickerSearchResults,
                    allowCurrentLocation = routePlan.pickerContext?.pointType == RoutePointType.ORIGIN,
                    onBack = viewModel::closePicker,
                    onQueryChange = viewModel::onPickerQueryChange,
                    onPickPoi = viewModel::onPickPoiFromPicker,
                    onPickRecent = viewModel::onPickRecentPoint,
                    onUseCurrentLocation = {
                        viewModel.startCurrentLocationAsOrigin()
                        if (!hasLocationPermission) {
                            pendingUseCurrentLocationAsOrigin = true
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else {
                            tryUseCurrentLocationAsOrigin()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MapSearchTopBar(
    modifier: Modifier,
    onOpenSearchClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenSearchClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search campus places"
            )
            Text(
                text = "Search campus places",
                modifier = Modifier.padding(start = 10.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteEditorTopPanel(
    modifier: Modifier,
    routePlan: RoutePlanUiState,
    onBack: () -> Unit,
    onOriginClick: () -> Unit,
    onDestinationClick: () -> Unit,
    onAddWaypoint: () -> Unit,
    onEditWaypoint: (Int) -> Unit,
    onRemoveWaypoint: (Int) -> Unit,
    onSwap: () -> Unit,
    onModeChange: (RouteMode) -> Unit,
    onCalculateRoute: () -> Unit,
    onExpandPanel: () -> Unit,
    onCollapsePanel: () -> Unit,
    onUseCurrentLocation: () -> Unit,
    onClearRoute: () -> Unit,
    onDismissError: () -> Unit
) {
    val isCollapsed = routePlan.plannerPanelMode == RoutePlannerPanelMode.COLLAPSED
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .let {
                if (isCollapsed) {
                    it.clickable(onClick = onExpandPanel)
                } else {
                    it
                }
            }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to map"
                        )
                    }
                    Text(
                        text = "Route Planner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!isCollapsed) {
                        FilledIconButton(onClick = onSwap) {
                            Icon(
                                imageVector = Icons.Filled.SwapHoriz,
                                contentDescription = "Swap origin and destination"
                            )
                        }
                    }
                    if (isCollapsed) {
                        IconButton(onClick = onExpandPanel) {
                            Icon(
                                imageVector = Icons.Filled.ExpandMore,
                                contentDescription = "Expand route planner"
                            )
                        }
                    } else if (routePlan.selectedAlternative != null) {
                        IconButton(onClick = onCollapsePanel) {
                            Icon(
                                imageVector = Icons.Filled.ExpandLess,
                                contentDescription = "Collapse route planner"
                            )
                        }
                    }
                }
            }

            if (isCollapsed) {
                CompactRouteSummary(
                    originLabel = routePlan.origin?.label ?: "Choose starting point",
                    destinationLabel = routePlan.destination?.label ?: "Choose destination",
                    onExpandPanel = onExpandPanel
                )
                return@Column
            }

            RouteEditorField(
                label = "Origin",
                value = routePlan.origin?.label ?: "Choose starting point",
                subtitle = routePlan.origin?.subtitle,
                onClick = onOriginClick
            )
            RouteEditorField(
                label = "Destination",
                value = routePlan.destination?.label ?: "Choose destination",
                subtitle = routePlan.destination?.subtitle,
                onClick = onDestinationClick
            )

            routePlan.waypoints.forEachIndexed { index, waypoint ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditWaypoint(index) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Stop ${index + 1}",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = waypoint.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            if (!waypoint.subtitle.isNullOrBlank()) {
                                Text(
                                    text = waypoint.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { onRemoveWaypoint(index) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Remove stop",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onAddWaypoint) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add stop",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(text = "Add stop", modifier = Modifier.padding(start = 4.dp))
                }
                TextButton(onClick = onUseCurrentLocation) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = "Use current location",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(text = "Use current location", modifier = Modifier.padding(start = 4.dp))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = routePlan.routeMode == RouteMode.DRIVING,
                    onClick = { onModeChange(RouteMode.DRIVING) },
                    label = { Text(text = "Driving") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.DriveEta,
                            contentDescription = "Driving mode",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                FilterChip(
                    selected = routePlan.routeMode == RouteMode.WALKING,
                    onClick = { onModeChange(RouteMode.WALKING) },
                    label = { Text(text = "Walking") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                            contentDescription = "Walking mode",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    enabled = routePlan.canCalculateRoute && !routePlan.isLoading,
                    onClick = onCalculateRoute
                ) {
                    if (routePlan.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Directions,
                            contentDescription = "Calculate route"
                        )
                        Text(text = "Calculate", modifier = Modifier.padding(start = 6.dp))
                    }
                }
                OutlinedButton(onClick = onClearRoute) {
                    Text(text = "Clear")
                }
            }

            if (routePlan.isCurrentLocationLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Text(
                        text = "Fetching current location...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!routePlan.currentLocationStatus.isNullOrBlank()) {
                Text(
                    text = routePlan.currentLocationStatus,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!routePlan.errorMessage.isNullOrBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = routePlan.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onDismissError) {
                            Text(text = "Dismiss")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactRouteSummary(
    originLabel: String,
    destinationLabel: String,
    onExpandPanel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExpandPanel)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Origin: $originLabel",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Destination: $destinationLabel",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Tap to expand planner",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RouteEditorField(
    label: String,
    value: String,
    subtitle: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (value.startsWith("Choose")) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteResultBottomPanel(
    modifier: Modifier,
    routePlanUiState: RoutePlanUiState,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onSelectAlternative: (Int) -> Unit
) {
    val selected = routePlanUiState.selectedAlternative ?: return
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpanded),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${selected.mode.label} route",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${selected.durationSeconds.toFormattedDuration()} · ${selected.distanceMeters.toFormattedDistance()}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.Close else Icons.Filled.Tune,
                    contentDescription = if (expanded) "Collapse route details" else "Expand route details"
                )
            }

            if (expanded) {
                if (!selected.instructionSummary.isNullOrBlank()) {
                    Text(
                        text = selected.instructionSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(text = "Steps: ${selected.stepCount}") }
                    )
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(text = "Mode: ${selected.mode.label}") }
                    )
                }
            }

            if (routePlanUiState.alternatives.size > 1) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(routePlanUiState.alternatives, key = { _, item -> item.id }) { index, _ ->
                        FilterChip(
                            selected = routePlanUiState.selectedAlternativeIndex == index,
                            onClick = { onSelectAlternative(index) },
                            label = { Text(text = "Route ${index + 1}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedPoiCard(
    modifier: Modifier,
    poi: Poi,
    onDirections: () -> Unit,
    onViewDetail: () -> Unit,
    onDismiss: () -> Unit
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = poi.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = poi.building,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss place"
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = onDirections) {
                    Icon(
                        imageVector = Icons.Filled.Directions,
                        contentDescription = "Directions"
                    )
                    Text(text = "Directions", modifier = Modifier.padding(start = 6.dp))
                }
                OutlinedButton(onClick = onViewDetail) {
                    Text(text = "View Detail")
                }
            }
        }
    }
}

@Composable
private fun PlacePickerOverlay(
    modifier: Modifier,
    contextLabel: String,
    query: String,
    favorites: List<Poi>,
    recentPoints: List<RoutePoint>,
    searchResults: List<Poi>,
    allowCurrentLocation: Boolean,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onPickPoi: (String) -> Unit,
    onPickRecent: (RoutePoint) -> Unit,
    onUseCurrentLocation: () -> Unit
) {
    Card(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to route editor"
                        )
                    }
                    Text(
                        text = contextLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(text = "Search campus places") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search route point"
                    )
                },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear search text"
                            )
                        }
                    }
                }
            )

            if (allowCurrentLocation) {
                TextButton(onClick = onUseCurrentLocation) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = "Use current location",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(text = "Use current location", modifier = Modifier.padding(start = 6.dp))
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (recentPoints.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recent picks",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    items(recentPoints, key = { "recent_${it.id}" }) { point ->
                        PickerRecentItem(point = point, onClick = { onPickRecent(point) })
                    }
                }

                if (favorites.isNotEmpty()) {
                    item {
                        Text(
                            text = "Favorites",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    items(favorites, key = { "fav_${it.id}" }) { poi ->
                        PickerPoiItem(poi = poi, onClick = { onPickPoi(poi.id) })
                    }
                }

                item {
                    Text(
                        text = if (query.isBlank()) "Popular places" else "Search results",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (searchResults.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "No matching places found.",
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(searchResults, key = { "result_${it.id}" }) { poi ->
                        PickerPoiItem(poi = poi, onClick = { onPickPoi(poi.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerPoiItem(
    poi: Poi,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "Pick this place",
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = poi.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${poi.category.label} · ${poi.building}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PickerRecentItem(
    point: RoutePoint,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Pick recent place"
            )
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (!point.subtitle.isNullOrBlank()) {
                    Text(
                        text = point.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BindMapLifecycle(
    mapView: MapView?,
    lifecycleOwner: LifecycleOwner
) {
    DisposableEffect(mapView, lifecycleOwner) {
        if (mapView == null) {
            return@DisposableEffect onDispose { }
        }
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                mapView.onResume()
            }

            override fun onPause(owner: LifecycleOwner) {
                mapView.onPause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        mapView.onResume()
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onDestroy()
        }
    }
}

@Composable
private fun MapErrorCard(
    message: String,
    technicalDetail: String?,
    isDebugBuild: Boolean
) {
    Card(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                modifier = Modifier.padding(top = 6.dp),
                text = "Check AMap API key, package name, and SHA1 binding.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isDebugBuild && !technicalDetail.isNullOrBlank()) {
                Text(
                    modifier = Modifier.padding(top = 6.dp),
                    text = "Debug: $technicalDetail",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun checkAmapPreflightIssue(context: Context): String? {
    val key = runCatching {
        val appInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
        appInfo.metaData?.getString("com.amap.api.v2.apikey")
    }.getOrNull()?.trim()

    return when {
        key.isNullOrBlank() -> "AMap key metadata is missing. Set AMAP_API_KEY in local.properties."
        key.contains("\${") -> "AMap key placeholder is unresolved. Check manifestPlaceholders."
        key.equals("YOUR_AMAP_API_KEY", ignoreCase = true) -> "AMap key is still the template value."
        else -> null
    }
}

private fun hasAnyLocationPermission(context: Context): Boolean {
    val fineGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fineGranted || coarseGranted
}

private fun RoutePoint.toLatLng(): LatLng = LatLng(latitude, longitude)

private fun Float.toFormattedDistance(): String {
    val meters = roundToInt()
    return if (meters >= 1000) {
        val km = meters / 1000f
        String.format("%.1f km", km)
    } else {
        "$meters m"
    }
}

private fun Long.toFormattedDuration(): String {
    val totalMinutes = this / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        "${hours}h ${minutes}m"
    } else {
        "${minutes}m"
    }
}
