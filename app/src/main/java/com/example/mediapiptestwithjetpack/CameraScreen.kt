package com.example.mediapiptestwithjetpack

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.core.*
import android.graphics.Color
import android.graphics.ImageFormat
import android.util.Log
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.test.annotation.ExperimentalTestApi
import androidx.test.runner.permission.PermissionRequester
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SuppressLint("PermissionLaunchedDuringComposition")
@androidx.annotation.OptIn(ExperimentalTestApi::class) @OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    navController: NavController
){
    val context = LocalContext.current
    val gestureResultState = remember { mutableStateOf<GestureRecognizerHelper.ResultBundle?>(null) }
    val cameraPermissionState: PermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    val gestureRecognizerHelper = remember {
        GestureRecognizerHelper(
            context = context,
            runningMode = RunningMode.LIVE_STREAM,
            gestureRecognizerListener = object : GestureRecognizerHelper.GestureRecognizerListener {
                override fun onResults(resultBundle: GestureRecognizerHelper.ResultBundle) {
                    gestureResultState.value=resultBundle
                }

                override fun onError(error: String, errorCode: Int) {
                    // Log any errors encountered during gesture recognition
                    Log.e("GestureResults", "Error recognizing gestures: $error")
                }
            }
        )
    }

    if (!cameraPermissionState.status.isGranted) {
        cameraPermissionState.launchPermissionRequest()
    }

    CameraSetup(navController = navController,
        gestureRecognizerHelper = gestureRecognizerHelper,
        gestureResultState)
}




@SuppressLint("RestrictedApi")
@Composable
fun CameraSetup(navController: NavController,gestureRecognizerHelper:GestureRecognizerHelper,
                gestureResultState:MutableState<GestureRecognizerHelper.ResultBundle?>) {

    lateinit var backgroundExecutor: ExecutorService
    backgroundExecutor=Executors.newSingleThreadExecutor()

    val context: Context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val cameraController: LifecycleCameraController = remember { LifecycleCameraController(context) }




// Set the target rotation and backpressure strategy if needed
    cameraController.setImageAnalysisBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)

// Set the analyzer for the camera controller
    cameraController.setImageAnalysisAnalyzer(backgroundExecutor) { imageProxy ->
        try {

                // Perform the analysis on the image
                recognizeHand(gestureRecognizerHelper, imageProxy)

        } finally {
            // Make sure to close the image to prevent memory leaks and ensure the next image is received
            imageProxy.close()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraController.unbind()
            backgroundExecutor.shutdownNow()
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
                    PreviewView(context).also { previewView ->
                        previewView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        previewView.setBackgroundColor(Color.BLACK)
                        previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        previewView.scaleType = PreviewView.ScaleType.FILL_START
                        previewView.controller = cameraController // This line is correct and needed

                        // Setting up the camera selector for the camera controller
                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                            .build()
                        cameraController.cameraSelector = cameraSelector
                        cameraController.bindToLifecycle(lifecycleOwner)
                    }
                }
            )

            var alphabet=""
            val temp= gestureResultState.value?.results?.get(0)?.gestures()
            if (temp != null) {
                if (!(temp.isEmpty())){
                    val temp2= temp[0][0].categoryName()
                    alphabet=temp2.toString()
                }
            }

            Text(text = "Detected alphabet: ${alphabet}",modifier=Modifier.padding(16.dp).align(Alignment.BottomCenter))

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