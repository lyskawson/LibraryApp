package com.example.libraryapp.data.dtos

import com.example.libraryapp.data.entities.Book
import com.squareup.moshi.Json

data class BookDto(
    @Json(name = "id") val id: Int, // FastAPI BookSchema has non-optional id
    @Json(name = "title") val title: String, // FastAPI BookSchema has non-optional title
    @Json(name = "description") val description: String?,
    @Json(name = "published_year") val publishedYear: Int?,
    @Json(name = "pages") val pages: Int?,
    @Json(name = "isbn") val isbn: String?,
    @Json(name = "language_id") val languageId: Int?,
    @Json(name = "cover_url") val coverUrl: String?,
    @Json(name = "categories") val categories: List<String>?, // List of genre names
    @Json(name = "average_rating") val averageRating: Float?,
    @Json(name = "author") val author: AuthorNestedDto?, // Nested author object
    @Json(name = "author_id") val authorId: Int // The foreign key
)

data class AuthorNestedDto( // Renamed to avoid conflict if you have a top-level AuthorDto
    @Json(name = "id") val id: Int,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String,
    @Json(name = "nationality_id") val nationalityId: Int?
)

data class BookListResponse(
    val books: List<BookDto>
    // val totalCount: Int? // Maybe pagination info
)

