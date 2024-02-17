package com.example.mediapiptestwithjetpack

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable


sealed class Screen(val route: String) {
    object StartScreen: Screen(route = "start_screen")
    object CameraScreen: Screen(route = "camera_screen")
    object NoPermissionScreen: Screen(route = "no_permission_screen")
}

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.StartScreen.route
    ){
        composable(route = Screen.StartScreen.route) {
            StartScreen(
                navController = navController
            )
        }

        composable(route = Screen.CameraScreen.route) {
            CameraScreen(
                navController = navController
            )
        }

        composable(route = Screen.NoPermissionScreen.route) {
            NoPermissionScreen(
                navController = navController,
                onRequestPermission = {
                    navController.popBackStack()
                }
            )
        }
    }
}
