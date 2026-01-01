// FILE: ui/DenikPlusApp.kt
package com.example.denikplus.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.denikplus.data.EntriesRepository
import com.example.denikplus.data.EntryItem
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DenikPlusApp(
    uid: String,
    onLogout: () -> Unit
) {
    val repo = remember { EntriesRepository() }

    // vybraný den + aktuální rok (kalendář je podle roku)
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var year by remember { mutableStateOf(selectedDate.year) }

    // sheet + editor
    var showDaySheet by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<EntryItem?>(null) }
    var creatingNew by remember { mutableStateOf(false) }

    // zápisy aktuálně vybraného dne
    val dayEntries by repo.observeDayEntries(uid, selectedDate)
        .collectAsState(initial = emptyList())

    // count map pro kalendář – zatím bezpečně prázdné (kalendář je plně funkční i bez badge)
    // Pokud máš/uděláš repo.observeYearCounts(uid, year), sem to napojíme.
    val counts = remember(year) { emptyMap<LocalDate, Int>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deník Plus") },
                actions = {
                    // skočit na dnešek + správný rok
                    IconButton(onClick = {
                        val today = LocalDate.now()
                        year = today.year
                        selectedDate = today
                    }) {
                        Icon(Icons.Default.Today, contentDescription = "Dnes")
                    }

                    // nový zápis
                    IconButton(onClick = {
                        creatingNew = true
                        editingEntry = null
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Nový zápis")
                    }

                    // logout
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Odhlásit")
                    }
                }
            )
        }
    ) { padding ->

        // ✅ TADY je ten “kompletní kalendář se scrollováním měsíců”
        YearCalendarScreen(
            year = year,
            selectedDate = selectedDate,
            counts = counts,
            onSelectDate = { d -> selectedDate = d },
            onPrevYear = {
                val newYear = year - 1
                year = newYear
                selectedDate = selectedDate.safeWithYear(newYear)
            },
            onNextYear = {
                val newYear = year + 1
                year = newYear
                selectedDate = selectedDate.safeWithYear(newYear)
            },
            onOpenDayEntries = { showDaySheet = true }
        )
    }

    // --- Bottom sheet se zápisy dne ---
    if (showDaySheet) {
        DayEntriesSheet(
            date = selectedDate,
            entries = dayEntries,
            onAddClick = {
                creatingNew = true
                editingEntry = null
            },
            onEdit = { e ->
                editingEntry = e
                creatingNew = false
            },
            onDelete = { e ->
                repo.deleteEntry(uid, e.id)
            },
            onDismiss = { showDaySheet = false }
        )
    }

    // --- Editor: nový zápis ---
    if (creatingNew) {
        EntryEditorDialog(
            date = selectedDate,
            title = "Nový zápis",
            moodLabel = "🙂",
            initialText = "",
            onDismiss = { creatingNew = false },
            onConfirm = { mood, text ->
                repo.addEntry(
                    uid = uid,
                    date = selectedDate,
                    moodLabel = mood,
                    text = text
                )
                creatingNew = false
                showDaySheet = true
            }
        )
    }

    // --- Editor: úprava zápisu ---
    val edit = editingEntry
    if (edit != null) {
        EntryEditorDialog(
            date = selectedDate,
            title = "Upravit zápis",
            moodLabel = edit.moodLabel,
            initialText = edit.text,
            onDismiss = { editingEntry = null },
            onConfirm = { mood, text ->
                repo.updateEntry(
                    uid = uid,
                    entryId = edit.id,
                    moodLabel = mood,
                    text = text
                )
                editingEntry = null
                showDaySheet = true
            }
        )
    }
}

/**
 * Bezpečná změna roku (kvůli 29.2., 31. v měsíci atd.)
 */
private fun LocalDate.safeWithYear(newYear: Int): LocalDate {
    val ym = YearMonth.of(newYear, this.month)
    val safeDay = min(this.dayOfMonth, ym.lengthOfMonth())
    return LocalDate.of(newYear, this.month, safeDay)
}
