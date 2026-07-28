package com.parsgames.sortpuzzle.monetization

/**
 * فهرست کالاها.
 *
 * شناسه‌های زیر باید دقیقاً با همین نام‌ها در Google Play Console ساخته شوند:
 *  • یک‌بارمصرف (Consumable): بسته‌های سکه و باندل‌ها
 *  • ماندگار (Non-consumable): حذف تبلیغات
 *  • اشتراک (Subscription): اشتراک ویژه با دو پلنِ پایه‌ی ماهانه و سالانه
 */
object Products {

    // ماندگار
    const val REMOVE_ADS = "remove_ads"

    // بسته‌های سکه (یک‌بارمصرف)
    const val COINS_S = "coins_small"      // ۵۰۰ سکه
    const val COINS_M = "coins_medium"     // ۱۵۰۰ سکه
    const val COINS_L = "coins_large"      // ۴۰۰۰ سکه

    // باندل‌های زمان‌دار (یک‌بارمصرف)
    const val BUNDLE_STARTER = "bundle_starter"    // ۲۴ ساعت پس از مرحله‌ی ۵
    const val BUNDLE_WEEKEND = "bundle_weekend"    // آخر هفته

    // اشتراک
    const val VIP_SUB = "vip_subscription"
    const val VIP_PLAN_MONTHLY = "vip-monthly"
    const val VIP_PLAN_YEARLY = "vip-yearly"

    val oneTimeProducts = listOf(
        REMOVE_ADS, COINS_S, COINS_M, COINS_L, BUNDLE_STARTER, BUNDLE_WEEKEND
    )
    val subscriptions = listOf(VIP_SUB)

    /** آنچه پس از خرید موفق به بازیکن داده می‌شود. */
    fun grantsFor(productId: String): Grant = when (productId) {
        REMOVE_ADS -> Grant.RemoveAds
        COINS_S -> Grant.Pack(coins = 500)
        COINS_M -> Grant.Pack(coins = 1_500, hints = 3)
        COINS_L -> Grant.Pack(coins = 4_000, hints = 10, undos = 10, tubes = 5)
        BUNDLE_STARTER -> Grant.Pack(coins = 1_200, hints = 5, undos = 8, tubes = 3)
        BUNDLE_WEEKEND -> Grant.Pack(coins = 2_500, hints = 8, undos = 12, tubes = 6, removeAds = true)
        VIP_SUB -> Grant.Vip(days = 31)
        else -> Grant.Pack()
    }

    /** آیا کالا باید پس از تحویل «مصرف» شود تا دوباره قابل خرید باشد؟ */
    fun isConsumable(productId: String): Boolean =
        productId != REMOVE_ADS && productId != VIP_SUB
}

sealed interface Grant {
    data object RemoveAds : Grant
    data class Vip(val days: Int) : Grant
    data class Pack(
        val coins: Int = 0,
        val hints: Int = 0,
        val undos: Int = 0,
        val tubes: Int = 0,
        val removeAds: Boolean = false
    ) : Grant
}

/** قیمت‌های سکه‌ای (ارزِ نرم) — نیازی به خرید درون‌برنامه‌ای ندارند. */
object CoinPrices {
    const val HINT = 60
    const val UNDO = 40
    const val EXTRA_TUBE = 120
    const val SKIP_LEVEL = 300
}
