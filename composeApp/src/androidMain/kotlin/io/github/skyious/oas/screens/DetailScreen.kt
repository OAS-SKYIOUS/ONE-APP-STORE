package io.github.skyious.oas.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import io.github.skyious.oas.data.model.AppDetail
import io.github.skyious.oas.data.model.AppInfo
import io.github.skyious.oas.ui.DetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    appInfo: io.github.skyious.oas.data.model.AppInfo,
    viewModel: DetailViewModel = viewModel()
) {
    val detail by viewModel.detail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(appInfo) {
        viewModel.loadDetail(appInfo)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(appInfo.name) })
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                detail?.let { d ->
                    DetailContent(appInfo, d)
                } ?: Text("Failed to load details.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}


@Composable
private fun DetailContent(appInfo: AppInfo, detail: AppDetail) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Logo
        if (!appInfo.logoUrl.isNullOrBlank()) {
            Image(
                painter = rememberAsyncImagePainter(appInfo.logoUrl),
                contentDescription = "${appInfo.name} logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
        // Name & version
        Text(
            text = detail.name ?: appInfo.name,
            style = MaterialTheme.typography.headlineSmall
        )
        detail.version?.let { v ->
            Text("Version: $v", style = MaterialTheme.typography.bodyMedium)
        }
        // Author
        detail.author?.let { a ->
            Text("By: $a", style = MaterialTheme.typography.bodyMedium)
        }
        // Description
        detail.description?.let { desc ->
            Text("Description:", style = MaterialTheme.typography.titleMedium)
            Text(desc, style = MaterialTheme.typography.bodyMedium)
        }
        // Images / screenshots
        if (detail.images.isNotEmpty()) {
            Text("Screenshots:", style = MaterialTheme.typography.titleMedium)
            // Show images in a row or column; here column
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                detail.images.forEach { imgUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(imgUrl),
                        contentDescription = "Screenshot",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }
        }
        // Changelog
        detail.changelog?.let { cl ->
            Text("Changelog:", style = MaterialTheme.typography.titleMedium)
            Text(cl, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(16.dp))
        // Download / Update button
        val downloadUrl = detail.downloadUrl ?: appInfo.downloadUrl
        if (!downloadUrl.isNullOrBlank()) {
            Button(
                onClick = {
                    // Trigger download via Intent or DownloadManager
                    // Remember to handle permissions if needed
                    // You can pass downloadUrl to a function in Activity/Context
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Download")
            }
        }
        // Other fields: can list them if you want
        if (detail.otherFields.isNotEmpty()) {
            Text("Other Info:", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                detail.otherFields.forEach { (key, value) ->
                    Text("$key: $value", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
