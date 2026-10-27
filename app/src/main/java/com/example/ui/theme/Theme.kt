package com.example.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.R
import java.io.File

private val LightColorScheme = lightColorScheme(
    primary = StudioViolet,
    secondary = StudioVioletLight,
    tertiary = StudioNeonPink,
    background = StudioBackground,
    surface = StudioSurface,
    surfaceVariant = StudioSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = StudioTextPrimary,
    onSurface = StudioTextPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = StudioViolet,
    secondary = StudioVioletLight,
    tertiary = StudioNeonPink,
    background = StudioBackground,
    surface = StudioSurface,
    surfaceVariant = StudioSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = StudioTextPrimary,
    onSurface = StudioTextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek Modern Studio dark mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

/**
 * Global App Theme Background displaying the user's uploaded wallpaper with an elegant,
 * contrast-enhancing dark gradient scrim for optimal readability, button clarity, and visual aesthetics.
 */
@Composable
fun AppThemeBackground(
    useThemeBackground: Boolean = true,
    dimAlpha: Float = 0.25f,
    customImagePath: String = "",
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackground)
    ) {
        if (useThemeBackground) {
            val contentScale = ContentScale.Crop
            if (customImagePath.isNotBlank()) {
                val file = File(customImagePath)
                if (file.exists()) {
                    AsyncImage(
                        model = file,
                        contentDescription = "UI Theme Wallpaper Background",
                        contentScale = contentScale,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_theme_bg),
                        contentDescription = "UI Theme Wallpaper Background",
                        contentScale = contentScale,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Image(
                    painter = painterResource(id = R.drawable.img_theme_bg),
                    contentDescription = "UI Theme Wallpaper Background",
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Full screen gradient scrim: subtle vignette allowing the full artwork to shine,
            // with gentle bottom depth for UI controls and buttons
            if (dimAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0A0F1D).copy(alpha = (dimAlpha * 0.35f).coerceIn(0f, 1f)),
                                    Color.Transparent,
                                    Color(0xFF0F172A).copy(alpha = (dimAlpha * 0.45f).coerceIn(0f, 1f)),
                                    Color(0xFF0A0F1D).copy(alpha = (dimAlpha * 0.90f).coerceIn(0f, 1f))
                                )
                            )
                        )
                )
            }
        }

        content()
    }
}

