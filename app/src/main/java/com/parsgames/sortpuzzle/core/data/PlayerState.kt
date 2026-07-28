package com.parsgames.sortpuzzle.core.data

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/** تمام چیزی که میان اجراها ذخیره می‌شود. */
@Immutable
@Serializable
data class PlayerState(
    // اقتصاد
    val coins: Int = 200,
    val hints: Int = 3,
    val undos: Int = 5,
    val extraTubes: Int = 2,

    // پیشرفت
    val highestUnlocked: Int = 1,
    val lastPlayed: Int = 1,
    val stars: Map<Int, Int> = emptyMap(),
    val bestMoves: Map<Int, Int> = emptyMap(),
    val totalWins: Int = 0,

    // خرید‌ها
    val noAds: Boolean = false,
    val vipUntilMillis: Long = 0L,
    val bundleStartMillis: Long = 0L,

    // هدیه‌ی روزانه
    val lastDailyClaimMillis: Long = 0L,
    val dailyStreak: Int = 0,
    val vipDailyGrantMillis: Long = 0L,

    // تنظیمات
    val musicOn: Boolean = true,
    val sfxOn: Boolean = true,
    val hapticsOn: Boolean = true,
    val themeId: String = "firouzeh",
    val unlockedThemes: Set<String> = setOf("firouzeh")
) {
    fun isVip(now: Long = System.currentTimeMillis()): Boolean = vipUntilMillis > now

    /** آیا تبلیغِ اجباری (بین مرحله‌ای و بنر) باید نمایش داده شود؟ */
    fun adsRemoved(now: Long = System.currentTimeMillis()): Boolean = noAds || isVip(now)

    val totalStars: Int get() = stars.values.sum()
}
