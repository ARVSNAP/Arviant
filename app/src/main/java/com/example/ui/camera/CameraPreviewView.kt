package com.example.ui.camera

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.model.CameraSource
import com.example.util.BitmapUtils
import java.util.concurrent.Executors

import com.example.model.ColorFilterType

@Composable
fun CameraPreviewView(
    selectedCameraSource: CameraSource,
    isTorchOn: Boolean,
    exposureIndex: Int = 0,
    filterType: ColorFilterType = ColorFilterType.NORMAL,
    brightness: Float = 0f,
    contrast: Float = 1f,
    skinSmoothing: Float = 0f,
    onPhotoCaptured: (Bitmap) -> Unit,
    bindCaptureTrigger: ((() -> Unit) -> Unit),
    modifier: Modifier = Modifier
) {
    CameraPreview(
        modifier = modifier,
        cameraSource = selectedCameraSource,
        isTorchOn = isTorchOn,
        exposureIndex = exposureIndex,
        filterType = filterType,
        brightness = brightness,
        contrast = contrast,
        skinSmoothing = skinSmoothing,
        onPhotoCaptured = onPhotoCaptured,
        bindCaptureTrigger = bindCaptureTrigger
    )
}
