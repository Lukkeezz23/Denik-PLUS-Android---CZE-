package com.example.denikplus.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    prefs: AppPrefs,
    onLogout: () -> Unit
) {
    val repo = remember { EntriesRepository() }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var month by remember { mutableStateOf(YearMonth.from(selectedDate)) }

    var showDaySheet by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<EntryItem?>(null) }
    var viewingEntry by remember { mutableStateOf<EntryItem?>(null) } // ✅ přehled
    var creatingNew by remember { mutableStateOf(false) }

    var showSettings by remember { mutableStateOf(false) }

    var showFeelingDialog by remember { mutableStateOf(false) }
    var showDetailsQuestion by remember { mutableStateOf(false) }
    var showDetailsPicker by remember { mutableStateOf(false) }

    var pendingMood by remember { mutableStateOf("🙂") }
    var pendingDetails by remember { mutableStateOf<List<DetailSelection>>(emptyList()) }

    fun startNewEntryFlow() {
        showDaySheet = false

        pendingMood = "🙂"
        pendingDetails = emptyList()
        creatingNew = false
        editingEntry = null
        viewingEntry = null
        showFeelingDialog = true
        showDetailsQuestion = false
        showDetailsPicker = false
    }

    val dayEntries by repo.observeDayEntries(uid, selectedDate)
        .collectAsState(initial = emptyList())

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
                    }) { Icon(Icons.Default.Today, contentDescription = "Dnes") }

                    IconButton(onClick = { startNewEntryFlow() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Nový zápis")
                    }

                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Nastavení")
                    }

                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Odhlásit")
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
                onOpenDayEntries = {
                    if (editingEntry == null && creatingNew == false && viewingEntry == null &&
                        !showFeelingDialog && !showDetailsQuestion && !showDetailsPicker
                    ) {
                        showDaySheet = true
                    } else {
                        showDaySheet = false
                    }
                },
                onQuickAddEntry = { d ->
                    selectedDate = d
                    month = YearMonth.from(d)
                    startNewEntryFlow()
                }
            )
        }
    }

    if (showSettings) {
        SettingsDialog(
            prefs = prefs,
            onDismiss = { showSettings = false }
        )
    }

    if (showDaySheet) {
        DayEntriesSheet(
            date = selectedDate,
            entries = dayEntries,
            onAddClick = {
                startNewEntryFlow()
            },
            onOpen = { e ->
                showDaySheet = false
                editingEntry = null
                creatingNew = false
                viewingEntry = e
            },
            onEdit = { e ->
                showDaySheet = false
                viewingEntry = null
                creatingNew = false
                editingEntry = e
            },
            onDelete = { e -> repo.deleteEntry(uid, e.id) },
            onDismiss = { showDaySheet = false }
        )
    }


    val view = viewingEntry
    if (view != null) {
        EntryEditorDialog(
            date = selectedDate,
            dialogTitle = "Přehled zápisu",
            initialEntryTitle = view.title.ifBlank { "Zápis" },
            moodLabel = view.moodLabel,
            initialText = view.text,
            initialDetails = view.details,
            readOnly = true,
            onDismiss = { viewingEntry = null },
            onConfirm = { _, _, _, _ -> }
        )
    }

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
                creatingNew = true
            }
        )
    }

    // ✅ Nový zápis
    if (creatingNew) {
        EntryEditorDialog(
            date = selectedDate,
            dialogTitle = "Nový zápis",
            initialEntryTitle = "Zápis",
            moodLabel = pendingMood,
            initialText = "",
            initialDetails = pendingDetails,
            onDismiss = { creatingNew = false },
            onConfirm = { title, mood, text, details ->
                val safeTitle = title.trim().ifBlank { "Zápis" }
                repo.addEntry(
                    uid = uid,
                    date = selectedDate,
                    title = safeTitle,
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

    // ✅ Editace
    val edit = editingEntry
    if (edit != null) {
        EntryEditorDialog(
            date = selectedDate,
            dialogTitle = "Upravit zápis",
            initialEntryTitle = edit.title.ifBlank { "Zápis" },
            moodLabel = edit.moodLabel,
            initialText = edit.text,
            initialDetails = edit.details,
            onDismiss = { editingEntry = null },
            onConfirm = { title, mood, text, details ->
                val safeTitle = title.trim().ifBlank { "Zápis" }
                repo.updateEntry(
                    uid = uid,
                    entryId = edit.id,
                    title = safeTitle,
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
