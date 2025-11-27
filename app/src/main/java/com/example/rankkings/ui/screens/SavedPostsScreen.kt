package com.example.rankkings.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rankkings.ui.components.NotLoggedInPlaceholder
import com.example.rankkings.ui.components.PostCard
import com.example.rankkings.ui.components.RankkingsBottomBar
import com.example.rankkings.ui.components.RankkingsTopBar
import com.example.rankkings.viewmodel.AuthViewModel
import com.example.rankkings.viewmodel.PostViewModel

/**
 * Pantalla de posts guardados por el usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPostsScreen(
    onNavigateToPostDetail: (Int) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToLogin: () -> Unit,
    postViewModel: PostViewModel = hiltViewModel(), // Inyectar PostViewModel con Hilt
    authViewModel: AuthViewModel = hiltViewModel() // Inyectar AuthViewModel con Hilt
) {
    val posts by postViewModel.posts.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState() // Obtener currentUser internamente
    val isLoggedIn = currentUser != null
    val savedPosts = if (isLoggedIn) posts.filter { it.isSaved } else emptyList()

    Scaffold(
        topBar = {
            RankkingsTopBar(
                title = "Guardados"
            )
        },
        bottomBar = {
            RankkingsBottomBar(
                selectedRoute = "saved",
                onNavigate = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "profile" -> onNavigateToProfile()
                        // No es necesario "saved", ya estamos aquí
                    }
                },
                isLoggedIn = isLoggedIn,
                onNavigateToLogin = onNavigateToLogin
            )
        }
    ) { paddingValues ->
        if (!isLoggedIn) {
            NotLoggedInPlaceholder(onNavigateToLogin = onNavigateToLogin)
        } else if (savedPosts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "No tienes posts guardados",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Guarda los rankings que más te gusten para verlos aquí",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(savedPosts, key = { it.id }) { post ->
                    val albums by postViewModel.getAlbumsForPost(post.id).collectAsState(initial = emptyList())
                    PostCard(
                        post = post,
                        albumImages = albums.map { it.albumImageUri },
                        onPostClick = { onNavigateToPostDetail(post.id) },
                        onLikeClick = { if (isLoggedIn) postViewModel.toggleLike(post) else onNavigateToLogin() },
                        onCommentClick = { if (isLoggedIn) onNavigateToPostDetail(post.id) else onNavigateToLogin() },
                        onSaveClick = { if (isLoggedIn) postViewModel.toggleSave(post) else onNavigateToLogin() },
                        onProfileClick = { /* No-op, para evitar clics accidentales */ },
                        isLoggedIn = isLoggedIn
                    )
                }
            }
        }
    }
}
