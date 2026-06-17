package com.sachini.emmamemorygame

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.math.*

object SoundManager {
    var isMuted     = false
    var musicVolume = 0.40f   // 0.0 – 1.0, read every note so sliders take effect instantly

    private var bgJob: Job? = null

    // ── Frequencies ───────────────────────────────────────────────────────────
    private const val C3  = 130.81f
    private const val F3  = 174.61f
    private const val G3  = 196.00f
    private const val A3  = 220.00f
    private const val C4  = 261.63f
    private const val D4  = 293.66f
    private const val E4  = 329.63f
    private const val F4  = 349.23f
    private const val G4  = 392.00f
    private const val A4  = 440.00f
    private const val B4  = 493.88f
    private const val C5  = 523.25f
    private const val D5  = 587.33f
    private const val E5  = 659.25f
    private const val G5  = 783.99f
    private const val A5  = 880.00f

    // ── Beautiful ambient melody — melody + bass mixed ────────────────────────
    // Triple(melodyHz, bassHz, durationMs)
    private val bgMelody = listOf(
        Triple(G4,  C3,  700), Triple(A4,  C3,  500), Triple(C5,  G3,  700), Triple(D5,  G3,  500),
        Triple(C5,  G3,  700), Triple(A4,  G3,  500), Triple(G4,  C3,  900), Triple(0f,  C3,  500),
        Triple(E4,  F3,  700), Triple(G4,  F3,  500), Triple(A4,  C3,  700), Triple(C5,  C3,  500),
        Triple(A4,  F3,  700), Triple(G4,  F3,  500), Triple(E4,  C3, 1100), Triple(0f,  C3,  500),
        Triple(D5,  G3,  700), Triple(C5,  G3,  500), Triple(A4,  C3,  700), Triple(G4,  C3,  500),
        Triple(F4,  F3,  700), Triple(E4,  F3,  500), Triple(D4,  G3,  700), Triple(C4,  C3,  500),
        Triple(E4,  C3,  700), Triple(G4,  G3,  500), Triple(A4,  F3,  700), Triple(C5,  C3,  500),
        Triple(B4,  G3,  700), Triple(A4,  G3,  500), Triple(G4,  C3, 1200), Triple(0f,  C3,  600)
    )

    // ── Short win fanfare ─────────────────────────────────────────────────────
    private val winFanfare = listOf(
        C4 to 110, E4 to 110, G4 to 110, C5 to 280,
        0f to 60,
        C5 to 90,  E5 to 90,  G5 to 90,
        E5 to 120, G5 to 120, A5 to 550
    )

    // ── Audio generation ──────────────────────────────────────────────────────

    private fun sineWave(freq: Float, durationMs: Int, sampleRate: Int, vol: Float): FloatArray {
        val n = (sampleRate * durationMs / 1000.0).toInt().coerceAtLeast(1)
        val arr = FloatArray(n)
        if (freq <= 0f) return arr
        val fadeLen = minOf((sampleRate * 0.018).toInt(), n / 3)
        for (i in arr.indices) {
            val fade = when {
                i < fadeLen           -> i.toDouble() / fadeLen
                i > n - fadeLen       -> (n - i).toDouble() / fadeLen
                else                  -> 1.0
            }
            arr[i] = (sin(2.0 * PI * freq * i / sampleRate) * vol * fade).toFloat()
        }
        return arr
    }

    private fun mix(melody: FloatArray, bass: FloatArray): ShortArray {
        val len = maxOf(melody.size, bass.size)
        return ShortArray(len) { i ->
            val m = if (i < melody.size) melody[i] * 0.65f else 0f
            val b = if (i < bass.size)   bass[i]   * 0.35f else 0f
            ((m + b).coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
        }
    }

    private fun toShorts(arr: FloatArray): ShortArray =
        ShortArray(arr.size) { i ->
            (arr[i].coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
        }

    private fun createTrack(sampleRate: Int = 44100): AudioTrack {
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        return AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
            AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            minBuf * 4,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )
    }

    // ── Public API ────────────────────────────────────────────────────────────

    fun startBackgroundMusic() {
        if (isMuted) return
        stopBackgroundMusic()
        bgJob = GlobalScope.launch(Dispatchers.IO) {
            val track = try { createTrack() } catch (_: Exception) { return@launch }
            try {
                track.play()
                while (isActive) {
                    for ((mFreq, bFreq, dur) in bgMelody) {
                        if (!isActive) break
                        val vol = musicVolume          // pick up slider changes every note
                        val melody = sineWave(mFreq, dur, 44100, vol)
                        val bass   = sineWave(bFreq, dur, 44100, vol * 0.6f)
                        val buf    = mix(melody, bass)
                        track.write(buf, 0, buf.size)
                    }
                }
            } catch (_: Exception) {
            } finally {
                try { track.stop(); track.release() } catch (_: Exception) {}
            }
        }
    }

    fun stopBackgroundMusic() {
        bgJob?.cancel()
        bgJob = null
    }

    suspend fun playWinFanfare() = withContext(Dispatchers.IO) {
        val track = try { createTrack() } catch (_: Exception) { return@withContext }
        try {
            track.play()
            for ((freq, dur) in winFanfare) {
                val buf = toShorts(sineWave(freq, dur, 44100, 0.55f))
                track.write(buf, 0, buf.size)
            }
        } catch (_: Exception) {
        } finally {
            try { track.stop(); track.release() } catch (_: Exception) {}
        }
    }
}
