package com.faendir.zachtronics.bot.model.om

enum class OmScorePart(val key: Char) {
    HEIGHT('h'), WIDTH('w'), COST('g'), CYCLES('c'), AREA('a'), INSTRUCTIONS('i'), AREA_OR_INSTRUCTIONS('?'), COMPUTED('#');

    companion object {
        fun parse(string: String): Pair<OmScorePart, Double>? {
            if (string.isEmpty()) return null
            val part = string.last().let { key -> values().find { key.equals(it.key, ignoreCase = true) } } ?: return null
            val number = string.substring(0, string.length - 1).toDoubleOrNull() ?: return null
            return part to number
        }
    }
}