package com.example.libraryapp.uis.home

import com.example.libraryapp.data.entities.Book

data class HomeUiState(
    val featuredBooks: List<Book> = emptyList(),
    val categories: List<String> = emptyList(),
    val discoverBooks: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

