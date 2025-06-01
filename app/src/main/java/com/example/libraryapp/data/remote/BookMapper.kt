package com.example.libraryapp.data.remote

import com.example.libraryapp.data.dtos.BookDto
import com.example.libraryapp.data.entities.Book

fun BookDto.toDomainBook(): Book {
    return Book(
        id = this.id.toString(), // Convert Int ID from DTO to String for domain
        title = this.title,
        authors = this.author?.let { // If nested author DTO exists
            listOf("${it.firstName} ${it.lastName}".trim())
        } ?: listOf("Author ID: ${this.authorId}"), // Fallback using authorId
        description = this.description,
        coverUrl = this.coverUrl,
        publishedDate = this.publishedYear?.toString(), // Or handle as Int in domain model
        categories = this.categories ?: emptyList(),
        averageRating = this.averageRating,
        pageCount = this.pages
        // isbn = this.isbn // Add if your domain.model.Book has isbn
    )
}

fun List<BookDto>.toDomainBookList(): List<Book> {
    return this.map { it.toDomainBook() }
}