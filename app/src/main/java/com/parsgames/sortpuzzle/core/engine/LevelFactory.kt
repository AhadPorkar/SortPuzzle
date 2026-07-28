package com.parsgames.sortpuzzle.core.engine

import com.parsgames.sortpuzzle.core.model.Board
import com.parsgames.sortpuzzle.core.model.LevelSpec
import com.parsgames.sortpuzzle.core.model.Tube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * سازنده‌ی مرحله‌ها.
 *
 * هر مرحله از روی شماره‌اش ساخته می‌شود، پس روی هر دستگاهی و در هر بار اجرا
 * دقیقاً یکسان است. حل‌پذیری با دو سازوکار تضمین می‌شود:
 *
 *  ۱. پخشِ تصادفیِ توپ‌ها و راستی‌آزمایی با [Solver]؛
 *  ۲. اگر چند تلاش نتیجه نداد، بُر زدنِ معکوس از روی تخته‌ی حل‌شده که
 *     ذاتاً حل‌پذیر است.
 */
object LevelFactory {

    private const val VERIFY_ATTEMPTS = 24

    suspend fun build(spec: LevelSpec): Board = withContext(Dispatchers.Default) {
        val fromDeal = dealAndVerify(spec)
        val board = fromDeal ?: reverseScramble(spec)
        if (spec.fog) applyFog(board) else board
    }

    // ---------------------------------------------------------------- pass 1

    private fun dealAndVerify(spec: LevelSpec): Board? {
        val pool = (0 until spec.colors).flatMap { color -> List(spec.capacity) { color } }

        repeat(VERIFY_ATTEMPTS) { attempt ->
            val rng = Random(seedOf(spec.index, attempt))
            val shuffled = pool.shuffled(rng)
            val filled = shuffled.chunked(spec.capacity).map { Tube(it) }

            // لوله‌ای که از همان ابتدا کامل است، مرحله را بی‌مزه می‌کند.
            if (filled.any { it.isComplete(spec.capacity) }) return@repeat

            val board = Board(filled + List(spec.emptyTubes) { Tube() }, spec.capacity)
            if (Solver.isSolvable(board)) return board
        }
        return null
    }

    // ---------------------------------------------------------------- pass 2

    /**
     * از تخته‌ی حل‌شده شروع می‌کند و حرکت‌های معکوسِ معتبر انجام می‌دهد.
     * چون هر حرکتِ معکوس، وارونِ یک حرکتِ قانونی است، مسیر بازگشت همیشه وجود دارد.
     */
    private fun reverseScramble(spec: LevelSpec): Board {
        val rng = Random(seedOf(spec.index, 999))
        val tubes = (0 until spec.colors)
            .map { color -> Tube(List(spec.capacity) { color }) }
            .plus(List(spec.emptyTubes) { Tube() })
            .toMutableList()

        val steps = spec.colors * spec.capacity * 6
        var performed = 0
        var guard = 0

        while (performed < steps && guard++ < steps * 20) {
            val d = rng.nextInt(tubes.size)
            val s = rng.nextInt(tubes.size)
            if (d == s) continue

            val donor = tubes[d]
            val receiver = tubes[s]
            if (donor.isEmpty) continue
            if (receiver.size >= spec.capacity) continue

            val color = donor.top!!
            if (!receiver.isEmpty && receiver.top != color) continue

            val run = donor.topRunLength
            val space = spec.capacity - receiver.size
            val maxK = minOf(run, space)
            if (maxK == 0) continue

            var k = 1 + rng.nextInt(maxK)
            // برای آنکه حرکتِ رو‌به‌جلو قانونی بماند، یا باید چیزی در لوله‌ی مبدأ
            // باقی بماند یا لوله کاملاً خالی شود.
            if (k == run && run != donor.size) k = run - 1
            if (k <= 0) continue

            tubes[d] = Tube(donor.balls.subList(0, donor.size - k))
            tubes[s] = Tube(receiver.balls + List(k) { color })
            performed++
        }

        // اگر بُر زدن به‌طور اتفاقی به تخته‌ی حل‌شده رسید، یک حرکت ساده باز می‌کنیم.
        val board = Board(tubes, spec.capacity)
        return if (board.isSolved) forceOneShuffle(board, rng) else board
    }

    private fun forceOneShuffle(board: Board, rng: Random): Board {
        val tubes = board.tubes.toMutableList()
        val full = tubes.indices.filter { tubes[it].size == board.capacity }
        val empty = tubes.indexOfFirst { it.isEmpty }
        if (full.isEmpty() || empty < 0) return board
        val src = full[rng.nextInt(full.size)]
        val color = tubes[src].top!!
        tubes[src] = Tube(tubes[src].balls.dropLast(1))
        tubes[empty] = Tube(listOf(color))
        return Board(tubes, board.capacity)
    }

    // ---------------------------------------------------------------- fog

    /** در مرحله‌های مه‌آلود تنها توپِ رویی هر لوله دیده می‌شود. */
    private fun applyFog(board: Board): Board =
        board.copy(tubes = board.tubes.map { t ->
            if (t.isEmpty) t else t.copy(revealed = 1)
        })

    private fun seedOf(level: Int, attempt: Int): Long =
        level * 1_000_003L + attempt * 7919L + 0x5DEECE66DL
}
