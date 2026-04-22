package com.cnpen.smartcampus.ui.map

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import kotlinx.coroutines.delay

private const val MAP_ZOOM_SELECTED_POI = 17f
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
    onViewDetailClick: (String) -> Unit,
    viewModel: MapViewModel
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isDebugBuild = remember(context) {
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
    val preflightIssue = remember(context) { checkAmapPreflightIssue(context) }

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
                MapView(context).apply {
                    onCreate(Bundle())
                }
            }.getOrNull()
        }
    }
    var aMap by remember { mutableStateOf<AMap?>(null) }
    var destinationMarker by remember { mutableStateOf<Marker?>(null) }
    var focusedPoiId by remember { mutableStateOf<String?>(null) }
    var centeredFallback by remember { mutableStateOf(false) }

    BindMapLifecycle(
        mapView = mapView,
        lifecycleOwner = lifecycleOwner
    )

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

    LaunchedEffect(
        aMap,
        uiState.selectedPoi,
        uiState.fallbackCenterLatitude,
        uiState.fallbackCenterLongitude,
        uiState.fallbackZoom
    ) {
        val map = aMap ?: return@LaunchedEffect
        val selectedPoi = uiState.selectedPoi
        if (selectedPoi != null) {
            val target = LatLng(selectedPoi.latitude, selectedPoi.longitude)
            val existing = destinationMarker
            if (existing == null) {
                destinationMarker = map.addMarker(
                    MarkerOptions()
                        .position(target)
                        .title(selectedPoi.name)
                        .snippet(selectedPoi.building)
                )
            } else {
                existing.position = target
                existing.title = selectedPoi.name
                existing.snippet = selectedPoi.building
            }
            destinationMarker?.showInfoWindow()
            if (focusedPoiId != selectedPoi.id) {
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(target, MAP_ZOOM_SELECTED_POI)
                )
                focusedPoiId = selectedPoi.id
            } else {
                map.animateCamera(CameraUpdateFactory.newLatLng(target))
            }
            centeredFallback = false
        } else {
            destinationMarker?.remove()
            destinationMarker = null
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = uiState.title) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onLocateClick) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = "Locate me"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
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
                                .padding(12.dp)
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
                                .padding(12.dp)
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
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Chosen Destination",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = uiState.destinationHint,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (uiState.selectedPoi == null) {
                        Text(
                            text = "No place selected yet.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = uiState.selectedPoi.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${uiState.selectedPoi.building} - ${uiState.selectedPoi.category.label}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = { onViewDetailClick(uiState.selectedPoi.id) }
                        ) {
                            Text(text = "View Detail")
                        }
                    }
                }
            }

            Text(
                text = uiState.focusStatus,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BindMapLifecycle(
    mapView: MapView?,
    lifecycleOwner: LifecycleOwner
) {
    androidx.compose.runtime.DisposableEffect(mapView, lifecycleOwner) {
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
    Card(
        modifier = Modifier.fillMaxSize()
    ) {
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
