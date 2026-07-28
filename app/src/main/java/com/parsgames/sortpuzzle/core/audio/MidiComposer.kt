package com.parsgames.sortpuzzle.core.audio

import java.io.ByteArrayOutputStream
import kotlin.random.Random

/**
 * آهنگ‌سازِ درون‌برنامه‌ای.
 *
 * برای هر مرحله یک قطعه‌ی MIDI کامل و یکتا می‌سازد و بایت‌های استانداردِ
 * Standard MIDI File (Format 1) را برمی‌گرداند. چون همه چیز از شماره‌ی مرحله
 * مشتق می‌شود، آهنگِ هر مرحله همیشه همان است و در بسته‌ی نصبی هیچ فایل صوتی
 * ذخیره نمی‌شود.
 *
 * هر فصل روی یکی از دستگاه‌های موسیقی ایرانی بنا شده و مرحله‌های درون فصل،
 * گوشه‌ها و ضرب‌های متفاوتی از همان دستگاه‌اند.
 */
object MidiComposer {

    private const val TPQ = 96              // تیک در هر سیاه
    private const val EIGHTH = TPQ / 2
    private const val BARS = 16

    /** دستگاه‌ها و ردیف‌ها؛ فاصله‌ها به نزدیک‌ترین نیم‌پرده گرد شده‌اند. */
    private val dastgah = listOf(
        "شور" to intArrayOf(0, 1, 3, 5, 7, 8, 10),
        "ابوعطا" to intArrayOf(0, 1, 3, 5, 7, 9, 10),
        "بیات ترک" to intArrayOf(0, 2, 3, 5, 7, 8, 10),
        "افشاری" to intArrayOf(0, 1, 3, 5, 6, 8, 10),
        "دشتی" to intArrayOf(0, 2, 3, 5, 7, 8, 10),
        "همایون" to intArrayOf(0, 1, 4, 5, 7, 8, 10),
        "اصفهان" to intArrayOf(0, 2, 3, 5, 7, 8, 11),
        "سه‌گاه" to intArrayOf(0, 2, 3, 5, 7, 9, 10),
        "چهارگاه" to intArrayOf(0, 1, 4, 5, 7, 8, 11),
        "ماهور" to intArrayOf(0, 2, 4, 5, 7, 9, 11),
        "نوا" to intArrayOf(0, 2, 3, 5, 7, 8, 10),
        "راست‌پنجگاه" to intArrayOf(0, 2, 4, 5, 7, 9, 10)
    )

    /** سازهای اصلی (شماره‌های General MIDI). */
    private val leadInstruments = intArrayOf(
        15,  // سنتور
        46,  // چنگ
        104, // سه‌تار/سیتار
        12,  // ماریمبا
        11,  // ویبرافون
        24   // گیتار نایلونی
    )
    private val counterInstruments = intArrayOf(73, 72, 79, 40, 71)   // نی، فلوت، ابوا، کمانچه

    fun modeNameFor(level: Int): String = dastgah[chapterIndex(level)].first

    fun compose(level: Int): ByteArray {
        val rng = Random(level * 2_654_435_761L)
        val ch = chapterIndex(level)
        val scale = dastgah[ch].second

        val tonic = 57 + (ch % 5) + rng.nextInt(3)          // حدود A3
        val barEighths = when (level % 3) {
            0 -> 6      // شش‌هشتم، ضربِ رقص ایرانی
            1 -> 8      // چهارچهارم
            else -> 7   // هفت‌هشتم
        }
        val barTicks = barEighths * EIGHTH
        val bpm = 74 + (level % 7) * 5 + ch
        val lead = leadInstruments[rng.nextInt(leadInstruments.size)]
        val counter = counterInstruments[rng.nextInt(counterInstruments.size)]

        val conductor = Track().apply {
            timeSignature(0, barEighths, 8)
            tempo(0, bpm)
            trackName(0, "SortPuzzle • ${dastgah[ch].first}")
        }
        val melody = Track().apply { program(0, 0, lead) }
        val second = Track().apply { program(0, 1, counter) }
        val bass = Track().apply { program(0, 2, 32) }      // باس آکوستیک
        val pad = Track().apply { program(0, 3, 89) }       // پدِ گرم
        val drums = Track()

        val phrase = buildPhrase(rng, scale, barEighths)

        for (bar in 0 until BARS) {
            val t0 = bar * barTicks
            val energy = when {
                bar < 2 -> 0.55f
                bar in 8..11 -> 1.0f
                else -> 0.8f
            }
            writeMelodyBar(melody, rng, phrase, bar, t0, tonic, scale, energy)
            if (bar >= 4) writeCounterBar(second, rng, t0, tonic, scale, barEighths, energy)
            writeBassBar(bass, t0, tonic, scale, barEighths, bar)
            if (bar % 4 == 0) pad.note(t0, 3, tonic - 12, 44, barTicks * 4)
            writeDrumBar(drums, rng, t0, barEighths, bar, energy)
        }

        return assemble(listOf(conductor, melody, second, bass, pad, drums))
    }

