package com.example.libraryapp.uis.myLibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.data.repositories.BookRepository
import com.example.libraryapp.data.repositories.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MyLibraryViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyLibraryUiState())
    val uiState: StateFlow<MyLibraryUiState> = _uiState.asStateFlow()

    private val loadTrigger = MutableStateFlow(System.currentTimeMillis())

    init {
        observeBooks()
    }

    private fun observeBooks() {
        viewModelScope.launch {
            loadTrigger.collect {
                bookRepository.getRentedBooks(1)
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
                            is Result.Loading -> {
                                _uiState.update { it.copy(isLoadingRented = true) }
                            }
                        }
                    }
            }
        }

        viewModelScope.launch {
            loadTrigger.collect {
                bookRepository.getPurchasedBooks(1)
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
        loadTrigger.value = System.currentTimeMillis()
    }
}