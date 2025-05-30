package com.example.libraryapp.data.dtos

import com.squareup.moshi.Json

data class BookDto(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String?,
    @Json(name = "authors") val authors: List<AuthorInBookDto>?,
    @Json(name = "description") val description: String?,
    @Json(name = "cover_url") val coverUrl: String?,
    @Json(name = "published_year") val publishedYear: Int?,
    @Json(name = "categories") val categories: List<String>?,
    @Json(name = "average_rating") val averageRating: Float?,
    @Json(name = "pages") val pages: Int?
)

data class AuthorInBookDto(
    @Json(name = "id") val id: Int,
    @Json(name = "first_name") val firstName: String?,
    @Json(name = "last_name") val lastName: String?
)

data class BookListResponse(
    val books: List<BookDto>
    // val totalCount: Int? // Maybe pagination info
)