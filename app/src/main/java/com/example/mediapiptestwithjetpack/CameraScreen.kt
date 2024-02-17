package com.example.mediapiptestwithjetpack

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.core.*
import android.graphics.Color
import android.util.Size
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.test.annotation.ExperimentalTestApi
import androidx.test.runner.permission.PermissionRequester
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SuppressLint("PermissionLaunchedDuringComposition")
@androidx.annotation.OptIn(ExperimentalTestApi::class) @OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    navController: NavController
){
    lateinit var gestureRecognizerHelper: GestureRecognizerHelper
    var context = LocalContext.current

    val cameraPermissionState: PermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    if (!cameraPermissionState.status.isGranted) {
        cameraPermissionState.launchPermissionRequest()
    }

    CameraSetup(
        navController = navController
    )
}

@SuppressLint("RestrictedApi")
@Composable
fun CameraSetup(navController: NavController) {
    lateinit var gestureRecognizerHelper: GestureRecognizerHelper
    lateinit var backgroundExecutor: ExecutorService
    backgroundExecutor=Executors.newSingleThreadExecutor()

    var cameraProvider: ProcessCameraProvider? = null
    var camera: Camera? = null
    var imageAnalyzer: ImageAnalysis? = null
    val context: Context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val cameraController: LifecycleCameraController = remember { LifecycleCameraController(context) }

    DisposableEffect(Unit) {
        onDispose {
            cameraController.unbind()
            imageAnalyzer?.clearAnalyzer()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.popBackStack() }
            ) {
                Text(text = "Back")
            }
        }
    ) { paddingValues: PaddingValues ->

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                factory = { context ->
                    PreviewView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        setBackgroundColor(Color.BLACK)
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_START
                    }.also { previewView ->
                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                            .build()

                        val preview = Preview.Builder()
                            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                            .setDefaultResolution(Size(640,480))
                            .build()

                        imageAnalyzer = ImageAnalysis.Builder()
                            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                            .build()
                            .also {
                                it.setAnalyzer(backgroundExecutor) { image ->
                                    recognizeHand(gestureRecognizerHelper, image)
                                }
                            }

                        previewView.controller = cameraController
                        cameraController.cameraSelector = cameraSelector
                        cameraController.bindToLifecycle(lifecycleOwner)

                        // Camera just shows a black screen???
//                        cameraProvider?.let { provider ->
//                            camera = provider.bindToLifecycle(
//                                lifecycleOwner, cameraSelector, preview
//                            )
//                            provider.bindToLifecycle(
//                                lifecycleOwner, cameraSelector, imageAnalyzer
//                            )
//                        }
                    }
                }
            )
        }
    }
}

@Composable
fun GestureResults() {
    // TODO Text box to display gestures.
}

fun recognizeHand(gestureRecognizerHelper: GestureRecognizerHelper, imageProxy: ImageProxy) {
    gestureRecognizerHelper.recognizeLiveStream(
        imageProxy = imageProxy
    )
}