package com.example.ui.camera

import android.graphics.Bitmap
import java.io.File
import coil.compose.AsyncImage
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SliderDefaults
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.text.style.TextOverflow
import com.example.model.PhotoBoothLayout
import com.example.model.CustomLayoutConfig
import com.example.model.PngSlotRect
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CameraSource
import com.example.model.ColorFilterType
import com.example.model.PhotoBoothUiState
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioNeonCyan
import com.example.ui.theme.StudioNeonPink
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceGlass
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioViolet
import com.example.ui.theme.StudioVioletLight

import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.AutoFixHigh

@Composable
fun CameraScreen(
    uiState: PhotoBoothUiState,
    onPhotoCaptured: (Bitmap) -> Unit,
    bindCaptureTrigger: ((() -> Unit) -> Unit),
    onStartSequence: () -> Unit,
    onCancelSequence: () -> Unit,
    onToggleLens: () -> Unit,
    onToggleTorch: () -> Unit,
    onSelectTimerDuration: (Int) -> Unit,
    onSelectFilter: (ColorFilterType) -> Unit,
    onSelectBrightness: (Float) -> Unit = {},
    onSelectContrast: (Float) -> Unit = {},
    onSelectSkinSmoothing: (Float) -> Unit = {},
    onBackToTemplates: () -> Unit,
    onSelectCameraSource: (CameraSource) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val totalRequiredPhotos = uiState.activePhotoCount
    var showGridLines by remember { mutableStateOf(true) }
    var exposureIndex by remember { mutableStateOf(0) }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. LEFT SIDE: Live Camera Viewfinder (Prominent Frame Border matching user's sketch)
        CameraViewfinderSection(
            uiState = uiState,
            totalRequiredPhotos = totalRequiredPhotos,
            exposureIndex = exposureIndex,
            showGridLines = showGridLines,
            onToggleGridLines = { showGridLines = !showGridLines },
            onPhotoCaptured = onPhotoCaptured,
            bindCaptureTrigger = bindCaptureTrigger,
            onBackToTemplates = onBackToTemplates,
            onSelectTimerDuration = onSelectTimerDuration,
            onToggleTorch = onToggleTorch,
            onToggleLens = onToggleLens,
            onStartSequence = onStartSequence,
            onCancelSequence = onCancelSequence,
            modifier = Modifier
                .weight(1.35f)
                .fillMaxHeight()
        )

        // 2. RIGHT SIDE: Selected Frame with Live Real-time Photo Slots
        SelectedFrameLiveView(
            uiState = uiState,
            totalRequiredPhotos = totalRequiredPhotos,
            modifier = Modifier
                .weight(0.95f)
                .fillMaxHeight()
        )
    }
}

/**
 * Left Side Live Camera Viewfinder Section
 */
