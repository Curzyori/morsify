package com.morsify.data

/**
 * International Morse code mapping (ITU-R M.1677-1).
 * Supports A-Z, 0-9, and a curated set of punctuation.
 *
 * Pro-signs (e.g. <AR>, <SK>) are exposed via [prosign] but not auto-inserted.
 */
object MorseCode {

    private val table: Map<Char, String> = mapOf(
        // Letters
        'A' to ".-",     'B' to "-...",   'C' to "-.-.",   'D' to "-..",
        'E' to ".",      'F' to "..-.",   'G' to "--.",    'H' to "....",
        'I' to "..",     'J' to ".---",   'K' to "-.-",    'L' to ".-..",
        'M' to "--",     'N' to "-.",     'O' to "---",    'P' to ".--.",
        'Q' to "--.-",   'R' to ".-.",    'S' to "...",    'T' to "-",
        'U' to "..-",    'V' to "...-",   'W' to ".--",    'X' to "-..-",
        'Y' to "-.--",   'Z' to "--..",
        // Digits
        '0' to "-----",  '1' to ".----",  '2' to "..---",  '3' to "...--",
        '4' to "....-",  '5' to ".....",  '6' to "-....",  '7' to "--...",
        '8' to "---..",  '9' to "----.",
        // Punctuation
        '.' to ".-.-.-", ',' to "--..--", '?' to "..--..", '\'' to ".----.",
        '!' to "-.-.--", '/' to "-..-.",  '(' to "-.--.",   ')' to "-.--.-",
        '&' to ".-...",  ':' to "---...", ';' to "-.-.-.", '=' to "-...-",
        '+' to ".-.-.",  '-' to "-....-", '_' to "..--.-", '"' to ".-..-.",
        '$' to "...-..-", '@' to ".--.-.",
        // Space is rendered as / between words
        ' ' to "/"
    )

    fun encodeChar(c: Char): String? = table[c.uppercaseChar()]

    /**
     * Encode full text. Returns Pair:
     *  - first: human-readable morse (with / for spaces, spaces between letters)
     *  - second: list of token groups, each group is a list of (Dot|Dash) elements
     *    that make up a single character. Word boundaries are encoded as null entries.
     */
    data class Encoded(
        val readable: String,
        val tokens: List<SymbolSequence>,
        val unsupported: List<Char> = emptyList()
    )

    sealed class Symbol {
        data object Dot : Symbol()
        data object Dash : Symbol()
        data object WordGap : Symbol()
    }

    data class SymbolSequence(
        val char: Char?,
        val symbols: List<Symbol>
    ) {
        val isWordGap: Boolean get() = symbols.isEmpty() && char == null
    }

    fun encode(text: String): Encoded {
        val tokens = ArrayList<SymbolSequence>()
        val readable = StringBuilder()
        val unsupported = ArrayList<Char>()

        for ((idx, raw) in text.withIndex()) {
            val c = raw.uppercaseChar()
            val code = table[c]
            if (code == null) {
                unsupported.add(c)
                continue
            }
            val syms = code.map {
                when (it) {
                    '.' -> Symbol.Dot
                    '-' -> Symbol.Dash
                    else -> null
                }
            }.filterNotNull()
            tokens.add(SymbolSequence(c, syms))
            if (readable.isNotEmpty() && c != ' ') readable.append(' ')
            readable.append(code)
        }
        return Encoded(readable.toString(), tokens, unsupported)
    }

    fun isSupported(c: Char): Boolean = table.containsKey(c.uppercaseChar()) || c == ' '
}
