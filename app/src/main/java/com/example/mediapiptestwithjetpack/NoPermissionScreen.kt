package com.example.mediapiptestwithjetpack

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NoPermissionScreen(
    navController: NavController,
    onRequestPermission: () -> Unit
) {
    val cameraPermissionState: PermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    NoPermissionContent(
        cameraPermissionState = cameraPermissionState,
        onRequestPermission = {
            cameraPermissionState.launchPermissionRequest()
        },
        navController = navController
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NoPermissionContent(
    cameraPermissionState: PermissionState,
    onRequestPermission: () -> Unit,
    navController: NavController
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!cameraPermissionState.status.isGranted) {
            Text(text = "Please grant the permission to use the camera to use the core functionality of this app.")
            Button(onClick = onRequestPermission) {
                Text(text = "Grant permission")
            }
        } else {
            Button(onClick = { navController.navigate(Screen.StartScreen.route) }) {
                Text(text = "Back")
            }
        }
    }
}

//cameraController.cameraSelector = cameraSelector
////                        cameraController.bindToLifecycle(lifecycleOwner)
//
//cameraProvider?.unbindAll()
//
//camera = cameraProvider?.bindToLifecycle(
//lifecycleOwner, cameraSelector, preview, imageAnalyzer
//)
//
//previewView.controller = cameraController
////                        cameraProvider?.let { provider ->
////                            camera = provider.bindToLifecycle(
////                                lifecycleOwner, cameraSelector, preview
////                            )
////                            provider.bindToLifecycle(
////                                lifecycleOwner, cameraSelector, imageAnalyzer
////                            )
////                        }