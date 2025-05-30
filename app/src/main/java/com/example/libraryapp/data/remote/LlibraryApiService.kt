package com.example.libraryapp.data.remote

import com.example.libraryapp.data.dtos.AuthorDto
import com.example.libraryapp.data.dtos.BookDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface LibraryApiService {

    @GET("books/")
    suspend fun getAllBooks(): Response<List<BookDto>>

    @GET("books/{id}")
    suspend fun getBookDetails(@Path("id") bookId: String): Response<BookDto?> // DTO can be nullable if API returns 200 OK with empty body for not found

    @GET("search/books/")
    suspend fun searchBooks(@Query("query") query: String): Response<List<BookDto>>

    @GET("authors/")
    suspend fun getAuthors(): Response<List<AuthorDto>>

    @GET("books/featured")
    suspend fun getFeaturedBooks(): Response<List<BookDto>>

    @GET("books/rented")
    suspend fun getRentedBooks(@Query("userId") userId: String): Response<List<BookDto>>

    @GET("books/purchased")
    suspend fun getPurchasedBooks(@Query("userId") userId: String): Response<List<BookDto>>

    @GET("books/discover")
    suspend fun getDiscoverBooks(): Response<List<BookDto>>

    // TODO: Add POST/PUT/DELETE methods later for write operations
    // @POST("books/")
    // suspend fun createBook(@Body book: BookCreateDto): Response<BookDto>
}