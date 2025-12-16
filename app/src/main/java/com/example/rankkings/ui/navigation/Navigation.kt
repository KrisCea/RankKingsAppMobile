package com.example.rankkings.ui.navigation

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
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.rankkings.ui.screens.*
import com.example.rankkings.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object CreatePost : Screen("create_post")
    object Profile : Screen("profile")
    object PostDetail : Screen("post_detail/{postId}") {
        fun createRoute(postId: Int) = "post_detail/$postId"
    }
    object Saved : Screen("saved")
    object Interests : Screen("interests")
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoggedIn = currentUser != null
    val startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToHome = {
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

        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                authViewModel = authViewModel,
                onNavigateToCreatePost = { navController.navigate(Screen.CreatePost.route) },
                onNavigateToPostDetail = { navController.navigate(Screen.PostDetail.createRoute(it)) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToSaved = { navController.navigate(Screen.Saved.route) },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.CreatePost.route) {
            if (!isLoggedIn) {
                LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) }
            } else {
                CreatePostScreen(
                    authViewModel = authViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onPostCreated = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToPostDetail = { navController.navigate(Screen.PostDetail.createRoute(it)) },
                onNavigateToSaved = { navController.navigate(Screen.Saved.route) },
                onNavigateToInterests = { navController.navigate(Screen.Interests.route) },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.Saved.route) {
            SavedPostsScreen(
                authViewModel = authViewModel,
                onNavigateToPostDetail = { navController.navigate(Screen.PostDetail.createRoute(it)) },
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.Interests.route) {
            InterestsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PostDetail.route,
            arguments = listOf(navArgument("postId") { type = NavType.IntType })
        ) {
            val postId = it.arguments?.getInt("postId") ?: 0
            PostDetailScreen(
                postId = postId,
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }
    }
}
