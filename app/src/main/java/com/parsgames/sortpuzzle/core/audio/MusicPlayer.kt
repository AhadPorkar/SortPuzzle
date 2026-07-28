package com.parsgames.sortpuzzle.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * پخش‌کننده‌ی موسیقی پس‌زمینه.
 *
 * قطعه‌ی هر مرحله در لحظه ساخته می‌شود، در حافظه‌ی موقت ذخیره می‌ماند و با
 * سینتی‌سایزرِ داخلی اندروید پخش می‌شود؛ به همین دلیل حجم بازی تقریباً صفر
 * افزایش می‌یابد و هر مرحله موسیقی مخصوص خودش را دارد.
 */
class MusicPlayer(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var player: MediaPlayer? = null
    private var loadJob: Job? = null
    private var currentTrack: Int = Int.MIN_VALUE
    private var wasPlaying = false

    var enabled: Boolean = true
        set(value) {
            field = value
            if (!value) stopInternal() else if (currentTrack != Int.MIN_VALUE) play(currentTrack, force = true)
        }

    var volume: Float = 0.55f
        set(value) {
            field = value.coerceIn(0f, 1f)
            runCatching { player?.setVolume(field, field) }
        }

    /** شماره‌ی ۰ برای منوی اصلی در نظر گرفته شده است. */
    fun play(track: Int, force: Boolean = false) {
        if (!enabled) { currentTrack = track; return }
        if (track == currentTrack && !force && player?.isPlaying == true) return
        currentTrack = track

        loadJob?.cancel()
        loadJob = scope.launch {
            val file = runCatching { midiFileFor(track) }.getOrNull() ?: return@launch
            withContext(Dispatchers.Main) { startPlayback(file) }
        }
    }

    private suspend fun midiFileFor(track: Int): File = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "midi").apply { mkdirs() }
        val file = File(dir, "track_$track.mid")
        if (!file.exists() || file.length() < 64L) {
            file.writeBytes(MidiComposer.compose(track))
        }
        file
    }

    private fun startPlayback(file: File) {
        stopInternal()
        if (!enabled) return
        runCatching {
            player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(file.absolutePath)
                isLooping = true
                setVolume(volume, volume)
                setOnErrorListener { _, what, extra ->
                    Log.w(TAG, "خطای پخش موسیقی: $what/$extra")
                    stopInternal(); true
                }
                prepare()
                start()
            }
            wasPlaying = true
        }.onFailure {
            Log.w(TAG, "پخش قطعه ممکن نشد", it)
            stopInternal()
        }
    }

    fun pause() {
        wasPlaying = player?.isPlaying == true
        runCatching { if (wasPlaying) player?.pause() }
    }

    fun resume() {
        if (!enabled) return
        if (player == null && currentTrack != Int.MIN_VALUE) {
            play(currentTrack, force = true)
        } else if (wasPlaying) {
            runCatching { player?.start() }
        }
    }

    private fun stopInternal() {
        runCatching {
            player?.let { if (it.isPlaying) it.stop(); it.reset(); it.release() }
        }
        player = null
    }

    fun release() {
        loadJob?.cancel()
        stopInternal()
    }

    private companion object { const val TAG = "MusicPlayer" }
}
