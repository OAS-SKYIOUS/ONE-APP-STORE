package io.github.skyious.oas.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.github.skyious.oas.data.IndexRepository
import io.github.skyious.oas.data.model.AppInfo
import io.github.skyious.oas.ui.components.LoadingProgress
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Discoverscreen(
    indexRepository: IndexRepository,
    onAppClick: (AppInfo) -> Unit,
    category: String? = null
) {
    val viewModel: DiscoverViewModel = viewModel { DiscoverViewModel(indexRepository, category) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (category == null) "Discover" else "Discover in $category") },
                actions = {
                    var showConfirmDialog by remember { mutableStateOf(false) }
                    val isLoading = uiState is DiscoverUiState.Loading

                    if (showConfirmDialog) {
                        AlertDialog(
                            onDismissRequest = { showConfirmDialog = false },
                            title = { Text("Force Refresh") },
                            text = { Text("This will clear the local cache and fetch the latest data from all sources. Continue?") },
                            confirmButton = {
                                Button({
                                    showConfirmDialog = false
                                    viewModel.loadApps(forceRefresh = true)
                                }) { Text("Yes") }
                            },
                            dismissButton = {
                                Button({ showConfirmDialog = false }) { Text("No") }
                            }
                        )
                    }

                    Box(
                        modifier = Modifier.combinedClickable(
                            onClick = {
                                if (!isLoading) {
                                    viewModel.loadApps(forceRefresh = false)
                                }
                            },
                            onLongClick = {
                                showConfirmDialog = true
                            }
                        ).padding(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is DiscoverUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingProgress(progress = -1f, message = "Loading apps...")
                    }
                }
                is DiscoverUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Error loading apps.")
                    }
                }
                is DiscoverUiState.Success -> {
                    val apps = state.apps

                    if (apps.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No apps found.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        AppList(apps = apps, onAppClick = onAppClick)
                    }
                }
            }
        }
    }
}

@Composable
fun AppList(apps: List<AppInfo>, onAppClick: (AppInfo) -> Unit) {
    LazyColumn {
        items(apps, key = { it.id ?: it.hashCode() }) { app ->
            AppRow(
                app = app,
                onClick = { onAppClick(app) }
            )
            Divider()
        }
    }
}

@Composable
fun AppRow(app: AppInfo, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (app.logoUrl.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.name.take(1),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            AsyncImage(
                model = app.logoUrl,
                contentDescription = "${app.name} logo",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(app.name, style = MaterialTheme.typography.titleMedium)
            Text("by ${app.author.orEmpty()}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
