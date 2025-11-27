package com.example.rankkings.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rankkings.model.Album
import com.example.rankkings.ui.components.AlbumRankingItem
import com.example.rankkings.viewmodel.AuthViewModel
import com.example.rankkings.viewmodel.PostUiState
import com.example.rankkings.viewmodel.PostViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    onPostCreated: () -> Unit,
    postViewModel: PostViewModel = hiltViewModel(), // Inyectar PostViewModel con Hilt
    authViewModel: AuthViewModel = hiltViewModel() // Inyectar AuthViewModel con Hilt
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var albums by remember { mutableStateOf<List<Album>>(emptyList()) }
    var showAddAlbumDialog by remember { mutableStateOf(false) }
    var currentAlbumIndex by remember { mutableStateOf(1) }

    val uiState by postViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState() // Obtener currentUser aquí

    LaunchedEffect(uiState) {
        if (uiState is PostUiState.Success) {
            postViewModel.resetUiState()
            onPostCreated()
        }
    }

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
                        onClick = {
                            currentUser?.let { user ->
                                postViewModel.createPost(user.id, user.username, title, description, albums)
                            }
                        },
                        enabled = title.isNotBlank() && albums.size >= 2 && uiState !is PostUiState.Loading && currentUser != null
                    ) {
                        Text(
                            "Publicar",
                            fontWeight = FontWeight.Bold,
                            color = if (title.isNotBlank() && albums.size >= 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título del Ranking") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción (opcional)") }, modifier = Modifier.fillMaxWidth().height(120.dp))
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Álbumes (${albums.size})", style = MaterialTheme.typography.titleLarge)
                Button(onClick = { showAddAlbumDialog = true; currentAlbumIndex = albums.size + 1 }) {
                    Text("Agregar Álbum")
                }
            }
            Spacer(Modifier.height(16.dp))
            albums.forEach { album ->
                AlbumRankingItem(album, onDelete = {
                    albums = albums.filter { a -> a.ranking != album.ranking }.mapIndexed { i, a -> a.copy(ranking = i + 1) }
                })
            }
        }
    }

    if (showAddAlbumDialog) {
        AddAlbumDialog(
            albumNumber = currentAlbumIndex,
            onDismiss = { showAddAlbumDialog = false },
            onAlbumAdded = { album ->
                albums = (albums + album).sortedBy { it.ranking }
                showAddAlbumDialog = false
            }
        )
    }
}

@Composable
private fun AddAlbumDialog(
    albumNumber: Int,
    onDismiss: () -> Unit,
    onAlbumAdded: (Album) -> Unit
) {
    val context = LocalContext.current
    var albumName by remember { mutableStateOf("") }
    var artistName by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    fun getTmpFileUri(context: Context): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tmpFile)
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) { /* La URI ya está en imageUri */ }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            imageUri = it
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val newUri = getTmpFileUri(context)
            imageUri = newUri
            cameraLauncher.launch(newUri)
        }
    }

    val galleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
    val galleryPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Álbum #$albumNumber") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = albumName, onValueChange = { albumName = it }, label = { Text("Nombre del álbum") })
                OutlinedTextField(value = artistName, onValueChange = { artistName = it }, label = { Text("Artista") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            val newUri = getTmpFileUri(context)
                            imageUri = newUri
                            cameraLauncher.launch(newUri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }) { Text("Cámara") }
                    Button(onClick = {
                        if (ContextCompat.checkSelfPermission(context, galleryPermission) == PackageManager.PERMISSION_GRANTED) {
                            galleryLauncher.launch("image/*")
                        } else {
                            galleryPermissionLauncher.launch(galleryPermission)
                        }
                    }) { Text("Galería") }
                }
                if(imageUri != null) Text("✓ Imagen seleccionada", color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    imageUri?.let { uri ->
                        onAlbumAdded(
                            Album(
                                postId = 0,
                                albumImageUri = uri.toString(),
                                albumName = albumName,
                                artistName = artistName,
                                ranking = albumNumber
                            )
                        )
                    } 
                },
                enabled = albumName.isNotBlank() && artistName.isNotBlank() && imageUri != null
            ) { Text("Agregar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
