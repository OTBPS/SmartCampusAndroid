package com.cnpen.smartcampus.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cnpen.smartcampus.data.model.PoiCategory
import com.cnpen.smartcampus.data.model.SearchSortOption
import com.cnpen.smartcampus.ui.components.PoiListItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onPoiClick: (String) -> Unit,
    onOpenMapWithPoi: (String) -> Unit,
    contentPadding: PaddingValues,
    viewModel: SearchViewModel
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    var sortExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Search") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text(text = "Search places") },
                placeholder = { Text(text = "Find library, cafeteria, dormitory, service hall...") },
                trailingIcon = {
                    if (uiState.query.isNotBlank()) {
                        IconButton(onClick = viewModel::clearQuery) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear search text"
                            )
                        }
                    }
                },
                singleLine = true
            )

            Text(
                text = "Search by keyword, then refine with category and sorting.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedCategory == null,
                        onClick = { viewModel.onSelectCategory(null) },
                        label = { Text(text = "All") }
                    )
                }
                items(PoiCategory.entries) { category ->
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = { viewModel.onSelectCategory(category) },
                        label = { Text(text = category.label) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box {
                    OutlinedButton(
                        onClick = { sortExpanded = true }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = "Sort options"
                        )
                        Text(
                            text = "Sort: ${uiState.selectedSort.label}",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = sortExpanded,
                        onDismissRequest = { sortExpanded = false }
                    ) {
                        SearchSortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(text = option.label) },
                                onClick = {
                                    viewModel.onSelectSort(option)
                                    sortExpanded = false
                                }
                            )
                        }
                    }
                }
                if (uiState.hasActiveFilters) {
                    TextButton(onClick = viewModel::resetFilters) {
                        Text(text = "Reset Filters")
                    }
                }
            }

            Text(
                text = "Results: ${uiState.resultCount}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            when (uiState.displayState) {
                SearchDisplayState.DEFAULT -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Browsing all places. Enter a keyword or select a category to narrow results.",
                            modifier = Modifier.padding(14.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                SearchDisplayState.FILTERED -> {
                    Text(
                        text = "Filtered results are shown below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                SearchDisplayState.NO_RESULTS -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "No matching places found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Try a different keyword, switch category, or reset filters.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = viewModel::resetFilters) {
                                Text(text = "Reset Filters")
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.results, key = { it.poi.id }) { item ->
                    PoiListItemCard(
                        poi = item.poi,
                        onClick = { onPoiClick(item.poi.id) },
                        showFavoriteAction = true,
                        isFavorite = item.isFavorite,
                        onFavoriteToggle = { viewModel.onToggleFavorite(item.poi.id) },
                        onViewOnMap = {
                            viewModel.onOpenOnMap(item.poi.id)
                            onOpenMapWithPoi(item.poi.id)
                        }
                    )
                }
            }
        }
    }
}
