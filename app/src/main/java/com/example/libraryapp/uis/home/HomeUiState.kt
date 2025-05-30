package com.example.libraryapp.uis.home

import com.example.libraryapp.data.entities.Book

data class HomeUiState(
    val featuredBooks: List<Book> = emptyList(),
    val categories: List<String> = emptyList(),
    val discoverBooks: List<Book> = emptyList(),
    val isLoadingFeatured: Boolean = false,
    val isLoadingDiscover: Boolean = false,
    val isLoadingCategories: Boolean = false,
    val error: String? = null
)
