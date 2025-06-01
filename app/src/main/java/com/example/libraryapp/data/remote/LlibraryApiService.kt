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

    @GET("books/") // Path exactly as in FastAPI
    suspend fun getAllBooks(): Response<List<BookDto>>

    // This was the path that caused issues earlier, ensure it's distinct if it exists
    // OR rely on the /books/{book_id} for specific items.
    // If your FastAPI has /books/featured, then:
    @GET("books/featured")
    suspend fun getFeaturedBooks(): Response<List<BookDto>>

    @GET("books/discover")
    suspend fun getDiscoverBooks(): Response<List<BookDto>>

    @GET("books/search/") // Path with trailing slash if FastAPI has it
    suspend fun searchBooks(@Query("query") query: String): Response<List<BookDto>>

    @GET("books/rented/") // Path with trailing slash
    suspend fun getRentedBooks(@Query("user_id") userId: Int): Response<List<BookDto>> // userId is Int

    @GET("books/purchased/") // Path with trailing slash
    suspend fun getPurchasedBooks(@Query("user_id") userId: Int): Response<List<BookDto>> // userId is Int

    @GET("books/{book_id}") // Path parameter book_id
    suspend fun getBookById(@Path("book_id") bookId: Int): Response<BookDto?>

    @POST("books/")
    suspend fun createBook(@Body book: BookCreateDto): Response<BookDto> // Use BookCreateDto, returns BookDto

    // === Author Endpoints ===
    @GET("authors/")
    suspend fun getAllAuthors(): Response<List<AuthorDto>>

    @GET("authors/{author_id}")
    suspend fun getAuthorById(@Path("author_id") authorId: Int): Response<AuthorDto?>

    // Add POST for authors if needed, using an AuthorCreateDto

    // === Genre Endpoints ===
    @GET("genres/")
    suspend fun getAllGenres(): Response<List<GenreDto>>

    // ... other genre methods ...

    // === Rental Endpoints ===
    @GET("rentals/")
    suspend fun getAllRentals(): Response<List<RentalDto>> // For admin or general listing

    @PUT("rentals/{rental_id}/return")
    suspend fun returnBookRental(@Path("rental_id") rentalId: Int): Response<RentalDto> // No request body

    // === Book Item Endpoints ===
    // @GET("book-items/") ...
    @PUT("book-items/{item_id}/status")
    suspend fun updateBookItemStatus(
        @Path("item_id") itemId: Int,
        @Body statusUpdate: BookItemStatusUpdateDto
    ): Response<BookItemDto>

}