package com.example.denikplus.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/**
 * Jedno “textové pole” s vloženými značkami (tokeny) pro média:
 *  - [IMG:...]  = fotka (obdélník)
 *  - [AUD:...]  = hlasovka (čára)
 *  - [MAP:...]  = místo (hvězda)
 *
 * Reálné pickery/URI napojíme později – zatím se token vloží hned do textu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditorDialog(
    date: LocalDate,
    title: String,
    moodLabel: String,
    initialText: String = "",
    onDismiss: () -> Unit,
    onConfirm: (moodLabel: String, text: String) -> Unit,

    // zatím jen callbacks (napojíme pickery v dalším kroku)
    onPickPhoto: ((afterCursor: Boolean) -> Unit)? = null,
    onPickAudio: ((afterCursor: Boolean) -> Unit)? = null,
    onPickLocation: ((kind: LocationKind) -> Unit)? = null,
) {
    var value by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialText,
                selection = TextRange(initialText.length)
            )
        )
    }

    fun insertToken(token: String) {
        val text = value.text
        val sel = value.selection
        val start = sel.start.coerceIn(0, text.length)
        val end = sel.end.coerceIn(0, text.length)

        val before = text.substring(0, start)
        val after = text.substring(end, text.length)

        // malé odsazení kolem tokenu, ať to nepůsobí “nalepeně”
        val prefix = if (before.isNotEmpty() && !before.last().isWhitespace()) " " else ""
        val suffix = if (after.isNotEmpty() && !after.first().isWhitespace()) " " else ""

        val inserted = prefix + token + suffix
        val newText = before + inserted + after
        val cursor = (before.length + inserted.length).coerceIn(0, newText.length)

        value = value.copy(text = newText, selection = TextRange(cursor))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$title • $moodLabel") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ---- horní “toolbar” jen ikony, bez textů ----
                Surface(
                    tonalElevation = 1.dp,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = {
                                onPickPhoto?.invoke(true)
                                // placeholder token (později dosadíme reálné URI)
                                insertToken("[IMG:${Uri.EMPTY}]")
                            }
                        ) { Icon(Icons.Default.Photo, contentDescription = "Foto") }

                        IconButton(
                            onClick = {
                                onPickAudio?.invoke(true)
                                insertToken("[AUD:${Uri.EMPTY}]")
                            }
                        ) { Icon(Icons.Default.Mic, contentDescription = "Hlasovka") }

                        IconButton(
                            onClick = {
                                onPickLocation?.invoke(LocationKind.PLANNED_TRIP)
                                insertToken("[MAP:PLANNED]")
                            }
                        ) { Icon(Icons.Default.Place, contentDescription = "Místo+") }

                        IconButton(
                            onClick = {
                                onPickLocation?.invoke(LocationKind.PAST_TIMELINE)
                                insertToken("[MAP:TIMELINE]")
                            }
                        ) { Icon(Icons.Default.Map, contentDescription = "Timeline") }

                        Spacer(Modifier.weight(1f))

                        // malé “?” tlačítko – rychlá nápověda tokenů
                        IconButton(
                            onClick = {
                                insertToken("\n[IMG:...] [AUD:...] [MAP:...]\n")
                            }
                        ) { Icon(Icons.Default.HelpOutline, contentDescription = "Nápověda") }
                    }
                }

                // ---- jediná velká “textarea” ----
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 320.dp, max = 560.dp),
                    label = { Text("Zápis") },
                    minLines = 10,
                    maxLines = 18
                )

                // Pozn.: Náhledy (obdélník/čára/hvězda) vykreslíme později v read-only zobrazení zápisu.
                // V editoru teď držíme vše v jednom textu pomocí tokenů.
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(moodLabel, value.text) }) { Text("Uložit") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Zrušit") }
        }
    )
}
