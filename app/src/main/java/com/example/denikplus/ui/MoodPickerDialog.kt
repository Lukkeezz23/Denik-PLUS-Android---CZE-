package com.example.denikplus.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

sealed class MoodChoice {
    data class Preset(val label: String) : MoodChoice()
    data class Custom(val text: String) : MoodChoice()
}

private val Presets = listOf(
    "Dobře", "Špatně", "Depresivně", "Naštvaně", "V pohodě", "Unaveně"
)

@Composable
fun MoodPickerDialog(
    date: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (MoodChoice) -> Unit
) {
    var selected by remember { mutableStateOf<MoodChoice?>(null) }
    var customText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Jak se zrovna teď cítíš?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // jednoduchá “mřížka” 2 sloupce
                val rows = Presets.chunked(2)
                for (row in rows) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (label in row) {
                            MoodTile(
                                label = label,
                                selected = selected == MoodChoice.Preset(label),
                                onClick = { selected = MoodChoice.Preset(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }

                MoodTile(
                    label = "Jinak",
                    selected = selected is MoodChoice.Custom,
                    onClick = { selected = MoodChoice.Custom(customText) },
                    modifier = Modifier.fillMaxWidth()
                )

                if (selected is MoodChoice.Custom) {
                    OutlinedTextField(
                        value = customText,
                        onValueChange = {
                            customText = it
                            selected = MoodChoice.Custom(customText)
                        },
                        label = { Text("Napiš svůj pocit") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val s = selected ?: return@TextButton
                    val finalChoice = if (s is MoodChoice.Custom) MoodChoice.Custom(customText) else s
                    onConfirm(finalChoice)
                },
                enabled = selected != null
            ) {
                Text("Pokračovat")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Zrušit") }
        }
    )
}

@Composable
private fun MoodTile(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = if (selected) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    } else {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    }

    Card(
        modifier = modifier,
        colors = colors,
        onClick = onClick
    ) {
        Box(Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
