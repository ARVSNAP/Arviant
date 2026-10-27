package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.example.model.PngSlotRect
import java.util.ArrayDeque
import kotlin.math.abs

/**
 * Intelligent detector and generator for transparent cutout boxes in external PNG layouts
 */
object PngLayoutDetector {

    /**
     * Analyzes a PNG Bitmap to automatically detect transparent cutout boxes/holes
     * Returns a list of normalized PngSlotRect with coordinates in [0f..1f].
     */
    fun detectTransparentSlots(bitmap: Bitmap): List<PngSlotRect> {
        val origW = bitmap.width
        val origH = bitmap.height
        if (origW <= 0 || origH <= 0) return emptyList()

        // Downscale for fast and memory-efficient pixel analysis (max 320px)
        val maxDim = 320
        val scale = if (origW > maxDim || origH > maxDim) {
            maxDim.toFloat() / maxOf(origW, origH)
        } else {
            1.0f
        }
        val scanW = (origW * scale).toInt().coerceAtLeast(40)
        val scanH = (origH * scale).toInt().coerceAtLeast(40)

        val scaled = Bitmap.createScaledBitmap(bitmap, scanW, scanH, false)
        val pixels = IntArray(scanW * scanH)
        scaled.getPixels(pixels, 0, scanW, 0, 0, scanW, scanH)
        if (scaled != bitmap) {
            scaled.recycle()
        }

        // Alpha threshold: pixels with alpha < 40 are treated as transparent cutouts
        val isTransparent = BooleanArray(scanW * scanH) { i ->
            Color.alpha(pixels[i]) < 40
        }

        val totalPixels = scanW * scanH
        val visited = BooleanArray(totalPixels)
        val detectedBoxes = mutableListOf<RawBox>()

        // BFS to find connected components of transparent pixels
        for (y in 0 until scanH) {
            for (x in 0 until scanW) {
                val index = y * scanW + x
                if (isTransparent[index] && !visited[index]) {
                    var minX = x
                    var maxX = x
                    var minY = y
                    var maxY = y
                    var count = 0

                    val queue = ArrayDeque<Int>()
                    queue.add(index)
                    visited[index] = true

                    while (!queue.isEmpty()) {
                        val curr = queue.poll()
                        val cx = curr % scanW
                        val cy = curr / scanW
                        count++

                        if (cx < minX) minX = cx
                        if (cx > maxX) maxX = cx
                        if (cy < minY) minY = cy
                        if (cy > maxY) maxY = cy

                        val leftIdx = if (cx > 0) cy * scanW + (cx - 1) else -1
                        val rightIdx = if (cx < scanW - 1) cy * scanW + (cx + 1) else -1
                        val topIdx = if (cy > 0) (cy - 1) * scanW + cx else -1
                        val bottomIdx = if (cy < scanH - 1) (cy + 1) * scanW + cx else -1

                        if (leftIdx >= 0 && isTransparent[leftIdx] && !visited[leftIdx]) {
                            visited[leftIdx] = true
                            queue.add(leftIdx)
                        }
                        if (rightIdx >= 0 && isTransparent[rightIdx] && !visited[rightIdx]) {
                            visited[rightIdx] = true
                            queue.add(rightIdx)
                        }
                        if (topIdx >= 0 && isTransparent[topIdx] && !visited[topIdx]) {
                            visited[topIdx] = true
                            queue.add(topIdx)
                        }
                        if (bottomIdx >= 0 && isTransparent[bottomIdx] && !visited[bottomIdx]) {
                            visited[bottomIdx] = true
                            queue.add(bottomIdx)
                        }
                    }

                    val boxW = maxX - minX + 1
                    val boxH = maxY - minY + 1
                    val boxArea = boxW * boxH

                    // Conditions to consider this a photo cutout slot:
                    // 1. Minimum area: at least 1.5% of total image area
                    // 2. Maximum area: at most 92% of total image area (exclude fully empty image)
                    // 3. Density: transparent pixels should fill at least 40% of the bounding box
                    // 4. Exclude outer edge bleed if it touches all four outer borders
                    val isOuterFrame = (minX <= 2 && minY <= 2 && maxX >= scanW - 3 && maxY >= scanH - 3)

                    if (!isOuterFrame &&
                        boxArea >= (totalPixels * 0.015f) &&
                        boxArea <= (totalPixels * 0.92f) &&
                        count >= (boxArea * 0.40f)
                    ) {
                        detectedBoxes.add(
                            RawBox(
                                left = minX.toFloat() / scanW,
                                top = minY.toFloat() / scanH,
                                right = (maxX + 1).toFloat() / scanW,
                                bottom = (maxY + 1).toFloat() / scanH
                            )
                        )
                    }
                }
            }
        }

        // Sort detected boxes in natural reading order: top-to-bottom, left-to-right
        val sorted = detectedBoxes.sortedWith(Comparator { a, b ->
            // If vertically within 8% of total height, sort left-to-right (same row)
            if (abs(a.top - b.top) < 0.08f) {
                a.left.compareTo(b.left)
            } else {
                a.top.compareTo(b.top)
            }
        })

        return sorted.mapIndexed { idx, box ->
            PngSlotRect(
                left = box.left.coerceIn(0f, 1f),
                top = box.top.coerceIn(0f, 1f),
                right = box.right.coerceIn(0f, 1f),
                bottom = box.bottom.coerceIn(0f, 1f),
                index = idx
            )
        }
    }

