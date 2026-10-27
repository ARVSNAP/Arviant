package com.example.util

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * Result model for Cloudinary uploads
 */
sealed class CloudinaryResult {
    data class Success(
        val secureUrl: String,
        val publicId: String,
        val width: Int,
        val height: Int,
        val format: String,
        val bytes: Long
    ) : CloudinaryResult()

    data class Error(val message: String) : CloudinaryResult()
}

/**
 * Cloudinary REST API Service for Photo Booth & QRIS uploads
 * Credentials:
 * CLOUDINARY_URL=cloudinary://865291324538569:DdFr-TgDz8NTwz91psJSuvP1i5c@jggtvymn
 */
object CloudinaryService {

    const val DEFAULT_CLOUDINARY_URL = "cloudinary://865291324538569:DdFr-TgDz8NTwz91psJSuvP1i5c@jggtvymn"
    const val DEFAULT_CLOUD_NAME = "jggtvymn"
    const val DEFAULT_API_KEY = "865291324538569"
    const val DEFAULT_API_SECRET = "DdFr-TgDz8NTwz91psJSuvP1i5c"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Uploads a Bitmap directly to Cloudinary using Signed REST API
     */
    suspend fun uploadBitmap(
        bitmap: Bitmap,
        folder: String = "photobooth",
        fileNamePrefix: String = "photo_strip",
        cloudName: String = DEFAULT_CLOUD_NAME,
        apiKey: String = DEFAULT_API_KEY,
        apiSecret: String = DEFAULT_API_SECRET
    ): CloudinaryResult = withContext(Dispatchers.IO) {
        try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, stream)
            val byteArray = stream.toByteArray()
            stream.close()

            uploadByteArray(
                byteArray = byteArray,
                folder = folder,
                fileName = "${fileNamePrefix}_${System.currentTimeMillis()}.jpg",
                cloudName = cloudName,
                apiKey = apiKey,
                apiSecret = apiSecret
            )
        } catch (e: Exception) {
            e.printStackTrace()
            CloudinaryResult.Error("Gagal mempersiapkan foto: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    /**
     * Uploads a raw byte array to Cloudinary
     */
    suspend fun uploadByteArray(
        byteArray: ByteArray,
        folder: String = "photobooth",
        fileName: String = "upload_${System.currentTimeMillis()}.jpg",
        cloudName: String = DEFAULT_CLOUD_NAME,
        apiKey: String = DEFAULT_API_KEY,
        apiSecret: String = DEFAULT_API_SECRET
    ): CloudinaryResult = withContext(Dispatchers.IO) {
        try {
            val timestamp = (System.currentTimeMillis() / 1000).toString()

            // Sort params to create Cloudinary signature
            val paramsToSign = linkedMapOf<String, String>()
            if (folder.isNotBlank()) {
                paramsToSign["folder"] = folder
            }
            paramsToSign["timestamp"] = timestamp

            val signature = generateSignature(paramsToSign, apiSecret)

            val requestBodyBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("api_key", apiKey)
                .addFormDataPart("timestamp", timestamp)
                .addFormDataPart("signature", signature)

            if (folder.isNotBlank()) {
                requestBodyBuilder.addFormDataPart("folder", folder)
            }

            val imageMediaType = "image/jpeg".toMediaType()
            requestBodyBuilder.addFormDataPart(
                "file",
                fileName,
                byteArray.toRequestBody(imageMediaType)
            )

            val uploadUrl = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"
            val request = Request.Builder()
                .url(uploadUrl)
                .post(requestBodyBuilder.build())
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBodyString = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val json = JSONObject(responseBodyString)
                val secureUrl = json.optString("secure_url", json.optString("url"))
                val publicId = json.optString("public_id", "")
                val width = json.optInt("width", 0)
                val height = json.optInt("height", 0)
                val format = json.optString("format", "jpg")
                val bytes = json.optLong("bytes", byteArray.size.toLong())

                CloudinaryResult.Success(
                    secureUrl = secureUrl,
                    publicId = publicId,
                    width = width,
                    height = height,
                    format = format,
                    bytes = bytes
                )
            } else {
                val errorMsg = try {
                    val errorJson = JSONObject(responseBodyString)
                    errorJson.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                } catch (_: Exception) {
                    "HTTP ${response.code}: $responseBodyString"
                }
                CloudinaryResult.Error("Upload gagal: $errorMsg")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            CloudinaryResult.Error("Koneksi gagal: ${e.localizedMessage ?: "Periksa koneksi internet"}")
        }
    }

    /**
     * Generates SHA-1 signature based on sorted parameters + API secret
     */
    private fun generateSignature(params: Map<String, String>, apiSecret: String): String {
        val sortedString = params.entries
            .sortedBy { it.key }
            .joinToString("&") { "${it.key}=${it.value}" }
        val toSign = sortedString + apiSecret

        val md = MessageDigest.getInstance("SHA-1")
        val bytes = md.digest(toSign.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Creates a forced-download URL from standard Cloudinary secure URL
     */
    fun getDirectDownloadUrl(secureUrl: String, customFileName: String? = null): String {
        if (!secureUrl.contains("res.cloudinary.com") || !secureUrl.contains("/image/upload/")) {
            return secureUrl
        }
        val attachmentParam = if (customFileName != null) "fl_attachment:$customFileName/" else "fl_attachment/"
        return secureUrl.replace("/image/upload/", "/image/upload/$attachmentParam")
    }
}
