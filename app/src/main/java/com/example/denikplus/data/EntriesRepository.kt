// FILE: data/EntriesRepository.kt
package com.example.denikplus.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.LocalDate

class EntriesRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun entriesCol(uid: String) =
        db.collection("users")
            .document(uid)
            .collection("entries")

    /**
     * DŮLEŽITÉ:
     * Nepoužíváme orderBy(createdAt) + whereEqualTo(dayKey), protože to vyžaduje composite index.
     * Seřazení uděláme lokálně (podle createdAt desc).
     */
    fun observeDayEntries(
        uid: String,
        date: LocalDate
    ): Flow<List<EntryItem>> = callbackFlow {

        val dayKey = date.year * 10000 + date.monthValue * 100 + date.dayOfMonth

        val reg = entriesCol(uid)
            .whereEqualTo("dayKey", dayKey)
            .addSnapshotListener { snap, err ->

                if (err != null || snap == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val items = snap.documents.map { d ->
                    EntryItem(
                        id = d.id,
                        moodLabel = d.getString("moodLabel") ?: "",
                        text = d.getString("text") ?: "",
                        createdAt = d.getTimestamp("createdAt"),
                        updatedAt = d.getTimestamp("updatedAt")
                    )
                }.sortedWith(compareByDescending<EntryItem> { it.createdAt?.seconds ?: 0L }
                    .thenByDescending { it.createdAt?.nanoseconds ?: 0 })

                trySend(items)
            }

        awaitClose { reg.remove() }
    }

    fun observeYearCounts(
        uid: String,
        year: Int
    ): Flow<Map<LocalDate, Int>> = callbackFlow {

        val startKey = year * 10000 + 101
        val endKey = year * 10000 + 1231

        val reg = entriesCol(uid)
            .whereGreaterThanOrEqualTo("dayKey", startKey)
            .whereLessThanOrEqualTo("dayKey", endKey)
            .addSnapshotListener { snap, err ->

                if (err != null || snap == null) {
                    trySend(emptyMap())
                    return@addSnapshotListener
                }

                val map = HashMap<LocalDate, Int>()
                for (d in snap.documents) {
                    val key = d.getLong("dayKey") ?: continue
                    val y = (key / 10000).toInt()
                    val m = ((key / 100) % 100).toInt()
                    val day = (key % 100).toInt()

                    val dt = LocalDate.of(y, m, day)
                    map[dt] = (map[dt] ?: 0) + 1
                }

                trySend(map)
            }

        awaitClose { reg.remove() }
    }

    fun addEntry(
        uid: String,
        date: LocalDate,
        moodLabel: String,
        text: String = ""
    ) {
        val dayKey = date.year * 10000 + date.monthValue * 100 + date.dayOfMonth

        val data = hashMapOf(
            "dayKey" to dayKey,
            "year" to date.year,
            "month" to date.monthValue,
            "day" to date.dayOfMonth,
            "moodLabel" to moodLabel,
            "text" to text,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        entriesCol(uid).add(data)
    }

    fun updateEntry(
        uid: String,
        entryId: String,
        moodLabel: String,
        text: String
    ) {
        entriesCol(uid)
            .document(entryId)
            .update(
                mapOf(
                    "moodLabel" to moodLabel,
                    "text" to text,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
    }

    fun deleteEntry(uid: String, entryId: String) {
        entriesCol(uid).document(entryId).delete()
    }
}
