package com.example.rankkings.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.rankkings.ui.theme.Gold

/**
 * Barra de navegación inferior con acceso rápido
 */
@Composable
fun RankkingsBottomBar(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    isLoggedIn: Boolean = false,
    onNavigateToLogin: () -> Unit // Nuevo parámetro para la navegación al login
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        // Home
        NavigationBarItem(
            selected = selectedRoute == "home",
            onClick = { onNavigate("home") },
            icon = {
                Icon(
                    imageVector = if (selectedRoute == "home") Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Inicio"
                )
            },
            label = { Text("Inicio") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Gold,
                selectedTextColor = Gold,
                indicatorColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        // Guardados
        NavigationBarItem(
            selected = selectedRoute == "saved",
            onClick = { if (isLoggedIn) onNavigate("saved") else onNavigateToLogin() }, // Redirigir si no está logueado
            icon = {
                Icon(
                    imageVector = if (selectedRoute == "saved") Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Guardados"
                )
            },
            label = { Text("Guardados") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Gold,
                selectedTextColor = Gold,
                indicatorColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        // Perfil
        NavigationBarItem(
            selected = selectedRoute == "profile",
            onClick = { if (isLoggedIn) onNavigate("profile") else onNavigateToLogin() }, // Redirigir si no está logueado
            icon = {
                Icon(
                    imageVector = if (selectedRoute == "profile") Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Perfil"
                )
            },
            label = { Text("Perfil") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Gold,
                selectedTextColor = Gold,
                indicatorColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
