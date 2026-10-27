package com.example.ui.template

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CustomLayoutConfig
import com.example.model.PngSlotRect
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
import java.io.File

/**
 * Category types for Photo Booth layouts matching the requested visual model
 */
enum class ShowcaseLayoutType {
    TWO_BY_SIX,
    FOUR_BY_SIX,
    PAPER_A4
}

/**
 * Pro badge pinned on top-left of each layout card (Hidden per request)
 */
@Composable
fun ProBadge(modifier: Modifier = Modifier) {
    // Hidden
}

/**
 * Section Header with title on left and yellow "LAINNYA" on right
 */
@Composable
fun LayoutSectionHeader(
    title: String,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 0.3.sp
        )
        Text(
            text = "LAINNYA",
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFACC15),
            letterSpacing = 0.5.sp,
            modifier = Modifier
                .clickable { onSeeAllClick() }
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .testTag("section_see_all_$title")
        )
    }
}

/**
 * Simulated photo slot for mockup photo booth frames (Clean Empty Slot)
 */
@Composable
fun SimulatedPortraitPhoto(
    photoId: Int,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFFEC4899),
    cornerRadiusDp: Float = 6f
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadiusDp.dp))
            .background(Color(0xFF0F172A))
            .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(cornerRadiusDp.dp))
    )
}

/**
 * 2x6 Photo Strip Card: Promise (Pink Romance)
 */
@Composable
fun Card2x6Promise(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Base2x6FrameContainer(
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFF472B6), Color(0xFFFB7185), Color(0xFFFBCFE8))
                    )
                )
                .padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "I love you!" top sticker
                Text(
                    text = "♡ I Love You! ♡",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 3 Photo Cutouts
                for (i in 0..2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.5.dp, Color(0xFFFDE047), RoundedCornerShape(6.dp))
                    ) {
                        SimulatedPortraitPhoto(photoId = i, modifier = Modifier.fillMaxSize())
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Promise Cursive Footer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("💌", fontSize = 9.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Promise",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF9D174D),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("✨", fontSize = 8.sp)
                }
            }
        }
    }
}

/**
 * 2x6 Photo Strip Card: SILVER HARMONY (Film Luxury)
 */
@Composable
fun Card2x6SilverHarmony(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Base2x6FrameContainer(
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF111215))
                .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(16.dp))
                .padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 4 Photo Cutouts stacked
                for (i in 0..3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                    ) {
                        SimulatedPortraitPhoto(
                            photoId = i + 2,
                            modifier = Modifier.fillMaxSize(),
                            accentColor = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // SILVER HARMONY text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SILVER",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = Color(0xFFE2E8F0)
                    )
                    Text(
                        text = "HARMONY",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

/**
 * 2x6 Photo Strip Card: Forever Yours (Lavender Glow)
 */
@Composable
fun Card2x6ForeverYours(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Base2x6FrameContainer(
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF818CF8), Color(0xFFA855F7), Color(0xFFC084FC))
                    )
                )
                .padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 3 Photo Cutouts
                for (i in 0..2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.5.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                    ) {
                        SimulatedPortraitPhoto(photoId = i + 1, modifier = Modifier.fillMaxSize())
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Forever Yours Cursive
                Text(
                    text = "Forever Yours",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "✨ 💫 ✨",
                    fontSize = 6.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * 2x6 Photo Strip Card: LOVE STORIES (Retro Comic Pop)
 */
@Composable
fun Card2x6LoveStories(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Base2x6FrameContainer(
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF38BDF8), Color(0xFF818CF8), Color(0xFFEC4899))
                    )
                )
                .padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Slot 1
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.5.dp, Color(0xFFF43F5E), RoundedCornerShape(6.dp))
                ) {
                    SimulatedPortraitPhoto(photoId = 0, modifier = Modifier.fillMaxSize())
                }

                Spacer(modifier = Modifier.height(3.dp))

                // LOVE STORIES Banner
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFDE047))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "LOVE STORIES ♡",
                        fontSize = 6.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF831843)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Slot 2
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(6.dp))
                ) {
                    SimulatedPortraitPhoto(photoId = 3, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

/**
 * 2x6 Photo Strip Card: Rainbow Rush (Playful Easter)
 */
@Composable
fun Card2x6RainbowRush(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Base2x6FrameContainer(
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A), Color(0xFFFCD34D))
                    )
                )
                .padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (i in 0..3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 1.5.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .border(1.2.dp, Color(0xFFFB7185), RoundedCornerShape(5.dp))
                    ) {
                        SimulatedPortraitPhoto(photoId = i + 4, modifier = Modifier.fillMaxSize())
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Rainbow Rush 🌈",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFB45309)
                )
            }
        }
    }
}

