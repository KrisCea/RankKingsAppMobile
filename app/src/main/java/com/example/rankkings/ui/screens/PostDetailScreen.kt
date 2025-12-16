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
import com.example.rankkings.model.Post
import com.example.rankkings.ui.components.AlbumRankingItem
import com.example.rankkings.ui.components.FullScreenLoadingIndicator
import com.example.rankkings.ui.components.PostCard
import com.example.rankkings.ui.theme.CommentBlue
import com.example.rankkings.ui.theme.DarkCard
import com.example.rankkings.ui.theme.Gold
import com.example.rankkings.ui.theme.TextSecondary
import com.example.rankkings.viewmodel.AuthViewModel
import com.example.rankkings.viewmodel.PostViewModel

/**
 * Pantalla de detalle de un post
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    postViewModel: PostViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {

    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    val isLoggedIn = currentUser != null

    val posts by postViewModel.posts.collectAsState(initial = emptyList())
    val post: Post? = posts.find { it.id == postId }
    val comments by postViewModel.comments.collectAsState()

    LaunchedEffect(postId) {
        postViewModel.loadComments(postId)
    }



    var commentText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Ranking") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Gold
                )
            )
        },
        bottomBar = {
            if (isLoggedIn) {
                Surface(shadowElevation = 8.dp) {
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
                            maxLines = 3
                        )

                        IconButton(
                            onClick = {
                                currentUser?.let { user ->
                                    postViewModel.addComment(
                                        postId = postId,
                                        userId = user.id,
                                        name = user.name,
                                        content = commentText
                                    )
                                    commentText = ""
                                }
                            },
                            enabled = commentText.isNotBlank()
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Enviar")
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

                item {
                    PostCard(
                        post = post,
                        albumImages = emptyList(),
                        onPostClick = {},
                        onLikeClick = {
                            if (isLoggedIn) postViewModel.toggleLike(post)
                            else onNavigateToLogin()
                        },
                        onCommentClick = {},
                        onSaveClick = {
                            if (isLoggedIn) postViewModel.toggleSave(post)
                            else onNavigateToLogin()
                        },
                        onProfileClick = {},
                        isLoggedIn = isLoggedIn
                    )
                }

                item {
                    Text(
                        text = "Comentarios",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Gold,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                if (comments.isEmpty()) {
                    item {
                        Text(
                            text = "Sé el primero en comentar",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
