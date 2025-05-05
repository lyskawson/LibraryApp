package com.example.libraryapp.uis.search

import com.example.libraryapp.data.entities.Book

data class SearchResultsUiState(
    val query: String = "",
    val results: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)