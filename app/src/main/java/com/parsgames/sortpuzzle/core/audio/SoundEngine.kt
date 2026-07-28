package com.parsgames.sortpuzzle.core.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * جلوه‌های صوتی، بدون هیچ فایل صوتی.
 *
 * هر صدا با فرمولِ موج ساخته و در حافظه نگه داشته می‌شود؛ نتیجه، صداهای تمیز و
 * هماهنگ با فضای بازی است بدون افزایش حجمِ بسته‌ی نصبی.
 */
class SoundEngine {

    enum class Sfx { LIFT, DROP, BLOCKED, TUBE_DONE, LEVEL_WIN, COIN, TAP }

    var enabled: Boolean = true
    var volume: Float = 0.7f

    private val handler = Handler(Looper.getMainLooper())
    private val cache = HashMap<Sfx, ShortArray>()

    fun play(sfx: Sfx) {
        if (!enabled) return
        val pcm = cache.getOrPut(sfx) { synthesize(sfx) }
        playPcm(pcm)
    }

    // ------------------------------------------------------------- سنتز صداها

    private fun synthesize(sfx: Sfx): ShortArray = when (sfx) {
        Sfx.LIFT      -> sweep(from = 520f, to = 780f, ms = 90, decay = 14f, harmonic = 0.25f)
        Sfx.DROP      -> sweep(from = 760f, to = 380f, ms = 120, decay = 11f, harmonic = 0.35f)
        Sfx.BLOCKED   -> buzz(freq = 150f, ms = 130)
        Sfx.TUBE_DONE -> arpeggio(floatArrayOf(587f, 698f, 880f), stepMs = 70)
        Sfx.LEVEL_WIN -> arpeggio(floatArrayOf(523f, 622f, 698f, 880f, 1046f), stepMs = 105)
        Sfx.COIN      -> arpeggio(floatArrayOf(988f, 1318f), stepMs = 60)
        Sfx.TAP       -> sweep(from = 900f, to = 900f, ms = 40, decay = 30f, harmonic = 0f)
    }

    private fun sweep(from: Float, to: Float, ms: Int, decay: Float, harmonic: Float): ShortArray {
        val n = ms * SAMPLE_RATE / 1000
        val out = ShortArray(n)
        var phase = 0.0
        for (i in 0 until n) {
            val t = i.toFloat() / n
            val freq = from + (to - from) * t
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val env = exp((-decay * t).toDouble())
            val s = sin(phase) + harmonic * sin(phase * 2.0)
            out[i] = (s * env * 0.55 * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    private fun buzz(freq: Float, ms: Int): ShortArray {
        val n = ms * SAMPLE_RATE / 1000
        val out = ShortArray(n)
        for (i in 0 until n) {
            val t = i.toFloat() / n
            val phase = 2.0 * PI * freq * i / SAMPLE_RATE
            val square = if (sin(phase) >= 0) 1.0 else -1.0
            val env = exp((-6f * t).toDouble()) * (1.0 - t * 0.3)
            out[i] = (square * env * 0.32 * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    private fun arpeggio(freqs: FloatArray, stepMs: Int): ShortArray {
        val step = stepMs * SAMPLE_RATE / 1000
        val tail = SAMPLE_RATE / 4
        val out = ShortArray(step * freqs.size + tail)
        for ((k, f) in freqs.withIndex()) {
            val start = k * step
            val len = out.size - start
            var phase = 0.0
            for (i in 0 until len) {
                val t = i.toFloat() / SAMPLE_RATE
                phase += 2.0 * PI * f / SAMPLE_RATE
                val env = exp((-7.0 * t))
                val s = (sin(phase) + 0.3 * sin(phase * 2)) * env * 0.4
                val mixed = out[start + i] + (s * Short.MAX_VALUE).toInt()
                out[start + i] = mixed.coerceIn(-32768, 32767).toShort()
            }
        }
        return out
    }

    // ------------------------------------------------------------------ پخش

    private fun playPcm(data: ShortArray) {
        runCatching {
            val bytes = data.size * 2
            val minBuffer = AudioTrack.getMinBufferSize(
                SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(1024)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(maxOf(bytes, minBuffer))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(data, 0, data.size)
            track.setVolume(volume.coerceIn(0f, 1f))
            track.play()

            val durationMs = data.size * 1000L / SAMPLE_RATE + 150L
            handler.postDelayed({ runCatching { track.release() } }, durationMs)
        }
    }

    private companion object { const val SAMPLE_RATE = 22_050 }
}
