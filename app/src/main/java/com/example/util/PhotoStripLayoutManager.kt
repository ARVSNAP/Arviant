package com.example.util

import androidx.compose.ui.graphics.Color

enum class StripOrientation {
    VERTICAL,
    HORIZONTAL,
    GRID
}

data class StripTemplate(
    val id: String,
    val name: String,
    val orientation: StripOrientation,
    val photoCount: Int,
    val backgroundColor: Color,
    val framePaddingDp: Int,
    val spacingDp: Int
)

object PhotoStripLayoutManager {
    val defaultTemplates = listOf(
        StripTemplate(
            id = "classic_vertical_3",
            name = "Classic 3-Strip Vertical",
            orientation = StripOrientation.VERTICAL,
            photoCount = 3,
            backgroundColor = Color.White,
            framePaddingDp = 16,
            spacingDp = 12
        ),
        StripTemplate(
            id = "modern_grid_4",
            name = "Modern Quad Grid",
            orientation = StripOrientation.GRID,
            photoCount = 4,
            backgroundColor = Color(0xFF1E1E2C),
            framePaddingDp = 20,
            spacingDp = 16
        ),
        StripTemplate(
            id = "elegant_horizontal_3",
            name = "Landscape Trio",
            orientation = StripOrientation.HORIZONTAL,
            photoCount = 3,
            backgroundColor = Color(0xFFF9F6F0),
            framePaddingDp = 24,
            spacingDp = 16
        )
    )

    fun getTemplate(id: String): StripTemplate {
        return defaultTemplates.firstOrNull { it.id == id } ?: defaultTemplates.first()
    }
}
