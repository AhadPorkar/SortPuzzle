package com.parsgames.sortpuzzle.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.parsgames.sortpuzzle.ui.theme.GameTheme
import com.parsgames.sortpuzzle.ui.theme.LocalGameTheme
import com.parsgames.sortpuzzle.ui.theme.Palette
import kotlin.math.cos
import kotlin.math.sin

/**
 * پس‌زمینه‌ی امضایِ بازی: شبکه‌ی گره‌چینیِ ایرانی (گره‌ی هشت و شمسه) که بسیار
 * آهسته می‌لغزد و نفس می‌کشد. عمداً کم‌رنگ است تا هرگز با توپ‌ها رقابت نکند.
 */
@Composable
fun GirihBackground(
    modifier: Modifier = Modifier,
    theme: GameTheme = LocalGameTheme.current,
    intensity: Float = 1f
) {
    val transition = rememberInfiniteTransition(label = "girih")
    val drift by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(46_000, easing = LinearEasing)),
        label = "drift"
    )
    val breathe by transition.animateFloat(
        initialValue = 0.72f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(9_000, easing = LinearEasing), RepeatMode.Reverse),
        label = "breathe"
    )

    val density = LocalDensity.current
    val cellPx = remember(density) { with(density) { 108.dp.toPx() } }
    val tile = remember(cellPx) { girihTile(cellPx) }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(Brush.verticalGradient(listOf(theme.top, theme.bottom)))

        val shift = drift * cellPx
        val stroke = Stroke(width = 1.4f)
        val lineColor = theme.accent.copy(alpha = 0.075f * intensity * breathe)
        val starColor = Palette.Sadaf.copy(alpha = 0.035f * intensity * breathe)

        var y = -cellPx
        while (y < size.height + cellPx) {
            var x = -cellPx
            while (x < size.width + cellPx) {
                translate(left = x + shift % cellPx, top = y + shift % cellPx) {
                    drawPath(tile.star, color = lineColor, style = stroke)
                    drawPath(tile.corners, color = starColor, style = stroke)
                }
                x += cellPx
            }
            y += cellPx
        }

        // هاله‌ی مرکزی تا لبه‌ها آرام بگیرند
        drawRect(
            Brush.radialGradient(
                colors = listOf(Color.Transparent, theme.bottom.copy(alpha = 0.85f)),
                center = Offset(size.width / 2f, size.height * 0.42f),
                radius = size.maxDimension * 0.72f
            )
        )
    }
}

private class GirihTile(val star: Path, val corners: Path)

private fun girihTile(cell: Float): GirihTile {
    val c = cell / 2f
    val star = Path()
    val outer = cell * 0.40f
    val inner = outer * 0.46f
    for (k in 0 until 16) {
        val r = if (k % 2 == 0) outer else inner
        val a = Math.toRadians((-90 + k * 22.5)).toFloat()
        val px = c + r * cos(a)
        val py = c + r * sin(a)
        if (k == 0) star.moveTo(px, py) else star.lineTo(px, py)
    }
    star.close()

    // لوزی‌های گوشه؛ با کنار هم قرار گرفتن کاشی‌ها به شمسه‌ی کامل می‌رسند.
    val corners = Path()
    val d = cell * 0.13f
    for (cx in listOf(0f, cell)) {
        for (cy in listOf(0f, cell)) {
            corners.moveTo(cx, cy - d)
            corners.lineTo(cx + d, cy)
            corners.lineTo(cx, cy + d)
            corners.lineTo(cx - d, cy)
            corners.close()
        }
    }
    return GirihTile(star, corners)
}

/** درخششِ نرم پشت عناصر مهم. */
fun DrawScope.softGlow(center: Offset, radius: Float, color: Color, alpha: Float = 0.35f) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = alpha), Color.Transparent),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}
