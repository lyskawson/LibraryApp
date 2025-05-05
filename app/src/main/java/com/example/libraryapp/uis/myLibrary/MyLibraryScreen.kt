package com.example.libraryapp.uis.myLibrary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.libraryapp.customcomposables.BookListItem

enum class LibraryFilterType {
    RENTED,
    PURCHASED
}


@Composable
fun MyLibraryScreen(
    modifier: Modifier = Modifier,
    onBookClick: (bookId: String) -> Unit = {},
    viewModel: MyLibraryViewModel = hiltViewModel()
) {
//    var selectedFilter by rememberSaveable { mutableStateOf(LibraryFilterType.RENTED) }
//    val rentedBooks = remember {
//        List(15) { SimpleBook(id = "f$it", title = "Book ${it + 1}") }
//    }
//    val purchasedBooks = remember {
//        List(15) { SimpleBook(id = "f$it", title = "Book ${it + 1}") }
//    }


    // Observe the UI state from the ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Determine items to show based on the state from the ViewModel
    val itemsToShow = when (uiState.selectedFilter) {
        LibraryFilterType.RENTED -> uiState.rentedBooks
        LibraryFilterType.PURCHASED -> uiState.purchasedBooks
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp), // Add some horizontal padding to the whole screen
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Segmented Button Row
        LibraryFilterSelector(
            selectedType = uiState.selectedFilter,
            onSelectionChange = { newFilter -> viewModel.selectFilter(newFilter) }, // Call ViewModel func,
            modifier = Modifier.padding(vertical = 16.dp) // Add padding above/below selector
        )

        // Divider (Optional, for visual separation)
        HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

        // List of Books
        Box(modifier = Modifier.fillMaxSize()) { // Use Box to overlay loading/error/list
            when {
                uiState.isLoadingRented -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                    // Optional: Add a retry button
                    // Button(onClick = { viewModel.refreshData() }, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)) { Text("Retry") }
                }
                itemsToShow.isEmpty() && !uiState.isLoadingRented -> {
                    Text(
                        text = "No ${uiState.selectedFilter.name.lowercase()} books found.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                else -> {
                    // Display the list when data is loaded and no error
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(), // Occupy the Box space
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(itemsToShow, key = { it.id }) { book ->
                            BookListItem(
                                book = book,
                                onClick = { onBookClick(book.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryFilterSelector(
    selectedType: LibraryFilterType,
    onSelectionChange: (LibraryFilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    val filterOptions = LibraryFilterType.values() // Get all enum values

    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        filterOptions.forEachIndexed { index, filterType ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = filterOptions.size),
                onClick = { onSelectionChange(filterType) },
                selected = (filterType == selectedType) // Check if this button's type is selected
            ) {
                // Capitalize the enum name for display, or use string resources
                Text(filterType.name.lowercase().replaceFirstChar { it.titlecase() })
            }
        }
    }
}


