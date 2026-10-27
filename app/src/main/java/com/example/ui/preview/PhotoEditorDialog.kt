package com.example.ui.preview

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioBorderGlass
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioNeonCyan
import com.example.ui.theme.StudioNeonPink
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioViolet
import com.example.ui.theme.StudioVioletLight
import com.example.util.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class FeatureType {
    NONE, FILTER, BRIGHTNESS, CONTRAST, SKIN, TRANSFORM
}

/**
 * Interactive Dialog for fine-tuning individual or all captured photos.
 * Provides controls for brightness, contrast, saturation, warmth, rotation, mirror flip,
 * and replacing photos from the device gallery.
 */
@Composable
fun PhotoEditorDialog(
    photos: List<Bitmap>,
    originalPhotos: List<Bitmap>,
    initialIndex: Int = 0,
    onDismiss: () -> Unit,
    onSavePhoto: (index: Int, editedBitmap: Bitmap) -> Unit,
    onApplyToAll: (brightness: Float, contrast: Float, saturation: Float, warmth: Float, rotate: Float, flipH: Boolean, flipV: Boolean) -> Unit,
    onResetPhoto: (index: Int) -> Unit,
    onReplacePhotoUri: (index: Int, Uri) -> Unit,
    onRetakeCamera: ((Int) -> Unit)? = null
) {
    if (photos.isEmpty()) {
        onDismiss()
        return
    }

    var selectedIndex by remember {
        mutableIntStateOf(initialIndex.coerceIn(0, photos.size - 1))
    }

    // Adjustment states
    var brightness by remember { mutableFloatStateOf(0f) }      // -1f to +1f
    var contrast by remember { mutableFloatStateOf(1f) }        // 0.4f to 2.2f
    var saturation by remember { mutableFloatStateOf(1f) }      // 0f to 2.2f
    var warmth by remember { mutableFloatStateOf(0f) }          // -1f to +1f
    var skinSmoothing by remember { mutableFloatStateOf(0f) }   // 0f to 1f
    var rotationDegrees by remember { mutableFloatStateOf(0f) } // degrees
    var flipHorizontal by remember { mutableStateOf(false) }
    var flipVertical by remember { mutableStateOf(false) }

    // Feature State
    var activeFeature by remember { mutableStateOf(FeatureType.NONE) }

    // Reset local sliders whenever user switches photo slot
    LaunchedEffect(selectedIndex) {
        brightness = 0f
        contrast = 1f
        saturation = 1f
        warmth = 0f
        skinSmoothing = 0f
        rotationDegrees = 0f
        flipHorizontal = false
        flipVertical = false
    }

    // Interaction source for "Tahan untuk Melihat Asli" button
    val compareInteractionSource = remember { MutableInteractionSource() }
    val isHoldingCompare by compareInteractionSource.collectIsPressedAsState()

    val currentOriginal = originalPhotos.getOrNull(selectedIndex) ?: photos[selectedIndex]
    val currentPhoto = photos[selectedIndex]

    // Live preview computation in background thread
    val previewBitmap by produceState<Bitmap?>(
        initialValue = currentPhoto,
        currentPhoto,
        brightness,
        contrast,
        saturation,
        warmth,
        rotationDegrees,
        flipHorizontal,
        flipVertical,
        isHoldingCompare
    ) {
        if (isHoldingCompare) {
            value = currentOriginal
        } else {
            value = withContext(Dispatchers.Default) {
                BitmapUtils.applyPhotoAdjustments(
                    source = currentPhoto,
                    brightness = brightness,
                    contrast = contrast,
                    saturation = saturation,
                    warmth = warmth,
                    rotationDegrees = rotationDegrees,
                    flipHorizontal = flipHorizontal,
                    flipVertical = flipVertical
                )
            }
        }
    }

    // Gallery picker launcher
    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onReplacePhotoUri(selectedIndex, uri)
            // Reset adjustment sliders
            brightness = 0f
            contrast = 1f
            saturation = 1f
            warmth = 0f
            rotationDegrees = 0f
            flipHorizontal = false
            flipVertical = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .clip(RoundedCornerShape(26.dp))
                .border(1.5.dp, Brush.horizontalGradient(listOf(StudioViolet, StudioNeonPink)), RoundedCornerShape(26.dp)),
            color = StudioBackground,
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(StudioViolet, StudioNeonPink))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Sunting Gambar",
                                color = StudioTextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sesuaikan warna, rotasi, atau ganti foto per slot",
                                color = StudioTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = StudioTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Photo Slot Selector Strip
                Text(
                    text = "PILIH FOTO UNTUK DISUNTING:",
                    color = StudioVioletLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    itemsIndexed(photos) { index, photo ->
                        val isSlotSelected = index == selectedIndex
                        val slotBorderColor by animateColorAsState(
                            targetValue = if (isSlotSelected) StudioNeonPink else Color(0x33FFFFFF),
                            label = "slot_border"
                        )

                        Card(
                            modifier = Modifier
                                .size(width = 72.dp, height = 76.dp)
                                .clickable { selectedIndex = index }
                                .testTag("photo_editor_slot_$index"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSlotSelected) StudioSurfaceVariant else StudioSurface
                            ),
                            border = BorderStroke(if (isSlotSelected) 2.dp else 1.dp, slotBorderColor)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(
                                    bitmap = photo.asImageBitmap(),
                                    contentDescription = "Foto ${index + 1}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Slot Label
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.7f))
                                        .padding(vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#${index + 1}",
                                        color = if (isSlotSelected) StudioNeonPink else Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (isSlotSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(3.dp)
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(StudioNeonPink),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Central Interactive Preview Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(StudioSurface)
                        .border(1.dp, StudioBorderGlass, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap!!.asImageBitmap(),
                            contentDescription = "Preview Foto #${selectedIndex + 1}",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp)
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        CircularProgressIndicator(
                            color = StudioNeonPink,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Compare Badge (Hold to view original)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .clickable(
                                interactionSource = compareInteractionSource,
                                indication = null
                            ) { }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Contrast,
                                contentDescription = null,
                                tint = if (isHoldingCompare) StudioAmber else Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isHoldingCompare) "Foto Asli (Tahan)" else "Tahan Cek Asli",
                                color = if (isHoldingCompare) StudioAmber else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Reset adjustments for this photo
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.75f))
                            .clickable {
                                brightness = 0f
                                contrast = 1f
                                saturation = 1f
                                warmth = 0f
                                rotationDegrees = 0f
                                flipHorizontal = false
                                flipVertical = false
                                onResetPhoto(selectedIndex)
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Nilai",
                            tint = StudioTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Feature Toolbar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FeatureIcon(
                        icon = Icons.Default.ColorLens,
                        label = "Filter",
                        isActive = activeFeature == FeatureType.FILTER,
                        onClick = { activeFeature = if (activeFeature == FeatureType.FILTER) FeatureType.NONE else FeatureType.FILTER }
                    )
                    FeatureIcon(
                        icon = Icons.Default.WbSunny,
                        label = "Cerah",
                        isActive = activeFeature == FeatureType.BRIGHTNESS,
                        onClick = { activeFeature = if (activeFeature == FeatureType.BRIGHTNESS) FeatureType.NONE else FeatureType.BRIGHTNESS }
                    )
                    FeatureIcon(
                        icon = Icons.Default.Contrast,
                        label = "Kontras",
                        isActive = activeFeature == FeatureType.CONTRAST,
                        onClick = { activeFeature = if (activeFeature == FeatureType.CONTRAST) FeatureType.NONE else FeatureType.CONTRAST }
                    )
                    FeatureIcon(
                        icon = Icons.Default.AutoAwesome,
                        label = "Kulit",
                        isActive = activeFeature == FeatureType.SKIN,
                        onClick = { activeFeature = if (activeFeature == FeatureType.SKIN) FeatureType.NONE else FeatureType.SKIN }
                    )
                    FeatureIcon(
                        icon = Icons.Default.SwapHoriz,
                        label = "Transform",
                        isActive = activeFeature == FeatureType.TRANSFORM,
                        onClick = { activeFeature = if (activeFeature == FeatureType.TRANSFORM) FeatureType.NONE else FeatureType.TRANSFORM }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Feature Content Panel
                if (activeFeature != FeatureType.NONE) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(StudioSurfaceVariant.copy(alpha = 0.45f))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (activeFeature) {
                            FeatureType.FILTER -> PresetTonePanel(
                                onSelectPreset = { b, c, s, w ->
                                    brightness = b
                                    contrast = c
                                    saturation = s
                                    warmth = w
                                }
                            )
                            FeatureType.BRIGHTNESS -> AdjustmentSliderRow(
                                icon = Icons.Default.WbSunny,
                                label = "Kecerahan",
                                valueText = "${(brightness * 100).toInt()}%",
                                value = brightness,
                                valueRange = -0.6f..0.6f,
                                onValueChange = { brightness = it },
                                onReset = { brightness = 0f }
                            )
                            FeatureType.CONTRAST -> AdjustmentSliderRow(
                                icon = Icons.Default.Contrast,
                                label = "Kontras",
                                valueText = "${((contrast - 1f) * 100).toInt()}%",
                                value = contrast,
                                valueRange = 0.5f..1.8f,
                                onValueChange = { contrast = it },
                                onReset = { contrast = 1f }
                            )
                            FeatureType.SKIN -> AdjustmentSliderRow(
                                icon = Icons.Default.AutoAwesome,
                                label = "Kulit Mulus",
                                valueText = "${(skinSmoothing * 100).toInt()}%",
                                value = skinSmoothing,
                                valueRange = 0f..1f,
                                onValueChange = { skinSmoothing = it },
                                onReset = { skinSmoothing = 0f }
                            )
                            FeatureType.TRANSFORM -> TransformToolsPanel(
                                rotation = rotationDegrees,
                                onRotateLeft = { rotationDegrees = (rotationDegrees - 90f) % 360f },
                                onRotateRight = { rotationDegrees = (rotationDegrees + 90f) % 360f },
                                flipHorizontal = flipHorizontal,
                                onToggleFlipH = { flipHorizontal = !flipHorizontal },
                                flipVertical = flipVertical,
                                onToggleFlipV = { flipVertical = !flipVertical },
                                onPickFromGallery = { galleryPicker.launch("image/*") },
                                onRetakeCamera = {
                                    onRetakeCamera?.invoke(selectedIndex)
                                    onDismiss()
                                }
                            )
                            else -> {}
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            onApplyToAll(
                                brightness,
                                contrast,
                                saturation,
                                warmth,
                                rotationDegrees,
                                flipHorizontal,
                                flipVertical
                            )
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, StudioVioletLight),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioVioletLight)
                    ) {
                        Text("Semua Foto", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            if (previewBitmap != null) {
                                onSavePhoto(selectedIndex, previewBitmap!!)
                            }
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioNeonPink
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}


@Composable
private fun AdjustmentSliderRow(
    icon: ImageVector,
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = StudioVioletLight,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = StudioTextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(76.dp)
        )

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = StudioNeonPink,
                activeTrackColor = StudioNeonPink,
                inactiveTrackColor = Color(0x33FFFFFF)
            )
        )

        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = valueText,
            color = StudioTextSecondary,
            fontSize = 10.sp,
            modifier = Modifier
                .width(36.dp)
                .clickable { onReset() },
            textAlign = TextAlign.End
        )
    }
}

