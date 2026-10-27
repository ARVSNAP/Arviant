package com.example.model

import android.graphics.Bitmap

/**
 * Screen navigation routes for the Photo Booth App
 */
enum class AppScreen {
    WELCOME,
    TEMPLATE_SELECT,
    CAMERA,
    PREVIEW,
    SETTINGS
}

/**
 * Available Photo Strip Templates & Layouts
 */
enum class PhotoBoothLayout(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val photoCount: Int,
    val description: String,
    val iconName: String
) {
    CLASSIC_3_STRIP(
        id = "classic_3",
        displayName = "Classic 3-Strip",
        subtitle = "3 Foto Vertikal",
        photoCount = 3,
        description = "Format strip vertikal klasik 3 slot dengan branding header/footer elegan.",
        iconName = "view_stream"
    ),
    CLASSIC_4_STRIP(
        id = "classic_4",
        displayName = "Classic 4-Strip",
        subtitle = "4 Foto Vertikal",
        photoCount = 4,
        description = "Strip panjang 4 pose vertikal ala studio foto vintage modern.",
        iconName = "view_agenda"
    ),
    GRID_2X2_POLAROID(
        id = "grid_2x2",
        displayName = "Grid 2x2 Polaroid",
        subtitle = "4 Foto Grid Kotak",
        photoCount = 4,
        description = "Tata letak 4 foto grid 2x2 simetris dengan bingkai polaroid instan.",
        iconName = "grid_view"
    ),
    AESTHETIC_FILM_ROLL(
        id = "film_roll",
        displayName = "Aesthetic Film Roll",
        subtitle = "35mm Analog Roll",
        photoCount = 3,
        description = "Desain klise film analog 35mm dengan lubang sprocket dan penomoran frame.",
        iconName = "movie"
    ),
    DUO_POLAROID(
        id = "duo_polaroid",
        displayName = "Duo Polaroid",
        subtitle = "2 Foto Stacked",
        photoCount = 2,
        description = "Format 2 foto ukuran besar gaya polaroid ganda dengan ruang caption luas.",
        iconName = "crop_portrait"
    );

    companion object {
        fun fromId(id: String): PhotoBoothLayout {
            return entries.find { it.id == id } ?: CLASSIC_3_STRIP
        }
    }
}

/**
 * Preset Color Filters for Realtime and Post-Processing Canvas
 */
enum class ColorFilterType(
    val displayName: String,
    val subtitle: String,
    val badgeColor: Long
) {
    NORMAL("Normal", "Alami & Jernih", 0xFF8B5CF6),
    BW_CLASSIC("B&W Classic", "Monokrom Lembut", 0xFF64748B),
    VINTAGE_FILM("Vintage Film", "Nuansa Analog Hangat", 0xFFF59E0B),
    CYBERPUNK("Cyberpunk", "Cool Cyan & Neon", 0xFF06B6D4),
    WARM_RETRO("Warm Retro", "Golden Hour Vibe", 0xFFEC4899),
    SEPIA("Sepia", "Nostalgia 90s", 0xFFD97706),
    NOIR_CONTRAST("Noir Contrast", "Hitam Putih Dramatis", 0xFF1E293B)
}

/**
 * Preset Frame Colors for Canvas Strip Background
 */
data class FrameColorOption(
    val name: String,
    val colorHex: Long,
    val textColorHex: Long,
    val isDark: Boolean
)

val PRESET_FRAME_COLORS = listOf(
    FrameColorOption("Slate Navy", 0xFF0F172A, 0xFFFFFFFF, true),
    FrameColorOption("Vibrant Violet", 0xFF8B5CF6, 0xFFFFFFFF, true),
    FrameColorOption("Studio White", 0xFFFFFFFF, 0xFF0F172A, false),
    FrameColorOption("Cyber Black", 0xFF050508, 0xFFFFFFFF, true),
    FrameColorOption("Neon Sunset", 0xFF2E1065, 0xFFF472B6, true),
    FrameColorOption("Deep Purple", 0xFF4C1D95, 0xFFE9D5FF, true),
    FrameColorOption("Emerald Dark", 0xFF064E3B, 0xFFA7F3D0, true),
    FrameColorOption("Ruby Red", 0xFF831843, 0xFFFBCFE8, true),
    FrameColorOption("Pastel Rose", 0xFFFCE7F3, 0xFF831843, false),
    FrameColorOption("Sage Mint", 0xFFD1FAE5, 0xFF065F46, false),
    FrameColorOption("Vintage Gold", 0xFFFEF3C7, 0xFF78350F, false),
    FrameColorOption("Charcoal Matte", 0xFF18181B, 0xFFFAFAFA, true)
)

