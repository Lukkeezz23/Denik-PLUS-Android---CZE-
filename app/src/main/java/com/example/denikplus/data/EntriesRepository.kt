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
        db.collection("users").document(uid).collection("entries")

    fun observeYearCounts(uid: String, year: Int): Flow<Map<LocalDate, Int>> = callbackFlow {
        val startKey = year * 10000 + 101      // yyyy0101
        val endKey = year * 10000 + 1231       // yyyy1231

        val reg = entriesCol(uid)
            .whereGreaterThanOrEqualTo("dayKey", startKey)
            .whereLessThanOrEqualTo("dayKey", endKey)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(emptyMap())
                    return@addSnapshotListener
                }

                val counts = HashMap<LocalDate, Int>()
                val docs = snap?.documents.orEmpty()

                for (d in docs) {
                    val dayKey = d.getLong("dayKey")?.toInt() ?: continue
                    val y = dayKey / 10000
                    val m = (dayKey / 100) % 100
                    val day = dayKey % 100
                    val date = LocalDate.of(y, m, day)
                    counts[date] = (counts[date] ?: 0) + 1
                }

                trySend(counts)
            }

        awaitClose { reg.remove() }
    }

    fun addEntry(uid: String, date: LocalDate, moodLabel: String, text: String = "") {
        val dayKey = date.year * 10000 + date.monthValue * 100 + date.dayOfMonth

        val data = hashMapOf(
            "dayKey" to dayKey,
            "year" to date.year,
            "month" to date.monthValue,
            "day" to date.dayOfMonth,
            "moodLabel" to moodLabel,
            "text" to text,
            "createdAt" to FieldValue.serverTimestamp()
        )

        entriesCol(uid).add(data)
    }
}
