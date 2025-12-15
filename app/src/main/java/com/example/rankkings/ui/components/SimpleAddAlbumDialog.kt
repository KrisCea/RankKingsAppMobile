package com.example.rankkings.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rankkings.model.Album

@Composable
fun SimpleAddAlbumDialog(
    onDismiss: () -> Unit,
    onAlbumAdded: (Album) -> Unit
) {
    var albumName by remember { mutableStateOf("") }
    var artistName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar álbum") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = albumName,
                    onValueChange = { albumName = it },
                    label = { Text("Nombre del álbum") }
                )
                OutlinedTextField(
                    value = artistName,
                    onValueChange = { artistName = it },
                    label = { Text("Artista") }
                )
            }
        },
        confirmButton = {
            Button(
                enabled = albumName.isNotBlank() && artistName.isNotBlank(),
                onClick = {
                    onAlbumAdded(
                        Album(
                            postId = 0,
                            albumName = albumName,
                            artistName = artistName,
                            albumImageUri = "",
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
