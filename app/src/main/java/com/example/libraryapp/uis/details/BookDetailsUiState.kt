package com.example.libraryapp.uis.details

import com.example.libraryapp.data.entities.Book

data class BookDetailUiState(
    val book: Book? = null, // Book details can be null initially or if not found
    val isLoading: Boolean = false,
    val error: String? = null
)
