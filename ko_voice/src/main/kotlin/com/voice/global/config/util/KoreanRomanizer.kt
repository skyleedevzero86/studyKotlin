package com.voice.domain.global.util

object KoreanRomanizer {
    private val initialConsonants = arrayOf(
        "g", "kk", "n", "d", "tt", "r", "m", "b", "pp", "s",
        "ss", "", "j", "jj", "ch", "k", "t", "p", "h"
    )

    private val medialVowels = arrayOf(
        "a", "ae", "ya", "yae", "eo", "e", "yeo", "ye", "o",
        "wa", "wae", "oe", "yo", "u", "wo", "we", "wi", "yu",
        "eu", "ui", "i"
    )

    private val finalConsonants = arrayOf(
        "", "k", "kk", "ks", "n", "nj", "nh", "t", "l", "lg",
        "lm", "lb", "ls", "lt", "lp", "lh", "m", "p", "bs",
        "s", "ss", "ng", "j", "ch", "k", "t", "p", "h"
    )

    fun containsKorean(text: String): Boolean {
        return text.any { char ->
            char.code in 0xAC00..0xD7A3 || char.code in 0x1100..0x11FF || char.code in 0x3130..0x318F
        }
    }

    private fun getNextChar(text: String, index: Int): Char? {
        return if (index + 1 < text.length) text[index + 1] else null
    }

    fun romanize(text: String): String {
        if (!containsKorean(text)) {
            return text
        }

        val result = StringBuilder()
        var i = 0

        while (i < text.length) {
            val char = text[i]

            if (char.code in 0xAC00..0xD7A3) {
                val code = char.code - 0xAC00
                val initial = code / (21 * 28)
                val medial = (code % (21 * 28)) / 28
                val final = code % 28

                val initialRoman = if (initial < initialConsonants.size) initialConsonants[initial] else ""
                val medialRoman = if (medial < medialVowels.size) medialVowels[medial] else ""
                
                var finalRoman = ""
                if (final > 0 && final < finalConsonants.size) {
                    finalRoman = finalConsonants[final]
                    
                    val nextChar = getNextChar(text, i)
                    if (nextChar != null && nextChar.code in 0xAC00..0xD7A3) {
                        val nextCode = nextChar.code - 0xAC00
                        val nextInitial = nextCode / (21 * 28)
                        
                        if (nextInitial == 0) {
                            when (final) {
                                1, 8, 17 -> finalRoman = "g"
                                3, 7, 20, 24, 25 -> finalRoman = "d"
                                4, 5, 9, 10, 11, 12, 13, 14, 15, 16 -> finalRoman = "n"
                                6 -> finalRoman = "r"
                                18, 19 -> finalRoman = "b"
                                21 -> finalRoman = "ng"
                                22, 23, 26 -> finalRoman = "j"
                                27 -> finalRoman = "h"
                            }
                        }
                    }
                }

                result.append(initialRoman)
                result.append(medialRoman)
                result.append(finalRoman)
            } else {
                result.append(char)
            }

            i++
        }

        return result.toString()
    }

    fun preprocessTextForTTS(text: String): String {
        if (!containsKorean(text)) {
            return text
        }

        val result = StringBuilder()
        val words = text.split(Regex("\\s+"))
        
        for (wordIndex in words.indices) {
            val word = words[wordIndex]
            
            if (containsKorean(word)) {
                val romanized = romanize(word)
                result.append(romanized)
            } else {
                result.append(word)
            }
            
            if (wordIndex < words.size - 1) {
                result.append(" ")
            }
        }

        return result.toString()
    }
}

