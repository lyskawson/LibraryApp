package com.example.libraryapp.data.remote

import com.example.libraryapp.data.dtos.BookDto
import com.example.libraryapp.data.entities.Book

fun BookDto.toDomainBook(): Book {
    return Book(
        id = this.id.toString(),
        title = this.title,
        authors = this.author?.let {
            listOf("${it.firstName} ${it.lastName}".trim())
        } ?: listOf("Author ID: ${this.authorId}"),
        description = this.description,
        coverUrl = this.coverUrl,
        publishedDate = this.publishedYear?.toString(),
        categories = this.categories ?: emptyList(),
        averageRating = this.averageRating,
        pageCount = this.pages

    )
}

fun List<BookDto>.toDomainBookList(): List<Book> {
    return this.map { it.toDomainBook() }
}