package com.example.libraryapp.uis.myLibrary

import com.example.libraryapp.data.entities.Book

//data class MyLibraryUiState(
//    val selectedFilter: LibraryFilterType = LibraryFilterType.RENTED,
//    val rentedBooks: List<Book> = emptyList(),
//    val purchasedBooks: List<Book> = emptyList(),
//    val isLoading: Boolean = false,
//    val error: String? = null
//)

data class MyLibraryUiState(
    val selectedFilter: LibraryFilterType = LibraryFilterType.RENTED,
    val rentedBooks: List<Book> = emptyList(), // Use domain Book
    val purchasedBooks: List<Book> = emptyList(), // Use domain Book
    val isLoadingRented: Boolean = false,
    val isLoadingPurchased: Boolean = false,
    val error: String? = null // Consolidated error for now
)
