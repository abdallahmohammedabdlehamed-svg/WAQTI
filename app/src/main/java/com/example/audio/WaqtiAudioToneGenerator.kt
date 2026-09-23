package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural Audio Tone Generator for WAQTI.
 * Synthesizes soft, elegant, click-free tones via AudioTrack.
 * Functions as an immediate zero-dependency, fully offline fallback when physical MP3s are absent.
 */
object WaqtiAudioToneGenerator {

    private const val TAG = "WaqtiAudioToneGen"
    private const val SAMPLE_RATE = 44100

    private val audioScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    enum class ToneType {
        NOTIFICATION,
        REMINDER,
        TASK_COMPLETION,
        FOCUS_START,
        FOCUS_WARNING,
        FOCUS_COMPLETION,
        WARNING
    }

    /**
     * Plays a procedural tone asynchronously.
     */
    fun playTone(type: ToneType, volume: Float = 0.85f) {
        audioScope.launch {
            try {
                val samples = generateSamples(type)
                playPcmSamples(samples, volume.coerceIn(0.1f, 1.0f))
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to generate or play procedural tone: ${e.message}")
            }
        }
    }

    fun generateSamples(type: ToneType): ShortArray {
        return when (type) {
            ToneType.NOTIFICATION -> {
                // Soft 2-tone chime: D5 (587.33 Hz) -> A5 (880.0 Hz)
                generateSequence(
                    listOf(
                        ToneSegment(587.33, 160, 0.7),
                        ToneSegment(880.0, 240, 0.8)
                    )
                )
            }
            ToneType.REMINDER -> {
                // Gentle 3-tone arpeggio: C5 (523.25 Hz) -> E5 (659.25 Hz) -> G5 (783.99 Hz)
                generateSequence(
                    listOf(
                        ToneSegment(523.25, 140, 0.65),
                        ToneSegment(659.25, 140, 0.75),
                        ToneSegment(783.99, 280, 0.85)
                    )
                )
            }
            ToneType.TASK_COMPLETION -> {
                // Uplifting confirmation chord: C5 -> E5 -> G5 -> C6 (1046.5 Hz)
                generateSequence(
                    listOf(
                        ToneSegment(523.25, 100, 0.6),
                        ToneSegment(659.25, 100, 0.7),
                        ToneSegment(783.99, 120, 0.8),
                        ToneSegment(1046.50, 320, 0.9)
                    )
                )
            }
            ToneType.FOCUS_START -> {
                // Deep, grounding warm tone: A4 (440 Hz) -> C#5 (554.37 Hz)
                generateSequence(
                    listOf(
                        ToneSegment(440.0, 220, 0.7),
                        ToneSegment(554.37, 450, 0.85)
                    )
                )
            }
            ToneType.FOCUS_WARNING -> {
                // Subtle two-pulse reminder: A4 (440 Hz) -> pause -> A4 (440 Hz)
                generateSequence(
                    listOf(
                        ToneSegment(440.0, 140, 0.7),
                        ToneSegment(0.0, 80, 0.0),
                        ToneSegment(440.0, 200, 0.7)
                    )
                )
            }
            ToneType.FOCUS_COMPLETION -> {
                // Soothing celebration arpeggio
                generateSequence(
                    listOf(
                        ToneSegment(440.0, 120, 0.6),
                        ToneSegment(554.37, 120, 0.7),
                        ToneSegment(659.25, 140, 0.8),
                        ToneSegment(880.0, 380, 0.9)
                    )
                )
            }
            ToneType.WARNING -> {
                // Soft descending dual tone: F4 (349.23 Hz) -> D4 (293.66 Hz)
                generateSequence(
                    listOf(
                        ToneSegment(349.23, 180, 0.75),
                        ToneSegment(293.66, 260, 0.8)
                    )
                )
            }
        }
    }

    private data class ToneSegment(val freqHz: Double, val durationMs: Int, val amplitude: Double)

    private fun generateSequence(segments: List<ToneSegment>): ShortArray {
        val totalDurationMs = segments.sumOf { it.durationMs }
        val totalSamples = (SAMPLE_RATE * (totalDurationMs / 1000.0)).toInt()
        val result = ShortArray(totalSamples)

        var sampleOffset = 0
        for (seg in segments) {
            val segSamples = (SAMPLE_RATE * (seg.durationMs / 1000.0)).toInt()
            if (seg.freqHz <= 0.0) {
                // Silence / Pause
                sampleOffset += segSamples
                continue
            }

            for (i in 0 until segSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                // Sine wave with overtone for warmth
                val sine = sin(2.0 * PI * seg.freqHz * t) + 0.25 * sin(4.0 * PI * seg.freqHz * t)

                // Smooth raised cosine envelope (attack and decay) to prevent audio clicks
                val progress = i.toDouble() / segSamples
                val envelope = 0.5 * (1.0 - cos(PI * progress.coerceIn(0.0, 0.1) * 10.0)) *
                        (1.0 - progress)

                val sampleVal = (sine * envelope * seg.amplitude * Short.MAX_VALUE).toInt()
                val bounded = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                if (sampleOffset + i < totalSamples) {
                    result[sampleOffset + i] = bounded
                }
            }
            sampleOffset += segSamples
        }
        return result
    }

    private fun playPcmSamples(samples: ShortArray, volume: Float) {
        val bufferSize = samples.size * 2 // 2 bytes per short
        var track: AudioTrack? = null
        try {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val format = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            track = AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(format)
                .setBufferSizeInBytes(bufferSize.coerceAtLeast(1024))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.setVolume(volume)
            track.write(samples, 0, samples.size)
            track.play()

            // Allow the track to finish before release
            val durationMs = (samples.size * 1000L) / SAMPLE_RATE
            Thread.sleep(durationMs + 60L)
        } catch (e: Exception) {
            Log.w(TAG, "Error during AudioTrack playback: ${e.message}")
        } finally {
            try {
                track?.stop()
                track?.release()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }
}