    // ------------------------------------------------------------------ ملودی

    /** یک جمله‌ی چهارمیزانی از درجه‌های گام؛ بقیه‌ی قطعه واریاسیونِ آن است. */
    private fun buildPhrase(rng: Random, scale: IntArray, barEighths: Int): List<List<Pair<Int, Int>>> {
        var degree = 0
        return List(4) {
            val cells = mutableListOf<Pair<Int, Int>>()   // درجه به همراه کشش (به هشتم)
            var left = barEighths
            while (left > 0) {
                val dur = when {
                    left == 1 -> 1
                    rng.nextFloat() < 0.42f -> 1
                    rng.nextFloat() < 0.75f -> 2
                    else -> minOf(3, left)
                }.coerceAtMost(left)
                val step = rng.nextInt(-2, 3)
                degree = (degree + step).coerceIn(-3, 9)
                cells.add(degree to dur)
                left -= dur
            }
            cells
        }
    }

    private fun writeMelodyBar(
        track: Track, rng: Random, phrase: List<List<Pair<Int, Int>>>,
        bar: Int, t0: Int, tonic: Int, scale: IntArray, energy: Float
    ) {
        val cells = phrase[bar % 4]
        var t = t0
        for ((degree, dur) in cells) {
            val vary = if (bar >= 8 && rng.nextFloat() < 0.3f) rng.nextInt(-1, 2) else 0
            val pitch = pitchOf(tonic + 12, scale, degree + vary)
            val vel = (72 * energy + rng.nextInt(0, 14)).toInt().coerceIn(48, 118)
            // گاهی سکوت، تا جمله نفس بکشد
            if (rng.nextFloat() > 0.12f) {
                track.note(t, 0, pitch, vel, dur * EIGHTH - 6)
                // زینتِ کوتاه (تحریر)
                if (dur >= 2 && rng.nextFloat() < 0.22f) {
                    track.note(t + dur * EIGHTH - EIGHTH / 2, 0,
                        pitchOf(tonic + 12, scale, degree + 1), (vel * 0.7f).toInt(), EIGHTH / 2 - 4)
                }
            }
            t += dur * EIGHTH
        }
    }

    private fun writeCounterBar(
        track: Track, rng: Random, t0: Int, tonic: Int,
        scale: IntArray, barEighths: Int, energy: Float
    ) {
        if (rng.nextFloat() < 0.35f) return
        val degree = intArrayOf(0, 2, 4, 6)[rng.nextInt(4)]
        val len = (barEighths / 2) * EIGHTH
        track.note(t0, 1, pitchOf(tonic + 12, scale, degree), (58 * energy).toInt().coerceIn(40, 90), len)
    }

    private fun writeBassBar(
        track: Track, t0: Int, tonic: Int, scale: IntArray, barEighths: Int, bar: Int
    ) {
        val root = pitchOf(tonic - 12, scale, 0)
        val fifth = pitchOf(tonic - 12, scale, 4)
        track.note(t0, 2, root, 82, EIGHTH * 2 - 8)
        if (barEighths >= 6) {
            track.note(t0 + EIGHTH * 3, 2, if (bar % 2 == 0) fifth else root, 70, EIGHTH * 2 - 8)
        }
        if (barEighths == 8) {
            track.note(t0 + EIGHTH * 6, 2, fifth, 66, EIGHTH * 2 - 8)
        }
    }

    // ---------------------------------------------------------------- کوبه‌ای

    private const val DRUM_CH = 9
    private const val TOMBAK_TOM = 64      // Low Conga  → «تم»
    private const val TOMBAK_BAK = 62      // Mute Hi Conga → «بک»
    private const val TOMBAK_RIZ = 63      // Open Hi Conga
    private const val SHAKER = 82
    private const val DAF = 47             // Low-Mid Tom، شبیه دف

    private fun writeDrumBar(
        track: Track, rng: Random, t0: Int, barEighths: Int, bar: Int, energy: Float
    ) {
        if (bar < 1) return
        val accent = (94 * energy).toInt().coerceIn(50, 120)
        val soft = (58 * energy).toInt().coerceIn(35, 90)

        // «تم» روی سرِ ضرب
        track.note(t0, DRUM_CH, TOMBAK_TOM, accent, EIGHTH / 2)

        val pattern = when (barEighths) {
            6 -> intArrayOf(0, 2, 3, 5)
            7 -> intArrayOf(0, 2, 4, 5)
            else -> intArrayOf(0, 2, 4, 6)
        }
        for (p in pattern.drop(1)) {
            track.note(t0 + p * EIGHTH, DRUM_CH, TOMBAK_BAK, soft + rng.nextInt(0, 10), EIGHTH / 2)
        }
        // ریزِ سبک روی هشتم‌ها
        for (e in 0 until barEighths) {
            if (rng.nextFloat() < 0.55f) {
                track.note(t0 + e * EIGHTH, DRUM_CH, SHAKER, (soft * 0.6f).toInt().coerceAtLeast(28), EIGHTH / 3)
            }
        }
        // ضربه‌ی دف در پایان هر چهار میزان
        if (bar % 4 == 3) {
            track.note(t0 + (barEighths - 1) * EIGHTH, DRUM_CH, DAF, accent, EIGHTH)
        }
    }

