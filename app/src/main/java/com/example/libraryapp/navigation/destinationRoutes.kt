package com.example.libraryapp.navigation

import kotlinx.serialization.Serializable

interface AppScreenRoute

@Serializable
object HomeRoute : AppScreenRoute

@Serializable
object MyLibraryRoute : AppScreenRoute

@Serializable
data class BookDetailRoute(val bookId: String) : AppScreenRoute

@Serializable
data class SearchResultsRoute(val query: String) : AppScreenRoute // Add this