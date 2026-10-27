package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.model.CustomLayoutConfig
import com.example.model.PngSlotRect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * Manages persistent storage for custom layouts and external PNG overlays
 */
class CustomLayoutRepository(private val context: Context) {

    private val layoutsFile = File(context.filesDir, "saved_custom_layouts_v2.json")
    private val pngDir = File(context.filesDir, "custom_layouts").apply {
        if (!exists()) mkdirs()
    }

    /**
     * Loads all saved custom layouts from persistent disk storage
     */
    fun loadCustomLayouts(): List<CustomLayoutConfig> {
        if (!layoutsFile.exists()) return emptyList()
        return try {
            val jsonStr = layoutsFile.readText()
            val jsonArray = JSONArray(jsonStr)
            val result = mutableListOf<CustomLayoutConfig>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val isPngOverlay = obj.optBoolean("isPngOverlay", false)
                val pngPath = obj.optString("pngOverlayPath", "")

                // If it's a PNG layout, ensure the image file still exists on disk
                if (isPngOverlay && pngPath.isNotBlank()) {
                    val file = File(pngPath)
                    if (!file.exists()) {
                        continue // Skip orphaned entry
                    }
                }

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
                        id = obj.optString("id", "custom_$i"),
                        name = obj.optString("name", "Layout Custom"),
                        photoCount = obj.optInt("photoCount", 4),
                        columns = obj.optInt("columns", 2),
                        photoAspectRatio = obj.optDouble("photoAspectRatio", 0.75).toFloat(),
                        cornerRadiusDp = obj.optDouble("cornerRadiusDp", 16.0).toFloat(),
                        frameColorHex = obj.optLong("frameColorHex", 0xFF0F172A),
                        textColorHex = obj.optLong("textColorHex", 0xFFFFFFFF),
                        eventTitle = obj.optString("eventTitle", "CUSTOM BOOTH STUDIO"),
                        captionText = obj.optString("captionText", "DESAIN FOTO CUSTOM ✨"),
                        showDate = obj.optBoolean("showDate", true),
                        stickerEmoji = obj.optString("stickerEmoji", "🎨"),
                        bgImagePath = obj.optString("bgImagePath", ""),
                        isPngOverlay = isPngOverlay,
                        pngOverlayPath = pngPath,
                        pngSlots = slots,
                        overlayAspectRatio = obj.optDouble("overlayAspectRatio", 1.5).toFloat()
                    )
                )
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Persists the list of custom layouts to disk storage atomically
     */
    fun saveCustomLayouts(layouts: List<CustomLayoutConfig>) {
        try {
            val jsonArray = JSONArray()
            for (layout in layouts) {
                val obj = JSONObject().apply {
                    put("id", layout.id)
                    put("name", layout.name)
                    put("photoCount", layout.photoCount)
                    put("columns", layout.columns)
                    put("photoAspectRatio", layout.photoAspectRatio.toDouble())
                    put("cornerRadiusDp", layout.cornerRadiusDp.toDouble())
                    put("frameColorHex", layout.frameColorHex)
                    put("textColorHex", layout.textColorHex)
                    put("eventTitle", layout.eventTitle)
                    put("captionText", layout.captionText)
                    put("showDate", layout.showDate)
                    put("stickerEmoji", layout.stickerEmoji)
                    put("bgImagePath", layout.bgImagePath)
                    put("isPngOverlay", layout.isPngOverlay)
                    put("pngOverlayPath", layout.pngOverlayPath)
                    put("overlayAspectRatio", layout.overlayAspectRatio.toDouble())

                    val slotsArray = JSONArray()
                    for (slot in layout.pngSlots) {
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

            // Write to disk
            val tempFile = File(context.filesDir, "saved_custom_layouts_v2.json.tmp")
            tempFile.writeText(jsonArray.toString())
            tempFile.renameTo(layoutsFile)
        } catch (_: Exception) {
        }
    }

    /**
     * Copies an external PNG image from a content URI to app internal storage permanently
     * and decodes it so slots can be analyzed.
     */
    suspend fun importPngFile(uri: Uri): Pair<String, Bitmap?> = withContext(Dispatchers.IO) {
        try {
            if (!pngDir.exists()) pngDir.mkdirs()
            val fileName = "png_layout_${System.currentTimeMillis()}.png"
            val destFile = File(pngDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            val opts = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val bitmap = BitmapFactory.decodeFile(destFile.absolutePath, opts)
            Pair(destFile.absolutePath, bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair("", null)
        }
    }

    /**
     * Removes associated files when a custom layout is deleted
     */
    fun deleteLayout(config: CustomLayoutConfig) {
        if (config.isPngOverlay && config.pngOverlayPath.isNotBlank()) {
            try {
                val file = File(config.pngOverlayPath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (_: Exception) {
            }
        }
    }
}
