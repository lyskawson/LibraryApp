package com.example.libraryapp.data.entities

data class Book(
    val id: String,
    val title: String,
    val authors: List<String>,
    val description: String?,
    val coverUrl: String?,
    val publishedDate: String?,
    val categories: List<String>?,
    val averageRating: Float?,
    val pageCount: Int?,

    // val isRented: Boolean = false,
    // val rentalExpiry: Date? = null,
    // val isPurchased: Boolean = false,
)