package com.morsify.service

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import kotlin.math.sin

/**
 * Plays Morse audio signals dynamically by writing 800Hz sine wave samples to AudioTrack.
 * Extremely low latency, perfectly synchronized with code delay timings.
 */
class Beeper(private val context: Context) {
    private val sampleRate = 8000 // 8kHz is very efficient for single frequency 800Hz
    private var audioTrack: AudioTrack? = null
    private val lock = Any()

    init {
        try {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufSize.coerceAtLeast(2048),
                AudioTrack.MODE_STREAM
            )
            audioTrack?.play()
        } catch (e: Exception) {
            Log.e("Morsify.Beeper", "AudioTrack init failed: ${e.message}")
        }
    }

    /**
     * Plays a pure 800Hz sine wave tone for exactly [durationMs].
     * Blocks during write to ensure precise scheduling without audio leaks.
     */
    fun playTone(durationMs: Long) {
        synchronized(lock) {
            val track = audioTrack ?: return
            try {
                if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                    track.play()
                }
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                if (numSamples <= 0) return
                
                val sample = ShortArray(numSamples)
                val freq = 800.0
                
                // Apply a quick 5ms fade-in and fade-out to prevent "pops/clicks"
                val fadeSamples = (sampleRate * 0.005).toInt().coerceAtMost(numSamples / 2)
                
                for (i in 0 until numSamples) {
                    val angle = 2.0 * Math.PI * i / (sampleRate / freq)
                    var volumeScale = 1.0
                    
                    if (i < fadeSamples) {
                        volumeScale = i.toDouble() / fadeSamples
                    } else if (i > numSamples - fadeSamples) {
                        volumeScale = (numSamples - i).toDouble() / fadeSamples
                    }
                    
                    sample[i] = (sin(angle) * Short.MAX_VALUE * 0.8 * volumeScale).toInt().toShort()
                }
                track.write(sample, 0, numSamples)
            } catch (e: Exception) {
                Log.w("Morsify.Beeper", "playTone failed: ${e.message}")
            }
        }
    }

    fun release() {
        synchronized(lock) {
            try {
                audioTrack?.stop()
                audioTrack?.release()
            } catch (e: Exception) {
                // ignore
            } finally {
                audioTrack = null
            }
        }
    }
}
