package com.parsgames.sortpuzzle.core.model

import androidx.compose.runtime.Immutable

/**
 * یک لوله. اندیس ۰ کفِ لوله است و آخرین عنصر، توپِ رویی.
 *
 * @param revealed تعداد توپ‌هایی که از بالا برای بازیکن آشکار شده‌اند.
 *        در مرحله‌های عادی برابر با اندازه‌ی لوله است؛ در مرحله‌های «مه‌آلود»
 *        فقط توپِ رویی دیده می‌شود و با خالی شدن لوله، توپ‌های زیرین آشکار می‌شوند.
 */
@Immutable
data class Tube(
    val balls: List<Int> = emptyList(),
    val revealed: Int = balls.size
) {
    val size: Int get() = balls.size
    val isEmpty: Boolean get() = balls.isEmpty()
    val top: Int? get() = balls.lastOrNull()

    /** تعداد توپ‌های هم‌رنگِ پشت‌سرهم در بالای لوله. */
    val topRunLength: Int
        get() {
            val t = top ?: return 0
            var n = 0
            for (i in balls.indices.reversed()) {
                if (balls[i] == t) n++ else break
            }
            return n
        }

    fun isVisible(index: Int): Boolean = index >= size - revealed

    fun isComplete(capacity: Int): Boolean =
        balls.size == capacity && balls.all { it == balls[0] }

    fun isUniform(): Boolean = balls.isEmpty() || balls.all { it == balls[0] }
}

/** یک حرکت: ریختن از لوله‌ی [from] به لوله‌ی [to]. */
@Immutable
data class Move(val from: Int, val to: Int, val count: Int = 1, val color: Int = -1)

/**
 * وضعیت کاملِ تخته. تمام عملیات، نمونه‌ی تازه برمی‌گردانند تا Compose
 * بتواند تغییرها را به‌درستی تشخیص دهد.
 */
@Immutable
data class Board(
    val tubes: List<Tube>,
    val capacity: Int
) {
    val isSolved: Boolean
        get() = tubes.all { it.isEmpty || it.isComplete(capacity) }

    val completedCount: Int
        get() = tubes.count { it.isComplete(capacity) }

    fun canPour(from: Int, to: Int): Boolean {
        if (from == to) return false
        val src = tubes[from]
        val dst = tubes[to]
        val ball = src.top ?: return false
        if (dst.size >= capacity) return false
        if (src.isComplete(capacity)) return false
        val dstTop = dst.top ?: return true
        return dstTop == ball
    }

    /** بیشترین تعداد توپی که در این حرکت جابه‌جا می‌شود. */
    fun pourAmount(from: Int, to: Int): Int {
        if (!canPour(from, to)) return 0
        val free = capacity - tubes[to].size
        return minOf(tubes[from].topRunLength, free)
    }

    fun pour(from: Int, to: Int): Pair<Board, Move>? {
        val n = pourAmount(from, to)
        if (n == 0) return null
        val src = tubes[from]
        val dst = tubes[to]
        val color = src.top!!

        val newSrcBalls = src.balls.subList(0, src.size - n)
        val newSrc = Tube(
            balls = newSrcBalls,
            // پس از برداشتن توپ‌ها، توپ تازه‌ی رویی همیشه آشکار می‌شود.
            revealed = if (newSrcBalls.isEmpty()) 0 else maxOf(1, src.revealed - n)
        )
        val newDst = Tube(
            balls = dst.balls + List(n) { color },
            revealed = dst.revealed + n
        )

        val newTubes = tubes.toMutableList()
        newTubes[from] = newSrc
        newTubes[to] = newDst
        return Board(newTubes, capacity) to Move(from, to, n, color)
    }

    /** افزودن یک لوله‌ی خالی (بوسترِ «لوله‌ی کمکی»). */
    fun withExtraTube(): Board = copy(tubes = tubes + Tube())

    /** همه‌ی توپ‌ها را آشکار می‌کند (پایانِ مرحله‌های مه‌آلود). */
    fun revealAll(): Board = copy(tubes = tubes.map { it.copy(revealed = it.size) })

    /** کلیدِ یکتا و مستقل از ترتیبِ لوله‌ها؛ برای حذف حالت‌های تکراری در حل‌کننده. */
    fun canonicalKey(): String =
        tubes.map { t -> t.balls.joinToString(",") }.sorted().joinToString("|")

    fun hasAnyMove(): Boolean {
        for (i in tubes.indices) for (j in tubes.indices) {
            if (i != j && canPour(i, j)) {
                // ریختن بین دو لوله‌ی خالی/یک‌دست بی‌فایده است و پیشرفتی ایجاد نمی‌کند.
                if (tubes[j].isEmpty && tubes[i].isUniform()) continue
                return true
            }
        }
        return false
    }

    companion object {
        fun of(vararg tubes: List<Int>, capacity: Int) =
            Board(tubes.map { Tube(it) }, capacity)
    }
}
