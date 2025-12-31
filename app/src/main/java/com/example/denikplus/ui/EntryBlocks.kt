package com.example.denikplus.ui

import android.net.Uri
import java.util.UUID

sealed class EntryBlock(open val id: String = UUID.randomUUID().toString()) {
    data class Text(
        override val id: String = UUID.randomUUID().toString(),
        val value: String
    ) : EntryBlock(id)

    data class Image(
        override val id: String = UUID.randomUUID().toString(),
        val uri: Uri
    ) : EntryBlock(id)

    data class Audio(
        override val id: String = UUID.randomUUID().toString(),
        val uri: Uri,
        val title: String = "Hlasovka"
    ) : EntryBlock(id)

    data class Location(
        override val id: String = UUID.randomUUID().toString(),
        val label: String,
        val lat: Double? = null,
        val lon: Double? = null,
        val kind: LocationKind
    ) : EntryBlock(id)

    data class Sketch(
        override val id: String = UUID.randomUUID().toString(),
        val placeholder: String = "Náčrt (zatím placeholder)"
    ) : EntryBlock(id)

    data class Music(
        override val id: String = UUID.randomUUID().toString(),
        val placeholder: String = "Hudba (zatím placeholder)"
    ) : EntryBlock(id)
}

enum class LocationKind { PLANNED_TRIP, PAST_TIMELINE }
