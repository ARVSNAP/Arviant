package com.example.ui.preview

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import coil.compose.AsyncImage
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterHdr
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Print
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Payment
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ColorFilterType
import com.example.model.FrameBackgroundPattern
import com.example.model.FrameBorderStyle
import com.example.model.FrameColorOption
import com.example.model.ImageFrameStyle
import com.example.model.PRESET_FRAME_COLORS
import com.example.model.PRESET_IMAGE_FRAMES
import com.example.model.PRESET_STICKERS
import com.example.model.PhotoBoothUiState
import com.example.model.SaveStatus
import com.example.model.CloudinaryUploadState
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioNeonPink
import com.example.ui.theme.StudioNeonCyan
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceGlass
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioViolet
import com.example.ui.theme.StudioVioletLight
import com.example.util.BitmapUtils
import com.example.util.CloudinaryService
import com.example.util.QrCodeUtils

import androidx.compose.material.icons.filled.Remove
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Repeat
import com.example.model.PrintPaperSize
import com.example.model.LoopMode
import com.example.model.LoopSpeed
import com.example.model.LoopTransition
import com.example.model.LoopParticleEffect
import com.example.model.LoopAnimationConfig
import com.example.model.PhotoScaleMode
import com.example.model.PhotoAlignment
import com.example.model.PhotoSlotAdjustment
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewResultScreen(
    uiState: PhotoBoothUiState,
    onRetakeAll: () -> Unit,
    onRetakeSinglePhoto: (Int) -> Unit = {},
    onSelectFilter: (ColorFilterType) -> Unit,
    onSelectFrameColor: (FrameColorOption) -> Unit,
    onUpdateCaption: (String) -> Unit,
    onToggleDate: () -> Unit,
    onSelectSticker: (String) -> Unit,
    onSaveToGallery: () -> Unit,
    onSharePhoto: () -> Unit,
    onShareToApp: (String) -> Unit,
    onPrintPhoto: (copies: Int, paperSize: PrintPaperSize, duplicateDualStrip: Boolean) -> Unit = { _, _, _ -> },
    onToggleQrDialog: (Boolean) -> Unit,
    onResetSaveStatus: () -> Unit,
    onUploadToCloudinary: () -> Unit = {},
    onSelectImageFrame: (ImageFrameStyle) -> Unit = {},
    onOpenCustomFrameDesigner: (ImageFrameStyle?) -> Unit = {},
    onCloseCustomFrameDesigner: () -> Unit = {},
    onUpdateCustomFrameConfig: (ImageFrameStyle) -> Unit = {},
    onSaveCustomFrame: (ImageFrameStyle) -> Unit = {},
    onDeleteCustomFrame: (String) -> Unit = {},
    onToggleLoopView: (Boolean) -> Unit = {},
    onSetLoopMode: (LoopMode) -> Unit = {},
    onSetLoopSpeed: (LoopSpeed) -> Unit = {},
    onSetLoopTransition: (LoopTransition) -> Unit = {},
    onSetLoopParticle: (LoopParticleEffect) -> Unit = {},
    onToggleLoopPlaying: () -> Unit = {},
    onSaveGif: () -> Unit = {},
    onShareGif: () -> Unit = {},
    onSaveMp4: () -> Unit = {},
    onShareMp4: () -> Unit = {},
    onShareToSpecificApp: (String, String) -> Unit = { _, _ -> },
    onShowFullscreenLoop: (Boolean) -> Unit = {},
    onOpenPhotoEditor: (Int) -> Unit = {},
    onClosePhotoEditor: () -> Unit = {},
    onSaveEditedPhoto: (Int, Bitmap) -> Unit = { _, _ -> },
    onApplyAdjustmentsToAll: (Float, Float, Float, Float, Float, Boolean, Boolean) -> Unit = { _, _, _, _, _, _, _ -> },
    onResetPhotoToOriginal: (Int) -> Unit = {},
    onReplacePhotoUri: (Int, Uri) -> Unit = { _, _ -> },
    onUpdatePhotoScaleMode: (PhotoScaleMode) -> Unit = {},
    onUpdatePhotoAlignment: (PhotoAlignment) -> Unit = {},
    onUpdatePhotoPan: (Float, Float) -> Unit = { _, _ -> },
    onUpdatePhotoZoom: (Float) -> Unit = {},
    onUpdateSlotBgColor: (Long) -> Unit = {},
    onUpdateSlotAdjustment: (Int, PhotoSlotAdjustment) -> Unit = { _, _ -> },
    onResetSlotAdjustments: () -> Unit = {},
    onSetCustomCanvasSettings: (Boolean, String) -> Unit = { _, _ -> },
    onSaveCustomCanvasImage: (Uri, (String) -> Unit) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showShareOptionsDialog by remember { mutableStateOf(false) }
    var showPrintConfigDialog by remember { mutableStateOf(false) }
    var showRetakeDialog by remember { mutableStateOf(false) }
    var slotToReplaceByGallery by remember { mutableIntStateOf(0) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onReplacePhotoUri(slotToReplaceByGallery, uri)
        }
    }

    val canvasImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onSaveCustomCanvasImage(uri) { savedPath ->
                onSetCustomCanvasSettings(true, savedPath)
            }
        }
    }

    val tabTitles = listOf("Sunting Foto", "Filter Warna", "Warna Canvas", "Teks & Acara")
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.saveStatus) {
        when (val status = uiState.saveStatus) {
            is SaveStatus.Error -> {
                Toast.makeText(context, status.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    LaunchedEffect(uiState.gifSaveStatus) {
        when (val status = uiState.gifSaveStatus) {
            is SaveStatus.Success -> {
                Toast.makeText(context, "Animasi GIF berhasil disimpan ke Galeri! (${status.fileName})", Toast.LENGTH_SHORT).show()
                onResetSaveStatus()
            }
            is SaveStatus.Error -> {
                Toast.makeText(context, status.message, Toast.LENGTH_LONG).show()
                onResetSaveStatus()
            }
            else -> {}
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Photo Strip Studio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = StudioTextPrimary
                        )
                        Text(
                            text = uiState.selectedLayout.displayName,
                            fontSize = 12.sp,
                            color = StudioVioletLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onRetakeAll,
                        modifier = Modifier.testTag("retake_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Sesi Baru",
                            tint = StudioTextPrimary
                        )
                    }
                },
                actions = {
                    // Cetak Photo Strip Direct
                    IconButton(
                        onClick = { showPrintConfigDialog = true },
                        modifier = Modifier.testTag("print_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Cetak Photo Strip",
                            tint = StudioEmerald
                        )
                    }
                    // QR Code Download Mockup Button
                    IconButton(
                        onClick = { onToggleQrDialog(true) },
                        modifier = Modifier.testTag("qr_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "QR Code Kiosk",
                            tint = StudioVioletLight
                        )
                    }
                    // Tombol Toggle Cepat Loop Animasi
                    IconButton(
                        onClick = { onToggleLoopView(!uiState.isLoopViewActive) },
                        modifier = Modifier.testTag("loop_toggle_top_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isLoopViewActive) Icons.Default.PhotoLibrary else Icons.Default.Movie,
                            contentDescription = if (uiState.isLoopViewActive) "Strip Cetak" else "Loop Animasi",
                            tint = if (uiState.isLoopViewActive) StudioNeonPink else Color(0xFFFFB703)
                        )
                    }
                    IconButton(
                        onClick = onSharePhoto,
                        modifier = Modifier.testTag("share_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Bagikan",
                            tint = StudioNeonPink
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xCC0F172A)
                )
            )
        },
        bottomBar = {
            // Bottom Action Bar
            ResultBottomActionBar(
                isSaving = uiState.saveStatus is SaveStatus.Saving,
                isLoopViewActive = uiState.isLoopViewActive,
                isGeneratingGif = uiState.isGeneratingGif,
                onSaveToGallery = onSaveToGallery,
                onSaveGif = onSaveGif,
                onPrint = { showPrintConfigDialog = true },
                onShare = {
                    if (uiState.isLoopViewActive) {
                        onShareGif()
                    } else {
                        showShareOptionsDialog = true
                    }
                },
                onOpenQr = { onToggleQrDialog(true) },
                onRetake = onRetakeAll
            )
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Left Side: Preview Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (uiState.isLoopViewActive) {
                    LoopAnimationPlayer(
                        photos = uiState.capturedPhotos,
                        filter = uiState.selectedFilter,
                        frameColor = uiState.selectedFrameColor,
                        captionText = uiState.captionText,
                        stickerEmoji = uiState.selectedSticker,
                        loopConfig = uiState.loopConfig,
                        isGeneratingGif = uiState.isGeneratingGif,
                        isGeneratingMp4 = uiState.isGeneratingMp4,
                        onSetLoopMode = onSetLoopMode,
                        onSetLoopSpeed = onSetLoopSpeed,
                        onSetLoopTransition = onSetLoopTransition,
                        onSetLoopParticle = onSetLoopParticle,
                        onTogglePlay = onToggleLoopPlaying,
                        onSaveGif = onSaveGif,
                        onShareGif = onShareGif,
                        onSaveMp4 = onSaveMp4,
                        onShareMp4 = onShareMp4,
                        onShareToSpecificApp = onShareToSpecificApp,
                        onOpenFullscreen = { onShowFullscreenLoop(true) }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(450.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.finalStripBitmap != null) {
                            Card(
                                modifier = Modifier
                                    .shadow(28.dp, RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0x338B5CF6), RoundedCornerShape(16.dp))
                                    .testTag("final_strip_preview"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                            ) {
                                Image(
                                    bitmap = uiState.finalStripBitmap.asImageBitmap(),
                                    contentDescription = "Photo Strip Canvas",
                                    modifier = Modifier
                                        .widthIn(max = 320.dp)
                                        .fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        if (uiState.isRenderingStrip) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xCC0F172A))
                                    .border(2.dp, StudioViolet, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = StudioViolet,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Right Side: Editing Menu (Tabs)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Segmented Mode Switcher: Strip Foto (Cetak) vs Loop Animasi (Boomerang)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(StudioSurface)
                        .border(1.dp, Color(0x338B5CF6), RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!uiState.isLoopViewActive) StudioViolet else Color.Transparent)
                            .clickable { onToggleLoopView(false) }
                            .padding(vertical = 9.dp)
                            .testTag("tab_strip_view"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = if (!uiState.isLoopViewActive) Color.White else StudioTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Strip Foto (Cetak)",
                                fontSize = 12.sp,
                                fontWeight = if (!uiState.isLoopViewActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (!uiState.isLoopViewActive) Color.White else StudioTextSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (uiState.isLoopViewActive) StudioNeonPink else Color.Transparent)
                            .clickable { onToggleLoopView(true) }
                            .padding(vertical = 9.dp)
                            .testTag("tab_loop_view"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = if (uiState.isLoopViewActive) Color.White else StudioTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Loop Animasi 🎬",
                                fontSize = 12.sp,
                                fontWeight = if (uiState.isLoopViewActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (uiState.isLoopViewActive) Color.White else StudioTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                
                // Quick Action Buttons: Retake Foto & Posisi Foto (Moved to right menu)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Button Retake Opsi
                    Button(
                        onClick = { showRetakeDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_retake_dialog_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioSurfaceVariant
                        ),
                        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(StudioNeonPink, StudioAmber)))
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = StudioNeonPink,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Opsi Retake",
                            color = StudioTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Customization Navigation Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = StudioSurface,
                    contentColor = StudioViolet,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = StudioViolet
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) StudioVioletLight else StudioTextSecondary
                                )
                            }
                        )
                    }
                }

                // Tab Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    when (selectedTabIndex) {
                        0 -> PhotoEditingTab(
                            photos = uiState.capturedPhotos,
                            onOpenEditorSlot = onOpenPhotoEditor,
                            onRetakeSlot = onRetakeSinglePhoto,
                            onRetakeAll = onRetakeAll,
                            onReplaceFromGallery = { slotIndex ->
                                slotToReplaceByGallery = slotIndex
                                galleryLauncher.launch("image/*")
                            }
                        )
                        1 -> FilterSelectionTab(
                            selectedFilter = uiState.selectedFilter,
                            onSelectFilter = onSelectFilter
                        )
                        2 -> FrameColorSelectionTab(
                            selectedColor = uiState.selectedFrameColor,
                            onSelectColor = onSelectFrameColor,
                            useCustomCanvasImage = uiState.useCustomCanvasImage,
                            customCanvasImagePath = uiState.customCanvasImagePath,
                            onPickCustomCanvasImage = { canvasImagePickerLauncher.launch("image/*") },
                            onClearCustomCanvasImage = { onSetCustomCanvasSettings(false, "") }
                        )
                        3 -> TextAndEventCustomizationTab(
                            caption = uiState.captionText,
                            onCaptionChange = onUpdateCaption,
                            showDate = uiState.showDate,
                            onToggleDate = onToggleDate,
                            selectedSticker = uiState.selectedSticker,
                            onSelectSticker = onSelectSticker
                        )
                    }
                }
                
                // Add padding at the bottom of the right side menu
                Spacer(modifier = Modifier.height(100.dp))
            }
        }


        // Dialog Sunting Gambar (Photo Editor)
        if (uiState.showPhotoEditorDialog) {
            PhotoEditorDialog(
                photos = uiState.capturedPhotos,
                originalPhotos = uiState.originalCapturedPhotos,
                initialIndex = uiState.editingPhotoIndex,
                onDismiss = onClosePhotoEditor,
                onSavePhoto = onSaveEditedPhoto,
                onApplyToAll = onApplyAdjustmentsToAll,
                onResetPhoto = onResetPhotoToOriginal,
                onReplacePhotoUri = onReplacePhotoUri,
                onRetakeCamera = { slotIndex ->
                    onRetakeSinglePhoto(slotIndex)
                }
            )
        }

        // Dialog Opsi Retake Foto Layout
        if (showRetakeDialog) {
            RetakeChooserDialog(
                photos = uiState.capturedPhotos,
                layoutName = uiState.selectedLayout.displayName,
                onDismiss = { showRetakeDialog = false },
                onRetakeSinglePhoto = { slotIndex ->
                    showRetakeDialog = false
                    onRetakeSinglePhoto(slotIndex)
                },
                onRetakeAll = {
                    showRetakeDialog = false
                    onRetakeAll()
                },
                onPickGalleryForSlot = { slotIndex ->
                    showRetakeDialog = false
                    slotToReplaceByGallery = slotIndex
                    galleryLauncher.launch("image/*")
                }
            )
        }



        // Dialog Fullscreen Loop Theater
        if (uiState.showFullscreenLoop) {
            FullscreenLoopDialog(
                photos = uiState.capturedPhotos,
                filter = uiState.selectedFilter,
                frameColor = uiState.selectedFrameColor,
                captionText = uiState.captionText,
                stickerEmoji = uiState.selectedSticker,
                loopConfig = uiState.loopConfig,
                onClose = { onShowFullscreenLoop(false) },
                onSaveGif = onSaveGif,
                onShareGif = onShareGif
            )
        }

        // Dialog Custom Frame Designer (Bingkai Custom)
        if (uiState.showCustomFrameDesigner) {
            CustomFrameDesignerDialog(
                currentConfig = uiState.customImageFrameConfig,
                onUpdateConfig = onUpdateCustomFrameConfig,
                onSave = onSaveCustomFrame,
                onDismiss = onCloseCustomFrameDesigner
            )
        }

        // Dialog Sukses Simpan ke Galeri
        if (uiState.saveStatus is SaveStatus.Success) {
            val success = uiState.saveStatus as SaveStatus.Success
            SaveSuccessResultDialog(
                fileName = success.fileName,
                onDismiss = onResetSaveStatus,
                onShare = {
                    onResetSaveStatus()
                    onSharePhoto()
                },
                onNewSession = {
                    onResetSaveStatus()
                    onRetakeAll()
                }
            )
        }

        // Dialog QR Code Interaktif & Download Kiosk
        if (uiState.showQrCodeDialog) {
            val context = LocalContext.current
            val clipboardManager = LocalClipboardManager.current
            val sessionId = remember { "PB-${System.currentTimeMillis().toString().takeLast(6)}" }
            val activeCloudinaryUrl = uiState.uploadedCloudinaryUrl ?: "https://res.cloudinary.com/${uiState.settings.cloudinaryCloudName}/image/upload/photobooth"

            KioskQrDownloadDialog(
                eventTitle = uiState.settings.eventTitle,
                sessionId = sessionId,
                cloudinaryUrl = activeCloudinaryUrl,
                qrisPaymentUrl = uiState.settings.qrisPaymentUrl,
                useCustomQrisImage = uiState.settings.useCustomQrisImage,
                customQrisImagePath = uiState.settings.customQrisImagePath,
                uploadState = uiState.cloudinaryUploadState,
                onUploadCloudinary = onUploadToCloudinary,
                onSave = {
                    onSaveToGallery()
                    onToggleQrDialog(false)
                },
                onShare = {
                    onSharePhoto()
                    onToggleQrDialog(false)
                },
                onOpenBrowser = { url ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Tidak dapat membuka browser: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                },
                onCopyLink = { url ->
                    clipboardManager.setText(AnnotatedString(url))
                    Toast.makeText(context, "Link berhasil disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { onToggleQrDialog(false) }
            )
        }

        // Dialog Share Options (WhatsApp, Telegram, Print, General)
        if (showShareOptionsDialog) {
            ShareOptionsDialog(
                onWhatsApp = {
                    showShareOptionsDialog = false
                    onShareToApp("com.whatsapp")
                },
                onTelegram = {
                    showShareOptionsDialog = false
                    onShareToApp("org.telegram.messenger")
                },
                onPrint = {
                    showShareOptionsDialog = false
                    showPrintConfigDialog = true
                },
                onGeneralShare = {
                    showShareOptionsDialog = false
                    onSharePhoto()
                },
                onDismiss = { showShareOptionsDialog = false }
            )
        }

        // Dialog Konfigurasi Cetak Photo Strip
        if (showPrintConfigDialog) {
            PrintConfigDialog(
                defaultCopies = uiState.settings.defaultPrintCopies,
                defaultPaperSize = uiState.settings.defaultPaperSize,
                defaultDuplicateDual = uiState.settings.duplicateDualStrip,
                finalBitmap = uiState.finalStripBitmap,
                onConfirmPrint = { copies, paperSize, duplicateDual ->
                    showPrintConfigDialog = false
                    onPrintPhoto(copies, paperSize, duplicateDual)
                },
                onDismiss = { showPrintConfigDialog = false }
            )
        }
    }
}

