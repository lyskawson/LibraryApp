package com.example.libraryapp.data.dtos

import com.example.libraryapp.data.entities.Book

data class RentalCreateDto(
    val user_id: Int,
    val book_item_id: Int,
    val rented_at: String?,
    val returned_at: String? = null
)

data class PurchaseDto(
    val id: Int,
    val user_id: Int,
    val book_id: Int,
    val price: Double,
    val purchase_date: String,
    val book_title: String,
    val book_author: String,
    val username: String
)

data class PurchaseCreateDto(
    val user_id: Int,
    val book_id: Int,
    val price: Double
)

data class UserLibraryDto(
    val book_id: Int,
    val title: String,
    val author: String,
    val purchase_date: String,
    val price: Double
)


fun UserLibraryDto.toDomainBook(): Book {
    return Book(
        id = this.book_id.toString(),
        title = this.title,
        authors = listOf(this.author),
        description = "Purchased on ${this.purchase_date}",
        coverUrl = null,
        publishedDate = null,
        categories = null,
        averageRating = null,
        pageCount = null
    )
}


fun List<UserLibraryDto>.toDomainBookList(): List<Book> {
    return this.map { it.toDomainBook() }
}
