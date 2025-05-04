package com.example.libraryapp.customcomposables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.example.libraryapp.ui.theme.LibraryAppTheme

enum class SearchWidgetState {
    OPENED,
    CLOSED
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class) // For LocalSoftwareKeyboardController
@Composable
fun SearchTopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    searchQuery: String,
    searchWidgetState: SearchWidgetState,
    onSearchQueryChange: (String) -> Unit,
    onSearchWidgetChange: (SearchWidgetState) -> Unit,
    onSearchTriggered: (String) -> Unit, // Callback when search action is performed
    onClearSearch: () -> Unit // Callback to explicitly clear search
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Request focus when the search widget opens
    LaunchedEffect(searchWidgetState) {
        if (searchWidgetState == SearchWidgetState.OPENED) {
            try {
                focusRequester.requestFocus()
            } catch (e: IllegalStateException) {
                // Handle cases where focus request might fail (e.g., view not ready)
                // Log or ignore as appropriate
            }
        }
    }

    TopAppBar(
        modifier = modifier,
        title = {
            if (searchWidgetState == SearchWidgetState.CLOSED) {
                Text(text = title)
            } else {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            // Optional: Close search if text field loses focus?
                            // if (!focusState.isFocused && searchQuery.isBlank()) {
                            //    onSearchWidgetChange(SearchWidgetState.CLOSED)
                            // }
                        },
                    value = searchQuery,
                    onValueChange = { onSearchQueryChange(it) },
                    placeholder = { Text("Search books...") }, // Consider using stringResource
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (searchQuery.isNotBlank()) { // Only trigger search if query exists
                                onSearchTriggered(searchQuery)
                                keyboardController?.hide() // Hide keyboard on search action
                                focusManager.clearFocus() // Clear focus
                            }
                        }
                    ),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = onClearSearch) { // Use the clear callback
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search" // stringResource
                                )
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors( // Make TextField background transparent
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary, // Or Transparent
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f) // Or Transparent
                    )
                )
            }
        },
        navigationIcon = {
            if (searchWidgetState == SearchWidgetState.OPENED) {
                IconButton(onClick = {
                    onSearchWidgetChange(SearchWidgetState.CLOSED)
                    onClearSearch() // Clear search when closing via back arrow
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close search" // stringResource
                    )
                }
            }
            // Add navigation icon for CLOSED state if needed (e.g., Drawer menu icon)
            // else { IconButton(onClick = { /*TODO: Open Drawer*/ }) { Icon(...) } }
        },
        actions = {
            if (searchWidgetState == SearchWidgetState.CLOSED) {
                IconButton(onClick = { onSearchWidgetChange(SearchWidgetState.OPENED) }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Open search" // stringResource
                    )
                }
                // Add other actions for the non-search state if needed
                // IconButton(onClick = { /* ... */ }) { Icon(...) }
            }
            // No actions needed in OPENED state usually, unless you add filtering etc.
        },
        // Optional: Customize colors if needed
        // colors = TopAppBarDefaults.topAppBarColors(...)
    )
}

@Preview(name = "Search Closed")
@Composable
private fun SearchTopAppBarClosedPreview() {
    LibraryAppTheme {
        SearchTopAppBar(
            title = "Home",
            searchQuery = "",
            searchWidgetState = SearchWidgetState.CLOSED,
            onSearchQueryChange = {},
            onSearchWidgetChange = {},
            onSearchTriggered = {},
            onClearSearch = {}
        )
    }
}

@Preview(name = "Search Opened Empty")
@Composable
private fun SearchTopAppBarOpenedEmptyPreview() {
    LibraryAppTheme {
        SearchTopAppBar(
            title = "Home", // Title is hidden when opened
            searchQuery = "",
            searchWidgetState = SearchWidgetState.OPENED,
            onSearchQueryChange = {},
            onSearchWidgetChange = {},
            onSearchTriggered = {},
            onClearSearch = {}
        )
    }
}

@Preview(name = "Search Opened With Text")
@Composable
private fun SearchTopAppBarOpenedWithTextPreview() {
    LibraryAppTheme {
        SearchTopAppBar(
            title = "Home",
            searchQuery = "Kotlin Programming",
            searchWidgetState = SearchWidgetState.OPENED,
            onSearchQueryChange = {},
            onSearchWidgetChange = {},
            onSearchTriggered = {},
            onClearSearch = {}
        )
    }
}