/**
 * 4x6 Postcard Card: Happy Birthday Jose Andrian
 */
@Composable
fun Card4x6JoseAndrian(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Base4x6FrameContainer(
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFE0F2FE), Color(0xFFF0FDF4), Color(0xFFFEF3C7))
                    )
                )
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 2x2 Grid of Photos
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                        ) {
                            SimulatedPortraitPhoto(photoId = 1, modifier = Modifier.fillMaxSize())
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                        ) {
                            SimulatedPortraitPhoto(photoId = 2, modifier = Modifier.fillMaxSize())
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                        ) {
                            SimulatedPortraitPhoto(photoId = 3, modifier = Modifier.fillMaxSize())
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                        ) {
                            SimulatedPortraitPhoto(photoId = 4, modifier = Modifier.fillMaxSize())
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Birthday Typography
                Text(
                    text = "Happy Birthday",
                    fontSize = 7.sp,
                    color = Color(0xFFEF4444),
                    fontStyle = FontStyle.Italic
                )
                Text(
                    text = "Jose Andrian",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFF0369A1)
                )
                Text(
                    text = "⭐ 12-01-2026 ⭐",
                    fontSize = 6.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
        }
    }
}

/**
 * 4x6 Postcard Card: Happy Birthday Jessica Johnson
 */
@Composable
fun Card4x6JessicaJohnson(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Base4x6FrameContainer(
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFCE7F3), Color(0xFFEDE9FE), Color(0xFFF5D0FE))
                    )
                )
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 2x2 Grid of Photos
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                        ) {
                            SimulatedPortraitPhoto(photoId = 0, modifier = Modifier.fillMaxSize())
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                        ) {
                            SimulatedPortraitPhoto(photoId = 3, modifier = Modifier.fillMaxSize())
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                        ) {
                            SimulatedPortraitPhoto(photoId = 5, modifier = Modifier.fillMaxSize())
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                        ) {
                            SimulatedPortraitPhoto(photoId = 1, modifier = Modifier.fillMaxSize())
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Birthday Typography
                Text(
                    text = "Happy Birthday",
                    fontSize = 7.sp,
                    color = Color(0xFFEC4899),
                    fontStyle = FontStyle.Italic
                )
                Text(
                    text = "Jessica Johnson",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFF4338CA)
                )
                Text(
                    text = "🌸 12-01-2026 🌸",
                    fontSize = 6.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B21A8)
                )
            }
        }
    }
}

/**
 * 4x6 Postcard Card: Happy Birthday Rainbow & Cat
 */
@Composable
fun Card4x6RainbowCat(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Base4x6FrameContainer(
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFEF9C3), Color(0xFFFED7AA), Color(0xFFFBCFE8))
                    )
                )
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Photo Collage (top big, bottom 2)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                    ) {
                        SimulatedPortraitPhoto(photoId = 2, modifier = Modifier.fillMaxSize())
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                    ) {
                        SimulatedPortraitPhoto(photoId = 4, modifier = Modifier.fillMaxSize())
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🐱 🌈", fontSize = 11.sp)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Happy Birthday",
                            fontSize = 6.5.sp,
                            color = Color(0xFFEA580C)
                        )
                        Text(
                            text = "Sweet Party ✨",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC2410C)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Base frame container for A4 layout templates
 */
