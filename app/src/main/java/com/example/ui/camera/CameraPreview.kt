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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.model.CameraSource
import com.example.util.BitmapUtils
import java.util.concurrent.Executors

/**
 * Komponen CameraPreview berbasis CameraX untuk menampilkan tayangan langsung (live feed)
 * dari kamera perangkat (Depan atau Belakang).
 *
 * Mendukung live preview, grid 3x3 overlay, kontrol lampu kilat/torch, pengambilan foto resolusi tinggi,
 * dan manajemen siklus hidup kamera secara otomatis.
 */
import com.example.model.ColorFilterType

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraSource: CameraSource = CameraSource.FRONT,
    isTorchOn: Boolean = false,
    exposureIndex: Int = 0,
    filterType: ColorFilterType = ColorFilterType.NORMAL,
    brightness: Float = 0f,
    contrast: Float = 1f,
    skinSmoothing: Float = 0f,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    implementationMode: PreviewView.ImplementationMode = PreviewView.ImplementationMode.COMPATIBLE,
    showGrid: Boolean = false,
    onPhotoCaptured: ((Bitmap) -> Unit)? = null,
    bindCaptureTrigger: (((() -> Unit) -> Unit))? = null,
    onCameraReady: ((Camera) -> Unit)? = null,
    onError: ((Exception) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var camera: Camera? by remember { mutableStateOf(null) }

    val isFrontCamera = cameraSource == CameraSource.FRONT

    val previewView = remember {
        PreviewView(context).apply {
            this.implementationMode = implementationMode
            this.scaleType = scaleType
        }
    }

    // Capture lambda
    val takePhoto = remember(isFrontCamera, imageCapture, filterType, brightness, contrast, skinSmoothing) {
        {
            val capture = imageCapture
            if (capture != null && onPhotoCaptured != null) {
                capture.takePicture(
                    cameraExecutor,
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(imageProxy: ImageProxy) {
                            try {
                                val rawBitmap = imageProxy.toBitmap()
                                val processed = BitmapUtils.processCapturedImage(
                                    originalBitmap = rawBitmap,
                                    rotationDegrees = imageProxy.imageInfo.rotationDegrees,
                                    isFrontCamera = isFrontCamera
                                )
                                val filtered = BitmapUtils.applyRealtimeFilter(
                                    source = processed,
                                    filterType = filterType,
                                    brightness = brightness,
                                    contrast = contrast,
                                    skinSmoothing = skinSmoothing
                                )
                                if (filtered != processed) {
                                    processed.recycle()
                                }
                                ContextCompat.getMainExecutor(context).execute {
                                    onPhotoCaptured(filtered)
                                }
                            } catch (e: Exception) {
                                Log.e("CameraPreview", "Error processing bitmap", e)
                                onError?.invoke(e)
                            } finally {
                                imageProxy.close()
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("CameraPreview", "Photo capture failed: ${exception.message}", exception)
                            onError?.invoke(exception)
                        }
                    }
                )
            }
        }
    }

    LaunchedEffect(takePhoto) {
        bindCaptureTrigger?.invoke(takePhoto)
    }

    LaunchedEffect(isTorchOn, camera) {
        try {
            camera?.cameraControl?.enableTorch(isTorchOn)
        } catch (e: Exception) {
            Log.e("CameraPreview", "Torch control failed: ${e.message}")
        }
    }

    LaunchedEffect(exposureIndex, camera) {
        try {
            val exposureState = camera?.cameraInfo?.exposureState
            if (exposureState != null && exposureState.isExposureCompensationSupported) {
                val range = exposureState.exposureCompensationRange
                val targetIndex = exposureIndex.coerceIn(range.lower, range.upper)
                camera?.cameraControl?.setExposureCompensationIndex(targetIndex)
            }
        } catch (e: Exception) {
            Log.e("CameraPreview", "Exposure compensation failed: ${e.message}")
        }
    }

    val lensFacing = when (cameraSource) {
        CameraSource.FRONT -> CameraSelector.LENS_FACING_FRONT
        CameraSource.BACK -> CameraSelector.LENS_FACING_BACK
    }

    LaunchedEffect(lensFacing) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                val capture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                cameraProvider.unbindAll()
                val boundCamera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    capture
                )
                camera = boundCamera
                imageCapture = capture
                onCameraReady?.invoke(boundCamera)
            } catch (exc: Exception) {
                Log.e("CameraPreview", "Use case binding failed", exc)
                onError?.invoke(exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        if (showGrid) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val gridColor = Color.White.copy(alpha = 0.25f)
                val strokeWidth = 1.dp.toPx()

                // Vertical Grid Lines
                drawLine(gridColor, start = Offset(width / 3f, 0f), end = Offset(width / 3f, height), strokeWidth = strokeWidth)
                drawLine(gridColor, start = Offset(2 * width / 3f, 0f), end = Offset(2 * width / 3f, height), strokeWidth = strokeWidth)

                // Horizontal Grid Lines
                drawLine(gridColor, start = Offset(0f, height / 3f), end = Offset(width, height / 3f), strokeWidth = strokeWidth)
                drawLine(gridColor, start = Offset(0f, 2 * height / 3f), end = Offset(width, 2 * height / 3f), strokeWidth = strokeWidth)
            }
        }
    }
}