@Composable
private fun CameraViewfinderSection(
    uiState: PhotoBoothUiState,
    totalRequiredPhotos: Int,
    exposureIndex: Int,
    showGridLines: Boolean,
    onToggleGridLines: () -> Unit,
    onPhotoCaptured: (Bitmap) -> Unit,
    bindCaptureTrigger: ((() -> Unit) -> Unit)?,
    onBackToTemplates: () -> Unit,
    onSelectTimerDuration: (Int) -> Unit,
    onToggleTorch: () -> Unit,
    onToggleLens: () -> Unit,
    onStartSequence: () -> Unit,
    onCancelSequence: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.Black)
            .border(2.5.dp, Color(0xFF1E293B), RoundedCornerShape(18.dp))
            .testTag("live_camera_viewfinder")
    ) {
        // CameraX PreviewView for built-in camera
        CameraPreviewView(
            selectedCameraSource = uiState.selectedCameraSource,
            isTorchOn = uiState.isTorchOn,
            exposureIndex = exposureIndex,
            filterType = uiState.selectedFilter,
            brightness = uiState.brightness,
            contrast = uiState.contrast,
            skinSmoothing = uiState.skinSmoothing,
            onPhotoCaptured = onPhotoCaptured,
            bindCaptureTrigger = bindCaptureTrigger ?: {},
            modifier = Modifier.fillMaxSize()
        )

        // 1b. Camera Live View 3x3 Grid Overlay (Rule of Thirds)
        if (showGridLines) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val gridColor = Color.White.copy(alpha = 0.22f)
                val strokeWidth = 1.dp.toPx()

                drawLine(gridColor, start = Offset(width / 3f, 0f), end = Offset(width / 3f, height), strokeWidth = strokeWidth)
                drawLine(gridColor, start = Offset(2 * width / 3f, 0f), end = Offset(2 * width / 3f, height), strokeWidth = strokeWidth)
                drawLine(gridColor, start = Offset(0f, height / 3f), end = Offset(width, height / 3f), strokeWidth = strokeWidth)
                drawLine(gridColor, start = Offset(0f, 2 * height / 3f), end = Offset(width, 2 * height / 3f), strokeWidth = strokeWidth)
            }
        }

        // 2. White Flash Screen Effect when Photo is Taken
        AnimatedVisibility(
            visible = uiState.showFlashScreen,
            enter = fadeIn(animationSpec = tween(50)),
            exit = fadeOut(animationSpec = tween(200)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.White))
        }

        // 3. Top Glassmorphic Control Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xCC0F172A))
                .border(1.dp, Color(0x338B5CF6), RoundedCornerShape(16.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back Button
            IconButton(
                onClick = {
                    if (uiState.isSequenceRunning) onCancelSequence()
                    onBackToTemplates()
                },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("camera_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Kembali ke Template",
                    tint = StudioTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Layout & Event Label
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (uiState.retakeSlotIndex != null) "Retake Slot #${uiState.retakeSlotIndex + 1}" else "Live Camera",
                    color = if (uiState.retakeSlotIndex != null) StudioNeonPink else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "${uiState.capturedPhotos.size}/$totalRequiredPhotos Pose",
                    color = StudioVioletLight,
                    fontSize = 10.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Grid Lines Toggle
                IconButton(
                    onClick = onToggleGridLines,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("toggle_grid_button")
                ) {
                    Icon(
                        imageVector = if (showGridLines) Icons.Default.GridOn else Icons.Default.GridOff,
                        contentDescription = "Toggle Grid Lines",
                        tint = if (showGridLines) StudioNeonCyan else StudioTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Timer Preset Cycling
                IconButton(
                    onClick = {
                        val nextDuration = when (uiState.selectedTimerDuration) {
                            3 -> 5
                            5 -> 10
                            else -> 3
                        }
                        onSelectTimerDuration(nextDuration)
                    },
                    enabled = !uiState.isSequenceRunning,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("cycle_timer_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Timer ${uiState.selectedTimerDuration}s",
                            tint = StudioVioletLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${uiState.selectedTimerDuration}s",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Torch Flash Toggle
                IconButton(
                    onClick = onToggleTorch,
                    enabled = !uiState.isSequenceRunning,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("toggle_torch_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle Torch",
                        tint = if (uiState.isTorchOn) StudioAmber else StudioTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Lens Flip (Front / Back)
                IconButton(
                    onClick = onToggleLens,
                    enabled = !uiState.isSequenceRunning,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("toggle_lens_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Ganti Kamera",
                        tint = StudioTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 4. Center Countdown Overlay
        AnimatedVisibility(
            visible = uiState.isCountingDown && uiState.countdownNumber > 0,
            enter = fadeIn() + scaleIn(initialScale = 0.5f),
            exit = fadeOut() + scaleOut(targetScale = 1.4f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "countdown_pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xDD0F172A))
                        .border(3.dp, Brush.horizontalGradient(listOf(StudioViolet, StudioNeonPink)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.countdownNumber.toString(),
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xCC0F172A))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (uiState.retakeSlotIndex != null) {
                            "Ambil Ulang Pose #${uiState.retakeSlotIndex + 1}"
                        } else {
                            "Pose #${uiState.currentSlotIndex + 1} Siap!"
                        },
                        color = if (uiState.retakeSlotIndex != null) StudioNeonPink else StudioVioletLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // 5. Bottom Shutter Controls on Camera View
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isSequenceRunning) {
                Card(
                    onClick = onCancelSequence,
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xCCEF4444)),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("cancel_sequence_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.retakeSlotIndex != null) "Batalkan Retake" else "Batalkan Sesi",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                MainShutterButton(
                    onClick = onStartSequence,
                    isRetake = uiState.retakeSlotIndex != null,
                    retakeSlotNumber = (uiState.retakeSlotIndex ?: 0) + 1
                )
            }
        }
    }
}

/**
 * Right Side Panel: Displays the selected frame and inserts taken photos into slots in real-time
 */
@Composable
private fun SelectedFrameLiveView(
    uiState: PhotoBoothUiState,
    totalRequiredPhotos: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.5.dp, Color(0x338B5CF6), RoundedCornerShape(18.dp))
            .testTag("selected_frame_live_view"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = StudioViolet,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (uiState.isCustomLayoutActive) uiState.customLayoutConfig.name else uiState.selectedLayout.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 140.dp)
                    )
                }

                Text(
                    text = "${uiState.capturedPhotos.size}/$totalRequiredPhotos Terisi",
                    style = MaterialTheme.typography.labelSmall,
                    color = StudioNeonCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioNeonCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // The Photobooth Frame with Real-time Slots
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isCustomLayoutActive && uiState.customLayoutConfig.isPngOverlay) {
                    // PNG Overlay / Cloud Frame Live View
                    PngOverlayFrameLiveView(
                        config = uiState.customLayoutConfig,
                        capturedPhotos = uiState.capturedPhotos,
                        currentSlotIndex = uiState.currentSlotIndex,
                        retakeSlotIndex = uiState.retakeSlotIndex,
                        totalCount = totalRequiredPhotos,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Standard Ticket Strip Photobooth Frame Live View (Matching Grobogan Vibes / ARVSNAP user sketch)
                    TicketStripFrameLiveView(
                        uiState = uiState,
                        totalCount = totalRequiredPhotos,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * Standard Photobooth Ticket Strip with Grobogan Vibes style matching user mockup
 */
@Composable
private fun TicketStripFrameLiveView(
    uiState: PhotoBoothUiState,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val isWideEnoughForTwin = maxWidth >= 200.dp
        val isStripLayout = uiState.selectedLayout == PhotoBoothLayout.CLASSIC_3_STRIP || uiState.selectedLayout == PhotoBoothLayout.CLASSIC_4_STRIP
        val stripCount = if (isWideEnoughForTwin && isStripLayout) 2 else 1

        Row(
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(stripCount) { stripIdx ->
                SingleTicketStrip(
                    uiState = uiState,
                    totalCount = totalCount,
                    ticketNumber = if (stripIdx == 0) "08" else "10",
                    ticketLabel = if (stripIdx == 0) "SUNSET" else "SORE",
                    modifier = Modifier
                        .fillMaxHeight(0.98f)
                        .aspectRatio(0.44f)
                )
            }
        }
    }
}

/**
 * Single Ticket Strip with Header, Live Photo Slots, and Barcode Footer
 */
@Composable
private fun SingleTicketStrip(
    uiState: PhotoBoothUiState,
    totalCount: Int,
    ticketNumber: String,
    ticketLabel: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(12.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFD4C7B0), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF8EE))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Header (Grobogan Vibes script + Ticket Row)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF8B1E2F), RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.captionText.isNotBlank()) uiState.captionText else "Grobogan Vibes",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B1E2F),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = ticketLabel, fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                        Text(text = "16:20", fontSize = 6.sp, color = Color(0xFF475569))
                    }
                    Text(
                        text = ticketNumber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF8B1E2F)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Seat 2", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                        Text(text = "Row 4", fontSize = 6.sp, color = Color(0xFF475569))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 2. Photo Slots (Dynamic insertion of captured photos)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 0 until totalCount) {
                    val photo = uiState.capturedPhotos.getOrNull(i)
                    val isBeingRetaken = uiState.retakeSlotIndex != null && i == uiState.retakeSlotIndex
                    val isCurrent = if (uiState.retakeSlotIndex != null) isBeingRetaken else (i == uiState.currentSlotIndex && photo == null)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFD4C7B0), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photo != null && !isBeingRetaken) {
                            Image(
                                bitmap = photo.asImageBitmap(),
                                contentDescription = "Foto ${i + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp)
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(9.dp)
                                )
                            }
                        } else {
                            LiveSlotPlaceholder(
                                slotNumber = i + 1,
                                isActive = isCurrent,
                                isRetaking = isBeingRetaken
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 3. Footer (ARVSNAP Photobooth + Barcode matching sketch)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFF1E3A8A),
                        modifier = Modifier.size(7.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF8B1E2F))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "ARVSNAP™",
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFF1E3A8A),
                        modifier = Modifier.size(7.dp)
                    )
                }
                Text(
                    text = "Photobooth",
                    fontSize = 6.sp,
                    color = Color(0xFF8B1E2F),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Barcode Bars
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .padding(horizontal = 2.dp)
                ) {
                    val barCount = 36
                    val barWidth = size.width / (barCount * 1.5f)
                    val barColor = Color(0xFF262626)
                    for (b in 0 until barCount) {
                        val x = b * (barWidth * 1.5f)
                        val w = if (b % 3 == 0) barWidth * 1.4f else barWidth * 0.8f
                        drawRect(color = barColor, topLeft = Offset(x, 0f), size = androidx.compose.ui.geometry.Size(w, size.height))
                    }
                }
            }
        }
    }
}

