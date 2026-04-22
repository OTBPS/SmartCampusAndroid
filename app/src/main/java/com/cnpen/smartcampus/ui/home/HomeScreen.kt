package com.cnpen.smartcampus.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cnpen.smartcampus.ui.components.PoiListItemCard
import com.cnpen.smartcampus.ui.components.QuickActionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onQuickSearchClick: () -> Unit,
    onQuickMapClick: () -> Unit,
    onQuickFavoritesClick: () -> Unit,
    onQuickAssistantClick: () -> Unit,
    onPoiClick: (String) -> Unit,
    contentPadding: PaddingValues,
    viewModel: HomeViewModel
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Home")
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = uiState.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onQuickSearchClick
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Quick Search",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = uiState.quickSearchHint,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = onQuickSearchClick,
                            modifier = Modifier.align(androidx.compose.ui.Alignment.End)
                        ) {
                            Text(text = "Open Search")
                        }
                    }
                }
            }
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            item {
                QuickActionCard(
                    title = "Search Places",
                    subtitle = "Find campus locations by keyword",
                    icon = Icons.Filled.Search,
                    onClick = onQuickSearchClick
                )
            }
            item {
                QuickActionCard(
                    title = "Open Map",
                    subtitle = "View selected destination on map",
                    icon = Icons.Filled.Map,
                    onClick = onQuickMapClick
                )
            }
            item {
                QuickActionCard(
                    title = "Favorites",
                    subtitle = "You have ${uiState.favoritesCount} saved places",
                    icon = Icons.Filled.Favorite,
                    onClick = onQuickFavoritesClick
                )
            }
            item {
                QuickActionCard(
                    title = "Assistant",
                    subtitle = "Get lightweight campus suggestions",
                    icon = Icons.Filled.SmartToy,
                    onClick = onQuickAssistantClick
                )
            }
            item {
                Text(
                    text = "Recommended Places",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(uiState.recommendedPois, key = { it.id }) { poi ->
                PoiListItemCard(
                    poi = poi,
                    onClick = { onPoiClick(poi.id) }
                )
            }
        }
    }
}
