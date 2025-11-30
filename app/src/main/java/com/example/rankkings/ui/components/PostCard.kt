package com.example.rankkings.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.* 
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

/**
 * Card que muestra un post completo con sus álbumes e interacciones
 */
@Composable
fun PostCard(
    post: Post,
    albumImages: List<String> = emptyList(),
    onPostClick: () -> Unit = {},
    onLikeClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    isLoggedIn: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onPostClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header del post (usuario, fecha)
            PostHeader(
                name = post.name, // CAMBIO AQUÍ
                userId = post.userId,
                timestamp = post.timestamp,
                onProfileClick = onProfileClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Título y descripción
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Gold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (post.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = post.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Preview de álbumes (scroll horizontal)
            if (albumImages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(albumImages.take(5)) { imageUri ->
                        AlbumPreviewItem(imageUri = imageUri)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Acciones (like, comentar, guardar)
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

/**
 * Header del post con info del usuario
 */
@Composable
private fun PostHeader(
    name: String, // CAMBIO AQUÍ
    userId: String,
    timestamp: Long,
    onProfileClick: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(onClick = { onProfileClick(userId) })
    ) {
        // Avatar placeholder
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = DarkSurface,
            border = BorderStroke(2.dp, Gold)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = name, // CAMBIO AQUÍ
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

/**
 * Preview de un álbum en el post
 */
@Composable
private fun AlbumPreviewItem(imageUri: String) {
    Card(
        modifier = Modifier.size(100.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        AsyncImage(
            model = imageUri,
            contentDescription = "Álbum",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * Botones de acción del post
 */
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Like
            ActionButton(
                icon = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                count = post.likesCount,
                tint = if (post.isLiked) LikeRed else TextSecondary,
                onClick = onLikeClick,
                enabled = isLoggedIn
            )

            // Comentarios
            ActionButton(
                icon = Icons.Outlined.ChatBubbleOutline,
                count = post.commentsCount,
                tint = TextSecondary,
                onClick = onCommentClick
            )
        }

        // Guardar
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

/**
 * Botón de acción con icono y contador
 */
@Composable
private fun ActionButton(
    icon: ImageVector,
    count: Int?,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        if (count != null && count > 0) {
            Text(
                text = formatCount(count),
                style = MaterialTheme.typography.bodyMedium,
                color = tint
            )
        }
    }
}

/**
 * Formatea el timestamp a texto legible
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Ahora"
        diff < 3600000 -> "${diff / 60000}m"
        diff < 86400000 -> "${diff / 3600000}h"
        diff < 604800000 -> "${diff / 86400000}d"
        else -> {
            val sdf = SimpleDateFormat("dd MMM", Locale("es", "ES"))
            sdf.format(Date(timestamp))
        }
    }
}

/**
 * Formatea números grandes (1000+ = 1k)
 */
private fun formatCount(count: Int): String {
    return when {
        count < 1000 -> count.toString()
        count < 1000000 -> "${count / 1000}k"
        else -> "${count / 1000000}M"
    }
}
