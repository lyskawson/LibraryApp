package com.example.libraryapp.uis.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.data.entities.Book
import com.example.libraryapp.data.repositories.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.libraryapp.data.repositories.Result

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookRepository: BookRepository // Inject repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Trigger for refreshing data
    private val refreshTrigger = MutableStateFlow(System.currentTimeMillis())

    init {
        // Keep static categories for now, or load them if dynamic
        _uiState.update { it.copy(categories = listOf("Fiction", "Science", "History", "Fantasy", "Biography")) }

        observeBooks()
    }

    private fun observeBooks() {
        // Observe Featured Books
        viewModelScope.launch {
            refreshTrigger.collectLatest { // Use collectLatest to restart on trigger
                bookRepository.getFeaturedBooks()
                    .onStart { _uiState.update { it.copy(isLoadingFeatured = true, error = null) } } // Clear general error on start
                    .catch { e -> _uiState.update { it.copy(isLoadingFeatured = false, error = "Error loading featured: ${e.message}") } }
                    .collect { result ->
                        when (result) {
                            is Result.Success -> _uiState.update { it.copy(isLoadingFeatured = false, featuredBooks = result.data) }
                            is Result.Error -> _uiState.update { it.copy(isLoadingFeatured = false, error = result.message ?: "Failed to load featured books") }
                            is Result.Loading -> _uiState.update { it.copy(isLoadingFeatured = true) }
                        }
                    }
            }
        }

        // Observe Discover Books
        viewModelScope.launch {
            refreshTrigger.collectLatest { // Use collectLatest to restart on trigger
                bookRepository.getDiscoverBooks()
                    .onStart { _uiState.update { it.copy(isLoadingDiscover = true/*, error = null*/) } } // Don't clear error from other sections
                    .catch { e -> _uiState.update { it.copy(isLoadingDiscover = false, error = combineErrors(uiState.value.error, "Error loading discover: ${e.message}")) } }
                    .collect { result ->
                        when (result) {
                            is Result.Success -> _uiState.update { it.copy(isLoadingDiscover = false, discoverBooks = result.data) }
                            is Result.Error -> _uiState.update { it.copy(isLoadingDiscover = false, error = combineErrors(uiState.value.error, result.message ?: "Failed to load discover books")) }
                            is Result.Loading -> _uiState.update { it.copy(isLoadingDiscover = true) }
                        }
                    }
            }
        }

        // TODO: Observe dynamic categories if needed later
    }

    fun refreshHomeData() {
        // Emit a new value to trigger collectLatest
        refreshTrigger.value = System.currentTimeMillis()
    }

    // Helper to combine multiple errors without overwriting
    private fun combineErrors(existingError: String?, newError: String?): String? {
        if (newError == null) return existingError
        if (existingError == null) return newError
        // Simple combination, could be more sophisticated
        return "$existingError\n$newError"
    }
}