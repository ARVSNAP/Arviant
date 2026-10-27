package com.example.ui.welcome

import android.os.Build
import android.widget.VideoView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.R
import com.example.model.LoopAnimationItem
import com.example.model.LoopMediaType
import com.example.model.WelcomeMediaScale
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioNeonCyan
import com.example.ui.theme.StudioNeonPink
import com.example.ui.theme.StudioViolet
import java.io.File
import kotlin.math.sin

@Composable
fun LoopMediaViewer(
    item: LoopAnimationItem?,
    primaryColor: Color = StudioNeonCyan,
    secondaryColor: Color = StudioNeonPink,
    mediaScale: WelcomeMediaScale = WelcomeMediaScale.CROP,
    isMuted: Boolean = true,
    isHeroStyle: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contentScale = when (mediaScale) {
        WelcomeMediaScale.FIT -> ContentScale.Fit
        WelcomeMediaScale.FILL -> ContentScale.FillBounds
        WelcomeMediaScale.CROP -> ContentScale.Crop
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (item?.type) {
            LoopMediaType.THEME_WALLPAPER -> {
                ThemeWallpaperLoop(
                    mediaUri = item.mediaUri,
                    mediaScale = mediaScale,
                    modifier = Modifier.fillMaxSize()
                )
            }

            LoopMediaType.SYNTHWAVE_PARTICLES -> {
                SynthwaveParticleCanvas(
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    modifier = Modifier.fillMaxSize()
                )
            }

            LoopMediaType.CINEMATIC_BOKEH -> {
                CinematicBokehCanvas(
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    modifier = Modifier.fillMaxSize()
                )
            }

            LoopMediaType.EXTERNAL_GIF -> {
                val imageLoader = remember(context) {
                    ImageLoader.Builder(context)
                        .components {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                add(ImageDecoderDecoder.Factory())
                            } else {
                                add(GifDecoder.Factory())
                            }
                        }
                        .build()
                }

                val mediaData = remember(item.mediaUri) {
                    if (item.mediaUri.startsWith("/")) File(item.mediaUri) else item.mediaUri
                }

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(mediaData)
                        .crossfade(true)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = item.name,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            }

            LoopMediaType.EXTERNAL_MP4 -> {
                DisposableEffect(item.mediaUri, isMuted) {
                    onDispose { }
                }

                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setVideoPath(item.mediaUri)
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                if (isMuted) {
                                    mp.setVolume(0f, 0f)
                                } else {
                                    mp.setVolume(1f, 1f)
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    mp.setVideoScalingMode(android.media.MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                                }
                                start()
                            }
                            setOnErrorListener { _, _, _ -> true }
                        }
                    },
                    update = { videoView ->
                        if (!videoView.isPlaying) {
                            videoView.setVideoPath(item.mediaUri)
                            videoView.start()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                // BUILTIN_PHOTO_REEL (Default Photo Reel Studio)
                PhotoReelStudioLoop(
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    isHeroStyle = isHeroStyle,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Interactive Multi-Photo Strip Reel Loop Animation with Film Borders and Flash Effects
 */
@Composable
fun PhotoReelStudioLoop(
    primaryColor: Color,
    secondaryColor: Color,
    isHeroStyle: Boolean = false,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "photo_reel_loop")

    val reelYOffset by transition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reel_y"
    )

    val rotateAngle by transition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reel_rotate"
    )

    val flashAlpha by transition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flash_alpha"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background Glowing Studio Ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(size.width * 0.5f, size.height * 0.5f)
            val radius = size.width.coerceAtMost(size.height) * 0.38f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent),
                    center = centerOffset,
                    radius = radius * 1.5f
                )
            )
        }

        // Animated Dual Photo Reel Strips (Clean Center)
        Row(
            modifier = Modifier
                .offset(y = reelYOffset.dp)
                .rotate(rotateAngle),
            horizontalArrangement = Arrangement.spacedBy(if (isHeroStyle) 24.dp else 36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Strip Left
            PhotoStripCardMock(
                rotation = -8f,
                accentColor = primaryColor,
                isHeroStyle = isHeroStyle
            )

            // Strip Right
            PhotoStripCardMock(
                rotation = 8f,
                accentColor = secondaryColor,
                isHeroStyle = isHeroStyle
            )
        }
    }
}

