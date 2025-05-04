package com.example.libraryapp.uis.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.data.entities.Book
import com.example.libraryapp.navigation.BookDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle // Inject SavedStateHandle
    // Inject Repository here later
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    // Retrieve bookId from navigation arguments using SavedStateHandle
    // Note: The key matches the argument name in the NavHost composable definition
    //       for the BookDetailRoute, which is typically derived from the data class property name.
    //       The nav-compose library with serialization handles this automatically.
    //       If using manual route strings like "detail/{bookId}", the key would be "bookId".
    private val bookId: String = checkNotNull(savedStateHandle[BookDetailRoute::bookId.name])


    init {
        loadBookDetails() // Load details when ViewModel is created
    }

    fun retryLoad() {
        loadBookDetails()
    }

    private fun loadBookDetails() {
        // Avoid reloading if already loading or already successfully loaded the same book
        if (_uiState.value.isLoading || (_uiState.value.book != null && _uiState.value.book?.id == bookId && _uiState.value.error == null)) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // --- Simulate Network/DB Delay ---
                delay(1000)

                val fakeBook = findFakeBookById(bookId)
                // --- End Placeholder Data ---

                if (fakeBook != null) {
                    _uiState.update { it.copy(book = fakeBook, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Book with ID '$bookId' not found.") }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load book details: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    // --- Helper for Placeholder Data ---
    private fun findFakeBookById(id: String): Book? {
        // Simulate finding a book. In a real app, this logic is in the repository.
        val baseBook = when {
            id.startsWith("f") -> Book(id = id, title = "Featured Book ${id.drop(1).toIntOrNull()?.plus(1)}", authors = listOf("Featured Author ${id.drop(1).toIntOrNull()?.rem(3)}"), description = "Detail description for featured book $id.", coverUrl = null, publishedDate = "2023", categories = listOf("Featured"), averageRating = 4.0f, pageCount = 250)
            id.startsWith("d") -> Book(id = id, title = "Discover Book ${id.drop(1).toIntOrNull()?.plus(1)}", authors = listOf("Discover Author ${id.drop(1).toIntOrNull()?.rem(4)}"), description = "Detail description for discover book $id.", coverUrl = null, publishedDate = "2022", categories = listOf("Discover"), averageRating = 3.8f, pageCount = 310)
            id.startsWith("r") -> Book(id = id, title = "Rented Book Title ${id.drop(1).toIntOrNull()?.plus(1)}", authors = listOf("Author ${id.drop(1).toIntOrNull()?.rem(5)}"), description = "Detail description for rented book $id.", coverUrl = null, publishedDate = "2024", categories = listOf("Rented"), averageRating = 4.2f, pageCount = 180)
            id.startsWith("p") -> Book(id = id, title = "Purchased Book Awesome ${id.drop(1).toIntOrNull()?.plus(1)}", authors = listOf("Author ${id.drop(1).toIntOrNull()?.rem(3)}"), description = "Detail description for purchased book $id.", coverUrl = null, publishedDate = "2021", categories = listOf("Purchased"), averageRating = 4.9f, pageCount = 400)
            id.startsWith("s") -> Book(id = id, title = "Search Result Book ${id.drop(1).toIntOrNull()?.plus(1)}", authors = listOf("Search Author"), description = "Detail description for search result book $id.", coverUrl = null, publishedDate = "2020", categories = listOf("Search"), averageRating = 3.5f, pageCount = 220)
            else -> null
        }
        // Simulate potentially missing details for some books
        return baseBook?.copy(description = baseBook.description?.takeIf { id.hashCode() % 3 != 0 }) // Remove description sometimes
    }
    // --- End Helper ---
}