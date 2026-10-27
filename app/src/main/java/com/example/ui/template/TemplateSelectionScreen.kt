package com.example.ui.template

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.ui.theme.StudioSurfaceGlass
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.util.CustomLayoutRepository
import kotlin.math.absoluteValue
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CustomLayoutConfig
import com.example.model.PRESET_FRAME_COLORS
import com.example.model.PRESET_STICKERS
import com.example.model.PhotoBoothLayout
import com.example.model.PhotoBoothUiState
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioNeonCyan
import com.example.ui.theme.StudioNeonPink
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioViolet
import com.example.ui.theme.StudioVioletLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateSelectionScreen(
    uiState: PhotoBoothUiState,
    onSelectLayout: (PhotoBoothLayout) -> Unit,
    onSelectCustomLayout: (CustomLayoutConfig) -> Unit,
    onDeleteCustomLayout: (String) -> Unit,
    onOpenCustomDesigner: (CustomLayoutConfig?) -> Unit,
    onSaveCustomLayout: (CustomLayoutConfig) -> Unit,
    onStartBooth: () -> Unit,
    onOpenSettings: () -> Unit,
    onBackToWelcome: (() -> Unit)? = null,
    onImportPngLayout: ((Uri, (CustomLayoutConfig, Bitmap) -> Unit, (String) -> Unit) -> Unit)? = null,
    onUploadBackgroundImage: ((Uri, (String) -> Unit, (String) -> Unit) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var layoutToDelete by remember { mutableStateOf<CustomLayoutConfig?>(null) }
    var pendingPngConfig by remember { mutableStateOf<CustomLayoutConfig?>(null) }
    var pendingPngBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isEditingExistingPng by remember { mutableStateOf(false) }
    var isLoadingPng by remember { mutableStateOf(false) }
    var showLainnya2x6 by remember { mutableStateOf(false) }
    var showLainnya4x6 by remember { mutableStateOf(false) }
    var showLainnyaA4 by remember { mutableStateOf(false) }

    val externalPngLayouts = remember(uiState.savedCustomLayouts) {
        uiState.savedCustomLayouts.filter { it.isPngOverlay && it.pngOverlayPath.isNotBlank() }
    }
    val externalPng2x6Layouts = remember(externalPngLayouts) {
        externalPngLayouts.filter { it.overlayAspectRatio >= 2.0f }
    }
    val externalPng4x6Layouts = remember(externalPngLayouts) {
        externalPngLayouts.filter { it.overlayAspectRatio >= 1.45f && it.overlayAspectRatio < 2.0f }
    }
    val externalPngA4Layouts = remember(externalPngLayouts) {
        externalPngLayouts.filter { it.overlayAspectRatio < 1.45f }
    }
    val manualCustomLayouts = remember(uiState.savedCustomLayouts) {
        uiState.savedCustomLayouts.filter { !it.isPngOverlay }
    }

    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var isAllPngGridView by remember { mutableStateOf(false) }

    val coverFlowItems = remember(
        selectedCategoryIndex,
        externalPng2x6Layouts,
        externalPng4x6Layouts,
        externalPngA4Layouts,
        manualCustomLayouts
    ) {
        val items = mutableListOf<CoverFlowLayoutItem>()
        when (selectedCategoryIndex) {
            1 -> { // Semua PNG
                externalPngLayouts.forEach {
                    val type = when {
                        it.overlayAspectRatio >= 2.0f -> ShowcaseLayoutType.TWO_BY_SIX
                        it.overlayAspectRatio >= 1.45f -> ShowcaseLayoutType.FOUR_BY_SIX
                        else -> ShowcaseLayoutType.PAPER_A4
                    }
                    items.add(CoverFlowLayoutItem.ExternalPng(it, type))
                }
            }
            2 -> { // 2x6
                externalPng2x6Layouts.forEach { items.add(CoverFlowLayoutItem.ExternalPng(it, ShowcaseLayoutType.TWO_BY_SIX)) }
            }
            3 -> { // 4x6
                externalPng4x6Layouts.forEach { items.add(CoverFlowLayoutItem.ExternalPng(it, ShowcaseLayoutType.FOUR_BY_SIX)) }
            }
            4 -> { // A4
                externalPngA4Layouts.forEach { items.add(CoverFlowLayoutItem.ExternalPng(it, ShowcaseLayoutType.PAPER_A4)) }
            }
            5 -> { // Custom
                manualCustomLayouts.forEach { items.add(CoverFlowLayoutItem.ManualCustom(it)) }
            }
            else -> { // Semua Layout
                externalPng2x6Layouts.forEach { items.add(CoverFlowLayoutItem.ExternalPng(it, ShowcaseLayoutType.TWO_BY_SIX)) }
                externalPng4x6Layouts.forEach { items.add(CoverFlowLayoutItem.ExternalPng(it, ShowcaseLayoutType.FOUR_BY_SIX)) }
                externalPngA4Layouts.forEach { items.add(CoverFlowLayoutItem.ExternalPng(it, ShowcaseLayoutType.PAPER_A4)) }
                manualCustomLayouts.forEach { items.add(CoverFlowLayoutItem.ManualCustom(it)) }
            }
        }
        items
    }

    val loopCount = 10000
    val initialPage = loopCount / 2
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { loopCount })

    LaunchedEffect(uiState.customLayoutConfig.id, selectedCategoryIndex, coverFlowItems) {
        if (uiState.isCustomLayoutActive && coverFlowItems.isNotEmpty()) {
            val activeId = uiState.customLayoutConfig.id
            val targetIdxInItems = coverFlowItems.indexOfFirst {
                when (it) {
                    is CoverFlowLayoutItem.ExternalPng -> it.config.id == activeId
                    is CoverFlowLayoutItem.ManualCustom -> it.config.id == activeId
                    else -> false
                }
            }
            if (targetIdxInItems >= 0) {
                val currentLoop = pagerState.currentPage / coverFlowItems.size
                val targetPage = (currentLoop * coverFlowItems.size) + targetIdxInItems
                if (targetPage != pagerState.currentPage) {
                    pagerState.scrollToPage(targetPage)
                }
            }
        }
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val pngPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isLoadingPng = true
            isEditingExistingPng = false
            if (onImportPngLayout != null) {
                onImportPngLayout(
                    uri,
                    { config, bitmap ->
                        isLoadingPng = false
                        pendingPngConfig = config
                        pendingPngBitmap = bitmap
                        isEditingExistingPng = false
                    },
                    { errorMsg ->
                        isLoadingPng = false
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xCC0F172A))
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Arvin Booth™",
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                letterSpacing = 0.5.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Pilih Layout Frame & Template",
                                fontSize = 11.sp,
                                color = StudioTextSecondary
                            )
                        }
                    },
                    navigationIcon = {
                        if (onBackToWelcome != null) {
                            IconButton(
                                onClick = onBackToWelcome,
                                modifier = Modifier.testTag("back_to_welcome_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Kembali ke Beranda",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { pngPickerLauncher.launch("image/*") },
                            modifier = Modifier.testTag("import_png_layout_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(StudioViolet.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Tambah Layout Frame PNG",
                                    tint = StudioNeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.testTag("open_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Pengaturan",
                                tint = StudioTextSecondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.horizontalGradient(listOf(StudioViolet, StudioNeonPink)))
                        .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .clickable { onStartBooth() }
                        .testTag("start_photobooth_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "MULAI FOTO BOOTH (${uiState.activeLayoutDisplayName})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tampilan Semua & Grid View Toggle Header above Cover Flow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedCategoryIndex) {
                        1 -> "Semua Frame PNG"
                        2 -> "Frame 2x6"
                        3 -> "Frame 4x6"
                        4 -> "Frame A4"
                        5 -> "Custom Layout"
                        else -> "Semua Layout"
                    },
                    color = StudioTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(
                    onClick = {
                        selectedCategoryIndex = 1
                        isAllPngGridView = !isAllPngGridView
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(StudioSurfaceVariant)
                        .border(1.dp, StudioViolet.copy(alpha = 0.4f), CircleShape)
                        .testTag("toggle_grid_view_button")
                ) {
                    Icon(
                        imageVector = if (isAllPngGridView && selectedCategoryIndex == 1) Icons.Default.ViewCarousel else Icons.Default.GridView,
                        contentDescription = "Tampilkan Semua Grid",
                        tint = if (isAllPngGridView && selectedCategoryIndex == 1) StudioVioletLight else StudioTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 3. 3D Cover Flow Carousel or Grid
            if (coverFlowItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = StudioTextSecondary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Belum Ada Template Layout", color = StudioTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Import frame PNG dari galeri perangkat Anda", color = StudioTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { pngPickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioViolet)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Import Frame PNG")
                        }
                    }
                }
            } else if (isAllPngGridView && selectedCategoryIndex == 1) {
                // PNG Grid View
                LazyVerticalGrid(
                    columns = GridCells.Fixed(uiState.settings.gridColumnCount.coerceIn(2, 4)),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(coverFlowItems) { item ->
                        val isSelected = when (item) {
                            is CoverFlowLayoutItem.ExternalPng -> uiState.isCustomLayoutActive && uiState.customLayoutConfig.id == item.config.id
                            is CoverFlowLayoutItem.ManualCustom -> uiState.isCustomLayoutActive && uiState.customLayoutConfig.id == item.config.id
                            else -> false
                        }

                        GridCardItem(
                            item = item,
                            isSelected = isSelected,
                            onSelect = {
                                when (item) {
                                    is CoverFlowLayoutItem.ExternalPng -> onSelectCustomLayout(item.config)
                                    is CoverFlowLayoutItem.ManualCustom -> onSelectCustomLayout(item.config)
                                    is CoverFlowLayoutItem.AddNewAction -> {
                                        isEditingExistingPng = false
                                        pngPickerLauncher.launch("image/*")
                                    }
                                }
                            },
                            onEdit = {
                                when (item) {
                                    is CoverFlowLayoutItem.ExternalPng -> {
                                        val file = File(item.config.pngOverlayPath)
                                        if (file.exists()) {
                                            val bmp = BitmapFactory.decodeFile(file.absolutePath)
                                            if (bmp != null) {
                                                pendingPngConfig = item.config
                                                pendingPngBitmap = bmp
                                                isEditingExistingPng = true
                                            }
                                        }
                                    }
                                    is CoverFlowLayoutItem.ManualCustom -> onOpenCustomDesigner(item.config)
                                    else -> {}
                                }
                            },
                            onDelete = {
                                when (item) {
                                    is CoverFlowLayoutItem.ExternalPng -> layoutToDelete = item.config
                                    is CoverFlowLayoutItem.ManualCustom -> layoutToDelete = item.config
                                    else -> {}
                                }
                            },
                            onAddNew = {
                                isEditingExistingPng = false
                                pngPickerLauncher.launch("image/*")
                            },
                            modifier = Modifier.aspectRatio(0.75f)
                        )
                    }
                }
            } else {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    val isTablet = maxWidth >= 720.dp
                    val horizontalPadding = if (isTablet) 32.dp else 16.dp
                    val cardWidth = if (isTablet) 280.dp else 220.dp

                    HorizontalPager(
                        state = pagerState,
                        pageSize = androidx.compose.foundation.pager.PageSize.Fixed(cardWidth),
                        pageSpacing = if (isTablet) 16.dp else 8.dp,
                        contentPadding = PaddingValues(horizontal = (maxWidth - cardWidth) / 2, vertical = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .testTag("cover_flow_horizontal_pager")
                    ) { page ->
                        val item = coverFlowItems.getOrNull(page % coverFlowItems.size) ?: return@HorizontalPager

                        // Advanced 3D Carousel Slider Math (Cylindrical Arc Cylinder, RotationY, Scale, Alpha)
                        val exactOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                        val pageOffset = exactOffset.absoluteValue
                        val isCentered = (page == pagerState.currentPage)
                        val isSelected = when (item) {
                            is CoverFlowLayoutItem.ExternalPng -> uiState.isCustomLayoutActive && uiState.customLayoutConfig.id == item.config.id
                            is CoverFlowLayoutItem.ManualCustom -> uiState.isCustomLayoutActive && uiState.customLayoutConfig.id == item.config.id
                            else -> false
                        }

                        val scale = if (isTablet) {
                            0.75f + (0.25f * (1f - pageOffset.coerceIn(0f, 1f)))
                        } else {
                            0.80f + (0.20f * (1f - pageOffset.coerceIn(0f, 1f)))
                        }
                        val alpha = if (isTablet) {
                            0.4f + (0.6f * (1f - pageOffset.coerceIn(0f, 1f)))
                        } else {
                            0.5f + (0.5f * (1f - pageOffset.coerceIn(0f, 1f)))
                        }
                        val rotationY = exactOffset * (if (isTablet) -25f else -20f)

                        Box(
                            modifier = Modifier
                                .width(cardWidth)
                                .fillMaxHeight()
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alpha
                                    shadowElevation = (1f - pageOffset.coerceIn(0f, 1f)) * 24f * density
                                    cameraDistance = 16f * density
                                }
                                .clickable {
                                    if (!isCentered) {
                                        coroutineScope.launch { pagerState.animateScrollToPage(page) }
                                    } else {
                                        when (item) {
                                            is CoverFlowLayoutItem.ExternalPng -> onSelectCustomLayout(item.config)
                                            is CoverFlowLayoutItem.ManualCustom -> onSelectCustomLayout(item.config)
                                            is CoverFlowLayoutItem.AddNewAction -> {
                                                isEditingExistingPng = false
                                                pngPickerLauncher.launch("image/*")
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            CoverFlowCardItem(
                                item = item,
                                isSelected = isSelected,
                                isCentered = isCentered,
                                onSelect = {
                                    when (item) {
                                        is CoverFlowLayoutItem.ExternalPng -> onSelectCustomLayout(item.config)
                                        is CoverFlowLayoutItem.ManualCustom -> onSelectCustomLayout(item.config)
                                        else -> {}
                                    }
                                },
                                onEdit = {
                                    when (item) {
                                        is CoverFlowLayoutItem.ExternalPng -> {
                                            val file = File(item.config.pngOverlayPath)
                                            if (file.exists()) {
                                                val bmp = BitmapFactory.decodeFile(file.absolutePath)
                                                if (bmp != null) {
                                                    pendingPngConfig = item.config
                                                    pendingPngBitmap = bmp
                                                    isEditingExistingPng = true
                                                }
                                            }
                                        }
                                        is CoverFlowLayoutItem.ManualCustom -> onOpenCustomDesigner(item.config)
                                        else -> {}
                                    }
                                },
                                onDelete = {
                                    when (item) {
                                        is CoverFlowLayoutItem.ExternalPng -> layoutToDelete = item.config
                                        is CoverFlowLayoutItem.ManualCustom -> layoutToDelete = item.config
                                        else -> {}
                                    }
                                },
                                onAddNew = {
                                    isEditingExistingPng = false
                                    pngPickerLauncher.launch("image/*")
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 4. Pagination Dots & Bottom Floating Agent Pill Dock (Matching cover.png)
            if (coverFlowItems.isNotEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        coverFlowItems.indices.forEach { index ->
                            // Map all pages to the indices of coverFlowItems
                            val isSelected = pagerState.currentPage % coverFlowItems.size == index
                            Box(
                                modifier = Modifier
                                    .height(6.dp)
                                    .width(if (isSelected) 24.dp else 6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (isSelected) StudioNeonPink else Color.White.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(3.dp)
                                    )
                                    .clickable {
                                        // Calculate target page in current loop
                                        val currentLoop = pagerState.currentPage / coverFlowItems.size
                                        val targetPage = (currentLoop * coverFlowItems.size) + index
                                        coroutineScope.launch { pagerState.animateScrollToPage(targetPage) }
                                    }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Floating Agent Pill Dock (Matching cover.png bottom dock)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0xE60F172A))
                            .border(1.5.dp, Brush.horizontalGradient(listOf(StudioViolet, StudioNeonCyan, StudioNeonPink)), RoundedCornerShape(28.dp))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val categories = listOf("Semua", "PNG 2x6", "PNG 4x6", "PNG A4", "Custom")
                            categories.forEachIndexed { catIdx, catName ->
                                val isCatSelected = selectedCategoryIndex == catIdx
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isCatSelected) StudioViolet.copy(alpha = 0.6f) else Color.Transparent)
                                        .clickable {
                                            selectedCategoryIndex = catIdx
                                            if (catIdx != 1) isAllPngGridView = false
                                            coroutineScope.launch {
                                                if (coverFlowItems.isNotEmpty()) {
                                                    pagerState.scrollToPage(0)
                                                }
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = catName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isCatSelected) Color.White else StudioTextSecondary
                                    )
                                }
                            }

                            // Glowing Center AI Orb Icon
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Brush.horizontalGradient(listOf(StudioNeonPink, StudioViolet, StudioNeonCyan)))
                                    .border(1.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Agent",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (layoutToDelete != null) {
        val target = layoutToDelete!!
        AlertDialog(
            onDismissRequest = { layoutToDelete = null },
            containerColor = StudioSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = StudioNeonPink,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hapus Layout Custom?", fontWeight = FontWeight.Bold, color = StudioTextPrimary, fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus layout '${target.name}'? Action ini tidak dapat dibatalkan.",
                    color = StudioTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCustomLayout(target.id)
                        layoutToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioNeonPink),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { layoutToDelete = null }) {
                    Text("Batal", color = StudioTextSecondary)
                }
            }
        )
    }

    // Interactive Custom Layout Designer Dialog
    if (uiState.showCustomLayoutDesigner) {
        val initialToUse = uiState.editingCustomLayoutConfig ?: CustomLayoutConfig()
        CustomLayoutDesignerDialog(
            initialConfig = initialToUse,
            onApply = onSaveCustomLayout,
            onDismiss = { onOpenCustomDesigner(null) }
        )
    }

    // PNG Layout Import / Edit Dialog
    if (pendingPngConfig != null && pendingPngBitmap != null) {
        PngLayoutImportDialog(
            initialConfig = pendingPngConfig!!,
            pngBitmap = pendingPngBitmap!!,
            isEditMode = isEditingExistingPng,
            onDismiss = {
                pendingPngConfig = null
                pendingPngBitmap = null
                isEditingExistingPng = false
            },
            onSaveLayout = { finalConfig ->
                onSaveCustomLayout(finalConfig)
                val msg = if (isEditingExistingPng) "Layout Frame PNG berhasil diperbarui!" else "Layout PNG berhasil disimpan!"
                pendingPngConfig = null
                pendingPngBitmap = null
                isEditingExistingPng = false
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            onReplacePng = { uri, onDone ->
                if (onImportPngLayout != null) {
                    onImportPngLayout(
                        uri,
                        { newConfig, newBmp ->
                            onDone(newConfig, newBmp)
                        },
                        { errorMsg ->
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
        )
    }

    // Loading PNG Dialog
    if (isLoadingPng) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = StudioSurface,
            title = {
                Text(
                    "Memproses Gambar PNG...",
                    color = StudioTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    CircularProgressIndicator(
                        color = StudioNeonPink,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        "Mendeteksi kotak transparan untuk foto...",
                        color = StudioTextSecondary,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = { }
        )
    }

    // Modal Dialog: LAINNYA Layout 2x6
    if (showLainnya2x6) {
        LayoutLainnyaDialog(
            title = "Koleksi Layout 2x6",
            type = ShowcaseLayoutType.TWO_BY_SIX,
            externalLayouts = externalPng2x6Layouts,
            activeConfigId = if (uiState.isCustomLayoutActive) uiState.customLayoutConfig.id else "",
            onDismiss = { showLainnya2x6 = false },
            onImportNew = {
                isEditingExistingPng = false
                pngPickerLauncher.launch("image/*")
            },
            onSelectLayout = { config ->
                onSelectCustomLayout(config)
            },
            onEditLayout = { config ->
                val file = File(config.pngOverlayPath)
                if (file.exists()) {
                    val bmp = BitmapFactory.decodeFile(file.absolutePath)
                    if (bmp != null) {
                        pendingPngConfig = config
                        pendingPngBitmap = bmp
                        isEditingExistingPng = true
                    } else {
                        Toast.makeText(context, "Tidak dapat membaca file gambar PNG", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDeleteLayout = { config ->
                layoutToDelete = config
            }
        )
    }

    // Modal Dialog: LAINNYA Layout 4x6
    if (showLainnya4x6) {
        LayoutLainnyaDialog(
            title = "Koleksi Layout 4x6",
            type = ShowcaseLayoutType.FOUR_BY_SIX,
            externalLayouts = externalPng4x6Layouts,
            activeConfigId = if (uiState.isCustomLayoutActive) uiState.customLayoutConfig.id else "",
            onDismiss = { showLainnya4x6 = false },
            onImportNew = {
                isEditingExistingPng = false
                pngPickerLauncher.launch("image/*")
            },
            onSelectLayout = { config ->
                onSelectCustomLayout(config)
            },
            onEditLayout = { config ->
                val file = File(config.pngOverlayPath)
                if (file.exists()) {
                    val bmp = BitmapFactory.decodeFile(file.absolutePath)
                    if (bmp != null) {
                        pendingPngConfig = config
                        pendingPngBitmap = bmp
                        isEditingExistingPng = true
                    } else {
                        Toast.makeText(context, "Tidak dapat membaca file gambar PNG", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDeleteLayout = { config ->
                layoutToDelete = config
            }
        )
    }

    // Modal Dialog: LAINNYA Layout A4
    if (showLainnyaA4) {
        LayoutLainnyaDialog(
            title = "Koleksi Layout A4",
            type = ShowcaseLayoutType.PAPER_A4,
            externalLayouts = externalPngA4Layouts,
            activeConfigId = if (uiState.isCustomLayoutActive) uiState.customLayoutConfig.id else "",
            onDismiss = { showLainnyaA4 = false },
            onImportNew = {
                isEditingExistingPng = false
                pngPickerLauncher.launch("image/*")
            },
            onSelectLayout = { config ->
                onSelectCustomLayout(config)
            },
            onEditLayout = { config ->
                val file = File(config.pngOverlayPath)
                if (file.exists()) {
                    val bmp = BitmapFactory.decodeFile(file.absolutePath)
                    if (bmp != null) {
                        pendingPngConfig = config
                        pendingPngBitmap = bmp
                        isEditingExistingPng = true
                    } else {
                        Toast.makeText(context, "Tidak dapat membaca file gambar PNG", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDeleteLayout = { config ->
                layoutToDelete = config
            }
        )
    }
}

/**
 * Layout Visual Graphic Image Preview for each template
 */
@Composable
private fun TemplateLayoutGraphicPreview(
    layout: PhotoBoothLayout,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Color(0xFF1E1B4B) else Color(0xFF0F172A)
            )
            .border(
                1.dp,
                if (isSelected) Brush.horizontalGradient(listOf(StudioViolet, StudioNeonPink))
                else Brush.horizontalGradient(listOf(Color(0x338B5CF6), Color(0x11EC4899))),
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        when (layout) {
            PhotoBoothLayout.CLASSIC_3_STRIP -> Classic3StripGraphicPreview(isSelected)
            PhotoBoothLayout.CLASSIC_4_STRIP -> Classic4StripGraphicPreview(isSelected)
            PhotoBoothLayout.GRID_2X2_POLAROID -> Grid2x2PolaroidGraphicPreview(isSelected)
            PhotoBoothLayout.AESTHETIC_FILM_ROLL -> AestheticFilmRollGraphicPreview(isSelected)
            PhotoBoothLayout.DUO_POLAROID -> DuoPolaroidGraphicPreview(isSelected)
        }
    }
}

@Composable
private fun Classic3StripGraphicPreview(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .width(52.dp)
            .height(84.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFFFFDF5))
            .border(0.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "★ STUDIO 3 ★",
                fontSize = 4.5.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF334155),
                letterSpacing = 0.2.sp
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Photo 1
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFFF7E5F), Color(0xFFFEB47B))))
                        .border(0.5.dp, Color(0x33000000), RoundedCornerShape(2.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌸", fontSize = 6.sp)
                }
                // Photo 2
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))))
                        .border(0.5.dp, Color(0x33000000), RoundedCornerShape(2.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✨", fontSize = 6.sp)
                }
                // Photo 3
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF10B981))))
                        .border(0.5.dp, Color(0x33000000), RoundedCornerShape(2.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💖", fontSize = 6.sp)
                }
            }

            Text(
                text = "MEMORIES",
                fontSize = 4.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun Classic4StripGraphicPreview(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .width(48.dp)
            .height(86.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFF8FAFC))
            .border(0.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 2.5.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "PHOTO 4",
                fontSize = 4.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1E293B)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val gradients = listOf(
                    listOf(Color(0xFFF59E0B), Color(0xFFEF4444)),
                    listOf(Color(0xFFEC4899), Color(0xFF8B5CF6)),
                    listOf(Color(0xFF3B82F6), Color(0xFF06B6D4)),
                    listOf(Color(0xFF10B981), Color(0xFF84CC16))
                )
                val emojis = listOf("📸", "✌️", "🥰", "🎉")
                for (i in 0..3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Brush.linearGradient(gradients[i]))
                            .border(0.5.dp, Color(0x33000000), RoundedCornerShape(2.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emojis[i], fontSize = 5.sp)
                    }
                }
            }

            Text(
                text = "2026 // RETRO",
                fontSize = 3.5.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun Grid2x2PolaroidGraphicPreview(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .width(66.dp)
            .height(82.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White)
            .border(0.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
            .padding(top = 4.dp, start = 4.dp, end = 4.dp, bottom = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(25.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFFFB7185), Color(0xFFF43F5E)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌷", fontSize = 7.sp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(25.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF60A5FA), Color(0xFF3B82F6)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("☀️", fontSize = 7.sp)
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(25.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFFA78BFA), Color(0xFF7C3AED)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✨", fontSize = 7.sp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(25.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF34D399), Color(0xFF059669)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍃", fontSize = 7.sp)
                    }
                }
            }

            Text(
                text = "POLAROID MEMORIES",
                fontSize = 4.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155),
                letterSpacing = 0.2.sp
            )
        }
    }
}

@Composable
private fun AestheticFilmRollGraphicPreview(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .width(62.dp)
            .height(84.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF18181B))
            .border(0.5.dp, Color(0x66FFFFFF), RoundedCornerShape(4.dp))
            .padding(horizontal = 2.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Sprockets
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 2.dp)
            ) {
                repeat(5) {
                    Box(
                        modifier = Modifier
                            .size(width = 3.dp, height = 4.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(Color(0x99FFFFFF))
                    )
                }
            }

            // Center film frames
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val filmPhotos = listOf(
                    Triple(Color(0xFFD97706), "01", "🎬"),
                    Triple(Color(0xFFDC2626), "02", "🎞️"),
                    Triple(Color(0xFF7C3AED), "03", "🎥")
                )
                filmPhotos.forEach { (color, num, icon) ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("▶ $num", fontSize = 3.5.sp, color = Color(0xFFFFB703), fontWeight = FontWeight.Bold)
                            Text("400", fontSize = 3.5.sp, color = Color(0xFFFFB703))
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Brush.linearGradient(listOf(color.copy(alpha = 0.9f), Color(0xFF1E1E24))))
                                .border(0.5.dp, Color(0x44FFFFFF), RoundedCornerShape(2.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(icon, fontSize = 6.sp)
                        }
                    }
                }
            }

            // Right Sprockets
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 2.dp)
            ) {
                repeat(5) {
                    Box(
                        modifier = Modifier
                            .size(width = 3.dp, height = 4.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(Color(0x99FFFFFF))
                    )
                }
            }
        }
    }
}

@Composable
private fun DuoPolaroidGraphicPreview(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .width(62.dp)
            .height(84.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFF1F5F9))
            .border(0.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top polaroid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White)
                    .border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(3.dp))
                    .padding(2.5.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(2.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFF43F5E), Color(0xFFFB923C)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💖", fontSize = 8.sp)
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Bottom polaroid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White)
                    .border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(3.dp))
                    .padding(2.5.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(2.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF38BDF8)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✨", fontSize = 8.sp)
                }
            }

            Text(
                text = "DUO POLAROID",
                fontSize = 4.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569)
            )
        }
    }
}

