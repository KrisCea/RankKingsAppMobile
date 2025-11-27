package com.example.rankkings.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.* // Importación para remember y collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel // Importar hiltViewModel
import com.example.rankkings.model.Post
import com.example.rankkings.model.User
import com.example.rankkings.ui.components.PostCard
import com.example.rankkings.ui.components.RankkingsBottomBar
import com.example.rankkings.ui.components.RankkingsTopBar
import com.example.rankkings.viewmodel.AuthViewModel // Importar AuthViewModel
import com.example.rankkings.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreatePost: () -> Unit,
    onNavigateToPostDetail: (Int) -> Unit,
    onNavigateToProfile: (String?) -> Unit,
    onNavigateToSaved: () -> Unit,
    onNavigateToLogin: () -> Unit,
    postViewModel: PostViewModel = hiltViewModel(), // Inyectar PostViewModel con Hilt
    authViewModel: AuthViewModel = hiltViewModel() // Inyectar AuthViewModel con Hilt
) {
    val posts by postViewModel.posts.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState() // Obtener currentUser internamente
    val isLoggedIn = currentUser != null

    Scaffold(
        topBar = { RankkingsTopBar(title = "Rankkings") },
        bottomBar = {
            RankkingsBottomBar(
                selectedRoute = "home",
                onNavigate = { route ->
                    when (route) {
                        "profile" -> onNavigateToProfile(currentUser?.id?.toString())
                        "saved" -> onNavigateToSaved()
                        "home" -> { /* Ya estamos en home */ }
                    }
                },
                isLoggedIn = isLoggedIn,
                onNavigateToLogin = onNavigateToLogin
            )
        },
        floatingActionButton = {
            if (isLoggedIn) {
                FloatingActionButton(onClick = onNavigateToCreatePost) {
                    Icon(Icons.Default.Add, contentDescription = "Crear post")
                }
            }
        }
    ) { paddingValues ->
        if (posts.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) {
                Text("No hay posts aún. ¡Sé el primero!", textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(posts, key = { it.id }) { post ->
                    PostCardWithAlbums(
                        post = post,
                        postViewModel = postViewModel,
                        authViewModel = authViewModel, // Pasar AuthViewModel
                        onNavigateToPostDetail = onNavigateToPostDetail,
                        onNavigateToLogin = onNavigateToLogin,
                        onNavigateToProfile = onNavigateToProfile
                    )
                }
            }
        }
    }
}

@Composable
private fun PostCardWithAlbums(
    post: Post,
    postViewModel: PostViewModel,
    authViewModel: AuthViewModel, // Añadir AuthViewModel aquí
    onNavigateToPostDetail: (Int) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToProfile: (String?) -> Unit
) {
    val albums by postViewModel.getAlbumsForPost(post.id).collectAsState(initial = emptyList())
    val currentUser by authViewModel.currentUser.collectAsState() // Obtener currentUser aquí
    val isLoggedIn = currentUser != null

    PostCard(
        post = post,
        albumImages = albums.map { it.albumImageUri },
        onPostClick = { onNavigateToPostDetail(post.id) },
        onLikeClick = {
            if (isLoggedIn) postViewModel.toggleLike(post) else onNavigateToLogin()
        },
        onCommentClick = { onNavigateToPostDetail(post.id) },
        onSaveClick = {
            if (isLoggedIn) postViewModel.toggleSave(post) else onNavigateToLogin()
        },
        onProfileClick = { onNavigateToProfile(post.userId) },
        isLoggedIn = isLoggedIn
    )
}