val PRESET_STICKERS = listOf(
    "✨", "💖", "📸", "🎉", "🔥", "🎀", "⭐", "🎬", "✌️", "😎", "💐", "🌟"
)

/**
 * Frame Border Styles for Photo Slots and Canvas
 */
enum class FrameBorderStyle(val displayName: String) {
    SOLID("Garis Studio"),
    DOUBLE_LINE("Garis Ganda"),
    POLAROID_BORDER("Polaroid Instan"),
    VINTAGE_GOLD("Emas Klasik Vintage"),
    NEON_GLOW("Neon Cyber Glow"),
    DOTTED_STAMP("Perangko / Dotted"),
    HEART_FRAME("Bingkai Hearts"),
    FLORAL_CORNER("Floral Siku")
}

/**
 * Background Patterns for Canvas Frame
 */
enum class FrameBackgroundPattern(val displayName: String) {
    NONE("Polos Studio"),
    DOTS("Bintik Polkadot"),
    STRIPES("Garis Diagonal"),
    STARS("Taburan Bintang"),
    CHECKERBOARD("Papan Catur Minimalis")
}

/**
 * Image Frame Customization Model
 */
data class ImageFrameStyle(
    val id: String = "clean_white",
    val name: String = "Clean Studio",
    val borderStyle: FrameBorderStyle = FrameBorderStyle.SOLID,
    val borderWidthDp: Float = 4f,
    val borderColorHex: Long = 0xFFFFFFFF,
    val innerBorderColorHex: Long = 0xFF8B5CF6,
    val cornerOrnament: String = "",
    val pattern: FrameBackgroundPattern = FrameBackgroundPattern.NONE,
    val isCustom: Boolean = false
)

val PRESET_IMAGE_FRAMES = listOf(
    ImageFrameStyle(
        id = "clean_white",
        name = "Clean Studio",
        borderStyle = FrameBorderStyle.SOLID,
        borderWidthDp = 4f,
        borderColorHex = 0xFFFFFFFF,
        cornerOrnament = ""
    ),
    ImageFrameStyle(
        id = "vintage_gold",
        name = "Vintage Gold",
        borderStyle = FrameBorderStyle.VINTAGE_GOLD,
        borderWidthDp = 6f,
        borderColorHex = 0xFFD4AF37,
        innerBorderColorHex = 0xFFFEF3C7,
        cornerOrnament = "⚜️"
    ),
    ImageFrameStyle(
        id = "polaroid_classic",
        name = "Polaroid Instan",
        borderStyle = FrameBorderStyle.POLAROID_BORDER,
        borderWidthDp = 10f,
        borderColorHex = 0xFFFFFFFF,
        cornerOrnament = ""
    ),
    ImageFrameStyle(
        id = "neon_cyber",
        name = "Neon Cyber",
        borderStyle = FrameBorderStyle.NEON_GLOW,
        borderWidthDp = 6f,
        borderColorHex = 0xFFEC4899,
        innerBorderColorHex = 0xFF06B6D4,
        cornerOrnament = "✨"
    ),
    ImageFrameStyle(
        id = "cute_hearts",
        name = "Cute Hearts",
        borderStyle = FrameBorderStyle.HEART_FRAME,
        borderWidthDp = 6f,
        borderColorHex = 0xFFF472B6,
        innerBorderColorHex = 0xFFFCE7F3,
        cornerOrnament = "💖"
    ),
    ImageFrameStyle(
        id = "retro_stamp",
        name = "Retro Stamp",
        borderStyle = FrameBorderStyle.DOTTED_STAMP,
        borderWidthDp = 5f,
        borderColorHex = 0xFFE2E8F0,
        cornerOrnament = "🎞️"
    ),
    ImageFrameStyle(
        id = "floral_elegance",
        name = "Floral Elegance",
        borderStyle = FrameBorderStyle.FLORAL_CORNER,
        borderWidthDp = 5f,
        borderColorHex = 0xFF10B981,
        innerBorderColorHex = 0xFFFEF08A,
        cornerOrnament = "💐"
    ),
    ImageFrameStyle(
        id = "starry_night",
        name = "Starry Night",
        borderStyle = FrameBorderStyle.DOUBLE_LINE,
        borderWidthDp = 5f,
        borderColorHex = 0xFF8B5CF6,
        innerBorderColorHex = 0xFFF43F5E,
        cornerOrnament = "⭐",
        pattern = FrameBackgroundPattern.STARS
    )
)

