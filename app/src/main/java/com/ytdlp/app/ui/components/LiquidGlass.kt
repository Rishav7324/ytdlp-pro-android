package com.ytdlp.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Apple-inspired spring specs
val AppleSpringSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMediumLow
)

val AppleSmoothSpringSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMedium
)

/**
 * Ultra iOS-style Liquid Glass Modifier.
 * Implements frosted glass translucency, specular refraction borders,
 * and ambient depth shadows.
 */
@Composable
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    borderAlpha: Float = 0.45f,
    elevation: Dp = 10.dp,
    tintColor: Color? = null,
    isDark: Boolean = isSystemInDarkTheme()
): Modifier {
    val backgroundBrush = if (isDark) {
        val baseTop = tintColor?.copy(alpha = 0.22f) ?: Color(0xFF152A2C).copy(alpha = 0.72f)
        val baseBottom = Color(0xFF0C1718).copy(alpha = 0.88f)
        Brush.verticalGradient(listOf(baseTop, baseBottom))
    } else {
        val baseTop = tintColor?.copy(alpha = 0.15f) ?: Color.White.copy(alpha = 0.85f)
        val baseBottom = Color(0xFFE6F5F5).copy(alpha = 0.65f)
        Brush.verticalGradient(listOf(baseTop, baseBottom))
    }

    val borderBrush = Brush.verticalGradient(
        listOf(
            if (isDark) Color.White.copy(alpha = borderAlpha * 0.75f) else Color.White.copy(alpha = 0.95f),
            if (isDark) Color(0xFF5C9FA2).copy(alpha = borderAlpha * 0.35f) else Color.White.copy(alpha = 0.40f),
            if (isDark) Color.Transparent else Color.White.copy(alpha = 0.15f)
        )
    )

    val ambientGlow = if (isDark) {
        tintColor?.copy(alpha = 0.30f) ?: Color(0xFF5C9FA2).copy(alpha = 0.15f)
    } else {
        Color(0xFF5C9FA2).copy(alpha = 0.18f)
    }

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = if (isDark) Color.Black.copy(alpha = 0.6f) else ambientGlow,
            spotColor = ambientGlow
        )
        .background(backgroundBrush, shape)
        .border(1.dp, borderBrush, shape)
}

/**
 * Interactive Liquid Glass Card with spring physics on touch.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    tintColor: Color? = null,
    borderAlpha: Float = 0.45f,
    elevation: Dp = 10.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.965f else 1.0f,
        animationSpec = AppleSpringSpec,
        label = "liquid-glass-card-scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .liquidGlass(shape = shape, borderAlpha = borderAlpha, elevation = elevation, tintColor = tintColor)
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        content = content
    )
}

/**
 * Capsule / Pill for tags, chips, and quick action triggers.
 */
@Composable
fun LiquidGlassPill(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    isSelected: Boolean = false,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    elevation: Dp = 4.dp,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.92f else 1.0f,
        animationSpec = AppleSpringSpec,
        label = "pill-scale"
    )

    val shape = CircleShape
    val isDark = isSystemInDarkTheme()

    val bgBrush = if (isSelected) {
        Brush.horizontalGradient(
            listOf(
                selectedColor.copy(alpha = if (isDark) 0.85f else 0.90f),
                selectedColor.copy(alpha = if (isDark) 0.65f else 0.75f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                if (isDark) Color(0xFF1E3335).copy(alpha = 0.60f) else Color.White.copy(alpha = 0.80f),
                if (isDark) Color(0xFF142224).copy(alpha = 0.70f) else Color(0xFFE9F4F5).copy(alpha = 0.55f)
            )
        )
    }

    val borderBrush = Brush.verticalGradient(
        listOf(
            if (isSelected) Color.White.copy(alpha = 0.8f) else if (isDark) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.9f),
            if (isSelected) Color.White.copy(alpha = 0.2f) else if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.25f)
        )
    )

    Row(
        modifier = modifier
            .scale(scale)
            .shadow(elevation = elevation, shape = shape)
            .background(bgBrush, shape)
            .border(1.dp, borderBrush, shape)
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}
