package com.parsgames.sortpuzzle.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.parsgames.sortpuzzle.core.util.Persian
import com.parsgames.sortpuzzle.ui.theme.LocalGameTheme
import com.parsgames.sortpuzzle.ui.theme.Palette

enum class ButtonTone { Primary, Gold, Ghost, Danger }

/** دکمه‌ی اصلیِ بازی: شیشه‌ای، با فشارِ محسوس و بدونِ سایه‌ی متریالِ پیش‌فرض. */
@Composable
fun GameButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: ButtonTone = ButtonTone.Primary,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 56.dp
) {
    val accent = LocalGameTheme.current.accent
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.965f else 1f, spring(), label = "press")

    val (fill, content, border) = when (tone) {
        ButtonTone.Primary -> Triple(
            Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.72f))),
            Palette.Shabrang, null
        )
        ButtonTone.Gold -> Triple(
            Brush.horizontalGradient(listOf(Palette.Zaferan, Color(0xFFE39A22))),
            Palette.Shabrang, null
        )
        ButtonTone.Danger -> Triple(
            Brush.horizontalGradient(listOf(Palette.Anaar, Color(0xFFB32B44))),
            Palette.Sadaf, null
        )
        ButtonTone.Ghost -> Triple(
            Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.07f), Color.White.copy(alpha = 0.04f))),
            Palette.Sadaf, BorderStroke(1.dp, Palette.GlassEdge)
        )
    }

    Box(
        modifier = modifier
            .height(height)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(fill)
            .then(if (border != null) Modifier.border(border, RoundedCornerShape(18.dp)) else Modifier)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) content else content.copy(alpha = 0.45f)
            )
        }
    }
}

/** نشانگرِ سکه در بالای صفحه. */
@Composable
fun CoinPill(coins: Int, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.32f))
            .border(1.dp, Palette.Zaferan.copy(alpha = 0.45f), CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(17.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Palette.Zaferan, Color(0xFFC9821A))))
        )
        Spacer(Modifier.width(7.dp))
        Text(
            text = Persian.number(coins),
            style = MaterialTheme.typography.labelMedium,
            color = Palette.Sadaf
        )
    }
}

/** دکمه‌ی کمک‌ها با شمارنده‌ی موجودی. */
@Composable
fun BoosterButton(
    icon: ImageVector,
    label: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val accent = LocalGameTheme.current.accent
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.93f else 1f, spring(), label = "boosterPress")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.scale(scale)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(Color.White.copy(alpha = 0.07f))
                    .border(1.dp, Palette.GlassEdge, RoundedCornerShape(17.dp))
                    .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = if (enabled) accent else Palette.SadafDim, modifier = Modifier.size(24.dp))
            }
            Box(
                modifier = Modifier
                    .size(21.dp)
                    .clip(CircleShape)
                    .background(if (count > 0) Palette.Zaferan else Palette.Anaar),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (count > 0) Persian.digits(count) else "+",
                    style = MaterialTheme.typography.labelSmall,
                    color = Palette.Shabrang
                )
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Palette.SadafDim)
    }
}

@Composable
fun StarRow(stars: Int, modifier: Modifier = Modifier, size: Dp = 22.dp, max: Int = 3) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(max) { i ->
            Icon(
                imageVector = if (i < stars) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = null,
                tint = if (i < stars) Palette.Zaferan else Palette.SadafDim.copy(alpha = 0.4f),
                modifier = Modifier.size(size)
            )
        }
    }
}

/** کارتِ شیشه‌ای؛ پایه‌ی همه‌ی پنل‌ها و گفت‌وگوها. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        color = Palette.Lajevard.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, Palette.GlassEdge),
        tonalElevation = 0.dp
    ) { Box(Modifier.padding(20.dp)) { content() } }
}

@Composable
fun GameDialog(
    title: String,
    onDismiss: () -> Unit,
    dismissOnOutside: Boolean = true,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = { if (dismissOnOutside) onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = dismissOnOutside,
            dismissOnClickOutside = dismissOnOutside,
            usePlatformDefaultWidth = false
        )
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth(0.88f)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Palette.Sadaf,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(14.dp))
                content()
            }
        }
    }
}

/** خط جداکننده‌ی ظریف با حالتِ کاشی. */
@Composable
fun Divider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, Palette.GlassEdge, Color.Transparent)
                )
            )
    )
}
