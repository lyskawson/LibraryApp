package com.example.libraryapp.customcomposables

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.libraryapp.data.entities.Book
import com.example.libraryapp.utils.randomColor

@OptIn(ExperimentalMaterial3Api::class) // For Card onClick
@Composable
fun BookListItem(
    book: Book, // Use a consistent data model (SimpleBook for now)
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        // elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // Optional elevation
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.Top // Align items to the top
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
                    .size(width = 80.dp, height = 120.dp) // Larger cover image
                    .clip(RoundedCornerShape(corner = CornerSize(16.dp))) // Rounded corners
                    .background(randomBgColor) // Set the random color as background
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Book Title and Author
            Column(
                modifier = Modifier
                    .weight(1f) // Takes remaining horizontal space
                    .height(120.dp), // Match image height roughly
                verticalArrangement = Arrangement.SpaceBetween // Pushes content to top and bottom
            ) {
                Column { // Group title and author
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = book.authors.joinToString().ifEmpty { "Unknown Author" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, // Slightly muted color
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Add more info here if needed (e.g., rating, buttons) aligned to bottom
                // Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { /* Buttons? */ }
            }
        }
    }
}
