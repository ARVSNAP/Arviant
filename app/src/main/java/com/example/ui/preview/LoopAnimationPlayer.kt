package com.example.ui.preview

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ColorFilterType
import com.example.model.FrameColorOption
import com.example.model.LoopAnimationConfig
import com.example.model.LoopMode
import com.example.model.LoopParticleEffect
import com.example.model.LoopSpeed
import com.example.model.LoopTransition
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioNeonCyan
import com.example.ui.theme.StudioNeonPink
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceGlass
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioViolet
import com.example.ui.theme.StudioVioletLight
import com.example.util.BitmapUtils
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Interactive Loop Animation Player for Photo Booth photos.
 * Plays the captured photos in an animated loop (Boomerang or Forward),
 * with customizable speeds, transitions, overlays, and GIF export.
 */
@Composable
fun LoopAnimationPlayer(
    photos: List<Bitmap>,
    filter: ColorFilterType,
    frameColor: FrameColorOption,
    captionText: String,
    stickerEmoji: String,
    loopConfig: LoopAnimationConfig,
    isGeneratingGif: Boolean,
    isGeneratingMp4: Boolean = false,
    onSetLoopMode: (LoopMode) -> Unit,
    onSetLoopSpeed: (LoopSpeed) -> Unit,
    onSetLoopTransition: (LoopTransition) -> Unit,
    onSetLoopParticle: (LoopParticleEffect) -> Unit,
    onTogglePlay: () -> Unit,
    onSaveGif: () -> Unit,
    onShareGif: () -> Unit,
    onSaveMp4: () -> Unit = {},
    onShareMp4: () -> Unit = {},
    onShareToSpecificApp: (String, String) -> Unit = { _, _ -> },
    onOpenFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (photos.isEmpty()) return

    var showShareTypeDialog by remember { mutableStateOf<String?>(null) }

    // Compute sequence of photo indices based on loop mode
    val frameSequence = remember(photos.size, loopConfig.mode) {
        when (loopConfig.mode) {
            LoopMode.BOOMERANG -> {
                if (photos.size <= 1) listOf(0)
                else {
                    val fwd = photos.indices.toList()
                    val bwd = (photos.size - 2 downTo 1).toList()
                    fwd + bwd
                }
            }
            LoopMode.FORWARD -> photos.indices.toList()
            LoopMode.DISCO_BURST -> photos.indices.toList()
            LoopMode.VINTAGE_STOPMOTION -> {
                if (photos.size <= 1) listOf(0)
                else photos.indices.toList() + (photos.size - 2 downTo 1).toList()
            }
        }
    }

    var sequenceIndex by remember { mutableIntStateOf(0) }
    var showFlashBlink by remember { mutableStateOf(false) }

    // Loop ticker
    LaunchedEffect(loopConfig.isPlaying, loopConfig.speed, frameSequence.size, loopConfig.transition) {
        if (!loopConfig.isPlaying || frameSequence.isEmpty()) return@LaunchedEffect

        while (true) {
            delay(loopConfig.speed.delayMs)
            if (loopConfig.transition == LoopTransition.SHUTTER_FLASH) {
                showFlashBlink = true
                delay(40)
                showFlashBlink = false
            }
            sequenceIndex = (sequenceIndex + 1) % frameSequence.size
        }
    }

    val currentPhotoIndex = frameSequence.getOrElse(sequenceIndex.coerceIn(0, frameSequence.lastIndex)) { 0 }
    val currentPhoto = photos.getOrNull(currentPhotoIndex) ?: photos[0]

    // Pre-filter photo bitmap
    val filteredBitmap = remember(currentPhoto, filter) {
        BitmapUtils.applyColorFilter(currentPhoto, filter)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Loop Frame Preview Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(380.dp),
            contentAlignment = Alignment.Center
        ) {
            LoopFrameDisplay(
                bitmap = filteredBitmap,
                frameColor = frameColor,
                captionText = captionText,
                stickerEmoji = stickerEmoji,
                particleEffect = loopConfig.particleEffect,
                transition = loopConfig.transition,
                showFlashBlink = showFlashBlink,
                currentPhotoIndex = currentPhotoIndex,
                totalPhotos = photos.size,
                isPlaying = loopConfig.isPlaying,
                onTogglePlay = onTogglePlay,
                onOpenFullscreen = onOpenFullscreen
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Playback Timeline Bar & Frame Navigator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(StudioSurface)
                .border(1.dp, Color(0x338B5CF6), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Step Previous Frame
                IconButton(
                    onClick = {
                        sequenceIndex = if (sequenceIndex - 1 < 0) frameSequence.lastIndex else sequenceIndex - 1
                    },
                    modifier = Modifier.size(36.dp).testTag("loop_prev_btn")
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Frame Sebelumnya", tint = StudioTextPrimary)
                }

                // Play / Pause Button
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (loopConfig.isPlaying) StudioViolet else StudioEmerald)
                        .testTag("loop_play_pause_btn")
                ) {
                    Icon(
                        imageVector = if (loopConfig.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (loopConfig.isPlaying) "Jeda Animasi" else "Putar Animasi",
                        tint = Color.White
                    )
                }

                // Step Next Frame
                IconButton(
                    onClick = {
                        sequenceIndex = (sequenceIndex + 1) % frameSequence.size
                    },
                    modifier = Modifier.size(36.dp).testTag("loop_next_btn")
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Frame Berikutnya", tint = StudioTextPrimary)
                }
            }

            // Frame counter indicator (e.g. Pose 2 dari 4)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Pose ${currentPhotoIndex + 1} dari ${photos.size}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = StudioVioletLight
                )
                Text(
                    text = loopConfig.mode.displayName + " • " + loopConfig.speed.multiplierText,
                    fontSize = 11.sp,
                    color = StudioTextSecondary
                )
            }

            // Fullscreen Button
            IconButton(
                onClick = onOpenFullscreen,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x338B5CF6))
                    .testTag("loop_fullscreen_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Layar Penuh",
                    tint = StudioVioletLight
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. GIF & MP4 Export & Share Action Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Save GIF Button
                Button(
                    onClick = onSaveGif,
                    enabled = !isGeneratingGif,
                    colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("save_gif_button")
                ) {
                    if (isGeneratingGif) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Proses...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan GIF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Save MP4 Button
                Button(
                    onClick = onSaveMp4,
                    enabled = !isGeneratingMp4,
                    colors = ButtonDefaults.buttonColors(containerColor = StudioViolet),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("save_mp4_button")
                ) {
                    if (isGeneratingMp4) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Proses MP4...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan MP4", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Share GIF Button
                Button(
                    onClick = { showShareTypeDialog = "gif" },
                    enabled = !isGeneratingGif,
                    colors = ButtonDefaults.buttonColors(containerColor = StudioNeonPink),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("share_gif_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bagikan GIF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Share MP4 Button
                Button(
                    onClick = { showShareTypeDialog = "mp4" },
                    enabled = !isGeneratingMp4,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088CC)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("share_mp4_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bagikan MP4", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Share Options Dialog for WhatsApp, Telegram, Instagram, etc.
        if (showShareTypeDialog != null) {
            val shareType = showShareTypeDialog!!
            AlertDialog(
                onDismissRequest = { showShareTypeDialog = null },
                containerColor = StudioSurface,
                title = {
                    Text(
                        text = if (shareType == "gif") "Bagikan Loop GIF Animasi" else "Bagikan Video Loop MP4",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = StudioTextPrimary
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Pilih platform tujuan untuk membagikan file:",
                            fontSize = 13.sp,
                            color = StudioTextSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // WhatsApp
                        Button(
                            onClick = {
                                showShareTypeDialog = null
                                onShareToSpecificApp("com.whatsapp", shareType)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        // Telegram
                        Button(
                            onClick = {
                                showShareTypeDialog = null
                                onShareToSpecificApp("org.telegram.messenger", shareType)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088cc)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Telegram", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        // Instagram
                        Button(
                            onClick = {
                                showShareTypeDialog = null
                                onShareToSpecificApp("com.instagram.android", shareType)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Instagram", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        // General Share Chooser
                        OutlinedButton(
                            onClick = {
                                showShareTypeDialog = null
                                if (shareType == "gif") onShareGif() else onShareMp4()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = StudioTextPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Aplikasi Lainnya...", color = StudioTextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Customization Controls for Loop
        LoopControlsSection(
            loopConfig = loopConfig,
            onSetLoopMode = onSetLoopMode,
            onSetLoopSpeed = onSetLoopSpeed,
            onSetLoopTransition = onSetLoopTransition,
            onSetLoopParticle = onSetLoopParticle
        )
    }
}

/**
 * Display Frame for Loop Animation with styled frame, caption, sticker, and particle overlays
 */
@Composable
fun LoopFrameDisplay(
    bitmap: Bitmap,
    frameColor: FrameColorOption,
    captionText: String,
    stickerEmoji: String,
    particleEffect: LoopParticleEffect,
    transition: LoopTransition,
    showFlashBlink: Boolean,
    currentPhotoIndex: Int,
    totalPhotos: Int,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onOpenFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (transition == LoopTransition.ZOOM_PULSE) 0.97f else 1f,
        targetValue = if (transition == LoopTransition.ZOOM_PULSE) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zoom_pulse"
    )

    Card(
        modifier = modifier
            .widthIn(max = 290.dp)
            .aspectRatio(0.72f)
            .shadow(28.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .border(2.dp, Color(frameColor.colorHex).copy(alpha = 0.8f), RoundedCornerShape(18.dp))
            .clickable { onTogglePlay() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(frameColor.colorHex)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Progress Indicators for all poses
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 0 until totalPhotos) {
                        val isActive = i == currentPhotoIndex
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) StudioVioletLight else Color.White.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                // Center Animated Photo Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Crossfade(
                        targetState = bitmap,
                        animationSpec = tween(if (transition == LoopTransition.CROSSFADE) 180 else 30),
                        label = "frame_crossfade"
                    ) { targetBmp ->
                        Image(
                            bitmap = targetBmp.asImageBitmap(),
                            contentDescription = "Loop Frame",
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(pulseScale),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Realtime Particle FX Overlay
                    LoopParticleOverlay(
                        particleEffect = particleEffect,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Shutter Flash overlay
                    if (showFlashBlink) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.75f)))
                    }

                    // Subtle Paused overlay
                    if (!isPlaying) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0x99000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Putar",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Sticker emoji on corner of photo
                    if (stickerEmoji.isNotBlank()) {
                        Text(
                            text = stickerEmoji,
                            fontSize = 32.sp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Frame bottom caption banner
                Text(
                    text = captionText.ifBlank { "PHOTO BOOTH LOOP ✨" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(frameColor.textColorHex),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Animated Particle FX Overlay (Sparkles, Hearts, Film Dust, Confetti)
 */
@Composable
fun LoopParticleOverlay(
    particleEffect: LoopParticleEffect,
    modifier: Modifier = Modifier
) {
    if (particleEffect == LoopParticleEffect.NONE) return

    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_progress"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        when (particleEffect) {
            LoopParticleEffect.SPARKLES -> {
                // Draw 6 animated pulsing stars/sparkles
                val positions = listOf(
                    Offset(width * 0.15f, height * 0.2f),
                    Offset(width * 0.82f, height * 0.25f),
                    Offset(width * 0.25f, height * 0.78f),
                    Offset(width * 0.75f, height * 0.72f),
                    Offset(width * 0.5f, height * 0.12f),
                    Offset(width * 0.88f, height * 0.55f)
                )

                positions.forEachIndexed { i, pos ->
                    val phase = (animProgress + (i * 0.18f)) % 1f
                    val starScale = sin(phase * Math.PI).toFloat() * 12f
                    val alpha = sin(phase * Math.PI).toFloat().coerceIn(0f, 1f)
                    val starColor = if (i % 2 == 0) StudioAmber.copy(alpha = alpha) else StudioNeonCyan.copy(alpha = alpha)

                    if (starScale > 1f) {
                        // 4-pointed sparkle
                        drawLine(starColor, Offset(pos.x - starScale, pos.y), Offset(pos.x + starScale, pos.y), strokeWidth = 2.5f)
                        drawLine(starColor, Offset(pos.x, pos.y - starScale), Offset(pos.x, pos.y + starScale), strokeWidth = 2.5f)
                        drawCircle(Color.White.copy(alpha = alpha), radius = starScale * 0.25f, center = pos)
                    }
                }
            }

            LoopParticleEffect.HEARTS -> {
                // Draw floating neon hearts ascending upwards
                val heartSeeds = listOf(0.2f, 0.45f, 0.7f, 0.85f, 0.35f)
                heartSeeds.forEachIndexed { idx, xRatio ->
                    val progress = (animProgress + (idx * 0.22f)) % 1f
                    val y = height - (progress * height * 1.1f)
                    val x = (width * xRatio) + sin(progress * 6.28f).toFloat() * 18f
                    val alpha = (1f - progress).coerceIn(0f, 0.85f)
                    val heartRadius = 6f + (idx * 1.5f)

                    drawCircle(StudioNeonPink.copy(alpha = alpha), radius = heartRadius, center = Offset(x, y))
                    drawCircle(Color.White.copy(alpha = alpha * 0.7f), radius = heartRadius * 0.4f, center = Offset(x, y))
                }
            }

            LoopParticleEffect.FILM_DUST -> {
                // Analog 35mm scratches and dust motes
                val seed = (animProgress * 10).toInt()
                val random = java.util.Random(seed.toLong())

                // 4-8 dust specks
                for (d in 0 until 8) {
                    val dx = random.nextFloat() * width
                    val dy = random.nextFloat() * height
                    val dRadius = random.nextFloat() * 2.5f + 1f
                    val alpha = random.nextFloat() * 0.35f + 0.15f
                    drawCircle(Color.White.copy(alpha = alpha), radius = dRadius, center = Offset(dx, dy))
                }

                // Vertical hairline scratch
                if (seed % 3 == 0) {
                    val scratchX = random.nextFloat() * width
                    drawLine(
                        color = Color.White.copy(alpha = 0.25f),
                        start = Offset(scratchX, 0f),
                        end = Offset(scratchX + (random.nextFloat() * 8f - 4f), height),
                        strokeWidth = 1f
                    )
                }
            }

            LoopParticleEffect.CONFETTI -> {
                // Festive celebration confetti falling
                val colors = listOf(StudioNeonPink, StudioAmber, StudioNeonCyan, StudioEmerald, StudioVioletLight)
                for (c in 0 until 14) {
                    val initialOffset = (c * 0.07f)
                    val progress = (animProgress + initialOffset) % 1f
                    val y = progress * height
                    val x = (width * ((c * 7) % 10) / 10f) + cos(progress * 8f + c).toFloat() * 15f
                    val color = colors[c % colors.size].copy(alpha = (1f - progress * 0.3f))

                    drawCircle(color, radius = 4.5f, center = Offset(x, y))
                }
            }

            LoopParticleEffect.NONE -> {}
        }
    }
}

/**
 * Customization controls for Mode, Speed, Transition, and Particle Effects
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoopControlsSection(
    loopConfig: LoopAnimationConfig,
    onSetLoopMode: (LoopMode) -> Unit,
    onSetLoopSpeed: (LoopSpeed) -> Unit,
    onSetLoopTransition: (LoopTransition) -> Unit,
    onSetLoopParticle: (LoopParticleEffect) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Mode Playback Loop (Boomerang vs Forward vs Disco vs Stop-Motion)
        Column {
            Text(
                text = "Mode Putar Loop",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = StudioTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LoopMode.values().forEach { mode ->
                    val isSelected = loopConfig.mode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSetLoopMode(mode) },
                        label = {
                            Text(
                                text = mode.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            val icon = when (mode) {
                                LoopMode.BOOMERANG -> Icons.Default.SwapHoriz
                                LoopMode.FORWARD -> Icons.Default.Autorenew
                                LoopMode.DISCO_BURST -> Icons.Default.FlashOn
                                LoopMode.VINTAGE_STOPMOTION -> Icons.Default.Movie
                            }
                            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioViolet,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = StudioSurface,
                            labelColor = StudioTextSecondary
                        )
                    )
                }
            }
        }

        // 2. Loop Speed
        Column {
            Text(
                text = "Kecepatan Animasi",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = StudioTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LoopSpeed.values().forEach { speed ->
                    val isSelected = loopConfig.speed == speed
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSetLoopSpeed(speed) },
                        label = {
                            Text(
                                text = "${speed.displayName} (${speed.multiplierText})",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioNeonCyan,
                            selectedLabelColor = Color.Black,
                            containerColor = StudioSurface,
                            labelColor = StudioTextSecondary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Visual Transition Effect
        Column {
            Text(
                text = "Efek Transisi Frame",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = StudioTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LoopTransition.values().forEach { transition ->
                    val isSelected = loopConfig.transition == transition
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSetLoopTransition(transition) },
                        label = {
                            Text(
                                text = transition.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioNeonPink,
                            selectedLabelColor = Color.White,
                            containerColor = StudioSurface,
                            labelColor = StudioTextSecondary
                        )
                    )
                }
            }
        }

        // 4. Floating Particle FX Overlay
        Column {
            Text(
                text = "Efek Partikel & Kilau ✨",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = StudioTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LoopParticleEffect.values().forEach { particle ->
                    val isSelected = loopConfig.particleEffect == particle
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSetLoopParticle(particle) },
                        label = {
                            Text(
                                text = "${particle.emoji} ${particle.displayName}",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioAmber,
                            selectedLabelColor = Color.Black,
                            containerColor = StudioSurface,
                            labelColor = StudioTextSecondary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Immersive Fullscreen Loop Animation Theater Dialog
 */
@Composable
fun FullscreenLoopDialog(
    photos: List<Bitmap>,
    filter: ColorFilterType,
    frameColor: FrameColorOption,
    captionText: String,
    stickerEmoji: String,
    loopConfig: LoopAnimationConfig,
    onClose: () -> Unit,
    onSaveGif: () -> Unit,
    onShareGif: () -> Unit
) {
    if (photos.isEmpty()) return

    val frameSequence = remember(photos.size, loopConfig.mode) {
        when (loopConfig.mode) {
            LoopMode.BOOMERANG -> {
                if (photos.size <= 1) listOf(0)
                else photos.indices.toList() + (photos.size - 2 downTo 1).toList()
            }
            LoopMode.FORWARD -> photos.indices.toList()
            LoopMode.DISCO_BURST -> photos.indices.toList()
            LoopMode.VINTAGE_STOPMOTION -> {
                if (photos.size <= 1) listOf(0)
                else photos.indices.toList() + (photos.size - 2 downTo 1).toList()
            }
        }
    }

    var sequenceIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }

    LaunchedEffect(isPlaying, loopConfig.speed, frameSequence.size) {
        if (!isPlaying || frameSequence.isEmpty()) return@LaunchedEffect
        while (true) {
            delay(loopConfig.speed.delayMs)
            sequenceIndex = (sequenceIndex + 1) % frameSequence.size
        }
    }

    val currentPhotoIndex = frameSequence.getOrElse(sequenceIndex.coerceIn(0, frameSequence.lastIndex)) { 0 }
    val currentPhoto = photos[currentPhotoIndex]
    val filteredBitmap = remember(currentPhoto, filter) {
        BitmapUtils.applyColorFilter(currentPhoto, filter)
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xF0050814))
                .clickable { isPlaying = !isPlaying }
        ) {
            // Ambient glow backdrop
            Box(
                modifier = Modifier
                    .size(340.dp)
                    .align(Alignment.Center)
                    .background(
                        Brush.radialGradient(
                            listOf(StudioViolet.copy(alpha = 0.35f), Color.Transparent)
                        )
                    )
            )

            // Close button top right
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 20.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0x66000000))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
            }

            // Center Loop Player
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoopFrameDisplay(
                    bitmap = filteredBitmap,
                    frameColor = frameColor,
                    captionText = captionText,
                    stickerEmoji = stickerEmoji,
                    particleEffect = loopConfig.particleEffect,
                    transition = loopConfig.transition,
                    showFlashBlink = false,
                    currentPhotoIndex = currentPhotoIndex,
                    totalPhotos = photos.size,
                    isPlaying = isPlaying,
                    onTogglePlay = { isPlaying = !isPlaying },
                    onOpenFullscreen = {}
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom actions in fullscreen
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onSaveGif,
                        colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan GIF", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onShareGif,
                        colors = ButtonDefaults.buttonColors(containerColor = StudioNeonPink),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bagikan GIF", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
