package com.example.libraryapp.data.remote

import com.example.libraryapp.data.dtos.BookCreateDto
import com.example.libraryapp.data.dtos.BookDto

import com.example.libraryapp.data.dtos.RentalDto
import com.example.libraryapp.data.dtos.RentalCreateDto
import com.example.libraryapp.data.dtos.PurchaseDto
import com.example.libraryapp.data.dtos.PurchaseCreateDto
import com.example.libraryapp.data.dtos.UserLibraryDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface LibraryApiService {

    @GET("books/")
    suspend fun getAllBooks(): Response<List<BookDto>>

    @GET("books/")
    suspend fun getFeaturedBooks(): Response<List<BookDto>>

    @GET("books/")
    suspend fun getDiscoverBooks(): Response<List<BookDto>>

    @GET("books/{book_id}")
    suspend fun getBookById(@Path("book_id") bookId: Int): Response<BookDto?>

    @POST("books/")
    suspend fun createBook(@Body book: BookCreateDto): Response<Map<String, String>>

    @GET("rentals/")
    suspend fun getAllRentals(): Response<List<RentalDto>>

    @GET("rentals/{rental_id}")
    suspend fun getRentalById(@Path("rental_id") rentalId: Int): Response<RentalDto>

    @GET("rentals/user/{user_id}")
    suspend fun getRentalsByUser(@Path("user_id") userId: Int): Response<List<RentalDto>>

    @GET("rentals/user/{user_id}/active")
    suspend fun getActiveRentalsByUser(@Path("user_id") userId: Int): Response<List<RentalDto>>

    @POST("rentals/")
    suspend fun rentBook(@Body rental: RentalCreateDto): Response<Map<String, Any>>

    @PUT("rentals/{rental_id}/return")
    suspend fun returnRental(@Path("rental_id") rentalId: Int): Response<Map<String, Any>>

    @DELETE("rentals/{rental_id}")
    suspend fun deleteRental(@Path("rental_id") rentalId: Int): Response<Map<String, Any>>

    @GET("purchases/")
    suspend fun getAllPurchases(): Response<List<PurchaseDto>>

    @GET("purchases/{purchase_id}")
    suspend fun getPurchaseById(@Path("purchase_id") purchaseId: Int): Response<PurchaseDto>

    @GET("purchases/user/{user_id}")
    suspend fun getPurchasesByUser(@Path("user_id") userId: Int): Response<List<PurchaseDto>>

    @GET("purchases/user/{user_id}/library")
    suspend fun getUserLibrary(@Path("user_id") userId: Int): Response<List<UserLibraryDto>>

    @POST("purchases/")
    suspend fun purchaseBook(@Body purchase: PurchaseCreateDto): Response<Map<String, Any>>

    @DELETE("purchases/{purchase_id}")
    suspend fun returnPurchase(@Path("purchase_id") purchaseId: Int): Response<Map<String, Any>>

    @GET("rentals/user/{user_id}/active")
    suspend fun getRentedBooks(@Path("user_id") userId: Int): Response<List<RentalDto>>

    @GET("purchases/user/{user_id}/library")
    suspend fun getPurchasedBooks(@Path("user_id") userId: Int): Response<List<UserLibraryDto>>
}