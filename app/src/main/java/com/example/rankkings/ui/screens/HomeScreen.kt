package com.example.rankkings.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rankkings.model.Post
import com.example.rankkings.ui.components.PostCard
import com.example.rankkings.ui.components.RankkingsBottomBar
import com.example.rankkings.ui.components.RankkingsTopBar
import com.example.rankkings.viewmodel.AuthViewModel
import com.example.rankkings.viewmodel.PostViewModel
import com.example.rankkings.ui.viewmodel.UserViewModel
import com.example.rankkings.model.UserDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreatePost: () -> Unit,
    onNavigateToPostDetail: (Int) -> Unit,
    onNavigateToProfile: (String?) -> Unit,
    onNavigateToSaved: () -> Unit,
    onNavigateToLogin: () -> Unit,
    postViewModel: PostViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel() // Inyectar UserViewModel
) {
    val posts by postViewModel.posts.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoggedIn = currentUser != null

    // Obtener la lista de usuarios de Xano
    val users by userViewModel.users.collectAsState()
    val errorMessage by userViewModel.errorMessage.collectAsState()

    // Llamar a la API cuando el Composable se lanza por primera vez
    LaunchedEffect(Unit) {
        userViewModel.getAllUsers()
    }

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
        Column(modifier = Modifier.padding(paddingValues)) {
            if (posts.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), Alignment.Center) {
                    Text("No hay posts aún. ¡Sé el primero!", textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(posts, key = { it.id }) { post ->
                        PostCardWithAlbums(
                            post = post,
                            postViewModel = postViewModel,
                            authViewModel = authViewModel,
                            onNavigateToPostDetail = onNavigateToPostDetail,
                            onNavigateToLogin = onNavigateToLogin,
                            onNavigateToProfile = onNavigateToProfile
                        )
                    }
                }
            }

            // --- Sección para mostrar usuarios de Xano ---
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Usuarios de Xano:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))

            if (errorMessage != null) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }

            if (users.isEmpty() && errorMessage == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(users) { user ->
                        Text("ID: ${user.id}, Nombre: ${user.name}, Email: ${user.email}")
                    }
                }
            }
        }
    }
}

@Composable
private fun PostCardWithAlbums(
    post: Post,
    postViewModel: PostViewModel,
    authViewModel: AuthViewModel,

    onNavigateToPostDetail: (Int) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToProfile: (String?) -> Unit
) {
    val albums by postViewModel.getAlbumsForPost(post.id).collectAsState(initial = emptyList())
    val currentUser by authViewModel.currentUser.collectAsState()
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
