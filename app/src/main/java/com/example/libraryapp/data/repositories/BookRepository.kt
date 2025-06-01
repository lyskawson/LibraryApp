package com.example.libraryapp.data.repositories

import android.util.Log
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
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

sealed class Result<out T> {
    data object Loading : Result<Nothing>()
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable? = null, val message: String? = null) : Result<Nothing>()
}

interface BookRepository {
    fun getFeaturedBooks(): Flow<Result<List<Book>>>
    fun getRentedBooks(): Flow<Result<List<Book>>>
    fun getPurchasedBooks(): Flow<Result<List<Book>>>
    fun getDiscoverBooks(): Flow<Result<List<Book>>>
    fun getBookDetails(bookId: String): Flow<Result<Book?>>
    suspend fun searchBooks(query: String): Result<List<Book>>

    // suspend fun rentBook(bookId: String, expiryDate: Long?): Result<Unit>
    // suspend fun purchaseBook(bookId: String): Result<Unit>
    // suspend fun addBookToLibrary(book: Book): Result<Unit>
    // suspend fun removeBookFromLibrary(bookId: String): Result<Unit>
    // suspend fun updateUserBookData(bookId: String, /* ... updated fields ... */): Result<Unit>

}

class BookRepositoryImpl @Inject constructor(
    private val apiService: LibraryApiService
) : BookRepository {

    private val TAG = "BookRepositoryImpl"


    private inline fun <reified T, R> apiFlow(
        crossinline apiCall: suspend () -> Response<T>,
        crossinline mapper: (T?) -> R?
    ): Flow<Result<R?>> = flow {
        emit(Result.Loading)
        try {
            Log.d(TAG, "apiFlow: Making API call...")

            val response = apiCall()
            Log.d(TAG, "apiFlow: Response received - Code: ${response.code()}, Successful: ${response.isSuccessful}")
            if (response.isSuccessful) {
                val body = response.body()
                Log.d(TAG, "apiFlow: Response successful. Body present: ${body != null}")
                val mappedData = mapper(body)
                Log.d(TAG, "apiFlow: Mapping successful. Emitting Success.")
                emit(Result.Success(mappedData))
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e(TAG, "apiFlow: API Error - Code: ${response.code()}, Message: ${response.message()}, ErrorBody: $errorBody")
                emit(Result.Error(message = "API Error: ${response.code()} ${response.message()} - $errorBody"))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "apiFlow: HttpException - Code: ${e.code()}, Message: ${e.message()}", e)
            emit(Result.Error(exception = e, message = "Network error (HTTP ${e.code()}): ${e.message()}"))
        } catch (e: IOException) {
            Log.e(TAG, "apiFlow: IOException - Message: ${e.message}", e)
            emit(Result.Error(exception = e, message = "Network error (IO): Please check connection. Details: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "apiFlow: Generic Exception - Message: ${e.message}", e)
            emit(Result.Error(exception = e, message = "An unexpected error occurred: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
        .catch { e ->
            Log.e(TAG, "apiFlow: Exception in flow's catch operator - Message: ${e.message}", e)
            emit(Result.Error(exception = e, message = "Flow error: ${e.message}"))
        }


    override fun getFeaturedBooks(): Flow<Result<List<Book>>> =
        apiFlow<List<BookDto>, List<Book>>(
            apiCall = { apiService.getFeaturedBooks() },
            mapper = { dtoList -> dtoList?.toDomainBookList() ?: emptyList() }
        ).map { result ->
            when (result) {
                is Result.Success -> Result.Success(result.data ?: emptyList())
                is Result.Error -> result
                is Result.Loading -> result
            }
        }

    override fun getRentedBooks(): Flow<Result<List<Book>>> {
        // TODO: Get actual userId (as Int) from auth manager/prefs
        val userId = 1 // Placeholder - MUST be an Int now
        return apiFlow<List<BookDto>, List<Book>>(
            apiCall = { apiService.getRentedBooks(userId) },
            mapper = { dtoList -> dtoList?.toDomainBookList() ?: emptyList() }
        ).map { result ->
            when (result) {
                is Result.Success -> Result.Success(result.data ?: emptyList())
                is Result.Error -> result
                is Result.Loading -> result
            } }
    }

    override fun getPurchasedBooks(): Flow<Result<List<Book>>> {
        // TODO: Get actual userId
        val userId = 1
        return apiFlow<List<BookDto>, List<Book>>(
            apiCall = { apiService.getPurchasedBooks(userId) },
            mapper = { dtoList -> dtoList?.toDomainBookList() ?: emptyList() }
        ).map { result ->
            when (result) {
                is Result.Success -> Result.Success(result.data ?: emptyList())
                is Result.Error -> result
                is Result.Loading -> result
            }
        }
    }

    override fun getDiscoverBooks(): Flow<Result<List<Book>>> =
        apiFlow<List<BookDto>, List<Book>>(
            apiCall = { apiService.getDiscoverBooks() },
            mapper = { dtoList -> dtoList?.toDomainBookList() ?: emptyList() }
        ).map { result -> // Map to ensure non-null list type
            when (result) {
                is Result.Success -> Result.Success(result.data ?: emptyList())
                is Result.Error -> result
                is Result.Loading -> result
            }
        }

    override fun getBookDetails(bookId: String): Flow<Result<Book?>> {
        val idAsInt = bookId.toIntOrNull()
        if (idAsInt == null) {
            return flow { emit(Result.Error(message = "Invalid book ID format for API call: $bookId")) }
        }
        return apiFlow<BookDto?, Book?>( // T is BookDto?, R is Book?
            apiCall = { apiService.getBookById(idAsInt) }, // Use getBookById
            mapper = { dto -> dto?.toDomainBook() }
        )
    }
    override suspend fun searchBooks(query: String): Result<List<Book>> {
        return try {
            val response = apiService.searchBooks(query)
            if (response.isSuccessful) {
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

    // TODO: Implement Write/Update Operations Later
    // override suspend fun rentBook(...) { ... call apiService.rentBook(...) ... }
    // override suspend fun purchaseBook(...) { ... call apiService.purchaseBook(...) ... }
}