package com.example.libraryapp.data.dtos

import com.squareup.moshi.Json

data class BookItemStatusUpdateDto(
    @Json(name = "status") val status: String
)