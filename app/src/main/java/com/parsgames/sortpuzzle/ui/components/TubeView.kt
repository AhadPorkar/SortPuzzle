package com.parsgames.sortpuzzle.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.parsgames.sortpuzzle.core.model.Tube
import com.parsgames.sortpuzzle.core.util.Persian
import com.parsgames.sortpuzzle.ui.theme.BallColors
import com.parsgames.sortpuzzle.ui.theme.Palette

/**
 * یک لوله‌ی شیشه‌ای همراه با توپ‌هایش.
 *
 * سه حالتِ دیداری دارد: عادی، انتخاب‌شده (دسته‌ی رویی بالا می‌آید) و کامل‌شده
 * (حلقه‌ی زرین). در مرحله‌های مه‌آلود، توپ‌های ناشناخته با نشانِ پرسش می‌آیند.
 */
@Composable
fun TubeView(
    tube: Tube,
    capacity: Int,
    index: Int,
    selected: Boolean,
    highlighted: Boolean,
    ballSize: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val innerPadding = 5.dp
    val tubeWidth = ballSize + innerPadding * 2
    val tubeHeight = ballSize * capacity + innerPadding * 2
    val complete = tube.isComplete(capacity)

    val liftCount = if (selected) tube.topRunLength else 0
    val lift by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 620f),
        label = "lift"
    )

    val pulse = rememberInfiniteTransition(label = "tubePulse")
    val glow by pulse.animateFloat(
        initialValue = 0.35f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1_400), RepeatMode.Reverse),
        label = "glow"
    )

    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .width(tubeWidth)
            .height(tubeHeight + ballSize * 0.9f)   // فضای بالا برای توپِ بلندشده
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .semantics { contentDescription = "لوله‌ی شماره ${Persian.digits(index + 1)}" },
        contentAlignment = Alignment.BottomCenter
    ) {
        // ---- بدنه‌ی شیشه‌ای
        Box(
            modifier = Modifier
                .width(tubeWidth)
                .height(tubeHeight)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 26.dp, bottomEnd = 26.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.03f),
                            Color.White.copy(alpha = 0.13f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    )
                )
                .border(
                    width = if (complete) 2.dp else 1.dp,
                    brush = if (complete) {
                        Brush.verticalGradient(listOf(Palette.Zaferan, Palette.Zaferan.copy(alpha = 0.35f)))
                    } else if (highlighted) {
                        Brush.verticalGradient(listOf(Palette.Mina.copy(alpha = glow), Palette.Firouzeh.copy(alpha = 0.4f)))
                    } else {
                        Brush.verticalGradient(listOf(Palette.GlassEdge, Color.White.copy(alpha = 0.08f)))
                    },
                    shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 26.dp, bottomEnd = 26.dp)
                )
                .drawBehind {
                    if (complete) {
                        softGlow(
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = size.width * 1.5f,
                            color = Palette.Zaferan,
                            alpha = 0.18f
                        )
                    }
                }
        )

        // ---- توپ‌ها
        tube.balls.forEachIndexed { i, colorIndex ->
            val fromTop = tube.size - 1 - i
            val isLifted = fromTop < liftCount
            val visible = tube.isVisible(i)

            key(i, colorIndex, visible) {
                val appear = remember(i, colorIndex) { Animatable(0f) }
                LaunchedEffect(i, colorIndex) {
                    appear.animateTo(1f, tween(240, easing = FastOutSlowInEasing))
                }

                Ball(
                    colorIndex = colorIndex,
                    hidden = !visible,
                    size = ballSize,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = innerPadding)
                        .graphicsLayer {
                            val stack = -(ballSize.toPx() * i)
                            val drop = (1f - appear.value) * ballSize.toPx() * 2.2f
                            val liftPx = if (isLifted) -lift * (ballSize.toPx() * 0.95f) else 0f
                            translationY = stack + drop + liftPx
                            alpha = appear.value
                            val s = 0.86f + 0.14f * appear.value
                            scaleX = s; scaleY = s
                        }
                )
            }
        }
    }
}

@Composable
fun Ball(
    colorIndex: Int,
    size: Dp,
    modifier: Modifier = Modifier,
    hidden: Boolean = false
) {
    val base = if (hidden) Color(0xFF39406B) else BallColors[colorIndex]

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val r = this.size.minDimension / 2f - 1.5f
            val c = Offset(this.size.width / 2f, this.size.height / 2f)

            // سایه‌ی زیرین
            drawCircle(
                color = Color.Black.copy(alpha = 0.30f),
                radius = r,
                center = c.copy(y = c.y + r * 0.10f)
            )
            // حجمِ اصلی
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        lerp(base, Color.White, 0.34f),
                        base,
                        lerp(base, Color.Black, 0.36f)
                    ),
                    center = Offset(c.x - r * 0.34f, c.y - r * 0.40f),
                    radius = r * 1.85f
                ),
                radius = r,
                center = c
            )
            // بازتابِ نقطه‌ای
            drawCircle(
                color = Color.White.copy(alpha = if (hidden) 0.12f else 0.45f),
                radius = r * 0.20f,
                center = Offset(c.x - r * 0.36f, c.y - r * 0.42f)
            )
            // لبه‌ی نرم
            drawCircle(
                color = Color.Black.copy(alpha = 0.14f),
                radius = r,
                center = c,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f)
            )
        }

        if (hidden) {
            Text(
                text = "؟",
                style = MaterialTheme.typography.titleMedium,
                color = Palette.SadafDim
            )
        }
    }
}
