package com.example.libraryapp.data.remote

import com.example.libraryapp.data.dtos.AuthorDto
import com.example.libraryapp.data.dtos.BookCreateDto
import com.example.libraryapp.data.dtos.BookDto
import com.example.libraryapp.data.dtos.BookItemDto
import com.example.libraryapp.data.dtos.BookItemStatusUpdateDto
import com.example.libraryapp.data.dtos.GenreDto
import com.example.libraryapp.data.dtos.RentalDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface LibraryApiService {

    // ALL book list endpoints now use the same API endpoint
    @GET("books/")
    suspend fun getAllBooks(): Response<List<BookDto>>

    @GET("books/")  // Same endpoint as getAllBooks
    suspend fun getFeaturedBooks(): Response<List<BookDto>>

    @GET("books/")  // Same endpoint as getAllBooks
    suspend fun getDiscoverBooks(): Response<List<BookDto>>

    @GET("books/")  // Same endpoint as getAllBooks - ignore user_id for now
    suspend fun getRentedBooks(@Query("user_id") userId: Int): Response<List<BookDto>>

    @GET("books/")  // Same endpoint as getAllBooks - ignore user_id for now
    suspend fun getPurchasedBooks(@Query("user_id") userId: Int): Response<List<BookDto>>

    // This endpoint exists in your FastAPI
    @GET("books/{book_id}")
    suspend fun getBookById(@Path("book_id") bookId: Int): Response<BookDto?>

    // This endpoint exists in your FastAPI
    @POST("books/")
    suspend fun createBook(@Body book: BookCreateDto): Response<Map<String, String>>

    // Remove or comment out endpoints that don't exist in your FastAPI
    /*
    @GET("books/search/")
    suspend fun searchBooks(@Query("query") query: String): Response<List<BookDto>>

    @GET("authors/")
    suspend fun getAllAuthors(): Response<List<AuthorDto>>

    @GET("authors/{author_id}")
    suspend fun getAuthorById(@Path("author_id") authorId: Int): Response<AuthorDto?>

    @GET("genres/")
    suspend fun getAllGenres(): Response<List<GenreDto>>

    @GET("rentals/")
    suspend fun getAllRentals(): Response<List<RentalDto>>

    @PUT("rentals/{rental_id}/return")
    suspend fun returnBookRental(@Path("rental_id") rentalId: Int): Response<RentalDto>

    @PUT("book-items/{item_id}/status")
    suspend fun updateBookItemStatus(
        @Path("item_id") itemId: Int,
        @Body statusUpdate: BookItemStatusUpdateDto
    ): Response<BookItemDto>
    */
}