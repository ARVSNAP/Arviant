package com.example

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.example.model.ColorFilterType
import com.example.model.ExportQuality
import com.example.model.PRESET_FRAME_COLORS
import com.example.model.PRESET_STICKERS
import com.example.model.PhotoBoothLayout
import com.example.util.BitmapUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Photo Booth", appName)
    }

    @Test
    fun `verify photo strip generation`() {
        val dummyBitmap1 = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
        val dummyBitmap2 = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
        val dummyBitmap3 = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)

        val strip = BitmapUtils.createPhotoStrip(
            photos = listOf(dummyBitmap1, dummyBitmap2, dummyBitmap3),
            layout = PhotoBoothLayout.CLASSIC_3_STRIP,
            filterType = ColorFilterType.SEPIA,
            frameColorLong = PRESET_FRAME_COLORS[0].colorHex,
            textColorLong = PRESET_FRAME_COLORS[0].textColorHex,
            eventTitle = "TEST EVENT",
            captionText = "TEST CAPTION",
            showDate = true,
            stickerEmoji = PRESET_STICKERS[0],
            quality = ExportQuality.HD_1080P
        )

        assertNotNull(strip)
    }

    @Test
    fun `verify qr bitmap generation`() {
        val qr = BitmapUtils.createStylizedQrBitmap(300)
        assertNotNull(qr)
        assertEquals(300, qr.width)
        assertEquals(300, qr.height)
    }

    @Test
    fun `verify welcome loop animation settings defaults`() {
        val defaultSettings = com.example.model.AppSettings()
        assertEquals(com.example.model.WelcomeLoopSource.BUILTIN_ANIMATION, defaultSettings.welcomeLoopSource)
        assertEquals(com.example.model.WelcomeLoopPlacement.BACKGROUND_FULL, defaultSettings.welcomeLoopPlacement)
        assertEquals("", defaultSettings.welcomeExternalMediaUri)
        assertEquals(true, defaultSettings.isWelcomeLoopSoundMuted)
    }

    @Test
    fun `verify photo adjustments produces valid bitmap`() {
        val original = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val adjusted = BitmapUtils.applyPhotoAdjustments(
            source = original,
            brightness = 0.2f,
            contrast = 1.1f,
            saturation = 1.0f,
            warmth = 0.1f,
            rotationDegrees = 90f,
            flipHorizontal = true,
            flipVertical = false
        )
        assertNotNull(adjusted)
        assertEquals(100, adjusted.width)
        assertEquals(100, adjusted.height)
    }
}