@Composable
private fun PhotoStripCardMock(
    rotation: Float,
    accentColor: Color,
    isHeroStyle: Boolean
) {
    Surface(
        modifier = Modifier
            .width(if (isHeroStyle) 85.dp else 110.dp)
            .height(if (isHeroStyle) 170.dp else 220.dp)
            .rotate(rotation),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E1E28),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, accentColor.copy(alpha = 0.7f)),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 3 Mock photo frames
            repeat(3) { idx ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isHeroStyle) 42.dp else 56.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.25f + idx * 0.1f),
                                    Color(0xFF2C2C3E)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (idx % 2 == 0) Icons.Default.AutoAwesome else Icons.Default.Star,
                        contentDescription = null,
                        tint = accentColor.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Footer Logo
            Text(
                text = "STUDIO BOOTH",
                fontSize = if (isHeroStyle) 7.sp else 9.sp,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
        }
    }
}

/**
 * Neon Synthwave Grid & Wave Canvas Loop
 */
@Composable
fun SynthwaveParticleCanvas(
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "synthwave_loop")
    val waveOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Radial Neon Sun Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(secondaryColor.copy(alpha = 0.35f), StudioViolet.copy(alpha = 0.1f), Color.Transparent),
                center = Offset(width * 0.5f, height * 0.4f),
                radius = width * 0.6f
            )
        )

        // Animated Wave Grid
        val path = Path()
        val steps = 30
        val stepX = width / steps

        path.moveTo(0f, height * 0.5f)
        for (i in 0..steps) {
            val x = i * stepX
            val y = height * 0.5f + sin(waveOffset + i * 0.3f) * 25f
            path.lineTo(x, y)
        }
        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.close()

        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.25f), Color.Transparent)
            )
        )
    }
}

/**
 * Cinematic Golden Bokeh Canvas Loop
 */
@Composable
fun CinematicBokehCanvas(
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "bokeh_loop")
    val pulseScale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bokeh_pulse"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Soft ambient bokeh circles
        val circles = listOf(
            Triple(0.2f, 0.3f, 0.35f),
            Triple(0.8f, 0.25f, 0.40f),
            Triple(0.5f, 0.65f, 0.45f),
            Triple(0.15f, 0.8f, 0.30f),
            Triple(0.85f, 0.75f, 0.35f)
        )

        circles.forEachIndexed { idx, (cx, cy, rRatio) ->
            val color = if (idx % 2 == 0) StudioAmber else primaryColor
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(width * cx, height * cy),
                    radius = (width * rRatio) * pulseScale
                )
            )
        }
    }
}

/**
 * Fullscreen Theme Wallpaper loop with high resolution rendering and optional custom image path.
 */
@Composable
fun ThemeWallpaperLoop(
    mediaUri: String = "",
    mediaScale: WelcomeMediaScale = WelcomeMediaScale.CROP,
    modifier: Modifier = Modifier
) {
    val contentScale = when (mediaScale) {
        WelcomeMediaScale.FIT -> ContentScale.Fit
        WelcomeMediaScale.FILL -> ContentScale.FillBounds
        WelcomeMediaScale.CROP -> ContentScale.Crop
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (mediaUri.isNotBlank()) {
            val file = remember(mediaUri) {
                if (mediaUri.startsWith("/")) File(mediaUri) else null
            }
            AsyncImage(
                model = file ?: mediaUri,
                contentDescription = "Theme Wallpaper",
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.img_theme_bg),
                contentDescription = "Theme Wallpaper",
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

