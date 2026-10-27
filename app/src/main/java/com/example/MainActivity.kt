package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppScreen
import com.example.util.BitmapUtils
import com.example.ui.PhotoBoothViewModel
import com.example.ui.camera.CameraPermissionView
import com.example.ui.camera.CameraScreen
import com.example.ui.preview.PreviewResultScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.template.TemplateSelectionScreen
import com.example.ui.theme.AppThemeBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioViolet
import com.example.ui.theme.StudioVioletLight
import com.example.ui.welcome.WelcomeScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    PhotoBoothApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PhotoBoothApp(
    boothViewModel: PhotoBoothViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val uiState by boothViewModel.uiState.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Determine active app pinning / kiosk lock state
    val isAppPinnedOrKiosk = settings.isAppPinned || settings.isKioskMode

    // 1. Android Native Screen Pinning / Task Locking (startLockTask / stopLockTask)
    LaunchedEffect(isAppPinnedOrKiosk) {
        val activity = context.findActivity()
        if (activity != null) {
            try {
                if (isAppPinnedOrKiosk) {
                    activity.startLockTask()
                } else {
                    activity.stopLockTask()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 2. Immersive Sticky Fullscreen: Hide bottom navigation bar & top status bar
    val shouldHideSystemBars = settings.isImmersiveFullscreen || isAppPinnedOrKiosk || uiState.currentScreen == AppScreen.WELCOME
    LaunchedEffect(shouldHideSystemBars, uiState.currentScreen) {
        val activity = context.findActivity()
        val window = activity?.window
        if (window != null) {
            try {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                if (shouldHideSystemBars) {
                    insetsController.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    insetsController.hide(WindowInsetsCompat.Type.systemBars())
                } else {
                    insetsController.show(WindowInsetsCompat.Type.systemBars())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 3. System Back Navigation & Anti-Keluar Interception
    var lastBackPressTime by remember { mutableStateOf(0L) }
    var showPinExitDialog by remember { mutableStateOf(false) }
    var pinExitInput by remember { mutableStateOf("") }
    var pinExitError by remember { mutableStateOf<String?>(null) }

    BackHandler {
        when (uiState.currentScreen) {
            AppScreen.SETTINGS -> {
                boothViewModel.navigateTo(AppScreen.TEMPLATE_SELECT)
            }
            AppScreen.TEMPLATE_SELECT -> {
                boothViewModel.navigateTo(AppScreen.WELCOME)
            }
            AppScreen.CAMERA -> {
                if (uiState.isSequenceRunning) {
                    boothViewModel.cancelBurstSequence()
                } else {
                    boothViewModel.navigateTo(AppScreen.TEMPLATE_SELECT)
                }
            }
            AppScreen.PREVIEW -> {
                boothViewModel.navigateTo(AppScreen.TEMPLATE_SELECT)
            }
            AppScreen.WELCOME -> {
                if (isAppPinnedOrKiosk || settings.preventExitWithPin) {
                    // Pinned / Kiosk protection: Do NOT exit directly! Require admin PIN to unlock or exit.
                    pinExitInput = ""
                    pinExitError = null
                    showPinExitDialog = true
                } else {
                    // Normal mode: Double-tap back protection to prevent accidental exit
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastBackPressTime < 2500L) {
                        context.findActivity()?.finish()
                    } else {
                        lastBackPressTime = currentTime
                        Toast.makeText(
                            context,
                            "Tekan sekali lagi untuk keluar dari Photo Booth",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    // Admin PIN Dialog to unpin or exit app safely
    if (showPinExitDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinExitDialog = false
                pinExitInput = ""
                pinExitError = null
            },
            containerColor = Color(0xFF1E2433),
            shape = RoundedCornerShape(20.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = null,
                    tint = StudioAmber,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Aplikasi Sedang Disematkan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Fitur sematkan aplikasi aktif untuk mencegah pengunjung keluar. Masukkan PIN Admin untuk melepas sematan atau keluar dari aplikasi:",
                        fontSize = 13.sp,
                        color = StudioTextSecondary
                    )
                    OutlinedTextField(
                        value = pinExitInput,
                        onValueChange = {
                            if (it.length <= 8) {
                                pinExitInput = it
                                pinExitError = null
                            }
                        },
                        label = { Text("PIN Admin") },
                        placeholder = { Text("Default: 1234") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        singleLine = true,
                        isError = pinExitError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinExitError != null) {
                        Text(
                            text = pinExitError ?: "",
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinExitInput == settings.adminPin) {
                            settingsViewModel.updateAppPinned(false)
                            settingsViewModel.updateKioskMode(false)
                            try {
                                context.findActivity()?.stopLockTask()
                            } catch (_: Exception) {}
                            showPinExitDialog = false
                            Toast.makeText(
                                context,
                                "Sematan dibuka & keluar aplikasi",
                                Toast.LENGTH_SHORT
                            ).show()
                            context.findActivity()?.finish()
                        } else {
                            pinExitError = "PIN Salah! Masukkan PIN yang benar."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StudioViolet
                    )
                ) {
                    Text("Keluar Aplikasi", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            if (pinExitInput == settings.adminPin) {
                                settingsViewModel.updateAppPinned(false)
                                settingsViewModel.updateKioskMode(false)
                                try {
                                    context.findActivity()?.stopLockTask()
                                } catch (_: Exception) {}
                                showPinExitDialog = false
                                Toast.makeText(
                                    context,
                                    "Sematan berhasil dilepas. Navigasi bebas.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                pinExitError = "PIN Salah! Masukkan PIN yang benar."
                            }
                        }
                    ) {
                        Text("Buka Sematan", color = StudioVioletLight)
                    }
                    TextButton(
                        onClick = {
                            showPinExitDialog = false
                            pinExitInput = ""
                            pinExitError = null
                        }
                    ) {
                        Text("Batal", color = StudioTextSecondary)
                    }
                }
            }
        )
    }

    // Runtime Permission Handler for CameraX
    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    // Main Screen Navigation with Smooth Crossfade and Global Theme Background
    AppThemeBackground(
        useThemeBackground = settings.useCustomThemeBackground && uiState.currentScreen != AppScreen.WELCOME,
        dimAlpha = settings.customThemeBackgroundDim,
        customImagePath = settings.customThemeBackgroundPath
    ) {
        Crossfade(
            targetState = uiState.currentScreen,
            animationSpec = tween(350),
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                AppScreen.WELCOME -> {
                WelcomeScreen(
                    uiState = uiState,
                    onStart = {
                        boothViewModel.navigateTo(AppScreen.TEMPLATE_SELECT)
                    },
                    onOpenSettings = {
                        boothViewModel.navigateTo(AppScreen.SETTINGS)
                    },
                    onOpenLoopManager = {
                        boothViewModel.openLoopAnimationManager()
                    },
                    onSelectLoopAnimation = { item ->
                        boothViewModel.selectLoopAnimation(item)
                    },
                    onImportLocalFile = { uri ->
                        boothViewModel.importLoopMedia(uri)
                    },
                    onAddUrlMedia = { url ->
                        boothViewModel.addUrlLoopMedia(url)
                    },
                    onDeleteAnimation = { item ->
                        boothViewModel.deleteLoopAnimation(item)
                    },
                    onUpdatePlacement = { plc ->
                        boothViewModel.updateWelcomeLoopPlacement(plc)
                    },
                    onUpdateMediaScale = { scale ->
                        boothViewModel.updateWelcomeMediaScale(scale)
                    },
                    onUpdateDimScrim = { dim ->
                        boothViewModel.updateWelcomeLoopBackgroundDim(dim)
                    },
                    onUpdateMuteSound = { muted ->
                        boothViewModel.updateWelcomeLoopSoundMuted(muted)
                    },
                    onUpdateAutoRotate = { auto ->
                        boothViewModel.updateAutoRotateLoopPlaylist(auto)
                    },
                    onCloseLoopManager = {
                        boothViewModel.closeLoopAnimationManager()
                    },
                    onCycleTheme = {
                        boothViewModel.cycleWelcomeTheme()
                    }
                )
            }

            AppScreen.TEMPLATE_SELECT -> {
                TemplateSelectionScreen(
                    uiState = uiState,
                    onSelectLayout = { layout ->
                        boothViewModel.selectLayout(layout)
                    },
                    onSelectCustomLayout = { config ->
                        boothViewModel.selectCustomLayout(config)
                    },
                    onDeleteCustomLayout = { layoutId ->
                        boothViewModel.deleteCustomLayout(layoutId)
                    },
                    onOpenCustomDesigner = { configToEdit ->
                        boothViewModel.openCustomLayoutDesigner(true, configToEdit)
                    },
                    onSaveCustomLayout = { config ->
                        boothViewModel.saveCustomLayout(config)
                    },
                    onStartBooth = {
                        boothViewModel.navigateTo(AppScreen.CAMERA)
                    },
                    onOpenSettings = {
                        boothViewModel.navigateTo(AppScreen.SETTINGS)
                    },
                    onBackToWelcome = {
                        boothViewModel.navigateTo(AppScreen.WELCOME)
                    },
                    onImportPngLayout = { uri, onSuccess, onError ->
                        boothViewModel.importPngLayout(uri, onSuccess, onError)
                    }
                )
            }

            AppScreen.CAMERA -> {
                if (!cameraPermissionState.status.isGranted) {
                    CameraPermissionView(
                        onRequestPermission = {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    )
                } else {
                    CameraScreen(
                        uiState = uiState,
                        onPhotoCaptured = { bitmap ->
                            boothViewModel.onPhotoCaptured(bitmap)
                        },
                        bindCaptureTrigger = { triggerLambda ->
                            boothViewModel.onTriggerCameraShot = triggerLambda
                        },
                        onStartSequence = {
                            boothViewModel.startBurstSequence()
                        },
                        onCancelSequence = {
                            boothViewModel.cancelBurstSequence()
                        },
                        onToggleLens = {
                            boothViewModel.toggleCameraLens()
                        },
                        onToggleTorch = {
                            boothViewModel.toggleTorch()
                        },
                        onSelectTimerDuration = { seconds ->
                            boothViewModel.setTimerDuration(seconds)
                        },
                        onSelectFilter = { filter ->
                            boothViewModel.setFilter(filter)
                        },
                        onSelectBrightness = { brightness ->
                            boothViewModel.setBrightness(brightness)
                        },
                        onSelectContrast = { contrast ->
                            boothViewModel.setContrast(contrast)
                        },
                        onSelectSkinSmoothing = { smoothing ->
                            boothViewModel.setSkinSmoothing(smoothing)
                        },
                        onBackToTemplates = {
                            if (uiState.retakeSlotIndex != null) {
                                boothViewModel.cancelRetakeSinglePhoto()
                            } else {
                                boothViewModel.navigateTo(AppScreen.TEMPLATE_SELECT)
                            }
                        },
                        onSelectCameraSource = { source ->
                            boothViewModel.setCameraSource(source)
                        }
                    )
                }
            }

                AppScreen.PREVIEW -> {
                    PreviewResultScreen(
                        uiState = uiState,
                        onRetakeAll = {
                            boothViewModel.retakeAll()
                        },
                        onRetakeSinglePhoto = { slotIndex ->
                            boothViewModel.startRetakeSinglePhoto(slotIndex)
                        },
                        onSelectFilter = { filter ->
                            boothViewModel.setFilter(filter)
                        },
                        onSelectFrameColor = { colorOption ->
                            boothViewModel.setFrameColor(colorOption)
                        },
                        onUpdateCaption = { text ->
                            boothViewModel.setCaption(text)
                        },
                        onToggleDate = {
                            boothViewModel.toggleDate()
                        },
                        onSelectSticker = { sticker ->
                            boothViewModel.setSticker(sticker)
                        },
                        onSaveToGallery = {
                            boothViewModel.saveToGallery(context)
                        },
                        onSharePhoto = {
                            boothViewModel.sharePhotoStrip(context)
                        },
                        onShareToApp = { pkg ->
                            boothViewModel.shareToApp(context, pkg)
                        },
                        onPrintPhoto = { copies, paperSize, duplicateDual ->
                            boothViewModel.printPhotoStrip(context, copies, paperSize, duplicateDual)
                        },
                        onToggleQrDialog = { show ->
                            boothViewModel.toggleQrCodeDialog(show)
                        },
                        onResetSaveStatus = {
                            boothViewModel.resetSaveStatus()
                        },
                        onUploadToCloudinary = {
                            boothViewModel.uploadToCloudinary()
                        },
                        onSelectImageFrame = { frame ->
                            boothViewModel.selectImageFrame(frame)
                        },
                        onOpenCustomFrameDesigner = { frame ->
                            boothViewModel.openCustomFrameDesigner(frame)
                        },
                        onCloseCustomFrameDesigner = {
                            boothViewModel.closeCustomFrameDesigner()
                        },
                        onUpdateCustomFrameConfig = { frame ->
                            boothViewModel.updateCustomFrameConfig(frame)
                        },
                        onSaveCustomFrame = { frame ->
                            boothViewModel.saveCustomFrame(frame)
                        },
                        onDeleteCustomFrame = { frameId ->
                            boothViewModel.deleteCustomFrame(frameId)
                        },
                        onToggleLoopView = { active ->
                            boothViewModel.toggleLoopView(active)
                        },
                        onSetLoopMode = { mode ->
                            boothViewModel.setLoopMode(mode)
                        },
                        onSetLoopSpeed = { speed ->
                            boothViewModel.setLoopSpeed(speed)
                        },
                        onSetLoopTransition = { trans ->
                            boothViewModel.setLoopTransition(trans)
                        },
                        onSetLoopParticle = { particle ->
                            boothViewModel.setLoopParticle(particle)
                        },
                        onToggleLoopPlaying = {
                            boothViewModel.toggleLoopPlaying()
                        },
                        onSaveGif = {
                            boothViewModel.saveGifToGallery(context)
                        },
                        onShareGif = {
                            boothViewModel.shareGif(context)
                        },
                        onSaveMp4 = {
                            boothViewModel.saveMp4ToGallery(context)
                        },
                        onShareMp4 = {
                            boothViewModel.shareMp4(context)
                        },
                        onShareToSpecificApp = { pkg, type ->
                            boothViewModel.shareToSpecificApp(context, pkg, type)
                        },
                        onShowFullscreenLoop = { show ->
                            boothViewModel.setShowFullscreenLoop(show)
                        },
                        onOpenPhotoEditor = { index ->
                            boothViewModel.openPhotoEditor(index)
                        },
                        onClosePhotoEditor = {
                            boothViewModel.closePhotoEditor()
                        },
                        onSaveEditedPhoto = { index, bitmap ->
                            boothViewModel.updatePhotoAt(index, bitmap)
                        },
                        onApplyAdjustmentsToAll = { b, c, s, w, rot, flipH, flipV ->
                            val updated = uiState.capturedPhotos.map { p ->
                                BitmapUtils.applyPhotoAdjustments(
                                    source = p,
                                    brightness = b,
                                    contrast = c,
                                    saturation = s,
                                    warmth = w,
                                    rotationDegrees = rot,
                                    flipHorizontal = flipH,
                                    flipVertical = flipV
                                )
                            }
                            updated.forEachIndexed { i, bmp ->
                                boothViewModel.updatePhotoAt(i, bmp)
                            }
                        },
                        onResetPhotoToOriginal = { index ->
                            boothViewModel.resetPhotoToOriginal(index)
                        },
                        onReplacePhotoUri = { index, uri ->
                            boothViewModel.replacePhotoFromUri(index, uri, context)
                        },
                        onUpdatePhotoScaleMode = { mode ->
                            boothViewModel.updatePhotoScaleMode(mode)
                        },
                        onUpdatePhotoAlignment = { alignment ->
                            boothViewModel.updatePhotoAlignment(alignment)
                        },
                        onUpdatePhotoPan = { panX, panY ->
                            boothViewModel.updatePhotoPan(panX, panY)
                        },
                        onUpdatePhotoZoom = { zoom ->
                            boothViewModel.updatePhotoZoom(zoom)
                        },
                        onUpdateSlotBgColor = { color ->
                            boothViewModel.updateSlotBgColor(color)
                        },
                        onUpdateSlotAdjustment = { slotIndex, adj ->
                            boothViewModel.updateSlotAdjustment(slotIndex, adj)
                        },
                        onResetSlotAdjustments = {
                            boothViewModel.resetSlotAdjustments()
                        },
                        onSetCustomCanvasSettings = { useCustom, path ->
                            boothViewModel.setCustomCanvasSettings(useCustom, path)
                        },
                        onSaveCustomCanvasImage = { uri, onSaved ->
                            boothViewModel.saveCustomCanvasImage(uri, onSaved)
                        }
                    )
                }

                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigateBack = {
                            boothViewModel.navigateTo(AppScreen.TEMPLATE_SELECT)
                        }
                    )
                }
            }
        }
    }
}

internal fun android.content.Context.findActivity(): android.app.Activity? {
    var context = this
    while (context is android.content.ContextWrapper) {
        if (context is android.app.Activity) {
            return context
        }
        context = context.baseContext
    }
    return null
}
