package com.example.rankkings.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rankkings.ui.components.AlbumRankingItem
import com.example.rankkings.ui.components.FullScreenLoadingIndicator
import com.example.rankkings.ui.components.PostCard
import com.example.rankkings.ui.theme.CommentBlue
import com.example.rankkings.ui.theme.DarkCard
import com.example.rankkings.ui.theme.Gold
import com.example.rankkings.ui.theme.TextSecondary
import com.example.rankkings.viewmodel.AuthViewModel
import com.example.rankkings.viewmodel.PostViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla de detalle de un post
 * Muestra el post completo, álbumes y comentarios
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    postViewModel: PostViewModel = hiltViewModel(), // Inyectar PostViewModel con Hilt
    authViewModel: AuthViewModel = hiltViewModel() // Inyectar AuthViewModel con Hilt
) {
    val currentUser by authViewModel.currentUser.collectAsState() // Obtener currentUser internamente
    val isLoggedIn = currentUser != null
    val coroutineScope = rememberCoroutineScope()

    var post by remember { mutableStateOf<com.example.rankkings.model.Post?>(null) }
    val albums by postViewModel.postAlbums.collectAsState()
    val comments by postViewModel.postComments.collectAsState()

    var commentText by remember { mutableStateOf("") }

    // Cargar datos del post
    LaunchedEffect(postId) {
        coroutineScope.launch {
            post = postViewModel.getPostById(postId)
            postViewModel.loadPostAlbums(postId)
            postViewModel.loadComments(postId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Ranking") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Gold,
                    navigationIconContentColor = Gold
                )
            )
        },
        bottomBar = {
            // Barra para agregar comentarios (solo si está logueado)
            currentUser?.let { user ->
                if (isLoggedIn) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                placeholder = { Text("Escribe un comentario...") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Gold,
                                    cursorColor = Gold
                                ),
                                maxLines = 3
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (commentText.isNotBlank()) {
                                        postViewModel.addComment(
                                            postId = postId,
                                            userId = user.id,
                                            name = user.name, // CAMBIO AQUÍ
                                            content = commentText
                                        )
                                        commentText = ""
                                    }
                                },
                                enabled = commentText.isNotBlank()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Enviar",
                                    tint = if (commentText.isNotBlank()) Gold
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (post == null) {
            FullScreenLoadingIndicator()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Post Card
                item {
                    post?.let { currentPost ->
                        PostCard(
                            post = currentPost,
                            albumImages = albums.map { it.albumImageUri },
                            onPostClick = { },
                            onLikeClick = {
                                if (isLoggedIn) {
                                    postViewModel.toggleLike(currentPost)
                                    coroutineScope.launch {
                                        post = postViewModel.getPostById(postId)
                                    }
                                } else {
                                    onNavigateToLogin()
                                }
                            },
                            onCommentClick = { },
                            onSaveClick = {
                                if (isLoggedIn) {
                                    postViewModel.toggleSave(currentPost)
                                    coroutineScope.launch {
                                        post = postViewModel.getPostById(postId)
                                    }
                                } else {
                                    onNavigateToLogin()
                                }
                            },
                            onProfileClick = { },
                            isLoggedIn = isLoggedIn
                        )
                    }
                }

                // Sección de álbumes
                item {
                    Text(
                        text = "Ranking de Álbumes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Gold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                    )
                }

                items(albums.sortedBy { it.ranking }) { album ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        AlbumRankingItem(album = album)
                    }
                }

                // Sección de comentarios
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Comentarios (${comments.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Gold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                    )
                }

                if (comments.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = if (isLoggedIn) "Sé el primero en comentar"
                                else "Inicia sesión para comentar",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(comments) { comment ->
                        CommentItem(
                            name = comment.name, // CAMBIO AQUÍ
                            content = comment.content,
                            timestamp = comment.timestamp
                        )
                    }
                }

                // Espaciado final
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

/**
 * Item de comentario
 */
@Composable
fun CommentItem(
    name: String, // CAMBIO AQUÍ
    content: String,
    timestamp: Long
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name, // CAMBIO AQUÍ
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CommentBlue
                )
                Text(
                    text = formatTimestamp(timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Formatea timestamp a texto legible
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
            val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale("es", "ES"))
            sdf.format(java.util.Date(timestamp))
        }
    }
}
