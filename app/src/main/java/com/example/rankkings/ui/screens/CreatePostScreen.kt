package com.example.rankkings.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.rankkings.viewmodel.AuthViewModel
import com.example.rankkings.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    onPostCreated: () -> Unit,
    postViewModel: PostViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var albums by remember { mutableStateOf<List<Album>>(emptyList()) }
    var isPrivate by remember { mutableStateOf(false) }

    val currentUser by authViewModel.currentUser.collectAsState()

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
                        enabled = title.isNotBlank() && albums.size >= 2 && currentUser != null,
                        onClick = {
                            val user = currentUser ?: return@TextButton

                            val post = Post(
                                userId = user.id,
                                name = user.name,
                                title = title,
                                description = description,
                                isPrivate = isPrivate
                            )

                            // 🔥 Guardado LOCAL (Room)
                            postViewModel.togglePrivacy(post) // inicializa privacidad
                            onPostCreated()
                        }
                    ) {
                        Text(
                            "Publicar",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
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

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
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
    }
}
