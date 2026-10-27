package com.example.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.net.Uri
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.platform.LocalContext
import com.example.model.AppSettings
import com.example.model.ExportQuality
import com.example.model.OutputFormat
import com.example.model.PrintPaperSize
import com.example.model.WelcomeHeroStyle
import com.example.model.WelcomeParticle
import com.example.model.WelcomeTheme
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import java.io.File
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioNeonCyan
import com.example.ui.theme.StudioNeonPink
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceGlass
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioViolet
import com.example.ui.theme.StudioVioletLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isPinUnlocked by viewModel.isPinUnlocked.collectAsStateWithLifecycle()
    val pinError by viewModel.pinError.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showPinDialog by remember { mutableStateOf(!isPinUnlocked && settings.isKioskMode) }
    var enteredPin by remember { mutableStateOf("") }

    var showChangePinDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }

    var showCloudinaryDialog by remember { mutableStateOf(false) }
    var editCloudName by remember { mutableStateOf(settings.cloudinaryCloudName) }
    var editApiKey by remember { mutableStateOf(settings.cloudinaryApiKey) }
    var editApiSecret by remember { mutableStateOf(settings.cloudinaryApiSecret) }
    var editFolder by remember { mutableStateOf(settings.cloudinaryFolder) }
    var editQrisUrl by remember { mutableStateOf(settings.qrisPaymentUrl) }
    var editUseCustomQris by remember { mutableStateOf(settings.useCustomQrisImage) }
    var editCustomQrisPath by remember { mutableStateOf(settings.customQrisImagePath) }

    val qrisLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.saveCustomQrisImage(uri) { savedPath ->
                editCustomQrisPath = savedPath
                android.widget.Toast.makeText(context, "Gambar QRIS berhasil disimpan secara permanen!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    val themeBgLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.saveCustomThemeBackgroundImage(uri) { _ ->
                android.widget.Toast.makeText(context, "Wallpaper tema visual berhasil diperbarui!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Welcome screen text editing state
    var editWelcomeTitle by remember { mutableStateOf(settings.welcomeTitle) }
    var editWelcomeTagline by remember { mutableStateOf(settings.welcomeTagline) }
    var editWelcomeDesc by remember { mutableStateOf(settings.welcomeDescription) }
    var editWelcomeBtn by remember { mutableStateOf(settings.welcomeButtonText) }

    LaunchedEffect(settings) {
        editCloudName = settings.cloudinaryCloudName
        editApiKey = settings.cloudinaryApiKey
        editApiSecret = settings.cloudinaryApiSecret
        editFolder = settings.cloudinaryFolder
        editQrisUrl = settings.qrisPaymentUrl
        editUseCustomQris = settings.useCustomQrisImage
        editCustomQrisPath = settings.customQrisImagePath
        editWelcomeTitle = settings.welcomeTitle
        editWelcomeTagline = settings.welcomeTagline
        editWelcomeDesc = settings.welcomeDescription
        editWelcomeBtn = settings.welcomeButtonText
    }

    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pengaturan & Kiosk Manager",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = StudioTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = StudioTextPrimary
                        )
                    }
                },
                actions = {
                    if (settings.isKioskMode) {
                        IconButton(
                            onClick = {
                                if (isPinUnlocked) {
                                    viewModel.lockSettings()
                                } else {
                                    showPinDialog = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isPinUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = "Kiosk Status",
                                tint = if (isPinUnlocked) StudioEmerald else StudioNeonPink
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xCC0F172A)
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Kiosk Status Alert Banner
                if (settings.isKioskMode && !isPinUnlocked) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x33EC4899)),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(StudioNeonPink, StudioViolet)))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = StudioNeonPink)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Mode Kiosk Aktif (Terkunci)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                    Text("Masukkan PIN Admin untuk mengubah pengaturan.", color = StudioTextSecondary, fontSize = 12.sp)
                                }
                            }
                            Button(
                                onClick = { showPinDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = StudioNeonPink),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("unlock_pin_button")
                            ) {
                                Text("Buka PIN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // SECTION 1: EVENT BRANDING & WATERMARK
            item {
                SettingsSection(
                    title = "BRANDING ACARA & WATERMARK",
                    icon = Icons.Default.BrandingWatermark
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = settings.eventTitle,
                            onValueChange = { viewModel.updateEventBranding(it, settings.eventSubtitle) },
                            enabled = !settings.isKioskMode || isPinUnlocked,
                            label = { Text("Nama Acara / Header Title") },
                            placeholder = { Text("Contoh: JOHN & JANE WEDDING") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_event_title_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = getTextFieldColors(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )

                        OutlinedTextField(
                            value = settings.eventSubtitle,
                            onValueChange = { viewModel.updateEventBranding(settings.eventTitle, it) },
                            enabled = !settings.isKioskMode || isPinUnlocked,
                            label = { Text("Subtitle / Caption Default") },
                            placeholder = { Text("Contoh: SWEET MEMORIES 2026 ✨") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_event_subtitle_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = getTextFieldColors(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )
                    }
                }
            }

            // SECTION 2: TAMPILAN WELCOME SCREEN (KIOSK)
            item {
                SettingsSection(
                    title = "TAMPILAN WELCOME SCREEN (KIOSK)",
                    icon = Icons.Default.Palette
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // 1. Live Mini-Preview Card
                        WelcomePreviewCard(
                            title = editWelcomeTitle.ifBlank { "PHOTO BOOTH" },
                            tagline = editWelcomeTagline.ifBlank { "STUDIO MEMORIES" },
                            buttonText = editWelcomeBtn.ifBlank { "TAP TO START" },
                            theme = settings.welcomeTheme,
                            heroStyle = settings.welcomeHeroStyle,
                            particle = settings.welcomeParticle,
                            showTopBadge = settings.showWelcomeTopBadge,
                            showFeatures = settings.showWelcomeFeatures
                        )

                        // Wallpaper Tema UI (Gambar Unggahan) Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = StudioSurfaceGlass.copy(alpha = 0.9f)),
                            border = BorderStroke(1.dp, StudioViolet.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(StudioViolet.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Palette,
                                                contentDescription = null,
                                                tint = StudioVioletLight,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "Wallpaper Tema UI (Gambar Unggahan)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = StudioTextPrimary
                                            )
                                            Text(
                                                text = "Jadikan gambar latar belakang tema visual seluruh aplikasi",
                                                fontSize = 11.sp,
                                                color = StudioTextSecondary
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = settings.useCustomThemeBackground,
                                        onCheckedChange = {
                                            viewModel.updateCustomThemeBackgroundEnabled(it)
                                        },
                                        enabled = !settings.isKioskMode || isPinUnlocked,
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = StudioViolet
                                        )
                                    )
                                }

                                if (settings.useCustomThemeBackground) {
                                    // Wallpaper Preview Thumbnail & Controls
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 65.dp, height = 100.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .border(1.dp, StudioViolet.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                        ) {
                                            if (settings.customThemeBackgroundPath.isNotBlank()) {
                                                val file = remember(settings.customThemeBackgroundPath) {
                                                    File(settings.customThemeBackgroundPath)
                                                }
                                                AsyncImage(
                                                    model = file,
                                                    contentDescription = "Thumbnail Wallpaper",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Image(
                                                    painter = painterResource(id = R.drawable.img_theme_bg),
                                                    contentDescription = "Thumbnail Wallpaper",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = if (settings.customThemeBackgroundPath.isNotBlank())
                                                    "Wallpaper Kustom (Galeri)"
                                                else
                                                    "Gambar Tema Bawaan Unggahan (Anime)",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = StudioTextPrimary
                                            )
                                            Text(
                                                text = "Transparansi & Scrim:",
                                                fontSize = 12.sp,
                                                color = StudioTextSecondary
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                listOf(
                                                    0.0f to "Jernih",
                                                    0.25f to "25% (Default)",
                                                    0.50f to "50%",
                                                    0.75f to "75%"
                                                ).forEach { (dim, label) ->
                                                    val isSelected = kotlin.math.abs(settings.customThemeBackgroundDim - dim) < 0.10f
                                                    SelectableChip(
                                                        label = label,
                                                        isSelected = isSelected,
                                                        enabled = !settings.isKioskMode || isPinUnlocked,
                                                        onClick = { viewModel.updateCustomThemeBackgroundDim(dim) }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { themeBgLauncher.launch("image/*") },
                                            enabled = !settings.isKioskMode || isPinUnlocked,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Ganti Gambar", fontSize = 12.sp)
                                        }

                                        if (settings.customThemeBackgroundPath.isNotBlank()) {
                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.updateCustomThemeBackgroundSettings(
                                                        enabled = true,
                                                        dim = settings.customThemeBackgroundDim,
                                                        path = ""
                                                    )
                                                },
                                                enabled = !settings.isKioskMode || isPinUnlocked,
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Reset", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Grid Column Count Configuration Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = StudioSurfaceGlass.copy(alpha = 0.9f)),
                            border = BorderStroke(1.dp, StudioViolet.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(StudioNeonCyan.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GridView,
                                            contentDescription = null,
                                            tint = StudioNeonCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Jumlah Kolom Grid Template",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = StudioTextPrimary
                                        )
                                        Text(
                                            text = "Atur jumlah kolom grid pada menu pemilihan layout & frame",
                                            fontSize = 11.sp,
                                            color = StudioTextSecondary
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(2, 3, 4).forEach { count ->
                                        val isSelected = settings.gridColumnCount == count
                                        Button(
                                            onClick = { viewModel.updateGridColumnCount(count) },
                                            enabled = !settings.isKioskMode || isPinUnlocked,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSelected) StudioViolet else StudioSurfaceVariant
                                            ),
                                            contentPadding = PaddingValues(vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = "$count Kolom",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else StudioTextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Preset Cepat
                        Text(
                            text = "Preset Cepat:",
                            color = StudioTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuickPresetButton(
                                label = "Anime Wallpaper",
                                onClick = {
                                    viewModel.updateWelcomeTheme(WelcomeTheme.ANIME_WALLPAPER)
                                    viewModel.updateWelcomeHeroStyle(WelcomeHeroStyle.DUAL_STRIP_MOCKUP)
                                    viewModel.updateWelcomeParticle(WelcomeParticle.STARS)
                                    viewModel.updateCustomThemeBackgroundEnabled(true)
                                    viewModel.updateActiveLoopAnimationId("preset_theme_wallpaper")
                                    viewModel.updateWelcomeText("PHOTO BOOTH", "STUDIO MEMORIES", "Abadikan setiap senyuman dalam strip foto klasik & animasi loop modern.", "TAP TO START")
                                },
                                modifier = Modifier.weight(1f)
                            )
                            QuickPresetButton(
                                label = "Neon Studio",
                                onClick = {
                                    viewModel.updateWelcomeTheme(WelcomeTheme.NEON_STUDIO)
                                    viewModel.updateWelcomeHeroStyle(WelcomeHeroStyle.DUAL_STRIP_MOCKUP)
                                    viewModel.updateWelcomeParticle(WelcomeParticle.STARS)
                                    viewModel.updateWelcomeText("PHOTO BOOTH", "STUDIO MEMORIES", "Abadikan setiap senyuman dalam strip foto klasik & animasi loop modern.", "TAP TO START")
                                },
                                modifier = Modifier.weight(1f)
                            )
                            QuickPresetButton(
                                label = "Wedding",
                                onClick = {
                                    viewModel.updateWelcomeTheme(WelcomeTheme.PASTEL_DREAM)
                                    viewModel.updateWelcomeHeroStyle(WelcomeHeroStyle.SINGLE_POLAROID)
                                    viewModel.updateWelcomeParticle(WelcomeParticle.HEARTS)
                                    viewModel.updateWelcomeText("THE WEDDING", "SWEET CELEBRATION", "Selamat datang di photo booth! Bergayalah dan simpan kenangan indah hari ini.", "SENTUH UNTUK FOTO")
                                },
                                modifier = Modifier.weight(1f)
                            )
                            QuickPresetButton(
                                label = "Birthday",
                                onClick = {
                                    viewModel.updateWelcomeTheme(WelcomeTheme.RETRO_SUNSET)
                                    viewModel.updateWelcomeHeroStyle(WelcomeHeroStyle.DUAL_STRIP_MOCKUP)
                                    viewModel.updateWelcomeParticle(WelcomeParticle.CONFETTI)
                                    viewModel.updateWelcomeText("BIRTHDAY BASH", "PARTY TIME", "Yuk ambil foto seru bareng teman-teman! Bergaya sekarang!", "MULAI BERGAYA")
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // 3. Pilihan Tema Warna
                        Text(
                            text = "Tema Warna (Palette):",
                            color = StudioTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            WelcomeTheme.entries.forEach { themeOption ->
                                ThemeSelectionRow(
                                    theme = themeOption,
                                    isSelected = settings.welcomeTheme == themeOption,
                                    enabled = !settings.isKioskMode || isPinUnlocked,
                                    onSelect = { viewModel.updateWelcomeTheme(themeOption) }
                                )
                            }
                        }

                        // 4. Gaya Visual Hero
                        Text(
                            text = "Gaya Visual Hero:",
                            color = StudioTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WelcomeHeroStyle.entries.forEach { style ->
                                SelectableChip(
                                    label = when (style) {
                                        WelcomeHeroStyle.DUAL_STRIP_MOCKUP -> "Dua Strip"
                                        WelcomeHeroStyle.SINGLE_POLAROID -> "Polaroid"
                                        WelcomeHeroStyle.MINIMAL_SHUTTER -> "Lensa"
                                        WelcomeHeroStyle.LOOP_ANIMATION -> "Loop Manager"
                                    },
                                    isSelected = settings.welcomeHeroStyle == style,
                                    enabled = !settings.isKioskMode || isPinUnlocked,
                                    onClick = { viewModel.updateWelcomeHeroStyle(style) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // 5. Partikel Latar Belakang
                        Text(
                            text = "Efek Partikel Latar:",
                            color = StudioTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WelcomeParticle.entries.forEach { particle ->
                                SelectableChip(
                                    label = when (particle) {
                                        WelcomeParticle.STARS -> "✨ Bintang"
                                        WelcomeParticle.CONFETTI -> "🎉 Confetti"
                                        WelcomeParticle.HEARTS -> "💖 Hati"
                                        WelcomeParticle.NONE -> "🚫 Polos"
                                    },
                                    isSelected = settings.welcomeParticle == particle,
                                    enabled = !settings.isKioskMode || isPinUnlocked,
                                    onClick = { viewModel.updateWelcomeParticle(particle) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // 6. Kustomisasi Teks Welcome Screen
                        Text(
                            text = "Teks & Pesan Layar Welcome:",
                            color = StudioTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedTextField(
                            value = editWelcomeTitle,
                            onValueChange = { editWelcomeTitle = it },
                            enabled = !settings.isKioskMode || isPinUnlocked,
                            label = { Text("Judul Utama (Header)") },
                            placeholder = { Text("PHOTO BOOTH") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = getTextFieldColors(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        OutlinedTextField(
                            value = editWelcomeTagline,
                            onValueChange = { editWelcomeTagline = it },
                            enabled = !settings.isKioskMode || isPinUnlocked,
                            label = { Text("Tagline / Subjudul") },
                            placeholder = { Text("STUDIO MEMORIES") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = getTextFieldColors(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        OutlinedTextField(
                            value = editWelcomeDesc,
                            onValueChange = { editWelcomeDesc = it },
                            enabled = !settings.isKioskMode || isPinUnlocked,
                            label = { Text("Deskripsi / Sambutan") },
                            placeholder = { Text("Abadikan setiap senyuman...") },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = getTextFieldColors(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        OutlinedTextField(
                            value = editWelcomeBtn,
                            onValueChange = { editWelcomeBtn = it },
                            enabled = !settings.isKioskMode || isPinUnlocked,
                            label = { Text("Teks Tombol Mulai") },
                            placeholder = { Text("TAP TO START") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = getTextFieldColors(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                viewModel.updateWelcomeText(editWelcomeTitle, editWelcomeTagline, editWelcomeDesc, editWelcomeBtn)
                                focusManager.clearFocus()
                            })
                        )

                        Button(
                            onClick = {
                                viewModel.updateWelcomeText(editWelcomeTitle, editWelcomeTagline, editWelcomeDesc, editWelcomeBtn)
                                focusManager.clearFocus()
                            },
                            enabled = !settings.isKioskMode || isPinUnlocked,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StudioViolet)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simpan Perubahan Teks Welcome", fontWeight = FontWeight.Bold)
                        }

                        // 7. Opsi Elemen & Animasi
                        Text(
                            text = "Opsi Tampilan & Animasi:",
                            color = StudioTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        SettingsSwitchRow(
                            title = "Badge Acara Pojok Atas",
                            subtitle = "Tampilkan pill identitas acara di pojok kiri atas",
                            icon = Icons.Default.Visibility,
                            checked = settings.showWelcomeTopBadge,
                            enabled = !settings.isKioskMode || isPinUnlocked,
                            onCheckedChange = {
                                viewModel.updateWelcomeToggles(settings.showWelcomeFeatures, it, settings.enablePulsingAnimation)
                            }
                        )
                        SettingsSwitchRow(
                            title = "Chip Fitur Unggulan",
                            subtitle = "Tampilkan 4 chip fitur (Strip & Grid, Loop GIF, Retro, Cetak)",
                            icon = Icons.Default.Collections,
                            checked = settings.showWelcomeFeatures,
                            enabled = !settings.isKioskMode || isPinUnlocked,
                            onCheckedChange = {
                                viewModel.updateWelcomeToggles(it, settings.showWelcomeTopBadge, settings.enablePulsingAnimation)
                            }
                        )
                        SettingsSwitchRow(
                            title = "Animasi Denyut (Pulse Effect)",
                            subtitle = "Tombol Tap to Start berdenyut memikat pengunjung kiosk",
                            icon = Icons.Default.TouchApp,
                            checked = settings.enablePulsingAnimation,
                            enabled = !settings.isKioskMode || isPinUnlocked,
                            onCheckedChange = {
                                viewModel.updateWelcomeToggles(settings.showWelcomeFeatures, settings.showWelcomeTopBadge, it)
                            }
                        )

                        // 8. Reset ke Pengaturan Awal
                        OutlinedButton(
                            onClick = {
                                viewModel.resetWelcomeDefaults()
                            },
                            enabled = !settings.isKioskMode || isPinUnlocked,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, tint = StudioTextSecondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset Tampilan Welcome ke Default", color = StudioTextSecondary)
                        }
                    }
                }
            }

            // SECTION 3: KAMERA & CAPTURE
            item {
                SettingsSection(
                    title = "KAMERA & CAPTURE ENGINE",
                    icon = Icons.Default.Timer
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Default Countdown Interval
                        Text("Durasi Hitung Mundur Default:", color = StudioTextSecondary, fontSize = 13.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(3, 5, 10).forEach { sec ->
                                val isSelected = settings.defaultCountdownSeconds == sec
                                SelectableChip(
                                    label = "$sec Detik",
                                    isSelected = isSelected,
                                    enabled = !settings.isKioskMode || isPinUnlocked,
                                    onClick = { viewModel.updateCountdown(sec) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Burst Interval Pause
                        Text("Jeda Antar Pose (Burst Delay):", color = StudioTextSecondary, fontSize = 13.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(1, 2, 3).forEach { sec ->
                                val isSelected = settings.burstDelaySeconds == sec
                                SelectableChip(
                                    label = "$sec Detik",
                                    isSelected = isSelected,
                                    enabled = !settings.isKioskMode || isPinUnlocked,
                                    onClick = { viewModel.updateBurstDelay(sec) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Toggle Shutter Sound
                        SettingsSwitchRow(
                            title = "Efek Suara Shutter & Beep",
                            subtitle = "Suara hitungan mundur dan jepretan kamera",
                            icon = Icons.Default.VolumeUp,
                            checked = settings.isShutterSoundEnabled,
                            enabled = !settings.isKioskMode || isPinUnlocked,
                            onCheckedChange = { viewModel.updateShutterSound(it) }
                        )

                        // Toggle Screen Flash
                        SettingsSwitchRow(
                            title = "Efek Kilat Layar (Screen Flash)",
                            subtitle = "Animasi kilat putih saat foto diambil",
                            icon = Icons.Default.FlashOn,
                            checked = settings.isScreenFlashEnabled,
                            enabled = !settings.isKioskMode || isPinUnlocked,
                            onCheckedChange = { viewModel.updateScreenFlash(it) }
                        )

                        // Toggle Default Lens
                        SettingsSwitchRow(
                            title = "Gunakan Kamera Depan Default",
                            subtitle = "Mulai sesi menggunakan kamera selfie depan",
                            icon = Icons.Default.Cameraswitch,
                            checked = settings.defaultUseFrontCamera,
                            enabled = !settings.isKioskMode || isPinUnlocked,
                            onCheckedChange = { viewModel.updateDefaultCamera(it) }
                        )

                        // USB OTG & External Camera Support Info Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0x1506B6D4) // subtle cyan glass highlight
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3306B6D4)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Usb,
                                    contentDescription = "USB OTG",
                                    tint = StudioNeonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Kamera Eksternal & USB OTG",
                                        color = StudioTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Hubungkan webcam USB eksternal via kabel OTG. Anda bisa mengganti kamera secara real-time pada layar kamera ke opsi 'USB (OTG)'.",
                                        color = StudioTextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 3: PENYIMPANAN & OUTPUT EKSPOR
            item {
                SettingsSection(
                    title = "PENYIMPANAN & EKSPOR",
                    icon = Icons.Default.Hd
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Grid Column Count Configuration Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = StudioSurfaceGlass.copy(alpha = 0.9f)),
                            border = BorderStroke(1.dp, StudioViolet.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(StudioNeonCyan.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GridView,
                                            contentDescription = null,
                                            tint = StudioNeonCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Jumlah Kolom Grid Template",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = StudioTextPrimary
                                        )
                                        Text(
                                            text = "Atur jumlah kolom grid pada menu pemilihan layout & frame",
                                            fontSize = 11.sp,
                                            color = StudioTextSecondary
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(2, 3, 4).forEach { count ->
                                        val isSelected = settings.gridColumnCount == count
                                        Button(
                                            onClick = { viewModel.updateGridColumnCount(count) },
                                            enabled = !settings.isKioskMode || isPinUnlocked,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSelected) StudioViolet else StudioSurfaceVariant
                                            ),
                                            contentPadding = PaddingValues(vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = "$count Kolom",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else StudioTextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Text("Format Gambar Output:", color = StudioTextSecondary, fontSize = 13.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutputFormat.entries.forEach { fmt ->
                                val isSelected = settings.outputFormat == fmt
                                SelectableChip(
                                    label = fmt.name,
                                    isSelected = isSelected,
                                    enabled = !settings.isKioskMode || isPinUnlocked,
                                    onClick = { viewModel.updateOutputFormat(fmt) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Text("Resolusi Ekspor Canvas:", color = StudioTextSecondary, fontSize = 13.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ExportQuality.entries.forEach { qlt ->
                                val isSelected = settings.exportQuality == qlt
                                SelectableChip(
                                    label = qlt.label,
                                    isSelected = isSelected,
                                    enabled = !settings.isKioskMode || isPinUnlocked,
                                    onClick = { viewModel.updateExportQuality(qlt) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0x330F172A))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, tint = StudioVioletLight, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Lokasi Galeri: Pictures/PhotoBooth",
                                    fontSize = 12.sp,
                                    color = StudioTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 4: KIOSK & ADMIN SECURITY
            item {
                SettingsSection(
                    title = "KIOSK MODE & SEMATKAN APLIKASI",
                    icon = Icons.Default.Security
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        SettingsSwitchRow(
                            title = "Sematkan Aplikasi (Screen Pinning)",
                            subtitle = "Kunci aplikasi di layar utama Android agar tamu tidak bisa keluar",
                            icon = Icons.Default.PushPin,
                            checked = settings.isAppPinned,
                            enabled = !settings.isKioskMode || isPinUnlocked,
                            onCheckedChange = { viewModel.updateAppPinned(it) }
                        )

                        SettingsSwitchRow(
                            title = "Mode Layar Penuh Immersive",
                            subtitle = "Sembunyikan bilah navigasi dan status Android agar tampilan mulus",
                            icon = Icons.Default.Fullscreen,
                            checked = settings.isImmersiveFullscreen,
                            enabled = !settings.isKioskMode || isPinUnlocked,
                            onCheckedChange = { viewModel.updateImmersiveFullscreen(it) }
                        )

                        SettingsSwitchRow(
                            title = "Cegah Keluar Menggunakan PIN",
                            subtitle = "Minta PIN Admin ketika tombol Kembali ditekan di layar awal",
                            icon = Icons.Default.ExitToApp,
                            checked = settings.preventExitWithPin,
                            enabled = !settings.isKioskMode || isPinUnlocked,
                            onCheckedChange = { viewModel.updatePreventExitWithPin(it) }
                        )

                        SettingsSwitchRow(
                            title = "Aktifkan Mode Kiosk Lock",
                            subtitle = "Kunci halaman pengaturan agar tidak diubah tamu",
                            icon = Icons.Default.Lock,
                            checked = settings.isKioskMode,
                            enabled = !settings.isKioskMode || isPinUnlocked,
                            onCheckedChange = { viewModel.updateKioskMode(it) }
                        )

                        if (!settings.isKioskMode || isPinUnlocked) {
                            OutlinedButton(
                                onClick = { showChangePinDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("change_pin_button")
                            ) {
                                Icon(Icons.Default.VpnKey, contentDescription = null, tint = StudioVioletLight)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ubah PIN Admin (Saat ini: ${settings.adminPin})", color = StudioTextPrimary)
                            }
                        }
                    }
                }
            }

            // SECTION 5: CLOUDINARY & QRIS CLOUD INTEGRATION
            item {
                SettingsSection(
                    title = "CLOUDINARY & QRIS CLOUD INTEGRATION",
                    icon = Icons.Default.CloudUpload
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0x330F172A))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = StudioEmerald, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Cloud: ${settings.cloudinaryCloudName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = StudioTextPrimary
                                    )
                                }
                                Text(
                                    text = "API Key: ${settings.cloudinaryApiKey.take(4)}**** | Folder: /${settings.cloudinaryFolder}",
                                    fontSize = 12.sp,
                                    color = StudioTextSecondary
                                )
                                Text(
                                    text = "Foto strip otomatis diunggah ke Cloudinary dan dapat diunduh instan via scan QR code tamu atau link unduh.",
                                    fontSize = 12.sp,
                                    color = StudioVioletLight
                                )
                            }
                        }

                        if (!settings.isKioskMode || isPinUnlocked) {
                            OutlinedButton(
                                onClick = { showCloudinaryDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("edit_cloudinary_button")
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null, tint = StudioVioletLight)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Kelola Cloudinary & Link QRIS", color = StudioTextPrimary)
                            }
                        }
                    }
                }
            }

            // SECTION 6: PRINTER & CETAK PHOTO STRIP
            item {
                SettingsSection(
                    title = "PRINTER & CETAK PHOTO STRIP",
                    icon = Icons.Default.Print
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Jumlah Salinan Cetak Bawaan:", color = StudioTextSecondary, fontSize = 13.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(1, 2, 3, 4, 6).forEach { cp ->
                                val isSelected = settings.defaultPrintCopies == cp
                                SelectableChip(
                                    label = "$cp Lembar",
                                    isSelected = isSelected,
                                    enabled = !settings.isKioskMode || isPinUnlocked,
                                    onClick = {
                                        viewModel.updatePrintSettings(
                                            defaultCopies = cp,
                                            autoPrint = settings.autoPrintOnSessionEnd,
                                            paperSize = settings.defaultPaperSize,
                                            duplicateDual = settings.duplicateDualStrip
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        SettingsSwitchRow(
                            title = "Cetak Dua Strip (2 Strip Berdampingan)",
                            subtitle = "Cetak 2 strip identik di kertas 4x6 inci dengan garis potong ✂️",
                            icon = Icons.Default.Print,
                            checked = settings.duplicateDualStrip,
                            enabled = !settings.isKioskMode || isPinUnlocked,
                            onCheckedChange = {
                                viewModel.updatePrintSettings(
                                    defaultCopies = settings.defaultPrintCopies,
                                    autoPrint = settings.autoPrintOnSessionEnd,
                                    paperSize = settings.defaultPaperSize,
                                    duplicateDual = it
                                )
                            }
                        )

                        SettingsSwitchRow(
                            title = "Auto-Print Otomatis Saat Sesi Selesai",
                            subtitle = "Langsung kirim ke printer setelah foto selesai diedit",
                            icon = Icons.Default.Print,
                            checked = settings.autoPrintOnSessionEnd,
                            enabled = !settings.isKioskMode || isPinUnlocked,
                            onCheckedChange = {
                                viewModel.updatePrintSettings(
                                    defaultCopies = settings.defaultPrintCopies,
                                    autoPrint = it,
                                    paperSize = settings.defaultPaperSize,
                                    duplicateDual = settings.duplicateDualStrip
                                )
                            }
                        )

                        Text("Ukuran Kertas Bawaan:", color = StudioTextSecondary, fontSize = 13.sp)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PrintPaperSize.entries.forEach { size ->
                                val isSelected = settings.defaultPaperSize == size
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) StudioEmerald.copy(alpha = 0.2f) else StudioSurfaceVariant,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) StudioEmerald else Color.Transparent,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable(enabled = !settings.isKioskMode || isPinUnlocked) {
                                            viewModel.updatePrintSettings(
                                                defaultCopies = settings.defaultPrintCopies,
                                                autoPrint = settings.autoPrintOnSessionEnd,
                                                paperSize = size,
                                                duplicateDual = settings.duplicateDualStrip
                                            )
                                        }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(size.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = StudioTextPrimary)
                                        Text(size.description, fontSize = 11.sp, color = StudioTextSecondary)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Print, contentDescription = "Terpilih", tint = StudioEmerald, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Dialog Input PIN Admin
        if (showPinDialog) {
            AlertDialog(
                onDismissRequest = { showPinDialog = false },
                containerColor = StudioSurface,
                title = { Text("Masukkan PIN Admin", fontWeight = FontWeight.Bold, color = StudioTextPrimary) },
                text = {
                    Column {
                        Text("Masukkan 4 digit PIN untuk membuka akses konfigurasi.", color = StudioTextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = enteredPin,
                            onValueChange = {
                                if (it.length <= 6) {
                                    enteredPin = it
                                    viewModel.clearPinError()
                                }
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            isError = pinError != null,
                            colors = getTextFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("pin_text_input"),
                            placeholder = { Text("Default: 1234") }
                        )
                        if (pinError != null) {
                            Text(
                                text = pinError ?: "",
                                color = StudioNeonPink,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (viewModel.verifyPin(enteredPin)) {
                                showPinDialog = false
                                enteredPin = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioViolet),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("pin_confirm_button")
                    ) {
                        Text("Verifikasi")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPinDialog = false }) {
                        Text("Batal", color = StudioTextSecondary)
                    }
                }
            )
        }

        // Dialog Ubah PIN Admin
        if (showChangePinDialog) {
            AlertDialog(
                onDismissRequest = { showChangePinDialog = false },
                containerColor = StudioSurface,
                title = { Text("Ubah PIN Admin Baru", fontWeight = FontWeight.Bold, color = StudioTextPrimary) },
                text = {
                    Column {
                        Text("Masukkan PIN baru minimal 4 angka.", color = StudioTextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { if (it.length <= 6) newPinInput = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            colors = getTextFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("new_pin_input"),
                            placeholder = { Text("Contoh: 5678") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPinInput.length >= 4) {
                                viewModel.updateAdminPin(newPinInput)
                                showChangePinDialog = false
                                newPinInput = ""
                            }
                        },
                        enabled = newPinInput.length >= 4,
                        colors = ButtonDefaults.buttonColors(containerColor = StudioViolet),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Simpan PIN")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showChangePinDialog = false }) {
                        Text("Batal", color = StudioTextSecondary)
                    }
                }
            )
        }

        // Dialog Kelola Cloudinary & QRIS
        if (showCloudinaryDialog) {
            AlertDialog(
                onDismissRequest = { showCloudinaryDialog = false },
                containerColor = StudioSurface,
                title = { Text("Konfigurasi Cloudinary & QRIS", fontWeight = FontWeight.Bold, color = StudioTextPrimary) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Atur akun Cloudinary untuk penyimpanan awan dan link download otomatis.", color = StudioTextSecondary, fontSize = 12.sp)

                        OutlinedTextField(
                            value = editCloudName,
                            onValueChange = { editCloudName = it },
                            label = { Text("Cloud Name") },
                            placeholder = { Text("jggtvymn") },
                            colors = getTextFieldColors(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editApiKey,
                            onValueChange = { editApiKey = it },
                            label = { Text("API Key") },
                            placeholder = { Text("865291324538569") },
                            colors = getTextFieldColors(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editApiSecret,
                            onValueChange = { editApiSecret = it },
                            label = { Text("API Secret") },
                            placeholder = { Text("DdFr-TgDz8NTwz91psJSuvP1i5c") },
                            visualTransformation = PasswordVisualTransformation(),
                            colors = getTextFieldColors(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editFolder,
                            onValueChange = { editFolder = it },
                            label = { Text("Folder Penyimpanan") },
                            placeholder = { Text("photobooth") },
                            colors = getTextFieldColors(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editQrisUrl,
                            onValueChange = { editQrisUrl = it },
                            label = { Text("URL Link / Kode QRIS") },
                            placeholder = { Text("https://res.cloudinary.com/jggtvymn/...") },
                            colors = getTextFieldColors(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0x22FFFFFF))
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "🖼️ GAMBAR QRIS EKSTERNAL (KUSTOM)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioNeonCyan
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(StudioSurfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Gunakan Gambar Kustom", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StudioTextPrimary)
                                Text("Tampilkan file gambar QRIS eksternal secara langsung", fontSize = 10.sp, color = StudioTextSecondary)
                            }
                            Switch(
                                checked = editUseCustomQris,
                                onCheckedChange = { editUseCustomQris = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = StudioNeonCyan,
                                    checkedTrackColor = StudioViolet
                                )
                            )
                        }

                        if (editUseCustomQris) {
                            if (editCustomQrisPath.isNotBlank() && java.io.File(editCustomQrisPath).exists()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(StudioSurfaceVariant)
                                        .border(1.dp, StudioViolet.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AsyncImage(
                                        model = java.io.File(editCustomQrisPath),
                                        contentDescription = "Custom QRIS Preview",
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = { qrisLauncher.launch("image/*") },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, StudioViolet)
                                        ) {
                                            Text("Ganti Gambar", fontSize = 11.sp, color = StudioTextPrimary)
                                        }
                                        IconButton(
                                            onClick = { editCustomQrisPath = "" },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus",
                                                tint = StudioNeonPink,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tersimpan permanen di memori aplikasi",
                                        fontSize = 9.sp,
                                        color = StudioEmerald,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { qrisLauncher.launch("image/*") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, StudioViolet)
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = StudioNeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Pilih Gambar QRIS dari Galeri", fontSize = 12.sp, color = StudioTextPrimary)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateCloudinaryConfig(
                                cloudName = editCloudName.trim(),
                                apiKey = editApiKey.trim(),
                                apiSecret = editApiSecret.trim(),
                                folder = editFolder.trim()
                            )
                            viewModel.updateQrisPaymentUrl(editQrisUrl.trim())
                            viewModel.updateCustomQrisSettings(editUseCustomQris, editCustomQrisPath)
                            showCloudinaryDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioViolet),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Simpan Konfigurasi")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCloudinaryDialog = false }) {
                        Text("Batal", color = StudioTextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0x338B5CF6), Color(0x11EC4899))))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = StudioVioletLight,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = StudioVioletLight
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x220F172A))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(imageVector = icon, contentDescription = null, tint = StudioVioletLight, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.SemiBold, color = StudioTextPrimary, fontSize = 14.sp)
                Text(text = subtitle, color = StudioTextSecondary, fontSize = 12.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = StudioViolet,
                uncheckedThumbColor = StudioTextSecondary,
                uncheckedTrackColor = StudioSurfaceVariant
            )
        )
    }
}

@Composable
private fun SelectableChip(
    label: String,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isSelected) Modifier.background(Brush.horizontalGradient(listOf(StudioViolet, StudioNeonPink)))
                else Modifier.background(Color(0x22FFFFFF))
            )
            .border(
                1.dp,
                if (isSelected) Color.Transparent else Color(0x33FFFFFF),
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else StudioTextSecondary
        )
    }
}

@Composable
private fun getTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = StudioViolet,
    unfocusedBorderColor = Color(0x448B5CF6),
    focusedTextColor = StudioTextPrimary,
    unfocusedTextColor = StudioTextPrimary,
    focusedContainerColor = StudioSurfaceVariant,
    unfocusedContainerColor = StudioSurfaceVariant
)

@Composable
private fun WelcomePreviewCard(
    title: String,
    tagline: String,
    buttonText: String,
    theme: WelcomeTheme,
    heroStyle: WelcomeHeroStyle,
    particle: WelcomeParticle,
    showTopBadge: Boolean,
    showFeatures: Boolean
) {
    val primaryColor = Color(theme.primaryColorHex)
    val secondaryColor = Color(theme.secondaryColorHex)
    val accentColor = Color(theme.accentColorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0F19)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            primaryColor.copy(alpha = 0.18f),
                            Color(0xFF0F172A),
                            secondaryColor.copy(alpha = 0.12f)
                        )
                    )
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Preview Header Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(primaryColor))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("LIVE PREVIEW", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                }

                Text(
                    text = "${particle.name} • ${theme.displayName}",
                    fontSize = 10.sp,
                    color = StudioTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mini Hero Representation
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(primaryColor.copy(alpha = 0.4f), Color.Transparent)
                        )
                    )
                    .border(1.5.dp, primaryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (heroStyle) {
                        WelcomeHeroStyle.DUAL_STRIP_MOCKUP -> Icons.Default.Collections
                        WelcomeHeroStyle.SINGLE_POLAROID -> Icons.Default.CameraAlt
                        WelcomeHeroStyle.MINIMAL_SHUTTER -> Icons.Default.AutoAwesome
                        WelcomeHeroStyle.LOOP_ANIMATION -> Icons.Default.AutoAwesome
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title & Tagline
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = tagline,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = primaryColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Mini Button Preview
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(listOf(primaryColor, secondaryColor, accentColor)))
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ThemeSelectionRow(
    theme: WelcomeTheme,
    isSelected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit
) {
    val primaryColor = Color(theme.primaryColorHex)
    val secondaryColor = Color(theme.secondaryColorHex)
    val accentColor = Color(theme.accentColorHex)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) StudioSurfaceVariant else Color(0x1A0F172A))
            .border(
                1.5.dp,
                if (isSelected) primaryColor else Color(0x22FFFFFF),
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled) { onSelect() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 3 Color dots
            Row(horizontalArrangement = Arrangement.spacedBy((-4).dp)) {
                Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(primaryColor).border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape))
                Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(secondaryColor).border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape))
                Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(accentColor).border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = theme.displayName,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else StudioTextPrimary,
                    fontSize = 13.5.sp
                )
            }
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(primaryColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickPresetButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x22FFFFFF))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = StudioTextPrimary
        )
    }
}
