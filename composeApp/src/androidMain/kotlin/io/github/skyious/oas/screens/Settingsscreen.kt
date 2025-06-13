package io.github.skyious.oas.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.platform.LocalContext
import android.util.Patterns
import androidx.compose.runtime.saveable.rememberSaveable
import io.github.skyious.oas.ui.SettingsViewModel
import io.github.skyious.oas.data.SettingsRepository


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settingsscreen(
    notificationsEnabled: Boolean,
    onNotificationsChanged: (Boolean) -> Unit,
    darkModeEnabled: Boolean,
    onDarkModeChanged: (Boolean) -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {

    val includeFDroid by viewModel.includeFDroidSources.collectAsState()

    val viewModel: SettingsViewModel = viewModel()


    val allowOtherSources by viewModel.allowOtherSources.collectAsState()
    val customSourceUrls by viewModel.customSourceUrls.collectAsState()

    // Local state for showing the warning dialog when enabling
    var showEnableWarning by remember { mutableStateOf(false) }
    // Local state for input text
    var newUrl by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    // Local state for showing URL validation error
    var urlError by remember { mutableStateOf<String?>(null) }



    // If you need the Context (e.g. to read version from PackageInfo)
    val context = LocalContext.current

    // Example toggles backed by remember; you can hook these up to DataStore or prefs later






    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {


            // 1. Notifications toggle
            ListItem(
                headlineContent = { Text("Enable Notifications") },
                trailingContent = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = onNotificationsChanged
                    )
                }
            )
            Divider()

            // 2. Dark mode toggle
            ListItem(
                headlineContent = { Text("Dark Mode") },
                trailingContent = {
                    Switch(
                        checked = darkModeEnabled,
                        onCheckedChange = onDarkModeChanged
                    )
                }
            )

            Divider()

            // 3. App version display
            ListItem(
                headlineContent = { Text("App Version") },
                supportingContent = { Text(getAppVersion(context)) }
            )
            Divider()

            // 4. You can add more items similarly...
            // ListItem( headlineText = { Text("Some Other Setting") }, trailingContent = { ... } )

            Spacer(modifier = Modifier.weight(1f))

            Spacer(Modifier.height(24.dp))
            Text("Advanced", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            // F-Droid toggle
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Include F-Droid sources",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = includeFDroid,
                    onCheckedChange = { viewModel.onIncludeFDroidChanged(it) }
                )
            }

            // 5. A footer or legal text
            Text(
                text = "© 2025 SKYIOUS, Github.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )











            // Row: Allow custom sources switch
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Allow custom sources",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = allowOtherSources,
                    onCheckedChange = { checked ->
                        if (checked) {
                            // User is turning ON: show warning dialog first
                            showEnableWarning = true
                        } else {
                            // Directly turn off
                            viewModel.onAllowOtherSourcesChanged(false)
                        }
                    }
                )
            }

            // If ON, show the list of URLs and input to add new
            if (allowOtherSources) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Custom source repositories:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                if (customSourceUrls.isEmpty()) {
                    Text(
                        text = "No custom sources added yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // List existing URLs
                    LazyColumn {
                        items(customSourceUrls) { url ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = url,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                IconButton(onClick = {
                                    viewModel.removeCustomSource(url)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove"
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input field to add new URL
                OutlinedTextField(
                    value = newUrl,
                    onValueChange = {
                        newUrl = it
                        urlError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Add repository URL") },
                    placeholder = { Text("https://github.com/username/repo") },
                    isError = urlError != null,
                    singleLine = true
                )
                if (urlError != null) {
                    Text(
                        text = urlError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val input = newUrl.text.trim()
                        // Basic validation: non-empty and valid URL pattern
                        if (input.isEmpty()) {
                            urlError = "URL cannot be empty"
                        } else if (!Patterns.WEB_URL.matcher(input).matches()) {
                            urlError = "Invalid URL"
                        } else {
                            // Optionally further validate: ensure it’s a Git repo URL? e.g., endsWith ".git" or github.com pattern
                            viewModel.addCustomSource(input)
                            newUrl = TextFieldValue("") // clear input
                            urlError = null
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Add")
                }
            }
        }

        // Warning dialog when enabling custom sources
        if (showEnableWarning) {
            AlertDialog(
                onDismissRequest = {
                    showEnableWarning = false
                    // revert switch to off
                    viewModel.onAllowOtherSourcesChanged(false)
                },
                title = { Text("Enable custom sources?") },
                text = {
                    Text(
                        "Warning: Custom sources may point to unverified repositories. " +
                                "Ensure you trust the source before enabling. Malicious or broken repositories could lead to issues. " +
                                "Proceed with caution."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        // User accepted warning
                        viewModel.onAllowOtherSourcesChanged(true)
                        showEnableWarning = false
                    }) {
                        Text("Proceed")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        // User declined: revert
                        showEnableWarning = false
                        viewModel.onAllowOtherSourcesChanged(false)
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

























/** Utility to fetch version name from package info */
private fun getAppVersion(context: Context): String =
    try {
        val pi = context.packageManager.getPackageInfo(context.packageName, 0)
        pi.versionName ?: "–"
    } catch (e: Exception) {
        "–"
    }
