package com.example.libraryapp.data.repositories

import com.example.libraryapp.data.entities.Book
import kotlinx.coroutines.flow.Flow

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable? = null, val message: String? = null) : Result<Nothing>()
}

interface BookRepository {

    // --- Read Operations ---

    /**
     * Gets a Flow of all featured books.
     * Flow allows observing changes if the underlying data source supports it.
     */
    fun getFeaturedBooks(): Flow<Result<List<Book>>> // Using Result wrapper

    /**
     * Gets a Flow of all books marked as rented by the user.
     */
    fun getRentedBooks(): Flow<Result<List<Book>>>

    /**
     * Gets a Flow of all books marked as purchased by the user.
     */
    fun getPurchasedBooks(): Flow<Result<List<Book>>>

    /**
     * Gets a Flow of books recommended or in a "discover" category.
     * (Could be combined with categories later)
     */
    fun getDiscoverBooks(): Flow<Result<List<Book>>> // Added for HomeScreen

    /**
     * Gets details for a specific book by its ID.
     * Returns Flow<Result<Book?>> to handle cases where the book might not be found
     * or details might update.
     */
    fun getBookDetails(bookId: String): Flow<Result<Book?>>

    /**
     * Searches for books matching the query in title, author, description, etc.
     * Returns a simple List as search results aren't typically observed in real-time
     * unless you implement live search suggestions. Using Result for error handling.
     */
    suspend fun searchBooks(query: String): Result<List<Book>> // Suspend for one-off operation

    // --- TODO: Write/Update Operations (Add later) ---

    /**
     * Marks a book as rented (potentially with an expiry date).
     */
    // suspend fun rentBook(bookId: String, expiryDate: Long?): Result<Unit>

    /**
     * Marks a book as purchased.
     */
    // suspend fun purchaseBook(bookId: String): Result<Unit>

    /**
     * Adds or updates a book in the user's library (e.g., adding from search).
     */
    // suspend fun addBookToLibrary(book: Book): Result<Unit>

    /**
     * Removes a book from the rented/purchased list (e.g., return rental).
     */
    // suspend fun removeBookFromLibrary(bookId: String): Result<Unit>

    /**
     * Updates user-specific data for a book (e.g., rating, reading progress).
     */
    // suspend fun updateUserBookData(bookId: String, /* ... updated fields ... */): Result<Unit>

}