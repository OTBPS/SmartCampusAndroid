package com.cnpen.smartcampus.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cnpen.smartcampus.ui.components.PoiListItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onPoiClick: (String) -> Unit,
    onExploreSearchClick: () -> Unit,
    onOpenMapForRouting: () -> Unit,
    contentPadding: PaddingValues,
    viewModel: FavoritesViewModel
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Favorites") })
        }
    ) { innerPadding ->
        if (uiState.isEmpty) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Data source: ${uiState.dataSourceLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (uiState.isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator()
                        Text(text = "Loading favorites...")
                    }
                }
                if (uiState.errorMessage != null) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage,
                                color = MaterialTheme.colorScheme.error
                            )
                            TextButton(onClick = viewModel::clearError) {
                                Text(text = "Dismiss")
                            }
                        }
                    }
                }
                Text(
                    text = "No favorite places yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Save places from the detail page to build your quick list.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onExploreSearchClick) {
                    Text(text = "Explore Search")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Saved Places",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Data source: ${uiState.dataSourceLabel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.isLoading) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator()
                                Text(text = "Refreshing favorites...")
                            }
                        }
                        if (uiState.errorMessage != null) {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = uiState.errorMessage,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    TextButton(onClick = viewModel::clearError) {
                                        Text(text = "Dismiss")
                                    }
                                }
                            }
                        }
                    }
                }
                items(uiState.favorites, key = { it.id }) { poi ->
                    var actionsExpanded by remember(poi.id) { mutableStateOf(false) }
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.onRemoveFavorite(poi.id)
                                true
                            } else {
                                false
                            }
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 24.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Remove favorite",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "Remove",
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            PoiListItemCard(
                                modifier = Modifier.fillMaxWidth(),
                                poi = poi,
                                onClick = { onPoiClick(poi.id) },
                                showFavoriteAction = true,
                                isFavorite = true,
                                onFavoriteToggle = { viewModel.onRemoveFavorite(poi.id) }
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 8.dp)
                            ) {
                                IconButton(onClick = { actionsExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Filled.MoreVert,
                                        contentDescription = "Route actions"
                                    )
                                }
                                DropdownMenu(
                                    expanded = actionsExpanded,
                                    onDismissRequest = { actionsExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(text = "Set as Origin") },
                                        onClick = {
                                            actionsExpanded = false
                                            viewModel.setPoiAsOrigin(poi)
                                            onOpenMapForRouting()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(text = "Set as Destination") },
                                        onClick = {
                                            actionsExpanded = false
                                            viewModel.setPoiAsDestination(poi)
                                            onOpenMapForRouting()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(text = "Add as Waypoint") },
                                        onClick = {
                                            actionsExpanded = false
                                            viewModel.addPoiAsWaypoint(poi)
                                            onOpenMapForRouting()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
