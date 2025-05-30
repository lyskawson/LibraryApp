package com.example.libraryapp.uis.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.data.repositories.BookRepository
import com.example.libraryapp.data.repositories.Result
import com.example.libraryapp.navigation.BookDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    private val bookId: String = checkNotNull(savedStateHandle[BookDetailRoute::bookId.name])

    private val loadTrigger = MutableStateFlow(Unit)

    init {
        observeBookDetails()
    }

    private fun observeBookDetails() {
        viewModelScope.launch {
            loadTrigger.flatMapLatest {
                bookRepository.getBookDetails(bookId)
                    .onStart { _uiState.update { it.copy(isLoading = true, error = null) } }
                    .catch { e -> _uiState.update { it.copy(isLoading = false, error = "Error loading details: ${e.message}") } }
            }.collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                book = result.data,
                                error = if (result.data != null) null else it.error
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
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
        loadTrigger.value = Unit
    }
}