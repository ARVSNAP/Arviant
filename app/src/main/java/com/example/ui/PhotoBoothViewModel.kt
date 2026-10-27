package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.ToneGenerator
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.print.PageRange
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AppSettings
import com.example.model.CameraSource
import com.example.model.AppScreen
import com.example.model.ColorFilterType
import com.example.model.CustomLayoutConfig
import com.example.model.FrameColorOption
import com.example.model.ImageFrameStyle
import com.example.model.PRESET_FRAME_COLORS
import com.example.model.PRESET_IMAGE_FRAMES
import com.example.model.PRESET_STICKERS
import com.example.model.PhotoBoothLayout
import com.example.model.PhotoBoothUiState
import com.example.model.SaveStatus
import com.example.model.CloudinaryUploadState
import com.example.model.LoopMode
import com.example.model.LoopSpeed
import com.example.model.LoopTransition
import com.example.model.LoopParticleEffect
import com.example.model.LoopAnimationConfig
import com.example.model.LoopAnimationItem
import com.example.model.WelcomeLoopPlacement
import com.example.model.WelcomeMediaScale
import com.example.model.PhotoScaleMode
import com.example.model.PhotoAlignment
import com.example.model.PhotoSlotAdjustment
import com.example.util.BitmapUtils
import com.example.util.SoundEffectManager
import com.example.util.CloudinaryResult
import com.example.util.CloudinaryService
import com.example.util.CustomLayoutRepository
import com.example.util.LoopAnimationRepository
import com.example.util.PngLayoutDetector
import com.example.util.SettingsRepository
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PhotoBoothViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application.applicationContext)
    private val customLayoutRepository = CustomLayoutRepository(application.applicationContext)
    private val loopRepository = LoopAnimationRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(PhotoBoothUiState())
    val uiState: StateFlow<PhotoBoothUiState> = _uiState.asStateFlow()

    private var toneGenerator: ToneGenerator? = null
    private val soundEffectManager = SoundEffectManager(application.applicationContext)
    private var sequenceJob: Job? = null
    var onTriggerCameraShot: (() -> Unit)? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        } catch (_: Exception) {
            toneGenerator = null
        }

        // Load saved loop animations
        val savedLoops = loopRepository.loadLoopAnimations()
        _uiState.update { current ->
            current.copy(savedLoopAnimations = savedLoops)
        }

        // Load persistent custom layouts from disk storage
        val persistedLayouts = customLayoutRepository.loadCustomLayouts()
        if (persistedLayouts.isNotEmpty()) {
            _uiState.update { current ->
                current.copy(
                    savedCustomLayouts = persistedLayouts
                )
            }
        }

        // Listen to settings changes from DataStore
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _uiState.update { current ->
                    current.copy(
                        settings = settings,
                        selectedTimerDuration = settings.defaultCountdownSeconds,
                        captionText = settings.eventSubtitle
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        toneGenerator?.release()
        toneGenerator = null
        soundEffectManager.release()
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { state ->
            state.copy(currentScreen = screen)
        }
    }

    fun selectLayout(layout: PhotoBoothLayout) {
        if (_uiState.value.isSequenceRunning) return
        _uiState.update {
            it.copy(
                selectedLayout = layout,
                isCustomLayoutActive = false,
                capturedPhotos = emptyList(),
                currentSlotIndex = 0,
                finalStripBitmap = null
            )
        }
    }

    fun openCustomLayoutDesigner(show: Boolean = true, configToEdit: CustomLayoutConfig? = null) {
        _uiState.update {
            it.copy(
                showCustomLayoutDesigner = show,
                editingCustomLayoutConfig = if (show) configToEdit else null
            )
        }
    }

    fun saveCustomLayout(config: CustomLayoutConfig) {
        if (_uiState.value.isSequenceRunning) return
        val layoutId = if (config.id.isBlank() || config.id == "custom_layout") {
            "custom_${System.currentTimeMillis()}"
        } else {
            config.id
        }
        val finalConfig = config.copy(id = layoutId)

        _uiState.update { currentState ->
            val updatedList = currentState.savedCustomLayouts.toMutableList()
            val existingIndex = updatedList.indexOfFirst { it.id == layoutId }
            if (existingIndex >= 0) {
                updatedList[existingIndex] = finalConfig
            } else {
                updatedList.add(0, finalConfig)
            }

            customLayoutRepository.saveCustomLayouts(updatedList)

            currentState.copy(
                customLayoutConfig = finalConfig,
                savedCustomLayouts = updatedList,
                isCustomLayoutActive = true,
                showCustomLayoutDesigner = false,
                editingCustomLayoutConfig = null,
                capturedPhotos = emptyList(),
                currentSlotIndex = 0,
                finalStripBitmap = null,
                captionText = finalConfig.captionText
            )
        }
    }

    fun selectCustomLayout(config: CustomLayoutConfig) {
        if (_uiState.value.isSequenceRunning) return
        _uiState.update {
            it.copy(
                customLayoutConfig = config,
                isCustomLayoutActive = true,
                capturedPhotos = emptyList(),
                currentSlotIndex = 0,
                finalStripBitmap = null,
                captionText = config.captionText
            )
        }
    }

    fun deleteCustomLayout(layoutId: String) {
        if (_uiState.value.isSequenceRunning) return
        _uiState.update { currentState ->
            val targetToDelete = currentState.savedCustomLayouts.find { it.id == layoutId }
            if (targetToDelete != null) {
                customLayoutRepository.deleteLayout(targetToDelete)
            }

            val updatedList = currentState.savedCustomLayouts.filterNot { it.id == layoutId }
            customLayoutRepository.saveCustomLayouts(updatedList)

            val isCurrentActiveDeleted = currentState.isCustomLayoutActive && currentState.customLayoutConfig.id == layoutId

            val nextActiveConfig = if (isCurrentActiveDeleted) {
                updatedList.firstOrNull() ?: CustomLayoutConfig()
            } else {
                currentState.customLayoutConfig
            }

            val nextIsCustomActive = if (isCurrentActiveDeleted) {
                updatedList.isNotEmpty()
            } else {
                currentState.isCustomLayoutActive
            }

            currentState.copy(
                savedCustomLayouts = updatedList,
                customLayoutConfig = nextActiveConfig,
                isCustomLayoutActive = nextIsCustomActive,
                capturedPhotos = if (isCurrentActiveDeleted) emptyList() else currentState.capturedPhotos,
                currentSlotIndex = if (isCurrentActiveDeleted) 0 else currentState.currentSlotIndex,
                finalStripBitmap = if (isCurrentActiveDeleted) null else currentState.finalStripBitmap
            )
        }
    }

    /**
     * Imports an external PNG file, detects transparent cutout slots,
     * and prepares a persistent CustomLayoutConfig
     */
    fun importPngLayout(
        uri: Uri,
        onSuccess: (CustomLayoutConfig, Bitmap) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val (filePath, bitmap) = customLayoutRepository.importPngFile(uri)
            if (filePath.isBlank() || bitmap == null) {
                onError("Gagal mengimpor file gambar. Pastikan memilih file gambar PNG yang valid.")
                return@launch
            }

            val detectedSlots = withContext(Dispatchers.Default) {
                PngLayoutDetector.detectTransparentSlots(bitmap)
            }

            val finalSlots = if (detectedSlots.isNotEmpty()) {
                detectedSlots
            } else {
                PngLayoutDetector.createPresetSlots(3)
            }

            val photoCount = finalSlots.size
            val config = CustomLayoutConfig(
                id = "png_layout_${System.currentTimeMillis()}",
                name = "Frame PNG ${(_uiState.value.savedCustomLayouts.count { it.isPngOverlay } + 1)}",
                photoCount = photoCount,
                columns = if (photoCount <= 3) 1 else 2,
                isPngOverlay = true,
                pngOverlayPath = filePath,
                pngSlots = finalSlots,
                overlayAspectRatio = (bitmap.height.toFloat() / bitmap.width.toFloat()).coerceIn(0.5f, 3.5f)
            )

            onSuccess(config, bitmap)
        }
    }

    fun toggleCameraLens() {
        _uiState.update { state ->
            val nextSource = if (state.selectedCameraSource == CameraSource.FRONT) CameraSource.BACK else CameraSource.FRONT
            state.copy(selectedCameraSource = nextSource)
        }
    }

    fun setCameraSource(source: CameraSource) {
        _uiState.update { it.copy(selectedCameraSource = source) }
    }

    fun toggleTorch() {
        _uiState.update { it.copy(isTorchOn = !it.isTorchOn) }
    }

    fun setTimerDuration(seconds: Int) {
        _uiState.update { it.copy(selectedTimerDuration = seconds) }
    }

    fun startBurstSequence() {
        if (_uiState.value.isSequenceRunning) return

        val retakeSlot = _uiState.value.retakeSlotIndex
        if (retakeSlot != null) {
            // Single photo retake mode for specific slot in layout
            _uiState.update {
                it.copy(
                    isSequenceRunning = true,
                    currentSlotIndex = retakeSlot
                )
            }

            sequenceJob = viewModelScope.launch {
                _uiState.update { it.copy(isCountingDown = true) }
                val timerSeconds = _uiState.value.selectedTimerDuration
                for (t in timerSeconds downTo 1) {
                    _uiState.update { it.copy(countdownNumber = t) }
                    playBeepSound()
                    delay(1000)
                }

                _uiState.update {
                    it.copy(
                        countdownNumber = 0,
                        isCountingDown = false,
                        isTakingShot = true,
                        showFlashScreen = _uiState.value.settings.isScreenFlashEnabled
                    )
                }
                playShutterSound()

                triggerCameraShotOrNikon()

                delay(250)
                _uiState.update { it.copy(showFlashScreen = false) }
                delay(600)

                _uiState.update {
                    it.copy(
                        isSequenceRunning = false,
                        isCountingDown = false,
                        isTakingShot = false,
                        retakeSlotIndex = null,
                        currentScreen = AppScreen.PREVIEW
                    )
                }
                renderFinalStrip()
            }
            return
        }

        val requiredPhotos = _uiState.value.activePhotoCount
        _uiState.update {
            it.copy(
                capturedPhotos = emptyList(),
                currentSlotIndex = 0,
                isSequenceRunning = true,
                finalStripBitmap = null
            )
        }

        sequenceJob = viewModelScope.launch {
            for (slot in 0 until requiredPhotos) {
                _uiState.update {
                    it.copy(
                        currentSlotIndex = slot,
                        isCountingDown = true
                    )
                }

                val timerSeconds = _uiState.value.selectedTimerDuration
                for (t in timerSeconds downTo 1) {
                    _uiState.update { it.copy(countdownNumber = t) }
                    playBeepSound()
                    delay(1000)
                }

                _uiState.update {
                    it.copy(
                        countdownNumber = 0,
                        isCountingDown = false,
                        isTakingShot = true,
                        showFlashScreen = _uiState.value.settings.isScreenFlashEnabled
                    )
                }
                playShutterSound()

                // Trigger CameraX Capture or Nikon DSLR Capture
                triggerCameraShotOrNikon()

                delay(250)
                _uiState.update { it.copy(showFlashScreen = false) }

                // Wait for photo to be processed & interval pause between shots
                delay(if (slot < requiredPhotos - 1) (_uiState.value.settings.burstDelaySeconds * 1000L).coerceAtLeast(1200L) else 800L)
            }

            _uiState.update {
                it.copy(
                    isSequenceRunning = false,
                    isCountingDown = false,
                    isTakingShot = false,
                    currentScreen = AppScreen.PREVIEW
                )
            }

            renderFinalStrip()
        }
    }

    fun cancelBurstSequence() {
        sequenceJob?.cancel()
        sequenceJob = null
        val retakeSlot = _uiState.value.retakeSlotIndex
        if (retakeSlot != null) {
            _uiState.update {
                it.copy(
                    isSequenceRunning = false,
                    isCountingDown = false,
                    isTakingShot = false,
                    showFlashScreen = false,
                    retakeSlotIndex = null,
                    currentScreen = AppScreen.PREVIEW
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isSequenceRunning = false,
                    isCountingDown = false,
                    isTakingShot = false,
                    showFlashScreen = false,
                    capturedPhotos = emptyList(),
                    currentSlotIndex = 0
                )
            }
        }
    }

    private suspend fun triggerCameraShotOrNikon() {
        withContext(Dispatchers.Main) {
            onTriggerCameraShot?.invoke()
        }
    }

    fun onPhotoCaptured(bitmap: Bitmap) {
        _uiState.update { state ->
            val retakeSlot = state.retakeSlotIndex
            val slotIndex = retakeSlot ?: state.currentSlotIndex
            
            val newState = if (retakeSlot != null) {
                val updatedList = state.capturedPhotos.toMutableList()
                val updatedOrig = state.originalCapturedPhotos.toMutableList()
                if (retakeSlot in updatedList.indices) {
                    updatedList[retakeSlot] = bitmap
                } else {
                    updatedList.add(bitmap)
                }
                if (retakeSlot in updatedOrig.indices) {
                    updatedOrig[retakeSlot] = bitmap
                } else {
                    updatedOrig.add(bitmap)
                }
                state.copy(
                    capturedPhotos = updatedList,
                    originalCapturedPhotos = updatedOrig,
                    isTakingShot = false
                )
            } else {
                val updatedList = state.capturedPhotos + bitmap
                val updatedOrig = state.originalCapturedPhotos + bitmap
                state.copy(
                    capturedPhotos = updatedList,
                    originalCapturedPhotos = updatedOrig,
                    isTakingShot = false
                )
            }
            
            // Trigger auto-crop
            viewModelScope.launch {
                val faceRect = com.example.util.FaceDetectionUtil.detectFace(bitmap)
                if (faceRect != null) {
                    val bitmapW = bitmap.width.toFloat()
                    val bitmapH = bitmap.height.toFloat()
                    
                    val faceCenterX = faceRect.centerX().toFloat()
                    val faceCenterY = faceRect.centerY().toFloat()
                    
                    val panX = (faceCenterX / bitmapW) - 0.5f
                    val panY = (faceCenterY / bitmapH) - 0.5f
                    
                    val adjustment = com.example.model.PhotoSlotAdjustment(
                        scaleMode = com.example.model.PhotoScaleMode.CROP_FILL,
                        alignment = com.example.model.PhotoAlignment.CENTER,
                        panX = panX * 2.0f, // Adjust scale factor as needed
                        panY = panY * 2.0f,
                        zoom = 1.5f, // Zoom in to focus on face
                        slotBgColorHex = 0xFFFFFFFF
                    )
                    updateSlotAdjustment(slotIndex, adjustment, 0L)
                }
            }
            
            newState
        }
    }

    fun startRetakeSinglePhoto(slotIndex: Int) {
        _uiState.update {
            it.copy(
                retakeSlotIndex = slotIndex,
                currentSlotIndex = slotIndex,
                showPhotoEditorDialog = false,
                currentScreen = AppScreen.CAMERA
            )
        }
    }

    fun cancelRetakeSinglePhoto() {
        _uiState.update {
            it.copy(
                retakeSlotIndex = null,
                isSequenceRunning = false,
                isCountingDown = false,
                isTakingShot = false,
                showFlashScreen = false,
                currentScreen = AppScreen.PREVIEW
            )
        }
    }

    fun openPhotoEditor(index: Int = 0) {
        _uiState.update {
            it.copy(
                showPhotoEditorDialog = true,
                editingPhotoIndex = index.coerceIn(0, maxOf(0, it.capturedPhotos.size - 1))
            )
        }
    }

    fun closePhotoEditor() {
        _uiState.update { it.copy(showPhotoEditorDialog = false) }
    }

    fun selectEditingPhotoIndex(index: Int) {
        _uiState.update {
            it.copy(editingPhotoIndex = index.coerceIn(0, maxOf(0, it.capturedPhotos.size - 1)))
        }
    }

    fun updatePhotoAt(index: Int, newBitmap: Bitmap) {
        _uiState.update { state ->
            val list = state.capturedPhotos.toMutableList()
            if (index in list.indices) {
                list[index] = newBitmap
            }
            state.copy(capturedPhotos = list)
        }
        renderFinalStrip()
    }

    fun replacePhotoFromUri(index: Int, uri: Uri, context: Context) {
        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                BitmapUtils.loadBitmapFromUri(context, uri)
            }
            if (bitmap != null) {
                updatePhotoAt(index, bitmap)
            }
        }
    }

    fun resetPhotoToOriginal(index: Int) {
        _uiState.update { state ->
            val orig = state.originalCapturedPhotos.getOrNull(index) ?: return@update state
            val list = state.capturedPhotos.toMutableList()
            if (index in list.indices) {
                list[index] = orig
            }
            state.copy(capturedPhotos = list)
        }
        renderFinalStrip()
    }

    fun resetAllPhotosToOriginal() {
        _uiState.update { state ->
            if (state.originalCapturedPhotos.isNotEmpty()) {
                state.copy(capturedPhotos = state.originalCapturedPhotos)
            } else {
                state
            }
        }
        renderFinalStrip()
    }

    fun setFilter(filter: ColorFilterType) {
        _uiState.update { it.copy(selectedFilter = filter) }
        renderFinalStrip()
    }

    fun setBrightness(brightness: Float) {
        _uiState.update { it.copy(brightness = brightness.coerceIn(-1f, 1f)) }
    }

    fun setContrast(contrast: Float) {
        _uiState.update { it.copy(contrast = contrast.coerceIn(0.5f, 2f)) }
    }

    fun setSkinSmoothing(smoothing: Float) {
        _uiState.update { it.copy(skinSmoothing = smoothing.coerceIn(0f, 1f)) }
    }

    fun setFrameColor(colorOption: FrameColorOption) {
        _uiState.update { it.copy(selectedFrameColor = colorOption) }
        renderFinalStrip()
    }

    fun setCaption(caption: String) {
        _uiState.update { it.copy(captionText = caption) }
        renderFinalStrip()
    }

    fun toggleDate() {
        _uiState.update { it.copy(showDate = !it.showDate) }
        renderFinalStrip()
    }

    fun setSticker(sticker: String) {
        _uiState.update { it.copy(selectedSticker = sticker) }
        renderFinalStrip()
    }

    fun selectImageFrame(frame: ImageFrameStyle) {
        _uiState.update { it.copy(selectedImageFrame = frame) }
        renderFinalStrip()
    }

    fun setCustomCanvasSettings(useCustom: Boolean, path: String) {
        _uiState.update {
            it.copy(
                useCustomCanvasImage = useCustom,
                customCanvasImagePath = path
            )
        }
        renderFinalStrip()
    }

    fun saveCustomCanvasImage(uri: Uri, onSaved: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val canvasDir = File(context.filesDir, "canvas_images").apply {
                    if (!exists()) mkdirs()
                }
                val fileName = "custom_canvas_${System.currentTimeMillis()}.png"
                val destFile = File(canvasDir, fileName)
                withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                val savedPath = destFile.absolutePath
                if (savedPath.isNotBlank()) {
                    onSaved(savedPath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun openCustomFrameDesigner(frameToEdit: ImageFrameStyle? = null) {
        val initialConfig = frameToEdit ?: ImageFrameStyle(
            id = "custom_frame_${System.currentTimeMillis()}",
            name = "Bingkai Custom ${(_uiState.value.savedCustomFrames.size + 1)}",
            isCustom = true
        )
        _uiState.update {
            it.copy(
                customImageFrameConfig = initialConfig,
                showCustomFrameDesigner = true
            )
        }
    }

    fun closeCustomFrameDesigner() {
        _uiState.update { it.copy(showCustomFrameDesigner = false) }
    }

    fun updateCustomFrameConfig(updatedFrame: ImageFrameStyle) {
        _uiState.update { it.copy(customImageFrameConfig = updatedFrame) }
    }

    fun saveCustomFrame(frameToSave: ImageFrameStyle) {
        val currentSaved = _uiState.value.savedCustomFrames.toMutableList()
        val existingIndex = currentSaved.indexOfFirst { it.id == frameToSave.id }
        if (existingIndex >= 0) {
            currentSaved[existingIndex] = frameToSave
        } else {
            currentSaved.add(frameToSave)
        }
        _uiState.update {
            it.copy(
                savedCustomFrames = currentSaved,
                selectedImageFrame = frameToSave,
                showCustomFrameDesigner = false
            )
        }
        renderFinalStrip()
    }

    fun deleteCustomFrame(frameId: String) {
        val currentSaved = _uiState.value.savedCustomFrames.filterNot { it.id == frameId }
        val fallbackFrame = PRESET_IMAGE_FRAMES[0]
        _uiState.update {
            it.copy(
                savedCustomFrames = currentSaved,
                selectedImageFrame = if (it.selectedImageFrame.id == frameId) fallbackFrame else it.selectedImageFrame
            )
        }
        renderFinalStrip()
    }

    private var renderStripJob: Job? = null

    fun renderFinalStrip(debounceMs: Long = 0L) {
        val photos = _uiState.value.capturedPhotos
        if (photos.isEmpty()) return

        renderStripJob?.cancel()
        renderStripJob = viewModelScope.launch {
            if (debounceMs > 0) {
                delay(debounceMs)
            }
            _uiState.update { it.copy(isRenderingStrip = true) }

            val layout = _uiState.value.selectedLayout
            val filter = _uiState.value.selectedFilter
            val frameColor = _uiState.value.selectedFrameColor
            val imageFrame = _uiState.value.selectedImageFrame
            val caption = _uiState.value.captionText
            val showDate = _uiState.value.showDate
            val sticker = _uiState.value.selectedSticker
            val eventTitle = _uiState.value.settings.eventTitle
            val quality = _uiState.value.settings.exportQuality
            val isCustomLayout = _uiState.value.isCustomLayoutActive
            val customConfig = _uiState.value.customLayoutConfig
            val scaleMode = _uiState.value.photoScaleMode
            val alignment = _uiState.value.photoAlignment
            val panX = _uiState.value.photoPanX
            val panY = _uiState.value.photoPanY
            val zoom = _uiState.value.photoZoom
            val slotBgColorLong = _uiState.value.slotBgColorHex
            val perSlotAdjustments = _uiState.value.perSlotAdjustments
            val useCustomCanvasImage = _uiState.value.useCustomCanvasImage
            val customCanvasImagePath = _uiState.value.customCanvasImagePath

            val rendered = withContext(Dispatchers.Default) {
                BitmapUtils.createPhotoStrip(
                    photos = photos,
                    layout = layout,
                    filterType = filter,
                    frameColorLong = frameColor.colorHex,
                    textColorLong = frameColor.textColorHex,
                    eventTitle = eventTitle,
                    captionText = caption,
                    showDate = showDate,
                    stickerEmoji = sticker,
                    quality = quality,
                    isCustomLayout = isCustomLayout,
                    customConfig = customConfig,
                    imageFrame = imageFrame,
                    scaleMode = scaleMode,
                    alignment = alignment,
                    panX = panX,
                    panY = panY,
                    zoom = zoom,
                    slotBgColorLong = slotBgColorLong,
                    perSlotAdjustments = perSlotAdjustments,
                    useCustomCanvasImage = useCustomCanvasImage,
                    customCanvasImagePath = customCanvasImagePath
                )
            }

            _uiState.update {
                it.copy(
                    finalStripBitmap = rendered,
                    isRenderingStrip = false
                )
            }
        }
    }

    fun updatePhotoScaleMode(mode: PhotoScaleMode) {
        _uiState.update { it.copy(photoScaleMode = mode) }
        renderFinalStrip(0L)
    }

    fun updatePhotoAlignment(alignment: PhotoAlignment) {
        _uiState.update { it.copy(photoAlignment = alignment) }
        renderFinalStrip(0L)
    }

    fun updatePhotoPan(panX: Float, panY: Float) {
        _uiState.update { it.copy(photoPanX = panX, photoPanY = panY) }
        renderFinalStrip(debounceMs = 250L)
    }

    fun updatePhotoZoom(zoom: Float) {
        _uiState.update { it.copy(photoZoom = zoom) }
        renderFinalStrip(debounceMs = 250L)
    }

    fun updateSlotBgColor(colorHex: Long) {
        _uiState.update { it.copy(slotBgColorHex = colorHex) }
        renderFinalStrip(0L)
    }

    fun updateSlotAdjustment(slotIndex: Int, adjustment: PhotoSlotAdjustment, debounceMs: Long = 250L) {
        val current = _uiState.value.perSlotAdjustments.toMutableMap()
        current[slotIndex] = adjustment
        _uiState.update { it.copy(perSlotAdjustments = current) }
        renderFinalStrip(debounceMs)
    }

    fun resetSlotAdjustments() {
        _uiState.update {
            it.copy(
                photoScaleMode = PhotoScaleMode.CROP_FILL,
                photoAlignment = PhotoAlignment.CENTER,
                photoPanX = 0f,
                photoPanY = 0f,
                photoZoom = 1.0f,
                slotBgColorHex = 0xFFFFFFFF,
                perSlotAdjustments = emptyMap()
            )
        }
        renderFinalStrip(0L)
    }

    fun retakeAll() {
        _uiState.update {
            it.copy(
                capturedPhotos = emptyList(),
                originalCapturedPhotos = emptyList(),
                currentSlotIndex = 0,
                retakeSlotIndex = null,
                finalStripBitmap = null,
                saveStatus = SaveStatus.Idle,
                currentScreen = AppScreen.CAMERA
            )
        }
    }

    fun resetToWelcome() {
        _uiState.update {
            it.copy(
                capturedPhotos = emptyList(),
                originalCapturedPhotos = emptyList(),
                currentSlotIndex = 0,
                retakeSlotIndex = null,
                finalStripBitmap = null,
                saveStatus = SaveStatus.Idle,
                gifSaveStatus = SaveStatus.Idle,
                currentScreen = AppScreen.WELCOME
            )
        }
    }

    fun saveToGallery(context: Context) {
        val bitmap = _uiState.value.finalStripBitmap ?: return
        if (_uiState.value.saveStatus is SaveStatus.Saving) return

        _uiState.update { it.copy(saveStatus = SaveStatus.Saving) }

        viewModelScope.launch {
            val format = _uiState.value.settings.outputFormat
            val result = withContext(Dispatchers.IO) {
                BitmapUtils.saveBitmapToGallery(context, bitmap, format)
            }

            if (result != null) {
                _uiState.update {
                    it.copy(saveStatus = SaveStatus.Success(fileName = result.first, filePath = result.second))
                }
            } else {
                _uiState.update {
                    it.copy(saveStatus = SaveStatus.Error("Gagal menyimpan photo strip ke galeri."))
                }
            }
        }
    }

    fun sharePhotoStrip(context: Context) {
        val bitmap = _uiState.value.finalStripBitmap ?: return

        viewModelScope.launch {
            val shareUri = withContext(Dispatchers.IO) {
                try {
                    val cachePath = File(context.cacheDir, "shared_photos").apply { if (!exists()) mkdirs() }
                    val file = File(cachePath, "photo_strip_${System.currentTimeMillis()}.png")
                    val stream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.flush()
                    stream.close()

                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            if (shareUri != null) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, shareUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Bagikan Photo Strip").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        }
    }

    fun shareToApp(context: Context, packageName: String) {
        val bitmap = _uiState.value.finalStripBitmap ?: return

        viewModelScope.launch {
            val shareUri = withContext(Dispatchers.IO) {
                try {
                    val cachePath = File(context.cacheDir, "shared_photos").apply { if (!exists()) mkdirs() }
                    val file = File(cachePath, "photo_strip_${System.currentTimeMillis()}.png")
                    val stream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.flush()
                    stream.close()

                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            if (shareUri != null) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, shareUri)
                    setPackage(packageName)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                try {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to general chooser if app is not installed
                    sharePhotoStrip(context)
                }
            }
        }
    }

    fun toggleLoopView(active: Boolean) {
        _uiState.update { it.copy(isLoopViewActive = active) }
    }

    fun setLoopMode(mode: LoopMode) {
        _uiState.update { it.copy(loopConfig = it.loopConfig.copy(mode = mode)) }
    }

    fun setLoopSpeed(speed: LoopSpeed) {
        _uiState.update { it.copy(loopConfig = it.loopConfig.copy(speed = speed)) }
    }

    fun setLoopTransition(transition: LoopTransition) {
        _uiState.update { it.copy(loopConfig = it.loopConfig.copy(transition = transition)) }
    }

    fun setLoopParticle(particle: LoopParticleEffect) {
        _uiState.update { it.copy(loopConfig = it.loopConfig.copy(particleEffect = particle)) }
    }

    fun toggleLoopPlaying() {
        _uiState.update { it.copy(loopConfig = it.loopConfig.copy(isPlaying = !it.loopConfig.isPlaying)) }
    }

    fun setShowFullscreenLoop(show: Boolean) {
        _uiState.update { it.copy(showFullscreenLoop = show) }
    }

    fun resetGifSaveStatus() {
        _uiState.update { it.copy(gifSaveStatus = SaveStatus.Idle) }
    }

    fun saveGifToGallery(context: Context) {
        val photos = _uiState.value.capturedPhotos
        if (photos.isEmpty() || _uiState.value.isGeneratingGif) return

        _uiState.update {
            it.copy(
                isGeneratingGif = true,
                gifSaveStatus = SaveStatus.Saving
            )
        }

        viewModelScope.launch {
            val filter = _uiState.value.selectedFilter
            val loopConfig = _uiState.value.loopConfig
            val frameColor = _uiState.value.selectedFrameColor
            val caption = _uiState.value.captionText
            val eventTitle = _uiState.value.settings.eventTitle
            val sticker = _uiState.value.selectedSticker

            val (resultStatus, resultToast) = withContext(Dispatchers.Default) {
                try {
                    val gifBytes = BitmapUtils.createAnimatedGifBytes(
                        photos = photos,
                        filterType = filter,
                        loopMode = loopConfig.mode,
                        speedMs = loopConfig.speed.delayMs.toInt(),
                        frameColorHex = frameColor.colorHex,
                        captionText = caption,
                        eventTitle = eventTitle,
                        stickerEmoji = sticker
                    )
                    if (gifBytes != null) {
                        val saved = BitmapUtils.saveGifToGallery(context, gifBytes)
                        if (saved != null) {
                            Pair(SaveStatus.Success(saved.first, saved.second), "GIF Animasi berhasil disimpan ke Galeri!")
                        } else {
                            Pair(SaveStatus.Error("Gagal menyimpan GIF ke Galeri"), "Gagal menyimpan GIF ke Galeri")
                        }
                    } else {
                        Pair(SaveStatus.Error("Gagal memproses frame GIF"), "Gagal memproses frame GIF")
                    }
                } catch (e: Exception) {
                    Pair(SaveStatus.Error(e.localizedMessage ?: "Error"), "Error: ${e.localizedMessage}")
                }
            }

            _uiState.update {
                it.copy(
                    isGeneratingGif = false,
                    gifSaveStatus = resultStatus
                )
            }
            Toast.makeText(context, resultToast, Toast.LENGTH_SHORT).show()
        }
    }

    fun shareGif(context: Context) {
        val photos = _uiState.value.capturedPhotos
        if (photos.isEmpty()) return

        viewModelScope.launch {
            Toast.makeText(context, "Menyiapkan GIF Animasi untuk dibagikan...", Toast.LENGTH_SHORT).show()

            val shareUri = withContext(Dispatchers.IO) {
                try {
                    val filter = _uiState.value.selectedFilter
                    val loopConfig = _uiState.value.loopConfig
                    val frameColor = _uiState.value.selectedFrameColor
                    val caption = _uiState.value.captionText
                    val eventTitle = _uiState.value.settings.eventTitle
                    val sticker = _uiState.value.selectedSticker

                    val gifBytes = BitmapUtils.createAnimatedGifBytes(
                        photos = photos,
                        filterType = filter,
                        loopMode = loopConfig.mode,
                        speedMs = loopConfig.speed.delayMs.toInt(),
                        frameColorHex = frameColor.colorHex,
                        captionText = caption,
                        eventTitle = eventTitle,
                        stickerEmoji = sticker
                    ) ?: return@withContext null

                    val cachePath = File(context.cacheDir, "shared_photos").apply { if (!exists()) mkdirs() }
                    val gifFile = File(cachePath, "booth_loop_${System.currentTimeMillis()}.gif")
                    java.io.FileOutputStream(gifFile).use { fos ->
                        fos.write(gifBytes)
                        fos.flush()
                    }

                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", gifFile)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            if (shareUri != null) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/gif"
                    putExtra(Intent.EXTRA_STREAM, shareUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Bagikan Loop Animasi GIF").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } else {
                Toast.makeText(context, "Gagal membagikan GIF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun resetMp4SaveStatus() {
        _uiState.update { it.copy(mp4SaveStatus = SaveStatus.Idle) }
    }

    fun saveMp4ToGallery(context: Context) {
        val photos = _uiState.value.capturedPhotos
        if (photos.isEmpty() || _uiState.value.isGeneratingMp4) return

        _uiState.update {
            it.copy(
                isGeneratingMp4 = true,
                mp4SaveStatus = SaveStatus.Saving
            )
        }

        viewModelScope.launch {
            val filter = _uiState.value.selectedFilter
            val loopConfig = _uiState.value.loopConfig
            val frameColor = _uiState.value.selectedFrameColor
            val caption = _uiState.value.captionText
            val eventTitle = _uiState.value.settings.eventTitle
            val sticker = _uiState.value.selectedSticker

            val (resultStatus, resultToast) = withContext(Dispatchers.Default) {
                try {
                    val mp4File = BitmapUtils.createAnimatedMp4File(
                        context = context,
                        photos = photos,
                        filterType = filter,
                        loopMode = loopConfig.mode,
                        speedMs = loopConfig.speed.delayMs.toInt(),
                        frameColorHex = frameColor.colorHex,
                        captionText = caption,
                        eventTitle = eventTitle,
                        stickerEmoji = sticker
                    )
                    if (mp4File != null) {
                        val saved = BitmapUtils.saveMp4ToGallery(context, mp4File)
                        if (saved != null) {
                            Pair(SaveStatus.Success(saved.first, saved.second), "Video MP4 berhasil disimpan ke Galeri!")
                        } else {
                            Pair(SaveStatus.Error("Gagal menyimpan MP4 ke Galeri"), "Gagal menyimpan MP4 ke Galeri")
                        }
                    } else {
                        Pair(SaveStatus.Error("Gagal merender video MP4"), "Gagal merender video MP4")
                    }
                } catch (e: Exception) {
                    Pair(SaveStatus.Error(e.localizedMessage ?: "Error"), "Error: ${e.localizedMessage}")
                }
            }

            _uiState.update {
                it.copy(
                    isGeneratingMp4 = false,
                    mp4SaveStatus = resultStatus
                )
            }
            Toast.makeText(context, resultToast, Toast.LENGTH_SHORT).show()
        }
    }

    fun shareMp4(context: Context) {
        val photos = _uiState.value.capturedPhotos
        if (photos.isEmpty()) return

        viewModelScope.launch {
            Toast.makeText(context, "Menyiapkan Video MP4 untuk dibagikan...", Toast.LENGTH_SHORT).show()

            val shareUri = withContext(Dispatchers.IO) {
                try {
                    val filter = _uiState.value.selectedFilter
                    val loopConfig = _uiState.value.loopConfig
                    val frameColor = _uiState.value.selectedFrameColor
                    val caption = _uiState.value.captionText
                    val eventTitle = _uiState.value.settings.eventTitle
                    val sticker = _uiState.value.selectedSticker

                    val mp4File = BitmapUtils.createAnimatedMp4File(
                        context = context,
                        photos = photos,
                        filterType = filter,
                        loopMode = loopConfig.mode,
                        speedMs = loopConfig.speed.delayMs.toInt(),
                        frameColorHex = frameColor.colorHex,
                        captionText = caption,
                        eventTitle = eventTitle,
                        stickerEmoji = sticker
                    ) ?: return@withContext null

                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", mp4File)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            if (shareUri != null) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "video/mp4"
                    putExtra(Intent.EXTRA_STREAM, shareUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Bagikan Loop Animasi MP4").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } else {
                Toast.makeText(context, "Gagal membagikan MP4", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun shareToSpecificApp(context: Context, packageName: String, shareType: String) {
        viewModelScope.launch {
            val shareUri = withContext(Dispatchers.IO) {
                try {
                    val cachePath = File(context.cacheDir, "shared_photos").apply { if (!exists()) mkdirs() }
                    when (shareType) {
                        "strip" -> {
                            val bitmap = _uiState.value.finalStripBitmap ?: return@withContext null
                            val file = File(cachePath, "photo_strip_${System.currentTimeMillis()}.png")
                            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        }
                        "gif" -> {
                            val photos = _uiState.value.capturedPhotos
                            if (photos.isEmpty()) return@withContext null
                            val gifBytes = BitmapUtils.createAnimatedGifBytes(
                                photos = photos,
                                filterType = _uiState.value.selectedFilter,
                                loopMode = _uiState.value.loopConfig.mode,
                                speedMs = _uiState.value.loopConfig.speed.delayMs.toInt(),
                                frameColorHex = _uiState.value.selectedFrameColor.colorHex,
                                captionText = _uiState.value.captionText,
                                eventTitle = _uiState.value.settings.eventTitle,
                                stickerEmoji = _uiState.value.selectedSticker
                            ) ?: return@withContext null
                            val file = File(cachePath, "booth_loop_${System.currentTimeMillis()}.gif")
                            file.writeBytes(gifBytes)
                            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        }
                        "mp4" -> {
                            val photos = _uiState.value.capturedPhotos
                            if (photos.isEmpty()) return@withContext null
                            val mp4File = BitmapUtils.createAnimatedMp4File(
                                context = context,
                                photos = photos,
                                filterType = _uiState.value.selectedFilter,
                                loopMode = _uiState.value.loopConfig.mode,
                                speedMs = _uiState.value.loopConfig.speed.delayMs.toInt(),
                                frameColorHex = _uiState.value.selectedFrameColor.colorHex,
                                captionText = _uiState.value.captionText,
                                eventTitle = _uiState.value.settings.eventTitle,
                                stickerEmoji = _uiState.value.selectedSticker
                            ) ?: return@withContext null
                            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", mp4File)
                        }
                        else -> null
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            if (shareUri != null) {
                val mimeType = when (shareType) {
                    "strip" -> "image/png"
                    "gif" -> "image/gif"
                    "mp4" -> "video/mp4"
                    else -> "*/*"
                }
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, shareUri)
                    if (packageName.isNotBlank()) setPackage(packageName)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                try {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    val chooserIntent = Intent(Intent.ACTION_SEND).apply {
                        type = mimeType
                        putExtra(Intent.EXTRA_STREAM, shareUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(chooserIntent, "Bagikan").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
            } else {
                Toast.makeText(context, "Gagal menyiapkan file untuk dibagikan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun printPhotoStrip(
        context: Context,
        copies: Int = _uiState.value.settings.defaultPrintCopies.coerceAtLeast(1),
        paperSize: com.example.model.PrintPaperSize = _uiState.value.settings.defaultPaperSize,
        duplicateDualStrip: Boolean = _uiState.value.settings.duplicateDualStrip
    ) {
        val bitmap = _uiState.value.finalStripBitmap ?: return
        try {
            Toast.makeText(context, "Memulai cetak $copies lembar photo strip...", Toast.LENGTH_SHORT).show()

            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            val jobName = "PhotoBooth_Print_${System.currentTimeMillis()}"

            val mediaSize = when (paperSize) {
                com.example.model.PrintPaperSize.PHOTO_4x6,
                com.example.model.PrintPaperSize.PHOTO_2x6_DUAL -> PrintAttributes.MediaSize.NA_INDEX_4X6
                com.example.model.PrintPaperSize.PAPER_A4 -> PrintAttributes.MediaSize.ISO_A4
                com.example.model.PrintPaperSize.LETTER -> PrintAttributes.MediaSize.NA_LETTER
            }

            printManager?.print(
                jobName,
                object : PrintDocumentAdapter() {
                    override fun onLayout(
                        oldAttributes: PrintAttributes?,
                        newAttributes: PrintAttributes?,
                        cancellationSignal: android.os.CancellationSignal?,
                        callback: LayoutResultCallback?,
                        extras: Bundle?
                    ) {
                        if (cancellationSignal?.isCanceled == true) {
                            callback?.onLayoutCancelled()
                            return
                        }
                        val pInfo = PrintDocumentInfo.Builder("photobooth_strip.pdf")
                            .setContentType(PrintDocumentInfo.CONTENT_TYPE_PHOTO)
                            .setPageCount(copies)
                            .build()
                        callback?.onLayoutFinished(pInfo, true)
                    }

                    override fun onWrite(
                        pages: Array<out PageRange>?,
                        destination: ParcelFileDescriptor?,
                        cancellationSignal: android.os.CancellationSignal?,
                        callback: WriteResultCallback?
                    ) {
                        try {
                            val pdfDocument = android.graphics.pdf.PdfDocument()
                            val marginPx = 20
                            val gapPx = 30

                            val pageWidth: Int
                            val pageHeight: Int

                            if (duplicateDualStrip || paperSize == com.example.model.PrintPaperSize.PHOTO_2x6_DUAL) {
                                pageWidth = bitmap.width * 2 + gapPx + (marginPx * 2)
                                pageHeight = bitmap.height + (marginPx * 2)
                            } else {
                                pageWidth = bitmap.width + (marginPx * 2)
                                pageHeight = bitmap.height + (marginPx * 2)
                            }

                            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

                            for (i in 1..copies) {
                                if (cancellationSignal?.isCanceled == true) {
                                    callback?.onWriteCancelled()
                                    pdfDocument.close()
                                    return
                                }

                                val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, i).create()
                                val page = pdfDocument.startPage(pageInfo)
                                val canvas = page.canvas

                                // Fill white paper background
                                canvas.drawColor(android.graphics.Color.WHITE)

                                if (duplicateDualStrip || paperSize == com.example.model.PrintPaperSize.PHOTO_2x6_DUAL) {
                                    // Strip 1 (Left)
                                    val left1 = marginPx.toFloat()
                                    val top1 = marginPx.toFloat()
                                    canvas.drawBitmap(bitmap, left1, top1, paint)

                                    // Cut Guide Line (Center Dashed)
                                    val lineX = left1 + bitmap.width + (gapPx / 2f)
                                    val linePaint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.LTGRAY
                                        strokeWidth = 3f
                                        style = android.graphics.Paint.Style.STROKE
                                        pathEffect = android.graphics.DashPathEffect(floatArrayOf(15f, 15f), 0f)
                                    }
                                    canvas.drawLine(lineX, 0f, lineX, pageHeight.toFloat(), linePaint)

                                    // Strip 2 (Right)
                                    val left2 = left1 + bitmap.width + gapPx
                                    canvas.drawBitmap(bitmap, left2, top1, paint)
                                } else {
                                    canvas.drawBitmap(bitmap, marginPx.toFloat(), marginPx.toFloat(), paint)
                                }

                                pdfDocument.finishPage(page)
                            }

                            destination?.fileDescriptor?.let { fd ->
                                FileOutputStream(fd).use { out ->
                                    pdfDocument.writeTo(out)
                                }
                            }
                            pdfDocument.close()
                            callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                        } catch (e: Exception) {
                            callback?.onWriteFailed(e.localizedMessage)
                        }
                    }
                },
                PrintAttributes.Builder()
                    .setMediaSize(mediaSize)
                    .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                    .build()
            )
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal mencetak: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
    fun resetSaveStatus() {
        _uiState.update { it.copy(saveStatus = SaveStatus.Idle) }
    }

    fun toggleQrDialog(show: Boolean) {
        _uiState.update { it.copy(showQrCodeDialog = show) }
        // Automatically upload to Cloudinary if opening dialog and not yet uploaded
        if (show && _uiState.value.finalStripBitmap != null && _uiState.value.uploadedCloudinaryUrl == null && _uiState.value.cloudinaryUploadState !is CloudinaryUploadState.Uploading) {
            uploadToCloudinary()
        }
    }

    fun toggleQrCodeDialog(show: Boolean) = toggleQrDialog(show)

    fun uploadToCloudinary() {
        val bitmap = _uiState.value.finalStripBitmap ?: return
        if (_uiState.value.cloudinaryUploadState is CloudinaryUploadState.Uploading) return

        _uiState.update { it.copy(cloudinaryUploadState = CloudinaryUploadState.Uploading) }

        viewModelScope.launch {
            val settings = _uiState.value.settings
            val result = CloudinaryService.uploadBitmap(
                bitmap = bitmap,
                folder = settings.cloudinaryFolder.ifBlank { "photobooth" },
                fileNamePrefix = "strip_${System.currentTimeMillis()}",
                cloudName = settings.cloudinaryCloudName.ifBlank { CloudinaryService.DEFAULT_CLOUD_NAME },
                apiKey = settings.cloudinaryApiKey.ifBlank { CloudinaryService.DEFAULT_API_KEY },
                apiSecret = settings.cloudinaryApiSecret.ifBlank { CloudinaryService.DEFAULT_API_SECRET }
            )

            when (result) {
                is CloudinaryResult.Success -> {
                    _uiState.update {
                        it.copy(
                            cloudinaryUploadState = CloudinaryUploadState.Success(
                                downloadUrl = result.secureUrl,
                                publicId = result.publicId
                            ),
                            uploadedCloudinaryUrl = result.secureUrl
                        )
                    }
                }
                is CloudinaryResult.Error -> {
                    _uiState.update {
                        it.copy(
                            cloudinaryUploadState = CloudinaryUploadState.Error(result.message)
                        )
                    }
                }
            }
        }
    }

    fun uploadCustomQrisBitmap(qrisBitmap: Bitmap, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            val settings = _uiState.value.settings
            val result = CloudinaryService.uploadBitmap(
                bitmap = qrisBitmap,
                folder = "qris_codes",
                fileNamePrefix = "qris_${System.currentTimeMillis()}",
                cloudName = settings.cloudinaryCloudName.ifBlank { CloudinaryService.DEFAULT_CLOUD_NAME },
                apiKey = settings.cloudinaryApiKey.ifBlank { CloudinaryService.DEFAULT_API_KEY },
                apiSecret = settings.cloudinaryApiSecret.ifBlank { CloudinaryService.DEFAULT_API_SECRET }
            )

            when (result) {
                is CloudinaryResult.Success -> {
                    settingsRepository.updateQrisPaymentUrl(result.secureUrl)
                    onComplete(result.secureUrl)
                }
                is CloudinaryResult.Error -> {
                    onComplete(null)
                }
            }
        }
    }

    fun resetCloudinaryStatus() {
        _uiState.update { it.copy(cloudinaryUploadState = CloudinaryUploadState.Idle) }
    }

    // --- LOOP ANIMATION MANAGER METHODS ---

    fun openLoopAnimationManager() {
        _uiState.update { it.copy(showLoopAnimationManager = true) }
    }

    fun closeLoopAnimationManager() {
        _uiState.update { it.copy(showLoopAnimationManager = false) }
    }

    fun selectLoopAnimation(item: LoopAnimationItem) {
        viewModelScope.launch {
            settingsRepository.updateActiveLoopAnimationId(item.id)
        }
    }

    fun importLoopMedia(uri: Uri) {
        viewModelScope.launch {
            loopRepository.importLoopMedia(
                sourceUri = uri,
                onSuccess = { newItem ->
                    viewModelScope.launch {
                        val updated = loopRepository.loadLoopAnimations()
                        _uiState.update { it.copy(savedLoopAnimations = updated) }
                        settingsRepository.updateActiveLoopAnimationId(newItem.id)
                    }
                },
                onError = { }
            )
        }
    }

    fun addUrlLoopMedia(url: String) {
        viewModelScope.launch {
            val newItem = loopRepository.addUrlLoopMedia(url)
            val updated = loopRepository.loadLoopAnimations()
            _uiState.update { it.copy(savedLoopAnimations = updated) }
            settingsRepository.updateActiveLoopAnimationId(newItem.id)
        }
    }

    fun deleteLoopAnimation(item: LoopAnimationItem) {
        viewModelScope.launch {
            loopRepository.deleteLoopAnimation(item)
            val updated = loopRepository.loadLoopAnimations()
            _uiState.update { it.copy(savedLoopAnimations = updated) }
            if (_uiState.value.settings.activeLoopAnimationId == item.id) {
                settingsRepository.updateActiveLoopAnimationId("preset_photo_reel")
            }
        }
    }

    fun updateLoopMotionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateLoopMotionEnabled(enabled)
            if (enabled && _uiState.value.settings.welcomeLoopPlacement == WelcomeLoopPlacement.NONE) {
                settingsRepository.updateWelcomeLoopPlacement(WelcomeLoopPlacement.BACKGROUND_FULL)
            }
        }
    }

    fun cycleWelcomeTheme() {
        val themes = com.example.model.WelcomeTheme.values()
        val currentIdx = themes.indexOf(_uiState.value.settings.welcomeTheme)
        val nextTheme = themes[(currentIdx + 1) % themes.size]
        viewModelScope.launch {
            settingsRepository.updateWelcomeTheme(nextTheme)
        }
    }

    fun updateWelcomeLoopPlacement(placement: WelcomeLoopPlacement) {
        viewModelScope.launch { settingsRepository.updateWelcomeLoopPlacement(placement) }
    }

    fun updateWelcomeMediaScale(scale: WelcomeMediaScale) {
        viewModelScope.launch { settingsRepository.updateWelcomeMediaScale(scale) }
    }

    fun updateWelcomeLoopBackgroundDim(dim: Float) {
        viewModelScope.launch { settingsRepository.updateWelcomeLoopBackgroundDim(dim) }
    }

    fun updateWelcomeLoopSoundMuted(muted: Boolean) {
        viewModelScope.launch { settingsRepository.updateIsWelcomeLoopSoundMuted(muted) }
    }

    fun updateAutoRotateLoopPlaylist(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateAutoRotateLoopPlaylist(enabled) }
    }

    private fun playBeepSound() {
        if (_uiState.value.settings.isShutterSoundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
            } catch (_: Exception) {}
        }
    }

    private fun playShutterSound() {
        if (_uiState.value.settings.isShutterSoundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
            } catch (_: Exception) {}
        }
    }
}
