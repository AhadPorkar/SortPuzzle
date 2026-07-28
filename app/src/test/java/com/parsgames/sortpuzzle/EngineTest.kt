package com.parsgames.sortpuzzle

import com.parsgames.sortpuzzle.core.audio.MidiComposer
import com.parsgames.sortpuzzle.core.engine.LevelFactory
import com.parsgames.sortpuzzle.core.engine.Solver
import com.parsgames.sortpuzzle.core.model.Board
import com.parsgames.sortpuzzle.core.model.Levels
import com.parsgames.sortpuzzle.core.model.Tube
import com.parsgames.sortpuzzle.core.util.Persian
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineTest {

    @Test
    fun `ریختن فقط روی رنگ همسان انجام می‌شود`() {
        val board = Board.of(
            listOf(0, 0, 1),
            listOf(1, 1),
            emptyList(),
            capacity = 4
        )
        assertTrue(board.canPour(0, 1))     // یک روی یک
        assertTrue(board.canPour(0, 2))     // روی لوله‌ی خالی
        assertEquals(1, board.pourAmount(0, 1))
    }

    @Test
    fun `دسته‌ی هم‌رنگ یکجا منتقل می‌شود`() {
        val board = Board.of(listOf(2, 3, 3), listOf(3), capacity = 4)
        assertEquals(2, board.pourAmount(0, 1))
        val (next, move) = board.pour(0, 1)!!
        assertEquals(2, move.count)
        assertEquals(listOf(2), next.tubes[0].balls)
        assertEquals(listOf(3, 3, 3), next.tubes[1].balls)
    }

    @Test
    fun `تخته‌ی مرتب حل‌شده شمرده می‌شود`() {
        val board = Board(
            listOf(Tube(List(4) { 0 }), Tube(List(4) { 1 }), Tube()),
            capacity = 4
        )
        assertTrue(board.isSolved)
    }

    @Test
    fun `هر مرحله‌ی تولیدشده حل‌پذیر است`() = runBlocking {
        // نمونه‌برداری از سراسر منحنی سختی
        for (level in listOf(1, 7, 25, 60, 120, 205, 300, 404, 480)) {
            val spec = Levels.specFor(level)
            val board = LevelFactory.build(spec)
            assertEquals(spec.tubeCount, board.tubes.size)
            assertNotNull("مرحله‌ی $level راه‌حل ندارد", Solver.solve(board.revealAll()))
        }
    }

    @Test
    fun `فایل MIDI با سرآیند درست ساخته می‌شود`() {
        val bytes = MidiComposer.compose(42)
        assertTrue(bytes.size > 512)
        assertEquals("MThd", String(bytes.copyOfRange(0, 4), Charsets.US_ASCII))
        assertEquals("MTrk", String(bytes.copyOfRange(14, 18), Charsets.US_ASCII))
    }

    @Test
    fun `ارقام فارسی درست قالب‌بندی می‌شوند`() {
        assertEquals("۱۲۳", Persian.digits(123))
        assertEquals("۱٬۲۵۰", Persian.number(1250))
        assertEquals("۰۱:۰۰:۰۰", Persian.countdown(3_600_000))
    }
}
