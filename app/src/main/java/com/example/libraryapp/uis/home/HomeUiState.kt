package com.example.libraryapp.uis.home

import com.example.libraryapp.data.entities.Book

data class HomeUiState(
    val featuredBooks: List<Book> = emptyList(),
    val categories: List<String> = emptyList(), // Keep static categories for now
    val discoverBooks: List<Book> = emptyList(),
    // Use separate loading flags for potentially independent sections
    val isLoadingFeatured: Boolean = false,
    val isLoadingDiscover: Boolean = false,
    val isLoadingCategories: Boolean = false, // If categories become dynamic
    val error: String? = null // Consolidated error message
)