/**
 * Photo Fitting / Scaling Modes inside layout columns and slots
 */
enum class PhotoScaleMode(val displayName: String, val description: String, val iconName: String) {
    FIT_CENTER("Pas di Tengah (Tanpa Terpotong)", "Foto utuh 100% tanpa terpotong, presisi di tengah kolom", "crop_free"),
    CROP_FILL("Isi Penuh Kolom", "Mengisi seluruh area kolom layout secara penuh", "crop"),
    FILL_BOUNDS("Regangkan Sesuai Kolom", "Menyesuaikan foto mengikuti batas bingkai kolom", "aspect_ratio")
}

/**
 * Alignment positioning for photos within layout slots
 */
enum class PhotoAlignment(val displayName: String, val description: String) {
    CENTER("Tengah Presisi", "Posisikan foto pas di tengah kolom (horizontal & vertikal)"),
    TOP("Rata Atas", "Posisikan foto menempel di bagian atas kolom"),
    BOTTOM("Rata Bawah", "Posisikan foto menempel di bagian bawah kolom"),
    START("Rata Kiri", "Posisikan foto menempel di sisi kiri kolom"),
    END("Rata Kanan", "Posisikan foto menempel di sisi kanan kolom")
}

/**
 * Fine-grained slot positioning and scaling adjustment
 */
data class PhotoSlotAdjustment(
    val scaleMode: PhotoScaleMode = PhotoScaleMode.FIT_CENTER,
    val alignment: PhotoAlignment = PhotoAlignment.CENTER,
    val panX: Float = 0f, // -1f (left) to +1f (right)
    val panY: Float = 0f, // -1f (top) to +1f (bottom)
    val zoom: Float = 1.0f, // 0.5f to 2.5f
    val slotBgColorHex: Long = 0xFFFFFFFF
)

/**
 * Output format setting
 */
enum class OutputFormat(val extension: String, val mimeType: String) {
    JPEG("jpg", "image/jpeg"),
    PNG("png", "image/png")
}

/**
 * Export quality resolution
 */
enum class ExportQuality(val widthPx: Int, val label: String) {
    HD_1080P(1080, "HD (1080p)"),
    ULTRA_HD_4K(2160, "Ultra HD (4K)")
}

/**
 * Print paper sizes for Photo Booth printing
 */
enum class PrintPaperSize(val displayName: String, val description: String) {
    PHOTO_4x6("Foto 4x6 inci (10x15 cm)", "Ukuran standar kertas foto kiosk"),
    PHOTO_2x6_DUAL("Dual Strip 2x6 inci", "2 strip berdampingan di kertas 4x6"),
    PAPER_A4("Kertas A4 (21x29.7 cm)", "Grid multiple photo strip"),
    LETTER("Kertas Letter (8.5x11 inci)", "Ukuran standar dokumen")
}

/**
 * Loop Animation Playback Modes
 */
enum class LoopMode(val displayName: String, val description: String, val iconName: String) {
    BOOMERANG("Boomerang", "1 → 2 → 3 → 4 → 3 → 2 → 1 (Bolak-Balik)", "swap_horiz"),
    FORWARD("Putar Maju", "1 → 2 → 3 → 4 → 1 (Terus Berputar)", "repeat"),
    DISCO_BURST("Disco Flash", "Kilatan cepat dan ritme dinamis", "flash_on"),
    VINTAGE_STOPMOTION("Stop-Motion", "Karakter getar analog retro 35mm", "movie")
}

/**
 * Loop Animation Speeds
 */
enum class LoopSpeed(val displayName: String, val delayMs: Long, val multiplierText: String) {
    SLOW("Santai", 500L, "0.5x"),
    NORMAL("Normal", 320L, "1.0x"),
    FAST("Cepat", 200L, "1.5x"),
    HYPER("Hyper", 120L, "2.0x")
}

