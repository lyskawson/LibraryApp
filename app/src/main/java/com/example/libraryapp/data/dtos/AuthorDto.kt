package com.example.libraryapp.data.dtos

import com.squareup.moshi.Json

data class AuthorDto(
    @Json(name = "id") val id: Int,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String,
    @Json(name = "nationality_id") val nationalityId: Int?
)
