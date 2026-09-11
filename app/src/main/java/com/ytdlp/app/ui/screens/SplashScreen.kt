package com.ytdlp.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ytdlp.app.R
import com.ytdlp.app.data.preferences.AppPreferences
import com.ytdlp.app.ui.theme.NovaAqua
import com.ytdlp.app.ui.theme.NovaAquaDeep
import com.ytdlp.app.ui.theme.NovaInk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    onNavigateNext: (destinationRoute: String) -> Unit
) {
    val context = LocalContext.current
    val scale = remember { Animatable(0.75f) }
    val alpha = remember { Animatable(0f) }
    val glowPulse = remember { Animatable(0.85f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.65f, stiffness = 380f)
        )
    }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
    }

    LaunchedEffect(Unit) {
        glowPulse.animateTo(
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    LaunchedEffect(Unit) {
        val prefs = AppPreferences(context)
        val isCompleted = prefs.onboardingCompleted.first()
        delay(1300)
        onNavigateNext(if (isCompleted) "home" else "permissions")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF070F1E),
                        Color(0xFF0C192E),
                        Color(0xFF060D19)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(glowPulse.value)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NovaAqua.copy(alpha = 0.28f),
                            NovaAquaDeep.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .shadow(24.dp, RoundedCornerShape(32.dp), ambientColor = NovaAqua, spotColor = NovaAquaDeep)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.22f),
                                Color.White.copy(alpha = 0.06f)
                            )
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.6f),
                                NovaAqua.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        ),
                        RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "NovaFetch Logo",
                    modifier = Modifier
                        .size(86.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "NovaFetch",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(NovaAqua, NovaAquaDeep)
                            )
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "PRO",
                        color = NovaInk,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Ultra Liquid Glass Engine",
                style = MaterialTheme.typography.labelMedium,
                color = NovaAqua,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = NovaAqua,
                strokeWidth = 2.5.dp
            )
        }
    }
}
