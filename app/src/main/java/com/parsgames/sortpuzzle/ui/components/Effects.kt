package com.parsgames.sortpuzzle.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.parsgames.sortpuzzle.BuildConfig
import com.parsgames.sortpuzzle.ui.theme.BallColors
import com.parsgames.sortpuzzle.ui.theme.Palette
import kotlin.math.sin
import kotlin.random.Random

private class Particle(
    val x: Float, val delay: Float, val speed: Float,
    val drift: Float, val spin: Float, val color: Color,
    val size: Float, val isStar: Boolean
)

/**
 * بارشِ کاغذرنگی هنگام حل شدن مرحله. ذره‌ها ترکیبی از لوزی و شمسه‌ی هشت‌پرند
 * تا با زبانِ بصریِ بقیه‌ی بازی یکی بمانند.
 */
@Composable
fun Confetti(
    play: Boolean,
    modifier: Modifier = Modifier,
    count: Int = 90
) {
    val progress = remember { Animatable(0f) }
    val particles = remember(count) {
        val rng = Random(11)
        List(count) {
            Particle(
                x = rng.nextFloat(),
                delay = rng.nextFloat() * 0.35f,
                speed = 0.75f + rng.nextFloat() * 0.6f,
                drift = (rng.nextFloat() - 0.5f) * 2.4f,
                spin = (rng.nextFloat() - 0.5f) * 14f,
                color = if (rng.nextFloat() < 0.25f) Palette.Zaferan else BallColors[rng.nextInt(BallColors.list.size)],
                size = 7f + rng.nextFloat() * 9f,
                isStar = rng.nextFloat() < 0.3f
            )
        }
    }

    LaunchedEffect(play) {
        if (play) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(2_600, easing = LinearEasing))
        }
    }

    if (!play) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = progress.value
        for (p in particles) {
            val local = ((t - p.delay) * p.speed).coerceIn(0f, 1f)
            if (local <= 0f) continue
            val y = -40f + local * (size.height + 120f)
            val x = p.x * size.width + sin(local * 6f + p.x * 10f) * 34f * p.drift
            val alpha = (1f - local).coerceIn(0f, 1f) * 0.95f

            rotate(degrees = local * 360f * p.spin, pivot = Offset(x, y)) {
                if (p.isStar) {
                    drawCircle(p.color.copy(alpha = alpha), radius = p.size * 0.45f, center = Offset(x, y))
                } else {
                    drawRect(
                        color = p.color.copy(alpha = alpha),
                        topLeft = Offset(x - p.size / 2f, y - p.size / 2f),
                        size = Size(p.size, p.size * 0.62f)
                    )
                }
            }
        }
    }
}

/**
 * بنر تبلیغاتی. اگر بازیکن تبلیغ‌ها را حذف کرده باشد اصلاً ساخته نمی‌شود،
 * پس هیچ درخواستِ شبکه‌ای هم فرستاده نمی‌شود.
 */
@Composable
fun BannerAd(show: Boolean, modifier: Modifier = Modifier) {
    if (!show) return
    Box(modifier = modifier.fillMaxWidth().height(52.dp)) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = BuildConfig.AD_UNIT_BANNER
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