/**
 * Transform tools panel: Rotate 90, Flip Mirror, and Replace from Gallery
 */
@Composable
private fun TransformToolsPanel(
    rotation: Float,
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit,
    flipHorizontal: Boolean,
    onToggleFlipH: () -> Unit,
    flipVertical: Boolean,
    onToggleFlipV: () -> Unit,
    onPickFromGallery: () -> Unit,
    onRetakeCamera: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Retake via camera
        TransformToolButton(
            icon = Icons.Default.CameraAlt,
            label = "Retake",
            accentColor = StudioNeonCyan,
            onClick = onRetakeCamera
        )

        // Rotate -90
        TransformToolButton(
            icon = Icons.AutoMirrored.Filled.RotateLeft,
            label = "Putar Kiri",
            onClick = onRotateLeft
        )

        // Rotate +90
        TransformToolButton(
            icon = Icons.AutoMirrored.Filled.RotateRight,
            label = "Putar Kanan",
            onClick = onRotateRight
        )

        // Mirror Horizontal
        TransformToolButton(
            icon = Icons.Default.SwapHoriz,
            label = "Cermin",
            isActive = flipHorizontal,
            onClick = onToggleFlipH
        )

        // Flip Vertical
        TransformToolButton(
            icon = Icons.Default.SwapVert,
            label = "Flip V",
            isActive = flipVertical,
            onClick = onToggleFlipV
        )

        // Replace from gallery
        TransformToolButton(
            icon = Icons.Default.AddPhotoAlternate,
            label = "Galeri",
            accentColor = StudioNeonPink,
            onClick = onPickFromGallery
        )
    }
}