/**
 * Loop Animation Visual Transitions
 */
enum class LoopTransition(val displayName: String, val description: String) {
    CROSSFADE("Crossfade", "Transisi memudar lembut antar frame"),
    ZOOM_PULSE("Zoom Pulse", "Denyut beat membesar saat frame berganti"),
    SHUTTER_FLASH("Shutter Flash", "Kilatan rana kamera retro putih"),
    SLIDE_HORIZONTAL("Slide", "Efek geser film strip menyamping")
}

/**
 * Realtime Particle FX Overlays for Loop Animation
 */
enum class LoopParticleEffect(val displayName: String, val emoji: String) {
    NONE("Tanpa Efek", "🚫"),
    SPARKLES("Kilau Bintang ✨", "✨"),
    HEARTS("Hati Cinta 💖", "💖"),
    FILM_DUST("Debu Film 🎞️", "🎞️"),
    CONFETTI("Pesta Confetti 🎉", "🎉")
}

/**
 * Configuration for Loop Animation
 */
data class LoopAnimationConfig(
    val mode: LoopMode = LoopMode.BOOMERANG,
    val speed: LoopSpeed = LoopSpeed.FAST,
    val transition: LoopTransition = LoopTransition.CROSSFADE,
    val particleEffect: LoopParticleEffect = LoopParticleEffect.SPARKLES,
    val isPlaying: Boolean = true,
    val showStickerOverlay: Boolean = true,
    val showFrameBorder: Boolean = true
)

/**
 * Welcome Screen Color Themes
 */
enum class WelcomeTheme(
    val displayName: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val accentColorHex: Long,
    val description: String
) {
    ANIME_WALLPAPER(
        displayName = "Wallpaper Anime",
        primaryColorHex = 0xFFA78BFA,
        secondaryColorHex = 0xFFEC4899,
        accentColorHex = 0xFF8B5CF6,
        description = "Tema anggun eksklusif dengan wallpaper anime yang diunggah"
    ),
    NEON_STUDIO(
        displayName = "Neon Studio",
        primaryColorHex = 0xFF8B5CF6,
        secondaryColorHex = 0xFFEC4899,
        accentColorHex = 0xFF06B6D4,
        description = "Violet elektrik & neon pink modern"
    ),
    RETRO_SUNSET(
        displayName = "Retro Sunset",
        primaryColorHex = 0xFFF59E0B,
        secondaryColorHex = 0xFFEF4444,
        accentColorHex = 0xFFF43F5E,
        description = "Nuansa senja oranye amber hangat"
    ),
    PASTEL_DREAM(
        displayName = "Pastel Dream",
        primaryColorHex = 0xFFF472B6,
        secondaryColorHex = 0xFFA78BFA,
        accentColorHex = 0xFF38BDF8,
        description = "Sakura manis & lavender lembut"
    ),
    MIDNIGHT_CYAN(
        displayName = "Midnight Cyan",
        primaryColorHex = 0xFF06B6D4,
        secondaryColorHex = 0xFF10B981,
        accentColorHex = 0xFF3B82F6,
        description = "Cyan futuristik & emerald neon"
    ),
    VINTAGE_GOLD(
        displayName = "Vintage Gold",
        primaryColorHex = 0xFFD97706,
        secondaryColorHex = 0xFF92400E,
        accentColorHex = 0xFFFDE68A,
        description = "Emas klasik mewah & retro 70s"
    )
}

/**
 * Welcome Screen Center Hero Visual Style
 */
enum class WelcomeHeroStyle(val displayName: String, val description: String) {
    DUAL_STRIP_MOCKUP("Dual Photo Strip", "Dua strip foto retro miring dengan lensa kamera bercahaya"),
    SINGLE_POLAROID("Single Polaroid", "Satu foto polaroid klasik vintage berbingkai putih"),
    MINIMAL_SHUTTER("Minimal Shutter Lens", "Emblem lensa kamera fokus minimalis bercahaya aura"),
    LOOP_ANIMATION("Loop Frame Manager", "Bingkai loop animasi pilihan dari Manajer Loop")
}

/**
 * Welcome Screen Loop Animation Item
 */
