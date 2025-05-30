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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction

enum class SearchWidgetState {
    OPENED,
    CLOSED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    searchQuery: String,
    searchWidgetState: SearchWidgetState,
    onSearchQueryChange: (String) -> Unit,
    onSearchWidgetChange: (SearchWidgetState) -> Unit,
    onSearchTriggered: (String) -> Unit,
    onClearSearch: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(searchWidgetState) {
        if (searchWidgetState == SearchWidgetState.OPENED) {
            try {
                focusRequester.requestFocus()
            } catch (e: IllegalStateException) {
                // Handle case
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
                            // if (!focusState.isFocused && searchQuery.isBlank()) {
                            //    onSearchWidgetChange(SearchWidgetState.CLOSED)
                            // }
                        },
                    value = searchQuery,
                    onValueChange = { onSearchQueryChange(it) },
                    placeholder = { Text("Search books...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (searchQuery.isNotBlank()) {
                                onSearchTriggered(searchQuery)
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        }
                    ),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = onClearSearch) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
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
                    onClearSearch()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close search" // stringResource
                    )
                }
            }
            // A navigation icon for CLOSED state Drawer menu icon
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
            }
        },
    )
}