    // ------------------------------------------------------------------ کمکی

    private fun chapterIndex(level: Int): Int =
        ((level - 1) / 40).coerceIn(0, dastgah.size - 1)

    /** درجه‌ی گام (که می‌تواند منفی یا بزرگ‌تر از یک اکتاو باشد) به نُتِ MIDI. */
    private fun pitchOf(base: Int, scale: IntArray, degree: Int): Int {
        val n = scale.size
        val octave = Math.floorDiv(degree, n)
        val idx = Math.floorMod(degree, n)
        return (base + octave * 12 + scale[idx]).coerceIn(24, 108)
    }

    // ------------------------------------------------- ساختِ بایت‌های فایل MIDI

    private class Event(val tick: Int, val order: Int, val bytes: ByteArray)

    private class Track {
        private val events = ArrayList<Event>(512)

        fun note(tick: Int, channel: Int, pitch: Int, velocity: Int, durationTicks: Int) {
            val p = pitch.coerceIn(0, 127)
            val v = velocity.coerceIn(1, 127)
            val d = durationTicks.coerceAtLeast(6)
            events.add(Event(tick, 1, byteArrayOf((0x90 or channel).toByte(), p.toByte(), v.toByte())))
            events.add(Event(tick + d, 0, byteArrayOf((0x80 or channel).toByte(), p.toByte(), 0x40)))
        }

        fun program(tick: Int, channel: Int, program: Int) {
            events.add(Event(tick, 0, byteArrayOf((0xC0 or channel).toByte(), program.toByte())))
        }

        fun tempo(tick: Int, bpm: Int) {
            val usPerQuarter = (60_000_000.0 / bpm).toInt()
            events.add(Event(tick, 0, byteArrayOf(
                0xFF.toByte(), 0x51, 0x03,
                (usPerQuarter shr 16 and 0xFF).toByte(),
                (usPerQuarter shr 8 and 0xFF).toByte(),
                (usPerQuarter and 0xFF).toByte()
            )))
        }

        fun timeSignature(tick: Int, numerator: Int, denominator: Int) {
            var dd = 0
            var d = denominator
            while (d > 1) { d = d shr 1; dd++ }
            events.add(Event(tick, 0, byteArrayOf(
                0xFF.toByte(), 0x58, 0x04, numerator.toByte(), dd.toByte(), 24, 8
            )))
        }

        fun trackName(tick: Int, name: String) {
            val raw = name.toByteArray(Charsets.UTF_8)
            val out = ByteArrayOutputStream()
            out.write(0xFF); out.write(0x03)
            writeVarLen(out, raw.size)
            out.write(raw)
            events.add(Event(tick, 0, out.toByteArray()))
        }

        fun toChunk(): ByteArray {
            events.sortWith(compareBy({ it.tick }, { it.order }))
            val body = ByteArrayOutputStream()
            var last = 0
            for (e in events) {
                writeVarLen(body, e.tick - last)
                body.write(e.bytes)
                last = e.tick
            }
            writeVarLen(body, 0)
            body.write(byteArrayOf(0xFF.toByte(), 0x2F, 0x00))

            val data = body.toByteArray()
            val chunk = ByteArrayOutputStream()
            chunk.write("MTrk".toByteArray(Charsets.US_ASCII))
            chunk.write(intBytes(data.size))
            chunk.write(data)
            return chunk.toByteArray()
        }
    }

    private fun assemble(tracks: List<Track>): ByteArray {
        val out = ByteArrayOutputStream()
        out.write("MThd".toByteArray(Charsets.US_ASCII))
        out.write(intBytes(6))
        out.write(shortBytes(1))                 // Format 1
        out.write(shortBytes(tracks.size))
        out.write(shortBytes(TPQ))
        tracks.forEach { out.write(it.toChunk()) }
        return out.toByteArray()
    }

    private fun intBytes(v: Int) = byteArrayOf(
        (v shr 24 and 0xFF).toByte(), (v shr 16 and 0xFF).toByte(),
        (v shr 8 and 0xFF).toByte(), (v and 0xFF).toByte()
    )

    private fun shortBytes(v: Int) = byteArrayOf((v shr 8 and 0xFF).toByte(), (v and 0xFF).toByte())

    private fun writeVarLen(out: ByteArrayOutputStream, value: Int) {
        var v = value.coerceAtLeast(0)
        var buffer = v and 0x7F
        while (v shr 7 > 0) {
            v = v shr 7
            buffer = buffer shl 8
            buffer = buffer or 0x80
            buffer += v and 0x7F
        }
        while (true) {
            out.write(buffer and 0xFF)
            if (buffer and 0x80 != 0) buffer = buffer shr 8 else break
        }
    }
}
