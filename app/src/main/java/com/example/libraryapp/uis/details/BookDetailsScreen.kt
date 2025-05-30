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
import com.example.libraryapp.utils.randomColor


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: BookDetailViewModel = hiltViewModel(),
    navigateUp: () -> Unit
    // for later to Inject ViewModel: viewModel: BookDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { /*for now empty */ },
                navigationIcon = {
                    IconButton(onClick = navigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back" // stringResource(R.string.back)
                        )
                    }
                },
                // actions = { ... }
                // colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent) // Make transparent if overlapping content
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Column(
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
                    BookDetailContent(
                        book = uiState.book!!,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    )
                }
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
                .placeholder(colorDrawable)
                .error(colorDrawable)
                .build(),
            contentDescription = "${book.title} cover",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth(0.7f)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = book.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = book.authors.joinToString().ifEmpty { "Unknown Author" },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
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


        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = book.description?.takeIf { it.isNotBlank() } ?: "No description available.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        book.categories?.takeIf { it.isNotEmpty() }?.let { categories ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                categories.take(3).forEach { category ->
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
                Text("Read")
            }
            OutlinedButton(onClick = { /* TODO: Handle Rent/Buy */ }) {
                Text("Rent/Buy")
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