@Composable
private fun FeatureIcon(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isActive) StudioNeonPink else StudioSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.White else StudioTextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isActive) StudioNeonPink else StudioTextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TransformToolButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean = false,
    accentColor: Color = StudioVioletLight,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (isActive) accentColor else Color(0x22FFFFFF))
                .border(1.dp, if (isActive) Color.Transparent else Color(0x33FFFFFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.White else accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = StudioTextPrimary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


/**
 * Preset tone quick filters (Vivid, Warm Vintage, Soft Glow, B&W Drama)
 */
@Composable
private fun PresetTonePanel(
    onSelectPreset: (brightness: Float, contrast: Float, saturation: Float, warmth: Float) -> Unit
) {
    val presets = listOf(
        PresetOption("Natural", 0f, 1f, 1f, 0f, StudioEmerald),
        PresetOption("Auto-Fix", 0.08f, 1.15f, 1.15f, 0.05f, StudioNeonPink),
        PresetOption("Warm Glow", 0.05f, 1.10f, 1.25f, 0.35f, StudioAmber),
        PresetOption("Cool Cinema", 0.06f, 1.15f, 0.95f, -0.30f, StudioNeonCyan),
        PresetOption("Vintage Film", 0.10f, 0.95f, 0.85f, 0.20f, StudioVioletLight),
        PresetOption("B&W Drama", 0.04f, 1.30f, 0.0f, 0f, Color.White)
    )

    LazyRow(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(presets.size) { i ->
            val preset = presets[i]
            Card(
                modifier = Modifier
                    .clickable {
                        onSelectPreset(
                            preset.brightness,
                            preset.contrast,
                            preset.saturation,
                            preset.warmth
                        )
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                border = BorderStroke(1.dp, preset.accentColor.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(preset.accentColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = preset.name,
                        color = StudioTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private data class PresetOption(
    val name: String,
    val brightness: Float,
    val contrast: Float,
    val saturation: Float,
    val warmth: Float,
    val accentColor: Color
)
