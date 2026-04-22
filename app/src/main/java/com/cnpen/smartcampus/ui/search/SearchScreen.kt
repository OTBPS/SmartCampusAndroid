package com.cnpen.smartcampus.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cnpen.smartcampus.data.model.PoiCategory
import com.cnpen.smartcampus.data.model.SearchSortOption
import com.cnpen.smartcampus.ui.components.PoiListItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onPoiClick: (String) -> Unit,
    contentPadding: PaddingValues,
    viewModel: SearchViewModel = viewModel()
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
                placeholder = { Text(text = "Enter keyword or building") },
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

            Text(
                text = "Results (${uiState.results.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (uiState.results.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "No places found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Try a different keyword or category.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.results, key = { it.id }) { poi ->
                        PoiListItemCard(
                            poi = poi,
                            onClick = { onPoiClick(poi.id) }
                        )
                    }
                }
            }
        }
    }
}
