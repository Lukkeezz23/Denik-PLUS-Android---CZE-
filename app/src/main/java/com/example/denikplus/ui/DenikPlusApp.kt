package com.example.denikplus.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.denikplus.data.EntriesRepository
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DenikPlusApp(
    uid: String,
    onLogout: () -> Unit
) {
    val repo = remember { EntriesRepository() }
    val vm: CalendarViewModel = viewModel(factory = CalendarViewModelFactory(uid, repo))

    val year by vm.year.collectAsState()
    val counts by vm.counts.collectAsState()

    var createForDate by remember { mutableStateOf<LocalDate?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deník Plus") },
                actions = {
                    TextButton(onClick = onLogout) { Text("Odhlásit") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { createForDate = LocalDate.now() }) {
                Icon(Icons.Default.Add, contentDescription = "Nový zápis")
            }
        }
    ) { padding ->
        YearCalendarScreen(
            year = year,
            entriesByDate = counts,
            onPrevYear = { vm.prevYear() },
            onNextYear = { vm.nextYear() },
            onDayClick = { date -> createForDate = date },
            contentPadding = padding
        )
    }

    val date = createForDate
    if (date != null) {
        MoodPickerDialog(
            date = date,
            onDismiss = { createForDate = null },
            onConfirm = { mood ->
                val label = when (mood) {
                    is MoodChoice.Preset -> mood.label
                    is MoodChoice.Custom -> mood.text.ifBlank { "Jinak" }
                }
                vm.addEntry(date, label)
                createForDate = null
            }
        )
    }
}
