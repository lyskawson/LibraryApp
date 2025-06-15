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
            mapper = { dtoList ->
                // Since we're using the same endpoint, we can filter or take first N books
                val featured = dtoList?.take(10) ?: emptyList()
                featured.toDomainBookList()
            }
        ).map { result ->
            when (result) {
                is Result.Success -> Result.Success(result.data ?: emptyList())
                is Result.Error -> result
                is Result.Loading -> result
            }
        }

    override fun getRentedBooks(): Flow<Result<List<Book>>> =
        apiFlow<List<BookDto>, List<Book>>(
            apiCall = { apiService.getRentedBooks(1) }, // userId parameter ignored by API
            mapper = { dtoList ->
                // For now, return empty list since we don't have rental data
                emptyList<BookDto>().toDomainBookList()
            }
        ).map { result ->
            when (result) {
                is Result.Success -> Result.Success(emptyList<Book>()) // Return empty for now
                is Result.Error -> result
                is Result.Loading -> result
            }
        }

    override fun getPurchasedBooks(): Flow<Result<List<Book>>> =
        apiFlow<List<BookDto>, List<Book>>(
            apiCall = { apiService.getPurchasedBooks(1) }, // userId parameter ignored by API
            mapper = { dtoList ->
                // For now, return empty list since we don't have purchase data
                emptyList<BookDto>().toDomainBookList()
            }
        ).map { result ->
            when (result) {
                is Result.Success -> Result.Success(emptyList<Book>()) // Return empty for now
                is Result.Error -> result
                is Result.Loading -> result
            }
        }

    override fun getDiscoverBooks(): Flow<Result<List<Book>>> =
        apiFlow<List<BookDto>, List<Book>>(
            apiCall = { apiService.getDiscoverBooks() },
            mapper = { dtoList ->
                // Since we're using the same endpoint, we can shuffle or take different subset
                val discover = dtoList?.shuffled()?.take(15) ?: emptyList()
                discover.toDomainBookList()
            }
        ).map { result ->
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
        return apiFlow<BookDto?, Book?>(
            apiCall = { apiService.getBookById(idAsInt) },
            mapper = { dto -> dto?.toDomainBook() }
        )
    }

    override suspend fun searchBooks(query: String): Result<List<Book>> {
        // Since search endpoint doesn't exist, get all books and filter locally
        return try {
            val response = apiService.getAllBooks()
            if (response.isSuccessful) {
                val allBooks = response.body()?.toDomainBookList() ?: emptyList()
                // Filter books that match the query
                val filteredBooks = allBooks.filter { book ->
                    book.title.contains(query, ignoreCase = true) ||
                            book.description?.contains(query, ignoreCase = true) == true
                }
                Result.Success(filteredBooks)
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
}