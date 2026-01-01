// FILE: ui/DayEntriesSheet.kt
package com.example.denikplus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
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
    // Stabilní sheet state – drží expanded (bez partial)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val title = remember(date) {
        date.format(DateTimeFormatter.ofPattern("d. M. yyyy"))
    }

    // Omezíme max výšku sheetu, aby po změně obsahu neskákal (resize animace)
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val maxSheetHeight = screenHeight * 0.88f

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxSheetHeight)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // --- Header ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Zápisy: ${entries.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onAddClick) {
                    Icon(Icons.Default.Add, contentDescription = "Přidat")
                }
            }

            HorizontalDivider()

            // --- List: dáme mu weight, aby měl stabilní prostor a jen scrolloval ---
            if (entries.isEmpty()) {
                Text(
                    text = "Zatím žádné zápisy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true),
                    contentPadding = PaddingValues(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(entries, key = { it.id }) { e ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = e.moodLabel,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(Modifier.width(10.dp))

                            Column(Modifier.weight(1f)) {
                                val preview = e.text.trim().replace("\n", " ")
                                Text(
                                    text = if (preview.length > 60) preview.take(60) + "…" else preview,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            IconButton(onClick = { onEdit(e) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Upravit")
                            }
                            IconButton(onClick = { onDelete(e) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Smazat")
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}
