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
        // Store query in state immediately
        _uiState.update { it.copy(query = searchQuery) }
        // Perform the initial search
        performSearch()
    }

    fun retrySearch() {
        performSearch() // Call the search logic again
    }

    private fun performSearch() {
        // Don't restart if already loading
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) } // Set loading, clear error

            // Call the suspend function from the repository
            val result = bookRepository.searchBooks(searchQuery)

            // Update state based on the Result from the suspend function
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
                // Loading state is handled manually before/after the call for suspend funs
                is Result.Loading -> {} // Not applicable here
            }
        }
    }
}