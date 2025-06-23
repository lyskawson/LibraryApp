package com.example.libraryapp.data.dtos

import com.squareup.moshi.Json

data class RentalDto(
    @Json(name = "id") val id: Int,
    @Json(name = "user_id") val userId: Int?,
    @Json(name = "book_item_id") val bookItemId: Int?,
    @Json(name = "rented_at") val rentedAt: String?,
    @Json(name = "returned_at") val returnedAt: String?
)