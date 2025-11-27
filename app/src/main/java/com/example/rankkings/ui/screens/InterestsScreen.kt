package com.example.rankkings.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rankkings.ui.components.FullScreenLoadingIndicator
import com.example.rankkings.ui.theme.Gold
import com.example.rankkings.viewmodel.AuthViewModel
import com.example.rankkings.viewmodel.AuthState

val musicGenres = listOf(
    "Rock", "Pop", "Hip Hop", "Jazz", "Electronic", "R&B", "Indie", "Metal", "Classical", "Reggae"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterestsScreen(
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val userInterests = currentUser?.interests ?: emptyList()
    var selectedInterests by remember { mutableStateOf(userInterests) }
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState, userInterests) {
        if (selectedInterests.isEmpty()) { // Ensure initial state is set
            selectedInterests = userInterests
        }
        if (authState is AuthState.Success) {
            onNavigateBack()
            authViewModel.resetAuthState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Intereses Musicales") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Gold,
                    navigationIconContentColor = Gold
                )
            )
        }
    ) { paddingValues ->
        val user = currentUser
        if (user == null) {
            FullScreenLoadingIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Selecciona tus géneros favoritos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Gold
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(musicGenres) { genre ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    selectedInterests = if (selectedInterests.contains(genre)) {
                                        selectedInterests - genre
                                    } else {
                                        selectedInterests + genre
                                    }
                                 }
                                .padding(vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = selectedInterests.contains(genre),
                                onCheckedChange = { isChecked ->
                                    selectedInterests = if (isChecked) {
                                        selectedInterests + genre 
                                    } else {
                                        selectedInterests - genre
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Gold)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = genre, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { authViewModel.updateUserInterests(user.id, selectedInterests) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    enabled = authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
