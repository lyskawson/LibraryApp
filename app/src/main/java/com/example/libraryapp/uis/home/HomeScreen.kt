package com.example.libraryapp.uis.home

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.libraryapp.customcomposables.BookListItem
import com.example.libraryapp.data.entities.Book
import com.example.libraryapp.utils.randomColor


data class SimpleBook(val id: String, val title: String, val author: String? = "Some Author", val coverUrl: String? = null)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onBookClick: (bookId: String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoadingDiscover -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.error != null -> {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            }
            else -> {
                HomeScreenContent(
                    uiState = uiState,
                    onBookClick = onBookClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onBookClick: (bookId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        if (uiState.featuredBooks.isNotEmpty()) {
            item {
                SectionTitle("Featured Books", modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))
                FeaturedBooksRow(
                    books = uiState.featuredBooks,
                    onBookClick = onBookClick,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        if (uiState.categories.isNotEmpty()) {
            item {
                SectionTitle("Categories", modifier = Modifier.padding(start = 16.dp, top = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    items(uiState.categories) { category ->
                        SuggestionChip(onClick = { /* TODO: Handle category click -> maybe navigate or filter */ }, label = { Text(category) })
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        if (uiState.discoverBooks.isNotEmpty()) {
            item {
                SectionTitle("Discover", modifier = Modifier.padding(start = 16.dp, top = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(uiState.discoverBooks, key = { it.id }) { book ->
                BookListItem(
                    book = book,
                    onClick = { onBookClick(book.id) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}





@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier
    )
}

@Composable
fun FeaturedBooksRow(
    books: List<Book>,
    onBookClick: (bookId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp) // Space between items
    ) {
        items(books, key = { it.id }) { book ->
            FeaturedBookItem(book = book, onClick = { onBookClick(book.id) })
        }
    }
}

@Composable
fun FeaturedBookItem(
    book: Book,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(120.dp)
    ) {
        Column {
            val randomBgColor = randomColor()
            val colorDrawable = ColorDrawable(randomBgColor.toArgb())
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(book.coverUrl)
                    .crossfade(true)
                    .placeholder(colorDrawable)
                    .error(colorDrawable)
                    .build(),
                contentDescription = "${book.title} cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Text(
                text = book.title,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(8.dp),
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}


