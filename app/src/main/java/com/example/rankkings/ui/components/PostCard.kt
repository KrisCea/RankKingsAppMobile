package com.example.rankkings.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.rankkings.model.Post
import com.example.rankkings.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PostCard(
    post: Post,
    albumImages: List<String> = emptyList(),
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onSaveClick: () -> Unit,
    onProfileClick: (Int) -> Unit,
    isLoggedIn: Boolean
) {
    Card(
        onClick = onPostClick, // ✅ CLICK SEGURO
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            PostHeader(
                name = post.name,
                userId = post.userId,
                timestamp = post.timestamp,
                onProfileClick = onProfileClick
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = post.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Gold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (post.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = post.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (albumImages.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(albumImages.take(5)) { image ->
                        AlbumPreviewItem(image)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            PostActions(
                post = post,
                onLikeClick = onLikeClick,
                onCommentClick = onCommentClick,
                onSaveClick = onSaveClick,
                isLoggedIn = isLoggedIn
            )
        }
    }
}

/* ---------------- HEADER ---------------- */

@Composable
private fun PostHeader(
    name: String,
    userId: Int,
    timestamp: Long,
    onProfileClick: (Int) -> Unit
) {
    IconButton(onClick = { onProfileClick(userId) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = DarkSurface,
                border = BorderStroke(2.dp, Gold)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = formatTimestamp(timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

/* ---------------- ALBUM PREVIEW ---------------- */

@Composable
private fun AlbumPreviewItem(imageUri: String) {
    Card(
        modifier = Modifier.size(100.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        AsyncImage(
            model = imageUri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

/* ---------------- ACTIONS ---------------- */

@Composable
private fun PostActions(
    post: Post,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onSaveClick: () -> Unit,
    isLoggedIn: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

            ActionButton(
                icon = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                count = post.likesCount,
                tint = if (post.isLiked) LikeRed else TextSecondary,
                enabled = isLoggedIn,
                onClick = onLikeClick
            )

            ActionButton(
                icon = Icons.Outlined.ChatBubbleOutline,
                count = post.commentsCount,
                tint = TextSecondary,
                onClick = onCommentClick
            )
        }

        if (isLoggedIn) {
            ActionButton(
                icon = if (post.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                count = null,
                tint = if (post.isSaved) SaveGreen else TextSecondary,
                onClick = onSaveClick
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    count: Int?,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    IconButton(onClick = onClick, enabled = enabled) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tint)
            if (count != null && count > 0) {
                Spacer(Modifier.width(4.dp))
                Text(text = formatCount(count), color = tint)
            }
        }
    }
}

/* ---------------- HELPERS ---------------- */

private fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60000 -> "Ahora"
        diff < 3600000 -> "${diff / 60000}m"
        diff < 86400000 -> "${diff / 3600000}h"
        diff < 604800000 -> "${diff / 86400000}d"
        else -> SimpleDateFormat("dd MMM", Locale("es", "ES")).format(Date(timestamp))
    }
}

private fun formatCount(count: Int): String =
    when {
        count < 1000 -> count.toString()
        count < 1_000_000 -> "${count / 1000}k"
        else -> "${count / 1_000_000}M"
    }
