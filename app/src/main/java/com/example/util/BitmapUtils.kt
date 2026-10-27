package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import com.example.model.ColorFilterType
import com.example.model.CustomLayoutConfig
import com.example.model.ExportQuality
import com.example.model.FrameBackgroundPattern
import com.example.model.FrameBorderStyle
import com.example.model.ImageFrameStyle
import com.example.model.OutputFormat
import com.example.model.PRESET_IMAGE_FRAMES
import com.example.model.PhotoBoothLayout
import com.example.model.PhotoScaleMode
import com.example.model.PhotoAlignment
import com.example.model.PhotoSlotAdjustment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BitmapUtils {

    /**
     * Rotates and mirrors captured bitmap based on camera sensor rotation and lens facing
     */
    fun processCapturedImage(
        originalBitmap: Bitmap,
        rotationDegrees: Int,
        isFrontCamera: Boolean
    ): Bitmap {
        val matrix = android.graphics.Matrix()
        if (rotationDegrees != 0) {
            matrix.postRotate(rotationDegrees.toFloat())
        }
        if (isFrontCamera) {
            matrix.postScale(-1f, 1f)
        }
        return Bitmap.createBitmap(
            originalBitmap,
            0,
            0,
            originalBitmap.width,
            originalBitmap.height,
            matrix,
            true
        )
    }

    /**
     * Applies fine-grained adjustments (Brightness, Contrast, Saturation, Warmth, Rotation, Flip)
     * to a single photo bitmap for the image editor ("Sunting Gambar").
     */
    fun applyPhotoAdjustments(
        source: Bitmap,
        brightness: Float = 0f,
        contrast: Float = 1f,
        saturation: Float = 1f,
        warmth: Float = 0f,
        rotationDegrees: Float = 0f,
        flipHorizontal: Boolean = false,
        flipVertical: Boolean = false
    ): Bitmap {
        val transformedBitmap = if (rotationDegrees != 0f || flipHorizontal || flipVertical) {
            val matrix = android.graphics.Matrix().apply {
                if (rotationDegrees != 0f) postRotate(rotationDegrees)
                val sx = if (flipHorizontal) -1f else 1f
                val sy = if (flipVertical) -1f else 1f
                if (sx != 1f || sy != 1f) postScale(sx, sy)
            }
            Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        } else {
            source
        }

        if (brightness == 0f && contrast == 1f && saturation == 1f && warmth == 0f) {
            return transformedBitmap
        }

        val resultBitmap = Bitmap.createBitmap(
            transformedBitmap.width,
            transformedBitmap.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(resultBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        val finalMatrix = ColorMatrix()
        if (saturation != 1f) {
            finalMatrix.setSaturation(saturation.coerceIn(0f, 2.5f))
        }

        val scale = contrast.coerceIn(0.2f, 3.0f)
        val bOffset = (brightness.coerceIn(-1f, 1f)) * 128f
        val translate = 128f * (1f - scale) + bOffset

        val rWarmth = (warmth.coerceIn(-1f, 1f)) * 25f
        val bWarmth = -(warmth.coerceIn(-1f, 1f)) * 25f

        val adjMatrix = ColorMatrix(floatArrayOf(
            scale, 0f,    0f,    0f, translate + rWarmth,
            0f,    scale, 0f,    0f, translate,
            0f,    0f,    scale, 0f, translate + bWarmth,
            0f,    0f,    0f,    1f, 0f
        ))

        finalMatrix.postConcat(adjMatrix)
        paint.colorFilter = ColorMatrixColorFilter(finalMatrix)
        canvas.drawBitmap(transformedBitmap, 0f, 0f, paint)

        if (transformedBitmap != source && transformedBitmap != resultBitmap) {
            transformedBitmap.recycle()
        }

        return resultBitmap
    }

    /**
     * Loads and normalizes a bitmap from content URI for replacing photo booth slots
     */
    fun loadBitmapFromUri(context: Context, uri: Uri, maxDim: Int = 1800): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
            val origW = options.outWidth
            val origH = options.outHeight
            if (origW <= 0 || origH <= 0) return null

            var inSampleSize = 1
            if (origW > maxDim || origH > maxDim) {
                val halfW = origW / 2
                val halfH = origH / 2
                while ((halfW / inSampleSize) >= maxDim && (halfH / inSampleSize) >= maxDim) {
                    inSampleSize *= 2
                }
            }

            val decodeOpts = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val loaded = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOpts)
            } ?: return null

            var orientation = android.media.ExifInterface.ORIENTATION_NORMAL
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val exif = android.media.ExifInterface(input)
                    orientation = exif.getAttributeInt(
                        android.media.ExifInterface.TAG_ORIENTATION,
                        android.media.ExifInterface.ORIENTATION_NORMAL
                    )
                }
            } catch (_: Exception) {}

            val matrix = android.graphics.Matrix()
            when (orientation) {
                android.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                android.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                android.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                android.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                android.media.ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            }

            if (!matrix.isIdentity) {
                Bitmap.createBitmap(loaded, 0, 0, loaded.width, loaded.height, matrix, true)
            } else {
                loaded
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Applies color filter to bitmap using ColorMatrix
     */
    fun applyColorFilter(source: Bitmap, filterType: ColorFilterType): Bitmap {
        if (filterType == ColorFilterType.NORMAL) {
            return source
        }

        val resultBitmap = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        val colorMatrix = ColorMatrix()
        when (filterType) {
            ColorFilterType.NORMAL -> {
                // No modification
            }
            ColorFilterType.BW_CLASSIC -> {
                colorMatrix.setSaturation(0f)
            }
            ColorFilterType.SEPIA -> {
                val sepiaMatrix = ColorMatrix(floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f,     0f,     0f,     1f, 0f
                ))
                colorMatrix.postConcat(sepiaMatrix)
            }
            ColorFilterType.VINTAGE_FILM -> {
                // Warm contrast boost with slight fade
                colorMatrix.set(floatArrayOf(
                    1.15f, 0.05f, 0.00f, 0f, 20f,
                    0.05f, 1.05f, 0.00f, 0f, 10f,
                    0.00f, 0.05f, 0.85f, 0f, -10f,
                    0f,    0f,    0f,    1f, 0f
                ))
            }
            ColorFilterType.CYBERPUNK -> {
                // Cyan shadows + Magenta highlights
                colorMatrix.set(floatArrayOf(
                    1.20f, -0.10f, 0.10f, 0f, 15f,
                    -0.10f, 1.10f, 0.20f, 0f, 10f,
                    0.10f,  0.20f, 1.40f, 0f, 30f,
                    0f,     0f,    0f,    1f, 0f
                ))
            }
            ColorFilterType.WARM_RETRO -> {
                // Golden hour glow
                colorMatrix.set(floatArrayOf(
                    1.25f, 0.00f, 0.00f, 0f, 25f,
                    0.00f, 1.10f, 0.00f, 0f, 15f,
                    0.00f, 0.00f, 0.80f, 0f, -15f,
                    0f,    0f,    0f,    1f, 0f
                ))
            }
            ColorFilterType.NOIR_CONTRAST -> {
                // High contrast dramatic monochrome
                colorMatrix.setSaturation(0f)
                val contrastVal = 1.6f
                val translate = (-0.5f * contrastVal + 0.5f) * 255f
                val contrastMatrix = ColorMatrix(floatArrayOf(
                    contrastVal, 0f, 0f, 0f, translate,
                    0f, contrastVal, 0f, 0f, translate,
                    0f, 0f, contrastVal, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                ))
                colorMatrix.postConcat(contrastMatrix)
            }
        }

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return resultBitmap
    }

    /**
     * Applies real-time GPU/CPU based image filter processor options (skin smoothing, brightness, contrast, filter)
     */
    fun applyRealtimeFilter(
        source: Bitmap,
        filterType: ColorFilterType,
        brightness: Float = 0f,
        contrast: Float = 1f,
        skinSmoothing: Float = 0f
    ): Bitmap {
        val smoothed = if (skinSmoothing > 0.01f) {
            applySkinSmoothing(source, skinSmoothing)
        } else {
            source
        }

        val adjusted = applyPhotoAdjustments(
            source = smoothed,
            brightness = brightness,
            contrast = contrast
        )

        val finalBitmap = applyColorFilter(adjusted, filterType)

        if (smoothed != source && smoothed != finalBitmap) {
            smoothed.recycle()
        }
        if (adjusted != smoothed && adjusted != finalBitmap) {
            adjusted.recycle()
        }

        return finalBitmap
    }

    /**
     * Applies skin smoothing (soft blur effect)
     */
    fun applySkinSmoothing(source: Bitmap, intensity: Float): Bitmap {
        val radius = (intensity * 6f).coerceIn(1f, 10f).toInt()
        val scale = 0.5f
        val w = (source.width * scale).toInt().coerceAtLeast(1)
        val h = (source.height * scale).toInt().coerceAtLeast(1)
        val small = Bitmap.createScaledBitmap(source, w, h, true)
        
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        
        canvas.drawBitmap(source, 0f, 0f, paint)
        paint.alpha = (intensity * 170).toInt().coerceIn(0, 255)
        
        val matrix = android.graphics.Matrix().apply {
            postScale(source.width.toFloat() / w, source.height.toFloat() / h)
        }
        canvas.drawBitmap(small, matrix, paint)
        small.recycle()
        return result
    }

    /**
     * Creates final Composite Photo Strip based on selected layout template or custom layout design
     */
    fun createPhotoStrip(
        photos: List<Bitmap>,
        layout: PhotoBoothLayout,
        filterType: ColorFilterType,
        frameColorLong: Long,
        textColorLong: Long,
        eventTitle: String = "PHOTO BOOTH 2026",
        captionText: String = "SWEET MEMORIES ✨",
        showDate: Boolean = true,
        stickerEmoji: String = "✨",
        quality: ExportQuality = ExportQuality.HD_1080P,
        isCustomLayout: Boolean = false,
        customConfig: CustomLayoutConfig = CustomLayoutConfig(),
        imageFrame: ImageFrameStyle = PRESET_IMAGE_FRAMES[0],
        scaleMode: PhotoScaleMode = PhotoScaleMode.CROP_FILL,
        alignment: PhotoAlignment = PhotoAlignment.CENTER,
        panX: Float = 0f,
        panY: Float = 0f,
        zoom: Float = 1.0f,
        slotBgColorLong: Long = 0xFFFFFFFF,
        perSlotAdjustments: Map<Int, PhotoSlotAdjustment> = emptyMap(),
        useCustomCanvasImage: Boolean = false,
        customCanvasImagePath: String = ""
    ): Bitmap {
        val filteredPhotos = photos.map { applyColorFilter(it, filterType) }
        val targetWidth = quality.widthPx

        if (isCustomLayout) {
            val effScaleMode = if (customConfig.photoScaleMode != PhotoScaleMode.CROP_FILL && scaleMode == PhotoScaleMode.CROP_FILL) customConfig.photoScaleMode else scaleMode
            val effAlignment = if (customConfig.photoAlignment != PhotoAlignment.CENTER && alignment == PhotoAlignment.CENTER) customConfig.photoAlignment else alignment
            val effPanX = if (customConfig.photoPanX != 0f && panX == 0f) customConfig.photoPanX else panX
            val effPanY = if (customConfig.photoPanY != 0f && panY == 0f) customConfig.photoPanY else panY
            val effZoom = if (customConfig.photoZoom != 1.0f && zoom == 1.0f) customConfig.photoZoom else zoom
            val effSlotBg = if (customConfig.slotBgColorHex != 0xFFFFFFFF && slotBgColorLong == 0xFFFFFFFF) customConfig.slotBgColorHex else slotBgColorLong

            if (customConfig.isPngOverlay && customConfig.pngOverlayPath.isNotBlank()) {
                return renderPngOverlayLayout(
                    photos = filteredPhotos.take(customConfig.photoCount),
                    customConfig = customConfig,
                    targetWidth = targetWidth,
                    frameColor = frameColorLong.toInt(),
                    scaleMode = effScaleMode,
                    alignment = effAlignment,
                    panX = effPanX,
                    panY = effPanY,
                    zoom = effZoom,
                    slotBgColorLong = effSlotBg,
                    perSlotAdjustments = perSlotAdjustments,
                    useCustomCanvasImage = useCustomCanvasImage,
                    customCanvasImagePath = customCanvasImagePath
                )
            }
            return renderCustomLayout(
                photos = filteredPhotos.take(customConfig.photoCount),
                customConfig = customConfig,
                targetWidth = targetWidth,
                frameColor = frameColorLong.toInt(),
                textColor = textColorLong.toInt(),
                eventTitle = eventTitle,
                caption = captionText,
                showDate = showDate,
                sticker = stickerEmoji,
                imageFrame = imageFrame,
                scaleMode = effScaleMode,
                alignment = effAlignment,
                panX = effPanX,
                panY = effPanY,
                zoom = effZoom,
                slotBgColorLong = effSlotBg,
                perSlotAdjustments = perSlotAdjustments,
                useCustomCanvasImage = useCustomCanvasImage,
                customCanvasImagePath = customCanvasImagePath
            )
        }

        return when (layout) {
            PhotoBoothLayout.CLASSIC_3_STRIP -> renderClassicVerticalStrip(
                photos = filteredPhotos.take(3),
                expectedCount = 3,
                targetWidth = targetWidth,
                frameColor = frameColorLong.toInt(),
                textColor = textColorLong.toInt(),
                eventTitle = eventTitle,
                caption = captionText,
                showDate = showDate,
                sticker = stickerEmoji,
                imageFrame = imageFrame,
                scaleMode = scaleMode,
                alignment = alignment,
                panX = panX,
                panY = panY,
                zoom = zoom,
                slotBgColorLong = slotBgColorLong,
                perSlotAdjustments = perSlotAdjustments,
                useCustomCanvasImage = useCustomCanvasImage,
                customCanvasImagePath = customCanvasImagePath
            )
            PhotoBoothLayout.CLASSIC_4_STRIP -> renderClassicVerticalStrip(
                photos = filteredPhotos.take(4),
                expectedCount = 4,
                targetWidth = targetWidth,
                frameColor = frameColorLong.toInt(),
                textColor = textColorLong.toInt(),
                eventTitle = eventTitle,
                caption = captionText,
                showDate = showDate,
                sticker = stickerEmoji,
                imageFrame = imageFrame,
                scaleMode = scaleMode,
                alignment = alignment,
                panX = panX,
                panY = panY,
                zoom = zoom,
                slotBgColorLong = slotBgColorLong,
                perSlotAdjustments = perSlotAdjustments,
                useCustomCanvasImage = useCustomCanvasImage,
                customCanvasImagePath = customCanvasImagePath
            )
            PhotoBoothLayout.GRID_2X2_POLAROID -> renderGrid2x2Polaroid(
                photos = filteredPhotos.take(4),
                targetWidth = targetWidth,
                frameColor = frameColorLong.toInt(),
                textColor = textColorLong.toInt(),
                eventTitle = eventTitle,
                caption = captionText,
                showDate = showDate,
                sticker = stickerEmoji,
                imageFrame = imageFrame,
                scaleMode = scaleMode,
                alignment = alignment,
                panX = panX,
                panY = panY,
                zoom = zoom,
                slotBgColorLong = slotBgColorLong,
                perSlotAdjustments = perSlotAdjustments,
                useCustomCanvasImage = useCustomCanvasImage,
                customCanvasImagePath = customCanvasImagePath
            )
            PhotoBoothLayout.AESTHETIC_FILM_ROLL -> renderFilmRoll(
                photos = filteredPhotos.take(3),
                targetWidth = targetWidth,
                frameColor = frameColorLong.toInt(),
                textColor = textColorLong.toInt(),
                eventTitle = eventTitle,
                caption = captionText,
                showDate = showDate,
                sticker = stickerEmoji,
                imageFrame = imageFrame,
                scaleMode = scaleMode,
                alignment = alignment,
                panX = panX,
                panY = panY,
                zoom = zoom,
                slotBgColorLong = slotBgColorLong,
                perSlotAdjustments = perSlotAdjustments
            )
            PhotoBoothLayout.DUO_POLAROID -> renderDuoPolaroid(
                photos = filteredPhotos.take(2),
                targetWidth = targetWidth,
                frameColor = frameColorLong.toInt(),
                textColor = textColorLong.toInt(),
                eventTitle = eventTitle,
                caption = captionText,
                showDate = showDate,
                sticker = stickerEmoji,
                imageFrame = imageFrame,
                scaleMode = scaleMode,
                alignment = alignment,
                panX = panX,
                panY = panY,
                zoom = zoom,
                slotBgColorLong = slotBgColorLong,
                perSlotAdjustments = perSlotAdjustments,
                useCustomCanvasImage = useCustomCanvasImage,
                customCanvasImagePath = customCanvasImagePath
            )
        }
    }

    /**
     * Renders External PNG Layout where captured photos fill the transparent cutout boxes
     * and the PNG overlay graphics, borders, and text sit cleanly on top
     */
    private fun renderPngOverlayLayout(
        photos: List<Bitmap>,
        customConfig: CustomLayoutConfig,
        targetWidth: Int,
        frameColor: Int,
        scaleMode: PhotoScaleMode = PhotoScaleMode.CROP_FILL,
        alignment: PhotoAlignment = PhotoAlignment.CENTER,
        panX: Float = 0f,
        panY: Float = 0f,
        zoom: Float = 1.0f,
        slotBgColorLong: Long = 0xFFFFFFFF,
        perSlotAdjustments: Map<Int, PhotoSlotAdjustment> = emptyMap(),
        useCustomCanvasImage: Boolean = false,
        customCanvasImagePath: String = ""
    ): Bitmap {
        val overlayFile = File(customConfig.pngOverlayPath)
        val overlayBitmap = try {
            if (overlayFile.exists()) {
                val opts = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                BitmapFactory.decodeFile(overlayFile.absolutePath, opts)
            } else null
        } catch (_: Exception) {
            null
        }

        if (overlayBitmap == null) {
            return renderCustomLayout(
                photos = photos,
                customConfig = customConfig,
                targetWidth = targetWidth,
                frameColor = frameColor,
                textColor = customConfig.textColorHex.toInt(),
                eventTitle = customConfig.eventTitle,
                caption = customConfig.captionText,
                showDate = customConfig.showDate,
                sticker = customConfig.stickerEmoji,
                scaleMode = scaleMode,
                alignment = alignment,
                panX = panX,
                panY = panY,
                zoom = zoom,
                slotBgColorLong = slotBgColorLong,
                perSlotAdjustments = perSlotAdjustments,
                useCustomCanvasImage = useCustomCanvasImage,
                customCanvasImagePath = customCanvasImagePath
            )
        }

        val origW = overlayBitmap.width
        val origH = overlayBitmap.height
        val targetW = targetWidth
        val targetH = ((targetW.toFloat() * origH) / origW).toInt()

        val output = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // Solid background base and/or custom canvas background image
        val effUseCustom = useCustomCanvasImage || customConfig.bgImagePath.isNotBlank()
        val effPath = if (customConfig.bgImagePath.isNotBlank()) customConfig.bgImagePath else customCanvasImagePath
        drawCanvasBackground(canvas, targetW, targetH, frameColor, effUseCustom, effPath)

        val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val slots = if (customConfig.pngSlots.isNotEmpty()) {
            customConfig.pngSlots
        } else {
            PngLayoutDetector.createPresetSlots(customConfig.photoCount)
        }

        // 1. Draw captured photos into their designated transparent cutout slots (placed underneath the PNG overlay)
        for (i in slots.indices) {
            val slot = slots[i]
            val photo = photos.getOrNull(i)
            if (photo != null) {
                val left = slot.left * targetW
                val top = slot.top * targetH
                val right = slot.right * targetW
                val bottom = slot.bottom * targetH

                // Expand by 2px on all sides to completely fill underneath the transparent window borders
                val slotRect = RectF(
                    (left - 2f).coerceAtLeast(0f),
                    (top - 2f).coerceAtLeast(0f),
                    (right + 2f).coerceAtMost(targetW.toFloat()),
                    (bottom + 2f).coerceAtMost(targetH.toFloat())
                )

                val adj = perSlotAdjustments[i]
                val sMode = adj?.scaleMode ?: scaleMode
                val sAlign = adj?.alignment ?: alignment
                val sPanX = adj?.panX ?: panX
                val sPanY = adj?.panY ?: panY
                val sZoom = adj?.zoom ?: zoom
                val sBg = (adj?.slotBgColorHex ?: slotBgColorLong).toInt()

                drawCroppedRoundedBitmap(
                    canvas = canvas,
                    bitmap = photo,
                    dstRect = slotRect,
                    cornerRadius = 0f,
                    paint = photoPaint,
                    scaleMode = sMode,
                    alignment = sAlign,
                    panX = sPanX,
                    panY = sPanY,
                    zoom = sZoom,
                    slotBgColor = sBg
                )
            }
        }

        // 2. Draw the external PNG frame overlay on top of the photos!
        val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val destRect = Rect(0, 0, targetW, targetH)
        canvas.drawBitmap(overlayBitmap, null, destRect, overlayPaint)

        overlayBitmap.recycle()
        return output
    }

    /**
     * Renders User Designed Custom Layout Strip/Grid
     */
    private fun renderCustomLayout(
        photos: List<Bitmap>,
        customConfig: CustomLayoutConfig,
        targetWidth: Int,
        frameColor: Int,
        textColor: Int,
        eventTitle: String,
        caption: String,
        showDate: Boolean,
        sticker: String,
        imageFrame: ImageFrameStyle = PRESET_IMAGE_FRAMES[0],
        scaleMode: PhotoScaleMode = PhotoScaleMode.CROP_FILL,
        alignment: PhotoAlignment = PhotoAlignment.CENTER,
        panX: Float = 0f,
        panY: Float = 0f,
        zoom: Float = 1.0f,
        slotBgColorLong: Long = 0xFFFFFFFF,
        perSlotAdjustments: Map<Int, PhotoSlotAdjustment> = emptyMap(),
        useCustomCanvasImage: Boolean = false,
        customCanvasImagePath: String = ""
    ): Bitmap {
        val count = customConfig.photoCount
        val cols = customConfig.columns.coerceAtLeast(1)
        val rows = Math.ceil(count.toDouble() / cols).toInt()
        val margin = targetWidth * 0.045f
        val innerSpacing = targetWidth * 0.035f

        val itemWidth = (targetWidth - (margin * 2) - ((cols - 1) * innerSpacing)) / cols
        val itemHeight = itemWidth * customConfig.photoAspectRatio

        val titleToUse = if (customConfig.eventTitle.isNotBlank()) customConfig.eventTitle else eventTitle
        val captionToUse = if (customConfig.captionText.isNotBlank()) customConfig.captionText else caption
        val stickerToUse = if (customConfig.stickerEmoji.isNotBlank()) customConfig.stickerEmoji else sticker

        val headerHeight = if (titleToUse.isNotBlank()) targetWidth * 0.14f else targetWidth * 0.06f
        val footerHeight = targetWidth * 0.24f

        val totalPhotosHeight = (rows * itemHeight) + ((rows - 1) * innerSpacing)
        val totalHeight = (headerHeight + totalPhotosHeight + footerHeight).toInt()

        val output = Bitmap.createBitmap(targetWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // Custom Background Frame Color & Texture Pattern
        val effUseCustom = useCustomCanvasImage || customConfig.bgImagePath.isNotBlank()
        val effPath = if (customConfig.bgImagePath.isNotBlank()) customConfig.bgImagePath else customCanvasImagePath
        drawCanvasBackground(canvas, targetWidth, totalHeight, frameColor, effUseCustom, effPath)
        applyFramePatternToCanvas(canvas, targetWidth, totalHeight, imageFrame.pattern)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            alpha = 180
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = targetWidth * 0.032f
        }
        val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        // Draw Header
        if (titleToUse.isNotBlank()) {
            textPaint.textSize = targetWidth * 0.045f
            canvas.drawText(titleToUse, targetWidth / 2f, margin + (targetWidth * 0.06f), textPaint)
        }

        // Draw Photos Grid
        val startY = headerHeight
        for (i in 0 until count) {
            val r = i / cols
            val c = i % cols
            val photo = photos.getOrNull(i)

            val left = margin + (c * (itemWidth + innerSpacing))
            val top = startY + (r * (itemHeight + innerSpacing))
            val rect = RectF(left, top, left + itemWidth, top + itemHeight)

            val cornerPx = (customConfig.cornerRadiusDp * (targetWidth / 1080f)).coerceAtLeast(0f)

            val adj = perSlotAdjustments[i]
            val sMode = adj?.scaleMode ?: scaleMode
            val sAlign = adj?.alignment ?: alignment
            val sPanX = adj?.panX ?: panX
            val sPanY = adj?.panY ?: panY
            val sZoom = adj?.zoom ?: zoom
            val sBg = (adj?.slotBgColorHex ?: slotBgColorLong).toInt()

            drawPhotoWithFrame(
                canvas = canvas,
                photo = photo,
                rect = rect,
                cornerRadius = cornerPx,
                photoPaint = photoPaint,
                imageFrame = imageFrame,
                targetWidth = targetWidth,
                scaleMode = sMode,
                alignment = sAlign,
                panX = sPanX,
                panY = sPanY,
                zoom = sZoom,
                slotBgColor = sBg
            )
        }

        // Draw Footer
        val footerY = startY + (rows * itemHeight) + ((rows - 1) * innerSpacing) + (margin * 0.8f)
        if (captionToUse.isNotBlank()) {
            textPaint.textSize = targetWidth * 0.046f
            canvas.drawText("$stickerToUse $captionToUse", targetWidth / 2f, footerY + (targetWidth * 0.08f), textPaint)
        }

        if (showDate || customConfig.showDate) {
            val sdf = SimpleDateFormat("yyyy.MM.dd '•' HH:mm", Locale.getDefault())
            val dateStr = sdf.format(Date())
            canvas.drawText(dateStr, targetWidth / 2f, footerY + (targetWidth * 0.15f), subTextPaint)
        }

        return output
    }

    private fun drawCanvasBackground(
        canvas: Canvas,
        targetWidth: Int,
        totalHeight: Int,
        frameColor: Int,
        useCustomCanvasImage: Boolean = false,
        customCanvasImagePath: String = ""
    ) {
        canvas.drawColor(frameColor)
        if (useCustomCanvasImage && customCanvasImagePath.isNotBlank()) {
            val bgFile = File(customCanvasImagePath)
            if (bgFile.exists()) {
                try {
                    val bgBmp = BitmapFactory.decodeFile(bgFile.absolutePath)
                    if (bgBmp != null) {
                        canvas.drawBitmap(
                            bgBmp,
                            Rect(0, 0, bgBmp.width, bgBmp.height),
                            RectF(0f, 0f, targetWidth.toFloat(), totalHeight.toFloat()),
                            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                        )
                        bgBmp.recycle()
                    }
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Renders Classic Vertical Strip (3 or 4 photos)
     */
    private fun renderClassicVerticalStrip(
        photos: List<Bitmap>,
        expectedCount: Int,
        targetWidth: Int,
        frameColor: Int,
        textColor: Int,
        eventTitle: String,
        caption: String,
        showDate: Boolean,
        sticker: String,
        imageFrame: ImageFrameStyle = PRESET_IMAGE_FRAMES[0],
        scaleMode: PhotoScaleMode = PhotoScaleMode.CROP_FILL,
        alignment: PhotoAlignment = PhotoAlignment.CENTER,
        panX: Float = 0f,
        panY: Float = 0f,
        zoom: Float = 1.0f,
        slotBgColorLong: Long = 0xFFFFFFFF,
        perSlotAdjustments: Map<Int, PhotoSlotAdjustment> = emptyMap(),
        useCustomCanvasImage: Boolean = false,
        customCanvasImagePath: String = ""
    ): Bitmap {
        val padding = targetWidth * 0.05f
        val photoSpacing = targetWidth * 0.035f
        val photoWidth = targetWidth - (padding * 2)
        val photoHeight = photoWidth * 0.75f // 4:3 ratio

        val headerHeight = if (eventTitle.isNotBlank()) targetWidth * 0.18f else targetWidth * 0.08f
        val footerHeight = targetWidth * 0.26f

        val totalPhotosHeight = (expectedCount * photoHeight) + ((expectedCount - 1) * photoSpacing)
        val totalHeight = (headerHeight + totalPhotosHeight + footerHeight).toInt()

        val output = Bitmap.createBitmap(targetWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // Draw Canvas Background & Texture Pattern
        drawCanvasBackground(canvas, targetWidth, totalHeight, frameColor, useCustomCanvasImage, customCanvasImagePath)
        applyFramePatternToCanvas(canvas, targetWidth, totalHeight, imageFrame.pattern)

        // Paint configurations
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            alpha = 190
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = targetWidth * 0.032f
        }
        val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        // Draw Header
        var currentY = padding
        if (eventTitle.isNotBlank()) {
            textPaint.textSize = targetWidth * 0.048f
            canvas.drawText(eventTitle, targetWidth / 2f, currentY + (targetWidth * 0.06f), textPaint)
            if (sticker.isNotBlank()) {
                val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = targetWidth * 0.052f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(sticker, targetWidth / 2f, currentY + (targetWidth * 0.13f), emojiPaint)
            }
            currentY += headerHeight
        } else {
            currentY += headerHeight
        }

        // Draw Photos
        for (i in 0 until expectedCount) {
            val photo = photos.getOrNull(i)
            val dstRect = RectF(padding, currentY, padding + photoWidth, currentY + photoHeight)

            val adj = perSlotAdjustments[i]
            val sMode = adj?.scaleMode ?: scaleMode
            val sAlign = adj?.alignment ?: alignment
            val sPanX = adj?.panX ?: panX
            val sPanY = adj?.panY ?: panY
            val sZoom = adj?.zoom ?: zoom
            val sBg = (adj?.slotBgColorHex ?: slotBgColorLong).toInt()

            drawPhotoWithFrame(
                canvas = canvas,
                photo = photo,
                rect = dstRect,
                cornerRadius = 20f,
                photoPaint = photoPaint,
                imageFrame = imageFrame,
                targetWidth = targetWidth,
                scaleMode = sMode,
                alignment = sAlign,
                panX = sPanX,
                panY = sPanY,
                zoom = sZoom,
                slotBgColor = sBg
            )

            currentY += photoHeight + photoSpacing
        }

        // Draw Footer
        currentY -= photoSpacing // revert last spacing
        currentY += padding * 0.8f

        if (caption.isNotBlank()) {
            textPaint.textSize = targetWidth * 0.046f
            canvas.drawText(caption, targetWidth / 2f, currentY + (targetWidth * 0.07f), textPaint)
        }

        if (showDate) {
            val sdf = SimpleDateFormat("dd MMMM yyyy • HH:mm", Locale.getDefault())
            val dateStr = sdf.format(Date())
            canvas.drawText(dateStr, targetWidth / 2f, currentY + (targetWidth * 0.14f), subTextPaint)
        }

        return output
    }

    /**
     * Renders Grid 2x2 Polaroid Layout
     */
    private fun renderGrid2x2Polaroid(
        photos: List<Bitmap>,
        targetWidth: Int,
        frameColor: Int,
        textColor: Int,
        eventTitle: String,
        caption: String,
        showDate: Boolean,
        sticker: String,
        imageFrame: ImageFrameStyle = PRESET_IMAGE_FRAMES[0],
        scaleMode: PhotoScaleMode = PhotoScaleMode.CROP_FILL,
        alignment: PhotoAlignment = PhotoAlignment.CENTER,
        panX: Float = 0f,
        panY: Float = 0f,
        zoom: Float = 1.0f,
        slotBgColorLong: Long = 0xFFFFFFFF,
        perSlotAdjustments: Map<Int, PhotoSlotAdjustment> = emptyMap(),
        useCustomCanvasImage: Boolean = false,
        customCanvasImagePath: String = ""
    ): Bitmap {
        val margin = targetWidth * 0.045f
        val innerSpacing = targetWidth * 0.035f
        val itemSize = (targetWidth - (margin * 2) - innerSpacing) / 2f
        val headerHeight = targetWidth * 0.14f
        val footerHeight = targetWidth * 0.28f

        val totalHeight = (headerHeight + (itemSize * 2) + innerSpacing + footerHeight).toInt()
        val output = Bitmap.createBitmap(targetWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        drawCanvasBackground(canvas, targetWidth, totalHeight, frameColor, useCustomCanvasImage, customCanvasImagePath)
        applyFramePatternToCanvas(canvas, targetWidth, totalHeight, imageFrame.pattern)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            alpha = 180
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = targetWidth * 0.032f
        }
        val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        // Header Title
        textPaint.textSize = targetWidth * 0.045f
        canvas.drawText(eventTitle, targetWidth / 2f, margin + (targetWidth * 0.06f), textPaint)

        // 2x2 Grid placement
        val startY = headerHeight
        for (row in 0..1) {
            for (col in 0..1) {
                val index = (row * 2) + col
                val photo = photos.getOrNull(index)

                val left = margin + (col * (itemSize + innerSpacing))
                val top = startY + (row * (itemSize + innerSpacing))
                val rect = RectF(left, top, left + itemSize, top + itemSize)

                val adj = perSlotAdjustments[index]
                val sMode = adj?.scaleMode ?: scaleMode
                val sAlign = adj?.alignment ?: alignment
                val sPanX = adj?.panX ?: panX
                val sPanY = adj?.panY ?: panY
                val sZoom = adj?.zoom ?: zoom
                val sBg = (adj?.slotBgColorHex ?: slotBgColorLong).toInt()

                drawPhotoWithFrame(
                    canvas = canvas,
                    photo = photo,
                    rect = rect,
                    cornerRadius = 18f,
                    photoPaint = photoPaint,
                    imageFrame = imageFrame,
                    targetWidth = targetWidth,
                    scaleMode = sMode,
                    alignment = sAlign,
                    panX = sPanX,
                    panY = sPanY,
                    zoom = sZoom,
                    slotBgColor = sBg
                )
            }
        }

        // Footer Caption & Timestamp
        val footerY = startY + (itemSize * 2) + innerSpacing + (margin * 0.8f)
        if (caption.isNotBlank()) {
            textPaint.textSize = targetWidth * 0.048f
            canvas.drawText("$sticker $caption", targetWidth / 2f, footerY + (targetWidth * 0.08f), textPaint)
        }

        if (showDate) {
            val sdf = SimpleDateFormat("yyyy.MM.dd '•' HH:mm", Locale.getDefault())
            val dateStr = sdf.format(Date())
            canvas.drawText(dateStr, targetWidth / 2f, footerY + (targetWidth * 0.15f), subTextPaint)
        }

        return output
    }

    /**
     * Renders Aesthetic Film Roll (35mm Analog Style with Sprocket Holes)
     */
    private fun renderFilmRoll(
        photos: List<Bitmap>,
        targetWidth: Int,
        frameColor: Int,
        textColor: Int,
        eventTitle: String,
        caption: String,
        showDate: Boolean,
        sticker: String,
        imageFrame: ImageFrameStyle = PRESET_IMAGE_FRAMES[0],
        scaleMode: PhotoScaleMode = PhotoScaleMode.CROP_FILL,
        alignment: PhotoAlignment = PhotoAlignment.CENTER,
        panX: Float = 0f,
        panY: Float = 0f,
        zoom: Float = 1.0f,
        slotBgColorLong: Long = 0xFFFFFFFF,
        perSlotAdjustments: Map<Int, PhotoSlotAdjustment> = emptyMap()
    ): Bitmap {
        val filmBlack = android.graphics.Color.rgb(18, 18, 20)
        val filmYellow = android.graphics.Color.rgb(255, 180, 0)

        val sprocketMargin = targetWidth * 0.085f
        val photoWidth = targetWidth - (sprocketMargin * 2)
        val photoHeight = photoWidth * 0.72f
        val photoSpacing = targetWidth * 0.06f

        val headerHeight = targetWidth * 0.15f
        val footerHeight = targetWidth * 0.22f
        val totalPhotosHeight = (3 * photoHeight) + (2 * photoSpacing)
        val totalHeight = (headerHeight + totalPhotosHeight + footerHeight).toInt()

        val output = Bitmap.createBitmap(targetWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // Dark Analog Film Canvas
        canvas.drawColor(filmBlack)
        applyFramePatternToCanvas(canvas, targetWidth, totalHeight, imageFrame.pattern)

        // Draw Film Perforation / Sprocket Holes on Left & Right
        val holeWidth = targetWidth * 0.038f
        val holeHeight = targetWidth * 0.052f
        val holeCorner = 8f
        val holeSpacing = targetWidth * 0.08f

        val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(35, 35, 40)
        }
        val filmTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = filmYellow
            textSize = targetWidth * 0.026f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }

        var holeY = targetWidth * 0.04f
        var holeIndex = 1
        while (holeY < totalHeight - (targetWidth * 0.05f)) {
            // Left sprocket hole
            val leftRect = RectF(targetWidth * 0.024f, holeY, targetWidth * 0.024f + holeWidth, holeY + holeHeight)
            canvas.drawRoundRect(leftRect, holeCorner, holeCorner, holePaint)

            // Right sprocket hole
            val rightRect = RectF(targetWidth - targetWidth * 0.024f - holeWidth, holeY, targetWidth - targetWidth * 0.024f, holeY + holeHeight)
            canvas.drawRoundRect(rightRect, holeCorner, holeCorner, holePaint)

            // Film frame numbers along right side
            if (holeIndex % 2 == 0) {
                canvas.drawText("▶ ${holeIndex}A", targetWidth - targetWidth * 0.065f, holeY + (holeHeight * 0.8f), filmTextPaint)
            }

            holeY += holeSpacing
            holeIndex++
        }

        // Draw Film Top Branding
        filmTextPaint.textSize = targetWidth * 0.032f
        canvas.drawText("KODAK PORTRA 400 • 35mm FILM", targetWidth / 2f, targetWidth * 0.08f, Paint(filmTextPaint).apply {
            textAlign = Paint.Align.CENTER
        })

        // Draw Photos
        val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        var currentY = headerHeight

        for (i in 0 until 3) {
            val photo = photos.getOrNull(i)
            val rect = RectF(sprocketMargin, currentY, sprocketMargin + photoWidth, currentY + photoHeight)

            val adj = perSlotAdjustments[i]
            val sMode = adj?.scaleMode ?: scaleMode
            val sAlign = adj?.alignment ?: alignment
            val sPanX = adj?.panX ?: panX
            val sPanY = adj?.panY ?: panY
            val sZoom = adj?.zoom ?: zoom
            val sBg = (adj?.slotBgColorHex ?: slotBgColorLong).toInt()

            drawPhotoWithFrame(
                canvas = canvas,
                photo = photo,
                rect = rect,
                cornerRadius = 12f,
                photoPaint = photoPaint,
                imageFrame = imageFrame,
                targetWidth = targetWidth,
                scaleMode = sMode,
                alignment = sAlign,
                panX = sPanX,
                panY = sPanY,
                zoom = sZoom,
                slotBgColor = sBg
            )

            // Analog frame badge
            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = filmYellow
                textSize = targetWidth * 0.028f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            }
            canvas.drawText("EXP 0${i + 1}", sprocketMargin + (targetWidth * 0.02f), currentY + photoHeight - (targetWidth * 0.02f), badgePaint)

            currentY += photoHeight + photoSpacing
        }

        // Footer Caption
        val footerY = currentY - photoSpacing + (targetWidth * 0.04f)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = targetWidth * 0.042f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        if (caption.isNotBlank()) {
            canvas.drawText("$sticker $caption", targetWidth / 2f, footerY + (targetWidth * 0.06f), textPaint)
        }

        if (showDate) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val dateStr = sdf.format(Date())
            val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = filmYellow
                textAlign = Paint.Align.CENTER
                textSize = targetWidth * 0.030f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            }
            canvas.drawText(dateStr, targetWidth / 2f, footerY + (targetWidth * 0.12f), datePaint)
        }

        return output
    }

    /**
     * Renders Duo Polaroid (2 large cards)
     */
    private fun renderDuoPolaroid(
        photos: List<Bitmap>,
        targetWidth: Int,
        frameColor: Int,
        textColor: Int,
        eventTitle: String,
        caption: String,
        showDate: Boolean,
        sticker: String,
        imageFrame: ImageFrameStyle = PRESET_IMAGE_FRAMES[0],
        scaleMode: PhotoScaleMode = PhotoScaleMode.CROP_FILL,
        alignment: PhotoAlignment = PhotoAlignment.CENTER,
        panX: Float = 0f,
        panY: Float = 0f,
        zoom: Float = 1.0f,
        slotBgColorLong: Long = 0xFFFFFFFF,
        perSlotAdjustments: Map<Int, PhotoSlotAdjustment> = emptyMap(),
        useCustomCanvasImage: Boolean = false,
        customCanvasImagePath: String = ""
    ): Bitmap {
        val margin = targetWidth * 0.06f
        val photoWidth = targetWidth - (margin * 2)
        val photoHeight = photoWidth * 0.85f
        val spacing = targetWidth * 0.08f

        val headerHeight = targetWidth * 0.16f
        val footerHeight = targetWidth * 0.24f
        val totalHeight = (headerHeight + (photoHeight * 2) + spacing + footerHeight).toInt()

        val output = Bitmap.createBitmap(targetWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        drawCanvasBackground(canvas, targetWidth, totalHeight, frameColor, useCustomCanvasImage, customCanvasImagePath)
        applyFramePatternToCanvas(canvas, targetWidth, totalHeight, imageFrame.pattern)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = targetWidth * 0.05f
        }
        val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            alpha = 180
            textAlign = Paint.Align.CENTER
            textSize = targetWidth * 0.032f
        }
        val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        // Event Header
        canvas.drawText(eventTitle, targetWidth / 2f, margin + (targetWidth * 0.06f), textPaint)

        var currentY = headerHeight
        for (i in 0 until 2) {
            val photo = photos.getOrNull(i)
            val rect = RectF(margin, currentY, margin + photoWidth, currentY + photoHeight)

            val adj = perSlotAdjustments[i]
            val sMode = adj?.scaleMode ?: scaleMode
            val sAlign = adj?.alignment ?: alignment
            val sPanX = adj?.panX ?: panX
            val sPanY = adj?.panY ?: panY
            val sZoom = adj?.zoom ?: zoom
            val sBg = (adj?.slotBgColorHex ?: slotBgColorLong).toInt()

            drawPhotoWithFrame(
                canvas = canvas,
                photo = photo,
                rect = rect,
                cornerRadius = 24f,
                photoPaint = photoPaint,
                imageFrame = imageFrame,
                targetWidth = targetWidth,
                scaleMode = sMode,
                alignment = sAlign,
                panX = sPanX,
                panY = sPanY,
                zoom = sZoom,
                slotBgColor = sBg
            )

            currentY += photoHeight + spacing
        }

        val footerY = currentY - spacing + (margin * 0.5f)
        if (caption.isNotBlank()) {
            canvas.drawText("$sticker $caption", targetWidth / 2f, footerY + (targetWidth * 0.07f), textPaint)
        }

        if (showDate) {
            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            canvas.drawText(sdf.format(Date()), targetWidth / 2f, footerY + (targetWidth * 0.14f), subTextPaint)
        }

        return output
    }

    /**
     * Draws photo onto canvas with rounded corners and custom ImageFrameStyle overlay/borders/ornaments
     */
    private fun drawPhotoWithFrame(
        canvas: Canvas,
        photo: Bitmap?,
        rect: RectF,
        cornerRadius: Float,
        photoPaint: Paint,
        imageFrame: ImageFrameStyle,
        targetWidth: Int,
        scaleMode: PhotoScaleMode = PhotoScaleMode.CROP_FILL,
        alignment: PhotoAlignment = PhotoAlignment.CENTER,
        panX: Float = 0f,
        panY: Float = 0f,
        zoom: Float = 1.0f,
        slotBgColor: Int = android.graphics.Color.WHITE
    ) {
        val scaleRatio = targetWidth / 1080f
        val bw = (imageFrame.borderWidthDp * scaleRatio).coerceAtLeast(1f)
        val borderColorInt = imageFrame.borderColorHex.toInt()
        val innerColorInt = imageFrame.innerBorderColorHex.toInt()

        // 1. Draw Polaroid background card if polaroid style
        if (imageFrame.borderStyle == FrameBorderStyle.POLAROID_BORDER) {
            val cardRect = RectF(
                rect.left - (bw * 1.2f),
                rect.top - (bw * 1.2f),
                rect.right + (bw * 1.2f),
                rect.bottom + (bw * 2.8f)
            )
            val bgCardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = borderColorInt
            }
            canvas.drawRoundRect(cardRect, cornerRadius + bw, cornerRadius + bw, bgCardPaint)
        }

        // 2. Draw Photo Bitmap or Placeholder
        if (photo != null) {
            drawCroppedRoundedBitmap(
                canvas = canvas,
                bitmap = photo,
                dstRect = rect,
                cornerRadius = cornerRadius,
                paint = photoPaint,
                scaleMode = scaleMode,
                alignment = alignment,
                panX = panX,
                panY = panY,
                zoom = zoom,
                slotBgColor = slotBgColor
            )
        } else {
            val placeholderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.argb(35, 128, 128, 128)
            }
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, placeholderPaint)
        }

        // 3. Draw Frame Border Overlay based on borderStyle
        when (imageFrame.borderStyle) {
            FrameBorderStyle.SOLID -> {
                val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = borderColorInt
                    style = Paint.Style.STROKE
                    strokeWidth = bw
                }
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, strokePaint)
            }
            FrameBorderStyle.DOUBLE_LINE -> {
                val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = borderColorInt
                    style = Paint.Style.STROKE
                    strokeWidth = bw
                }
                val innerRect = RectF(
                    rect.left - (bw * 0.8f),
                    rect.top - (bw * 0.8f),
                    rect.right + (bw * 0.8f),
                    rect.bottom + (bw * 0.8f)
                )
                val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = innerColorInt
                    style = Paint.Style.STROKE
                    strokeWidth = (bw * 0.6f).coerceAtLeast(1f)
                }
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, outerPaint)
                canvas.drawRoundRect(innerRect, cornerRadius + bw, cornerRadius + bw, innerPaint)
            }
            FrameBorderStyle.POLAROID_BORDER -> {
                val borderStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.argb(40, 0, 0, 0)
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                }
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderStroke)
            }
            FrameBorderStyle.VINTAGE_GOLD -> {
                val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = borderColorInt
                    style = Paint.Style.STROKE
                    strokeWidth = bw
                }
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, goldPaint)

                val innerRect = RectF(
                    rect.left + (bw * 1.2f),
                    rect.top + (bw * 1.2f),
                    rect.right - (bw * 1.2f),
                    rect.bottom - (bw * 1.2f)
                )
                val thinGold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = innerColorInt
                    style = Paint.Style.STROKE
                    strokeWidth = (bw * 0.4f).coerceAtLeast(1f)
                }
                canvas.drawRoundRect(innerRect, (cornerRadius - bw).coerceAtLeast(2f), (cornerRadius - bw).coerceAtLeast(2f), thinGold)
            }
            FrameBorderStyle.NEON_GLOW -> {
                val glowOuter = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = borderColorInt
                    style = Paint.Style.STROKE
                    strokeWidth = bw * 1.5f
                    alpha = 160
                }
                val glowInner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = innerColorInt
                    style = Paint.Style.STROKE
                    strokeWidth = bw * 0.8f
                }
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, glowOuter)
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, glowInner)
            }
            FrameBorderStyle.DOTTED_STAMP -> {
                val stampPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = borderColorInt
                    style = Paint.Style.STROKE
                    strokeWidth = bw
                    pathEffect = DashPathEffect(floatArrayOf(bw * 2f, bw * 1.5f), 0f)
                }
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, stampPaint)
            }
            FrameBorderStyle.HEART_FRAME -> {
                val heartPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = borderColorInt
                    style = Paint.Style.STROKE
                    strokeWidth = bw
                }
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, heartPaint)
            }
            FrameBorderStyle.FLORAL_CORNER -> {
                val floralBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = borderColorInt
                    style = Paint.Style.STROKE
                    strokeWidth = bw
                }
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, floralBorder)
            }
        }

        // 4. Draw Corner Ornaments (Emoji / Icon) if set
        if (imageFrame.cornerOrnament.isNotBlank()) {
            val ornamentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = (targetWidth * 0.042f).coerceAtLeast(24f)
                textAlign = Paint.Align.CENTER
            }
            val offset = bw * 0.8f
            canvas.drawText(imageFrame.cornerOrnament, rect.left + offset, rect.top + (ornamentPaint.textSize * 0.8f), ornamentPaint)
            canvas.drawText(imageFrame.cornerOrnament, rect.right - offset, rect.top + (ornamentPaint.textSize * 0.8f), ornamentPaint)
            canvas.drawText(imageFrame.cornerOrnament, rect.right - offset, rect.bottom - (offset * 0.5f), ornamentPaint)
            canvas.drawText(imageFrame.cornerOrnament, rect.left + offset, rect.bottom - (offset * 0.5f), ornamentPaint)
        }
    }

    /**
     * Draws background pattern textures on canvas according to FrameBackgroundPattern
     */
    private fun applyFramePatternToCanvas(
        canvas: Canvas,
        targetWidth: Int,
        totalHeight: Int,
        pattern: FrameBackgroundPattern
    ) {
        val patternPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            alpha = 25
        }

        when (pattern) {
            FrameBackgroundPattern.NONE -> { /* Solid color */ }
            FrameBackgroundPattern.DOTS -> {
                val step = targetWidth * 0.06f
                val radius = targetWidth * 0.008f
                var y = step / 2f
                while (y < totalHeight) {
                    var x = step / 2f
                    while (x < targetWidth) {
                        canvas.drawCircle(x, y, radius, patternPaint)
                        x += step
                    }
                    y += step
                }
            }
            FrameBackgroundPattern.STRIPES -> {
                val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.WHITE
                    alpha = 20
                    strokeWidth = targetWidth * 0.012f
                }
                val step = targetWidth * 0.08f
                var x = -totalHeight.toFloat()
                while (x < targetWidth + totalHeight) {
                    canvas.drawLine(x, 0f, x + totalHeight, totalHeight.toFloat(), linePaint)
                    x += step
                }
            }
            FrameBackgroundPattern.STARS -> {
                val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = targetWidth * 0.035f
                    textAlign = Paint.Align.CENTER
                }
                val rng = java.util.Random(101L)
                val count = (totalHeight / (targetWidth * 0.15f)).toInt() * 6
                for (i in 0 until count) {
                    val sx = rng.nextFloat() * targetWidth
                    val sy = rng.nextFloat() * totalHeight
                    canvas.drawText("✨", sx, sy, starPaint)
                }
            }
            FrameBackgroundPattern.CHECKERBOARD -> {
                val tileSize = targetWidth * 0.08f
                val checkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.WHITE
                    alpha = 15
                }
                var r = 0
                var y = 0f
                while (y < totalHeight) {
                    var c = 0
                    var x = 0f
                    while (x < targetWidth) {
                        if ((r + c) % 2 == 0) {
                            canvas.drawRect(x, y, x + tileSize, y + tileSize, checkPaint)
                        }
                        x += tileSize
                        c++
                    }
                    y += tileSize
                    r++
                }
            }
        }
    }

    /**
     * Draws a bitmap into destination slot honoring PhotoScaleMode (Fit Center, Crop Fill, Fill Bounds),
     * PhotoAlignment (Center, Top, Bottom, Left, Right), manual pan offsets, and zoom scale
     */
    fun drawCroppedRoundedBitmap(
        canvas: Canvas,
        bitmap: Bitmap,
        dstRect: RectF,
        cornerRadius: Float,
        paint: Paint,
        scaleMode: PhotoScaleMode = PhotoScaleMode.CROP_FILL,
        alignment: PhotoAlignment = PhotoAlignment.CENTER,
        panX: Float = 0f,
        panY: Float = 0f,
        zoom: Float = 1.0f,
        slotBgColor: Int = android.graphics.Color.WHITE
    ) {
        val srcWidth = bitmap.width.toFloat()
        val srcHeight = bitmap.height.toFloat()
        val dstWidth = dstRect.width()
        val dstHeight = dstRect.height()

        if (srcWidth <= 0f || srcHeight <= 0f || dstWidth <= 0f || dstHeight <= 0f) return

        canvas.save()
        val clipPath = Path().apply {
            if (cornerRadius > 0f) {
                addRoundRect(dstRect, cornerRadius, cornerRadius, Path.Direction.CW)
            } else {
                addRect(dstRect, Path.Direction.CW)
            }
        }
        canvas.clipPath(clipPath)

        // Draw slot background (clean letterbox / container foundation)
        if (slotBgColor != 0) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = slotBgColor
                style = Paint.Style.FILL
            }
            canvas.drawRect(dstRect, bgPaint)
        }

        val effectiveZoom = zoom.coerceIn(0.5f, 3.0f)

        when (scaleMode) {
            PhotoScaleMode.FILL_BOUNDS -> {
                val shiftX = panX * dstWidth * 0.5f
                val shiftY = panY * dstHeight * 0.5f
                val targetRect = RectF(
                    dstRect.left + shiftX,
                    dstRect.top + shiftY,
                    dstRect.right + shiftX,
                    dstRect.bottom + shiftY
                )
                canvas.drawBitmap(bitmap, null, targetRect, paint)
            }
            PhotoScaleMode.FIT_CENTER -> {
                // Pas di Tengah (Tanpa Terpotong) -> Fits 100% of the image inside dstRect preserving aspect ratio
                val baseScale = minOf(dstWidth / srcWidth, dstHeight / srcHeight)
                val totalScale = baseScale * effectiveZoom
                val scaledW = srcWidth * totalScale
                val scaledH = srcHeight * totalScale

                val baseX = when (alignment) {
                    PhotoAlignment.START -> dstRect.left
                    PhotoAlignment.END -> dstRect.right - scaledW
                    else -> dstRect.left + (dstWidth - scaledW) / 2f
                }
                val baseY = when (alignment) {
                    PhotoAlignment.TOP -> dstRect.top
                    PhotoAlignment.BOTTOM -> dstRect.bottom - scaledH
                    else -> dstRect.top + (dstHeight - scaledH) / 2f
                }

                val posX = baseX + (panX * dstWidth * 0.5f)
                val posY = baseY + (panY * dstHeight * 0.5f)
                val targetRect = RectF(posX, posY, posX + scaledW, posY + scaledH)
                canvas.drawBitmap(bitmap, null, targetRect, paint)
            }
            PhotoScaleMode.CROP_FILL -> {
                // Isi Penuh Kolom -> Covers entire dstRect, honoring alignment, pan, and zoom
                val baseScale = maxOf(dstWidth / srcWidth, dstHeight / srcHeight)
                val totalScale = baseScale * effectiveZoom
                val scaledW = srcWidth * totalScale
                val scaledH = srcHeight * totalScale

                val baseX = when (alignment) {
                    PhotoAlignment.START -> dstRect.left
                    PhotoAlignment.END -> dstRect.right - scaledW
                    else -> dstRect.left + (dstWidth - scaledW) / 2f
                }
                val baseY = when (alignment) {
                    PhotoAlignment.TOP -> dstRect.top
                    PhotoAlignment.BOTTOM -> dstRect.bottom - scaledH
                    else -> dstRect.top + (dstHeight - scaledH) / 2f
                }

                val posX = baseX + (panX * dstWidth * 0.5f)
                val posY = baseY + (panY * dstHeight * 0.5f)
                val targetRect = RectF(posX, posY, posX + scaledW, posY + scaledH)
                canvas.drawBitmap(bitmap, null, targetRect, paint)
            }
        }
        canvas.restore()
    }

    /**
     * Saves bitmap to public MediaStore gallery with Scoped Storage support
     */
    fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        format: OutputFormat = OutputFormat.JPEG
    ): Pair<String, String>? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "PhotoBooth_$timestamp.${format.extension}"
        val compressFormat = if (format == OutputFormat.PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        val quality = if (format == OutputFormat.PNG) 100 else 95

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, format.mimeType)
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/PhotoBooth")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                if (uri != null) {
                    val stream: OutputStream? = resolver.openOutputStream(uri)
                    if (stream != null) {
                        bitmap.compress(compressFormat, quality, stream)
                        stream.flush()
                        stream.close()
                    }
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                    Pair(fileName, uri.toString())
                } else null
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val boothDir = File(picturesDir, "PhotoBooth").apply { if (!exists()) mkdirs() }
                val imageFile = File(boothDir, fileName)
                val fos = FileOutputStream(imageFile)
                bitmap.compress(compressFormat, quality, fos)
                fos.flush()
                fos.close()
                Pair(fileName, imageFile.absolutePath)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Creates an animated GIF byte array from captured photos with frame layout, filter, and looping mode.
     */
    fun createAnimatedGifBytes(
        photos: List<Bitmap>,
        filterType: ColorFilterType = ColorFilterType.NORMAL,
        loopMode: com.example.model.LoopMode = com.example.model.LoopMode.BOOMERANG,
        speedMs: Int = 200,
        frameColorHex: Long = 0xFF0F172A,
        captionText: String = "",
        eventTitle: String = "",
        stickerEmoji: String = ""
    ): ByteArray? {
        if (photos.isEmpty()) return null
        try {
            val gifEncoder = GifEncoder()
            gifEncoder.setDelay(speedMs)
            gifEncoder.setRepeat(0) // Infinite loop

            // Determine sequence of frames based on loopMode
            val frameIndices = when (loopMode) {
                com.example.model.LoopMode.BOOMERANG -> {
                    if (photos.size <= 1) listOf(0)
                    else {
                        val forward = photos.indices.toList()
                        val backward = (photos.size - 2 downTo 1).toList()
                        forward + backward
                    }
                }
                com.example.model.LoopMode.FORWARD -> photos.indices.toList()
                com.example.model.LoopMode.DISCO_BURST -> photos.indices.toList() + photos.indices.toList()
                com.example.model.LoopMode.VINTAGE_STOPMOTION -> {
                    if (photos.size <= 1) listOf(0)
                    else photos.indices.toList() + (photos.size - 2 downTo 1).toList()
                }
            }

            val targetWidth = 480
            val targetHeight = 640

            val renderedFrames = mutableListOf<Bitmap>()
            for (idx in frameIndices) {
                val basePhoto = photos[idx]
                val frameBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(frameBitmap)

                // Draw background
                val bgPaint = Paint().apply { color = frameColorHex.toInt() }
                canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), bgPaint)

                // Filtered photo
                val filtered = applyColorFilter(basePhoto, filterType)

                // Margin for photo
                val photoMarginX = 32f
                val photoMarginTop = 32f
                val photoHeight = (targetHeight - 140).toFloat()
                val photoWidth = (targetWidth - 64).toFloat()
                val photoRect = RectF(photoMarginX, photoMarginTop, photoMarginX + photoWidth, photoMarginTop + photoHeight)

                drawCroppedRoundedBitmap(canvas, filtered, photoRect, 16f, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))

                // Bottom banner for caption & sticker
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.WHITE
                    textAlign = Paint.Align.CENTER
                    textSize = 24f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }

                val titleToDraw = if (captionText.isNotBlank()) captionText else eventTitle
                if (titleToDraw.isNotBlank()) {
                    canvas.drawText(titleToDraw, targetWidth / 2f, targetHeight - 50f, textPaint)
                }

                if (stickerEmoji.isNotBlank()) {
                    val stickerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        textSize = 40f
                        textAlign = Paint.Align.RIGHT
                    }
                    canvas.drawText(stickerEmoji, targetWidth - 40f, targetHeight - 40f, stickerPaint)
                }

                gifEncoder.addFrame(frameBitmap)
                renderedFrames.add(frameBitmap)
            }

            val baos = java.io.ByteArrayOutputStream()
            val success = gifEncoder.encode(baos)
            for (f in renderedFrames) {
                f.recycle()
            }

            return if (success) baos.toByteArray() else null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Saves GIF bytes to public MediaStore gallery with Scoped Storage support
     */
    fun saveGifToGallery(
        context: Context,
        gifBytes: ByteArray
    ): Pair<String, String>? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "PhotoBooth_Loop_$timestamp.gif"

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/gif")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/PhotoBooth")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                if (uri != null) {
                    val stream: OutputStream? = resolver.openOutputStream(uri)
                    if (stream != null) {
                        stream.write(gifBytes)
                        stream.flush()
                        stream.close()
                    }
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                    Pair(fileName, uri.toString())
                } else null
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val boothDir = File(picturesDir, "PhotoBooth").apply { if (!exists()) mkdirs() }
                val gifFile = File(boothDir, fileName)
                val fos = FileOutputStream(gifFile)
                fos.write(gifBytes)
                fos.flush()
                fos.close()
                Pair(fileName, gifFile.absolutePath)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generates a stylized Mockup QR Code Bitmap for Kiosk guest downloads
     */
    fun createStylizedQrBitmap(sizePx: Int = 512, title: String = "PHOTO-BOOTH"): Bitmap {
        val qrBitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(qrBitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val darkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(15, 23, 42) // Slate navy
        }
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(139, 92, 246) // Violet
        }

        val moduleSize = sizePx / 25f
        val margin = moduleSize * 2

        // Draw Corner Finder Patterns (Top-Left, Top-Right, Bottom-Left)
        drawFinderPattern(canvas, margin, margin, moduleSize * 7, darkPaint, accentPaint)
        drawFinderPattern(canvas, sizePx - margin - (moduleSize * 7), margin, moduleSize * 7, darkPaint, accentPaint)
        drawFinderPattern(canvas, margin, sizePx - margin - (moduleSize * 7), moduleSize * 7, darkPaint, accentPaint)

        // Draw Decorative QR Data matrix blocks with deterministic pattern
        val rndSeed = 42L
        val random = java.util.Random(rndSeed)
        for (row in 3 until 22) {
            for (col in 3 until 22) {
                val inTopLeft = row < 10 && col < 10
                val inTopRight = row < 10 && col > 14
                val inBottomLeft = row > 14 && col < 10
                val inCenterLogo = row in 11..13 && col in 11..13

                if (!inTopLeft && !inTopRight && !inBottomLeft && !inCenterLogo) {
                    if (random.nextFloat() > 0.45f) {
                        val left = margin + (col * moduleSize)
                        val top = margin + (row * moduleSize)
                        val rect = RectF(left, top, left + (moduleSize * 0.9f), top + (moduleSize * 0.9f))
                        val isSpecial = (row + col) % 5 == 0
                        canvas.drawRoundRect(rect, 4f, 4f, if (isSpecial) accentPaint else darkPaint)
                    }
                }
            }
        }

        // Center Studio Emblem inside QR
        val centerSize = moduleSize * 3.5f
        val centerLeft = (sizePx - centerSize) / 2f
        val centerTop = (sizePx - centerSize) / 2f
        val centerRect = RectF(centerLeft, centerTop, centerLeft + centerSize, centerTop + centerSize)
        val centerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(139, 92, 246)
        }
        canvas.drawRoundRect(centerRect, 10f, 10f, centerBgPaint)

        val centerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = moduleSize * 1.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("📸", sizePx / 2f, (sizePx / 2f) + (moduleSize * 0.5f), centerTextPaint)

        return qrBitmap
    }

    private fun drawFinderPattern(
        canvas: Canvas,
        x: Float,
        y: Float,
        size: Float,
        darkPaint: Paint,
        accentPaint: Paint
    ) {
        val outerRect = RectF(x, y, x + size, y + size)
        canvas.drawRoundRect(outerRect, 12f, 12f, darkPaint)

        val innerWhite = RectF(x + (size * 0.14f), y + (size * 0.14f), x + (size * 0.86f), y + (size * 0.86f))
        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE }
        canvas.drawRoundRect(innerWhite, 8f, 8f, whitePaint)

        val coreRect = RectF(x + (size * 0.28f), y + (size * 0.28f), x + (size * 0.72f), y + (size * 0.72f))
        canvas.drawRoundRect(coreRect, 6f, 6f, accentPaint)
    }

    /**
     * Creates an animated MP4 video file from captured photos with frame layout, filter, and looping mode.
     */
    fun createAnimatedMp4File(
        context: Context,
        photos: List<Bitmap>,
        filterType: ColorFilterType = ColorFilterType.NORMAL,
        loopMode: com.example.model.LoopMode = com.example.model.LoopMode.BOOMERANG,
        speedMs: Int = 200,
        frameColorHex: Long = 0xFF0F172A,
        captionText: String = "",
        eventTitle: String = "",
        stickerEmoji: String = ""
    ): File? {
        if (photos.isEmpty()) return null
        try {
            val width = 480
            val height = 640
            val cachePath = File(context.cacheDir, "shared_photos").apply { if (!exists()) mkdirs() }
            val mp4File = File(cachePath, "booth_loop_${System.currentTimeMillis()}.mp4")

            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, 2000000)
                setInteger(MediaFormat.KEY_FRAME_RATE, 15)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val surface = codec.createInputSurface()
            codec.start()

            val muxer = MediaMuxer(mp4File.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var trackIndex = -1
            var muxerStarted = false

            val frameIndices = when (loopMode) {
                com.example.model.LoopMode.BOOMERANG -> {
                    if (photos.size <= 1) listOf(0)
                    else {
                        val forward = photos.indices.toList()
                        val backward = (photos.size - 2 downTo 1).toList()
                        forward + backward
                    }
                }
                com.example.model.LoopMode.FORWARD -> photos.indices.toList()
                com.example.model.LoopMode.DISCO_BURST -> photos.indices.toList() + photos.indices.toList()
                com.example.model.LoopMode.VINTAGE_STOPMOTION -> {
                    if (photos.size <= 1) listOf(0)
                    else photos.indices.toList() + (photos.size - 2 downTo 1).toList()
                }
            }

            val bufferInfo = MediaCodec.BufferInfo()

            for (loopCount in 0 until 4) {
                for ((_, idx) in frameIndices.withIndex()) {
                    val basePhoto = photos[idx]
                    val frameBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(frameBitmap)

                    val bgPaint = Paint().apply { color = frameColorHex.toInt() }
                    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

                    val filtered = applyColorFilter(basePhoto, filterType)
                    val photoRect = RectF(32f, 32f, width - 32f, (height - 140).toFloat())
                    drawCroppedRoundedBitmap(canvas, filtered, photoRect, 16f, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))

                    val titleToDraw = if (captionText.isNotBlank()) captionText else eventTitle
                    if (titleToDraw.isNotBlank()) {
                        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = android.graphics.Color.WHITE
                            textAlign = Paint.Align.CENTER
                            textSize = 24f
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        }
                        canvas.drawText(titleToDraw, width / 2f, height - 50f, textPaint)
                    }

                    if (stickerEmoji.isNotBlank()) {
                        val stickerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            textSize = 40f
                            textAlign = Paint.Align.RIGHT
                        }
                        canvas.drawText(stickerEmoji, (width - 40).toFloat(), (height - 40).toFloat(), stickerPaint)
                    }

                    try {
                        val sCanvas = surface.lockCanvas(null)
                        sCanvas.drawBitmap(frameBitmap, 0f, 0f, null)
                        surface.unlockCanvasAndPost(sCanvas)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    frameBitmap.recycle()

                    var outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 0)
                    while (outputBufferId >= 0) {
                        val outBuffer = codec.getOutputBuffer(outputBufferId)
                        if (outBuffer != null && (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0 && bufferInfo.size > 0) {
                            outBuffer.position(bufferInfo.offset)
                            outBuffer.limit(bufferInfo.offset + bufferInfo.size)
                            if (muxerStarted) {
                                muxer.writeSampleData(trackIndex, outBuffer, bufferInfo)
                            }
                        }
                        codec.releaseOutputBuffer(outputBufferId, false)
                        outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 0)
                    }
                    if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        trackIndex = muxer.addTrack(codec.outputFormat)
                        muxer.start()
                        muxerStarted = true
                    }
                }
            }

            codec.signalEndOfInputStream()
            var outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 10000)
            while (outputBufferId >= 0) {
                val outBuffer = codec.getOutputBuffer(outputBufferId)
                if (outBuffer != null && bufferInfo.size > 0 && muxerStarted) {
                    outBuffer.position(bufferInfo.offset)
                    outBuffer.limit(bufferInfo.offset + bufferInfo.size)
                    muxer.writeSampleData(trackIndex, outBuffer, bufferInfo)
                }
                codec.releaseOutputBuffer(outputBufferId, false)
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) break
                outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 10000)
            }

            try { codec.stop() } catch (e: Exception) {}
            try { codec.release() } catch (e: Exception) {}
            if (muxerStarted) {
                try { muxer.stop() } catch (e: Exception) {}
                try { muxer.release() } catch (e: Exception) {}
            }

            return if (mp4File.exists() && mp4File.length() > 0) mp4File else null
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val cachePath = File(context.cacheDir, "shared_photos").apply { if (!exists()) mkdirs() }
                val fallbackFile = File(cachePath, "booth_loop_${System.currentTimeMillis()}.mp4")
                val gifBytes = createAnimatedGifBytes(photos, filterType, loopMode, speedMs, frameColorHex, captionText, eventTitle, stickerEmoji)
                if (gifBytes != null) {
                    fallbackFile.writeBytes(gifBytes)
                    return fallbackFile
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return null
        }
    }

    /**
     * Saves MP4 video file to public MediaStore gallery with Scoped Storage support
     */
    fun saveMp4ToGallery(
        context: Context,
        mp4File: File
    ): Pair<String, String>? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "PhotoBooth_Loop_$timestamp.mp4"

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/PhotoBooth")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri: Uri? = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)

                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { stream ->
                        FileInputStream(mp4File).use { input ->
                            input.copyTo(stream)
                        }
                    }
                    values.clear()
                    values.put(MediaStore.Video.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                    Pair(fileName, uri.toString())
                } else null
            } else {
                val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                val boothDir = File(moviesDir, "PhotoBooth").apply { if (!exists()) mkdirs() }
                val destFile = File(boothDir, fileName)
                mp4File.copyTo(destFile, overwrite = true)
                Pair(fileName, destFile.absolutePath)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
