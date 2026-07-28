package com.parsgames.sortpuzzle.core.model

import androidx.compose.runtime.Immutable

/** مشخصاتِ ساختِ یک مرحله. کاملاً از روی شماره‌ی مرحله محاسبه می‌شود. */
@Immutable
data class LevelSpec(
    val index: Int,
    val colors: Int,
    val emptyTubes: Int,
    val capacity: Int,
    val fog: Boolean,
    val parMoves: Int
) {
    val tubeCount: Int get() = colors + emptyTubes
}

@Immutable
data class Chapter(
    val number: Int,
    val title: String,
    val firstLevel: Int,
    val lastLevel: Int,
    val paletteSeed: Int
)

object Levels {

    const val LEVELS_PER_CHAPTER = 40
    const val TOTAL_LEVELS = 480

    val chapters: List<Chapter> = listOf(
        "باغ فیروزه", "کویر زعفران", "کاشی لاجورد", "انار و مینا",
        "شب‌های کویر", "نگارخانه", "آب‌انبار", "بازار مسگرها",
        "برف دماوند", "تالار آینه", "راه ابریشم", "گنبد مینا"
    ).mapIndexed { i, title ->
        Chapter(
            number = i + 1,
            title = title,
            firstLevel = i * LEVELS_PER_CHAPTER + 1,
            lastLevel = (i + 1) * LEVELS_PER_CHAPTER,
            paletteSeed = i
        )
    }

    fun chapterOf(level: Int): Chapter =
        chapters.getOrElse((level - 1) / LEVELS_PER_CHAPTER) { chapters.last() }

    /**
     * منحنی سختی. آرام شروع می‌شود، هر فصل یک عنصر تازه اضافه می‌کند و
     * هر ۱۰ مرحله یک «نفس‌گیری» ساده‌تر دارد تا بازیکن خسته نشود.
     */
    fun specFor(level: Int): LevelSpec {
        val l = level.coerceAtLeast(1)
        val chapter = chapterOf(l).number
        val within = (l - 1) % LEVELS_PER_CHAPTER

        // مرحله‌های ۱۰، ۲۰، ۳۰ و ۴۰ هر فصل کمی نفس‌گیر هستند.
        val breather = within % 10 == 9

        val base = when {
            l <= 3 -> 3
            l <= 8 -> 4
            l <= 15 -> 5
            else -> 5 + (l - 15) / 12
        }
        var colors = base.coerceIn(3, 13)
        if (breather) colors = (colors - 1).coerceAtLeast(3)

        // ظرفیت لوله از فصل ششم گاهی ۵ می‌شود.
        val capacity = if (chapter >= 6 && within % 7 == 3) 5 else 4

        // دو لوله‌ی خالی استاندارد؛ در مرحله‌های سختِ فصل‌های بالا فقط یکی.
        val emptyTubes = when {
            l <= 10 -> 2
            chapter >= 4 && within % 8 == 7 -> 1
            else -> 2
        }

        // مرحله‌های «مه‌آلود» از فصل پنجم آغاز می‌شوند.
        val fog = chapter >= 5 && within % 6 == 4 && !breather

        val par = (colors * capacity * 0.9f + colors * 1.6f).toInt() + emptyTubes
        return LevelSpec(l, colors, emptyTubes, capacity, fog, par)
    }

    /** ستاره‌ها بر پایه‌ی تعداد حرکت نسبت به «پار» مرحله. */
    fun starsFor(spec: LevelSpec, moves: Int): Int = when {
        moves <= spec.parMoves -> 3
        moves <= (spec.parMoves * 1.45f).toInt() -> 2
        else -> 1
    }
}
