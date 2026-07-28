package com.parsgames.sortpuzzle.core.engine

import com.parsgames.sortpuzzle.core.model.Board
import com.parsgames.sortpuzzle.core.model.Move

/**
 * حل‌کننده‌ی تخته با جست‌وجوی عمق‌اول، حافظه‌ی حالت‌های دیده‌شده و
 * مرتب‌سازیِ ابتکاریِ حرکت‌ها.
 *
 * دو کاربرد دارد:
 *  ۱. تضمینِ حل‌پذیریِ مرحله‌های تولیدشده،
 *  ۲. ساختِ راهنما (اولین حرکتِ یک مسیرِ درست).
 *
 * همیشه خارج از رشته‌ی اصلی صدا زده می‌شود.
 */
object Solver {

    private const val DEFAULT_NODE_LIMIT = 400_000

    fun solve(board: Board, nodeLimit: Int = DEFAULT_NODE_LIMIT): List<Move>? {
        val visited = HashSet<String>(1 shl 14)
        val path = ArrayList<Move>(256)
        var nodes = 0

        fun dfs(state: Board): Boolean {
            if (state.isSolved) return true
            if (++nodes > nodeLimit) return false

            val key = state.canonicalKey()
            if (!visited.add(key)) return false

            for (move in candidateMoves(state)) {
                val (next, applied) = state.pour(move.from, move.to) ?: continue
                path.add(applied)
                if (dfs(next)) return true
                path.removeAt(path.lastIndex)
                if (nodes > nodeLimit) return false
            }
            return false
        }

        return if (dfs(board)) path.toList() else null
    }

    /** اولین حرکتِ یک مسیرِ حل، برای دکمه‌ی راهنما. */
    fun hint(board: Board): Move? = solve(board, nodeLimit = 200_000)?.firstOrNull()

    fun isSolvable(board: Board, nodeLimit: Int = 250_000): Boolean =
        solve(board, nodeLimit) != null

    /**
     * حرکت‌های معنادار، مرتب‌شده از امیدبخش‌ترین به کم‌ارزش‌ترین.
     * از میان چند لوله‌ی خالی فقط یکی در نظر گرفته می‌شود چون با هم تفاوتی ندارند.
     */
    private fun candidateMoves(state: Board): List<Move> {
        val scored = ArrayList<Pair<Move, Int>>(32)
        val tubes = state.tubes
        val firstEmpty = tubes.indexOfFirst { it.isEmpty }

        for (from in tubes.indices) {
            val src = tubes[from]
            if (src.isEmpty || src.isComplete(state.capacity)) continue
            // لوله‌ای که فقط یک رنگ دارد، چیزی برای مرتب کردن ندارد
            // مگر آنکه بشود آن را روی همان رنگ در جای دیگری خالی کرد.
            val srcUniform = src.isUniform()

            for (to in tubes.indices) {
                if (to == from) continue
                val dst = tubes[to]
                if (dst.isEmpty) {
                    if (to != firstEmpty) continue      // لوله‌های خالی هم‌ارزند
                    if (srcUniform) continue            // جابه‌جاییِ بی‌حاصل
                }
                val amount = state.pourAmount(from, to)
                if (amount == 0) continue

                val score = when {
                    dst.size + amount == state.capacity && dst.isUniform() &&
                        (dst.isEmpty || dst.top == src.top) -> 100      // یک لوله کامل می‌شود
                    !dst.isEmpty && src.topRunLength == amount -> 60    // کلِ دسته منتقل می‌شود
                    !dst.isEmpty -> 40
                    else -> 10                                          // ریختن در لوله‌ی خالی
                }
                scored.add(Move(from, to, amount, src.top ?: -1) to score)
            }
        }
        scored.sortByDescending { it.second }
        return scored.map { it.first }
    }
}
