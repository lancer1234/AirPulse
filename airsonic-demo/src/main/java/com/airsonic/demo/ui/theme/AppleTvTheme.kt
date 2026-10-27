// Copyright (c) 2026 Chunguang Wei / Enhanced with Apple Human Interface Guidelines
package com.airsonic.demo.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onPreviewKeyEvent

/**
 * Apple Human Interface Guidelines (iOS 18 / tvOS 18 Dark Mode System Tokens)
 * - Restraint over decoration: clean layered surfaces (#000000 -> #1C1C1E -> #2C2C2E) without noisy borders.
 * - Authentic Apple dark mode system colors and hairline separators.
 * - Critically damped spring physics for touch press and TV D-pad focus.
 */
object AppleTvTheme {
    // System Backgrounds (iOS / tvOS Dark Mode)
    val BackgroundDeep = Color(0xFF000000)
    val BackgroundTop = Color(0xFF0B0C10)
    val BackgroundBottom = Color(0xFF000000)

    // Grouped & Elevated Surfaces
    val GroupedSurface = Color(0xFF1C1C1E)          // secondarySystemGroupedBackground
    val ElevatedSurface = Color(0xFF2C2C2E)         // tertiarySystemGroupedBackground
    val ElevatedSurfaceActive = Color(0xFF3A3A3C)   // quaternarySystemFill
    val SegmentedTrack = Color(0xFF252528)          // UISegmentedControl track
    val SegmentedThumb = Color(0xFF48484A)          // UISegmentedControl selected thumb

    // Compatibility aliases
    val GlassSurface = GroupedSurface
    val GlassSurfaceActive = ElevatedSurface
    val GlassSurfaceElevated = ElevatedSurfaceActive
    val GlassBorder = Color(0x14FFFFFF)             // Subtle 8% specular edge
    val HairlineDivider = Color(0x38545458)         // Official iOS Dark Separator
    val GlassBorderFocused = Color(0xFF0A84FF)
    val GlassBorderFocusedAlt = Color(0xFFFFFFFF)

    // Apple System Accent Colors (Dark Mode)
    val Blue = Color(0xFF0A84FF)
    val Green = Color(0xFF30D158)
    val Orange = Color(0xFFFF9F0A)
    val Red = Color(0xFFFF453A)
    val Indigo = Color(0xFF5E5CE6)
    val Teal = Color(0xFF64D2FF)

    // Typography Hierarchy
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0x99EBEBF5)           // 60% label
    val TextTertiary = Color(0x4DEBEBF5)            // 30% label

    val bgGradient: Brush
        get() = Brush.verticalGradient(
            listOf(BackgroundTop, BackgroundDeep, BackgroundBottom)
        )
}

/**
 * Adaptive Apple surface supporting both Touch (iOS press feedback) and D-pad (tvOS focus ring).
 * tvOS 18 high-contrast focus styling: 2.5dp crisp focus ring, subtle lift scale (1.04x).
 */
@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun TvFocusableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    selected: Boolean = false,
    showRestingBorder: Boolean = false,
    focusedBorderColor: Color = Color.White,
    backgroundColor: Color = if (selected) AppleTvTheme.ElevatedSurface else Color.Transparent,
    pressedBackgroundColor: Color = AppleTvTheme.ElevatedSurfaceActive,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable BoxScope.(isFocused: Boolean) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.98f
            isFocused -> 1.04f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "apple_surface_scale"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isPressed -> pressedBackgroundColor
            isFocused -> AppleTvTheme.ElevatedSurfaceActive
            else -> backgroundColor
        },
        label = "apple_surface_bg"
    )

    val borderStroke: BorderStroke? = when {
        isFocused -> BorderStroke(2.5.dp, focusedBorderColor)
        selected && showRestingBorder -> BorderStroke(1.dp, AppleTvTheme.Blue.copy(alpha = 0.85f))
        showRestingBorder -> BorderStroke(0.5.dp, AppleTvTheme.GlassBorder)
        else -> null
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(bgColor)
            .then(
                if (borderStroke != null) Modifier.border(borderStroke, shape)
                else Modifier
            )
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
            .focusable(
                interactionSource = interactionSource,
                enabled = true
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .onPreviewKeyEvent { keyEvent ->
                if ((keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
                     keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_ENTER ||
                     keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_NUMPAD_ENTER)
                ) {
                    if (keyEvent.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) {
                        onClick()
                    }
                    true
                } else {
                    false
                }
            }
            .padding(contentPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        content(isFocused)
    }
}
