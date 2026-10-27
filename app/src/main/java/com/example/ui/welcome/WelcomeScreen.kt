package com.example.ui.welcome

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.R
import com.example.model.LoopAnimationItem
import com.example.model.LoopMediaType
import com.example.model.PhotoBoothUiState
import com.example.model.WelcomeHeroStyle
import com.example.model.WelcomeLoopPlacement
import com.example.model.WelcomeMediaScale
import com.example.model.WelcomeParticle
import com.example.model.WelcomeTheme
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioNeonCyan
import com.example.ui.theme.StudioNeonPink
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioViolet
import com.example.util.LoopAnimationRepository

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

/**
 * Modern Kiosk Attract / Welcome Screen with dynamic themes, loop animation background,
 * floating photo strips, ambient particle effects, and quick settings access.
 */
@Composable
fun WelcomeScreen(
    uiState: PhotoBoothUiState,
    onStart: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLoopManager: (() -> Unit)? = null,
    onSelectLoopAnimation: ((LoopAnimationItem) -> Unit)? = null,
    onImportLocalFile: ((Uri) -> Unit)? = null,
    onAddUrlMedia: ((String) -> Unit)? = null,
    onDeleteAnimation: ((LoopAnimationItem) -> Unit)? = null,
    onUpdatePlacement: ((WelcomeLoopPlacement) -> Unit)? = null,
    onUpdateMediaScale: ((WelcomeMediaScale) -> Unit)? = null,
    onUpdateDimScrim: ((Float) -> Unit)? = null,
    onUpdateMuteSound: ((Boolean) -> Unit)? = null,
    onUpdateAutoRotate: ((Boolean) -> Unit)? = null,
    onCloseLoopManager: (() -> Unit)? = null,
    onCycleTheme: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Immersive fullscreen handling
    LaunchedEffect(Unit) {
        val activity = context.findActivity()
        val window = activity?.window
        if (window != null) {
            try {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val welcomeSettings = uiState.settings
    val currentTheme = welcomeSettings.welcomeTheme

    // Theme Color Palette
    val primaryThemeColor = Color(currentTheme.primaryColorHex)
    val secondaryThemeColor = Color(currentTheme.secondaryColorHex)
    val accentThemeColor = Color(currentTheme.accentColorHex)

    val backgroundGradient = when (currentTheme) {
        WelcomeTheme.ANIME_WALLPAPER -> listOf(Color(0xFF0F0824), Color(0xFF1B0F38), Color(0xFF090B1C))
        WelcomeTheme.NEON_STUDIO -> listOf(Color(0xFF0F051D), Color(0xFF1E0B36), Color(0xFF080C1E))
        WelcomeTheme.RETRO_SUNSET -> listOf(Color(0xFF2A0818), Color(0xFF3B1024), Color(0xFF12050E))
        WelcomeTheme.PASTEL_DREAM -> listOf(Color(0xFF180E29), Color(0xFF26143F), Color(0xFF0E0B20))
        WelcomeTheme.MIDNIGHT_CYAN -> listOf(Color(0xFF031424), Color(0xFF0A2540), Color(0xFF030D1A))
        WelcomeTheme.VINTAGE_GOLD -> listOf(Color(0xFF1C1405), Color(0xFF2A1C06), Color(0xFF0D0A02))
    }

    val infiniteTransition = rememberInfiniteTransition(label = "welcome_animations")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_offset"
    )

    // Smooth fade-in/fade-out animation for the 'Tap to Start' camera icon
    val cameraIconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "camera_icon_fade"
    )

    // Active Loop Animation
    val activeAnimation = remember(uiState.savedLoopAnimations, welcomeSettings.activeLoopAnimationId) {
        uiState.savedLoopAnimations.firstOrNull { it.id == welcomeSettings.activeLoopAnimationId }
            ?: LoopAnimationRepository.PRESET_PHOTO_REEL
    }

    // Always use gradient background for Welcome Screen since global theme is disabled for this screen
    val bgModifier = modifier.fillMaxSize().background(Brush.verticalGradient(backgroundGradient))

    BoxWithConstraints(
        modifier = bgModifier.testTag("welcome_screen_root")
    ) {
        val maxHeight = constraints.maxHeight
        val maxWidth = constraints.maxWidth
        val isCompactHeight = maxHeight < 700
        val isTablet = maxWidth > 800

        val horizontalPadding = if (isTablet) 48.dp else 20.dp
        val verticalPadding = if (isCompactHeight) 8.dp else 14.dp

        // 1. Fullscreen Loop Background (Always shown on Welcome Screen)
        LoopMediaViewer(
            item = activeAnimation,
            primaryColor = primaryThemeColor,
            secondaryColor = secondaryThemeColor,
            mediaScale = WelcomeMediaScale.CROP,
            isMuted = welcomeSettings.isWelcomeLoopSoundMuted,
            modifier = Modifier.fillMaxSize()
        )

        // Dim Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = welcomeSettings.welcomeLoopBackgroundDim))
        )

        // Ambient Particle Canvas (Stars / Sparkles)
        if (welcomeSettings.welcomeParticle != WelcomeParticle.NONE) {
            WelcomeAmbientParticles(
                particleType = welcomeSettings.welcomeParticle,
                primaryColor = primaryThemeColor,
                secondaryColor = secondaryThemeColor,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Main Content Layout with dynamic scaling
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP CONTENT GROUP (Pushed to the top)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // TOP BAR: Studio Badge, Live Mirror Quick Toggle, Theme Switcher & Settings Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Top Badge
                    if (welcomeSettings.showWelcomeTopBadge) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Black.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, primaryThemeColor.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(secondaryThemeColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "STUDIO PRO 2026",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // Top Actions: Theme Switcher, Loop Animation & Settings Button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Loop Animation Button (converted to Icon Button)
                        Surface(
                            onClick = { onOpenLoopManager?.invoke() },
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("welcome_loop_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Loop Manager: ${activeAnimation.name}",
                                    tint = primaryThemeColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            onClick = { onCycleTheme?.invoke() },
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Ganti Tema",
                                    tint = secondaryThemeColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            onClick = onOpenSettings,
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("welcome_settings_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // CENTER HERO SECTION: Clean Title & Subtitle (Center Logo / Mockup Cleared)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    // Title
                    Text(
                        text = welcomeSettings.welcomeTitle,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            shadow = Shadow(
                                color = primaryThemeColor.copy(alpha = 0.7f),
                                offset = Offset(0f, 4f),
                                blurRadius = 12f
                            )
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Tagline / Subtitle
                    Text(
                        text = welcomeSettings.welcomeTagline,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = secondaryThemeColor,
                        textAlign = TextAlign.Center
                    )

                    if (welcomeSettings.showWelcomeFeatures) {
                        Spacer(modifier = Modifier.height(18.dp))
                        // Feature Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            WelcomeFeatureBadge("4-Pose Strip", Icons.Default.CameraAlt, primaryThemeColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            WelcomeFeatureBadge("QR Instant", Icons.Default.QrCode, secondaryThemeColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            WelcomeFeatureBadge("Loop Motion", Icons.Default.Movie, StudioEmerald)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "💡 Ketuk tombol di bawah untuk mulai memilih template & berpose",
                            fontSize = 11.sp,
                            color = StudioTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // BOTTOM ACTION SECTION: Start Button & Loop Manager Quick Access
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                // PRIMARY "TAP TO START" BUTTON
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Glowing Neon Aura
                    if (welcomeSettings.enablePulsingAnimation) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .scale(pulseScale)
                                .clip(RoundedCornerShape(32.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            primaryThemeColor.copy(alpha = glowAlpha),
                                            secondaryThemeColor.copy(alpha = glowAlpha)
                                        )
                                    )
                                )
                                .blur(12.dp)
                        )
                    }

                    Surface(
                        onClick = onStart,
                        shape = RoundedCornerShape(32.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.8f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        primaryThemeColor,
                                        Color(0xFF8B5CF6),
                                        secondaryThemeColor
                                    )
                                )
                            )
                            .testTag("welcome_tap_to_start_button")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .size(24.dp)
                                    .alpha(cameraIconAlpha)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = welcomeSettings.welcomeButtonText,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // 3. Loop Animation Manager Dialog
        if (uiState.showLoopAnimationManager) {
            LoopAnimationManagerDialog(
                settings = uiState.settings,
                savedAnimations = uiState.savedLoopAnimations,
                activeAnimation = activeAnimation,
                onSelectAnimation = { item ->
                    onSelectLoopAnimation?.invoke(item)
                },
                onImportLocalFile = { uri ->
                    onImportLocalFile?.invoke(uri)
                },
                onAddUrlMedia = { url ->
                    onAddUrlMedia?.invoke(url)
                },
                onDeleteAnimation = { item ->
                    onDeleteAnimation?.invoke(item)
                },
                onUpdatePlacement = { plc ->
                    onUpdatePlacement?.invoke(plc)
                },
                onUpdateMediaScale = { scale ->
                    onUpdateMediaScale?.invoke(scale)
                },
                onUpdateDimScrim = { dim ->
                    onUpdateDimScrim?.invoke(dim)
                },
                onUpdateMuteSound = { muted ->
                    onUpdateMuteSound?.invoke(muted)
                },
                onUpdateAutoRotate = { auto ->
                    onUpdateAutoRotate?.invoke(auto)
                },
                onDismiss = {
                    onCloseLoopManager?.invoke()
                }
            )
        }
    }
}

@Composable
private fun WelcomeFeatureBadge(
    text: String,
    icon: ImageVector,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun WelcomeDualStripHero(
    primaryColor: Color,
    secondaryColor: Color,
    floatingOffset: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Strip 1 (Left tilted)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(2.dp, primaryColor),
            modifier = Modifier
                .width(100.dp)
                .height(175.dp)
                .rotate(-7f)
                .scale(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF334155), Color(0xFF1E293B))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = primaryColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = "PHOTO BOOTH",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        // Strip 2 (Right tilted & Floating)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(2.dp, secondaryColor),
            modifier = Modifier
                .width(108.dp)
                .height(185.dp)
                .rotate(8f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = secondaryColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = "VIP MEMORIES",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = secondaryColor
                )
            }
        }
    }
}

@Composable
private fun WelcomeAmbientParticles(
    particleType: WelcomeParticle,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val count = 24
        for (i in 0 until count) {
            val progress = (phase + (i.toFloat() / count)) % 1f
            val x = (size.width * ((i * 37) % 100) / 100f)
            val y = size.height * (1f - progress)
            val alpha = (1f - progress) * 0.6f
            val radius = 2.5f + (i % 4) * 1.5f
            val color = if (i % 2 == 0) primaryColor else secondaryColor

            drawCircle(
                color = color.copy(alpha = alpha),
                radius = radius,
                center = Offset(x, y)
            )
        }
    }
}
