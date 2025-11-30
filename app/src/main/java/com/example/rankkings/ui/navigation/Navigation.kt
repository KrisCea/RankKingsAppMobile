package com.example.rankkings.ui.navigation

import android.util.Log // Importar Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.rankkings.ui.screens.*
import com.example.rankkings.viewmodel.AuthViewModel

/**
 * Rutas de navegación de la aplicación
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object CreatePost : Screen("create_post")
    object Profile : Screen("profile?userId={userId}") {
        fun createRoute(userId: String?) = "profile?userId=$userId"
    }
    object PostDetail : Screen("post_detail/{postId}") {
        fun createRoute(postId: Int) = "post_detail/$postId"
    }
    object Saved : Screen("saved")
    object Interests : Screen("interests")
}

/**
 * Configuración de navegación de la aplicación
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoggedIn = currentUser != null

    // Determinar la ruta de inicio basada en el estado de autenticación
    val initialStartDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route

    Log.d("AppNavigation", "Composable Recomposed. CurrentUser: ${currentUser?.name}, IsLoggedIn: $isLoggedIn, InitialDest: $initialStartDestination")

    // LaunchedEffect para navegar después del login o al restaurar la sesión
    LaunchedEffect(currentUser) {
        Log.d("AppNavigation", "LaunchedEffect triggered. CurrentUser: ${currentUser?.name}, IsLoggedIn: ${currentUser != null}")

        if (currentUser != null) {
            Log.d("AppNavigation", "Navigating to Home after login/session restore. User: ${currentUser?.name}")
            // Si el usuario se loguea o ya está logueado al iniciar, ir a Home
            navController.navigate(Screen.Home.route) {
                // Limpiar la pila de atrás para que no se pueda volver a Login/Register
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        } else {
            Log.d("AppNavigation", "No current user. Ensuring navigation to Login.")
            // Si no hay usuario, asegurar que estamos en la pantalla de Login
            // Esto es importante si el usuario cierra sesión y queremos ir al login
            if (navController.currentDestination?.route != Screen.Login.route && navController.currentDestination?.route != Screen.Register.route) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = initialStartDestination,
        modifier = modifier
    ) {
        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToHome = { // <-- Aquí la lambda debe realizar la navegación
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onSkipLogin = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Register Screen
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToHome = { // <-- Aquí la lambda debe realizar la navegación
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Home Screen
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCreatePost = {
                    navController.navigate(Screen.CreatePost.route)
                },
                onNavigateToPostDetail = { postId ->
                    navController.navigate(Screen.PostDetail.createRoute(postId))
                },
                onNavigateToProfile = { userId ->
                    navController.navigate(Screen.Profile.createRoute(userId))
                },
                onNavigateToSaved = {
                    navController.navigate(Screen.Saved.route)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        // Create Post Screen
        composable(Screen.CreatePost.route) {
            // Esta comprobación ahora está redundante si el LaunchedEffect superior funciona correctamente
            // pero la mantenemos como fallback o si el usuario no tiene sesion iniciada y salta el login
            if (currentUser == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.CreatePost.route) { inclusive = true }
                    }
                }
            } else {
                CreatePostScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onPostCreated = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // Profile Screen
        composable(
            route = Screen.Profile.route,
            arguments = listOf(navArgument("userId") { nullable = true })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            ProfileScreen(
                userId = userId,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route)
                },
                onNavigateToPostDetail = { postId ->
                    navController.navigate(Screen.PostDetail.createRoute(postId))
                },
                onNavigateToSaved = {
                    navController.navigate(Screen.Saved.route)
                },
                onNavigateToInterests = {
                    navController.navigate(Screen.Interests.route)
                }
            )
        }

        // Post Detail Screen
        composable(
            route = Screen.PostDetail.route,
            arguments = listOf(
                navArgument("postId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getInt("postId") ?: 0
            PostDetailScreen(
                postId = postId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        // Saved Posts Screen (Posts guardados)
        composable(Screen.Saved.route) {
            val user = currentUser
            if (user == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Saved.route) { inclusive = true }
                    }
                }
            } else {
                SavedPostsScreen(
                    onNavigateToPostDetail = { postId ->
                        navController.navigate(Screen.PostDetail.createRoute(postId))
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.createRoute(user.id.toString()))
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route)
                    }
                )
            }
        }

        // Interests Screen
        composable(Screen.Interests.route) {
            val user = currentUser
            if (user == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Interests.route) { inclusive = true }
                    }
                }
            } else {
                InterestsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
