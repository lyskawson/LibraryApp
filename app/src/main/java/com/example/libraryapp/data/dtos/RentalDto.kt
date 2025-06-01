package com.example.libraryapp.data.dtos

import com.squareup.moshi.Json

data class RentalDto(
    @Json(name = "id") val id: Int, // Assuming ID non-optional in response
    @Json(name = "user_id") val userId: Int?, // Was optional in FastAPI schema if NULL in DB
    @Json(name = "book_item_id") val bookItemId: Int?, // Was optional
    @Json(name = "rented_at") val rentedAt: String?, // String (ISO format from backend)
    @Json(name = "returned_at") val returnedAt: String? // String (ISO format from backend)
)