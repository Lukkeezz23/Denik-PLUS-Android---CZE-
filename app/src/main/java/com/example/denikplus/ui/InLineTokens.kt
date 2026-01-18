// FILE: ui/InlineTokens.kt
package com.example.denikplus.ui

/**
 * RAW text obsahuje tokeny např.:
 *   [IMG:content://...]
 *   [AUD:content://...]
 *   [MAP:lat,lon|title]
 *   [DET:payload]
 *   [MUS:payload]
 *
 * Tyto tokeny se v editoru vizuálně nahradí inline prvky (chip/miniatura/ikona).
 */
enum class InlineTokenType { IMG, AUD, MAP, DET, MUS }

data class InlineTokenMatch(
    val type: InlineTokenType,
    val payload: String,
    val start: Int,          // inclusive
    val endExclusive: Int    // exclusive
)

// ✅ DŮLEŽITÉ: přidáno DET a MUS
private val TOKEN_REGEX = Regex("""\[(IMG|AUD|MAP|DET|MUS):([^\]]*)\]""")

fun findInlineTokens(raw: String): List<InlineTokenMatch> {
    if (raw.isEmpty()) return emptyList()

    val out = ArrayList<InlineTokenMatch>()
    for (m in TOKEN_REGEX.findAll(raw)) {
        val typeStr = m.groups[1]?.value ?: continue
        val payload = m.groups[2]?.value ?: ""
        val type = when (typeStr.uppercase()) {
            "IMG" -> InlineTokenType.IMG
            "AUD" -> InlineTokenType.AUD
            "MAP" -> InlineTokenType.MAP
            "DET" -> InlineTokenType.DET
            "MUS" -> InlineTokenType.MUS
            else -> continue
        }
        out += InlineTokenMatch(
            type = type,
            payload = payload,
            start = m.range.first,
            endExclusive = m.range.last + 1
        )
    }
    return out
}

fun buildToken(type: InlineTokenType, payload: String): String {
    val t = when (type) {
        InlineTokenType.IMG -> "IMG"
        InlineTokenType.AUD -> "AUD"
        InlineTokenType.MAP -> "MAP"
        InlineTokenType.DET -> "DET"
        InlineTokenType.MUS -> "MUS"
    }
    return "[$t:$payload]"
}