    /**
     * Standard slot presets if no transparent regions detected or user prefers preset arrangements
     */
    fun createPresetSlots(count: Int): List<PngSlotRect> {
        return when (count) {
            1 -> listOf(
                PngSlotRect(left = 0.08f, top = 0.08f, right = 0.92f, bottom = 0.88f, index = 0)
            )
            2 -> listOf(
                PngSlotRect(left = 0.08f, top = 0.06f, right = 0.92f, bottom = 0.47f, index = 0),
                PngSlotRect(left = 0.08f, top = 0.51f, right = 0.92f, bottom = 0.92f, index = 1)
            )
            3 -> listOf(
                PngSlotRect(left = 0.08f, top = 0.06f, right = 0.92f, bottom = 0.33f, index = 0),
                PngSlotRect(left = 0.08f, top = 0.36f, right = 0.92f, bottom = 0.63f, index = 1),
                PngSlotRect(left = 0.08f, top = 0.66f, right = 0.92f, bottom = 0.93f, index = 2)
            )
            4 -> listOf(
                // 2x2 Grid
                PngSlotRect(left = 0.06f, top = 0.08f, right = 0.47f, bottom = 0.47f, index = 0),
                PngSlotRect(left = 0.53f, top = 0.08f, right = 0.94f, bottom = 0.47f, index = 1),
                PngSlotRect(left = 0.06f, top = 0.51f, right = 0.47f, bottom = 0.90f, index = 2),
                PngSlotRect(left = 0.53f, top = 0.51f, right = 0.94f, bottom = 0.90f, index = 3)
            )
            6 -> listOf(
                // 2x3 Grid
                PngSlotRect(left = 0.06f, top = 0.06f, right = 0.47f, bottom = 0.33f, index = 0),
                PngSlotRect(left = 0.53f, top = 0.06f, right = 0.94f, bottom = 0.33f, index = 1),
                PngSlotRect(left = 0.06f, top = 0.36f, right = 0.47f, bottom = 0.63f, index = 2),
                PngSlotRect(left = 0.53f, top = 0.36f, right = 0.94f, bottom = 0.63f, index = 3),
                PngSlotRect(left = 0.06f, top = 0.66f, right = 0.47f, bottom = 0.93f, index = 4),
                PngSlotRect(left = 0.53f, top = 0.66f, right = 0.94f, bottom = 0.93f, index = 5)
            )
            else -> {
                // Fallback vertical strip
                val total = count.coerceAtLeast(1)
                val marginH = 0.08f
                val marginV = 0.05f
                val availableH = 1f - (2 * marginV)
                val spacing = 0.02f
                val slotH = (availableH - ((total - 1) * spacing)) / total
                (0 until total).map { i ->
                    val top = marginV + i * (slotH + spacing)
                    PngSlotRect(
                        left = marginH,
                        top = top,
                        right = 1f - marginH,
                        bottom = top + slotH,
                        index = i
                    )
                }
            }
        }
    }

    /**
     * Creates a decorative simulated sample photo to demonstrate how photos fill the transparent slot
     */
    fun createSamplePhotoForSlot(width: Int, height: Int, slotIndex: Int): Bitmap {
        val w = width.coerceAtLeast(100)
        val h = height.coerceAtLeast(100)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Palette of vibrant gradient colors for preview
        val colors = listOf(
            0xFF8B5CF6.toInt(), // Violet
            0xFFEC4899.toInt(), // Neon Pink
            0xFF3B82F6.toInt(), // Blue
            0xFF10B981.toInt(), // Emerald
            0xFFF59E0B.toInt(), // Amber
            0xFF06B6D4.toInt()  // Cyan
        )
        val baseColor = colors[slotIndex % colors.size]
        canvas.drawColor(baseColor)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = (minOf(w, h) * 0.18f).coerceAtLeast(20f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xCCFFFFFF.toInt()
            textAlign = Paint.Align.CENTER
            textSize = (minOf(w, h) * 0.09f).coerceAtLeast(12f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        canvas.drawText("FOTO #${slotIndex + 1}", w / 2f, h * 0.48f, paint)
        canvas.drawText("Pose Kamera", w / 2f, h * 0.62f, subPaint)

        return bitmap
    }

    private data class RawBox(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float
    )
}
