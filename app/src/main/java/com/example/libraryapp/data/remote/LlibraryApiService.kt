package com.example.libraryapp.data.remote

import com.example.libraryapp.data.dtos.BookDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface LibraryApiService {

    // Example: Assumes API returns a list directly
    @GET("api/books/featured") // Matches backend endpoint path
    suspend fun getFeaturedBooks(): Response<List<BookDto>> // Returns Response wrapper

    @GET("api/books/rented")
    suspend fun getRentedBooks(@Query("userId") userId: String): Response<List<BookDto>> // Example user ID

    @GET("api/books/purchased")
    suspend fun getPurchasedBooks(@Query("userId") userId: String): Response<List<BookDto>>

    @GET("api/books/discover")
    suspend fun getDiscoverBooks(): Response<List<BookDto>>

    @GET("api/books/{id}") // Use path parameter for bookId
    suspend fun getBookDetails(@Path("id") bookId: String): Response<BookDto?> // DTO can be nullable if API returns 200 OK with empty body for not found

    @GET("api/search/books")
    suspend fun searchBooks(@Query("query") query: String): Response<List<BookDto>>

    // TODO: Add POST/PUT/DELETE methods later for write operations
}