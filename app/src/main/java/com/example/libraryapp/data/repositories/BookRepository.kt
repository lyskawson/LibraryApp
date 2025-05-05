package com.example.libraryapp.data.repositories

import com.example.libraryapp.data.dtos.BookDto
import com.example.libraryapp.data.entities.Book
import com.example.libraryapp.data.remote.LibraryApiService
import com.example.libraryapp.data.remote.toDomainBook
import com.example.libraryapp.data.remote.toDomainBookList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import retrofit2.HttpException
import retrofit2.Response // Import Response
import java.io.IOException

sealed class Result<out T> {
    data object Loading : Result<Nothing>() // Add this state
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable? = null, val message: String? = null) : Result<Nothing>()
}

interface BookRepository {
    fun getFeaturedBooks(): Flow<Result<List<Book>>> // Using Result wrapper
    fun getRentedBooks(): Flow<Result<List<Book>>>
    fun getPurchasedBooks(): Flow<Result<List<Book>>>
    fun getDiscoverBooks(): Flow<Result<List<Book>>> // Added for HomeScreen
    fun getBookDetails(bookId: String): Flow<Result<Book?>>
    suspend fun searchBooks(query: String): Result<List<Book>> // Suspend for one-off operation

    // suspend fun rentBook(bookId: String, expiryDate: Long?): Result<Unit>
    // suspend fun purchaseBook(bookId: String): Result<Unit>
    // suspend fun addBookToLibrary(book: Book): Result<Unit>
    // suspend fun removeBookFromLibrary(bookId: String): Result<Unit>
    // suspend fun updateUserBookData(bookId: String, /* ... updated fields ... */): Result<Unit>

}

class BookRepositoryImpl @Inject constructor(
    private val apiService: LibraryApiService
    // Inject DAOs here later if adding local caching (Room)
) : BookRepository {

    // Helper function to wrap API calls in Flow<Result<T>>
    // Assumes your Result class has Loading, Success, Error states
    private inline fun <reified T, R> apiFlow(
        crossinline apiCall: suspend () -> Response<T>, // Retrofit suspend function
        crossinline mapper: (T?) -> R? // Mapper function (DTO? -> Domain?)
    ): Flow<Result<R?>> = flow {
        emit(Result.Loading) // Emit loading state first
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                // Map even if body is null (important for calls returning nullable DTOs like getBookDetails)
                val mappedData = mapper(body)
                emit(Result.Success(mappedData))
            } else {
                // Handle unsuccessful HTTP responses (like 4xx, 5xx)
                emit(Result.Error(message = "API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: HttpException) {
            // Handle exceptions from the HTTP client (e.g., network errors mapped by Retrofit/OkHttp)
            emit(Result.Error(exception = e, message = "Network error: ${e.message()}"))
        } catch (e: IOException) {
            // Handle general IO exceptions (e.g., no internet connection before request starts)
            emit(Result.Error(exception = e, message = "Network error: Please check connection."))
        } catch (e: Exception) {
            // Catch any other unexpected exceptions (e.g., during mapping, unforeseen issues)
            emit(Result.Error(exception = e, message = "An unexpected error occurred: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO) // Ensure network operations run on the IO dispatcher
        .catch { e -> // Catch exceptions that might occur within the flow's downstream operators (less likely here, but good practice)
            emit(Result.Error(exception = e, message = "Flow error: ${e.message}"))
        }

    // --- Implement Read Operations ---

    override fun getFeaturedBooks(): Flow<Result<List<Book>>> =
        // Explicitly define T=List<BookDto> and R=List<Book> for apiFlow
        apiFlow<List<BookDto>, List<Book>>(
            apiCall = { apiService.getFeaturedBooks() },
            mapper = { dtoList -> dtoList?.toDomainBookList() ?: emptyList() } // Mapper ensures non-null list
        ).map { result -> // Map the Flow<Result<List<Book>?>> to Flow<Result<List<Book>>>
            when (result) {
                is Result.Success -> Result.Success(result.data ?: emptyList()) // Ensure non-null list in success
                is Result.Error -> result // Pass error through
                is Result.Loading -> result // Pass loading through
            }
        }

    override fun getRentedBooks(): Flow<Result<List<Book>>> {
        // TODO: Get actual userId from auth manager/prefs
        val userId = "user123"
        return apiFlow<List<BookDto>, List<Book>>( // Explicit types
            apiCall = { apiService.getRentedBooks(userId) },
            mapper = { dtoList -> dtoList?.toDomainBookList() ?: emptyList() }
        ).map { result -> // Map to ensure non-null list type
            when (result) {
                is Result.Success -> Result.Success(result.data ?: emptyList())
                is Result.Error -> result
                is Result.Loading -> result
            }
        }
    }

    override fun getPurchasedBooks(): Flow<Result<List<Book>>> {
        // TODO: Get actual userId
        val userId = "user123"
        return apiFlow<List<BookDto>, List<Book>>( // Explicit types
            apiCall = { apiService.getPurchasedBooks(userId) },
            mapper = { dtoList -> dtoList?.toDomainBookList() ?: emptyList() }
        ).map { result -> // Map to ensure non-null list type
            when (result) {
                is Result.Success -> Result.Success(result.data ?: emptyList())
                is Result.Error -> result
                is Result.Loading -> result
            }
        }
    }

    override fun getDiscoverBooks(): Flow<Result<List<Book>>> =
        apiFlow<List<BookDto>, List<Book>>( // Explicit types
            apiCall = { apiService.getDiscoverBooks() },
            mapper = { dtoList -> dtoList?.toDomainBookList() ?: emptyList() }
        ).map { result -> // Map to ensure non-null list type
            when (result) {
                is Result.Success -> Result.Success(result.data ?: emptyList())
                is Result.Error -> result
                is Result.Loading -> result
            }
        }

    override fun getBookDetails(bookId: String): Flow<Result<Book?>> =
        // Explicitly define T=BookDto (Retrofit response type) and R=Book? (Domain type)
        apiFlow<BookDto?, Book?>( // Note: T is BookDto? if API might return 200 OK with empty body for not found
            // Or if API returns 404, T can be BookDto, and error handling catches it
            // Let's assume API returns 200 OK with nullable body or 404
            apiCall = { apiService.getBookDetails(bookId) },
            mapper = { dto -> dto?.toDomainBook() } // Mapper correctly handles nullable DTO -> nullable Domain Book?
        )
    // No .map needed here as the interface already expects Flow<Result<Book?>>

    override suspend fun searchBooks(query: String): Result<List<Book>> {
        // Use a direct try-catch for the suspend function as it's not a Flow pipeline
        return try {
            val response = apiService.searchBooks(query)
            if (response.isSuccessful) {
                // Map the response body, provide empty list if body is null
                Result.Success(response.body()?.toDomainBookList() ?: emptyList())
            } else {
                Result.Error(message = "API Error: ${response.code()} ${response.message()}")
            }
        } catch (e: HttpException) {
            Result.Error(exception = e, message = "Network error: ${e.message()}")
        } catch (e: IOException) {
            Result.Error(exception = e, message = "Network error: Please check connection.")
        } catch (e: Exception) {
            Result.Error(exception = e, message = "An unexpected error occurred: ${e.message}")
        }
    }

    // --- Implement Write/Update Operations Later ---
    // override suspend fun rentBook(...) { ... call apiService.rentBook(...) ... }
    // override suspend fun purchaseBook(...) { ... call apiService.purchaseBook(...) ... }
    // ... etc
}