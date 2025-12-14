package com.example.rankkings.ui.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun WelcomeDialog(
    userName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("¡Entendido!")
            }
        },
        title = {
            Text("¡Bienvenido a Rankkings!")
        },
        text = {
            Text(
                """
Hola $userName 👋

Rankkings es una app donde puedes:
• Crear rankings personalizados
• Compartir tus listados
• Guardar rankings favoritos
• Explorar contenido de la comunidad

¡Disfruta la experiencia!
                """.trimIndent()
            )
        }
    )
}
