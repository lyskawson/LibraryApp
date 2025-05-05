package com.example.libraryapp.data.remote

import com.example.libraryapp.data.dtos.BookDto
import com.example.libraryapp.data.entities.Book

fun BookDto.toDomainBook(): Book {
    return Book(
        id = this.bookId, // Map appropriate fields
        title = this.title ?: "No Title", // Provide defaults for non-nullable domain fields
        authors = this.authors ?: emptyList(),
        description = this.description,
        coverUrl = this.cover_url, // Map correct DTO field name
        publishedDate = this.published_date,
        categories = this.categories,
        averageRating = this.average_rating,
        pageCount = this.page_count
    )
}

fun List<BookDto>.toDomainBookList(): List<Book> {
    return this.map { it.toDomainBook() }
}