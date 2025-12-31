package com.example.denikplus.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import java.time.LocalDate

@Composable
fun DenikPlusApp() {
    var year by remember { mutableIntStateOf(LocalDate.now().year) }

    // MVP úložiště: počet zápisů pro konkrétní den
    val entriesByDate = remember { mutableStateMapOf<LocalDate, Int>() }

    // Demo data (ať hned něco vidíš)
    LaunchedEffect(Unit) {
        val today = LocalDate.now()
        entriesByDate[today] = 1
        entriesByDate[today.minusDays(2)] = 2
        entriesByDate[today.minusDays(10)] = 1
        entriesByDate[today.minusMonths(1).withDayOfMonth(5)] = 1
    }

    var createForDate by remember { mutableStateOf<LocalDate?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { createForDate = LocalDate.now() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nový zápis")
            }
        }
    ) { padding ->
        YearCalendarScreen(
            year = year,
            entriesByDate = entriesByDate,
            onPrevYear = { year -= 1 },
            onNextYear = { year += 1 },
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
                // MVP: jen zvýšíme počet zápisů pro den
                val current = entriesByDate[date] ?: 0
                entriesByDate[date] = current + 1
                createForDate = null

                // Mood zatím jen sbíráme (dáme do DB až v dalším kroku)
                // (mood může být předdefinovaný nebo custom)
            }
        )
    }
}
