package com.example.libraryapp.data.remote

import com.example.libraryapp.data.dtos.BookDto
import com.example.libraryapp.data.entities.Book

fun BookDto.toDomainBook(): Book {
    return Book(
        id = this.id.toString(),
        title = this.title ?: "No Title",
        authors = this.authors?.map { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim() } ?: emptyList(),
        description = this.description,
        coverUrl = this.coverUrl,
        publishedDate = this.publishedYear?.toString(), // Convert Int to String or handle as Int in domain
        categories = this.categories,
        averageRating = this.averageRating,
        pageCount = this.pages
    )
}

fun List<BookDto>.toDomainBookList(): List<Book> {
    return this.map { it.toDomainBook() }
}