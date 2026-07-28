package com.parsgames.sortpuzzle.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val GameColorScheme = darkColorScheme(
    primary = Palette.Firouzeh,
    onPrimary = Palette.Shabrang,
    primaryContainer = Palette.LajevardUp,
    onPrimaryContainer = Palette.Sadaf,
    secondary = Palette.Zaferan,
    onSecondary = Palette.Shabrang,
    tertiary = Palette.Anaar,
    onTertiary = Palette.Sadaf,
    background = Palette.Shabrang,
    onBackground = Palette.Sadaf,
    surface = Palette.Lajevard,
    onSurface = Palette.Sadaf,
    surfaceVariant = Palette.LajevardUp,
    onSurfaceVariant = Palette.SadafDim,
    error = Palette.Anaar,
    outline = Palette.GlassEdge
)

val LocalGameTheme = staticCompositionLocalOf { GameTheme.FIROUZEH }

/**
 * پوسته‌ی سراسری بازی.
 *
 * جهتِ چیدمان همیشه راست‌به‌چپ است — حتی اگر زبان دستگاه انگلیسی باشد — چون
 * کل بازی فارسی است و نباید در دستگاهی با تنظیمات دیگر به‌هم بریزد.
 */
@Composable
fun SortPuzzleTheme(
    theme: GameTheme = GameTheme.FIROUZEH,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        LocalGameTheme provides theme
    ) {
        MaterialTheme(
            colorScheme = GameColorScheme,
            typography = GameTypography,
            content = content
        )
    }
}
