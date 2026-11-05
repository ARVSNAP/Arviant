package com.example.util

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object FaceDetectionUtil {
    private const val TAG = "FaceDetectionUtil"

    private val options by lazy {
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .build()
    }

    private val detector: FaceDetector? by lazy {
        try {
            FaceDetection.getClient(options)
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to initialize ML Kit FaceDetector: ${e.message}")
            null
        }
    }

    suspend fun detectFace(bitmap: Bitmap): Rect? {
        val client = detector ?: return null
        return try {
            suspendCoroutine { continuation ->
                try {
                    val image = InputImage.fromBitmap(bitmap, 0)
                    client.process(image)
                        .addOnSuccessListener { faces ->
                            if (faces.isNotEmpty()) {
                                continuation.resume(faces[0].boundingBox)
                            } else {
                                continuation.resume(null)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Face detection unavailable or failed: ${e.message}")
                            continuation.resume(null)
                        }
                } catch (e: Throwable) {
                    Log.w(TAG, "Error processing image for face detection: ${e.message}")
                    continuation.resume(null)
                }
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Exception during detectFace: ${e.message}")
            null
        }
    }
}
