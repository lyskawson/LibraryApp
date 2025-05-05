package com.example.libraryapp.data.dtos

import com.squareup.moshi.Json

data class BookDto(
    @Json(name = "id") val bookId: String, // Use @Json if JSON key differs from property name
    val title: String,
    val authors: List<String>?, // Make fields nullable if they might be missing in JSON
    val description: String?,
    val cover_url: String?, // Example: snake_case in JSON
    val published_date: String?,
    val categories: List<String>?,
    val average_rating: Float?,
    val page_count: Int?
    // Add other fields returned by the API
)

// DTO for a list response if your API wraps lists, e.g., { "books": [...] }
data class BookListResponse(
    val books: List<BookDto>
    // val totalCount: Int? // Maybe pagination info
)