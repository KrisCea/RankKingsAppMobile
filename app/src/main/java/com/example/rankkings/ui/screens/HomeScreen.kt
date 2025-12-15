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
import com.example.rankkings.ui.components.TutorialBanner
import com.example.rankkings.viewmodel.AuthViewModel
import com.example.rankkings.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreatePost: () -> Unit,
    onNavigateToPostDetail: (Int) -> Unit,
    onNavigateToProfile: (Int?) -> Unit,
    onNavigateToSaved: () -> Unit,
    onNavigateToLogin: () -> Unit,
    postViewModel: PostViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val posts by postViewModel.posts.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    val isLoggedIn = currentUser != null
    val showTutorial by postViewModel.showTutorial.collectAsState()
    val tutorialText by postViewModel.tutorialText.collectAsState()

    Scaffold(
        topBar = {
            RankkingsTopBar(
                title = if (isLoggedIn)
                    "Rankkings · Bienvenido ${currentUser?.name}"
                else
                    "Rankkings"
            )
        },
        bottomBar = {
            RankkingsBottomBar(
                selectedRoute = "home",
                onNavigate = { route ->
                    when (route) {
                        "home" -> {}
                        "profile" -> onNavigateToProfile(currentUser?.id)
                        "saved" -> onNavigateToSaved()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // TUTORIAL
            if (showTutorial) {
                TutorialBanner(
                    text = tutorialText,
                    onClose = { postViewModel.closeTutorial() }
                )
            }

        }

        if (posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hay posts aún. ¡Sé el primero!",
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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

    }
}

/* -------------------------------------------------------------------------- */
/*                         POST + ÁLBUMES (ITEM)                               */
/* -------------------------------------------------------------------------- */

@Composable
fun PostCardWithAlbums(
    post: Post,
    postViewModel: PostViewModel,
    authViewModel: AuthViewModel,
    onNavigateToPostDetail: (Int) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToProfile: (Int?) -> Unit
) {
    val albums by postViewModel
        .getAlbumsForPost(post.id) // ✅ FUNCIÓN CORRECTA
        .collectAsState(initial = emptyList())

    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    val isLoggedIn = currentUser != null

    PostCard(
        post = post,
        albumImages = albums.map { it.albumImageUri }, // ✅ YA NO DA ERROR
        onPostClick = { onNavigateToPostDetail(post.id) },
        onLikeClick = {
            if (isLoggedIn) postViewModel.toggleLike(post)
            else onNavigateToLogin()
        },
        onCommentClick = { onNavigateToPostDetail(post.id) },
        onSaveClick = {
            if (isLoggedIn) postViewModel.toggleSave(post)
            else onNavigateToLogin()
        },
        onProfileClick = { onNavigateToProfile(post.userId) },
        isLoggedIn = isLoggedIn
    )
}