data class LoopAnimationItem(
    val id: String = "preset_photo_reel",
    val name: String = "Photo Reel Studio",
    val mediaUri: String = "",
    val type: LoopMediaType = LoopMediaType.BUILTIN_PHOTO_REEL,
    val mimeType: String = "auto",
    val isPreset: Boolean = true,
    val speedRatio: Float = 1.0f,
    val description: String = "Loop strip foto melayang interaktif dengan cahaya shutter kamera"
)

/**
 * Loop Media Type Classification
 */
enum class LoopMediaType(val displayName: String, val iconEmoji: String) {
    THEME_WALLPAPER("Wallpaper Tema Anime", "🖼️"),
    BUILTIN_PHOTO_REEL("Photo Reel Studio", "🎞️"),
    SYNTHWAVE_PARTICLES("Neon Synthwave", "🌌"),
    CINEMATIC_BOKEH("Cinematic Bokeh", "✨"),
    EXTERNAL_GIF("Animasi GIF", "🖼️"),
    EXTERNAL_MP4("Video Loop MP4", "🎬")
}

/**
 * Welcome Screen Loop Animation Placement
 */
enum class WelcomeLoopPlacement(val displayName: String, val description: String) {
    BACKGROUND_FULL("Latar Penuh (Full Screen)", "Animasi loop memenuhi latar belakang seluruh layar Welcome"),
    HERO_FRAME("Bingkai Hero Tengah", "Animasi loop ditampilkan di dalam bingkai kartu hero tengah Welcome Screen"),
    SPLIT_CARD("Kartu Split Side-by-Side", "Animasi loop berdampingan dengan pesan sambutan studio"),
    NONE("Nonaktif", "Tanpa animasi loop")
}

/**
 * Welcome Screen Video / GIF Display Scaling Mode
 */
enum class WelcomeMediaScale(val displayName: String, val description: String) {
    CROP("Zoom Crop (Proporsional)", "Perbesar memenuhi layar secara proporsional dan potong tepi berlebih"),
    FIT("Fit (Utuh)", "Tampilkan video/gif secara utuh tanpa terpotong (aspek rasio asli)"),
    FILL("Fill (Regang)", "Regangkan video/gif memenuhi seluruh area tanpa terpotong")
}

/**
 * Ambient Background Particle Style for Welcome Screen
 */
enum class WelcomeParticle(val displayName: String, val emoji: String) {
    STARS("Bintang ✨", "✨"),
    CONFETTI("Confetti 🎉", "🎉"),
    HEARTS("Hati 💖", "💖"),
    NONE("Tanpa Partikel 🚫", "🚫")
}

/**
 * App Settings Persisted via Jetpack DataStore
 */