/**
 * Tab 0: Sunting Foto (Penyuntingan Gambar, Kecerahan, Rotasi, Penggantian Slot & Retake)
 */
@Composable
private fun PhotoEditingTab(
    photos: List<Bitmap>,
    onOpenEditorSlot: (Int) -> Unit,
    onRetakeSlot: (Int) -> Unit = {},
    onRetakeAll: () -> Unit = {},
    onReplaceFromGallery: (Int) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top banner with quick action
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
            border = BorderStroke(1.dp, Color(0x338B5CF6)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Opsi Retake Foto Layout",
                        style = MaterialTheme.typography.titleSmall,
                        color = StudioTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ambil ulang pose per slot kamera, ganti dari galeri, atau ulangi seluruh sesi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = StudioTextSecondary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onRetakeAll,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, StudioNeonPink),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioNeonPink),
                    modifier = Modifier.testTag("btn_retake_all_slots")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ulang Semua", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Slot cards
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(photos) { index, photo ->
                Card(
                    modifier = Modifier
                        .width(118.dp)
                        .testTag("photo_edit_slot_$index"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioSurface),
                    border = BorderStroke(1.5.dp, StudioVioletLight.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(108.dp)
                                .clickable { onOpenEditorSlot(index) }
                        ) {
                            Image(
                                bitmap = photo.asImageBitmap(),
                                contentDescription = "Foto ${index + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.75f))
                                    .padding(vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Slot #${index + 1}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Slot Actions Row (Retake Camera, Gallery, Edit)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x330F172A))
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Retake with Camera
                            IconButton(
                                onClick = { onRetakeSlot(index) },
                                modifier = Modifier
                                    .size(30.dp)
                                    .testTag("slot_retake_camera_$index")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Retake Slot #${index + 1}",
                                    tint = StudioNeonPink,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Replace from Gallery
                            IconButton(
                                onClick = { onReplaceFromGallery(index) },
                                modifier = Modifier
                                    .size(30.dp)
                                    .testTag("slot_replace_gallery_$index")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Ganti dari Galeri #${index + 1}",
                                    tint = StudioNeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 1: Pilihan Filter Warna (Realtime Canvas Filtering)
 */
@Composable
private fun FilterSelectionTab(
    selectedFilter: ColorFilterType,
    onSelectFilter: (ColorFilterType) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Pilih Filter Warna Studio",
            style = MaterialTheme.typography.titleSmall,
            color = StudioTextSecondary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(ColorFilterType.entries.toTypedArray()) { filter ->
                val isSelected = filter == selectedFilter
                Card(
                    onClick = { onSelectFilter(filter) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) StudioSurfaceVariant else StudioSurface
                    ),
                    border = if (isSelected) {
                        CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(StudioViolet, StudioNeonPink)),
                            width = 2.dp
                        )
                    } else {
                        CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0x228B5CF6), Color(0x11EC4899))))
                    },
                    modifier = Modifier.testTag("filter_${filter.name}")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = filter.displayName,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) StudioVioletLight else StudioTextPrimary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = filter.subtitle,
                            color = StudioTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tab 2: Pilihan Warna Bingkai
 */
@Composable
private fun FrameColorSelectionTab(
    selectedColor: FrameColorOption,
    onSelectColor: (FrameColorOption) -> Unit,
    useCustomCanvasImage: Boolean,
    customCanvasImagePath: String,
    onPickCustomCanvasImage: () -> Unit,
    onClearCustomCanvasImage: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Pilih Palet Warna Bingkai",
            style = MaterialTheme.typography.titleSmall,
            color = StudioTextSecondary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))

        val chunkedColors = PRESET_FRAME_COLORS.chunked(4)
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            chunkedColors.forEach { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowItems.forEach { option ->
                        val isSelected = option.name == selectedColor.name && !useCustomCanvasImage
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { 
                                    onClearCustomCanvasImage()
                                    onSelectColor(option) 
                                }
                                .padding(4.dp)
                                .testTag("frame_color_${option.name}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color(option.colorHex))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) StudioViolet else Color(0x44FFFFFF),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Terpilih",
                                        tint = Color(option.textColorHex),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = option.name,
                                fontSize = 11.sp,
                                color = if (isSelected) StudioTextPrimary else StudioTextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (rowItems.size < 4) {
                        val emptyCount = 4 - rowItems.size
                        repeat(emptyCount) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0x22FFFFFF))
        )
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Gambar Latar Canvas Eksternal",
            style = MaterialTheme.typography.titleSmall,
            color = StudioTextSecondary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (useCustomCanvasImage && customCanvasImagePath.isNotBlank()) {
            val file = java.io.File(customCanvasImagePath)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x11FFFFFF))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x22FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (file.exists()) {
                            AsyncImage(
                                model = file,
                                contentDescription = "Gambar latar kustom",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Gambar latar kustom",
                                tint = StudioVioletLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Background Aktif",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioTextPrimary
                        )
                        Text(
                            text = file.name,
                            fontSize = 11.sp,
                            color = StudioTextSecondary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 150.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onPickCustomCanvasImage,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioViolet,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Ganti", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = onClearCustomCanvasImage,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Gambar",
                            tint = Color.Red.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x05FFFFFF))
                    .border(
                        width = 1.dp,
                        color = StudioTextSecondary.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onPickCustomCanvasImage() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Tambah Gambar Latar",
                        tint = StudioVioletLight,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "Tambah Gambar Eksternal",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StudioTextPrimary
                        )
                        Text(
                            text = "Gunakan gambar/wallpaper kustom sebagai latar canvas",
                            fontSize = 10.sp,
                            color = StudioTextSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tab 3: Teks Caption Footer, Stiker, dan Tanggal
 */
@Composable
private fun TextAndEventCustomizationTab(
    caption: String,
    onCaptionChange: (String) -> Unit,
    showDate: Boolean,
    onToggleDate: () -> Unit,
    selectedSticker: String,
    onSelectSticker: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Teks Caption Input
        Column {
            Text(
                text = "Teks Caption Footer",
                style = MaterialTheme.typography.titleSmall,
                color = StudioTextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = caption,
                onValueChange = onCaptionChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("preview_caption_input"),
                placeholder = { Text("Contoh: SWEET MEMORIES ✨", color = StudioTextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StudioViolet,
                    unfocusedBorderColor = Color(0x338B5CF6),
                    focusedTextColor = StudioTextPrimary,
                    unfocusedTextColor = StudioTextPrimary,
                    focusedContainerColor = StudioSurfaceVariant,
                    unfocusedContainerColor = StudioSurfaceVariant
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )
        }

        // Dekorasi Stiker Emoji
        Column {
            Text(
                text = "Pilih Stiker & Ikon Dekoratif",
                style = MaterialTheme.typography.titleSmall,
                color = StudioTextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(PRESET_STICKERS) { sticker ->
                    val isSelected = sticker == selectedSticker
                    Surface(
                        onClick = { onSelectSticker(sticker) },
                        shape = CircleShape,
                        color = if (isSelected) StudioViolet else StudioSurfaceVariant,
                        border = if (isSelected) CardDefaults.outlinedCardBorder() else null,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("preview_sticker_$sticker")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = sticker, fontSize = 20.sp)
                        }
                    }
                }
            }
        }

        // Toggle Tanggal
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = StudioVioletLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Cap Tanggal & Waktu",
                            fontWeight = FontWeight.SemiBold,
                            color = StudioTextPrimary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Cetak tanggal acara di strip foto",
                            color = StudioTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Switch(
                    checked = showDate,
                    onCheckedChange = { onToggleDate() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = StudioViolet,
                        uncheckedThumbColor = StudioTextSecondary,
                        uncheckedTrackColor = StudioSurface
                    ),
                    modifier = Modifier.testTag("preview_date_switch")
                )
            }
        }
    }
}

