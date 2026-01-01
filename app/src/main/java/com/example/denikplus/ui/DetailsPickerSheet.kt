// FILE: ui/DetailsPickerSheet.kt
package com.example.denikplus.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.denikplus.data.DetailCategory
import com.example.denikplus.data.DetailItem
import com.example.denikplus.data.DetailSelection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsPickerSheet(
    categories: List<DetailCategory>,
    initial: List<DetailSelection>,
    onConfirm: (List<DetailSelection>) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember(initial) { mutableStateOf(initial.associateBy { it.itemId }.toMutableMap()) }

    var noteFor by remember { mutableStateOf<Pair<DetailCategory, DetailItem>?>(null) }
    var noteText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Rozšířené detaily", style = MaterialTheme.typography.titleLarge)

            if (selected.isNotEmpty()) {
                Text(
                    "Vybráno: ${selected.size} (ťukni znovu pro odebrání / přidání poznámky)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(categories) { cat ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(cat.title, style = MaterialTheme.typography.titleMedium)

                            FlowRow(spacedBy = 8.dp) {
                                cat.items.forEach { item ->
                                    val isSel = selected.containsKey(item.id)
                                    AssistChip(
                                        onClick = {
                                            if (isSel) {
                                                selected.remove(item.id)
                                            } else {
                                                // vyber + hned nabídni poznámku
                                                selected[item.id] = DetailSelection(
                                                    categoryId = cat.id,
                                                    itemId = item.id,
                                                    itemTitle = item.title,
                                                    note = ""
                                                )
                                                noteFor = cat to item
                                                noteText = ""
                                            }
                                        },
                                        label = { Text(item.title) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = if (isSel)
                                                MaterialTheme.colorScheme.secondaryContainer
                                            else
                                                MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Zrušit") }
                TextButton(onClick = { onConfirm(selected.values.toList()) }) { Text("Použít") }
            }

            Spacer(Modifier.height(10.dp))
        }
    }

    // poznámka k položce (volitelné)
    val nf = noteFor
    if (nf != null) {
        val (cat, item) = nf
        AlertDialog(
            onDismissRequest = { noteFor = null },
            title = { Text("Poznámka") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${cat.title} • ${item.title}", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Komentář (volitelně)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val cur = selected[item.id]
                    if (cur != null) {
                        selected[item.id] = cur.copy(note = noteText.trim())
                    }
                    noteFor = null
                }) { Text("Uložit") }
            },
            dismissButton = {
                TextButton(onClick = { noteFor = null }) { Text("Přeskočit") }
            }
        )
    }
}

@Composable
private fun FlowRow(
    spacedBy: Dp,
    content: @Composable RowScope.() -> Unit
) {
    // jednoduchá “flow row” bez extra dependency
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacedBy),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}
