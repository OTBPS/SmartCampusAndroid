package com.cnpen.smartcampus.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoiDetailScreen(
    onBackClick: () -> Unit,
    onViewOnMap: () -> Unit,
    viewModel: PoiDetailViewModel
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Place Detail") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        val poi = uiState.poi
        if (poi == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = uiState.errorMessage ?: "Unable to load place details.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DetailTopImage(
                    imageUrl = poi.imageUrl,
                    placeName = poi.name
                )

                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = poi.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text(text = poi.category.label) }
                        )
                        Text(
                            text = poi.description,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Location Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "Location"
                            )
                            Text(text = "Building: ${poi.building}")
                        }
                        Text(
                            text = "Coordinates: ${poi.latitude}, ${poi.longitude}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        modifier = Modifier.weight(1f),
                        onClick = viewModel::onToggleFavorite
                    ) {
                        Text(
                            text = if (uiState.isFavorite) {
                                "Remove Favorite"
                            } else {
                                "Add to Favorites"
                            }
                        )
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.onPrepareMapFocus()
                            onViewOnMap()
                        }
                    ) {
                        Text(text = "View on Map")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTopImage(
    imageUrl: String?,
    placeName: String
) {
    val shape = RoundedCornerShape(20.dp)
    val context = LocalContext.current

    if (imageUrl.isNullOrBlank()) {
        DetailImageFallback(
            icon = Icons.Filled.Image,
            text = "No image available for this place.",
            shape = shape
        )
        return
    }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build()
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when (val painterState = painter.state) {
            is AsyncImagePainter.State.Loading,
            is AsyncImagePainter.State.Empty -> {
                CircularProgressIndicator()
            }

            is AsyncImagePainter.State.Error -> {
                DetailImageFallback(
                    icon = Icons.Filled.BrokenImage,
                    text = "Unable to load image.",
                    shape = shape
                )
            }

            else -> {
                Image(
                    painter = painter,
                    contentDescription = "$placeName image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun DetailImageFallback(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    shape: RoundedCornerShape
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(shape)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = "Image fallback",
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = text,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
