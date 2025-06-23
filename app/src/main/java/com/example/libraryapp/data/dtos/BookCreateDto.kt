package com.example.libraryapp.data.dtos

import com.squareup.moshi.Json

data class BookCreateDto(
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String?,
    @Json(name = "published_year") val publishedYear: Int?,
    @Json(name = "pages") val pages: Int?,
    @Json(name = "isbn") val isbn: String?,
    @Json(name = "language_id") val languageId: Int?,
    @Json(name = "cover_url") val coverUrl: String?,
    @Json(name = "categories") val categories: List<String>?,
    @Json(name = "average_rating") val averageRating: Float?,
    @Json(name = "author") val author: AuthorNestedDto?,

)