package com.example.libraryapp.uis.myLibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.data.entities.Book
import com.example.libraryapp.uis.home.SimpleBook
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Enum should already be defined in MyLibraryScreen.kt or moved here/to a common place
// enum class LibraryFilterType { RENTED, PURCHASED }

@HiltViewModel
class MyLibraryViewModel @Inject constructor(
    // Inject Repository here later, e.g.:
    // private val bookRepository: BookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyLibraryUiState())
    val uiState: StateFlow<MyLibraryUiState> = _uiState.asStateFlow()

    init {
        // Load initial data when the ViewModel is created
        loadBooks()
    }

    fun selectFilter(filter: LibraryFilterType) {
        // Update the selected filter immediately for responsiveness
        _uiState.update { it.copy(selectedFilter = filter) }
        // Decide if you need to re-fetch data based on filter or if it's already loaded
        // For this example, we assume all data is loaded initially.
        // If fetching per filter: loadBooks(filter)
    }

    fun refreshData() {
        // Allow manual refresh
        loadBooks()
    }

    private fun loadBooks() {
        // Don't reload if already loading
        if (_uiState.value.isLoading)
            return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) } // Start loading, clear old errors
            try {
                // --- Simulate Network/DB Delay ---
                delay(1500) // Simulate 1.5 seconds loading time

                val fakeRented = List(15) {
                    Book(
                        id = "r$it",
                        title = "Rented Book Title ${it + 1}",
                        authors = listOf("Author ${it % 5}"),
                        description = "This is a rented book description.",
                        coverUrl = null,
                        publishedDate = "2023",
                        categories = listOf("Rented", if (it%2==0) "Fiction" else "Non-Fiction"),
                        averageRating = 4.1f,
                        pageCount = 150 + it * 10
                    )
                }
                val fakePurchased = List(10) {
                    Book(
                        id = "p$it",
                        title = "Purchased Book Awesome ${it + 1}",
                        authors = listOf("Author ${it % 3}", "Co-author ${it % 2}"),
                        description = "A permanently owned book.",
                        coverUrl = null,
                        publishedDate = "2022",
                        categories = listOf("Purchased", if (it%3==0) "Sci-Fi" else "History"),
                        averageRating = 4.8f,
                        pageCount = 300 + it * 5
                    )
                }
                // --- End Placeholder Data ---

                _uiState.update {
                    it.copy(
                        rentedBooks = fakeRented,
                        purchasedBooks = fakePurchased,
                        isLoading = false // Finish loading
                    )
                }

            } catch (e: Exception) {
                // Handle exceptions during data fetching
                _uiState.update {
                    it.copy(
                        isLoading = false, // Finish loading (with error)
                        error = "Failed to load library: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }
}