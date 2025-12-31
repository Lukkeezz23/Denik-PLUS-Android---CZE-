package com.example.denikplus.data

import com.google.firebase.Timestamp
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class EntryItem(
    val id: String,
    val moodLabel: String,
    val text: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun timeText(): String {
        val ts = createdAt ?: return ""
        val time = ts.toDate()
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalTime()

        return time.format(DateTimeFormatter.ofPattern("HH:mm"))
    }
}
