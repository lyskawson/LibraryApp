package com.example.libraryapp.uis.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.data.entities.Book
import com.example.libraryapp.navigation.SearchResultsRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle // Inject SavedStateHandle
    // Inject Repository here later
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchResultsUiState())
    val uiState: StateFlow<SearchResultsUiState> = _uiState.asStateFlow()

    // Retrieve searchQuery from navigation arguments
    private val searchQuery: String = checkNotNull(savedStateHandle[SearchResultsRoute::query.name])

    init {
        // Store query in state and load initial results
        _uiState.update { it.copy(query = searchQuery) }
        loadSearchResults()
    }

    fun retrySearch() {
        loadSearchResults()
    }

    private fun loadSearchResults() {
        // Prevent reload if already loading
        if (_uiState.value.isLoading)
            return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                delay(800) // Simulate loading

                // --- Placeholder Search Logic (using Book model) ---
                val fakeResults = performFakeSearch(searchQuery)
                // --- End Placeholder Search Logic ---

                _uiState.update { it.copy(results = fakeResults, isLoading = false) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Search failed: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    // --- Helper for Placeholder Search (updated to return Book) ---
    private fun performFakeSearch(query: String): List<Book> {
        if (query.isBlank()) return emptyList()
        // Simple simulation: return books containing the query (case-insensitive)
        val allFakeBooks = List(25) {
            val type = when(it % 4) { 0 -> "Action"; 1 -> "Mystery"; 2 -> "Kotlin"; else -> "Android" }
            Book(
                id = "s$it",
                title = "Book About $type ${it + 1}",
                authors = listOf("Author ${it % 6}"),
                description = "Search result description $it",
                coverUrl = null,
                publishedDate = "2021",
                categories = listOf(type, "Search"),
                averageRating = 3.0f + (it % 20)/10f,
                pageCount = 100 + it * 8
            )
        }

        return allFakeBooks.filter { book ->
            book.title.contains(query, ignoreCase = true) ||
                    book.authors.any { it.contains(query, ignoreCase = true) } || // Check authors list
                    book.categories?.any { it.contains(query, ignoreCase = true) } == true // Check categories
        }.take(10) // Limit results
    }
    // --- End Helper ---
}