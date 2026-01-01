// FILE: ui/FeelingPickerDialog.kt
package com.example.denikplus.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

data class FeelingOption(val emoji: String, val label: String)

@Composable
fun FeelingPickerDialog(
    onSkip: () -> Unit,
    onPick: (FeelingOption) -> Unit,
    onDismiss: () -> Unit
) {
    val pages = remember {
        listOf(
            listOf(
                FeelingOption("😁", "Šťastný"),
                FeelingOption("🥳", "Nadšený"),
                FeelingOption("🙂", "V pohodě"),
                FeelingOption("😌", "Klidný"),
                FeelingOption("😍", "Zamilovaný"),
                FeelingOption("🤩", "Motivovaný"),
                FeelingOption("😎", "Sebejistý"),
                FeelingOption("🤗", "Vděčný"),
                FeelingOption("😴", "Unavený"),
                FeelingOption("🤒", "Nemocný"),
                FeelingOption("😐", "Neutrální"),
                FeelingOption("🤔", "Zamyšlený"),
            ),
            listOf(
                FeelingOption("😟", "Smutný"),
                FeelingOption("😢", "Uplakaný"),
                FeelingOption("😞", "Na dně"),
                FeelingOption("😰", "Nervózní"),
                FeelingOption("😕", "Zmatený"),
                FeelingOption("😵‍💫", "Omámený"),
                FeelingOption("😖", "Ublížený"),
                FeelingOption("😠", "Napadený"),
                FeelingOption("😡", "Naštvaný"),
                FeelingOption("🤢", "Znechucený"),
                FeelingOption("😤", "Frustrovaný"),
                FeelingOption("😳", "Ve stresu"),
            )
        )
    }

    var selected by remember { mutableStateOf<FeelingOption?>(null) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(visible = visible, enter = fadeIn() + scaleIn(initialScale = 0.96f)) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Jak se teď cítíš?", style = MaterialTheme.typography.titleLarge)

                    val pagerState = rememberPagerState(pageCount = { pages.size })

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            for (row in pages[page].chunked(3)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    for (opt in row) {
                                        val isSel = selected == opt
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isSel)
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                            else
                                                MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selected = opt }
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(opt.emoji, style = MaterialTheme.typography.headlineMedium)
                                                Text(
                                                    opt.label,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    textAlign = TextAlign.Center,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                    if (row.size < 3) repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                                }
                            }
                        }
                    }

                    // “tečky” stran
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(pages.size) { i ->
                            val a = if (pagerState.currentPage == i) 1f else 0.35f
                            Text("●", color = MaterialTheme.colorScheme.onSurface.copy(alpha = a))
                            Spacer(Modifier.width(6.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onSkip,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFFFFB300) // oranžovo-žlutá
                            )
                        ) { Text("Přeskočit") }

                        TextButton(
                            onClick = { selected?.let(onPick) },
                            enabled = selected != null,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF2E7D32) // zelená
                            )
                        ) { Text("Vybrat") }
                    }
                }
            }
        }
    }
}
