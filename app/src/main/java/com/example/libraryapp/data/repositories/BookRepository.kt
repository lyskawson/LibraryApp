package com.example.libraryapp.data.repositories

import android.util.Log
import com.example.libraryapp.data.dtos.BookCreateDto
import com.example.libraryapp.data.dtos.BookDto
import com.example.libraryapp.data.dtos.PurchaseCreateDto
import com.example.libraryapp.data.dtos.RentalDto
import com.example.libraryapp.data.dtos.UserLibraryDto
import com.example.libraryapp.data.entities.Book
import com.example.libraryapp.data.remote.LibraryApiService
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
import kotlin.jvm.JvmName


fun BookDto.toDomainBook(): Book {
    return Book(
        id = this.id.toString(),
        title = this.title,
        authors = this.author?.let { listOf("${it.firstName} ${it.lastName}") } ?: emptyList(),
        description = this.description,
        coverUrl = this.coverUrl,
        publishedDate = this.publishedYear?.toString(),
        categories = this.categories,
        averageRating = this.averageRating,
        pageCount = this.pages
    )
}

@JvmName("mapBookDtoListToDomain")
fun List<BookDto>.toDomainBookList(): List<Book> {
    return this.map { it.toDomainBook() }
}

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

@JvmName("mapUserLibraryDtoListToDomain")
fun List<UserLibraryDto>.toDomainBookList(): List<Book> {
    return this.map { it.toDomainBook() }
}

sealed class Result<out T> {
    data object Loading : Result<Nothing>()
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable? = null, val message: String? = null) : Result<Nothing>()
}

interface BookRepository {
    fun getAllBooks(): Flow<Result<List<Book>>>
    fun getFeaturedBooks(): Flow<Result<List<Book>>>
    fun getRentedBooks(userId: Int): Flow<Result<List<Book>>>
    fun getPurchasedBooks(userId: Int): Flow<Result<List<Book>>>
    fun getDiscoverBooks(): Flow<Result<List<Book>>>
    fun getBookDetails(bookId: String): Flow<Result<Book?>>
    suspend fun createBook(book: BookCreateDto): Result<Map<String, String>>
    suspend fun searchBooks(query: String): Result<List<Book>>
    suspend fun purchaseBook(purchaseDto: PurchaseCreateDto): Result<Map<String, Any>>
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

    override fun getAllBooks(): Flow<Result<List<Book>>> =
        apiFlow<List<BookDto>, List<Book>>(
            apiCall = { apiService.getAllBooks() },
            mapper = { dtoList -> dtoList?.toDomainBookList() }
        ).map { result ->
            when (result) {
                is Result.Success -> Result.Success(result.data ?: emptyList())
                is Result.Error -> result
                is Result.Loading -> result
            }
        }

    override fun getFeaturedBooks(): Flow<Result<List<Book>>> =
        apiFlow<List<BookDto>, List<Book>>(
            apiCall = { apiService.getFeaturedBooks() },
            mapper = { dtoList ->
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

    override fun getRentedBooks(userId: Int): Flow<Result<List<Book>>> =
        apiFlow<List<RentalDto>, List<Book>>(
            apiCall = { apiService.getRentedBooks(userId) },
            mapper = { rentalList ->
                Log.w(TAG, "getRentedBooks: API returned ${rentalList?.size ?: 0} rentals, but they cannot be mapped to Book objects. Returning empty list.")
                emptyList()
            }
        ).map { result ->
            when (result) {
                is Result.Success -> Result.Success(result.data ?: emptyList())
                is Result.Error -> result
                is Result.Loading -> result
            }
        }

    override fun getPurchasedBooks(userId: Int): Flow<Result<List<Book>>> =
        apiFlow<List<UserLibraryDto>, List<Book>>(
            apiCall = { apiService.getPurchasedBooks(userId) },
            mapper = { dtoList -> dtoList?.toDomainBookList() }
        ).map { result ->
            when (result) {
                is Result.Success -> Result.Success(result.data ?: emptyList())
                is Result.Error -> result
                is Result.Loading -> result
            }
        }

    override fun getDiscoverBooks(): Flow<Result<List<Book>>> =
        apiFlow<List<BookDto>, List<Book>>(
            apiCall = { apiService.getDiscoverBooks() },
            mapper = { dtoList ->
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

    override suspend fun createBook(book: BookCreateDto): Result<Map<String, String>> {
        return try {
            val response = apiService.createBook(book)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
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

    override suspend fun searchBooks(query: String): Result<List<Book>> {
        return try {
            val response = apiService.getAllBooks()
            if (response.isSuccessful) {
                val allBooks = response.body()?.toDomainBookList() ?: emptyList()
                if (query.isBlank()) {
                    return Result.Success(allBooks)
                }
                val filteredBooks = allBooks.filter { book ->
                    book.title.contains(query, ignoreCase = true) ||
                            (book.authors.any { it.contains(query, ignoreCase = true) })
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

    override suspend fun purchaseBook(purchaseDto: PurchaseCreateDto): Result<Map<String, Any>> {
        return try {
            val response = apiService.purchaseBook(purchaseDto)
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "purchaseBook successful: ${response.body()}")
                Result.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Brak szczegółów błędu"
                Log.e(TAG, "purchaseBook error: Code ${response.code()}, Body: $errorBody")
                Result.Error(message = "Błąd API (${response.code()}): $errorBody")
            }
        } catch (e: HttpException) {
            Log.e(TAG, "purchaseBook HttpException", e)
            Result.Error(exception = e, message = "Błąd sieci (HTTP): ${e.message()}")
        } catch (e: IOException) {
            Log.e(TAG, "purchaseBook IOException", e)
            Result.Error(exception = e, message = "Błąd połączenia. Sprawdź internet.")
        } catch (e: Exception) {
            Log.e(TAG, "purchaseBook Exception", e)
            Result.Error(exception = e, message = "Wystąpił nieoczekiwany błąd.")
        }
    }
}