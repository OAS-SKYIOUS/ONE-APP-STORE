package io.github.skyious.oas.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.skyious.oas.data.IndexRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface CategoriesUiState {
    data class Success(val categories: List<String>) : CategoriesUiState
    object Error : CategoriesUiState
    object Loading : CategoriesUiState
}

class CategoriesViewModel(indexRepository: IndexRepository) : ViewModel() {

    val uiState: StateFlow<CategoriesUiState> = indexRepository.categories
        .map { categories ->
            if (categories.isNotEmpty()) {
                CategoriesUiState.Success(categories)
            } else {
                CategoriesUiState.Loading
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CategoriesUiState.Loading
        )
}
