package com.morsify.service

import android.content.Context
import com.morsify.data.MorseCode
import com.morsify.data.OutputMode
import com.morsify.data.TimingProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Orchestrates a Morse transmission. Sends each symbol as a unit-length ON pulse
 * followed by OFF gaps, using the device's torch and/or tone.
 *
 * Cancellation is cooperative: a new [transmit] cancels the previous job and
 * releases any hardware it held. UI must call [stop] to fully release the torch.
 */
class Transmitter(context: Context) {

    private val flasher = Flasher(context)
    private val beeper = Beeper(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var currentJob: Job? = null

    val hasFlash: Boolean get() = flasher.isAvailable

    /**
     * Transmit the encoded morse sequence.
     */
    fun transmit(
        encoded: MorseCode.Encoded,
        timing: TimingProfile,
        mode: OutputMode,
        onProgress: (Progress) -> Unit = {},
        onComplete: () -> Unit = {}
    ) {
        currentJob?.cancel()
        currentJob = scope.launch {
            try {
                runTransmit(encoded, timing, mode, onProgress)
                // When successfully finished, call onComplete on the main thread
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } finally {
                flasher.release()
            }
        }
    }

    fun stop() {
        currentJob?.cancel()
        currentJob = null
        flasher.release()
    }

    fun shutdown() {
        stop()
        beeper.release()
        scope.cancel()
    }

    private suspend fun runTransmit(
        encoded: MorseCode.Encoded,
        timing: TimingProfile,
        mode: OutputMode,
        onProgress: (Progress) -> Unit
    ) {
        val tokens = encoded.tokens
        val total = tokens.sumOf { it.symbols.size.coerceAtLeast(1) }
        var played = 0

        for ((charIdx, seq) in tokens.withIndex()) {
            if (!isActiveJob()) return

            if (seq.isWordGap) {
                // Word gap = 7 units OFF
                onProgress(Progress(played, total, charIdx, tokens.size, Transmitter.SymKind.WORD_GAP, false))
                delay(timing.offWordMs)
                played += 1
                onProgress(Progress(played, total, charIdx, tokens.size, null, false))
                continue
            }

            val syms = seq.symbols
            for ((symIdx, sym) in syms.withIndex()) {
                if (!isActiveJob()) return
                val onMs = if (sym is MorseCode.Symbol.Dash) timing.onMs * 3 else timing.onMs

                // ON: flash + beep + mark played
                val isDash = sym is MorseCode.Symbol.Dash
                playOn(onMs, mode, isDash)
                played += 1
                onProgress(Progress(
                    played,
                    total,
                    charIdx,
                    tokens.size,
                    SymKind.from(sym),
                    true
                ))

                // OFF: intra-character gap
                playOffInternal(timing.offIntraMs, mode)
                onProgress(Progress(
                    played,
                    total,
                    charIdx,
                    tokens.size,
                    SymKind.from(sym),
                    false
                ))
            }
            // Inter-character gap: 3 units total OFF; last symbol already got 1t intra
            val nextSeq = tokens.getOrNull(charIdx + 1)
            if (nextSeq != null && !nextSeq.isWordGap) {
                delay(timing.offInterMs - timing.offIntraMs)
            }
        }
        // Correct completion index to prevent index out of bounds rendering
        onProgress(Progress(total, total, (tokens.size - 1).coerceAtLeast(0), tokens.size, null, false))
    }

    /** Returns true if the current job is still active, false if cancelled. */
    private fun isActiveJob(): Boolean = currentJob?.isActive ?: false

    private suspend fun playOn(ms: Long, mode: OutputMode, isDash: Boolean) {
        withContext(Dispatchers.IO) {
            if (mode.usesFlash()) flasher.on()
        }
        if (mode.usesSound()) {
            // Write audio tone synchronously, which blocks for precisely `ms` milliseconds
            withContext(Dispatchers.IO) {
                beeper.playTone(ms)
            }
        } else {
            delay(ms)
        }
        withContext(Dispatchers.IO) {
            if (mode.usesFlash()) flasher.off()
        }
    }

    private suspend fun playOffInternal(ms: Long, mode: OutputMode) {
        delay(ms)
    }

    data class Progress(
        val played: Int,
        val total: Int,
        val charIndex: Int,
        val charTotal: Int,
        val lastSymbol: SymKind?,
        val isOn: Boolean
    )

    enum class SymKind { DOT, DASH, WORD_GAP, NONE;
        companion object {
            fun from(s: MorseCode.Symbol?): SymKind = when (s) {
                is MorseCode.Symbol.Dot -> DOT
                is MorseCode.Symbol.Dash -> DASH
                is MorseCode.Symbol.WordGap -> WORD_GAP
                else -> NONE
            }
        }
    }
}
