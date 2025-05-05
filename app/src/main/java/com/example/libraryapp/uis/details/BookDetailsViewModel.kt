package com.example.libraryapp.uis.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.data.entities.Book
import com.example.libraryapp.data.repositories.BookRepository
import com.example.libraryapp.navigation.BookDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.libraryapp.data.repositories.Result

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository // Inject repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    private val bookId: String = checkNotNull(savedStateHandle[BookDetailRoute::bookId.name])

    // Trigger for loading/retrying
    private val loadTrigger = MutableStateFlow(Unit) // Simple trigger

    init {
        observeBookDetails()
    }

    private fun observeBookDetails() {
        viewModelScope.launch {
            loadTrigger.flatMapLatest { // Use flatMapLatest to switch to the repo flow
                bookRepository.getBookDetails(bookId)
                    .onStart { _uiState.update { it.copy(isLoading = true, error = null) } }
                    .catch { e -> _uiState.update { it.copy(isLoading = false, error = "Error loading details: ${e.message}") } }
            }.collect { result ->
                // This collect block receives results from getBookDetails
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                book = result.data,
                                // Clear error only if book is found, keep if book is null (not found)
                                error = if (result.data != null) null else it.error
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                // Keep existing book data on error? Optional.
                                // book = null, // Uncomment to clear book on error
                                error = result.message ?: "Failed to load book details"
                            )
                        }
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }


    fun retryLoad() {
        // Emit a new value to trigger flatMapLatest
        loadTrigger.value = Unit
    }
}