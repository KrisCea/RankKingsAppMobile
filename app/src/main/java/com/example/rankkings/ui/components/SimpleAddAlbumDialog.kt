package com.example.rankkings.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.rankkings.model.Album
import com.example.rankkings.ui.theme.DarkSurface
import com.example.rankkings.ui.theme.Gold
import com.example.rankkings.ui.theme.TextSecondary

@Composable
fun SimpleAddAlbumDialog(
    onDismiss: () -> Unit,
    onAlbumAdded: (Album) -> Unit
) {
    var albumName by remember { mutableStateOf("") }
    var artistName by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri?.toString() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar álbum") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // --- PREVIEW IMAGEN ---
                Surface(
                    onClick = { imagePicker.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurface,
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Agregar imagen (opcional)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }


                OutlinedTextField(
                    value = albumName,
                    onValueChange = { albumName = it },
                    label = { Text("Nombre del álbum") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = artistName,
                    onValueChange = { artistName = it },
                    label = { Text("Artista") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = albumName.isNotBlank() && artistName.isNotBlank(),
                onClick = {
                    onAlbumAdded(
                        Album(
                            postId = -1,
                            albumName = albumName,
                            artistName = artistName,
                            albumImageUri = imageUri ?: "",
                            ranking = 1
                        )
                    )
                }
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
