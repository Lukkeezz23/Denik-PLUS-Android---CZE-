// FILE: ui/MoodPickerDialog.kt
package com.example.denikplus.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Immutable
data class MoodOption(
    val id: String,
    val emoji: String,
    val label: String
)

private val SkipColor = Color(0xFFFFB300)   // oranžovo-žlutá
private val ConfirmColor = Color(0xFF2E7D32) // zelená

private val MoodOptions: List<MoodOption> = listOf(
    MoodOption("ok", "🙂", "V pohodě"),
    MoodOption("happy", "😀", "Šťastný/á"),
    MoodOption("calm", "😌", "Klidný/á"),
    MoodOption("excited", "🥳", "Nadšený/á"),
    MoodOption("tired", "😴", "Unavený/á"),
    MoodOption("unsure", "😕", "Nejistý/á"),
    MoodOption("stress", "😟", "Ve stresu"),
    MoodOption("sad", "😢", "Smutný/á"),

    MoodOption("angry", "😠", "Naštvaný/á"),
    MoodOption("frustrated", "😤", "Frustrovaný/á"),
    MoodOption("nervous1", "😰", "Nervózní"),
    MoodOption("down", "😔", "Skleslý/á"),
    MoodOption("sick", "🤒", "Necítím se dobře"),
    MoodOption("overwhelmed", "🤯", "Přetížený/á"),
    MoodOption("grateful", "😇", "Vděčný/á"),
    MoodOption("confident", "😎", "Sebevědomý/á"),

    MoodOption("hurt", "🥺", "Ublížený/á"),
    MoodOption("attacked", "😣", "Napadený/á"),
    MoodOption("disgusted", "🤢", "Znechucený/á"),
    MoodOption("crying", "😭", "Uplakaný/á"),
    MoodOption("rockbottom", "😩", "Na dně"),
    MoodOption("nervous2", "😬", "Nervózní"),
    MoodOption("dazed", "😵‍💫", "Omámený/á"),
    MoodOption("confused", "🤔", "Zmatený/á"),
)

@Composable
fun MoodPickerDialog(
    onDismiss: () -> Unit,
    onSkip: () -> Unit,
    onConfirm: (MoodOption) -> Unit
) {
    val pages = remember { MoodOptions.chunked(8) }

    var page by remember { mutableIntStateOf(0) }
    var selectedId by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + scaleIn(initialScale = 0.96f)
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Jak se teď cítíš?",
                        style = MaterialTheme.typography.titleLarge
                    )

                    // page header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = page > 0) { page-- }
                                .padding(6.dp)
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Předchozí"
                            )
                        }

                        Text(
                            text = "Stránka ${page + 1} / ${pages.size}",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = page < pages.lastIndex) { page++ }
                                .padding(6.dp)
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Další"
                            )
                        }
                    }

                    // grid 4x2
                    val grid = pages[page]
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (r in 0..1) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (c in 0..3) {
                                    val idx = r * 4 + c
                                    val item = grid.getOrNull(idx)
                                    if (item == null) {
                                        Spacer(Modifier.weight(1f))
                                    } else {
                                        MoodCell(
                                            option = item,
                                            selected = selectedId == item.id,
                                            onClick = { selectedId = item.id },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val selected = MoodOptions.firstOrNull { it.id == selectedId }
                    if (selected != null) {
                        Text(
                            text = selected.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Spacer(Modifier.height(0.dp))
                    }

                    // actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onSkip,
                        ) { Text("Přeskočit", color = SkipColor, fontWeight = FontWeight.SemiBold) }

                        Spacer(Modifier.width(6.dp))

                        TextButton(
                            enabled = selected != null,
                            onClick = { selected?.let(onConfirm) }
                        ) {
                            Text(
                                "Vybrat",
                                color = if (selected != null) ConfirmColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodCell(
    option: MoodOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val bg = when {
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    }

    Column(
        modifier = modifier
            .aspectRatio(1.25f)
            .clip(shape)
            .background(bg)
            .clickable { onClick() }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(option.emoji, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            text = option.label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