/**
 * Live View for PNG Overlay and Cloud Frames
 */
@Composable
private fun PngOverlayFrameLiveView(
    config: CustomLayoutConfig,
    capturedPhotos: List<Bitmap>,
    currentSlotIndex: Int,
    retakeSlotIndex: Int?,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val aspect = 1f / config.overlayAspectRatio.coerceAtLeast(0.5f)

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val containerW = maxWidth
        val containerH = maxHeight

        val cardModifier = if (aspect < (containerW.value / containerH.value)) {
            Modifier.fillMaxHeight(0.98f).aspectRatio(aspect)
        } else {
            Modifier.fillMaxWidth(0.98f).aspectRatio(aspect)
        }

        Card(
            modifier = cardModifier
                .shadow(16.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(config.frameColorHex))
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val stripW = maxWidth
                val stripH = maxHeight

                val slots = if (config.pngSlots.isNotEmpty()) {
                    config.pngSlots
                } else {
                    val count = totalCount.coerceAtLeast(1)
                    (0 until count).map { i ->
                        val top = 0.12f + (i * (0.74f / count))
                        val bottom = top + (0.70f / count)
                        PngSlotRect(left = 0.08f, top = top, right = 0.92f, bottom = bottom, index = i)
                    }
                }

                slots.forEachIndexed { idx, slot ->
                    val slotLeft = stripW * slot.left
                    val slotTop = stripH * slot.top
                    val slotW = stripW * (slot.right - slot.left)
                    val slotH = stripH * (slot.bottom - slot.top)
                    val photo = capturedPhotos.getOrNull(idx)
                    val isCurrent = (retakeSlotIndex ?: currentSlotIndex) == idx && photo == null
                    val isRetaking = retakeSlotIndex == idx

                    Box(
                        modifier = Modifier
                            .offset(x = slotLeft, y = slotTop)
                            .size(width = slotW, height = slotH)
                            .clip(RoundedCornerShape(config.cornerRadiusDp.coerceIn(2f, 10f).dp))
                            .background(Color(config.slotBgColorHex))
                    ) {
                        if (photo != null && !isRetaking) {
                            Image(
                                bitmap = photo.asImageBitmap(),
                                contentDescription = "Foto $idx",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            LiveSlotPlaceholder(
                                slotNumber = idx + 1,
                                isActive = isCurrent || isRetaking,
                                isRetaking = isRetaking
                            )
                        }
                    }
                }

                val overlayModel: Any? = when {
                    config.pngOverlayPath.isNotBlank() && File(config.pngOverlayPath).exists() -> File(config.pngOverlayPath)
                    config.cloudFrameUrl.isNotBlank() -> config.cloudFrameUrl
                    else -> null
                }
                if (overlayModel != null) {
                    AsyncImage(
                        model = overlayModel,
                        contentDescription = config.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }
            }
        }
    }
}

