package com.example.denikplus.data

import com.google.firebase.Timestamp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class EntryItem(
    val id: String,
    val moodLabel: String,
    val text: String,
    val createdAt: Timestamp?
) {
    fun timeText(): String {
        val ts = createdAt ?: return ""
        val instant: Instant = ts.toDate().toInstant()
        val localTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
        return localTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    }
}
