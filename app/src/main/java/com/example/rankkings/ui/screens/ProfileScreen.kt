package com.example.rankkings.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.rankkings.R
import com.example.rankkings.model.User
import com.example.rankkings.ui.components.NotLoggedInPlaceholder
import com.example.rankkings.ui.components.PostCard
import com.example.rankkings.ui.components.RankkingsBottomBar
import com.example.rankkings.ui.components.RankkingsTopBar
import com.example.rankkings.viewmodel.AuthViewModel
import com.example.rankkings.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToPostDetail: (Int) -> Unit,
    onNavigateToSaved: () -> Unit,
    onNavigateToInterests: () -> Unit,
    postViewModel: PostViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {

    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    val isLoggedIn = currentUser != null

    if (!isLoggedIn) {
        NotLoggedInPlaceholder(onNavigateToLogin)
        return
    }

    val user = currentUser!!

    // ✅ USO CORRECTO DEL VIEWMODEL
    val userPosts by postViewModel
        .loadUserPosts(user.id)
        .collectAsState(initial = emptyList())

    Scaffold(
        topBar = { RankkingsTopBar(title = "Mi Perfil") },
        bottomBar = {
            RankkingsBottomBar(
                selectedRoute = "profile",
                onNavigate = {
                    when (it) {
                        "home" -> onNavigateToHome()
                        "saved" -> onNavigateToSaved()
                    }
                },
                isLoggedIn = true,
                onNavigateToLogin = onNavigateToLogin
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            item {
                ProfileHeader(
                    user = user,
                    postCount = userPosts.size,
                    onLogout = {
                        authViewModel.logout()
                        onNavigateToHome()
                    },
                    onEditInterests = onNavigateToInterests
                )
            }

            item {
                Divider(modifier = Modifier.padding(vertical = 16.dp))
            }

            item {
                Text(
                    text = "Mis Rankings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (userPosts.isEmpty()) {
                item {
                    Text(
                        text = "Aún no has creado ningún ranking.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(userPosts, key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        albumImages = emptyList(), // ⛔ álbumes no cargados aquí
                        onPostClick = { onNavigateToPostDetail(post.id) },
                        onLikeClick = { postViewModel.toggleLike(post) },
                        onCommentClick = { onNavigateToPostDetail(post.id) },
                        onSaveClick = { postViewModel.toggleSave(post) },
                        onProfileClick = {},
                        isLoggedIn = true
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    user: User,
    postCount: Int,
    onLogout: () -> Unit,
    onEditInterests: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = rememberAsyncImagePainter(
                model = user.profileImageUri ?: R.drawable.ic_launcher_background
            ),
            contentDescription = "Perfil",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(user.name, style = MaterialTheme.typography.headlineSmall)
        Text(user.email)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Rankings: $postCount")

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onEditInterests) {
                Icon(Icons.Default.MusicNote, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Intereses")
            }

            OutlinedButton(onClick = onLogout) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Salir")
            }
        }
    }
}
