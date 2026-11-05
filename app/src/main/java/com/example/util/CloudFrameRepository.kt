package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.model.CustomLayoutConfig
import com.example.model.PngSlotRect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * Repository to fetch, cache, and manage Cloud Frames from Google Sheets Apps Script API.
 * Supports automatic Google Drive file URL conversion to direct image streaming,
 * transparent cutout slot detection, and offline caching.
 */
class CloudFrameRepository(private val context: Context) {

    private val tag = "CloudFrameRepository"

    private val cloudFramesDir = File(context.filesDir, "cloud_frames").apply {
        if (!exists()) mkdirs()
    }

    private val cacheJsonFile = File(context.filesDir, "cloud_frames_cache.json")

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Converts Google Drive view/share links into direct image streaming URLs.
     */
    fun resolveDirectImageUrl(rawUrl: String): String {
        if (rawUrl.isBlank()) return ""
        val trimmed = rawUrl.trim()

        val driveIdRegex = Regex("(?:file/d/|id=|open\\?id=)([a-zA-Z0-9_-]+)")
        val match = driveIdRegex.find(trimmed)
        if (match != null && trimmed.contains("drive.google.com")) {
            val fileId = match.groupValues[1]
            return "https://lh3.googleusercontent.com/d/$fileId"
        }
        return trimmed
    }

    /**
     * Reads cached cloud frames from local storage for instant offline loading.
     */
    fun loadCachedFrames(): List<CustomLayoutConfig> {
        if (!cacheJsonFile.exists()) return emptyList()
        return try {
            val jsonStr = cacheJsonFile.readText()
            parseJsonArrayToConfigs(JSONArray(jsonStr))
        } catch (e: Exception) {
            Log.e(tag, "Failed to load cached cloud frames: ${e.message}")
            emptyList()
        }
    }

    /**
     * Fetches frames from the Google Sheets Apps Script endpoint, downloads their PNGs,
     * detects transparent slots, and updates local cache.
     */
    suspend fun fetchCloudFrames(scriptUrl: String): Result<List<CustomLayoutConfig>> = withContext(Dispatchers.IO) {
        try {
            val endpoint = scriptUrl.trim()
            if (endpoint.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("URL Apps Script kosong"))
            }

            Log.d(tag, "Fetching frames from Google Sheets Apps Script: $endpoint")
            val request = Request.Builder()
                .url(endpoint)
                .header("User-Agent", "ArvinPhotoBooth/1.0")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Gagal menghubungi server: HTTP ${response.code}"))
            }

            val bodyString = response.body?.string()
                ?: return@withContext Result.failure(Exception("Respon kosong dari server"))

            val rootJson = JSONObject(bodyString)
            val success = rootJson.optBoolean("success", true)
            if (!success) {
                val errorMsg = rootJson.optString("message", "Gagal memuat daftar frame dari Google Sheets")
                return@withContext Result.failure(Exception(errorMsg))
            }

            val framesArray = rootJson.optJSONArray("frames")
                ?: return@withContext Result.success(emptyList())

            val configs = mutableListOf<CustomLayoutConfig>()

