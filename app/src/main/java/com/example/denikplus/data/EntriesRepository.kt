// FILE: data/EntriesRepository.kt
package com.example.denikplus.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class EntriesRepository {

    private val db = FirebaseFirestore.getInstance()
    private val dayKeyFmt = DateTimeFormatter.BASIC_ISO_DATE // yyyyMMdd

    private fun entriesCol(uid: String) =
        db.collection("users").document(uid).collection("entries")

    private fun dayKey(date: LocalDate): String = date.format(dayKeyFmt)

    fun observeDayEntries(uid: String, date: LocalDate): Flow<List<EntryItem>> = callbackFlow {
        val key = dayKey(date)

        // ✅ Bez orderBy => NEPOTŘEBUJE composite index
        val q = entriesCol(uid)
            .whereEqualTo("dayKey", key)

        val reg = q.addSnapshotListener { snap, err ->
            if (err != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }

            val list = snap?.documents?.mapNotNull { doc ->
                val mood = doc.getString("moodLabel") ?: "🙂"
                val text = doc.getString("text") ?: ""
                val createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L
                val details = parseDetails(doc.get("details"))

                // ✅ title musí být tady, kde máme doc + text
                val title = doc.getString("title")?.trim().orEmpty().ifBlank {
                    val t = text.trim().lineSequence().firstOrNull().orEmpty()
                    if (t.isBlank()) "Zápis" else t.take(40)
                }

                EntryItem(
                    id = doc.id,
                    title = title,
                    moodLabel = mood,
                    text = text,
                    createdAt = createdAt,
                    details = details
                )
            }.orEmpty()
                .sortedByDescending { it.createdAt } // ✅ seřazení lokálně

            trySend(list)
        }

        awaitClose { reg.remove() }
    }

    fun observeMonthCounts(uid: String, month: YearMonth): Flow<Map<LocalDate, Int>> = callbackFlow {
        val start = month.atDay(1)
        val end = month.atEndOfMonth()
        val startKey = dayKey(start)
        val endKey = dayKey(end)

        val q = entriesCol(uid)
            .whereGreaterThanOrEqualTo("dayKey", startKey)
            .whereLessThanOrEqualTo("dayKey", endKey)

        val reg = q.addSnapshotListener { snap, err ->
            if (err != null) {
                trySend(emptyMap())
                return@addSnapshotListener
            }

            val counts = HashMap<LocalDate, Int>()
            snap?.documents?.forEach { doc ->
                val k = doc.getString("dayKey") ?: return@forEach
                val d = runCatching { LocalDate.parse(k, dayKeyFmt) }.getOrNull() ?: return@forEach
                counts[d] = (counts[d] ?: 0) + 1
            }

            trySend(counts)
        }

        awaitClose { reg.remove() }
    }

    fun addEntry(
        uid: String,
        date: LocalDate,
        title: String,
        moodLabel: String,
        text: String,
        details: List<DetailSelection> = emptyList()
    ) {
        val data = hashMapOf<String, Any>(
            "title" to title,
            "dayKey" to dayKey(date),
            "moodLabel" to moodLabel,
            "text" to text,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        if (details.isNotEmpty()) {
            data["details"] = encodeDetails(details)
        }

        entriesCol(uid).add(data)
    }

    fun updateEntry(
        uid: String,
        entryId: String,
        title: String,
        moodLabel: String,
        text: String,
        details: List<DetailSelection>? = null // null = neměň detaily
    ) {
        val upd = hashMapOf<String, Any>(
            "title" to title,
            "moodLabel" to moodLabel,
            "text" to text,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        if (details != null) {
            upd["details"] = encodeDetails(details)
        }

        entriesCol(uid).document(entryId).update(upd)
    }

    fun deleteEntry(uid: String, entryId: String) {
        entriesCol(uid).document(entryId).delete()
    }

    // ---------- helpers ----------

    private fun encodeDetails(details: List<DetailSelection>): List<Map<String, Any>> =
        details.map {
            mapOf(
                "categoryId" to it.categoryId,
                "itemId" to it.itemId,
                "itemTitle" to it.itemTitle,
                "note" to it.note
            )
        }

    private fun parseDetails(raw: Any?): List<DetailSelection> {
        val list = raw as? List<*> ?: return emptyList()

        return list.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            val categoryId = m["categoryId"] as? String ?: return@mapNotNull null
            val itemId = m["itemId"] as? String ?: return@mapNotNull null
            val itemTitle = m["itemTitle"] as? String ?: ""
            val note = m["note"] as? String ?: ""
            DetailSelection(
                categoryId = categoryId,
                itemId = itemId,
                itemTitle = itemTitle,
                note = note
            )
        }
    }
}
