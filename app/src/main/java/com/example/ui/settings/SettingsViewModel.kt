package com.example.ui.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AppSettings
import com.example.model.ExportQuality
import com.example.model.OutputFormat
import com.example.util.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application.applicationContext)

    val settings: StateFlow<AppSettings> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    private val _isPinUnlocked = MutableStateFlow(false)
    val isPinUnlocked: StateFlow<Boolean> = _isPinUnlocked.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    fun verifyPin(enteredPin: String): Boolean {
        return if (enteredPin == settings.value.adminPin) {
            _isPinUnlocked.value = true
            _pinError.value = null
            true
        } else {
            _pinError.value = "PIN Salah! Masukkan PIN yang benar."
            false
        }
    }

    fun lockSettings() {
        _isPinUnlocked.value = false
        _pinError.value = null
    }

    fun clearPinError() {
        _pinError.value = null
    }

    fun updateCountdown(seconds: Int) {
        viewModelScope.launch { repository.updateCountdown(seconds) }
    }

    fun updateBurstDelay(seconds: Int) {
        viewModelScope.launch { repository.updateBurstDelay(seconds) }
    }

    fun updateShutterSound(enabled: Boolean) {
        viewModelScope.launch { repository.updateShutterSound(enabled) }
    }

    fun updateScreenFlash(enabled: Boolean) {
        viewModelScope.launch { repository.updateScreenFlash(enabled) }
    }

    fun updateDefaultCamera(useFront: Boolean) {
        viewModelScope.launch { repository.updateDefaultCamera(useFront) }
    }

    fun updateOutputFormat(format: OutputFormat) {
        viewModelScope.launch { repository.updateOutputFormat(format) }
    }

    fun updateExportQuality(quality: ExportQuality) {
        viewModelScope.launch { repository.updateExportQuality(quality) }
    }

    fun updateEventBranding(title: String, subtitle: String) {
        viewModelScope.launch { repository.updateEventBranding(title, subtitle) }
    }

    fun updateKioskMode(enabled: Boolean) {
        viewModelScope.launch { repository.updateKioskMode(enabled) }
    }

    fun updateAdminPin(newPin: String) {
        viewModelScope.launch { repository.updateAdminPin(newPin) }
    }

    fun updateCloudinaryConfig(
        cloudName: String,
        apiKey: String,
        apiSecret: String,
        folder: String
    ) {
        viewModelScope.launch {
            repository.updateCloudinaryConfig(cloudName, apiKey, apiSecret, folder)
        }
    }

    fun updateQrisPaymentUrl(qrisUrl: String) {
        viewModelScope.launch {
            repository.updateQrisPaymentUrl(qrisUrl)
        }
    }

    fun updateCustomQrisSettings(useCustom: Boolean, path: String) {
        viewModelScope.launch {
            repository.updateCustomQrisSettings(useCustom, path)
        }
    }

    fun saveCustomQrisImage(uri: android.net.Uri, onSaved: (String) -> Unit) {
        viewModelScope.launch {
            val path = repository.saveCustomQrisImage(uri)
            if (path.isNotBlank()) {
                onSaved(path)
            }
        }
    }

    fun updatePrintSettings(
        defaultCopies: Int,
        autoPrint: Boolean,
        paperSize: com.example.model.PrintPaperSize,
        duplicateDual: Boolean
    ) {
        viewModelScope.launch {
            repository.updatePrintSettings(defaultCopies, autoPrint, paperSize, duplicateDual)
        }
    }

    fun updateWelcomeText(title: String, tagline: String, description: String, buttonText: String) {
        viewModelScope.launch {
            repository.updateWelcomeText(title, tagline, description, buttonText)
        }
    }

    fun updateWelcomeTheme(theme: com.example.model.WelcomeTheme) {
        viewModelScope.launch {
            repository.updateWelcomeTheme(theme)
        }
    }

    fun updateWelcomeHeroStyle(heroStyle: com.example.model.WelcomeHeroStyle) {
        viewModelScope.launch {
            repository.updateWelcomeHeroStyle(heroStyle)
        }
    }

    fun updateWelcomeParticle(particle: com.example.model.WelcomeParticle) {
        viewModelScope.launch {
            repository.updateWelcomeParticle(particle)
        }
    }

    fun updateWelcomeToggles(showFeatures: Boolean, showTopBadge: Boolean, enablePulse: Boolean) {
        viewModelScope.launch {
            repository.updateWelcomeToggles(showFeatures, showTopBadge, enablePulse)
        }
    }

    fun resetWelcomeDefaults() {
        viewModelScope.launch {
            repository.resetWelcomeDefaults()
        }
    }

    fun updateAppPinned(pinned: Boolean) {
        viewModelScope.launch {
            repository.updateAppPinned(pinned)
        }
    }

    fun updateImmersiveFullscreen(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateImmersiveFullscreen(enabled)
        }
    }

    fun updatePreventExitWithPin(enabled: Boolean) {
        viewModelScope.launch {
            repository.updatePreventExitWithPin(enabled)
        }
    }

    fun updateActiveLoopAnimationId(id: String) {
        viewModelScope.launch {
            repository.updateActiveLoopAnimationId(id)
        }
    }

    fun updateWelcomeLoopPlacement(placement: com.example.model.WelcomeLoopPlacement) {
        viewModelScope.launch {
            repository.updateWelcomeLoopPlacement(placement)
        }
    }

    fun updateWelcomeLoopBackgroundDim(dim: Float) {
        viewModelScope.launch {
            repository.updateWelcomeLoopBackgroundDim(dim)
        }
    }

    fun updateIsWelcomeLoopSoundMuted(muted: Boolean) {
        viewModelScope.launch {
            repository.updateIsWelcomeLoopSoundMuted(muted)
        }
    }

    fun updateWelcomeMediaScale(scale: com.example.model.WelcomeMediaScale) {
        viewModelScope.launch {
            repository.updateWelcomeMediaScale(scale)
        }
    }

    fun updateAutoRotateLoopPlaylist(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateAutoRotateLoopPlaylist(enabled)
        }
    }

    fun updateCustomThemeBackgroundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateCustomThemeBackgroundEnabled(enabled)
        }
    }

    fun updateCustomThemeBackgroundDim(dim: Float) {
        viewModelScope.launch {
            repository.updateCustomThemeBackgroundDim(dim)
        }
    }

    fun updateCustomThemeBackgroundSettings(enabled: Boolean, dim: Float, path: String) {
        viewModelScope.launch {
            repository.updateCustomThemeBackgroundSettings(enabled, dim, path)
        }
    }

    fun updateGridColumnCount(count: Int) {
        viewModelScope.launch {
            repository.updateGridColumnCount(count)
        }
    }

    fun saveCustomThemeBackgroundImage(uri: Uri, onSaved: (String) -> Unit) {
        viewModelScope.launch {
            val savedPath = repository.saveCustomThemeBackgroundImage(uri)
            if (savedPath.isNotBlank()) {
                repository.updateCustomThemeBackgroundSettings(
                    enabled = true,
                    dim = settings.value.customThemeBackgroundDim,
                    path = savedPath
                )
                onSaved(savedPath)
            }
        }
    }
}