@Composable
fun BaseA4FrameContainer(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1.0f,
        animationSpec = tween(180),
        label = "scale"
    )

    Card(
        modifier = modifier
            .width(184.dp)
            .height(260.dp)
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = if (isSelected) {
            BorderStroke(
                2.5.dp,
                Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFF43F5E), Color(0xFF06B6D4)))
            )
        } else {
            null
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF59E0B))
                        .size(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Terpilih",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * A4 Poster Card: 3x3 Photo Grid (9-Grid Memory)
 */
@Composable
fun CardA4Grid9(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BaseA4FrameContainer(
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E1B4B), Color(0xFF311042), Color(0xFF1E1B4B))
                    )
                )
                .padding(6.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "✧ MEMORY COLLAGE ✧",
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFDE047),
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 3x3 grid of photo slots
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    for (row in 0..2) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            for (col in 0..2) {
                                val index = row * 3 + col
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(3.dp))
                                        .border(0.5.dp, Color(0x66FFFFFF), RoundedCornerShape(3.dp))
                                ) {
                                    SimulatedPortraitPhoto(photoId = index, modifier = Modifier.fillMaxSize())
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "THE BEST OF TIMES",
                        fontSize = 5.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "A4 STUDIO POSTER",
                        fontSize = 5.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA78BFA)
                    )
                }
            }
        }
    }
}

/**
 * A4 Layout Card: Classic Double-Strip
 */
@Composable
fun CardA4ClassicDouble(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BaseA4FrameContainer(
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                    )
                )
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Left strip (3 photos)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LUCKY ME ✌️",
                        fontSize = 5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    for (i in 0..2) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(vertical = 1.dp)
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            SimulatedPortraitPhoto(photoId = i, modifier = Modifier.fillMaxSize())
                        }
                    }
                }

                // Right strip (3 photos)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LUCKY YOU ✨",
                        fontSize = 5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    for (i in 0..2) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(vertical = 1.dp)
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            SimulatedPortraitPhoto(photoId = i + 3, modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}

/**
 * A4 Layout Card: Mosaic Memory Panel
 */
