package com.example.libraryapp.data.dtos

import com.squareup.moshi.Json

data class BookItemDto(
    @Json(name = "id") val id: Int,
    @Json(name = "book_id") val bookId: Int,
    @Json(name = "status") val status: String
)