/**
 * Bottom Action Bar: Simpan, Cetak, Bagikan, QR Code, Sesi Baru
 */
@Composable
private fun ResultBottomActionBar(
    isSaving: Boolean,
    isLoopViewActive: Boolean = false,
    isGeneratingGif: Boolean = false,
    onSaveToGallery: () -> Unit,
    onSaveGif: () -> Unit = {},
    onPrint: () -> Unit,
    onShare: () -> Unit,
    onOpenQr: () -> Unit,
    onRetake: () -> Unit
) {
    Surface(
        color = StudioSurfaceGlass,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x338B5CF6), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tombol Simpan ke Galeri / Simpan GIF
            if (isLoopViewActive) {
                Button(
                    onClick = onSaveGif,
                    enabled = !isGeneratingGif,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("save_loop_gif_bottom_btn")
                ) {
                    if (isGeneratingGif) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Membuat GIF...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Simpan GIF",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Simpan GIF",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            } else {
                Button(
                    onClick = onSaveToGallery,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .background(Brush.horizontalGradient(listOf(StudioViolet, StudioNeonPink)), RoundedCornerShape(14.dp))
                        .testTag("save_strip_button")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Menyimpan...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Simpan",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Simpan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Tombol Cetak Photo Strip
            OutlinedButton(
                onClick = onPrint,
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(StudioEmerald, StudioVioletLight))),
                modifier = Modifier.height(52.dp).testTag("print_strip_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = "Cetak",
                    tint = StudioEmerald
                )
            }

            // Tombol QR Code Kiosk Download
            OutlinedButton(
                onClick = onOpenQr,
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(StudioViolet, StudioNeonPink))),
                modifier = Modifier.height(52.dp).testTag("qr_kiosk_button")
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = "QR Code",
                    tint = StudioVioletLight
                )
            }

            // Tombol Bagikan
            OutlinedButton(
                onClick = onShare,
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(StudioViolet, StudioNeonPink))),
                modifier = Modifier.height(52.dp).testTag("share_strip_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Bagikan",
                    tint = StudioNeonPink
                )
            }

            // Tombol Ambil Ulang / Sesi Baru
            OutlinedButton(
                onClick = onRetake,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioTextPrimary),
                modifier = Modifier.height(52.dp).testTag("retake_strip_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Sesi Baru",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Dialog Notifikasi Sukses Simpan
 */
@Composable
private fun SaveSuccessResultDialog(
    fileName: String,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onNewSession: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioSurface,
        titleContentColor = StudioTextPrimary,
        textContentColor = StudioTextSecondary,
        icon = {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(StudioEmerald.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Sukses",
                    tint = StudioEmerald,
                    modifier = Modifier.size(36.dp)
                )
            }
        },
        title = {
            Text(
                text = "Tersimpan di Galeri!",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 20.sp
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Photo strip telah berhasil diekspor ke album Galeri (Pictures/PhotoBooth).",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = StudioViolet),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("dialog_share_btn")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Bagikan Sekarang", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onNewSession,
                modifier = Modifier.testTag("dialog_new_session_btn")
            ) {
                Text("Ambil Sesi Baru", color = StudioVioletLight)
            }
        }
    )
}

