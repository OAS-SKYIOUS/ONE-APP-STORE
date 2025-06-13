package io.github.skyious.oas.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.skyious.oas.R
import androidx.compose.ui.Alignment


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import io.github.skyious.oas.data.model.AppInfo
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import coil.compose.rememberAsyncImagePainter
import androidx.lifecycle.viewmodel.compose.viewModel


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import androidx.navigation.compose.*
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.coroutineScope


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Discoverscreen(
    viewModel: DiscoverViewModel = viewModel(),
    onAppClick: (io.github.skyious.oas.data.model.AppInfo) -> Unit

) {
    val apps by viewModel.apps.collectAsState()
    var query by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        isLoading = true
        try {
            viewModel.loadApps(forceRefresh = false)
        } finally {
            isLoading = false
        }
    }

    val filtered = apps.filter {
        it.name.contains(query, ignoreCase = true) ||
                it.author.contains(query, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discover") },
                actions = {
                    IconButton(onClick = {
                        isLoading = true
                        coroutineScope.launch { // Use the coroutineScope to launch a new coroutine
                            isLoading = true
                            try {
                                viewModel.loadApps(forceRefresh = true) // Changed to true for refresh button
                            } finally {
                                isLoading = false
                            }
                        }

                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search apps…") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when {
                    isLoading -> CircularProgressIndicator()
                    filtered.isEmpty() -> Text("No apps found.", style = MaterialTheme.typography.bodyMedium)
                    else -> LazyColumn {
                        items(filtered) { app ->
                            AppRow(app = app, onClick = { onAppClick(app) })
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppRow(
    app: io.github.skyious.oas.data.model.AppInfo,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(app.logoUrl),
            contentDescription = "${app.name} logo",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
        Column {
            Text(app.name, style = MaterialTheme.typography.titleMedium)
            Text("by ${app.author}", style = MaterialTheme.typography.bodySmall)
        }
    }
}





@Composable
fun DebugDiscoverscreen(
    viewModel: DiscoverViewModel = viewModel()
) {
    val apps by viewModel.apps.collectAsState()
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Raw apps count: ${apps.size}")
        Button(onClick = { viewModel.loadApps(forceRefresh = true) }) {
            Text("Force Refresh")
        }
    }
}