@Composable
fun CardA4MosaicMemory(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BaseA4FrameContainer(
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
                    )
                )
                .padding(6.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top section: Row of 2 photos
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                    ) {
                        SimulatedPortraitPhoto(photoId = 1, modifier = Modifier.fillMaxSize())
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                    ) {
                        SimulatedPortraitPhoto(photoId = 4, modifier = Modifier.fillMaxSize())
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom section: 1 huge landscape photo or row of 2 photos
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                    ) {
                        SimulatedPortraitPhoto(photoId = 3, modifier = Modifier.fillMaxSize())
                    }
                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                    ) {
                        SimulatedPortraitPhoto(photoId = 5, modifier = Modifier.fillMaxSize())
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Brand line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🌸 CUTE MOSAIC 🌸", fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("A4 MEMORIES", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}

/**
 * Base frame container for 2x6 photo strips
 */
@Composable
fun Base2x6FrameContainer(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1.0f,
        animationSpec = tween(180),
        label = "scale"
    )

    Card(
        modifier = modifier
            .width(116.dp)
            .height(282.dp)
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = if (isSelected) {
            BorderStroke(
                2.5.dp,
                Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFF43F5E), Color(0xFF06B6D4)))
            )
        } else {
            null
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF59E0B))
                        .size(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Terpilih",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Base frame container for 4x6 postcards
 */
@Composable
fun Base4x6FrameContainer(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1.0f,
        animationSpec = tween(180),
        label = "scale"
    )

    Card(
        modifier = modifier
            .width(178.dp)
            .height(262.dp)
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = if (isSelected) {
            BorderStroke(
                2.5.dp,
                Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFF43F5E), Color(0xFF06B6D4)))
            )
        } else {
            null
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF59E0B))
                        .size(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Terpilih",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Card displaying an externally imported PNG layout inside the 2x6 carousel
 */
@Composable
fun ExternalPng2x6Card(
    customConfig: CustomLayoutConfig,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Base2x6FrameContainer(
        isSelected = isSelected,
        onClick = onSelect,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // Cutout Slots Preview underneath
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val containerW = maxWidth
                val containerH = maxHeight

                customConfig.pngSlots.forEachIndexed { idx, slot ->
                    val slotLeft = containerW * slot.left
                    val slotTop = containerH * slot.top
                    val slotW = containerW * (slot.right - slot.left)
                    val slotH = containerH * (slot.bottom - slot.top)

                    Box(
                        modifier = Modifier
                            .offset(x = slotLeft, y = slotTop)
                            .size(width = slotW, height = slotH)
                            .clip(RoundedCornerShape(customConfig.cornerRadiusDp.coerceIn(0f, 8f).dp))
                    ) {
                        SimulatedPortraitPhoto(photoId = idx, modifier = Modifier.fillMaxSize())
                    }
                }

                // Overlay PNG
                if (customConfig.pngOverlayPath.isNotBlank()) {
                    val file = File(customConfig.pngOverlayPath)
                    if (file.exists()) {
                        AsyncImage(
                            model = file,
                            contentDescription = customConfig.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card displaying an externally imported PNG layout inside the A4 carousel
 */
@Composable
fun ExternalPngA4Card(
    customConfig: CustomLayoutConfig,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    BaseA4FrameContainer(
        isSelected = isSelected,
        onClick = onSelect,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // Cutout Slots Preview underneath
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val containerW = maxWidth
                val containerH = maxHeight

                customConfig.pngSlots.forEachIndexed { idx, slot ->
                    val slotLeft = containerW * slot.left
                    val slotTop = containerH * slot.top
                    val slotW = containerW * (slot.right - slot.left)
                    val slotH = containerH * (slot.bottom - slot.top)

                    Box(
                        modifier = Modifier
                            .offset(x = slotLeft, y = slotTop)
                            .size(width = slotW, height = slotH)
                            .clip(RoundedCornerShape(customConfig.cornerRadiusDp.coerceIn(0f, 8f).dp))
                    ) {
                        SimulatedPortraitPhoto(photoId = idx, modifier = Modifier.fillMaxSize())
                    }
                }

                // Overlay PNG
                if (customConfig.pngOverlayPath.isNotBlank()) {
                    val file = File(customConfig.pngOverlayPath)
                    if (file.exists()) {
                        AsyncImage(
                            model = file,
                            contentDescription = customConfig.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card displaying an externally imported PNG layout inside the 4x6 carousel
 */
@Composable
fun ExternalPng4x6Card(
    customConfig: CustomLayoutConfig,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Base4x6FrameContainer(
        isSelected = isSelected,
        onClick = onSelect,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // Cutout Slots Preview underneath
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val containerW = maxWidth
                val containerH = maxHeight

                customConfig.pngSlots.forEachIndexed { idx, slot ->
                    val slotLeft = containerW * slot.left
                    val slotTop = containerH * slot.top
                    val slotW = containerW * (slot.right - slot.left)
                    val slotH = containerH * (slot.bottom - slot.top)

                    Box(
                        modifier = Modifier
                            .offset(x = slotLeft, y = slotTop)
                            .size(width = slotW, height = slotH)
                            .clip(RoundedCornerShape(customConfig.cornerRadiusDp.coerceIn(0f, 8f).dp))
                    ) {
                        SimulatedPortraitPhoto(photoId = idx, modifier = Modifier.fillMaxSize())
                    }
                }

                // Overlay PNG
                if (customConfig.pngOverlayPath.isNotBlank()) {
                    val file = File(customConfig.pngOverlayPath)
                    if (file.exists()) {
                        AsyncImage(
                            model = file,
                            contentDescription = customConfig.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }
            }
        }
    }
}

/**
 * Add New PNG card pinned at the end of 2x6 or 4x6 carousel
 */
@Composable
fun AddNewFrameCard(
    type: ShowcaseLayoutType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val width = when (type) {
        ShowcaseLayoutType.TWO_BY_SIX -> 116.dp
        ShowcaseLayoutType.FOUR_BY_SIX -> 178.dp
        ShowcaseLayoutType.PAPER_A4 -> 184.dp
    }
    val height = when (type) {
        ShowcaseLayoutType.TWO_BY_SIX -> 282.dp
        ShowcaseLayoutType.FOUR_BY_SIX -> 262.dp
        ShowcaseLayoutType.PAPER_A4 -> 260.dp
    }

    Card(
        modifier = modifier
            .width(width)
            .height(height)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131826)),
        border = BorderStroke(
            1.2.dp,
            Brush.linearGradient(listOf(Color(0xFFF59E0B).copy(alpha = 0.5f), Color(0xFFEC4899).copy(alpha = 0.5f)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFF59E0B).copy(alpha = 0.35f), StudioNeonPink.copy(alpha = 0.35f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val labelText = when (type) {
                ShowcaseLayoutType.TWO_BY_SIX -> "+ Tambah\nFrame 2x6"
                ShowcaseLayoutType.FOUR_BY_SIX -> "+ Tambah\nFrame 4x6"
                ShowcaseLayoutType.PAPER_A4 -> "+ Tambah\nFrame A4"
            }

            Text(
                text = labelText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = StudioTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Import file PNG transparan",
                fontSize = 9.sp,
                color = StudioTextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Modal Dialog displayed when clicking "LAINNYA" on either Layout 2x6 or Layout 4x6
 */
@Composable
fun LayoutLainnyaDialog(
    title: String,
    type: ShowcaseLayoutType,
    externalLayouts: List<CustomLayoutConfig>,
    activeConfigId: String,
    onDismiss: () -> Unit,
    onImportNew: () -> Unit,
    onSelectLayout: (CustomLayoutConfig) -> Unit,
    onEditLayout: (CustomLayoutConfig) -> Unit,
    onDeleteLayout: (CustomLayoutConfig) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(searchQuery, externalLayouts) {
        if (searchQuery.isBlank()) externalLayouts
        else externalLayouts.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary
                    )
                    Text(
                        text = "Semua koleksi template & frame PNG eksternal",
                        fontSize = 11.sp,
                        color = StudioTextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
            ) {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Cari layout...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = StudioTextSecondary, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF59E0B),
                        unfocusedBorderColor = Color(0x33FFFFFF)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Import Button inside Dialog
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onImportNew()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (type == ShowcaseLayoutType.TWO_BY_SIX) "+ Import Frame PNG 2x6" else "+ Import Frame PNG 4x6",
                        color = Color(0xFFF59E0B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Grid of items
                val cols = if (type == ShowcaseLayoutType.TWO_BY_SIX) 3 else 2
                LazyVerticalGrid(
                    columns = GridCells.Fixed(cols),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(filteredList, key = { it.id }) { config ->
                        val isSelected = (config.id == activeConfigId)
                        when (type) {
                            ShowcaseLayoutType.TWO_BY_SIX -> {
                                ExternalPng2x6Card(
                                    customConfig = config,
                                    isSelected = isSelected,
                                    onSelect = {
                                        onSelectLayout(config)
                                        onDismiss()
                                    },
                                    onEdit = {
                                        onDismiss()
                                        onEditLayout(config)
                                    },
                                    onDelete = { onDeleteLayout(config) }
                                )
                            }
                            ShowcaseLayoutType.FOUR_BY_SIX -> {
                                ExternalPng4x6Card(
                                    customConfig = config,
                                    isSelected = isSelected,
                                    onSelect = {
                                        onSelectLayout(config)
                                        onDismiss()
                                    },
                                    onEdit = {
                                        onDismiss()
                                        onEditLayout(config)
                                    },
                                    onDelete = { onDeleteLayout(config) }
                                )
                            }
                            ShowcaseLayoutType.PAPER_A4 -> {
                                ExternalPngA4Card(
                                    customConfig = config,
                                    isSelected = isSelected,
                                    onSelect = {
                                        onSelectLayout(config)
                                        onDismiss()
                                    },
                                    onEdit = {
                                        onDismiss()
                                        onEditLayout(config)
                                    },
                                    onDelete = { onDeleteLayout(config) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = StudioVioletLight, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF0F1524),
        shape = RoundedCornerShape(20.dp)
    )
}

val PRESET_PROMISE = CustomLayoutConfig(
    id = "preset_2x6_promise",
    name = "Promise",
    photoCount = 3,
    overlayAspectRatio = 3.0f,
    isPngOverlay = false
)

val PRESET_SILVER_HARMONY = CustomLayoutConfig(
    id = "preset_2x6_silver_harmony",
    name = "Silver Harmony",
    photoCount = 4,
    overlayAspectRatio = 3.0f,
    isPngOverlay = false
)

val PRESET_FOREVER_YOURS = CustomLayoutConfig(
    id = "preset_2x6_forever_yours",
    name = "Forever Yours",
    photoCount = 3,
    overlayAspectRatio = 3.0f,
    isPngOverlay = false
)

val PRESET_LOVE_STORIES = CustomLayoutConfig(
    id = "preset_2x6_love_stories",
    name = "Love Stories",
    photoCount = 2,
    overlayAspectRatio = 3.0f,
    isPngOverlay = false
)

val PRESET_RAINBOW_RUSH = CustomLayoutConfig(
    id = "preset_2x6_rainbow_rush",
    name = "Rainbow Rush",
    photoCount = 4,
    overlayAspectRatio = 3.0f,
    isPngOverlay = false
)

val PRESET_JOSE_ANDRIAN = CustomLayoutConfig(
    id = "preset_4x6_jose_andrian",
    name = "Jose Andrian",
    photoCount = 4,
    overlayAspectRatio = 1.5f,
    isPngOverlay = false
)

val PRESET_JESSICA_JOHNSON = CustomLayoutConfig(
    id = "preset_4x6_jessica_johnson",
    name = "Jessica Johnson",
    photoCount = 4,
    overlayAspectRatio = 1.5f,
    isPngOverlay = false
)

val PRESET_RAINBOW_CAT = CustomLayoutConfig(
    id = "preset_4x6_rainbow_cat",
    name = "Rainbow Cat",
    photoCount = 2,
    overlayAspectRatio = 1.5f,
    isPngOverlay = false
)

val PRESET_A4_GRID_9 = CustomLayoutConfig(
    id = "preset_a4_grid_9",
    name = "Memory Collage 9-Grid",
    photoCount = 9,
    columns = 3,
    photoAspectRatio = 0.75f,
    overlayAspectRatio = 1.414f,
    isPngOverlay = false,
    eventTitle = "9-GRID COLLAGE",
    captionText = "A4 MEMORY COLLAGE ✧"
)

val PRESET_A4_DOUBLE_STRIP = CustomLayoutConfig(
    id = "preset_a4_double_strip",
    name = "Classic Double-Strip",
    photoCount = 6,
    columns = 2,
    photoAspectRatio = 0.75f,
    overlayAspectRatio = 1.414f,
    isPngOverlay = false,
    eventTitle = "DOUBLE STRIP",
    captionText = "LUCKY TIMES ✨"
)

val PRESET_A4_MOSAIC = CustomLayoutConfig(
    id = "preset_a4_mosaic",
    name = "Mosaic Memory Panel",
    photoCount = 4,
    columns = 2,
    photoAspectRatio = 0.75f,
    overlayAspectRatio = 1.414f,
    isPngOverlay = false,
    eventTitle = "CUTE MOSAIC",
    captionText = "A4 MOSAIC MEMORIES 🌸"
)


