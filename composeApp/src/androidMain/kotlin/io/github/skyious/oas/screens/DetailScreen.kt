package io.github.skyious.oas.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.github.skyious.oas.data.model.AppDetail
import io.github.skyious.oas.data.model.AppInfo
import io.github.skyious.oas.ui.DetailUiState
import io.github.skyious.oas.ui.DetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    appInfo: AppInfo,
    onBackPress: () -> Unit,
    viewModel: DetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(appInfo) {
        viewModel.loadDetail(appInfo)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(appInfo.name) },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is DetailUiState.Loading -> CircularProgressIndicator()
                is DetailUiState.Error -> Text(state.message, style = MaterialTheme.typography.bodyMedium)
                is DetailUiState.Success -> {
                    DetailContent(appInfo = appInfo, detail = state.appDetail)
                }
            }
        }
    }
}

@Composable
private fun DetailContent(appInfo: AppInfo, detail: AppDetail) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { AppHeader(appInfo, detail) }
        if (detail.images.isNotEmpty()) {
            item { ScreenshotGallery(detail.images) }
        }
        item { DescriptionCard(detail.description) }
        if (!detail.changelog.isNullOrBlank()) {
            item { WhatsNewCard(detail.changelog) }
        }
    }
}

@Composable
private fun AppHeader(appInfo: AppInfo, detail: AppDetail) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = appInfo.logoUrl,
            contentDescription = "${appInfo.name} logo",
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = detail.name ?: appInfo.name, style = MaterialTheme.typography.headlineSmall)
            detail.author?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Button(onClick = { /* TODO: Handle Install */ }) {
            Text("Get")
        }
    }
}

@Composable
private fun ScreenshotGallery(images: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Screenshots", style = MaterialTheme.typography.titleLarge)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(images) { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Screenshot",
                    modifier = Modifier
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun DescriptionCard(description: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("About this app", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                text = description ?: "No description available.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun WhatsNewCard(changelog: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("What's New", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(text = changelog, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
