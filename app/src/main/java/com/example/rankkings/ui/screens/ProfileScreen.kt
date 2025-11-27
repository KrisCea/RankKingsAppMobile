package com.example.rankkings.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.rankkings.ui.components.FullScreenLoadingIndicator
import com.example.rankkings.ui.components.NotLoggedInPlaceholder
import com.example.rankkings.ui.components.PostCard
import com.example.rankkings.ui.components.RankkingsBottomBar
import com.example.rankkings.ui.components.RankkingsTopBar
import com.example.rankkings.ui.theme.Gold
import com.example.rankkings.viewmodel.AuthViewModel
import com.example.rankkings.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    userId: String?,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToPostDetail: (Int) -> Unit,
    onNavigateToSaved: () -> Unit,
    onNavigateToInterests: () -> Unit,
    postViewModel: PostViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val isMyProfile = userId == null || userId == currentUser?.id.toString()
    val userToShowId = if (isMyProfile) currentUser?.id.toString() else userId
    val isLoggedIn = currentUser != null

    val userToShow by if (userToShowId != null) {
        authViewModel.getUserById(userToShowId).collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }

    val userPosts by postViewModel.userPosts.collectAsState()

    LaunchedEffect(userToShow) {
        userToShow?.let {
            postViewModel.loadUserPosts(it.id)
        }
    }

    Scaffold(
        topBar = {
            RankkingsTopBar(title = if (isMyProfile) "Mi Perfil" else userToShow?.username ?: "Perfil")
        },
        bottomBar = {
            RankkingsBottomBar(
                selectedRoute = "profile",
                onNavigate = {
                    when (it) {
                        "home" -> onNavigateToHome()
                        "saved" -> onNavigateToSaved()
                    }
                },
                isLoggedIn = isLoggedIn,
                onNavigateToLogin = onNavigateToLogin
            )
        }
    ) { paddingValues ->
        if (!isLoggedIn && isMyProfile) {
            NotLoggedInPlaceholder(onNavigateToLogin = onNavigateToLogin)
        } else if (userToShow == null) {
            FullScreenLoadingIndicator()
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                item {
                    userToShow?.let { user ->
                        ProfileHeader(
                            user = user,
                            postCount = userPosts.size,
                            isMyProfile = isMyProfile,
                            onLogout = {
                                authViewModel.logout()
                                onNavigateToHome()
                            },
                            onEditInterests = onNavigateToInterests,
                            onProfileImageChange = { uri ->
                                authViewModel.updateProfileImage(user.id, uri.toString())
                            }
                        )
                    }
                }

                item { Divider(modifier = Modifier.padding(vertical = 16.dp)) }

                item {
                    Text(
                        text = if (isMyProfile) "Mis Rankings" else "Rankings de ${userToShow?.username}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                if (userPosts.isEmpty()) {
                    item {
                        Text(
                            text = if (isMyProfile) "Aún no has creado ningún ranking."
                            else "Este usuario aún no ha creado ningún ranking.",
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    items(userPosts, key = { it.id }) { post ->
                        val albums by postViewModel.getAlbumsForPost(post.id).collectAsState(initial = emptyList())
                        PostCard(
                            post = post,
                            albumImages = albums.map { it.albumImageUri },
                            onPostClick = { onNavigateToPostDetail(post.id) },
                            onLikeClick = { if(isLoggedIn) postViewModel.toggleLike(post) else onNavigateToLogin() },
                            onCommentClick = { if(isLoggedIn) onNavigateToPostDetail(post.id) else onNavigateToLogin() },
                            onSaveClick = { if(isLoggedIn) postViewModel.toggleSave(post) else onNavigateToLogin() },
                            onProfileClick = { /* No hacer nada si ya estás en el perfil */ },
                            isLoggedIn = isLoggedIn
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class) // <-- AÑADIDO
@Composable
fun ProfileHeader(
    user: User,
    postCount: Int,
    isMyProfile: Boolean,
    onLogout: () -> Unit,
    onEditInterests: () -> Unit,
    onProfileImageChange: (Uri) -> Unit
) {
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { onProfileImageChange(it) }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            val painter = rememberAsyncImagePainter(
                model = user.profileImageUri ?: R.drawable.ic_launcher_background
            )
            Image(
                painter = painter,
                contentDescription = "Foto de perfil",
                modifier = Modifier.size(120.dp).clip(CircleShape).clickable(enabled = isMyProfile) { galleryLauncher.launch("image/*") },
                contentScale = ContentScale.Crop
            )
            if (isMyProfile) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Cambiar foto",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = user.username, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = user.email, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (user.interests.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                user.interests.forEach { interest ->
                    Chip(label = interest)
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(label = "Rankings", value = postCount.toString())
        }

        if (isMyProfile) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onEditInterests, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Intereses")
                }
                OutlinedButton(onClick = onLogout, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Salir")
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun Chip(label: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
