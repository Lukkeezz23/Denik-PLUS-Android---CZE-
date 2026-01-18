// FILE: data/EntryItem.kt
package com.example.denikplus.data

import com.google.firebase.Timestamp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class EntryItem(
    val id: String,
    val title: String = "Zápis",
    val moodLabel: String,
    val text: String = "",
    val createdAt: Long,
    val updatedAt: Timestamp? = null,
    val details: List<DetailSelection> = emptyList()
) {
    fun timeText(): String {
        return Instant.ofEpochMilli(createdAt)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(DateTimeFormatter.ofPattern("HH:mm"))
    }
}