/**
 * Dialog QR Code Interaktif & Download untuk Tamu Kiosk dengan Cloudinary & QRIS
 */
@Composable
private fun KioskQrDownloadDialog(
    eventTitle: String,
    sessionId: String,
    cloudinaryUrl: String,
    qrisPaymentUrl: String,
    useCustomQrisImage: Boolean,
    customQrisImagePath: String,
    uploadState: CloudinaryUploadState,
    onUploadCloudinary: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onOpenBrowser: (String) -> Unit,
    onCopyLink: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedQrTab by remember { mutableIntStateOf(0) }
    val activeUrl = if (selectedQrTab == 0) cloudinaryUrl else qrisPaymentUrl

    val qrBitmap = remember(activeUrl) {
        QrCodeUtils.generateQrBitmap(content = activeUrl, sizePx = 450)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioSurface,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Cloud Download & QRIS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = StudioTextPrimary
                )
                Text(
                    text = eventTitle.ifBlank { "Photo Booth Cloud" },
                    fontSize = 12.sp,
                    color = StudioVioletLight
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Selector Tabs: Photo Strip Cloud vs QRIS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioSurfaceVariant, RoundedCornerShape(10.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedQrTab == 0) StudioViolet else Color.Transparent)
                            .clickable { selectedQrTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📸 Photo Cloud",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedQrTab == 0) Color.White else StudioTextSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedQrTab == 1) StudioViolet else Color.Transparent)
                            .clickable { selectedQrTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💳 Kode QRIS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedQrTab == 1) Color.White else StudioTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cloudinary upload status indicator (for Tab 0)
                if (selectedQrTab == 0) {
                    when (uploadState) {
                        is CloudinaryUploadState.Uploading -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(StudioViolet.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = StudioVioletLight
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Mengunggah ke Cloudinary...",
                                    fontSize = 11.sp,
                                    color = StudioVioletLight
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        is CloudinaryUploadState.Success -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(StudioEmerald.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CloudDone, contentDescription = null, tint = StudioEmerald, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Foto tersimpan di Cloudinary!",
                                    fontSize = 11.sp,
                                    color = StudioEmerald,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        is CloudinaryUploadState.Error -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(StudioNeonPink.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Upload: ${uploadState.message}",
                                    fontSize = 10.sp,
                                    color = StudioNeonPink,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = onUploadCloudinary) {
                                    Text("Coba Lagi", fontSize = 10.sp, color = StudioVioletLight)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        is CloudinaryUploadState.Idle -> {
                            Button(
                                onClick = onUploadCloudinary,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x338B5CF6)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(34.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = StudioVioletLight, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload ke Cloudinary Cloud", fontSize = 11.sp, color = StudioTextPrimary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                Text(
                    text = if (selectedQrTab == 0)
                        "Scan QR code dengan kamera HP Anda untuk membuka dan mendownload foto strip dari Cloudinary."
                    else
                        "Scan QRIS untuk pembayaran photo booth atau donasi studio acara.",
                    textAlign = TextAlign.Center,
                    color = StudioTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Real Scannable QR Code Card
                val showCustomQrisImage = (selectedQrTab == 1) && useCustomQrisImage && customQrisImagePath.isNotBlank() && java.io.File(customQrisImagePath).exists()

                Card(
                    onClick = { if (!showCustomQrisImage) onOpenBrowser(activeUrl) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.padding(2.dp)
                ) {
                    if (showCustomQrisImage) {
                        AsyncImage(
                            model = java.io.File(customQrisImagePath),
                            contentDescription = "Custom QRIS Image",
                            modifier = Modifier
                                .size(240.dp)
                                .padding(8.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    } else {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code Cloud / QRIS",
                            modifier = Modifier
                                .size(180.dp)
                                .padding(8.dp)
                        )
                    }
                }

                if (!showCustomQrisImage) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Direct URL Row with Copy and Browser Open
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StudioSurfaceVariant, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedQrTab == 0) "Cloudinary URL:" else "QRIS Link / Data:",
                                fontSize = 10.sp,
                                color = StudioTextSecondary
                            )
                            Text(
                                text = activeUrl,
                                fontSize = 11.sp,
                                color = StudioVioletLight,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                        IconButton(
                            onClick = { onCopyLink(activeUrl) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Salin", tint = StudioVioletLight, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = { onOpenBrowser(activeUrl) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = "Buka di Browser", tint = StudioEmerald, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ID Sesi: #$sessionId",
                    color = StudioTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSave,
                    colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download", fontSize = 12.sp)
                }
                Button(
                    onClick = onShare,
                    colors = ButtonDefaults.buttonColors(containerColor = StudioViolet),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bagikan", fontSize = 12.sp)
                }
            }
        }
    )
}

/**
 * Dialog Pilihan Berbagi (WhatsApp, Telegram, Print, General)
 */
@Composable
private fun ShareOptionsDialog(
    onWhatsApp: () -> Unit,
    onTelegram: () -> Unit,
    onPrint: () -> Unit,
    onGeneralShare: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioSurface,
        title = {
            Text(
                text = "Bagikan & Cetak Photo Strip",
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
                    text = "Pilih platform untuk membagikan atau mencetak karya Anda:",
                    fontSize = 13.sp,
                    color = StudioTextSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // WhatsApp Button
                Button(
                    onClick = onWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bagikan ke WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Telegram Button
                Button(
                    onClick = onTelegram,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088cc)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bagikan ke Telegram", color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Print Button
                Button(
                    onClick = onPrint,
                    colors = ButtonDefaults.buttonColors(containerColor = StudioViolet),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cetak (Printer / PDF)", color = Color.White, fontWeight = FontWeight.Bold)
                }

                // General Share Button
                OutlinedButton(
                    onClick = onGeneralShare,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = StudioNeonPink)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aplikasi Lainnya...", color = StudioTextPrimary)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = StudioTextSecondary)
            }
        }
    )
}

/**
 * Tab 3: Pilihan Bingkai Gambar & Custom Designer
 */
@Composable
private fun ImageFrameSelectionTab(
    selectedFrame: ImageFrameStyle,
    savedCustomFrames: List<ImageFrameStyle>,
    onSelectFrame: (ImageFrameStyle) -> Unit,
    onOpenCustomDesigner: () -> Unit,
    onEditCustomFrame: (ImageFrameStyle) -> Unit,
    onDeleteCustomFrame: (String) -> Unit
) {
    val allFrames = remember(savedCustomFrames) {
        PRESET_IMAGE_FRAMES + savedCustomFrames
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Pilih Bingkai Gambar Foto",
                    style = MaterialTheme.typography.titleSmall,
                    color = StudioTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Bingkai unik untuk setiap foto di strip",
                    style = MaterialTheme.typography.bodySmall,
                    color = StudioTextSecondary.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onOpenCustomDesigner,
                colors = ButtonDefaults.buttonColors(containerColor = StudioViolet),
                shape = RoundedCornerShape(20.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Custom", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preset & Custom Frame Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(allFrames) { frame ->
                val isSelected = frame.id == selectedFrame.id
                Card(
                    onClick = { onSelectFrame(frame) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) StudioSurfaceVariant else StudioSurface
                    ),
                    border = if (isSelected) {
                        CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(StudioViolet, StudioNeonPink)),
                            width = 2.dp
                        )
                    } else {
                        CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0x228B5CF6), Color(0x11EC4899))))
                    },
                    modifier = Modifier.testTag("image_frame_${frame.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Miniature Frame Visual Box
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            val borderColor = Color(frame.borderColorHex)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(0.78f)
                                    .border(
                                        width = frame.borderWidthDp.dp.coerceIn(1.dp, 4.dp),
                                        color = borderColor,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .background(Color(0xFF334155)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (frame.cornerOrnament.isNotBlank()) {
                                    Text(text = frame.cornerOrnament, fontSize = 12.sp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CropSquare,
                                        contentDescription = null,
                                        tint = borderColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = frame.name,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) StudioVioletLight else StudioTextPrimary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Text(
                            text = frame.borderStyle.displayName,
                            color = StudioTextSecondary,
                            fontSize = 10.sp
                        )

                        // If custom frame, show edit and delete actions
                        if (frame.isCustom) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { onEditCustomFrame(frame) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Tune, contentDescription = "Sesuaikan", tint = StudioVioletLight, modifier = Modifier.size(14.dp))
                                }
                                IconButton(
                                    onClick = { onDeleteCustomFrame(frame.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog Interactive Custom Frame Designer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomFrameDesignerDialog(
    currentConfig: ImageFrameStyle,
    onUpdateConfig: (ImageFrameStyle) -> Unit,
    onSave: (ImageFrameStyle) -> Unit,
    onDismiss: () -> Unit
) {
    var frameName by remember(currentConfig.id) { mutableStateOf(currentConfig.name) }
    var selectedStyle by remember(currentConfig.id) { mutableStateOf(currentConfig.borderStyle) }
    var borderWidthDp by remember(currentConfig.id) { mutableStateOf(currentConfig.borderWidthDp) }
    var borderColorHex by remember(currentConfig.id) { mutableStateOf(currentConfig.borderColorHex) }
    var innerBorderColorHex by remember(currentConfig.id) { mutableStateOf(currentConfig.innerBorderColorHex) }
    var cornerOrnament by remember(currentConfig.id) { mutableStateOf(currentConfig.cornerOrnament) }
    var selectedPattern by remember(currentConfig.id) { mutableStateOf(currentConfig.pattern) }

    val colorPresets = listOf(
        Pair("Emas Gold", 0xFFFFD700L),
        Pair("Putih Bersih", 0xFFFFFFFFL),
        Pair("Neon Pink", 0xFFEC4899L),
        Pair("Cyber Cyan", 0xFF06B6D4L),
        Pair("Emerald", 0xFF10B981L),
        Pair("Silver Gray", 0xFFCBD5E1L),
        Pair("Violet Studio", 0xFF8B5CF6L),
        Pair("Ruby Red", 0xFFF43F5EL),
        Pair("Charcoal", 0xFF1E293BL),
        Pair("Amber Sunset", 0xFFF59E0BL)
    )

    val ornamentPresets = listOf(
        "Tanpa Ornamen" to "",
        "Royal ⚜️" to "⚜️",
        "Heart 💖" to "💖",
        "Sparkle ✨" to "✨",
        "Floral 💐" to "💐",
        "Star ⭐" to "⭐",
        "Ribbon 🎀" to "🎀",
        "Film 🎞️" to "🎞️",
        "Cherry 🌸" to "🌸",
        "Crown 👑" to "👑"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Palette, contentDescription = null, tint = StudioVioletLight)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Desain Bingkai Custom",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = StudioTextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Live Interactive Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    val mainBorderColor = Color(borderColorHex)
                    val innerColor = Color(innerBorderColorHex)

                    Box(
                        modifier = Modifier
                            .size(140.dp, 90.dp)
                            .border(
                                width = borderWidthDp.dp.coerceIn(1.dp, 8.dp),
                                color = mainBorderColor,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (cornerOrnament.isNotBlank()) {
                                Text(text = cornerOrnament, fontSize = 20.sp)
                            }
                            Text(
                                text = selectedStyle.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioTextPrimary
                            )
                        }
                    }
                }

                // Input Nama Bingkai
                OutlinedTextField(
                    value = frameName,
                    onValueChange = { frameName = it },
                    label = { Text("Nama Bingkai") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioViolet,
                        unfocusedBorderColor = Color(0x338B5CF6),
                        focusedLabelColor = StudioVioletLight,
                        unfocusedLabelColor = StudioTextSecondary,
                        focusedTextColor = StudioTextPrimary,
                        unfocusedTextColor = StudioTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("custom_frame_name_input")
                )

                // Pilih Gaya Garis / Border Style
                Text("Gaya Garis Bingkai:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = StudioTextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(FrameBorderStyle.entries.toTypedArray()) { style ->
                        val isSelected = style == selectedStyle
                        Card(
                            onClick = { selectedStyle = style },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) StudioViolet else StudioSurfaceVariant
                            )
                        ) {
                            Text(
                                text = style.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else StudioTextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Thickness Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ketebalan Garis:", fontSize = 13.sp, color = StudioTextSecondary)
                    Text("${borderWidthDp.toInt()} dp", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StudioVioletLight)
                }
                Slider(
                    value = borderWidthDp,
                    onValueChange = { borderWidthDp = it },
                    valueRange = 1f..16f,
                    colors = SliderDefaults.colors(
                        thumbColor = StudioViolet,
                        activeTrackColor = StudioVioletLight
                    )
                )

                // Border Color Presets
                Text("Warna Utama Bingkai:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = StudioTextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(colorPresets) { (name, colorLong) ->
                        val isSelected = borderColorHex == colorLong
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(colorLong))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) StudioVioletLight else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable { borderColorHex = colorLong },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (colorLong == 0xFFFFFFFFL) Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Corner Ornament
                Text("Hiasan Ornamen Sudut:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = StudioTextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ornamentPresets) { (label, emoji) ->
                        val isSelected = cornerOrnament == emoji
                        Card(
                            onClick = { cornerOrnament = emoji },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) StudioNeonPink else StudioSurfaceVariant
                            )
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else StudioTextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Background Texture Pattern
                Text("Tekstur Pola Background Canvas:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = StudioTextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(FrameBackgroundPattern.entries.toTypedArray()) { pattern ->
                        val isSelected = pattern == selectedPattern
                        Card(
                            onClick = { selectedPattern = pattern },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) StudioEmerald else StudioSurfaceVariant
                            )
                        ) {
                            Text(
                                text = pattern.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else StudioTextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalConfig = currentConfig.copy(
                        name = if (frameName.isNotBlank()) frameName else "Bingkai Custom",
                        borderStyle = selectedStyle,
                        borderWidthDp = borderWidthDp,
                        borderColorHex = borderColorHex,
                        innerBorderColorHex = innerBorderColorHex,
                        cornerOrnament = cornerOrnament,
                        pattern = selectedPattern,
                        isCustom = true
                    )
                    onSave(finalConfig)
                },
                colors = ButtonDefaults.buttonColors(containerColor = StudioViolet),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_custom_frame_button")
            ) {
                Text("Simpan & Terapkan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = StudioTextSecondary)
            }
        }
    )
}

/**
 * Dialog Konfigurasi Cetak Photo Strip
 */
@Composable
private fun PrintConfigDialog(
    defaultCopies: Int,
    defaultPaperSize: PrintPaperSize,
    defaultDuplicateDual: Boolean,
    finalBitmap: Bitmap?,
    onConfirmPrint: (copies: Int, paperSize: PrintPaperSize, duplicateDual: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var copies by remember { mutableIntStateOf(defaultCopies.coerceAtLeast(1)) }
    var selectedPaperSize by remember { mutableStateOf(defaultPaperSize) }
    var duplicateDual by remember { mutableStateOf(defaultDuplicateDual) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = null,
                    tint = StudioEmerald
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Cetak Photo Strip",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = StudioTextPrimary
                    )
                    Text(
                        text = "Kirim langsung ke printer",
                        fontSize = 12.sp,
                        color = StudioTextSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Layout Visual Mockup / Print Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0x3310B981), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (finalBitmap != null) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            // Strip 1
                            Image(
                                bitmap = finalBitmap.asImageBitmap(),
                                contentDescription = "Strip 1",
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            )

                            if (duplicateDual || selectedPaperSize == PrintPaperSize.PHOTO_2x6_DUAL) {
                                Spacer(modifier = Modifier.width(8.dp))
                                // Cut Line indicator
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Text("✂️", fontSize = 12.sp)
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .weight(1f)
                                            .background(Color.Gray.copy(alpha = 0.5f))
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                // Strip 2
                                Image(
                                    bitmap = finalBitmap.asImageBitmap(),
                                    contentDescription = "Strip 2",
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                )
                            }
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            tint = StudioEmerald,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Pengaturan Jumlah Cetak (Copies)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Jumlah Lembar Cetak:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = StudioTextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledIconButton(
                                onClick = { if (copies > 1) copies-- },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = StudioSurfaceVariant),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Kurang", tint = Color.White)
                            }

                            Text(
                                text = "$copies",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = StudioEmerald,
                                modifier = Modifier.widthIn(min = 32.dp),
                                textAlign = TextAlign.Center
                            )

                            FilledIconButton(
                                onClick = { if (copies < 20) copies++ },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = StudioSurfaceVariant),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Tambah", tint = Color.White)
                            }
                        }

                        // Presets
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(1, 2, 4).forEach { num ->
                                val isSelected = copies == num
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { copies = num },
                                    label = { Text("$num", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StudioEmerald,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Mode Dual Strip Switch
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cetak 2 Strip Berdampingan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = StudioTextPrimary
                            )
                            Text(
                                text = "2 salinan strip dalam 1 lembar foto 4x6",
                                fontSize = 11.sp,
                                color = StudioTextSecondary
                            )
                        }
                        Switch(
                            checked = duplicateDual,
                            onCheckedChange = { duplicateDual = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = StudioEmerald
                            )
                        )
                    }
                }

                // Pilihan Ukuran Kertas Printer
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Ukuran Kertas Printer:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = StudioTextSecondary
                    )

                    PrintPaperSize.entries.forEach { paperSize ->
                        val isSelected = selectedPaperSize == paperSize
                        Card(
                            onClick = { selectedPaperSize = paperSize },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) StudioEmerald.copy(alpha = 0.2f) else StudioSurfaceVariant
                            ),
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(StudioEmerald, StudioVioletLight)), width = 1.5.dp) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = paperSize.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) StudioEmerald else StudioTextPrimary
                                    )
                                    Text(
                                        text = paperSize.description,
                                        fontSize = 10.sp,
                                        color = StudioTextSecondary
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Pilih",
                                        tint = StudioEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmPrint(copies, selectedPaperSize, duplicateDual)
                },
                colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_print_button")
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cetak Sekarang ($copies)", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = StudioTextSecondary)
            }
        }
    )
}

