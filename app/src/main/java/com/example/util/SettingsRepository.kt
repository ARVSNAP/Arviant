package com.example.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppSettings
import com.example.model.ExportQuality
import com.example.model.OutputFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "photobooth_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val COUNTDOWN_SECONDS = intPreferencesKey("countdown_seconds")
        val BURST_DELAY_SECONDS = intPreferencesKey("burst_delay_seconds")
        val SHUTTER_SOUND = booleanPreferencesKey("shutter_sound")
        val SCREEN_FLASH = booleanPreferencesKey("screen_flash")
        val FRONT_CAMERA = booleanPreferencesKey("front_camera")
        val OUTPUT_FORMAT = stringPreferencesKey("output_format")
        val EXPORT_QUALITY = stringPreferencesKey("export_quality")
        val EVENT_TITLE = stringPreferencesKey("event_title")
        val EVENT_SUBTITLE = stringPreferencesKey("event_subtitle")
        val KIOSK_MODE = booleanPreferencesKey("kiosk_mode")
        val ADMIN_PIN = stringPreferencesKey("admin_pin")
        val CLOUDINARY_CLOUD_NAME = stringPreferencesKey("cloudinary_cloud_name")
        val CLOUDINARY_API_KEY = stringPreferencesKey("cloudinary_api_key")
        val CLOUDINARY_API_SECRET = stringPreferencesKey("cloudinary_api_secret")
        val CLOUDINARY_FOLDER = stringPreferencesKey("cloudinary_folder")
        val QRIS_PAYMENT_URL = stringPreferencesKey("qris_payment_url")
        val USE_CUSTOM_QRIS_IMAGE = booleanPreferencesKey("use_custom_qris_image")
        val CUSTOM_QRIS_IMAGE_PATH = stringPreferencesKey("custom_qris_image_path")
        val DEFAULT_PRINT_COPIES = intPreferencesKey("default_print_copies")
        val AUTO_PRINT_ON_SESSION_END = booleanPreferencesKey("auto_print_on_session_end")
        val DEFAULT_PAPER_SIZE = stringPreferencesKey("default_paper_size")
        val DUPLICATE_DUAL_STRIP = booleanPreferencesKey("duplicate_dual_strip")
        val WELCOME_TITLE = stringPreferencesKey("welcome_title")
        val WELCOME_TAGLINE = stringPreferencesKey("welcome_tagline")
        val WELCOME_DESCRIPTION = stringPreferencesKey("welcome_description")
        val WELCOME_BUTTON_TEXT = stringPreferencesKey("welcome_button_text")
        val WELCOME_THEME = stringPreferencesKey("welcome_theme")
        val WELCOME_HERO_STYLE = stringPreferencesKey("welcome_hero_style")
        val WELCOME_PARTICLE = stringPreferencesKey("welcome_particle")
        val SHOW_WELCOME_FEATURES = booleanPreferencesKey("show_welcome_features")
        val SHOW_WELCOME_TOP_BADGE = booleanPreferencesKey("show_welcome_top_badge")
        val ENABLE_PULSING_ANIMATION = booleanPreferencesKey("enable_pulsing_animation")
        val IS_LOOP_MOTION_ENABLED = booleanPreferencesKey("is_loop_motion_enabled")
        val ACTIVE_LOOP_ANIMATION_ID = stringPreferencesKey("active_loop_animation_id")
        val WELCOME_LOOP_PLACEMENT = stringPreferencesKey("welcome_loop_placement")
        val WELCOME_LOOP_BACKGROUND_DIM = floatPreferencesKey("welcome_loop_background_dim")
        val IS_WELCOME_LOOP_SOUND_MUTED = booleanPreferencesKey("is_welcome_loop_sound_muted")
        val WELCOME_MEDIA_SCALE = stringPreferencesKey("welcome_media_scale")
        val ENABLE_AUTO_ROTATE_LOOP_PLAYLIST = booleanPreferencesKey("enable_auto_rotate_loop_playlist")
        val ROTATE_INTERVAL_SECONDS = intPreferencesKey("rotate_interval_seconds")
        val APP_PINNED = booleanPreferencesKey("app_pinned")
        val IMMERSIVE_FULLSCREEN = booleanPreferencesKey("immersive_fullscreen")
        val PREVENT_EXIT_WITH_PIN = booleanPreferencesKey("prevent_exit_with_pin")
        val USE_CUSTOM_THEME_BACKGROUND = booleanPreferencesKey("use_custom_theme_background")
        val CUSTOM_THEME_BACKGROUND_DIM = floatPreferencesKey("custom_theme_background_dim")
        val CUSTOM_THEME_BACKGROUND_PATH = stringPreferencesKey("custom_theme_background_path")
        val GRID_COLUMN_COUNT = intPreferencesKey("grid_column_count")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        val countdown = preferences[PreferencesKeys.COUNTDOWN_SECONDS] ?: 3
        val burstDelay = preferences[PreferencesKeys.BURST_DELAY_SECONDS] ?: 2
        val shutterSound = preferences[PreferencesKeys.SHUTTER_SOUND] ?: true
        val screenFlash = preferences[PreferencesKeys.SCREEN_FLASH] ?: true
        val frontCamera = preferences[PreferencesKeys.FRONT_CAMERA] ?: true
        val formatStr = preferences[PreferencesKeys.OUTPUT_FORMAT] ?: OutputFormat.JPEG.name
        val qualityStr = preferences[PreferencesKeys.EXPORT_QUALITY] ?: ExportQuality.HD_1080P.name
        val eventTitle = preferences[PreferencesKeys.EVENT_TITLE] ?: "PHOTO BOOTH 2026"
        val eventSubtitle = preferences[PreferencesKeys.EVENT_SUBTITLE] ?: "SWEET MEMORIES ✨"
        val kioskMode = preferences[PreferencesKeys.KIOSK_MODE] ?: false
        val adminPin = preferences[PreferencesKeys.ADMIN_PIN] ?: "1234"
        val cloudName = preferences[PreferencesKeys.CLOUDINARY_CLOUD_NAME] ?: "jggtvymn"
        val apiKey = preferences[PreferencesKeys.CLOUDINARY_API_KEY] ?: "865291324538569"
        val apiSecret = preferences[PreferencesKeys.CLOUDINARY_API_SECRET] ?: "DdFr-TgDz8NTwz91psJSuvP1i5c"
        val folder = preferences[PreferencesKeys.CLOUDINARY_FOLDER] ?: "photobooth"
        val qrisUrl = preferences[PreferencesKeys.QRIS_PAYMENT_URL] ?: "https://res.cloudinary.com/jggtvymn/image/upload/photobooth_qris"
        val useCustomQris = preferences[PreferencesKeys.USE_CUSTOM_QRIS_IMAGE] ?: false
        val customQrisPath = preferences[PreferencesKeys.CUSTOM_QRIS_IMAGE_PATH] ?: ""
        val defaultCopies = preferences[PreferencesKeys.DEFAULT_PRINT_COPIES] ?: 1
        val autoPrint = preferences[PreferencesKeys.AUTO_PRINT_ON_SESSION_END] ?: false
        val paperSizeStr = preferences[PreferencesKeys.DEFAULT_PAPER_SIZE] ?: com.example.model.PrintPaperSize.PHOTO_4x6.name
        val duplicateDual = preferences[PreferencesKeys.DUPLICATE_DUAL_STRIP] ?: true
        val welcomeTitle = preferences[PreferencesKeys.WELCOME_TITLE] ?: "PHOTO BOOTH"
        val welcomeTagline = preferences[PreferencesKeys.WELCOME_TAGLINE] ?: "STUDIO MEMORIES"
        val welcomeDesc = preferences[PreferencesKeys.WELCOME_DESCRIPTION] ?: "Abadikan setiap senyuman dalam strip foto klasik & animasi loop modern."
        val welcomeBtnText = preferences[PreferencesKeys.WELCOME_BUTTON_TEXT] ?: "TAP TO START"
        val welcomeThemeStr = preferences[PreferencesKeys.WELCOME_THEME] ?: com.example.model.WelcomeTheme.NEON_STUDIO.name
        val welcomeHeroStr = preferences[PreferencesKeys.WELCOME_HERO_STYLE] ?: com.example.model.WelcomeHeroStyle.DUAL_STRIP_MOCKUP.name
        val welcomeParticleStr = preferences[PreferencesKeys.WELCOME_PARTICLE] ?: com.example.model.WelcomeParticle.STARS.name
        val showFeatures = preferences[PreferencesKeys.SHOW_WELCOME_FEATURES] ?: true
        val showTopBadge = preferences[PreferencesKeys.SHOW_WELCOME_TOP_BADGE] ?: true
        val enablePulse = preferences[PreferencesKeys.ENABLE_PULSING_ANIMATION] ?: true
        val isLoopMotionEnabled = preferences[PreferencesKeys.IS_LOOP_MOTION_ENABLED] ?: true
        val activeLoopId = preferences[PreferencesKeys.ACTIVE_LOOP_ANIMATION_ID] ?: "preset_photo_reel"
        val loopPlacementStr = preferences[PreferencesKeys.WELCOME_LOOP_PLACEMENT] ?: com.example.model.WelcomeLoopPlacement.BACKGROUND_FULL.name
        val loopBgDim = preferences[PreferencesKeys.WELCOME_LOOP_BACKGROUND_DIM] ?: 0.35f
        val isLoopMuted = preferences[PreferencesKeys.IS_WELCOME_LOOP_SOUND_MUTED] ?: true
        val mediaScaleStr = preferences[PreferencesKeys.WELCOME_MEDIA_SCALE] ?: com.example.model.WelcomeMediaScale.CROP.name
        val autoRotate = preferences[PreferencesKeys.ENABLE_AUTO_ROTATE_LOOP_PLAYLIST] ?: false
        val rotateInterval = preferences[PreferencesKeys.ROTATE_INTERVAL_SECONDS] ?: 10
        val appPinned = preferences[PreferencesKeys.APP_PINNED] ?: false
        val immersiveFullscreen = preferences[PreferencesKeys.IMMERSIVE_FULLSCREEN] ?: true
        val preventExitWithPin = preferences[PreferencesKeys.PREVENT_EXIT_WITH_PIN] ?: true
        val useCustomThemeBg = preferences[PreferencesKeys.USE_CUSTOM_THEME_BACKGROUND] ?: true
        val customThemeBgDim = preferences[PreferencesKeys.CUSTOM_THEME_BACKGROUND_DIM] ?: 0.25f
        val customThemeBgPath = preferences[PreferencesKeys.CUSTOM_THEME_BACKGROUND_PATH] ?: ""
        val gridColumnCount = preferences[PreferencesKeys.GRID_COLUMN_COUNT] ?: 2

        val welcomeLoopPlacement = try {
            com.example.model.WelcomeLoopPlacement.valueOf(loopPlacementStr)
        } catch (_: Exception) {
            com.example.model.WelcomeLoopPlacement.BACKGROUND_FULL
        }

        val welcomeMediaScale = try {
            com.example.model.WelcomeMediaScale.valueOf(mediaScaleStr)
        } catch (_: Exception) {
            com.example.model.WelcomeMediaScale.CROP
        }

        val outputFormat = try {
            OutputFormat.valueOf(formatStr)
        } catch (_: Exception) {
            OutputFormat.JPEG
        }

        val exportQuality = try {
            ExportQuality.valueOf(qualityStr)
        } catch (_: Exception) {
            ExportQuality.HD_1080P
        }

        val paperSize = try {
            com.example.model.PrintPaperSize.valueOf(paperSizeStr)
        } catch (_: Exception) {
            com.example.model.PrintPaperSize.PHOTO_4x6
        }

        val welcomeTheme = try {
            com.example.model.WelcomeTheme.valueOf(welcomeThemeStr)
        } catch (_: Exception) {
            com.example.model.WelcomeTheme.ANIME_WALLPAPER
        }

        val welcomeHeroStyle = try {
            com.example.model.WelcomeHeroStyle.valueOf(welcomeHeroStr)
        } catch (_: Exception) {
            com.example.model.WelcomeHeroStyle.DUAL_STRIP_MOCKUP
        }

        val welcomeParticle = try {
            com.example.model.WelcomeParticle.valueOf(welcomeParticleStr)
        } catch (_: Exception) {
            com.example.model.WelcomeParticle.STARS
        }

        AppSettings(
            defaultCountdownSeconds = countdown,
            burstDelaySeconds = burstDelay,
            isShutterSoundEnabled = shutterSound,
            isScreenFlashEnabled = screenFlash,
            defaultUseFrontCamera = frontCamera,
            outputFormat = outputFormat,
            exportQuality = exportQuality,
            eventTitle = eventTitle,
            eventSubtitle = eventSubtitle,
            isKioskMode = kioskMode,
            adminPin = adminPin,
            cloudinaryCloudName = cloudName,
            cloudinaryApiKey = apiKey,
            cloudinaryApiSecret = apiSecret,
            cloudinaryFolder = folder,
            qrisPaymentUrl = qrisUrl,
            useCustomQrisImage = useCustomQris,
            customQrisImagePath = customQrisPath,
            defaultPrintCopies = defaultCopies,
            autoPrintOnSessionEnd = autoPrint,
            defaultPaperSize = paperSize,
            duplicateDualStrip = duplicateDual,
            welcomeTitle = welcomeTitle,
            welcomeTagline = welcomeTagline,
            welcomeDescription = welcomeDesc,
            welcomeButtonText = welcomeBtnText,
            welcomeTheme = welcomeTheme,
            welcomeHeroStyle = welcomeHeroStyle,
            welcomeParticle = welcomeParticle,
            showWelcomeFeatures = showFeatures,
            showWelcomeTopBadge = showTopBadge,
            enablePulsingAnimation = enablePulse,
            isWelcomeLoopEnabled = isLoopMotionEnabled,
            activeLoopAnimationId = activeLoopId,
            welcomeLoopPlacement = welcomeLoopPlacement,
            welcomeLoopBackgroundDim = loopBgDim,
            isWelcomeLoopSoundMuted = isLoopMuted,
            welcomeMediaScale = welcomeMediaScale,
            enableAutoRotateLoopPlaylist = autoRotate,
            rotateIntervalSeconds = rotateInterval,
            isAppPinned = appPinned,
            isImmersiveFullscreen = immersiveFullscreen,
            preventExitWithPin = preventExitWithPin,
            useCustomThemeBackground = useCustomThemeBg,
            customThemeBackgroundDim = customThemeBgDim,
            customThemeBackgroundPath = customThemeBgPath,
            gridColumnCount = gridColumnCount
        )
    }

    suspend fun updateCountdown(seconds: Int) {
        context.dataStore.edit { it[PreferencesKeys.COUNTDOWN_SECONDS] = seconds }
    }

    suspend fun updateBurstDelay(seconds: Int) {
        context.dataStore.edit { it[PreferencesKeys.BURST_DELAY_SECONDS] = seconds }
    }

    suspend fun updateShutterSound(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHUTTER_SOUND] = enabled }
    }

    suspend fun updateScreenFlash(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SCREEN_FLASH] = enabled }
    }

    suspend fun updateDefaultCamera(useFront: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.FRONT_CAMERA] = useFront }
    }

    suspend fun updateOutputFormat(format: OutputFormat) {
        context.dataStore.edit { it[PreferencesKeys.OUTPUT_FORMAT] = format.name }
    }

    suspend fun updateExportQuality(quality: ExportQuality) {
        context.dataStore.edit { it[PreferencesKeys.EXPORT_QUALITY] = quality.name }
    }

    suspend fun updateEventBranding(title: String, subtitle: String) {
        context.dataStore.edit {
            it[PreferencesKeys.EVENT_TITLE] = title
            it[PreferencesKeys.EVENT_SUBTITLE] = subtitle
        }
    }

    suspend fun updateKioskMode(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.KIOSK_MODE] = enabled }
    }

    suspend fun updateAdminPin(pin: String) {
        context.dataStore.edit { it[PreferencesKeys.ADMIN_PIN] = pin }
    }

    suspend fun updateCloudinaryConfig(
        cloudName: String,
        apiKey: String,
        apiSecret: String,
        folder: String
    ) {
        context.dataStore.edit {
            it[PreferencesKeys.CLOUDINARY_CLOUD_NAME] = cloudName
            it[PreferencesKeys.CLOUDINARY_API_KEY] = apiKey
            it[PreferencesKeys.CLOUDINARY_API_SECRET] = apiSecret
            it[PreferencesKeys.CLOUDINARY_FOLDER] = folder
        }
    }

    suspend fun updateQrisPaymentUrl(qrisUrl: String) {
        context.dataStore.edit { it[PreferencesKeys.QRIS_PAYMENT_URL] = qrisUrl }
    }

    suspend fun updatePrintSettings(
        defaultCopies: Int,
        autoPrint: Boolean,
        paperSize: com.example.model.PrintPaperSize,
        duplicateDual: Boolean
    ) {
        context.dataStore.edit {
            it[PreferencesKeys.DEFAULT_PRINT_COPIES] = defaultCopies
            it[PreferencesKeys.AUTO_PRINT_ON_SESSION_END] = autoPrint
            it[PreferencesKeys.DEFAULT_PAPER_SIZE] = paperSize.name
            it[PreferencesKeys.DUPLICATE_DUAL_STRIP] = duplicateDual
        }
    }

    suspend fun updateWelcomeText(title: String, tagline: String, description: String, buttonText: String) {
        context.dataStore.edit {
            it[PreferencesKeys.WELCOME_TITLE] = title
            it[PreferencesKeys.WELCOME_TAGLINE] = tagline
            it[PreferencesKeys.WELCOME_DESCRIPTION] = description
            it[PreferencesKeys.WELCOME_BUTTON_TEXT] = buttonText
        }
    }

    suspend fun updateWelcomeTheme(theme: com.example.model.WelcomeTheme) {
        context.dataStore.edit {
            it[PreferencesKeys.WELCOME_THEME] = theme.name
        }
    }

    suspend fun updateWelcomeHeroStyle(heroStyle: com.example.model.WelcomeHeroStyle) {
        context.dataStore.edit {
            it[PreferencesKeys.WELCOME_HERO_STYLE] = heroStyle.name
        }
    }

    suspend fun updateWelcomeParticle(particle: com.example.model.WelcomeParticle) {
        context.dataStore.edit {
            it[PreferencesKeys.WELCOME_PARTICLE] = particle.name
        }
    }

    suspend fun updateWelcomeToggles(showFeatures: Boolean, showTopBadge: Boolean, enablePulse: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.SHOW_WELCOME_FEATURES] = showFeatures
            it[PreferencesKeys.SHOW_WELCOME_TOP_BADGE] = showTopBadge
            it[PreferencesKeys.ENABLE_PULSING_ANIMATION] = enablePulse
        }
    }

    suspend fun updateAppPinned(pinned: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.APP_PINNED] = pinned
        }
    }

    suspend fun updateImmersiveFullscreen(enabled: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.IMMERSIVE_FULLSCREEN] = enabled
        }
    }

    suspend fun updatePreventExitWithPin(enabled: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.PREVENT_EXIT_WITH_PIN] = enabled
        }
    }

    suspend fun updateActiveLoopAnimationId(id: String) {
        context.dataStore.edit {
            it[PreferencesKeys.ACTIVE_LOOP_ANIMATION_ID] = id
        }
    }

    suspend fun updateLoopMotionEnabled(enabled: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.IS_LOOP_MOTION_ENABLED] = enabled
        }
    }

    suspend fun updateWelcomeLoopPlacement(placement: com.example.model.WelcomeLoopPlacement) {
        context.dataStore.edit {
            it[PreferencesKeys.WELCOME_LOOP_PLACEMENT] = placement.name
        }
    }

    suspend fun updateWelcomeLoopBackgroundDim(dim: Float) {
        context.dataStore.edit {
            it[PreferencesKeys.WELCOME_LOOP_BACKGROUND_DIM] = dim.coerceIn(0.05f, 0.95f)
        }
    }

    suspend fun updateIsWelcomeLoopSoundMuted(muted: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.IS_WELCOME_LOOP_SOUND_MUTED] = muted
        }
    }

    suspend fun updateWelcomeMediaScale(scale: com.example.model.WelcomeMediaScale) {
        context.dataStore.edit {
            it[PreferencesKeys.WELCOME_MEDIA_SCALE] = scale.name
        }
    }

    suspend fun updateAutoRotateLoopPlaylist(enabled: Boolean, intervalSeconds: Int = 10) {
        context.dataStore.edit {
            it[PreferencesKeys.ENABLE_AUTO_ROTATE_LOOP_PLAYLIST] = enabled
            it[PreferencesKeys.ROTATE_INTERVAL_SECONDS] = intervalSeconds.coerceIn(3, 120)
        }
    }

    suspend fun resetWelcomeDefaults() {
        context.dataStore.edit {
            it[PreferencesKeys.WELCOME_TITLE] = "PHOTO BOOTH"
            it[PreferencesKeys.WELCOME_TAGLINE] = "STUDIO MEMORIES"
            it[PreferencesKeys.WELCOME_DESCRIPTION] = "Abadikan setiap senyuman dalam strip foto klasik & animasi loop modern."
            it[PreferencesKeys.WELCOME_BUTTON_TEXT] = "TAP TO START"
            it[PreferencesKeys.WELCOME_THEME] = com.example.model.WelcomeTheme.NEON_STUDIO.name
            it[PreferencesKeys.WELCOME_HERO_STYLE] = com.example.model.WelcomeHeroStyle.DUAL_STRIP_MOCKUP.name
            it[PreferencesKeys.WELCOME_PARTICLE] = com.example.model.WelcomeParticle.STARS.name
            it[PreferencesKeys.SHOW_WELCOME_FEATURES] = true
            it[PreferencesKeys.SHOW_WELCOME_TOP_BADGE] = true
            it[PreferencesKeys.ENABLE_PULSING_ANIMATION] = true
        }
    }

    suspend fun saveCustomQrisImage(uri: Uri): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val qrisDir = File(context.filesDir, "qris_images").apply {
                if (!exists()) mkdirs()
            }
            val fileName = "custom_qris_${System.currentTimeMillis()}.png"
            val destFile = File(qrisDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                java.io.FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    suspend fun updateCustomQrisSettings(useCustom: Boolean, path: String) {
        context.dataStore.edit {
            it[PreferencesKeys.USE_CUSTOM_QRIS_IMAGE] = useCustom
            it[PreferencesKeys.CUSTOM_QRIS_IMAGE_PATH] = path
        }
    }

    suspend fun updateCustomThemeBackgroundEnabled(enabled: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.USE_CUSTOM_THEME_BACKGROUND] = enabled
        }
    }

    suspend fun updateCustomThemeBackgroundDim(dim: Float) {
        context.dataStore.edit {
            it[PreferencesKeys.CUSTOM_THEME_BACKGROUND_DIM] = dim
        }
    }

    suspend fun updateCustomThemeBackgroundSettings(enabled: Boolean, dim: Float, path: String) {
        context.dataStore.edit {
            it[PreferencesKeys.USE_CUSTOM_THEME_BACKGROUND] = enabled
            it[PreferencesKeys.CUSTOM_THEME_BACKGROUND_DIM] = dim
            it[PreferencesKeys.CUSTOM_THEME_BACKGROUND_PATH] = path
        }
    }

    suspend fun updateGridColumnCount(count: Int) {
        context.dataStore.edit {
            it[PreferencesKeys.GRID_COLUMN_COUNT] = count.coerceIn(2, 4)
        }
    }

    suspend fun saveCustomThemeBackgroundImage(uri: Uri): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val bgDir = File(context.filesDir, "theme_backgrounds").apply {
                if (!exists()) mkdirs()
            }
            val fileName = "custom_theme_bg_${System.currentTimeMillis()}.jpg"
            val destFile = File(bgDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                java.io.FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