            for (i in 0 until framesArray.length()) {
                val item = framesArray.getJSONObject(i)
                val status = item.optString("status", "Active")
                if (!status.equals("Active", ignoreCase = true)) {
                    continue // Skip inactive frames
                }

                val frameId = item.optString("frame_id", (i + 1).toString())
                val frameName = item.optString("frame_name", "Cloud Frame #$frameId")
                val rawUrl = item.optString("frame_url", "")
                val orientation = item.optString("orientation", "Strip")
                val photoCount = item.optInt("photo_count", if (orientation.equals("Strip", ignoreCase = true)) 3 else 4)

                val directUrl = resolveDirectImageUrl(rawUrl)
                val localPngFile = File(cloudFramesDir, "cloud_frame_${frameId}.png")

                // Download or refresh the image file locally
                var localFilePath = ""
                var detectedSlots = emptyList<PngSlotRect>()
                var aspectRatio = when {
                    orientation.equals("Strip", ignoreCase = true) -> 3.0f
                    orientation.equals("Portrait", ignoreCase = true) -> 1.5f
                    orientation.equals("Landscape", ignoreCase = true) -> 0.67f
                    orientation.equals("A4", ignoreCase = true) -> 1.414f
                    else -> 1.5f
                }

                if (directUrl.isNotBlank()) {
                    try {
                        downloadImageToFile(directUrl, localPngFile)
                        if (localPngFile.exists() && localPngFile.length() > 0) {
                            localFilePath = localPngFile.absolutePath

                            // Analyze image and detect transparent slots
                            val bmp = BitmapFactory.decodeFile(localFilePath)
                            if (bmp != null) {
                                if (bmp.width > 0 && bmp.height > 0) {
                                    aspectRatio = bmp.height.toFloat() / bmp.width.toFloat()
                                }
                                val foundSlots = PngLayoutDetector.detectTransparentSlots(bmp)
                                if (foundSlots.isNotEmpty()) {
                                    detectedSlots = foundSlots
                                }
                                bmp.recycle()
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(tag, "Failed to download image for frame $frameId: ${e.message}")
                    }
                }

                // If no transparent slots were detected, generate fallback slots matching photo_count
                if (detectedSlots.isEmpty()) {
                    detectedSlots = generateFallbackSlots(photoCount, orientation, aspectRatio)
                }

                val layoutConfig = CustomLayoutConfig(
                    id = "cloud_frame_$frameId",
                    name = frameName,
                    photoCount = if (detectedSlots.isNotEmpty()) detectedSlots.size else photoCount,
                    columns = if (orientation.equals("Strip", ignoreCase = true) || photoCount <= 4) 1 else 2,
                    photoAspectRatio = 0.75f,
                    cornerRadiusDp = 8f,
                    frameColorHex = 0xFF0F172A,
                    textColorHex = 0xFFFFFFFF,
                    eventTitle = frameName,
                    captionText = "CLOUD FRAME • $frameName",
                    showDate = true,
                    stickerEmoji = "☁️",
                    isPngOverlay = true,
                    pngOverlayPath = localFilePath,
                    pngSlots = detectedSlots,
                    overlayAspectRatio = aspectRatio,
                    isCloudFrame = true,
                    cloudFrameUrl = directUrl,
                    cloudOrientation = orientation,
                    cloudStatus = status
                )

                configs.add(layoutConfig)
            }

            // Save to local cache
            saveConfigsToCache(configs)

            Result.success(configs)
        } catch (e: Exception) {
            Log.e(tag, "Error fetching cloud frames: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun downloadImageToFile(url: String, targetFile: File) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "ArvinPhotoBooth/1.0")
            .get()
            .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            response.body?.byteStream()?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    /**
     * Intelligently creates clean photo cutout slots based on orientation & count
     * when an image does not have detected transparent holes.
     */
    private fun generateFallbackSlots(
        count: Int,
        orientation: String,
        aspectRatio: Float
    ): List<PngSlotRect> {
        val isStrip = orientation.equals("Strip", ignoreCase = true) || aspectRatio >= 2.0f
        val isPortrait = orientation.equals("Portrait", ignoreCase = true) || (aspectRatio in 1.3f..1.9f)

        return when {
            isStrip -> {
                // Vertical strip (e.g. 2x6) with N slots stacked vertically
                val topMargin = 0.06f
                val bottomMargin = 0.12f
                val availableHeight = 1.0f - topMargin - bottomMargin
                val slotHeight = availableHeight / count
                val padding = slotHeight * 0.05f

                (0 until count).map { i ->
                    val top = topMargin + (i * slotHeight) + (padding / 2)
                    val bottom = topMargin + ((i + 1) * slotHeight) - (padding / 2)
                    PngSlotRect(
                        left = 0.08f,
                        top = top.coerceIn(0f, 1f),
                        right = 0.92f,
                        bottom = bottom.coerceIn(0f, 1f),
                        index = i
                    )
                }
            }
            count == 1 -> {
                // Single portrait photo
                listOf(
                    PngSlotRect(
                        left = 0.08f,
                        top = 0.08f,
                        right = 0.92f,
                        bottom = 0.88f,
                        index = 0
                    )
                )
            }
            count == 2 -> {
                // 2 stacked photos
                listOf(
                    PngSlotRect(left = 0.08f, top = 0.07f, right = 0.92f, bottom = 0.46f, index = 0),
                    PngSlotRect(left = 0.08f, top = 0.50f, right = 0.92f, bottom = 0.89f, index = 1)
                )
            }
            count == 4 -> {
                // 2x2 grid
                listOf(
                    PngSlotRect(left = 0.06f, top = 0.06f, right = 0.48f, bottom = 0.46f, index = 0),
                    PngSlotRect(left = 0.52f, top = 0.06f, right = 0.94f, bottom = 0.46f, index = 1),
                    PngSlotRect(left = 0.06f, top = 0.50f, right = 0.48f, bottom = 0.90f, index = 2),
                    PngSlotRect(left = 0.52f, top = 0.50f, right = 0.94f, bottom = 0.90f, index = 3)
                )
            }
            else -> {
                // Generic evenly spaced slots
                val topMargin = 0.05f
                val bottomMargin = 0.10f
                val availableHeight = 1.0f - topMargin - bottomMargin
                val slotHeight = availableHeight / count
                (0 until count).map { i ->
                    PngSlotRect(
                        left = 0.08f,
                        top = topMargin + (i * slotHeight),
                        right = 0.92f,
                        bottom = topMargin + ((i + 1) * slotHeight) - 0.01f,
                        index = i
                    )
                }
            }
        }
    }

    private fun saveConfigsToCache(configs: List<CustomLayoutConfig>) {
        try {
            val jsonArray = JSONArray()
            for (cfg in configs) {
                val obj = JSONObject().apply {
                    put("id", cfg.id)
                    put("name", cfg.name)
                    put("photoCount", cfg.photoCount)
                    put("columns", cfg.columns)
                    put("photoAspectRatio", cfg.photoAspectRatio.toDouble())
                    put("cornerRadiusDp", cfg.cornerRadiusDp.toDouble())
                    put("frameColorHex", cfg.frameColorHex)
                    put("textColorHex", cfg.textColorHex)
                    put("eventTitle", cfg.eventTitle)
                    put("captionText", cfg.captionText)
                    put("showDate", cfg.showDate)
                    put("stickerEmoji", cfg.stickerEmoji)
                    put("isPngOverlay", cfg.isPngOverlay)
                    put("pngOverlayPath", cfg.pngOverlayPath)
                    put("overlayAspectRatio", cfg.overlayAspectRatio.toDouble())
                    put("isCloudFrame", cfg.isCloudFrame)
                    put("cloudFrameUrl", cfg.cloudFrameUrl)
                    put("cloudOrientation", cfg.cloudOrientation)
                    put("cloudStatus", cfg.cloudStatus)

                    val slotsArray = JSONArray()
                    for (slot in cfg.pngSlots) {
                        val sObj = JSONObject().apply {
                            put("left", slot.left.toDouble())
                            put("top", slot.top.toDouble())
                            put("right", slot.right.toDouble())
                            put("bottom", slot.bottom.toDouble())
                            put("index", slot.index)
                        }
                        slotsArray.put(sObj)
                    }
                    put("pngSlots", slotsArray)
                }
                jsonArray.put(obj)
            }
            cacheJsonFile.writeText(jsonArray.toString())
        } catch (e: Exception) {
            Log.e(tag, "Failed to save cloud frames cache: ${e.message}")
        }
    }

    private fun parseJsonArrayToConfigs(jsonArray: JSONArray): List<CustomLayoutConfig> {
        val result = mutableListOf<CustomLayoutConfig>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val slotsArray = obj.optJSONArray("pngSlots")
            val slots = mutableListOf<PngSlotRect>()
            if (slotsArray != null) {
                for (s in 0 until slotsArray.length()) {
                    val sObj = slotsArray.getJSONObject(s)
                    slots.add(
                        PngSlotRect(
                            left = sObj.optDouble("left", 0.0).toFloat(),
                            top = sObj.optDouble("top", 0.0).toFloat(),
                            right = sObj.optDouble("right", 1.0).toFloat(),
                            bottom = sObj.optDouble("bottom", 1.0).toFloat(),
                            index = sObj.optInt("index", s)
                        )
                    )
                }
            }

            result.add(
                CustomLayoutConfig(
                    id = obj.optString("id", "cloud_frame_$i"),
                    name = obj.optString("name", "Cloud Frame"),
                    photoCount = obj.optInt("photoCount", 3),
                    columns = obj.optInt("columns", 1),
                    photoAspectRatio = obj.optDouble("photoAspectRatio", 0.75).toFloat(),
                    cornerRadiusDp = obj.optDouble("cornerRadiusDp", 8.0).toFloat(),
                    frameColorHex = obj.optLong("frameColorHex", 0xFF0F172A),
                    textColorHex = obj.optLong("textColorHex", 0xFFFFFFFF),
                    eventTitle = obj.optString("eventTitle", "CLOUD BOOTH"),
                    captionText = obj.optString("captionText", "CLOUD FRAME"),
                    showDate = obj.optBoolean("showDate", true),
                    stickerEmoji = obj.optString("stickerEmoji", "☁️"),
                    isPngOverlay = obj.optBoolean("isPngOverlay", true),
                    pngOverlayPath = obj.optString("pngOverlayPath", ""),
                    pngSlots = slots,
                    overlayAspectRatio = obj.optDouble("overlayAspectRatio", 1.5).toFloat(),
                    isCloudFrame = true,
                    cloudFrameUrl = obj.optString("cloudFrameUrl", ""),
                    cloudOrientation = obj.optString("cloudOrientation", "Strip"),
                    cloudStatus = obj.optString("cloudStatus", "Active")
                )
            )
        }
        return result
    }
}
