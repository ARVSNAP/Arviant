package com.example.ui.camera

import android.graphics.Bitmap
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
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WbSunny
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

    Box(modifier = modifier.fillMaxSize().background(Color.Transparent)) {
        // CameraX PreviewView for built-in HP camera (Front / Back)
        CameraPreviewView(
            selectedCameraSource = uiState.selectedCameraSource,
            isTorchOn = uiState.isTorchOn,
            exposureIndex = exposureIndex,
            filterType = uiState.selectedFilter,
            brightness = uiState.brightness,
            contrast = uiState.contrast,
            skinSmoothing = uiState.skinSmoothing,
            onPhotoCaptured = onPhotoCaptured,
            bindCaptureTrigger = bindCaptureTrigger,
            modifier = Modifier.fillMaxSize()
        )

        // 1b. Camera Live View 3x3 Grid Overlay (Rule of Thirds)
        if (showGridLines) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val gridColor = Color.White.copy(alpha = 0.22f)
                val strokeWidth = 1.dp.toPx()

                // Vertical Grid Lines
                drawLine(gridColor, start = Offset(width / 3f, 0f), end = Offset(width / 3f, height), strokeWidth = strokeWidth)
                drawLine(gridColor, start = Offset(2 * width / 3f, 0f), end = Offset(2 * width / 3f, height), strokeWidth = strokeWidth)

                // Horizontal Grid Lines
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
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xAA0F172A))
                .border(1.dp, Color(0x338B5CF6), RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back Button
            IconButton(
                onClick = {
                    if (uiState.isSequenceRunning) onCancelSequence()
                    onBackToTemplates()
                },
                modifier = Modifier.testTag("camera_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Kembali ke Template",
                    tint = StudioTextPrimary
                )
            }

            // Layout & Event Label
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (uiState.retakeSlotIndex != null) "Retake Slot #${uiState.retakeSlotIndex + 1}" else uiState.selectedLayout.displayName,
                    color = if (uiState.retakeSlotIndex != null) StudioNeonPink else StudioVioletLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = if (uiState.retakeSlotIndex != null) "Mode Ambil Ulang Foto" else "${uiState.capturedPhotos.size}/$totalRequiredPhotos Pose",
                    color = if (uiState.retakeSlotIndex != null) StudioNeonCyan else StudioTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = if (uiState.retakeSlotIndex != null) FontWeight.SemiBold else FontWeight.Normal
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Grid Lines Toggle
                IconButton(
                    onClick = { showGridLines = !showGridLines },
                    modifier = Modifier.testTag("toggle_grid_button")
                ) {
                    Icon(
                        imageVector = if (showGridLines) Icons.Default.GridOn else Icons.Default.GridOff,
                        contentDescription = "Toggle Grid Lines",
                        tint = if (showGridLines) StudioNeonCyan else StudioTextSecondary
                    )
                }

                // Timer Preset Cycling Icon Button (Moved to top actions bar)
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
                    modifier = Modifier.testTag("cycle_timer_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Timer ${uiState.selectedTimerDuration}s",
                            tint = StudioVioletLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${uiState.selectedTimerDuration}s",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Torch Flash Toggle
                IconButton(
                    onClick = onToggleTorch,
                    enabled = !uiState.isSequenceRunning,
                    modifier = Modifier.testTag("toggle_torch_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle Torch",
                        tint = if (uiState.isTorchOn) StudioAmber else StudioTextSecondary
                    )
                }

                // Lens Flip (Front / Back)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    IconButton(
                        onClick = onToggleLens,
                        enabled = !uiState.isSequenceRunning,
                        modifier = Modifier.testTag("toggle_lens_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Ganti Kamera",
                            tint = StudioTextPrimary
                        )
                    }
                    Text(
                        text = when (uiState.selectedCameraSource) {
                            CameraSource.FRONT -> "FRONT"
                            CameraSource.BACK -> "BACK"
                        },
                        color = StudioTextSecondary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 5. Dynamic Countdown Overlay (Pulsing Animation & Sound)
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
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC0F172A))
                        .border(3.dp, Brush.horizontalGradient(listOf(StudioViolet, StudioNeonPink)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.countdownNumber.toString(),
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xAA0F172A))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (uiState.retakeSlotIndex != null) {
                            "Ambil Ulang Pose #${uiState.retakeSlotIndex + 1} Siap!"
                        } else {
                            "Pose #${uiState.currentSlotIndex + 1} Siap!"
                        },
                        color = if (uiState.retakeSlotIndex != null) StudioNeonPink else StudioVioletLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // 6. Bottom Glassmorphic Control Panel & Photo Slot Gallery
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 28.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            // Live Thumbnail Progress Slots
            PhotoSlotIndicatorBar(
                totalCount = totalRequiredPhotos,
                currentIndex = uiState.currentSlotIndex,
                retakeSlotIndex = uiState.retakeSlotIndex,
                capturedPhotos = uiState.capturedPhotos
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Shutter Button / Cancel Sequence Controls
            if (uiState.isSequenceRunning) {
                // Cancel Active Burst Sequence
                Card(
                    onClick = onCancelSequence,
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xCCEF4444)),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("cancel_sequence_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.retakeSlotIndex != null) "Batalkan Retake" else "Batalkan Sesi",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // Main Shutter Button
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
 * Thumbnail Slot Progress Bar
 */
@Composable
private fun PhotoSlotIndicatorBar(
    totalCount: Int,
    currentIndex: Int,
    retakeSlotIndex: Int? = null,
    capturedPhotos: List<Bitmap>
) {
    Surface(
        color = Color(0xAA0F172A),
        shape = RoundedCornerShape(24.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0x338B5CF6), Color(0x33EC4899)))),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until totalCount) {
                val photo = capturedPhotos.getOrNull(i)
                val isBeingRetaken = retakeSlotIndex != null && i == retakeSlotIndex
                val isActive = if (retakeSlotIndex != null) isBeingRetaken else (i == currentIndex && photo == null)
                val isDone = photo != null && !isBeingRetaken

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when {
                                isBeingRetaken -> StudioNeonPink.copy(alpha = 0.25f)
                                isDone -> Color.Transparent
                                isActive -> StudioViolet.copy(alpha = 0.3f)
                                else -> Color(0x22FFFFFF)
                            }
                        )
                        .border(
                            width = if (isBeingRetaken || isActive) 2.dp else 1.dp,
                            color = when {
                                isBeingRetaken -> StudioNeonPink
                                isActive -> StudioViolet
                                isDone -> StudioNeonCyan
                                else -> Color(0x33FFFFFF)
                            },
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (photo != null) {
                        Image(
                            bitmap = photo.asImageBitmap(),
                            contentDescription = "Foto $i",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (isBeingRetaken) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.45f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "RETAKE",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = StudioNeonPink
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "${i + 1}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isActive) StudioVioletLight else StudioTextSecondary
                        )
                    }
                }
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
