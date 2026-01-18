
package com.example.denikplus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.denikplus.data.DetailCategory
import com.example.denikplus.data.DetailItem
import com.example.denikplus.data.DetailSelection

private val SkipColor = Color(0xFFFFB300)
private val ConfirmColor = Color(0xFF2E7D32)

private val DefaultCategories: List<DetailCategory> = listOf(
    DetailCategory(
        id = "intim_health",
        title = "Intimita a zdraví",
        items = listOf(
            DetailItem("intimacy", "Intimita"),
            DetailItem("masturbation", "Masturbace"),
            DetailItem("menstruation", "Menstruace"),
            DetailItem("ovulation", "Ovulace / plodné období"),
            DetailItem("protection", "Ochrana / antikoncepce"),
            DetailItem("libido", "Libido")
        )
    ),
    DetailCategory(
        id = "physical",
        title = "Fyzická aktivita",
        items = listOf(
            DetailItem("walk", "Chůze"),
            DetailItem("run", "Běh"),
            DetailItem("gym", "Posilování"),
            DetailItem("bike", "Kolo"),
            DetailItem("hike", "Turistika / hory"),
            DetailItem("yoga", "Jóga / strečink"),
            DetailItem("sport_other",  "Sport (jiné)")
        )
    ),
    DetailCategory(
        id = "sleep",
        title = "Spánek",
        items = listOf(
            DetailItem("sleep_good", "Kvalitní spánek"),
            DetailItem("sleep_short", "Krátký spánek"),
            DetailItem("sleep_late", "Pozdní usnutí"),
            DetailItem("sleep_wakeup", "Noční buzení"),
            DetailItem("sleep_bad_dreams", "Špatné sny")
        )
    ),
    DetailCategory(
        id = "food",
        title = "Jídlo a pití",
        items = listOf(
            DetailItem("food_healthy", "Zdravé jídlo"),
            DetailItem("food_fast", "Fastfood"),
            DetailItem("food_sweets", "Sladké"),
            DetailItem("drink_ok", "Pitný režim OK"),
            DetailItem("drink_low", "Málo tekutin"),
            DetailItem("caffeine", "Kofein"),
            DetailItem("alcohol", "Alkohol")
        )
    ),
    DetailCategory(
        id = "work",
        title = "Práce / škola",
        items = listOf(
            DetailItem("work_productive", "Produktivní den"),
            DetailItem("work_stress", "Stres"),
            DetailItem("work_deadline", "Deadline / tlak"),
            DetailItem("study", "Učení / studium"),
            DetailItem("success", "Úspěch / posun")
        )
    ),
    DetailCategory(
        id = "social",
        title = "Sociální",
        items = listOf(
            DetailItem("family", "Rodina"),
            DetailItem("friends", "Přátelé"),
            DetailItem("partner", "Partner/ka"),
            DetailItem("alone", "Samota"),
            DetailItem("conflict", "Konflikt / hádka")
        )
    ),
    DetailCategory(
        id = "mind",
        title = "Psychika / péče o sebe",
        items = listOf(
            DetailItem("meditation", "Meditace"),
            DetailItem("therapy", "Terapie / sezení"),
            DetailItem("relax", "Relax"),
            DetailItem("brainstorm", "Brainstorming"),
            DetailItem("creative", "Kreativita")
        )
    ),
    DetailCategory(
        id = "other",
        title = "Ostatní",
        items = listOf(
            DetailItem("travel", "Cestování"),
            DetailItem("symptoms", "Zdravotní příznaky"),
            DetailItem("event", "Událost dne"),
            DetailItem("custom", "Vlastní")
        )
    )
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExtendedDetailsDialog(
    onDismiss: () -> Unit,
    onDone: (List<DetailSelection>) -> Unit,
    categories: List<DetailCategory> = DefaultCategories
) {
    // key = "${categoryId}:${itemId}"
    var selected by remember { mutableStateOf<Map<String, DetailSelection>>(emptyMap()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rozšířený detailní zápis") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Text(
                    "Vyberte položky (volitelné). Ke každé lze přidat poznámku.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories, key = { it.id }) { cat ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(cat.title, style = MaterialTheme.typography.titleMedium)

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (item in cat.items) {
                                    val key = "${cat.id}:${item.id}"
                                    val isSel = selected.containsKey(key)

                                    FilterChip(
                                        selected = isSel,
                                        onClick = {
                                            selected = if (isSel) {
                                                selected - key
                                            } else {
                                                selected + (key to DetailSelection(
                                                    categoryId = cat.id,
                                                    itemId = item.id,
                                                    itemTitle = item.title,
                                                    note = ""
                                                ))
                                            }
                                        },
                                        label = { Text(item.title) }
                                    )
                                }
                            }
                        }
                    }

                    if (selected.isNotEmpty()) {
                        item {
                            Spacer(Modifier.width(0.dp))
                            Text(
                                "Poznámky k vybraným položkám",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        items(selected.values.toList(), key = { it.categoryId + ":" + it.itemId }) { sel ->
                            val key = "${sel.categoryId}:${sel.itemId}"
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = sel.itemTitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                OutlinedTextField(
                                    value = sel.note,
                                    onValueChange = { new ->
                                        val clipped = new.take(120)
                                        selected = selected + (key to sel.copy(note = clipped))
                                    },
                                    singleLine = true,
                                    placeholder = { Text("Volitelná poznámka…") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Nyní ne", color = SkipColor, fontWeight = FontWeight.SemiBold)
            }
        },
        confirmButton = {
            TextButton(onClick = { onDone(selected.values.toList()) }) {
                Text("Ano", color = ConfirmColor, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
