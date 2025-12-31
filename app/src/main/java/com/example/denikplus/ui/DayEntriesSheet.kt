package com.example.denikplus.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    onEdit: (EntryItem) -> Unit,
    onDelete: (EntryItem) -> Unit,
    onDismiss: () -> Unit
) {
    val title = date.format(DateTimeFormatter.ofPattern("d. M. yyyy"))

    var pendingDelete by remember { mutableStateOf<EntryItem?>(null) }

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
                    items(entries, key = { it.id }) { e ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onEdit(e) }
                        ) {
                            Column(Modifier.fillMaxWidth().padding(14.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(e.moodLabel, style = MaterialTheme.typography.titleMedium)
                                        Text(e.timeText(), style = MaterialTheme.typography.labelMedium)
                                    }

                                    IconButton(onClick = { pendingDelete = e }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Smazat")
                                    }
                                }

                                if (e.text.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(e.text, maxLines = 3)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- potvrzení smazání ---
    val del = pendingDelete
    if (del != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Smazat zápis?") },
            text = { Text("Opravdu chceš smazat tento zápis? Tuto akci nelze vrátit zpět.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(del)
                        pendingDelete = null
                    }
                ) { Text("Smazat") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Zrušit") }
            }
        )
    }
}
