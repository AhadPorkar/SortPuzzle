package com.parsgames.sortpuzzle.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parsgames.sortpuzzle.R
import com.parsgames.sortpuzzle.ui.components.GirihBackground
import com.parsgames.sortpuzzle.ui.theme.Palette
import kotlin.math.cos
import kotlin.math.sin

/**
 * معرفیِ استودیوی سازنده.
 *
 * شمسه‌ی هشت‌پر ابتدا به‌صورت خط کشیده می‌شود، سپس با نور پر می‌شود و نام
 * استودیو از پایین بالا می‌آید. با یک ضربه می‌توان رد شد.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var finished by remember { mutableStateOf(false) }

    val draw = remember { Animatable(0f) }
    val fill = remember { Animatable(0f) }
    val spin = remember { Animatable(-22f) }
    var showText by remember { mutableStateOf(false) }

    fun finishOnce() {
        if (!finished) { finished = true; onFinished() }
    }

    LaunchedEffect(Unit) {
        spin.animateTo(0f, tween(1_400, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        draw.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        showText = true
        fill.animateTo(1f, tween(700))
        kotlinx.coroutines.delay(1_300)
        finishOnce()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { finishOnce() },
        contentAlignment = Alignment.Center
    ) {
        GirihBackground(intensity = 1.4f)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(148.dp)
                    .graphicsLayer { rotationZ = spin.value }
            ) {
                val c = Offset(size.width / 2f, size.height / 2f)
                val outer = size.minDimension * 0.46f
                val inner = outer * 0.44f

                val path = Path()
                val steps = 16
                val shown = (draw.value * steps).toInt().coerceAtLeast(1)
                for (k in 0..shown) {
                    val r = if (k % 2 == 0) outer else inner
                    val a = Math.toRadians((-90 + k * 22.5)).toFloat()
                    val p = Offset(c.x + r * cos(a), c.y + r * sin(a))
                    if (k == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                }
                if (draw.value >= 1f) path.close()

                if (fill.value > 0f) {
                    drawPath(
                        path = path,
                        brush = Brush.linearGradient(
                            listOf(
                                Palette.Firouzeh.copy(alpha = 0.30f * fill.value),
                                Palette.Mina.copy(alpha = 0.10f * fill.value)
                            )
                        )
                    )
                }
                drawPath(path, color = Palette.Firouzeh, style = Stroke(width = 3.2f))

                // شمسه‌ی درونی
                if (fill.value > 0.3f) {
                    val ip = Path()
                    for (k in 0..steps) {
                        val r = if (k % 2 == 0) outer * 0.42f else inner * 0.42f
                        val a = Math.toRadians((-90 + k * 22.5 + 22.5)).toFloat()
                        val p = Offset(c.x + r * cos(a), c.y + r * sin(a))
                        if (k == 0) ip.moveTo(p.x, p.y) else ip.lineTo(p.x, p.y)
                    }
                    ip.close()
                    drawPath(ip, color = Palette.Zaferan.copy(alpha = fill.value), style = Stroke(width = 2.2f))
                }
            }

            Spacer(Modifier.height(34.dp))

            AnimatedVisibility(
                visible = showText,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 2 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ارائه‌ای از",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Palette.SadafDim
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.studio_name),
                        style = MaterialTheme.typography.displayMedium.copy(fontSize = 30.sp),
                        color = Palette.Sadaf,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.studio_tagline),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Palette.Firouzeh
                    )
                }
            }
        }

        Text(
            text = "برای رد شدن ضربه بزنید",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.35f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        )
    }
}
