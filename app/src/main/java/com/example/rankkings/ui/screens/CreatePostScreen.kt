package com.example.rankkings.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rankkings.model.Album
import com.example.rankkings.model.Post
import com.example.rankkings.ui.components.AlbumRankingItem
import com.example.rankkings.ui.components.SimpleAddAlbumDialog
import com.example.rankkings.viewmodel.AuthViewModel
import com.example.rankkings.viewmodel.PostViewModel
import com.example.rankkings.viewmodel.CreatePostState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onPostCreated: () -> Unit,
    postViewModel: PostViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var albums by remember { mutableStateOf<List<Album>>(emptyList()) }
    var isPrivate by remember { mutableStateOf(false) }
    var showAddAlbumDialog by remember { mutableStateOf(false) }

    val currentUser by authViewModel.currentUser.collectAsState()
    val createState by postViewModel.createPostState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Ranking") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(
                        enabled = title.isNotBlank()
                                && albums.size >= 2
                                && currentUser != null
                                && createState !is CreatePostState.Loading,
                        onClick = {
                            val user = currentUser ?: return@TextButton

                            val post = Post(
                                userId = user.id,
                                name = user.name,
                                title = title,
                                description = description,
                                isPrivate = isPrivate
                            )

                            postViewModel.createPostWithAlbums(post, albums)
                        }
                    ) {
                        if (createState is CreatePostState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Publicar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título del Ranking") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Álbumes del Ranking (${albums.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = { showAddAlbumDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir Álbum")
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (albums.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(albums) { album ->
                            AlbumRankingItem(
                                album = album,
                                onDelete = {
                                    albums = albums
                                        .filter { it != album }
                                        .mapIndexed { index, a ->
                                            a.copy(ranking = index + 1)
                                        }
                                }
                            )
                        }
                    }
                } else {
                    Text(
                        "Añade al menos 2 álbumes para publicar.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(24.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPrivate) "Post privado 🔒" else "Post público 🌍",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = isPrivate,
                    onCheckedChange = { isPrivate = it }
                )
            }
        }

        if (showAddAlbumDialog) {
            SimpleAddAlbumDialog(
                onAlbumAdded = {
                    val newAlbum = it.copy(ranking = albums.size + 1)
                    albums = albums + newAlbum
                    showAddAlbumDialog = false
                },
                onDismiss = { showAddAlbumDialog = false }
            )
        }
    }

    // ✅ NAVEGAR SOLO UNA VEZ Y LIMPIAR ESTADO
    LaunchedEffect(createState) {
        if (createState is CreatePostState.Success) {
            postViewModel.resetCreatePostState()
            onPostCreated()
        }
    }
}

