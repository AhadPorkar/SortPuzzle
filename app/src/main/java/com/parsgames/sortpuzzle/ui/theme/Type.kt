package com.parsgames.sortpuzzle.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.parsgames.sortpuzzle.R

/** وزیرمتن — تنها خانواده‌ی قلمِ بازی، با پنج وزن. */
val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
    Font(R.font.vazirmatn_black, FontWeight.Black)
)

private fun style(
    weight: FontWeight,
    size: Int,
    lineHeight: Int = (size * 1.55f).toInt(),
    letterSpacing: Float = 0f
) = TextStyle(
    fontFamily = Vazirmatn,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp
)

val GameTypography = Typography(
    displayLarge  = style(FontWeight.Black, 40, 52),
    displayMedium = style(FontWeight.Black, 32, 44),
    headlineLarge = style(FontWeight.Bold, 26, 38),
    headlineMedium= style(FontWeight.Bold, 22, 32),
    titleLarge    = style(FontWeight.SemiBold, 19, 28),
    titleMedium   = style(FontWeight.SemiBold, 16, 24),
    bodyLarge     = style(FontWeight.Normal, 16, 26),
    bodyMedium    = style(FontWeight.Normal, 14, 23),
    bodySmall     = style(FontWeight.Normal, 12, 20),
    labelLarge    = style(FontWeight.SemiBold, 15, 22),
    labelMedium   = style(FontWeight.Medium, 13, 18),
    labelSmall    = style(FontWeight.Medium, 11, 16)
)
