package com.example.ui.template

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.CustomLayoutConfig
import com.example.model.PngSlotRect
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioNeonCyan
import com.example.ui.theme.StudioNeonPink
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioViolet
import com.example.ui.theme.StudioVioletLight
import com.example.util.PngLayoutDetector

@Composable
fun PngLayoutImportDialog(
    initialConfig: CustomLayoutConfig,
    pngBitmap: Bitmap,
    isEditMode: Boolean = false,
    onDismiss: () -> Unit,
    onSaveLayout: (CustomLayoutConfig) -> Unit,
    onReplacePng: ((Uri, (CustomLayoutConfig, Bitmap) -> Unit) -> Unit)? = null
) {
    var currentConfig by remember(initialConfig) { mutableStateOf(initialConfig) }
    var currentBitmap by remember(pngBitmap) { mutableStateOf(pngBitmap) }
    var layoutName by remember { mutableStateOf(initialConfig.name) }
    var currentSlots by remember { mutableStateOf(initialConfig.pngSlots) }
    var selectedPresetCount by remember { mutableStateOf(initialConfig.pngSlots.size) }
    var showSamplePhotos by remember { mutableStateOf(true) }
    var slotCornerRadius by remember { mutableFloatStateOf(initialConfig.cornerRadiusDp.coerceIn(0f, 32f)) }
    var selectedSlotIndex by remember { mutableStateOf<Int?>(null) }

    val origW = currentBitmap.width
    val origH = currentBitmap.height
    val imgAspectRatio = (origW.toFloat() / origH.toFloat()).coerceIn(0.25f, 2.5f)

    // Image Picker for replacing the PNG file during edit
    val replacePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && onReplacePng != null) {
            onReplacePng(uri) { newConfig, newBitmap ->
                currentBitmap = newBitmap
                currentConfig = newConfig.copy(
                    id = currentConfig.id,
                    name = layoutName
                )
                currentSlots = newConfig.pngSlots
                selectedPresetCount = newConfig.pngSlots.size
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(StudioViolet, StudioNeonPink)),
                width = 1.5.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
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
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(StudioViolet, StudioNeonPink))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.Layers,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isEditMode) "Sunting Layout PNG" else "Konfigurasi Layout PNG",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioTextPrimary
                                )
                                if (isEditMode) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(StudioNeonCyan.copy(alpha = 0.25f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "EDIT MODE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StudioNeonCyan
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Resolusi: ${origW} x ${origH} px • ${currentSlots.size} Slot Foto",
                                fontSize = 11.sp,
                                color = StudioTextSecondary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onReplacePng != null) {
                            IconButton(
                                onClick = { replacePickerLauncher.launch("image/*") },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(StudioViolet.copy(alpha = 0.25f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Ganti Gambar PNG",
                                    tint = StudioNeonPink,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0x22FFFFFF))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = StudioTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Detection status pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (currentSlots.isNotEmpty()) StudioViolet.copy(alpha = 0.20f)
                            else Color(0x33F59E0B)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (currentSlots.isNotEmpty()) StudioNeonPink else Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentSlots.isNotEmpty()) {
                                "Terdeteksi ${currentSlots.size} kotak transparan! Foto akan otomatis mengisi kotak ini."
                            } else {
                                "Pilih jumlah slot foto di bawah untuk template gambar ini:"
                            },
                            fontSize = 11.sp,
                            color = StudioTextPrimary,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Real-Time Visualizer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp, max = 290.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .padding(10.dp)
                            .aspectRatio(imgAspectRatio)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                    ) {
                        val containerW = maxWidth
                        val containerH = maxHeight

                        // 1. Underneath layer: Transparent cutouts
                        if (showSamplePhotos) {
                            currentSlots.forEachIndexed { _, slot ->
                                val slotLeft = containerW * slot.left
                                val slotTop = containerH * slot.top
                                val slotW = containerW * (slot.right - slot.left)
                                val slotH = containerH * (slot.bottom - slot.top)

                                Box(
                                    modifier = Modifier
                                        .offset(x = slotLeft, y = slotTop)
                                        .size(width = slotW, height = slotH)
                                        .clip(RoundedCornerShape(slotCornerRadius.dp))
                                        .background(Color.Transparent)
                                        .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(slotCornerRadius.dp))
                                )
                            }
                        }

                        // 2. Middle layer: The actual external PNG image with transparent cutouts
                        Image(
                            bitmap = currentBitmap.asImageBitmap(),
                            contentDescription = "PNG Layout Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )

                        // 3. Top layer: Highlight outlines & numbering badges for cutouts (interactive)
                        currentSlots.forEachIndexed { idx, slot ->
                            val slotLeft = containerW * slot.left
                            val slotTop = containerH * slot.top
                            val slotW = containerW * (slot.right - slot.left)
                            val slotH = containerH * (slot.bottom - slot.top)
                            val isSlotSelected = (selectedSlotIndex == idx)

                            Box(
                                modifier = Modifier
                                    .offset(x = slotLeft, y = slotTop)
                                    .size(width = slotW, height = slotH)
                                    .border(
                                        width = if (isSlotSelected) 2.5.dp else 1.5.dp,
                                        color = if (isSlotSelected) StudioNeonCyan else StudioNeonPink,
                                        shape = RoundedCornerShape(slotCornerRadius.dp)
                                    )
                                    .clickable {
                                        selectedSlotIndex = if (selectedSlotIndex == idx) null else idx
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSlotSelected) StudioNeonCyan else StudioNeonPink)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "#${idx + 1}${if (isSlotSelected) " ★" else ""}",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Slot Selector & Quick Navigation Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Slot:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioTextSecondary
                        )
                        currentSlots.forEachIndexed { idx, _ ->
                            val isCur = (selectedSlotIndex == idx)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isCur) StudioNeonCyan else StudioSurfaceVariant)
                                    .clickable { selectedSlotIndex = if (isCur) null else idx }
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "#${idx + 1}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCur) Color.Black else StudioTextPrimary
                                )
                            }
                        }
                    }

                    // Add new slot button
                    OutlinedButton(
                        onClick = {
                            val newIdx = currentSlots.size
                            val newSlot = PngSlotRect(
                                left = 0.12f,
                                top = 0.12f,
                                right = 0.88f,
                                bottom = 0.45f,
                                index = newIdx
                            )
                            currentSlots = currentSlots + newSlot
                            selectedSlotIndex = newIdx
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, StudioVioletLight),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = StudioVioletLight,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("+ Slot", fontSize = 10.sp, color = StudioVioletLight, fontWeight = FontWeight.Bold)
                    }
                }

                // Dedicated Slot Position / Size Nudge Panel (when a slot is selected)
                if (selectedSlotIndex != null && selectedSlotIndex!! < currentSlots.size) {
                    val sIdx = selectedSlotIndex!!
                    val curSlot = currentSlots[sIdx]

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D33)),
                        border = BorderStroke(1.dp, StudioNeonCyan.copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = StudioNeonCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Sunting Posisi & Ukuran Slot #${sIdx + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioNeonCyan
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x22FFFFFF))
                                        .clickable { selectedSlotIndex = null }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Tutup", fontSize = 9.sp, color = StudioTextSecondary)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Directional D-Pad
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Geser Posisi", fontSize = 9.sp, color = StudioTextSecondary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    // Up
                                    IconButton(
                                        onClick = {
                                            val step = 0.015f
                                            val newTop = (curSlot.top - step).coerceAtLeast(0f)
                                            val newBottom = (curSlot.bottom - step).coerceAtLeast(newTop + 0.05f)
                                            val updated = currentSlots.toMutableList()
                                            updated[sIdx] = curSlot.copy(top = newTop, bottom = newBottom)
                                            currentSlots = updated
                                        },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(StudioSurfaceVariant)
                                    ) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = "Geser Atas", tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        // Left
                                        IconButton(
                                            onClick = {
                                                val step = 0.015f
                                                val newLeft = (curSlot.left - step).coerceAtLeast(0f)
                                                val newRight = (curSlot.right - step).coerceAtLeast(newLeft + 0.05f)
                                                val updated = currentSlots.toMutableList()
                                                updated[sIdx] = curSlot.copy(left = newLeft, right = newRight)
                                                currentSlots = updated
                                            },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(StudioSurfaceVariant)
                                        ) {
                                            Icon(Icons.Default.ArrowBack, contentDescription = "Geser Kiri", tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                        // Down
                                        IconButton(
                                            onClick = {
                                                val step = 0.015f
                                                val newBottom = (curSlot.bottom + step).coerceAtMost(1f)
                                                val newTop = (curSlot.top + step).coerceAtMost(newBottom - 0.05f)
                                                val updated = currentSlots.toMutableList()
                                                updated[sIdx] = curSlot.copy(top = newTop, bottom = newBottom)
                                                currentSlots = updated
                                            },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(StudioSurfaceVariant)
                                        ) {
                                            Icon(Icons.Default.ArrowDownward, contentDescription = "Geser Bawah", tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                        // Right
                                        IconButton(
                                            onClick = {
                                                val step = 0.015f
                                                val newRight = (curSlot.right + step).coerceAtMost(1f)
                                                val newLeft = (curSlot.left + step).coerceAtMost(newRight - 0.05f)
                                                val updated = currentSlots.toMutableList()
                                                updated[sIdx] = curSlot.copy(left = newLeft, right = newRight)
                                                currentSlots = updated
                                            },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(StudioSurfaceVariant)
                                        ) {
                                            Icon(Icons.Default.ArrowForward, contentDescription = "Geser Kanan", tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }

                                // Scale & Delete
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Ukuran & Aksi", fontSize = 9.sp, color = StudioTextSecondary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        // Zoom In (Expand)
                                        IconButton(
                                            onClick = {
                                                val step = 0.012f
                                                val updated = currentSlots.toMutableList()
                                                updated[sIdx] = curSlot.copy(
                                                    left = (curSlot.left - step).coerceAtLeast(0f),
                                                    top = (curSlot.top - step).coerceAtLeast(0f),
                                                    right = (curSlot.right + step).coerceAtMost(1f),
                                                    bottom = (curSlot.bottom + step).coerceAtMost(1f)
                                                )
                                                currentSlots = updated
                                            },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(StudioSurfaceVariant)
                                        ) {
                                            Icon(Icons.Default.ZoomIn, contentDescription = "Perbesar Slot", tint = StudioNeonCyan, modifier = Modifier.size(16.dp))
                                        }
                                        // Zoom Out (Shrink)
                                        IconButton(
                                            onClick = {
                                                val step = 0.012f
                                                val w = curSlot.right - curSlot.left
                                                val h = curSlot.bottom - curSlot.top
                                                if (w > 0.08f && h > 0.08f) {
                                                    val updated = currentSlots.toMutableList()
                                                    updated[sIdx] = curSlot.copy(
                                                        left = curSlot.left + step,
                                                        top = curSlot.top + step,
                                                        right = curSlot.right - step,
                                                        bottom = curSlot.bottom - step
                                                    )
                                                    currentSlots = updated
                                                }
                                            },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(StudioSurfaceVariant)
                                        ) {
                                            Icon(Icons.Default.ZoomOut, contentDescription = "Perkecil Slot", tint = StudioVioletLight, modifier = Modifier.size(16.dp))
                                        }
                                        // Delete Slot
                                        if (currentSlots.size > 1) {
                                            IconButton(
                                                onClick = {
                                                    val updated = currentSlots.toMutableList()
                                                    updated.removeAt(sIdx)
                                                    currentSlots = updated.mapIndexed { newIdx, item -> item.copy(index = newIdx) }
                                                    selectedSlotIndex = null
                                                },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0x33EC4899))
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Hapus Slot", tint = StudioNeonPink, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = StudioVioletLight,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ketuk kotak foto pada pratinjau untuk menyunting posisi/ukuran lubang",
                            fontSize = 10.sp,
                            color = StudioTextSecondary.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Toggle Sample Photo Preview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSamplePhotos = !showSamplePhotos }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (showSamplePhotos) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = StudioVioletLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Simulasikan Foto Di Kotak Transparan",
                            fontSize = 12.sp,
                            color = StudioTextPrimary
                        )
                    }
                    Switch(
                        checked = showSamplePhotos,
                        onCheckedChange = { showSamplePhotos = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = StudioNeonPink,
                            uncheckedTrackColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Slot presets selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "PILIHAN JUMLAH SLOT FOTO:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextSecondary
                    )

                    OutlinedButton(
                        onClick = {
                            val detected = PngLayoutDetector.detectTransparentSlots(currentBitmap)
                            if (detected.isNotEmpty()) {
                                currentSlots = detected
                                selectedPresetCount = detected.size
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = StudioNeonCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Deteksi Ulang", fontSize = 10.sp, color = StudioNeonCyan)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        initialConfig.pngSlots.size to "Auto (${initialConfig.pngSlots.size})",
                        1 to "1 Foto",
                        2 to "2 Foto",
                        3 to "3 Strip",
                        4 to "4 Grid",
                        6 to "6 Grid"
                    ).distinctBy { it.first }

                    presets.forEach { (count, label) ->
                        val isSelected = (currentSlots.size == count && selectedPresetCount == count)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) StudioViolet else StudioSurfaceVariant)
                                .clickable {
                                    selectedPresetCount = count
                                    currentSlots = if (count == initialConfig.pngSlots.size) {
                                        initialConfig.pngSlots
                                    } else {
                                        PngLayoutDetector.createPresetSlots(count)
                                    }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else StudioTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Corner radius adjustment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kelengkungan Sudut Foto",
                        fontSize = 11.sp,
                        color = StudioTextSecondary
                    )
                    Text(
                        text = "${slotCornerRadius.toInt()} dp",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioNeonPink
                    )
                }

                Slider(
                    value = slotCornerRadius,
                    onValueChange = { slotCornerRadius = it },
                    valueRange = 0f..28f,
                    colors = SliderDefaults.colors(
                        thumbColor = StudioNeonPink,
                        activeTrackColor = StudioViolet,
                        inactiveTrackColor = Color(0x33FFFFFF)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Layout Name Field
                OutlinedTextField(
                    value = layoutName,
                    onValueChange = { layoutName = it },
                    label = { Text("Nama Template Frame PNG") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioViolet,
                        unfocusedBorderColor = Color(0x44FFFFFF),
                        focusedLabelColor = StudioVioletLight,
                        unfocusedLabelColor = StudioTextSecondary,
                        focusedTextColor = StudioTextPrimary,
                        unfocusedTextColor = StudioTextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(Color(0x44FFFFFF), Color(0x22FFFFFF)))
                        )
                    ) {
                        Text("Batal", color = StudioTextSecondary, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val finalConfig = currentConfig.copy(
                                name = if (layoutName.isNotBlank()) layoutName.trim() else "Frame PNG",
                                photoCount = currentSlots.size,
                                columns = if (currentSlots.size <= 3) 1 else 2,
                                pngSlots = currentSlots,
                                cornerRadiusDp = slotCornerRadius,
                                isPngOverlay = true,
                                overlayAspectRatio = (currentBitmap.height.toFloat() / currentBitmap.width.toFloat()).coerceIn(0.5f, 3.5f)
                            )
                            onSaveLayout(finalConfig)
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.horizontalGradient(listOf(StudioViolet, StudioNeonPink))),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEditMode) "Simpan Perubahan" else "Simpan & Gunakan",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
