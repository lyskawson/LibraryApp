package com.example.libraryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.libraryapp.customcomposables.SearchTopAppBar
import com.example.libraryapp.customcomposables.SearchWidgetState
import com.example.libraryapp.navigation.AppBottomNavigationBar
import com.example.libraryapp.navigation.BookDetailRoute
import com.example.libraryapp.navigation.HomeRoute
import com.example.libraryapp.navigation.MyLibraryRoute
import com.example.libraryapp.navigation.SearchResultsRoute
import com.example.libraryapp.uis.details.BookDetailScreen
import com.example.libraryapp.uis.home.HomeScreen
import com.example.libraryapp.uis.myLibrary.MyLibraryScreen
import com.example.libraryapp.uis.search.SearchResultsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LibraryApp()
        }
    }
}

@Composable
fun LibraryApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController() // Allow injecting NavController for previews/tests
) {
    // --- State Hoisting for Search ---
    var searchWidgetState by rememberSaveable { mutableStateOf(SearchWidgetState.CLOSED) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    // ---------------------------------

    // --- Determine Current Screen Title ---
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentScreenTitle = remember(currentBackStackEntry) { // Recalculate only when backstack changes
        when (currentBackStackEntry?.destination?.route) {
            HomeRoute::class.qualifiedName -> "Home" // Use qualified name for comparison
            MyLibraryRoute::class.qualifiedName -> "My Library"
            else -> "Library App" // Default title
        }
    }
    // ------------------------------------

    Scaffold(
        modifier = modifier,
        topBar = {
            SearchTopAppBar(
                title = currentScreenTitle,
                searchQuery = searchQuery,
                searchWidgetState = searchWidgetState,
                onSearchQueryChange = { query ->
                    searchQuery = query
                    // Optional: Trigger search dynamically as user types (debounce recommended)
                },
                onSearchWidgetChange = { newState ->
                    searchWidgetState = newState
                    // If closing search, potentially clear query (handled in component now)
                    // if(newState == SearchWidgetState.CLOSED) {
                    //    searchQuery = ""
                    // }
                },
                onSearchTriggered = { query ->
                    if (query.isNotBlank()) { // Only navigate if query is not blank
                        navController.navigate(SearchResultsRoute(query = query))
                        // Optional: Clear search field immediately after navigating
                        // searchQuery = "" // Or clear it when the SearchResultsScreen appears
                        // Optional: Close search bar after triggering
                        searchWidgetState = SearchWidgetState.CLOSED
                    }// Optionally close search bar after triggering
                },
                onClearSearch = {
                    searchQuery = "" // Clear the state held here
                }
            )
        },
        bottomBar = {
            AppBottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        // Pass the navController and the padding provided by Scaffold
        AppNavHost(
            navController = navController,
            // Apply the padding to the NavHost container
            // This padding now accounts for BOTH top and bottom bars
            modifier = Modifier.padding(innerPadding)
            // --- TODO: Pass search query/state down if needed ---
            // Pass searchQuery or search related callbacks down to screens
            // that need to display search results, e.g.:
            // searchViewModel = hiltViewModel() (inject later)
            // onSearchQuery = { query -> searchViewModel.search(query) }
        )
    }
}
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier // Modifier for padding, etc.
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute, // Keep start destination
        modifier = modifier
    ) {
        composable<HomeRoute> {
            HomeScreen(
                // Pass a lambda that navigates to the detail screen
                onBookClick = { bookId ->
                    navController.navigate(BookDetailRoute(bookId = bookId))
                }
            )
        }

        composable<MyLibraryRoute> {
            MyLibraryScreen(
                // Pass a lambda that navigates to the detail screen
                onBookClick = { bookId ->
                    navController.navigate(BookDetailRoute(bookId = bookId))
                }
            )
        }

        // --- Add Destination for Book Detail ---
        composable<BookDetailRoute> { backStackEntry ->
            // Automatically extracts arguments using .toRoute()
            val routeArgs: BookDetailRoute = backStackEntry.toRoute()
            BookDetailScreen(
                navigateUp = { navController.navigateUp() } // Standard back navigation
            )
        }
        // --- End Book Detail Destination ---

        // --- Add Destination for Search Results ---
        composable<SearchResultsRoute> { backStackEntry ->
            // Automatically extracts arguments using .toRoute()
            val routeArgs: SearchResultsRoute = backStackEntry.toRoute()
            SearchResultsScreen(
                navigateUp = { navController.navigateUp() },
                navigateToDetail = { bookId ->
                    navController.navigate(BookDetailRoute(bookId = bookId))
                }
            )
        }
        // --- End Search Results Destination ---
    }
}

