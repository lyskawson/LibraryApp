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
    var searchWidgetState by rememberSaveable { mutableStateOf(SearchWidgetState.CLOSED) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentScreenTitle = remember(currentBackStackEntry) {
        when (currentBackStackEntry?.destination?.route) {
            HomeRoute::class.qualifiedName -> "Home"
            MyLibraryRoute::class.qualifiedName -> "My Library"
            else -> "Library App" // Default title
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            SearchTopAppBar(
                title = currentScreenTitle,
                searchQuery = searchQuery,
                searchWidgetState = searchWidgetState,
                onSearchQueryChange = { query ->
                    searchQuery = query
                },
                onSearchWidgetChange = { newState ->
                    searchWidgetState = newState

                },
                onSearchTriggered = { query ->
                    if (query.isNotBlank()) {
                        navController.navigate(SearchResultsRoute(query = query))
                        searchWidgetState = SearchWidgetState.CLOSED
                    }
                },
                onClearSearch = {
                    searchQuery = ""
                }
            )
        },
        bottomBar = {
            AppBottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
            // TODO: Pass search query/state down if needed
            // searchViewModel = hiltViewModel() (inject later)
            // onSearchQuery = { query -> searchViewModel.search(query) }
        )
    }
}
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onBookClick = { bookId ->
                    navController.navigate(BookDetailRoute(bookId = bookId))
                }
            )
        }

        composable<MyLibraryRoute> {
            MyLibraryScreen(
                onBookClick = { bookId ->
                    navController.navigate(BookDetailRoute(bookId = bookId))
                }
            )
        }

        composable<BookDetailRoute> { backStackEntry ->
            val routeArgs: BookDetailRoute = backStackEntry.toRoute()
            BookDetailScreen(
                navigateUp = { navController.navigateUp() } // Standard back navigation
            )
        }

        composable<SearchResultsRoute> { backStackEntry ->
            val routeArgs: SearchResultsRoute = backStackEntry.toRoute()
            SearchResultsScreen(
                navigateUp = { navController.navigateUp() },
                navigateToDetail = { bookId ->
                    navController.navigate(BookDetailRoute(bookId = bookId))
                }
            )
        }
    }
}