data class AppSettings(
    val defaultCountdownSeconds: Int = 3,
    val burstDelaySeconds: Int = 2,
    val isShutterSoundEnabled: Boolean = true,
    val isScreenFlashEnabled: Boolean = true,
    val defaultUseFrontCamera: Boolean = true,
    val outputFormat: OutputFormat = OutputFormat.JPEG,
    val exportQuality: ExportQuality = ExportQuality.HD_1080P,
    val eventTitle: String = "PHOTO BOOTH 2026",
    val eventSubtitle: String = "SWEET MEMORIES ✨",
    val isKioskMode: Boolean = false,
    val adminPin: String = "1234",
    val cloudinaryCloudName: String = "jggtvymn",
    val cloudinaryApiKey: String = "865291324538569",
    val cloudinaryApiSecret: String = "DdFr-TgDz8NTwz91psJSuvP1i5c",
    val cloudinaryFolder: String = "photobooth",
    val qrisPaymentUrl: String = "https://res.cloudinary.com/jggtvymn/image/upload/photobooth_qris",
    val useCustomQrisImage: Boolean = false,
    val customQrisImagePath: String = "",
    val defaultPrintCopies: Int = 1,
    val autoPrintOnSessionEnd: Boolean = false,
    val defaultPaperSize: PrintPaperSize = PrintPaperSize.PHOTO_4x6,
    val duplicateDualStrip: Boolean = true,
    val defaultLoopMode: LoopMode = LoopMode.BOOMERANG,
    val defaultLoopSpeed: LoopSpeed = LoopSpeed.FAST,
    val defaultLoopParticle: LoopParticleEffect = LoopParticleEffect.SPARKLES,
    val autoPreviewLoop: Boolean = false,
    // Welcome Screen Customization Settings
    val welcomeTitle: String = "PHOTO BOOTH",
    val welcomeTagline: String = "STUDIO MEMORIES",
    val welcomeDescription: String = "Abadikan setiap senyuman dalam strip foto klasik & animasi loop modern.",
    val welcomeButtonText: String = "TAP TO START",
    val welcomeTheme: WelcomeTheme = WelcomeTheme.NEON_STUDIO,
    val welcomeHeroStyle: WelcomeHeroStyle = WelcomeHeroStyle.DUAL_STRIP_MOCKUP,
    val welcomeParticle: WelcomeParticle = WelcomeParticle.STARS,
    val showWelcomeFeatures: Boolean = true,
    val showWelcomeTopBadge: Boolean = true,
    val enablePulsingAnimation: Boolean = true,
    val isWelcomeLoopEnabled: Boolean = true,
    val activeLoopAnimationId: String = "preset_photo_reel",
    val welcomeLoopPlacement: WelcomeLoopPlacement = WelcomeLoopPlacement.BACKGROUND_FULL,
    val welcomeLoopBackgroundDim: Float = 0.35f,
    val isWelcomeLoopSoundMuted: Boolean = true,
    val welcomeMediaScale: WelcomeMediaScale = WelcomeMediaScale.CROP,
    val enableAutoRotateLoopPlaylist: Boolean = false,
    val rotateIntervalSeconds: Int = 10,
    val defaultPhotoScaleMode: PhotoScaleMode = PhotoScaleMode.CROP_FILL,
    val defaultPhotoAlignment: PhotoAlignment = PhotoAlignment.CENTER,
    val defaultSlotBgColorHex: Long = 0xFFFFFFFF,
    val isAppPinned: Boolean = false,
    val isImmersiveFullscreen: Boolean = true,
    val preventExitWithPin: Boolean = true,
    // Global UI Theme Wallpaper Background
    val useCustomThemeBackground: Boolean = true,
    val customThemeBackgroundDim: Float = 0.25f,
    val customThemeBackgroundPath: String = "",
    val gridColumnCount: Int = 2
)

/**
 * Cloudinary Upload Status
 */
sealed class CloudinaryUploadState {
    object Idle : CloudinaryUploadState()
    object Uploading : CloudinaryUploadState()
    data class Success(val downloadUrl: String, val publicId: String) : CloudinaryUploadState()
    data class Error(val message: String) : CloudinaryUploadState()
}

/**
 * Save / Export Status UI State
 */
sealed class SaveStatus {
    object Idle : SaveStatus()
    object Saving : SaveStatus()
    data class Success(val fileName: String, val filePath: String) : SaveStatus()
    data class Error(val message: String) : SaveStatus()
}

/**
 * Rectangular slot definition in normalized coordinates [0f..1f] for transparent cutouts in PNG layouts
 */
data class PngSlotRect(
    val left: Float = 0f,
    val top: Float = 0f,
    val right: Float = 1f,
    val bottom: Float = 1f,
    val index: Int = 0
)

/**
 * Configuration for user-designed custom layouts and external PNG overlay layouts
 */
data class CustomLayoutConfig(
    val id: String = "custom_layout",
    val name: String = "Layout Custom Saya",
    val photoCount: Int = 4,
    val columns: Int = 2,
    val photoAspectRatio: Float = 0.75f, // 4:3 portrait ratio
    val cornerRadiusDp: Float = 16f,
    val frameColorHex: Long = 0xFF0F172A,
    val textColorHex: Long = 0xFFFFFFFF,
    val eventTitle: String = "CUSTOM BOOTH STUDIO",
    val captionText: String = "DESAIN FOTO CUSTOM ✨",
    val showDate: Boolean = true,
    val stickerEmoji: String = "🎨",
    val photoScaleMode: PhotoScaleMode = PhotoScaleMode.FIT_CENTER,
    val photoAlignment: PhotoAlignment = PhotoAlignment.CENTER,
    val photoPanX: Float = 0f,
    val photoPanY: Float = 0f,
    val photoZoom: Float = 1.0f,
    val slotBgColorHex: Long = 0xFFFFFFFF,
    val bgImagePath: String = "",
    val isPngOverlay: Boolean = false,
    val pngOverlayPath: String = "",
    val pngSlots: List<PngSlotRect> = emptyList(),
    val overlayAspectRatio: Float = 1.5f // height / width
)

/**
 * Primary UI State for Photo Booth Session
 */
