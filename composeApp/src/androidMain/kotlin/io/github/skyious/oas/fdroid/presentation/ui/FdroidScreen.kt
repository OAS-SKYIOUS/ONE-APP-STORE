package io.github.skyious.oas.fdroid.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.skyious.oas.fdroid.presentation.state.FdroidScreenState
import io.github.skyious.oas.fdroid.presentation.viewmodel.FdroidViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun FdroidScreen(
    viewModel: FdroidViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val screenState = state) {
            is FdroidScreenState.Loading -> {
                CircularProgressIndicator()
            }
            is FdroidScreenState.Error -> {
                Text(text = "Error: ${screenState.message}")
            }
            is FdroidScreenState.Success -> {
                Column {
                    Text("Repositories:")
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(screenState.repos) { repo ->
                            Text(text = repo.name)
                        }
                    }
                    Text("Apps:")
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(screenState.apps) { app ->
                            Text(text = app.name)
                        }
                    }
                }
            }
        }
    }
}
