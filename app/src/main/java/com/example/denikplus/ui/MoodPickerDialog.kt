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
    // držíme si jen “co je vybráno”
    var selectedPreset by remember { mutableStateOf<String?>(null) }
    var isCustom by remember { mutableStateOf(false) }
    var customText by remember { mutableStateOf("") }

    val canConfirm = remember(selectedPreset, isCustom, customText) {
        (selectedPreset != null) || (isCustom && customText.isNotBlank())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Jak se zrovna teď cítíš?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // jednoduchá “mřížka” 2 sloupce
                val rows = Presets.chunked(2)
                for (row in rows) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (label in row) {
                            MoodTile(
                                label = label,
                                selected = (!isCustom && selectedPreset == label),
                                onClick = {
                                    isCustom = false
                                    selectedPreset = label
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }

                MoodTile(
                    label = "Jinak",
                    selected = isCustom,
                    onClick = {
                        isCustom = true
                        selectedPreset = null
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (isCustom) {
                    OutlinedTextField(
                        value = customText,
                        onValueChange = { customText = it },
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
                    val result = when {
                        selectedPreset != null -> MoodChoice.Preset(selectedPreset!!)
                        isCustom -> MoodChoice.Custom(customText.trim())
                        else -> return@TextButton
                    }
                    onConfirm(result)
                },
                enabled = canConfirm
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
