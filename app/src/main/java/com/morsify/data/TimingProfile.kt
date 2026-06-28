package com.morsify.data

/**
 * Timing profile for Morse playback. Based on the standard unit length `t`:
 *  - Dot: 1 unit on
 *  - Dash: 3 units on
 *  - Intra-character gap: 1 unit off
 *  - Inter-character gap: 3 units off
 *  - Word gap: 7 units off
 *
 * `unitMs` is the duration of one "t" in milliseconds. A normal CW speed is
 * ~20 WPM which corresponds to t ≈ 60 ms.
 */
data class TimingProfile(
    val unitMs: Long,
    val onMs: Long = unitMs,
    val offIntraMs: Long = unitMs,
    val offInterMs: Long = unitMs * 3,
    val offWordMs: Long = unitMs * 7
) {
    companion object {
        val SLOW   = TimingProfile(unitMs = 240)  // t=240ms → 5 WPM (very deliberate)
        val NORMAL = TimingProfile(unitMs = 120)  // t=120ms → ~10 WPM
        val FAST   = TimingProfile(unitMs = 60)   // t=60ms  → ~20 WPM (CW)

        /**
         * Translate a slider value (0..100) into a unit duration. Lower slider = slower.
         * Progress 67 maps to NORMAL (120ms), 33 maps to midpoint (180ms).
         */
        fun fromProgress(progress: Int): TimingProfile {
            val clamped = progress.coerceIn(0, 100)
            // Unit ms: 240 (slow) at 0, 60 (fast) at 100
            val unit = (240 - (clamped * 180 / 100)).toLong()
            return TimingProfile(unit)
        }
    }
}

enum class OutputMode {
    FLASH_ONLY, SOUND_ONLY, BOTH;

    fun usesFlash() = this == FLASH_ONLY || this == BOTH
    fun usesSound() = this == SOUND_ONLY || this == BOTH
}
