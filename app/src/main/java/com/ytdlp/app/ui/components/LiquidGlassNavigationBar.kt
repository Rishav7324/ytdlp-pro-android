package com.ytdlp.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

@Composable
fun LiquidGlassNavigationBar(currentRoute: String?, onNavigate: (String) -> Unit, backdrop: Backdrop, modifier: Modifier = Modifier) {
    val items = listOf(
        GlassNavItem("home", "Home", Icons.Rounded.Home),
        GlassNavItem("browser", "Browser", Icons.Rounded.Language),
        GlassNavItem("queue", "Queue", Icons.Rounded.Download),
        GlassNavItem("library", "Library", Icons.Rounded.LibraryMusic),
        GlassNavItem("settings", "Settings", Icons.Rounded.Settings)
    )
    Row(
        modifier = modifier.fillMaxWidth().height(72.dp).padding(horizontal = 4.dp, vertical = 4.dp)
            .drawBackdrop(backdrop = backdrop, shape = { RoundedCornerShape(26.dp) }, effects = { vibrancy(); blur(10.dp.toPx()); lens(22.dp.toPx(), 26.dp.toPx(), chromaticAberration = true) }, onDrawSurface = { drawRect(Color.White.copy(alpha = .58f)) }),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) { items.forEach { item -> GlassNavButton(item, currentRoute == item.route) { onNavigate(item.route) } } }
}

@Composable
private fun RowScope.GlassNavButton(item: GlassNavItem, selected: Boolean, onClick: () -> Unit) {
    val scale = animateFloatAsState(if (selected) 1.2f else .94f, spring(dampingRatio = .62f, stiffness = 520f), label = "nav-${item.route}")
    Box(Modifier.weight(1f).height(62.dp).padding(horizontal = 3.dp).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(item.icon, item.label, tint = if (selected) Color(0xFF3F8F92) else Color(0xFF5F7475), modifier = Modifier.scale(scale.value))
            if (selected) Text(item.label, color = Color(0xFF3F8F92), style = MaterialTheme.typography.labelSmall)
        }
    }
}

private data class GlassNavItem(val route: String, val label: String, val icon: ImageVector)