/**
 * Animated Empty Slot Placeholder with camera icon and pulsing highlight
 */
@Composable
private fun LiveSlotPlaceholder(
    slotNumber: Int,
    isActive: Boolean,
    isRetaking: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_slot")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (isActive) 0.35f else 0.15f,
        targetValue = if (isActive) 0.85f else 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                when {
                    isRetaking -> StudioNeonPink.copy(alpha = 0.18f)
                    isActive -> StudioViolet.copy(alpha = 0.22f)
                    else -> Color(0xFF1E293B).copy(alpha = 0.5f)
                }
            )
            .border(
                width = if (isActive || isRetaking) 2.dp else 1.dp,
                color = when {
                    isRetaking -> StudioNeonPink.copy(alpha = pulseAlpha)
                    isActive -> StudioVioletLight.copy(alpha = pulseAlpha)
                    else -> Color(0x33FFFFFF)
                },
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.CameraAlt else Icons.Default.AddAPhoto,
                contentDescription = null,
                tint = if (isActive) StudioVioletLight else Color.White.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(if (isActive) 22.dp else 18.dp)
                    .scale(if (isActive) 1.05f else 1.0f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isRetaking) "RETAKE" else "Slot $slotNumber",
                fontSize = 9.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) Color.White else Color.White.copy(alpha = 0.5f)
            )
            if (isActive) {
                Text(
                    text = "Ambil Foto",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StudioNeonCyan
                )
            }
        }
    }
}

/**
 * Modern Glowing Shutter Button
 */
@Composable
private fun MainShutterButton(
    onClick: () -> Unit,
    isRetake: Boolean = false,
    retakeSlotNumber: Int = 1,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(if (isRetake) Color(0x44EC4899) else Color(0x448B5CF6))
                .border(2.dp, if (isRetake) Color(0x66EC4899) else Color(0x668B5CF6), CircleShape)
                .clickable { onClick() }
                .testTag("main_shutter_button"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        if (isRetake) {
                            Brush.linearGradient(listOf(StudioNeonPink, StudioAmber))
                        } else {
                            Brush.linearGradient(listOf(StudioViolet, StudioNeonPink))
                        }
                    )
                    .shadow(12.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = if (isRetake) "Ambil Ulang Foto #$retakeSlotNumber" else "Ambil Foto",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        if (isRetake) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Ambil Ulang Foto #$retakeSlotNumber",
                color = StudioNeonPink,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
