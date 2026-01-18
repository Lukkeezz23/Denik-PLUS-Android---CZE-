// FILE: ui/DetailsPickerSheet.kt
package com.example.denikplus.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.denikplus.data.DetailCategory
import com.example.denikplus.data.DetailSelection
import com.example.denikplus.ui.theme.DenikPlusTheme

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsPickerSheet(
    categories: List<DetailCategory>,
    initial: List<DetailSelection>,
    onConfirm: (List<DetailSelection>) -> Unit,
    onDismiss: () -> Unit
) {

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val selected = remember { mutableStateListOf<DetailSelection>().apply { addAll(initial) } }
    var editNoteFor by remember { mutableStateOf<DetailSelection?>(null) }
    val listState = rememberLazyListState()

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val maxSheetHeight = screenHeight * 0.92f

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxSheetHeight)
        ) {

            // “handle”
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f))
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Rozšířené detaily", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Tap = vybrat • Tap na vybraný = poznámka • Long-press = rychle vybrat/odebrat",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "${selected.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider()

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 14.dp, end = 14.dp, top = 12.dp, bottom = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                categories.forEach { cat ->
                    item(key = "cat_${cat.id}") {
                        CategorySection(
                            category = cat,
                            selected = selected.toList(),
                            onToggle = { sel ->
                                val exists = selected.any { it.itemId == sel.itemId }
                                if (exists) selected.removeAll { it.itemId == sel.itemId }
                                else selected.add(sel)
                            },
                            onEditNote = { payload -> editNoteFor = payload }
                        )
                    }
                }

                item { Spacer(Modifier.height(70.dp)) }
            }

            HorizontalDivider()
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) { Text("Zrušit") }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { onConfirm(selected.toList()) },
                    colors = ButtonDefaults.buttonColors()
                ) { Text("Potvrdit") }
            }
        }
    }

    val editing = editNoteFor
    if (editing != null) {
        NoteDialog(
            title = editing.itemTitle,
            initial = editing.note,
            onDismiss = { editNoteFor = null },
            onSave = { note ->
                selected.replaceAll { s ->
                    if (s.itemId == editing.itemId) s.copy(note = note) else s
                }
                editNoteFor = null
            }
        )
    }
}

@Composable
private fun CategorySection(
    category: DetailCategory,
    selected: List<DetailSelection>,
    onToggle: (DetailSelection) -> Unit,
    onEditNote: (DetailSelection) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(category.title, style = MaterialTheme.typography.titleMedium)

        FlowWrapRow(
            horizontalGap = 10.dp,
            verticalGap = 10.dp
        ) {
            category.items.forEach { item ->
                val isSelected = selected.any { it.itemId == item.id }
                val sel = selected.firstOrNull { it.itemId == item.id }

                DetailPill(
                    title = item.title,
                    selected = isSelected,
                    hasNote = !sel?.note.isNullOrBlank(),
                    itemId = item.id,
                    onClick = {
                        val next = DetailSelection(
                            categoryId = category.id,
                            itemId = item.id,
                            itemTitle = item.title,
                            note = sel?.note.orEmpty()
                        )
                        if (isSelected) onEditNote(next) else onToggle(next)
                    },
                    onLongPressToggle = {
                        val exists = selected.any { it.itemId == item.id }
                        if (exists) {
                            onToggle(
                                DetailSelection(category.id, item.id, item.title, note = sel?.note.orEmpty())
                            )
                        } else {
                            onToggle(DetailSelection(category.id, item.id, item.title, note = ""))
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DetailPill(
    title: String,
    selected: Boolean,
    hasNote: Boolean,
    itemId: String,
    onClick: () -> Unit,
    onLongPressToggle: () -> Unit
) {
    val shape = RoundedCornerShape(999.dp)
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    androidx.compose.material3.Surface(
        shape = shape,
        color = bg,
        modifier = Modifier
            .clip(shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPressToggle
            )
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = detailIconFor(itemId),
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                color = fg,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (hasNote) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(fg.copy(alpha = 0.8f))
                )
            }
        }
    }
}

@Composable
private fun FlowWrapRow(
    horizontalGap: Dp,
    verticalGap: Dp,
    content: @Composable () -> Unit
) {
    Layout(content = content) { measurables, constraints ->
        val maxWidth = constraints.maxWidth

        val gapX = horizontalGap.roundToPx()
        val gapY = verticalGap.roundToPx()

        val childConstraints = constraints.copy(minWidth = 0)

        val placeables = measurables.map { it.measure(childConstraints) }

        var x = 0
        var y = 0
        var rowH = 0

        val positions = ArrayList<Pair<Int, Int>>(placeables.size)

        for (p in placeables) {
            if (x + p.width > maxWidth && x > 0) {
                x = 0
                y += rowH + gapY
                rowH = 0
            }
            positions.add(x to y)
            x += p.width + gapX
            rowH = maxOf(rowH, p.height)
        }

        val rawHeight = y + rowH
        val finalHeight = rawHeight.coerceIn(constraints.minHeight, constraints.maxHeight)

        layout(width = maxWidth, height = finalHeight) {
            placeables.forEachIndexed { i, p ->
                val (px, py) = positions[i]
                p.placeRelative(px, py)
            }
        }
    }
}

@Composable
private fun NoteDialog(
    title: String,
    initial: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initial) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Poznámka: $title") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Poznámka") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = { TextButton(onClick = { onSave(text.trim()) }) { Text("Uložit") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Zrušit") } }
    )
}

@Preview(showBackground = true)
@Composable
private fun DetailsPickerSheetPreview() {
    DenikPlusTheme {
        DetailsPickerSheet(
            categories = defaultDetailCategories(),
            initial = listOf(
                DetailSelection("physical", "walk", "Chůze", "Ranní procházka"),
                DetailSelection("food", "coffee", "Káva")
            ),
            onConfirm = {},
            onDismiss = {}
        )
    }
}
