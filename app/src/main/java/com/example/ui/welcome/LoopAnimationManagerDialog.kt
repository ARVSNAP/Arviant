package com.example.ui.welcome

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AppSettings
import com.example.model.LoopAnimationItem
import com.example.model.LoopMediaType
import com.example.model.WelcomeLoopPlacement
import com.example.model.WelcomeMediaScale
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioNeonCyan
import com.example.ui.theme.StudioNeonPink
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.util.LoopAnimationRepository

/**
 * Loop Animation Manager Dialog
 * Memungkinkan pemilihan animasi bawaan, import video MP4 / GIF lokal, memasukkan URL media,
 * serta mengatur penempatan (Placement), tingkat redup (Dimming), dan skala media.
 */
@Composable
fun LoopAnimationManagerDialog(
    settings: AppSettings,
    savedAnimations: List<LoopAnimationItem>,
    activeAnimation: LoopAnimationItem?,
    onSelectAnimation: (LoopAnimationItem) -> Unit,
    onImportLocalFile: (Uri) -> Unit,
    onAddUrlMedia: (String) -> Unit,
    onDeleteAnimation: (LoopAnimationItem) -> Unit,
    onUpdatePlacement: (WelcomeLoopPlacement) -> Unit,
    onUpdateMediaScale: (WelcomeMediaScale) -> Unit,
    onUpdateDimScrim: (Float) -> Unit,
    onUpdateMuteSound: (Boolean) -> Unit,
    onUpdateAutoRotate: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedPreviewItem by remember { mutableStateOf<LoopAnimationItem?>(activeAnimation) }
    var urlInputText by remember { mutableStateOf("") }
    var urlInputError by remember { mutableStateOf<String?>(null) }

    // Media pickers
    val gifPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImportLocalFile(it) }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImportLocalFile(it) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .fillMaxHeight(0.92f)
                    .testTag("loop_animation_manager_dialog"),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.5.dp, Color(0x44EC4899))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEC4899).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFFEC4899)),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFEC4899),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "LOOP ANIMATION MANAGER",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Kelola animasi latar & loop video untuk Welcome Screen",
                                    fontSize = 11.5.sp,
                                    color = StudioTextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Preview Card (Top Half)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black)
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedPreviewItem != null) {
                            LoopMediaViewer(
                                item = selectedPreviewItem,
                                mediaScale = settings.welcomeMediaScale,
                                isHeroStyle = false,
                                isMuted = true,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = "Pilih animasi di bawah untuk pratinjau",
                                color = StudioTextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        // Preview overlay info tag
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.75f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = StudioNeonCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = selectedPreviewItem?.name ?: "Pratinjau Loop",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Navigation Tabs
                    val tabs = listOf("Koleksi Animasi", "Tambah Media", "Pengaturan Tampilan")
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color.White,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = Color(0xFFEC4899),
                                height = 3.dp
                            )
                        },
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTabIndex == index) Color.White else StudioTextSecondary
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab Content Body
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        when (selectedTabIndex) {
                            0 -> LoopLibraryTab(
                                animations = savedAnimations,
                                activeAnimationId = settings.activeLoopAnimationId,
                                onPreview = { selectedPreviewItem = it },
                                onApply = { item ->
                                    selectedPreviewItem = item
                                    onSelectAnimation(item)
                                },
                                onDelete = onDeleteAnimation
                            )

                            1 -> AddLoopMediaTab(
                                onImportGif = { gifPickerLauncher.launch("image/gif") },
                                onImportVideo = { videoPickerLauncher.launch("video/mp4") },
                                urlText = urlInputText,
                                onUrlTextChange = {
                                    urlInputText = it
                                    urlInputError = null
                                },
                                urlError = urlInputError,
                                onAddUrl = {
                                    if (urlInputText.isBlank()) {
                                        urlInputError = "Masukkan URL media terlebih dahulu"
                                    } else if (!urlInputText.startsWith("http://") && !urlInputText.startsWith("https://")) {
                                        urlInputError = "URL harus diawali dengan http:// atau https://"
                                    } else {
                                        onAddUrlMedia(urlInputText.trim())
                                        urlInputText = ""
                                        selectedTabIndex = 0
                                    }
                                }
                            )

                            2 -> LoopSettingsTab(
                                settings = settings,
                                onUpdatePlacement = onUpdatePlacement,
                                onUpdateMediaScale = onUpdateMediaScale,
                                onUpdateDimScrim = onUpdateDimScrim,
                                onUpdateMuteSound = onUpdateMuteSound,
                                onUpdateAutoRotate = onUpdateAutoRotate
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bottom Dismiss Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEC4899)
                        )
                    ) {
                        Text(
                            text = "Terapkan & Selesai",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoopLibraryTab(
    animations: List<LoopAnimationItem>,
    activeAnimationId: String,
    onPreview: (LoopAnimationItem) -> Unit,
    onApply: (LoopAnimationItem) -> Unit,
    onDelete: (LoopAnimationItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(animations, key = { it.id }) { item ->
            val isActive = item.id == activeAnimationId

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isActive) Color(0xFF1E293B) else Color(0xFF141D2E),
                border = BorderStroke(
                    width = if (isActive) 1.5.dp else 1.dp,
                    color = if (isActive) Color(0xFF00E5FF) else Color(0x22FFFFFF)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onPreview(item)
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (item.type) {
                                LoopMediaType.THEME_WALLPAPER -> Color(0xFFA78BFA).copy(alpha = 0.2f)
                                LoopMediaType.BUILTIN_PHOTO_REEL -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                                LoopMediaType.SYNTHWAVE_PARTICLES -> Color(0xFF8B5CF6).copy(alpha = 0.2f)
                                LoopMediaType.CINEMATIC_BOKEH -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                LoopMediaType.EXTERNAL_GIF -> Color(0xFF10B981).copy(alpha = 0.2f)
                                LoopMediaType.EXTERNAL_MP4 -> Color(0xFFEC4899).copy(alpha = 0.2f)
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = item.type.iconEmoji,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (item.isPreset) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF334155))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "PRESET",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StudioNeonCyan
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.description,
                                fontSize = 11.sp,
                                color = StudioTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isActive) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Aktif",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00E5FF)
                                    )
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onApply(item) },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0x66FFFFFF)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = "Pilih",
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }

                        if (!item.isPreset) {
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { onDelete(item) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus",
                                    tint = Color(0xFFF43F5E),
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

@Composable
private fun AddLoopMediaTab(
    onImportGif: () -> Unit,
    onImportVideo: () -> Unit,
    urlText: String,
    onUrlTextChange: (String) -> Unit,
    urlError: String?,
    onAddUrl: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Local File Pickers Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF141D2E),
            border = BorderStroke(1.dp, Color(0x3338BDF8)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "UNGGAH DARI PENYIMPANAN HP",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Pilih file GIF animasi atau video MP4 dari galeri perangkat. File akan tersimpan permanen di memori lokal aplikasi.",
                    fontSize = 11.5.sp,
                    color = StudioTextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onImportGif,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF06B6D4)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF06B6D4))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Unggah GIF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onImportVideo,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFEC4899)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEC4899))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Unggah MP4", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // URL Input Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF141D2E),
            border = BorderStroke(1.dp, Color(0x33A855F7)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "TAMBAH VIA LINK / URL ONLINE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Masukkan direct link file GIF atau MP4 dari web / Cloudinary / hosting Anda.",
                    fontSize = 11.5.sp,
                    color = StudioTextSecondary
                )

                OutlinedTextField(
                    value = urlText,
                    onValueChange = onUrlTextChange,
                    placeholder = {
                        Text("https://example.com/animation.mp4 atau .gif", fontSize = 12.sp, color = StudioTextSecondary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = urlError != null,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFA855F7),
                        unfocusedBorderColor = Color(0x44FFFFFF)
                    )
                )

                if (urlError != null) {
                    Text(
                        text = urlError,
                        color = Color(0xFFF43F5E),
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onAddUrl,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambahkan Media URL", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LoopSettingsTab(
    settings: AppSettings,
    onUpdatePlacement: (WelcomeLoopPlacement) -> Unit,
    onUpdateMediaScale: (WelcomeMediaScale) -> Unit,
    onUpdateDimScrim: (Float) -> Unit,
    onUpdateMuteSound: (Boolean) -> Unit,
    onUpdateAutoRotate: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Placement Selection
        Text(
            text = "POSISI & TAMPILAN ANIMASI",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            color = Color.White
        )

        val placements = WelcomeLoopPlacement.values()
        placements.forEach { placement ->
            val isSelected = settings.welcomeLoopPlacement == placement
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) Color(0xFF1E293B) else Color(0xFF141A29),
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) Color(0xFF00E5FF) else Color(0x22FFFFFF)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUpdatePlacement(placement) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = placement.displayName,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = placement.description,
                            fontSize = 10.5.sp,
                            color = StudioTextSecondary
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Video Scaling Mode
        Text(
            text = "SKALA TAMPILAN VIDEO / GIF",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            color = Color.White
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WelcomeMediaScale.values().forEach { scale ->
                val isSelected = settings.welcomeMediaScale == scale
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color(0xFF831843).copy(alpha = 0.4f) else Color(0xFF141A29),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) Color(0xFFEC4899) else Color(0x22FFFFFF)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onUpdateMediaScale(scale) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = scale.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else StudioTextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 3. Dimming Slider
        Text(
            text = "TINGKAT REDUP LATAR (DIMMING: ${(settings.welcomeLoopBackgroundDim * 100).toInt()}%)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            color = Color.White
        )

        Slider(
            value = settings.welcomeLoopBackgroundDim,
            onValueChange = onUpdateDimScrim,
            valueRange = 0.0f..0.8f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFEC4899),
                activeTrackColor = Color(0xFFEC4899),
                inactiveTrackColor = Color(0xFF334155)
            )
        )

        // 4. Mute Sound Switch
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF141D2E),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (settings.isWelcomeLoopSoundMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = StudioNeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bisukan Audio Video",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                Switch(
                    checked = settings.isWelcomeLoopSoundMuted,
                    onCheckedChange = onUpdateMuteSound,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFEC4899)
                    )
                )
            }
        }
    }
}
