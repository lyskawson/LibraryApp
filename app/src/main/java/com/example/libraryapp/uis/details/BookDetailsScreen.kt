package com.example.libraryapp.uis.details

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.libraryapp.data.entities.Book
import com.example.libraryapp.uis.home.SimpleBook
import com.example.libraryapp.utils.randomColor

val placeholderBookDetail = SimpleBook(
    id = "preview1",
    title = "A Fantastically Interesting Book",
    author = "Jane Doe",
    coverUrl = null // Let placeholder show
)

@OptIn(ExperimentalMaterial3Api::class) // For TopAppBar
@Composable
fun BookDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: BookDetailViewModel = hiltViewModel(),
    navigateUp: () -> Unit // Callback to go back
    // Inject ViewModel later: viewModel: BookDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* Title can be empty or dynamic */ },
                navigationIcon = {
                    IconButton(onClick = navigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back" // stringResource(R.string.back)
                        )
                    }
                },
                // Optional: Add actions like share, favorite etc.
                // actions = { ... }
                // colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent) // Make transparent if overlapping content
            )
        }
    ) { innerPadding ->
        Box( // Use Box to handle loading/error/content switching
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Column( // Use Column for error message and retry button
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.retryLoad() }) {
                            Text("Retry")
                        }
                    }
                }
                uiState.book != null -> {
                    // Pass the loaded book to the content composable
                    BookDetailContent(
                        book = uiState.book!!, // Safe non-null assertion here
                        modifier = Modifier
                            .fillMaxSize() // Content fills the Box
                            .verticalScroll(rememberScrollState())
                    )
                }
                // Optional: Handle case where book is null but not loading and no error
                // else -> { Text("Book details not available.", modifier = Modifier.align(Alignment.Center)) }
            }
        }
    }
}

@Composable
fun BookDetailContent(
    book: Book,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val randomBgColor = randomColor()
        val colorDrawable = ColorDrawable(randomBgColor.toArgb())
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(book.coverUrl)
                .crossfade(true)
                .placeholder(colorDrawable) // Use the random color as placeholder
                .error(colorDrawable) // Same for error
                .build(),
            contentDescription = "${book.title} cover",
            contentScale = ContentScale.Fit, // Fit might be better here
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth(0.7f)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = book.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Author
        Text(
            text = book.authors.joinToString().ifEmpty { "Unknown Author" },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Display More Details ---
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly, // Space out details
            verticalAlignment = Alignment.CenterVertically
        ) {
            book.averageRating?.let { rating ->
                DetailItem(label = "Rating", value = "%.1f ★".format(rating))
            }
            book.pageCount?.let { count ->
                DetailItem(label = "Pages", value = count.toString())
            }
            book.publishedDate?.let { date ->
                DetailItem(label = "Published", value = date)
            }
        }
        // --- End More Details ---


        Spacer(modifier = Modifier.height(24.dp))

        // Description (uses book.description - updated)
        Text(
            // Use actual description or a placeholder if null/empty
            text = book.description?.takeIf { it.isNotBlank() } ?: "No description available.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Categories/Genres (uses book.categories - added)
        book.categories?.takeIf { it.isNotEmpty() }?.let { categories ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                categories.take(3).forEach { category -> // Show max 3 categories for example
                    SuggestionChip(
                        onClick = { /* TODO: Maybe navigate to category search? */ },
                        label = { Text(category) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(24.dp))






        // Action Buttons (Read/Rent/Buy)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            Button(onClick = { /* TODO: Handle Read */ }) {
                Text("Read") // stringResource(R.string.read)
            }
            OutlinedButton(onClick = { /* TODO: Handle Rent/Buy */ }) {
                // Change text based on rented/purchased status later
                Text("Rent/Buy") // stringResource(R.string.rent_buy)
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Bottom padding
    }
}

@Composable
private fun DetailItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