data class PhotoBoothUiState(
    val currentScreen: AppScreen = AppScreen.WELCOME,
    val selectedLayout: PhotoBoothLayout = PhotoBoothLayout.CLASSIC_3_STRIP,
    val isCustomLayoutActive: Boolean = false,
    val customLayoutConfig: CustomLayoutConfig = CustomLayoutConfig(),
    val showCustomLayoutDesigner: Boolean = false,
    val editingCustomLayoutConfig: CustomLayoutConfig? = null,
    val savedCustomLayouts: List<CustomLayoutConfig> = emptyList(),
    val capturedPhotos: List<Bitmap> = emptyList(),
    val originalCapturedPhotos: List<Bitmap> = emptyList(),
    val showPhotoEditorDialog: Boolean = false,
    val editingPhotoIndex: Int = 0,
    val currentSlotIndex: Int = 0,
    val isSequenceRunning: Boolean = false,
    val countdownNumber: Int = 0,
    val isCountingDown: Boolean = false,
    val isTakingShot: Boolean = false,
    val showFlashScreen: Boolean = false,
    val selectedCameraSource: CameraSource = CameraSource.FRONT,
    val isTorchOn: Boolean = false,
    val selectedTimerDuration: Int = 3,
    val selectedFilter: ColorFilterType = ColorFilterType.NORMAL,
    val brightness: Float = 0f, // -1f to 1f
    val contrast: Float = 1f,   // 0.5f to 2f
    val skinSmoothing: Float = 0f, // 0f to 1f
    val selectedFrameColor: FrameColorOption = PRESET_FRAME_COLORS[0],
    val selectedImageFrame: ImageFrameStyle = PRESET_IMAGE_FRAMES[0],
    val customImageFrameConfig: ImageFrameStyle = ImageFrameStyle(id = "custom_frame", name = "Bingkai Custom Saya", isCustom = true),
    val showCustomFrameDesigner: Boolean = false,
    val savedCustomFrames: List<ImageFrameStyle> = emptyList(),
    // Photo Positioning & Column Scaling Controls
    val photoScaleMode: PhotoScaleMode = PhotoScaleMode.CROP_FILL,
    val photoAlignment: PhotoAlignment = PhotoAlignment.CENTER,
    val photoPanX: Float = 0f, // -1f .. +1f
    val photoPanY: Float = 0f, // -1f .. +1f
    val photoZoom: Float = 1.0f, // 0.5f .. 2.5f
    val slotBgColorHex: Long = 0xFFFFFFFF,
    val perSlotAdjustments: Map<Int, PhotoSlotAdjustment> = emptyMap(),
    val captionText: String = "SWEET MEMORIES ✨",
    val showDate: Boolean = true,
    val selectedSticker: String = "✨",
    val useCustomCanvasImage: Boolean = false,
    val customCanvasImagePath: String = "",
    val finalStripBitmap: Bitmap? = null,
    val isRenderingStrip: Boolean = false,
    val saveStatus: SaveStatus = SaveStatus.Idle,
    val loopConfig: LoopAnimationConfig = LoopAnimationConfig(),
    val isLoopViewActive: Boolean = false,
    val isGeneratingGif: Boolean = false,
    val gifSaveStatus: SaveStatus = SaveStatus.Idle,
    val isGeneratingMp4: Boolean = false,
    val mp4SaveStatus: SaveStatus = SaveStatus.Idle,
    val showFullscreenLoop: Boolean = false,
    val showLoopAnimationManager: Boolean = false,
    val savedLoopAnimations: List<LoopAnimationItem> = emptyList(),
    val showQrCodeDialog: Boolean = false,
    val retakeSlotIndex: Int? = null,
    val cloudinaryUploadState: CloudinaryUploadState = CloudinaryUploadState.Idle,
    val uploadedCloudinaryUrl: String? = null,
    val settings: AppSettings = AppSettings()
) {
    val isFrontCamera: Boolean
        get() = selectedCameraSource == CameraSource.FRONT

    val activePhotoCount: Int
        get() = if (isCustomLayoutActive) customLayoutConfig.photoCount else selectedLayout.photoCount

    val activeLayoutDisplayName: String
        get() = if (isCustomLayoutActive) customLayoutConfig.name else selectedLayout.displayName
}

enum class CameraSource {
    FRONT,
    BACK
}
