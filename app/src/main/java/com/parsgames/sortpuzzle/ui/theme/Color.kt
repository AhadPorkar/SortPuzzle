package com.parsgames.sortpuzzle.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * پالت از رنگ‌های کاشی‌کاری و مینای ایرانی گرفته شده است:
 * لاجورد، فیروزه، زعفران، انار و صدف.
 */
object Palette {
    val Shabrang   = Color(0xFF070B22)   // شبرنگ — تیره‌ترین لایه
    val Lajevard   = Color(0xFF11184A)   // لاجورد
    val LajevardUp = Color(0xFF1B2668)   // لاجورد روشن
    val Firouzeh   = Color(0xFF2BC4C9)   // فیروزه
    val Mina       = Color(0xFF7BE0D6)   // مینای روشن
    val Zaferan    = Color(0xFFF4B740)   // زعفران
    val Anaar      = Color(0xFFE1465E)   // انار
    val Sadaf      = Color(0xFFF6F1E4)   // صدف — متن اصلی
    val SadafDim   = Color(0xFFB9C0DA)   // متن فرعی
    val Glass      = Color(0x1FFFFFFF)   // شیشه‌ی لوله‌ها
    val GlassEdge  = Color(0x40FFFFFF)
}

/** رنگ توپ‌ها. با نام‌های رنگ‌دانه‌های سنتی. */
object BallColors {
    val list = listOf(
        Color(0xFF2BC4C9), // فیروزه‌ای
        Color(0xFFF4B740), // زعفرانی
        Color(0xFFE1465E), // اناری
        Color(0xFF4CC96B), // سبز مینا
        Color(0xFF9B6BE8), // ارغوانی
        Color(0xFFFF8A3D), // نارنجی
        Color(0xFF3B82F6), // آبی کاشی
        Color(0xFFFF7EB6), // گل‌محمدی
        Color(0xFFA3B84A), // زیتونی
        Color(0xFFC2371F), // لاکی
        Color(0xFF4C57C8), // نیلی
        Color(0xFFEFE3C8), // شیری
        Color(0xFF8A93A8)  // دودی
    )

    operator fun get(index: Int): Color = list[index.mod(list.size)]
}

/** پوسته‌های قابل انتخاب؛ فقط گرادیانِ پس‌زمینه و رنگِ تأکید را عوض می‌کنند. */
enum class GameTheme(
    val id: String,
    val title: String,
    val top: Color,
    val bottom: Color,
    val accent: Color,
    val priceInCoins: Int
) {
    FIROUZEH("firouzeh", "کاشی فیروزه", Palette.LajevardUp, Palette.Shabrang, Palette.Firouzeh, 0),
    ZAFERAN("zaferan", "غروب زعفران", Color(0xFF4A2A12), Color(0xFF160B14), Palette.Zaferan, 1200),
    ANAAR("anaar", "باغ انار", Color(0xFF4A1030), Color(0xFF11061C), Palette.Anaar, 1600),
    SHAB("shab", "شب کویر", Color(0xFF102A38), Color(0xFF04080F), Palette.Mina, 2000);

    companion object {
        fun byId(id: String): GameTheme = entries.firstOrNull { it.id == id } ?: FIROUZEH
    }
}
