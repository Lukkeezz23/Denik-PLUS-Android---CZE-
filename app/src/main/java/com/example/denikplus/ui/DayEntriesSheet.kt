package com.example.denikplus.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.denikplus.data.EntryItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayEntriesSheet(
    date: LocalDate,
    entries: List<EntryItem>,
    onAddClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = date.format(DateTimeFormatter.ofPattern("d. M. yyyy"))

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            Button(onClick = onAddClick, modifier = Modifier.fillMaxWidth()) {
                Text("Nový zápis v tento den")
            }

            Spacer(Modifier.height(12.dp))

            if (entries.isEmpty()) {
                Text("Zatím žádné zápisy.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(24.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(entries) { e ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth().padding(14.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(e.moodLabel, style = MaterialTheme.typography.titleMedium)
                                    Text(e.timeText(), style = MaterialTheme.typography.labelMedium)
                                }
                                if (e.text.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(e.text, maxLines = 2)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
