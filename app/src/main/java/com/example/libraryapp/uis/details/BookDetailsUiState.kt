package com.example.libraryapp.uis.details

import com.example.libraryapp.data.entities.Book

data class BookDetailUiState(
    val book: Book? = null,
    val isLoading: Boolean = false,
    val error: String? =null,
    val isPurchasing: Boolean = false,
    val purchaseMessage: String? = null
)