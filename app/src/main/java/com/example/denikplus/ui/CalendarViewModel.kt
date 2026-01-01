// FILE: ui/YearCalendarScreen.kt
package com.example.denikplus.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Immutable
private data class MonthUi(
    val year: Int,
    val month: Int
) {
    val ym: YearMonth = YearMonth.of(year, month)
}

@Composable
fun YearCalendarScreen(
    year: Int,
    selectedDate: LocalDate,
    counts: Map<LocalDate, Int>,
    onSelectDate: (LocalDate) -> Unit,
    onPrevYear: () -> Unit,
    onNextYear: () -> Unit,
    onOpenDayEntries: () -> Unit
) {
    val months = (1..12).map { MonthUi(year, it) }

    Column(Modifier.fillMaxWidth()) {
        // header year
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ArrowBackIosNew,
                contentDescription = "Předchozí rok",
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onPrevYear() }
                    .padding(10.dp)
            )
            Text(
                text = year.toString(),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ArrowForwardIos,
                contentDescription = "Další rok",
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onNextYear() }
                    .padding(10.dp)
            )
        }

        val dateTitle = selectedDate.format(DateTimeFormatter.ofPattern("d. M. yyyy"))
        val dayCount = counts[selectedDate] ?: 0

        Surface(
            tonalElevation = 1.dp,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(horizontal = 14.dp)
                .fillMaxWidth()
        ) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(dateTitle, style = MaterialTheme.typography.titleMedium)
                Button(onClick = onOpenDayEntries, modifier = Modifier.fillMaxWidth()) {
                    Text("Otevřít zápisy dne")
                }
                Text("Zápisy dne: $dayCount", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(months, key = { "${it.year}-${it.month}" }) { m ->
                MonthCard(
                    ym = m.ym,
                    selected = selectedDate,
                    counts = counts,
                    onSelectDate = onSelectDate
                )
            }
        }
    }
}

@Composable
private fun MonthCard(
    ym: YearMonth,
    selected: LocalDate,
    counts: Map<LocalDate, Int>,
    onSelectDate: (LocalDate) -> Unit
) {
    val locale = Locale.getDefault()
    val monthTitle = ym.month.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.titlecase(locale) }

    Surface(
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(horizontal = 14.dp)
            .fillMaxWidth()
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(
                text = monthTitle,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )

            Spacer(Modifier.height(6.dp))

            WeekHeader()

            Spacer(Modifier.height(6.dp))

            MonthGrid(
                ym = ym,
                selected = selected,
                counts = counts,
                onSelectDate = onSelectDate
            )
        }
    }
}

@Composable
private fun WeekHeader() {
    val days = listOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    )
    Row(Modifier.fillMaxWidth()) {
        for (d in days) {
            Text(
                text = d.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
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
    onSelectDate: (LocalDate) -> Unit
) {
    val first = ym.atDay(1)
    val firstDow = first.dayOfWeek // Monday..Sunday
    val shift = ((firstDow.value + 6) % 7) // Monday=0 ... Sunday=6
    val daysInMonth = ym.lengthOfMonth()

    val totalCells = 42 // 6 rows
    val cells = IntArray(totalCells) { idx ->
        val dayNum = idx - shift + 1
        if (dayNum in 1..daysInMonth) dayNum else 0
    }

    for (row in 0 until 6) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (col in 0 until 7) {
                val idx = row * 7 + col
                val day = cells[idx]
                DayCell(
                    day = day,
                    date = if (day == 0) null else ym.atDay(day),
                    selected = selected,
                    count = if (day == 0) 0 else (counts[ym.atDay(day)] ?: 0),
                    onSelectDate = onSelectDate,
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
    modifier: Modifier = Modifier
) {
    val isSelected = date != null && date == selected
    val shape = RoundedCornerShape(12.dp)

    val bg = when {
        day == 0 -> MaterialTheme.colorScheme.surface
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val fg = when {
        day == 0 -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0f)
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(bg)
            .let {
                if (date != null) it.clickable { onSelectDate(date) } else it
            }
            .padding(6.dp)
    ) {
        if (day != 0) {
            Text(
                text = day.toString(),
                color = fg,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.align(Alignment.TopStart)
            )

            if (count > 0) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}
