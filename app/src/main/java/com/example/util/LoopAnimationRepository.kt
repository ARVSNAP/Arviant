package com.example.util

import android.content.Context
import android.net.Uri
import com.example.model.LoopAnimationItem
import com.example.model.LoopMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class LoopAnimationRepository(private val context: Context) {

    private val jsonFile = File(context.filesDir, "loop_animations.json")

    companion object {
        val PRESET_THEME_WALLPAPER = LoopAnimationItem(
            id = "preset_theme_wallpaper",
            name = "Wallpaper Tema Anime",
            description = "Latar belakang tema UI dengan gambar wallpaper anime yang diunggah",
            type = LoopMediaType.THEME_WALLPAPER,
            isPreset = true
        )

        val PRESET_PHOTO_REEL = LoopAnimationItem(
            id = "preset_photo_reel",
            name = "Photo Reel Studio",
            description = "Loop strip foto melayang interaktif dengan kilat shutter & efek film retro",
            type = LoopMediaType.BUILTIN_PHOTO_REEL,
            isPreset = true
        )

        val PRESET_NEON_SYNTHWAVE = LoopAnimationItem(
            id = "preset_neon_synthwave",
            name = "Neon Synthwave Matrix",
            description = "Gelombang partikel neon glowing dengan efek aura pulsing retro cyber",
            type = LoopMediaType.SYNTHWAVE_PARTICLES,
            isPreset = true
        )

        val PRESET_CINEMATIC_BOKEH = LoopAnimationItem(
            id = "preset_cinematic_bokeh",
            name = "Cinematic Bokeh Studio",
            description = "Gelembung cahaya bokeh emas melayang elegan untuk suasana photo booth mewah",
            type = LoopMediaType.CINEMATIC_BOKEH,
            isPreset = true
        )

        // Compatibility references
        val PRESET_NEON_PULSE = PRESET_NEON_SYNTHWAVE
        val PRESET_DISCO_STARS = PRESET_CINEMATIC_BOKEH
        val PRESET_RETRO_SCANLINE = PRESET_PHOTO_REEL

        val DEFAULT_PRESETS = listOf(
            PRESET_THEME_WALLPAPER,
            PRESET_PHOTO_REEL,
            PRESET_NEON_SYNTHWAVE,
            PRESET_CINEMATIC_BOKEH
        )
    }

    fun loadLoopAnimations(): List<LoopAnimationItem> {
        val result = mutableListOf<LoopAnimationItem>()
        result.addAll(DEFAULT_PRESETS)

        if (jsonFile.exists()) {
            try {
                val content = jsonFile.readText()
                val jsonArray = JSONArray(content)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.optString("id", UUID.randomUUID().toString())
                    val name = obj.optString("name", "Loop Kustom")
                    val mediaUri = obj.optString("mediaUri", "")
                    val typeStr = obj.optString("type", LoopMediaType.EXTERNAL_GIF.name)
                    val mimeType = obj.optString("mimeType", "auto")
                    val speedRatio = obj.optDouble("speedRatio", 1.0).toFloat()
                    val description = obj.optString("description", "Media loop kustom")

                    val type = try {
                        LoopMediaType.valueOf(typeStr)
                    } catch (_: Exception) {
                        if (mimeType.contains("video") || mediaUri.endsWith(".mp4", ignoreCase = true)) {
                            LoopMediaType.EXTERNAL_MP4
                        } else {
                            LoopMediaType.EXTERNAL_GIF
                        }
                    }

                    result.add(
                        LoopAnimationItem(
                            id = id,
                            name = name,
                            mediaUri = mediaUri,
                            type = type,
                            mimeType = mimeType,
                            isPreset = false,
                            speedRatio = speedRatio,
                            description = description
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return result
    }

    fun saveLoopAnimations(customItems: List<LoopAnimationItem>) {
        try {
            val jsonArray = JSONArray()
            customItems.filter { !it.isPreset }.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("mediaUri", item.mediaUri)
                    put("type", item.type.name)
                    put("mimeType", item.mimeType)
                    put("speedRatio", item.speedRatio.toDouble())
                    put("description", item.description)
                }
                jsonArray.put(obj)
            }
            jsonFile.writeText(jsonArray.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun importLoopMedia(
        sourceUri: Uri,
        customName: String? = null,
        onSuccess: (LoopAnimationItem) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val mimeType = try {
                context.contentResolver.getType(sourceUri) ?: if (sourceUri.toString().endsWith(".mp4", ignoreCase = true)) "video/mp4" else "image/gif"
            } catch (_: Exception) {
                "auto"
            }

            val isVideo = mimeType.contains("video") || sourceUri.toString().endsWith(".mp4", ignoreCase = true)
            val extension = if (isVideo) "mp4" else "gif"
            val newId = "loop_" + UUID.randomUUID().toString().take(8)

            val destFile = File(context.filesDir, "loop_media_$newId.$extension")

            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                destFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            val mediaPath = if (destFile.exists() && destFile.length() > 0) {
                destFile.absolutePath
            } else {
                sourceUri.toString()
            }

            val type = if (isVideo) LoopMediaType.EXTERNAL_MP4 else LoopMediaType.EXTERNAL_GIF
            val finalName = customName.takeIf { !it.isNullBlinking() } ?: if (isVideo) "Video MP4 ${destFile.name}" else "Animasi GIF ${destFile.name}"

            val newItem = LoopAnimationItem(
                id = newId,
                name = finalName,
                mediaUri = mediaPath,
                type = type,
                mimeType = mimeType,
                isPreset = false,
                description = if (isVideo) "Video loop kustom dari perangkat" else "Animasi GIF kustom dari perangkat"
            )

            val currentCustom = loadLoopAnimations().filter { !it.isPreset }.toMutableList()
            currentCustom.add(newItem)
            saveLoopAnimations(currentCustom)

            onSuccess(newItem)
        } catch (e: Exception) {
            e.printStackTrace()
            onError(e.localizedMessage ?: "Gagal mengimpor file media loop")
        }
    }

    fun addUrlLoopMedia(url: String, customName: String? = null): LoopAnimationItem {
        val trimmedUrl = url.trim()
        val isVideo = trimmedUrl.endsWith(".mp4", ignoreCase = true)
        val newId = "url_" + UUID.randomUUID().toString().take(8)
        val type = if (isVideo) LoopMediaType.EXTERNAL_MP4 else LoopMediaType.EXTERNAL_GIF
        val mimeType = if (isVideo) "video/mp4" else "image/gif"
        val name = customName.takeIf { !it.isNullBlinking() } ?: if (isVideo) "Video URL Loop" else "GIF URL Loop"

        val newItem = LoopAnimationItem(
            id = newId,
            name = name,
            mediaUri = trimmedUrl,
            type = type,
            mimeType = mimeType,
            isPreset = false,
            description = "Media URL $trimmedUrl"
        )

        val currentCustom = loadLoopAnimations().filter { !it.isPreset }.toMutableList()
        currentCustom.add(newItem)
        saveLoopAnimations(currentCustom)
        return newItem
    }

    fun deleteLoopAnimation(item: LoopAnimationItem) {
        if (item.isPreset) return
        try {
            if (item.mediaUri.startsWith(context.filesDir.absolutePath)) {
                File(item.mediaUri).delete()
            }
        } catch (_: Exception) {}

        val currentCustom = loadLoopAnimations().filter { !it.isPreset && it.id != item.id }
        saveLoopAnimations(currentCustom)
    }

    private fun String?.isNullBlinking(): Boolean = this == null || this.isBlank()
}
