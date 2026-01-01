// FILE: ui/DenikPlusApp.kt
package com.example.denikplus.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.denikplus.data.AppPrefs
import com.example.denikplus.data.DetailSelection
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
    val context = LocalContext.current
    val repo = remember { EntriesRepository() }
    val prefs = remember { AppPrefs(context.applicationContext) }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var month by remember { mutableStateOf(YearMonth.from(selectedDate)) }

    var showDaySheet by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<EntryItem?>(null) }
    var creatingNew by remember { mutableStateOf(false) }

    var showSettings by remember { mutableStateOf(false) }

    // --- NEW ENTRY FLOW states ---
    var showFeelingDialog by remember { mutableStateOf(false) }
    var showDetailsQuestion by remember { mutableStateOf(false) }
    var showDetailsPicker by remember { mutableStateOf(false) }

    var pendingMood by remember { mutableStateOf("🙂") }
    var pendingDetails by remember { mutableStateOf<List<DetailSelection>>(emptyList()) }

    fun startNewEntryFlow() {
        // reset + start flow
        pendingMood = "🙂"
        pendingDetails = emptyList()

        // close other related things (jistota)
        creatingNew = false
        editingEntry = null

        showFeelingDialog = true
        showDetailsQuestion = false
        showDetailsPicker = false
    }

    // list pro vybraný den
    val dayEntries by repo.observeDayEntries(uid, selectedDate)
        .collectAsState(initial = emptyList())

    // counts pro aktuální měsíc
    val monthCounts by repo.observeMonthCounts(uid, month)
        .collectAsState(initial = emptyMap())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deník Plus") },
                actions = {
                    IconButton(onClick = {
                        val today = LocalDate.now()
                        selectedDate = today
                        month = YearMonth.from(today)
                    }) {
                        Icon(Icons.Default.Today, contentDescription = "Dnes")
                    }

                    IconButton(onClick = { startNewEntryFlow() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Nový zápis")
                    }

                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Nastavení")
                    }

                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Odhlásit")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            MonthCalendarScreen(
                month = month,
                selectedDate = selectedDate,
                counts = monthCounts,
                onSelectDate = { d ->
                    selectedDate = d
                    month = YearMonth.from(d)
                },
                onPrevMonth = {
                    val newMonth = month.minusMonths(1)
                    month = newMonth
                    selectedDate = selectedDate.safeWithMonth(newMonth)
                },
                onNextMonth = {
                    val newMonth = month.plusMonths(1)
                    month = newMonth
                    selectedDate = selectedDate.safeWithMonth(newMonth)
                },

                onOpenDayEntries = { showDaySheet = true },
                onQuickAddEntry = { d ->
                    selectedDate = d
                    month = YearMonth.from(d)
                    startNewEntryFlow()
                }
            )
        }
    }

    // Settings dialog
    if (showSettings) {
        SettingsDialog(
            prefs = prefs,
            onDismiss = { showSettings = false }
        )
    }

    // Bottom sheet se zápisy dne
    if (showDaySheet) {
        DayEntriesSheet(
            date = selectedDate,
            entries = dayEntries,
            onAddClick = {
                startNewEntryFlow()
                // volitelně můžeš sheet zavřít:
                // showDaySheet = false
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

    // 1) Feeling dialog
    if (showFeelingDialog) {
        FeelingPickerDialog(
            onSkip = {
                showFeelingDialog = false
                showDetailsQuestion = true
            },
            onPick = { opt ->
                pendingMood = opt.emoji
                showFeelingDialog = false
                showDetailsQuestion = true
            },
            onDismiss = { showFeelingDialog = false }
        )
    }

    // 2) Question: expand details?
    if (showDetailsQuestion) {
        ExpandDetailsDialog(
            onNo = {
                showDetailsQuestion = false
                creatingNew = true
            },
            onYes = {
                showDetailsQuestion = false
                showDetailsPicker = true
            },
            onDismiss = { showDetailsQuestion = false }
        )
    }

    // 3) Details picker sheet
    if (showDetailsPicker) {
        val categories = remember { defaultDetailCategories() }
        DetailsPickerSheet(
            categories = categories,
            initial = pendingDetails,
            onConfirm = { picked ->
                pendingDetails = picked
                showDetailsPicker = false
                creatingNew = true
            },
            onDismiss = {
                showDetailsPicker = false
                // když uživatel sheet zavře, půjdeme rovnou do editoru bez detailů
                creatingNew = true
            }
        )
    }

    // --- Editor: nový zápis ---
    if (creatingNew) {
        EntryEditorDialog(
            date = selectedDate,
            title = "Nový zápis",
            moodLabel = pendingMood,
            initialText = "",
            initialDetails = pendingDetails,
            onDismiss = { creatingNew = false },
            onConfirm = { mood, text, details ->
                repo.addEntry(
                    uid = uid,
                    date = selectedDate,
                    moodLabel = mood,
                    text = text,
                    details = details
                )
                creatingNew = false
                pendingMood = "🙂"
                pendingDetails = emptyList()
            }
        )
    }

    // --- Editor: úprava ---
    val edit = editingEntry
    if (edit != null) {
        EntryEditorDialog(
            date = selectedDate,
            title = "Upravit zápis",
            moodLabel = edit.moodLabel,
            initialText = edit.text,
            initialDetails = edit.details,
            onDismiss = { editingEntry = null },
            onConfirm = { mood, text, details ->
                repo.updateEntry(
                    uid = uid,
                    entryId = edit.id,
                    moodLabel = mood,
                    text = text,
                    details = details
                )
                editingEntry = null
            }
        )
    }
}

private fun LocalDate.safeWithMonth(newMonth: YearMonth): LocalDate {
    val safeDay = min(this.dayOfMonth, newMonth.lengthOfMonth())
    return LocalDate.of(newMonth.year, newMonth.monthValue, safeDay)
}