/**
 * Dialog to choose retake options: Single slot retake via camera, gallery replace, or retake all
 */
@Composable
fun RetakeChooserDialog(
    photos: List<Bitmap>,
    layoutName: String,
    onDismiss: () -> Unit,
    onRetakeSinglePhoto: (Int) -> Unit,
    onRetakeAll: () -> Unit,
    onPickGalleryForSlot: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(StudioNeonPink, StudioAmber))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Opsi Retake Foto",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary
                    )
                    Text(
                        text = layoutName,
                        fontSize = 11.sp,
                        color = StudioNeonPink,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "PILIH SLOT FOTO UNTUK DIAMBIL ULANG:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioVioletLight,
                    letterSpacing = 0.5.sp
                )

                // List of slots
                photos.forEachIndexed { index, photo ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("retake_dialog_slot_$index"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                        border = BorderStroke(1.dp, Color(0x338B5CF6))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, StudioVioletLight, RoundedCornerShape(8.dp))
                                ) {
                                    Image(
                                        bitmap = photo.asImageBitmap(),
                                        contentDescription = "Foto ${index + 1}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Foto Slot #${index + 1}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = StudioTextPrimary
                                    )
                                    Text(
                                        text = "Ambil ulang kamera / ganti",
                                        fontSize = 10.sp,
                                        color = StudioTextSecondary
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Retake button
                                Button(
                                    onClick = { onRetakeSinglePhoto(index) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = StudioNeonPink),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("btn_retake_single_cam_$index")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Retake", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                // Gallery button
                                IconButton(
                                    onClick = { onPickGalleryForSlot(index) },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x22FFFFFF))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Pilih Galeri",
                                        tint = StudioNeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Retake all photos card
                Card(
                    onClick = onRetakeAll,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_retake_all_in_dialog"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x22EC4899)),
                    border = BorderStroke(1.dp, StudioNeonPink.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = StudioNeonPink,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Ambil Ulang Semua Foto (Sesi Baru)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = StudioNeonPink
                            )
                            Text(
                                text = "Reset dan foto ulang semua ${photos.size} pose dari awal",
                                fontSize = 10.sp,
                                color = StudioTextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_retake_dialog_button")
            ) {
                Text("Tutup", color = StudioTextSecondary, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}











