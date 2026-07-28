package com.parsgames.sortpuzzle.core.util

/**
 * ابزارهای فارسی‌سازیِ متن.
 *
 * تمام عددهایی که روی صفحه دیده می‌شوند از این‌جا عبور می‌کنند تا هیچ رقم لاتینی
 * در رابط کاربری باقی نماند.
 */
object Persian {

    private val digits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun digits(value: Any?): String {
        val text = value?.toString() ?: return ""
        val sb = StringBuilder(text.length)
        for (ch in text) {
            sb.append(if (ch in '0'..'9') digits[ch - '0'] else ch)
        }
        return sb.toString()
    }

    /** عدد با جداکننده‌ی هزارگان و ارقام فارسی: ۱٬۲۵۰ */
    fun number(value: Long): String {
        val raw = kotlin.math.abs(value).toString()
        val sb = StringBuilder()
        for ((i, ch) in raw.withIndex()) {
            if (i > 0 && (raw.length - i) % 3 == 0) sb.append('٬')
            sb.append(digits[ch - '0'])
        }
        return if (value < 0) "−$sb" else sb.toString()
    }

    fun number(value: Int): String = number(value.toLong())

    /** شمارش معکوس به شکل «۰۲:۱۴:۰۹». */
    fun countdown(millis: Long): String {
        val total = (millis / 1000).coerceAtLeast(0)
        val h = total / 3600
        val m = (total % 3600) / 60
        val s = total % 60
        fun two(v: Long) = digits(v.toString().padStart(2, '0'))
        return "${two(h)}:${two(m)}:${two(s)}"
    }

    /** تاریخ کوتاه برای نمایش اعتبار اشتراک. */
    fun shortDate(epochMillis: Long): String {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = epochMillis
        val y = cal.get(java.util.Calendar.YEAR)
        val mo = cal.get(java.util.Calendar.MONTH) + 1
        val d = cal.get(java.util.Calendar.DAY_OF_MONTH)
        return digits("$y/${mo.toString().padStart(2, '0')}/${d.toString().padStart(2, '0')}")
    }
}
