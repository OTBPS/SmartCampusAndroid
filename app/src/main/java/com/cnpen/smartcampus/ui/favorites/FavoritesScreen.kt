package com.cnpen.smartcampus.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cnpen.smartcampus.ui.components.PoiListItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onPoiClick: (String) -> Unit,
    onExploreSearchClick: () -> Unit,
    contentPadding: PaddingValues,
    viewModel: FavoritesViewModel = viewModel()
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
                    Text(
                        text = "Saved Places",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(uiState.favorites, key = { it.id }) { poi ->
                    PoiListItemCard(
                        modifier = Modifier.fillMaxWidth(),
                        poi = poi,
                        onClick = { onPoiClick(poi.id) }
                    )
                }
            }
        }
    }
}
