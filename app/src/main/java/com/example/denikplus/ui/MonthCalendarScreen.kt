// FILE: ui/MonthCalendarScreen.kt
package com.example.denikplus.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthCalendarScreen(
    month: YearMonth,
    selectedDate: LocalDate,
    counts: Map<LocalDate, Int>,
    onSelectDate: (LocalDate) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onOpenDayEntries: () -> Unit,
    onQuickAddEntry: (LocalDate) -> Unit, // ✅ FIX: teď bere LocalDate
) {
    val locale = Locale.getDefault()
    val monthTitle = month.month.getDisplayName(TextStyle.FULL, locale)
        .replaceFirstChar { it.titlecase(locale) }

    // ✅ bez roku
    val dateTitle = selectedDate.format(DateTimeFormatter.ofPattern("d. M."))
    val dayCount = counts[selectedDate] ?: 0

    // lehké zvýraznění čísel ve světlém režimu
    val isLight = MaterialTheme.colorScheme.background.luminance() > 0.5f
    val dayNumberWeight = if (isLight) FontWeight.SemiBold else FontWeight.Medium

    Column(Modifier.fillMaxWidth()) {

        // --- Karta kalendáře (header měsíce je hned nad dny) ---
        Surface(
            tonalElevation = 1.dp,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .fillMaxWidth()
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Header (měsíc + šipky) – uvnitř karty
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Předchozí měsíc",
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onPrevMonth() }
                            .padding(10.dp)
                    )

                    Text(
                        text = "$monthTitle ${month.year}",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Další měsíc",
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onNextMonth() }
                            .padding(10.dp)
                    )
                }

                WeekHeader()

                Spacer(Modifier.height(8.dp))

                MonthGrid(
                    ym = month,
                    selected = selectedDate,
                    counts = counts,
                    onSelectDate = onSelectDate,
                    onOpenDayEntries = onOpenDayEntries,
                    onQuickAddEntry = onQuickAddEntry, // ✅ propasované dál
                    dayNumberWeight = dayNumberWeight
                )
            }
        }

        // --- Panel vybraného dne POD kalendářem ---
        Surface(
            tonalElevation = 1.dp,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(horizontal = 14.dp)
                .fillMaxWidth()
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(dateTitle, style = MaterialTheme.typography.titleMedium)

                // ✅ tlačítko jen pokud existuje aspoň 1 zápis
                if (dayCount > 0) {
                    Button(onClick = onOpenDayEntries, modifier = Modifier.fillMaxWidth()) {
                        Text("Otevřít zápisy dne")
                    }
                }

                Text(
                    text = "Zápisy dne: $dayCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun WeekHeader() {
    val locale = Locale.getDefault()
    val days = listOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    )

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (d in days) {
            Text(
                text = d.getDisplayName(TextStyle.SHORT, locale).replace(".", ""),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MonthGrid(
    ym: YearMonth,
    selected: LocalDate,
    counts: Map<LocalDate, Int>,
    onSelectDate: (LocalDate) -> Unit,
    onOpenDayEntries: () -> Unit,
    onQuickAddEntry: (LocalDate) -> Unit, // ✅ FIX: bere LocalDate
    dayNumberWeight: FontWeight
) {
    val first = ym.atDay(1)
    val shift = ((first.dayOfWeek.value + 6) % 7) // Monday=0 ... Sunday=6
    val daysInMonth = ym.lengthOfMonth()

    val cells = IntArray(42) { idx ->
        val dayNum = idx - shift + 1
        if (dayNum in 1..daysInMonth) dayNum else 0
    }

    for (row in 0 until 6) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (col in 0 until 7) {
                val idx = row * 7 + col
                val day = cells[idx]
                val date = if (day == 0) null else ym.atDay(day)
                val count = if (date == null) 0 else (counts[date] ?: 0)

                DayCell(
                    day = day,
                    date = date,
                    selected = selected,
                    count = count,
                    onSelectDate = onSelectDate,
                    onOpenDayEntries = onOpenDayEntries,
                    onQuickAddEntry = onQuickAddEntry, // ✅ propasované dál
                    dayNumberWeight = dayNumberWeight,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun DayCell(
    day: Int,
    date: LocalDate?,
    selected: LocalDate,
    count: Int,
    onSelectDate: (LocalDate) -> Unit,
    onOpenDayEntries: () -> Unit,
    onQuickAddEntry: (LocalDate) -> Unit, // ✅ FIX: bere LocalDate
    dayNumberWeight: FontWeight,
    modifier: Modifier = Modifier
) {
    val isSelected = (date != null && date == selected)
    val hasEntries = count > 0
    val shape = RoundedCornerShape(12.dp)

    var menuExpanded by remember(date) { mutableStateOf(false) }

    val bg = when {
        day == 0 -> MaterialTheme.colorScheme.surface
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        hasEntries -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val fg = when {
        day == 0 -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0f)
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        hasEntries -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(bg)
            .pointerInput(date) {
                detectTapGestures(
                    onTap = {
                        if (date != null) onSelectDate(date)
                    },
                    onLongPress = {
                        if (date != null) {
                            onSelectDate(date)
                            menuExpanded = true
                        }
                    }
                )
            }
            .padding(6.dp)
    ) {
        if (day != 0) {
            Text(
                text = day.toString(),
                color = fg,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = dayNumberWeight),
                modifier = Modifier.align(Alignment.TopStart)
            )

            // ✅ počet zápisů bez kroužku
            if (hasEntries) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = fg,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }

            // ✅ long-press menu
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Nový zápis") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32) // zelená
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        if (date != null) onQuickAddEntry(date) // ✅ FIX: pošle datum
                    }
                )

                // oko jen pokud existuje aspoň 1 zápis
                if (hasEntries) {
                    DropdownMenuItem(
                        text = { Text("Zobrazit zápisy") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onOpenDayEntries()
                        }
                    )
                }
            }
        }
    }
}