/**
 * Custom Layout Graphic Image Preview for user-created custom templates
 */
@Composable
private fun CustomTemplateGraphicPreview(
    config: CustomLayoutConfig,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Transparent)
            .border(
                1.dp,
                if (isSelected) StudioNeonPink else Color(0x33FFFFFF),
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (config.isPngOverlay && config.pngOverlayPath.isNotBlank()) {
            Box(
                modifier = Modifier
                    .width(62.dp)
                    .height(84.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Transparent)
                    .border(1.dp, if (isSelected) StudioNeonPink else Color(0x44FFFFFF), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = File(config.pngOverlayPath),
                    contentDescription = "Preview Frame PNG",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .width(62.dp)
                    .height(84.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(config.frameColorHex))
                    .border(1.dp, if (isSelected) StudioNeonPink else Color(0x44FFFFFF), RoundedCornerShape(6.dp))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val cols = config.columns.coerceIn(1, 3)
                    val rows = Math.ceil(config.photoCount.toDouble() / cols).toInt().coerceAtMost(4)

                    Column(
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        for (r in 0 until rows) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                for (c in 0 until cols) {
                                    val idx = (r * cols) + c
                                    if (idx < config.photoCount) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(if (rows == 1) 40.dp else if (rows == 2) 20.dp else 13.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(Color(0x55FFFFFF))
                                                .border(0.5.dp, Color(0x33000000), RoundedCornerShape(2.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (config.stickerEmoji.isNotBlank()) {
                        Text(text = config.stickerEmoji, fontSize = 8.sp)
                    }
                }
            }
        }
    }
}

/**
 * Thumbnail Visual Indicator for each template (maintained for compatibility)
 */
@Composable
private fun TemplateThumbnailIcon(
    layout: PhotoBoothLayout,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    TemplateLayoutGraphicPreview(layout, isSelected, modifier)
}

/**
 * Custom Thumbnail Visual Indicator for user-created custom templates (maintained for compatibility)
 */
@Composable
private fun CustomTemplateThumbnailIcon(
    config: CustomLayoutConfig,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    CustomTemplateGraphicPreview(config, isSelected, modifier)
}

/**
  * Custom Layout Designer Dialog for user-defined photo booth layouts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomLayoutDesignerDialog(
    initialConfig: CustomLayoutConfig,
    onApply: (CustomLayoutConfig) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf(initialConfig.name) }
    var photoCount by remember { mutableIntStateOf(initialConfig.photoCount) }
    var columns by remember { mutableIntStateOf(initialConfig.columns) }
    var photoAspectRatio by remember { mutableFloatStateOf(initialConfig.photoAspectRatio) }
    var cornerRadiusDp by remember { mutableFloatStateOf(initialConfig.cornerRadiusDp) }
    var frameColorHex by remember { mutableLongStateOf(initialConfig.frameColorHex) }
    var eventTitle by remember { mutableStateOf(initialConfig.eventTitle) }
    var captionText by remember { mutableStateOf(initialConfig.captionText) }
    var stickerEmoji by remember { mutableStateOf(initialConfig.stickerEmoji) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = StudioNeonPink,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (initialConfig.id.isNotBlank() && initialConfig.id != "custom_layout") "Edit Layout Custom" else "Desain Layout Custom",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = StudioTextPrimary
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
                // 1. Live Miniature Visual Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(frameColorHex))
                        .border(1.dp, Color(0x338B5CF6), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (eventTitle.isNotBlank()) {
                            Text(
                                text = eventTitle,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        // Grid preview slots
                        val rows = Math.ceil(photoCount.toDouble() / columns).toInt()
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (r in 0 until rows) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (c in 0 until columns) {
                                        val idx = (r * columns) + c
                                        if (idx < photoCount) {
                                            Box(
                                                modifier = Modifier
                                                    .size(
                                                        width = if (columns == 1) 120.dp else if (columns == 2) 50.dp else 34.dp,
                                                        height = if (columns == 1) 24.dp else if (columns == 2) 30.dp else 22.dp
                                                    )
                                                    .clip(RoundedCornerShape((cornerRadiusDp * 0.4f).dp))
                                                    .background(Color(0x55FFFFFF))
                                                    .border(1.dp, Color(0x77FFFFFF), RoundedCornerShape((cornerRadiusDp * 0.4f).dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "${idx + 1}", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (captionText.isNotBlank()) {
                            Text(
                                text = "$stickerEmoji $captionText",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }

                // 2. Nama Layout
                Column {
                    Text("Nama Layout Custom", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StudioTextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth().testTag("custom_layout_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = StudioTextPrimary,
                            unfocusedTextColor = StudioTextPrimary,
                            focusedContainerColor = StudioSurfaceVariant,
                            unfocusedContainerColor = StudioSurfaceVariant,
                            focusedBorderColor = StudioViolet,
                            unfocusedBorderColor = Color(0x338B5CF6)
                        )
                    )
                }

                // 3. Jumlah Pose Foto (1..6)
                Column {
                    Text("Jumlah Pose Foto (${photoCount} Pose)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StudioTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(1, 2, 3, 4, 5, 6)) { count ->
                            FilterChip(
                                selected = photoCount == count,
                                onClick = { photoCount = count },
                                label = { Text("$count Pose", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StudioViolet,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("custom_pose_$count")
                            )
                        }
                    }
                }

                // 4. Susunan Grid / Kolom
                Column {
                    Text("Susunan Layout Grid", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StudioTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            1 to "Strip Vertikal (1 Kolom)",
                            2 to "Grid 2 Kolom",
                            3 to "Wide 3 Kolom"
                        ).forEach { (col, label) ->
                            FilterChip(
                                selected = columns == col,
                                onClick = { columns = col },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StudioViolet,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("custom_cols_$col")
                            )
                        }
                    }
                }

                // 5. Rasio Foto
                Column {
                    Text("Proposi Rasio Frame Foto", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StudioTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            0.75f to "Portrait 4:3",
                            1.0f to "Kotak 1:1",
                            1.33f to "Landscape 3:4"
                        ).forEach { (ratio, label) ->
                            FilterChip(
                                selected = photoAspectRatio == ratio,
                                onClick = { photoAspectRatio = ratio },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StudioViolet,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // 6. Sudut Frame Foto (Corner Radius)
                Column {
                    Text("Bentuk Sudut Bingkai", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StudioTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            0f to "Tajam (0dp)",
                            16f to "Soft (16dp)",
                            28f to "Round (28dp)"
                        ).forEach { (radius, label) ->
                            FilterChip(
                                selected = cornerRadiusDp == radius,
                                onClick = { cornerRadiusDp = radius },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StudioViolet,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // 7. Warna Bingkai Background
                Column {
                    Text("Warna Bingkai Canvas", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StudioTextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(PRESET_FRAME_COLORS) { option ->
                            val isSelected = frameColorHex == option.colorHex
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(option.colorHex))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) StudioNeonPink else Color(0x44FFFFFF),
                                        shape = CircleShape
                                    )
                                    .clickable { frameColorHex = option.colorHex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(option.textColorHex),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 8. Header & Caption
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = eventTitle,
                        onValueChange = { eventTitle = it },
                        label = { Text("Judul Header Studio") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = captionText,
                        onValueChange = { captionText = it },
                        label = { Text("Teks Caption Footer") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // 9. Stiker
                Column {
                    Text("Stiker Dekoratif Footer", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StudioTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(PRESET_STICKERS) { sticker ->
                            val isSelected = stickerEmoji == sticker
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) StudioViolet else StudioSurfaceVariant)
                                    .clickable { stickerEmoji = sticker },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = sticker, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val config = initialConfig.copy(
                        name = name.ifBlank { "Layout Custom ($photoCount Pose)" },
                        photoCount = photoCount,
                        columns = columns,
                        photoAspectRatio = photoAspectRatio,
                        cornerRadiusDp = cornerRadiusDp,
                        frameColorHex = frameColorHex,
                        bgImagePath = "",
                        eventTitle = eventTitle,
                        captionText = captionText,
                        stickerEmoji = stickerEmoji
                    )
                    onApply(config)
                },
                colors = ButtonDefaults.buttonColors(containerColor = StudioViolet),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("apply_custom_layout_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simpan & Gunakan", fontWeight = FontWeight.Bold)
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
 * Slide card showcasing an externally imported PNG layout in the HorizontalPager carousel.
 * Allows swiping left and right, selecting, and editing (sunting) the frame.
 */
@Composable
fun ExternalPngSlideCard(
    customConfig: CustomLayoutConfig,
    isSelected: Boolean,
    pageIndex: Int,
    totalPages: Int,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("external_png_slide_${customConfig.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF243048) else StudioSurface
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(StudioViolet, StudioNeonPink)),
                width = 2.dp
            )
        } else {
            CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(Color(0x338B5CF6), Color(0x22EC4899))),
                width = 1.dp
            )
        },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Status Badge & Quick Actions (Sunting & Hapus)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Status Pill
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Brush.horizontalGradient(listOf(StudioViolet, StudioNeonPink)))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SEDANG AKTIF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x22FFFFFF))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "FRAME PNG",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioTextSecondary
                        )
                    }
                }

                // Action Buttons: Sunting & Hapus
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Quick Edit Pill Button
                    OutlinedButton(
                        onClick = onEdit,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, StudioNeonCyan),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("quick_edit_png_${customConfig.id}")
                    ) {
                        Text(
                            text = "Sunting",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioNeonCyan
                        )
                    }

                    // Delete Button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("delete_png_${customConfig.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Frame PNG",
                            tint = StudioNeonPink,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Graphic Preview of the Frame PNG and cutouts
            ExternalPngSlideGraphicPreview(
                config = customConfig,
                isSelected = isSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(215.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Layout Name
            Text(
                text = customConfig.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = StudioTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Info Subtitle
            Text(
                text = "${customConfig.photoCount} Slot Foto • Desain PNG Eksternal",
                fontSize = 12.sp,
                color = StudioTextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Slide Action Buttons: Sunting Frame & Pilih / Pakai
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary Button: Sunting Frame
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("slide_btn_edit_${customConfig.id}"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, StudioVioletLight)
                ) {
                    Text(
                        text = "Sunting Frame",
                        color = StudioVioletLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Primary Button: Pilih / Gunakan
                Button(
                    onClick = onSelect,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(42.dp)
                        .testTag("slide_btn_select_${customConfig.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) StudioEmerald else StudioViolet
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSelected) "Sedang Dipakai" else "Pilih Frame Ini",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Slide card to prompt user to add another external PNG layout.
 */
@Composable
fun AddNewPngSlideCard(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onAddClick() }
            .testTag("add_new_png_slide_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF192237)
        ),
        border = BorderStroke(
            1.5.dp,
            Brush.linearGradient(listOf(Color(0x558B5CF6), Color(0x55EC4899)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(StudioViolet.copy(alpha = 0.4f), StudioNeonPink.copy(alpha = 0.4f)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tambah Frame PNG Baru",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = StudioTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Import file PNG transparan tambahan dari perangkat Anda. Otomatis mendeteksi lubang foto & dapat disunting kapan saja.",
                fontSize = 12.sp,
                color = StudioTextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StudioNeonPink),
                modifier = Modifier.height(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "+ Import Frame PNG",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Slide card shown when there are no external PNG layouts imported yet.
 */
@Composable
fun EmptyPngSlideCard(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onAddClick() }
            .testTag("empty_png_slide_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF192237)),
        border = BorderStroke(
            1.5.dp,
            Brush.linearGradient(listOf(Color(0x448B5CF6), Color(0x44EC4899)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(StudioViolet.copy(alpha = 0.3f), StudioNeonPink.copy(alpha = 0.3f)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = StudioVioletLight,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Belum Ada Frame PNG Eksternal",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = StudioTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Tambahkan frame PNG transparan Anda dari perangkat untuk dijadikan slide template foto booth interaktif yang bisa digeser dan disunting.",
                fontSize = 12.sp,
                color = StudioTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 6.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StudioViolet),
                modifier = Modifier.height(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "+ Tambah Frame PNG Sekarang",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Graphic preview of an external PNG layout displaying cutouts and the overlay bitmap.
 */
@Composable
fun ExternalPngSlideGraphicPreview(
    config: CustomLayoutConfig,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent)
            .border(
                1.dp,
                if (isSelected) StudioNeonPink.copy(alpha = 0.6f) else Color(0x33FFFFFF),
                RoundedCornerShape(16.dp)
            )
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        val aspect = (1f / config.overlayAspectRatio).coerceIn(0.45f, 1.6f)

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(aspect)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            val containerW = maxWidth
            val containerH = maxHeight

            // 1. Photo Cutout placeholders (underneath PNG) - Transparent without icons or numbers
            config.pngSlots.forEachIndexed { _, slot ->
                val slotLeft = containerW * slot.left
                val slotTop = containerH * slot.top
                val slotW = containerW * (slot.right - slot.left)
                val slotH = containerH * (slot.bottom - slot.top)

                Box(
                    modifier = Modifier
                        .offset(x = slotLeft, y = slotTop)
                        .size(width = slotW, height = slotH)
                        .clip(RoundedCornerShape(config.cornerRadiusDp.coerceIn(0f, 12f).dp))
                        .background(Color.Transparent)
                        .border(
                            0.5.dp,
                            Color(0x33FFFFFF),
                            RoundedCornerShape(config.cornerRadiusDp.coerceIn(0f, 12f).dp)
                        )
                )
            }

            // 2. The PNG overlay on top
            if (config.pngOverlayPath.isNotBlank()) {
                val file = File(config.pngOverlayPath)
                if (file.exists()) {
                    AsyncImage(
                        model = file,
                        contentDescription = "Frame PNG Overlay",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }
            }
        }
    }
}

/**
 * Sealed class representing items inside the Cover Flow carousel
 */
sealed class CoverFlowLayoutItem {
    abstract val id: String
    abstract val title: String

    data class ExternalPng(
        val config: CustomLayoutConfig,
        val type: ShowcaseLayoutType
    ) : CoverFlowLayoutItem() {
        override val id: String = config.id
        override val title: String = config.name
    }

    data class ManualCustom(
        val config: CustomLayoutConfig
    ) : CoverFlowLayoutItem() {
        override val id: String = config.id
        override val title: String = config.name
    }

    data class AddNewAction(
        val type: ShowcaseLayoutType,
        val actionTitle: String
    ) : CoverFlowLayoutItem() {
        override val id: String = "add_${type.name}"
        override val title: String = actionTitle
    }
}

/**
 * Card Component rendered for each item inside the Cover Flow carousel
 */
@Composable
fun CoverFlowCardItem(
    item: CoverFlowLayoutItem,
    isSelected: Boolean,
    isCentered: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .testTag("cover_flow_card_${item.id}"),
        contentAlignment = Alignment.Center
    ) {
        val isTablet = maxWidth > 300.dp || maxHeight > 440.dp
        val cardWidth = if (isTablet) 280.dp else 240.dp
        val cardHeight = if (isTablet) 420.dp else 370.dp

        val cardBorder = if (isSelected) {
            BorderStroke(
                3.dp,
                Brush.horizontalGradient(listOf(StudioViolet, StudioNeonPink, StudioNeonCyan))
            )
        } else {
            BorderStroke(
                1.5.dp,
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.6f),
                        Color.White.copy(alpha = 0.15f),
                        StudioViolet.copy(alpha = 0.4f)
                    )
                )
            )
        }

        Card(
            modifier = Modifier
                .width(cardWidth)
                .height(cardHeight)
                .shadow(
                    elevation = if (isCentered) 28.dp else 12.dp,
                    shape = RoundedCornerShape(22.dp),
                    ambientColor = if (isSelected) StudioViolet else Color.Black,
                    spotColor = if (isSelected) StudioNeonCyan else Color.Black
                )
                .clickable { onSelect() },
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xDD0B0F19)
            ),
            border = cardBorder,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.2f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (item) {
                    is CoverFlowLayoutItem.ExternalPng -> {
                        when (item.type) {
                            ShowcaseLayoutType.TWO_BY_SIX -> {
                                ExternalPng2x6Card(
                                    customConfig = item.config,
                                    isSelected = isSelected,
                                    onSelect = onSelect,
                                    onEdit = onEdit,
                                    onDelete = onDelete,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            ShowcaseLayoutType.FOUR_BY_SIX -> {
                                ExternalPng4x6Card(
                                    customConfig = item.config,
                                    isSelected = isSelected,
                                    onSelect = onSelect,
                                    onEdit = onEdit,
                                    onDelete = onDelete,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            ShowcaseLayoutType.PAPER_A4 -> {
                                ExternalPngA4Card(
                                    customConfig = item.config,
                                    isSelected = isSelected,
                                    onSelect = onSelect,
                                    onEdit = onEdit,
                                    onDelete = onDelete,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    is CoverFlowLayoutItem.ManualCustom -> {
                        CustomTemplateGraphicPreview(
                            config = item.config,
                            isSelected = isSelected,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    is CoverFlowLayoutItem.AddNewAction -> {
                        AddNewFrameCard(
                            type = item.type,
                            onClick = onAddNew,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Glass Shine / Reflection Overlay on top
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.12f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }
    }
}

/**
 * Card Component rendered for each item inside the Grid View
 */
@Composable
fun GridCardItem(
    item: CoverFlowLayoutItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBorder = if (isSelected) {
        BorderStroke(
            2.5.dp,
            Brush.horizontalGradient(listOf(StudioViolet, StudioNeonPink, StudioNeonCyan))
        )
    } else {
        BorderStroke(1.dp, StudioViolet.copy(alpha = 0.3f))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.80f)
            .testTag("grid_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = StudioSurfaceVariant.copy(alpha = 0.7f)
        ),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (item) {
                    is CoverFlowLayoutItem.ExternalPng -> {
                        when (item.type) {
                            ShowcaseLayoutType.TWO_BY_SIX -> {
                                ExternalPng2x6Card(
                                    customConfig = item.config,
                                    isSelected = isSelected,
                                    onSelect = onSelect,
                                    onEdit = onEdit,
                                    onDelete = onDelete
                                )
                            }
                            ShowcaseLayoutType.FOUR_BY_SIX -> {
                                ExternalPng4x6Card(
                                    customConfig = item.config,
                                    isSelected = isSelected,
                                    onSelect = onSelect,
                                    onEdit = onEdit,
                                    onDelete = onDelete
                                )
                            }
                            ShowcaseLayoutType.PAPER_A4 -> {
                                ExternalPngA4Card(
                                    customConfig = item.config,
                                    isSelected = isSelected,
                                    onSelect = onSelect,
                                    onEdit = onEdit,
                                    onDelete = onDelete
                                )
                            }
                        }
                    }
                    is CoverFlowLayoutItem.ManualCustom -> {
                        CustomTemplateGraphicPreview(
                            config = item.config,
                            isSelected = isSelected,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    is CoverFlowLayoutItem.AddNewAction -> {
                        AddNewFrameCard(
                            type = item.type,
                            onClick = onAddNew,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (item !is CoverFlowLayoutItem.AddNewAction) {
                Button(
                    onClick = onSelect,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) StudioNeonPink else StudioViolet
                    ),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AKTIF", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    } else {
                        Text("PILIH", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
