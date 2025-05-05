package com.example.libraryapp.uis.myLibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.data.repositories.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.libraryapp.data.repositories.Result

// Enum should already be defined in MyLibraryScreen.kt or moved here/to a common place
// enum class LibraryFilterType { RENTED, PURCHASED }

@HiltViewModel
class MyLibraryViewModel @Inject constructor(
    private val bookRepository: BookRepository // Inject the repository interface
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyLibraryUiState())
    val uiState: StateFlow<MyLibraryUiState> = _uiState.asStateFlow()

    // Trigger to load data
    private val loadTrigger = MutableStateFlow(System.currentTimeMillis()) // Trigger refresh

    init {
        observeBooks()
    }

    private fun observeBooks() {
        viewModelScope.launch {
            loadTrigger.collect { // Re-collect when trigger changes
                // Collect rented books flow
                bookRepository.getRentedBooks()
                    .onStart { _uiState.update { it.copy(isLoadingRented = true, error = null) } }
                    .catch { e -> _uiState.update { it.copy(isLoadingRented = false, error = "Error loading rented: ${e.message}") } }
                    .collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _uiState.update { it.copy(isLoadingRented = false, rentedBooks = result.data) }
                            }
                            is Result.Error -> {
                                _uiState.update { it.copy(isLoadingRented = false, error = result.message ?: "Failed to load rented books") }
                            }
                            is Result.Loading -> { // Handle optional loading state from repo
                                _uiState.update { it.copy(isLoadingRented = true) }
                            }
                        }
                    }
            }
        }

        viewModelScope.launch {
            loadTrigger.collect {
                // Collect purchased books flow
                bookRepository.getPurchasedBooks()
                    .onStart { _uiState.update { it.copy(isLoadingPurchased = true, error = null) } }
                    .catch { e -> _uiState.update { it.copy(isLoadingPurchased = false, error = "Error loading purchased: ${e.message}") } }
                    .collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _uiState.update { it.copy(isLoadingPurchased = false, purchasedBooks = result.data) }
                            }
                            is Result.Error -> {
                                _uiState.update { it.copy(isLoadingPurchased = false, error = result.message ?: "Failed to load purchased books") }
                            }
                            is Result.Loading -> {
                                _uiState.update { it.copy(isLoadingPurchased = true) }
                            }
                        }
                    }
            }
        }
    }

    fun selectFilter(filter: LibraryFilterType) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun refreshData() {
        // Update trigger to cause flows to re-collect
        loadTrigger.value = System.currentTimeMillis()
    }
}