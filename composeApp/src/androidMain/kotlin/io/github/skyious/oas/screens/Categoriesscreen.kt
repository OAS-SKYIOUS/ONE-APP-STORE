package io.github.skyious.oas.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.skyious.oas.R
import io.github.skyious.oas.data.IndexRepository

@Composable
fun Categoriesscreen(
    indexRepository: IndexRepository,
    onCategoryClick: (String) -> Unit
) {
    val viewModel: CategoriesViewModel = viewModel { CategoriesViewModel(indexRepository) }
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is CategoriesUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is CategoriesUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error loading categories.")
            }
        }
        is CategoriesUiState.Success -> {
            CategoryGrid(categories = state.categories, onCategoryClick = onCategoryClick)
        }
    }
}

@Composable
fun CategoryGrid(categories: List<String>, onCategoryClick: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            CategoryCard(category = category, onClick = { onCategoryClick(category) })
        }
    }
}

@Composable
fun CategoryCard(category: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = getIconForCategory(category), contentDescription = null)
            Text(text = category, textAlign = TextAlign.Center)
        }
    }
}

fun getIconForCategory(category: String): ImageVector {
    return when (category) {
        "Connectivity" -> Icons.Filled.Wifi
        "Development" -> Icons.Default.Build
        "Games" -> Icons.Filled.SportsEsports
        "Graphics" -> Icons.Filled.Palette
        "Internet" -> Icons.Filled.Language
        "Money" -> Icons.Filled.AttachMoney
        "Multimedia" -> Icons.Filled.VideoLibrary
        "Navigation" -> Icons.Filled.Map
        "Phone & SMS" -> Icons.Default.Phone
        "Reading" -> Icons.Filled.Book
        "Science & Education" -> Icons.Filled.School
        "Security" -> Icons.Filled.Security
        "Sports & Health" -> Icons.Filled.FitnessCenter
        "System" -> Icons.Default.Settings
        "Theming" -> Icons.Filled.Brush
        "Time" -> Icons.Filled.Schedule
        "Writing" -> Icons.Default.Create
        else -> Icons.Filled.Apps
    }
}