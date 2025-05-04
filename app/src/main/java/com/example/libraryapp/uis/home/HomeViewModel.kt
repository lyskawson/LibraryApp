package com.example.libraryapp.uis.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.data.entities.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    // Inject Repository here later
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun refreshHomeData() {
        loadHomeData()
    }

    private fun loadHomeData() {
        if (_uiState.value.isLoading) return // Prevent concurrent loads

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // --- Simulate Network/DB Delay ---
                delay(1200) // Slightly different delay for variety

                // --- Replace with actual Repository calls later ---

                // --- Placeholder Data ---
                val fakeFeatured = List(5) {
                    Book( // Create instances of the new Book model
                        id = "f$it",
                        title = "Featured Book ${it + 1}",
                        authors = listOf("Featured Author ${it % 3}"), // Use List
                        description = "A short description for featured book $it.",
                        coverUrl = null, // Add URLs later if needed for testing
                        publishedDate = "202${it % 4}",
                        categories = listOf("Featured", if(it % 2 == 0) "Fiction" else "Non-Fiction"),
                        averageRating = (3.5f + (it % 15)/10f).coerceAtMost(5.0f),
                        pageCount = 200 + (it*15)
                    )
                }
                val fakeCategories = listOf("Fiction", "Science", "History", "Fantasy", "Biography", "Thriller", "Mystery")
                val fakeDiscover = List(5) {
                    Book( // Create instances of the new Book model
                        id = "f$it",
                        title = "Featured Book ${it + 1}",
                        authors = listOf("Featured Author ${it % 3}"), // Use List
                        description = "A short description for featured book $it.",
                        coverUrl = null, // Add URLs later if needed for testing
                        publishedDate = "202${it % 4}",
                        categories = listOf("Featured", if(it % 2 == 0) "Fiction" else "Non-Fiction"),
                        averageRating = (3.5f + (it % 15)/10f).coerceAtMost(5.0f),
                        pageCount = 200 + (it*15)
                    )
                }
                // --- End Placeholder Data ---

                _uiState.update {
                    it.copy(
                        featuredBooks = fakeFeatured,
                        categories = fakeCategories,
                        discoverBooks = fakeDiscover,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load home screen: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }
}