package com.example.denikplus.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.ceil
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items


private val CZ = Locale("cs", "CZ")
private val WeekdaysCz = listOf("Po", "Út", "St", "Čt", "Pá", "So", "Ne")

@Composable
fun YearCalendarScreen(
    year: Int,
    entriesByDate: Map<LocalDate, Int>,
    onPrevYear: () -> Unit,
    onNextYear: () -> Unit,
    onDayClick: (LocalDate) -> Unit,
    contentPadding: PaddingValues
) {
    val months = Month.entries.toList()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + 120.dp // místo pro FAB
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TopBarYearSelector(
                year = year,
                onPrevYear = onPrevYear,
                onNextYear = onNextYear
            )
        }

        items(months) { month ->
            MonthCard(
                year = year,
                month = month,
                entriesByDate = entriesByDate,
                onDayClick = onDayClick
            )
        }
    }
}

@Composable
private fun TopBarYearSelector(
    year: Int,
    onPrevYear: () -> Unit,
    onNextYear: () -> Unit
) {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevYear) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Předchozí rok")
            }

            Text(
                text = year.toString(),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            IconButton(onClick = onNextYear) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Další rok")
            }
        }
    }
}

@Composable
private fun MonthCard(
    year: Int,
    month: Month,
    entriesByDate: Map<LocalDate, Int>,
    onDayClick: (LocalDate) -> Unit
) {
    val monthName = month.getDisplayName(TextStyle.FULL, CZ).replaceFirstChar { it.uppercase(CZ) }

    val daysWithEntries = (1..month.length(isLeapYear(year))).mapNotNull { d ->
        val date = LocalDate.of(year, month, d)
        val count = entriesByDate[date] ?: 0
        if (count > 0) date to count else null
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(monthName, style = MaterialTheme.typography.titleMedium)

            if (daysWithEntries.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                TrainRow(daysWithEntries.map { it.first })
            }

            Spacer(Modifier.height(10.dp))
            WeekdayHeader()

            Spacer(Modifier.height(6.dp))
            MonthGrid(
                year = year,
                month = month,
                entriesByDate = entriesByDate,
                onDayClick = onDayClick
            )
        }
    }
}

@Composable
private fun TrainRow(dates: List<LocalDate>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (d in dates.sortedBy { it.dayOfMonth }) {
            Surface(
                shape = CircleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                tonalElevation = 1.dp
            ) {
                Text(
                    text = d.dayOfMonth.toString(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun WeekdayHeader() {
    Row(Modifier.fillMaxWidth()) {
        for (w in WeekdaysCz) {
            Text(
                text = w,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MonthGrid(
    year: Int,
    month: Month,
    entriesByDate: Map<LocalDate, Int>,
    onDayClick: (LocalDate) -> Unit
) {
    val weeks = buildMonthWeeks(year, month, firstDayOfWeek = DayOfWeek.MONDAY)

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (week in weeks) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (date in week) {
                    DayCell(
                        date = date,
                        hasEntry = date != null && (entriesByDate[date] ?: 0) > 0,
                        onClick = { if (date != null) onDayClick(date) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate?,
    hasEntry: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled = date != null
    val containerColor =
        if (!enabled) MaterialTheme.colorScheme.surface
        else if (hasEntry) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surface

    val contentColor =
        if (!enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        else if (hasEntry) MaterialTheme.colorScheme.onSecondaryContainer
        else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier
            .aspectRatio(1f),
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        tonalElevation = if (hasEntry) 2.dp else 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        onClick = onClick,
        enabled = enabled
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = date?.dayOfMonth?.toString() ?: "",
                color = contentColor,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun buildMonthWeeks(
    year: Int,
    month: Month,
    firstDayOfWeek: DayOfWeek
): List<List<LocalDate?>> {
    val first = LocalDate.of(year, month, 1)
    val daysInMonth = month.length(isLeapYear(year))

    // DayOfWeek.value: Monday=1 .. Sunday=7
    val offset = ((first.dayOfWeek.value - firstDayOfWeek.value) + 7) % 7

    val cells = ArrayList<LocalDate?>(42)
    repeat(offset) { cells.add(null) }

    for (d in 1..daysInMonth) {
        cells.add(LocalDate.of(year, month, d))
    }

    // doplnění do celých týdnů
    while (cells.size % 7 != 0) cells.add(null)

    val weekCount = ceil(cells.size / 7.0).toInt()
    return (0 until weekCount).map { i ->
        cells.subList(i * 7, i * 7 + 7)
    }
}

private fun isLeapYear(year: Int): Boolean =
    (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
