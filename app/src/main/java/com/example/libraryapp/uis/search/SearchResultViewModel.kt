package com.example.libraryapp.uis.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.data.repositories.BookRepository
import com.example.libraryapp.data.repositories.Result
import com.example.libraryapp.navigation.SearchResultsRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository // Inject repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchResultsUiState())
    val uiState: StateFlow<SearchResultsUiState> = _uiState.asStateFlow()

    private val searchQuery: String = checkNotNull(savedStateHandle[SearchResultsRoute::query.name])

    init {
        _uiState.update { it.copy(query = searchQuery) }
        performSearch()
    }

    fun retrySearch() {
        performSearch()
    }

    private fun performSearch() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) } // Set loading, clear error

            val result = bookRepository.searchBooks(searchQuery)

            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            results = result.data
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Search failed"
                        )
                    }
                }
                is Result.Loading -> {}
            }
        }
    }
}