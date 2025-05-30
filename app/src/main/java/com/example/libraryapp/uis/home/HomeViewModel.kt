package com.example.libraryapp.uis.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.data.repositories.BookRepository
import com.example.libraryapp.data.repositories.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val refreshTrigger = MutableStateFlow(System.currentTimeMillis())

    init {
        _uiState.update { it.copy(categories = listOf("Fiction", "Science", "History", "Fantasy", "Biography")) }

        observeBooks()
    }

    private fun observeBooks() {
        viewModelScope.launch {
            refreshTrigger.collectLatest {
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

        viewModelScope.launch {
            refreshTrigger.collectLatest {
                bookRepository.getDiscoverBooks()
                    .onStart { _uiState.update { it.copy(isLoadingDiscover = true) } }
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
        refreshTrigger.value = System.currentTimeMillis()
    }

    private fun combineErrors(existingError: String?, newError: String?): String? {
        if (newError == null) return existingError
        if (existingError == null) return newError
        return "$existingError\n$newError"
    }
}