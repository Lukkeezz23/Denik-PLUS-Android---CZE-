package com.example.denikplus.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.util.UUID
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditorDialog(
    date: LocalDate,
    title: String,
    moodLabel: String,
    initialText: String = "",
    onDismiss: () -> Unit,
    onConfirm: (moodLabel: String, text: String) -> Unit,

    // pickery (napojíš později)
    onPickPhoto: (() -> Unit)? = null,
    onPickAudio: (() -> Unit)? = null,
    onPickLocation: ((LocationKind) -> Unit)? = null,
) {
    var text by remember { mutableStateOf(initialText) }

    // preview state (zatím jen informace / placeholder)
    var previewImage by remember { mutableStateOf<Uri?>(null) }
    var previewAudio by remember { mutableStateOf<Uri?>(null) }
    var previewMap by remember { mutableStateOf<String?>(null) }

    fun appendToken(token: String) {
        val needsSpace = text.isNotEmpty() && !text.last().isWhitespace()
        text = buildString {
            append(text)
            if (needsSpace) append(' ')
            append(token)
            append(' ')
        }
    }

    val inline = remember(text) {
        buildInlineContent(
            raw = text,
            onImageClick = { uri -> previewImage = uri },
            onAudioClick = { uri -> previewAudio = uri },
            onMapClick = { payload -> previewMap = payload }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$title • $moodLabel") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionIconRow(
                    onPhoto = {
                        onPickPhoto?.invoke()
                        appendToken("[IMG:${Uri.EMPTY}]")
                    },
                    onAudio = {
                        onPickAudio?.invoke()
                        appendToken("[AUD:${Uri.EMPTY}]")
                    },
                    onMap = {
                        onPickLocation?.invoke(LocationKind.PLANNED_TRIP)
                        appendToken("[MAP:PLANNED]")
                    },
                    onTimeline = {
                        onPickLocation?.invoke(LocationKind.PAST_TIMELINE)
                        appendToken("[MAP:TIMELINE]")
                    }
                )

                TextAreaWithInlinePreviews(
                    value = text,
                    annotated = inline.annotated,
                    inlineContent = inline.inlineContent,
                    onValueChange = { text = it },
                    minHeight = 260.dp,
                    maxHeight = 520.dp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(moodLabel, text) }) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Uložit")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Zrušit") } }
    )

    // --- Image preview (zatím bez Coil jen placeholder) ---
    if (previewImage != null) {
        AlertDialog(
            onDismissRequest = { previewImage = null },
            title = { Text("Foto") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp, max = 520.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Photo, contentDescription = null)
                    }
                    Text(
                        text = "URI:\n${previewImage}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Až přidáme Coil, tady se zobrazí obrázek v plném rozlišení.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = { TextButton(onClick = { previewImage = null }) { Text("Zavřít") } }
        )
    }

    // --- Audio preview (placeholder) ---
    if (previewAudio != null) {
        AlertDialog(
            onDismissRequest = { previewAudio = null },
            title = { Text("Hlasovka") },
            text = {
                Text(
                    "Zatím placeholder přehrávače.\n\nURI:\n${previewAudio}",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = { TextButton(onClick = { previewAudio = null }) { Text("Zavřít") } }
        )
    }

    // --- Map preview (placeholder) ---
    if (previewMap != null) {
        AlertDialog(
            onDismissRequest = { previewMap = null },
            title = { Text("Místo / událost") },
            text = { Text("Payload:\n${previewMap}") },
            confirmButton = { TextButton(onClick = { previewMap = null }) { Text("Zavřít") } }
        )
    }
}

@Composable
private fun ActionIconRow(
    onPhoto: () -> Unit,
    onAudio: () -> Unit,
    onMap: () -> Unit,
    onTimeline: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MiniIconChip(icon = Icons.Default.Photo, label = "Foto", onClick = onPhoto)
        MiniIconChip(icon = Icons.Default.Mic, label = "Hlas", onClick = onAudio)
        MiniIconChip(icon = Icons.Default.Place, label = "Místo", onClick = onMap)
        MiniIconChip(icon = Icons.Default.Public, label = "Timeline", onClick = onTimeline)
        Spacer(Modifier.weight(1f))
        Icon(
            Icons.Default.TextFields,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MiniIconChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = { Icon(icon, contentDescription = null) }
    )
}

private data class InlineBuildResult(
    val annotated: AnnotatedString,
    val inlineContent: Map<String, InlineTextContent>
)

private fun buildInlineContent(
    raw: String,
    onImageClick: (Uri) -> Unit,
    onAudioClick: (Uri) -> Unit,
    onMapClick: (String) -> Unit
): InlineBuildResult {
    val inlineMap = LinkedHashMap<String, InlineTextContent>()

    fun dpToPlaceholderUnit(dp: Dp): TextUnit {
        // Placeholder chce TextUnit. Použijeme rozumný převod (1dp ~ 1sp pro náhledové prvky).
        return dp.value.sp
    }

    val annotated = buildAnnotatedString {
        var i = 0
        while (i < raw.length) {
            val start = raw.indexOf('[', i)
            if (start == -1) {
                append(raw.substring(i))
                break
            }
            val end = raw.indexOf(']', start + 1)
            if (end == -1) {
                append(raw.substring(i))
                break
            }

            if (start > i) append(raw.substring(i, start))

            val token = raw.substring(start + 1, end) // TYPE:payload
            val colon = token.indexOf(':')
            if (colon <= 0 || colon == token.lastIndex) {
                append(raw.substring(start, end + 1))
                i = end + 1
                continue
            }

            val type = token.substring(0, colon).trim().uppercase()
            val payload = token.substring(colon + 1).trim()

            when (type) {
                "IMG" -> {
                    val key = "img_" + UUID.randomUUID().toString()
                    val uri = runCatching { Uri.parse(payload) }.getOrNull() ?: Uri.EMPTY

                    appendInlineContent(key, "[IMG]")
                    inlineMap[key] = InlineTextContent(
                        placeholder = Placeholder(
                            width = dpToPlaceholderUnit(34.dp),
                            height = dpToPlaceholderUnit(34.dp),
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .clickable { onImageClick(uri) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Photo, contentDescription = null)
                        }
                    }
                }

                "AUD" -> {
                    val key = "aud_" + UUID.randomUUID().toString()
                    val uri = runCatching { Uri.parse(payload) }.getOrNull() ?: Uri.EMPTY

                    appendInlineContent(key, "[AUD]")
                    inlineMap[key] = chipInline(
                        height = 30.dp,
                        text = "Hlasovka",
                        icon = Icons.Default.Mic,
                        onClick = { onAudioClick(uri) }
                    )
                }

                "MAP" -> {
                    val key = "map_" + UUID.randomUUID().toString()
                    appendInlineContent(key, "[MAP]")
                    inlineMap[key] = chipInline(
                        height = 30.dp,
                        text = "Místo",
                        icon = Icons.Default.Place,
                        onClick = { onMapClick(payload) }
                    )
                }

                else -> {
                    append(raw.substring(start, end + 1))
                }
            }

            i = end + 1
        }
    }

    return InlineBuildResult(annotated = annotated, inlineContent = inlineMap)
}

private fun chipInline(
    height: Dp,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
): InlineTextContent {
    val widthDp = min(120, 64 + text.length * 6)
    fun dpToPlaceholderUnit(dp: Dp): TextUnit = dp.value.sp

    return InlineTextContent(
        placeholder = Placeholder(
            width = dpToPlaceholderUnit(widthDp.dp),
            height = dpToPlaceholderUnit(height),
            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
        )
    ) {
        Surface(
            modifier = Modifier
                .height(height)
                .clip(RoundedCornerShape(999.dp))
                .clickable { onClick() },
            color = MaterialTheme.colorScheme.secondaryContainer,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(text, style = MaterialTheme.typography.labelMedium, maxLines = 1)
            }
        }
    }
}

@Composable
private fun TextAreaWithInlinePreviews(
    value: String,
    annotated: AnnotatedString,
    inlineContent: Map<String, InlineTextContent>,
    onValueChange: (String) -> Unit,
    minHeight: Dp,
    maxHeight: Dp
) {
    val shape = RoundedCornerShape(14.dp)

    Surface(
        shape = shape,
        tonalElevation = 1.dp,
        border = ButtonDefaults.outlinedButtonBorder,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight, max = maxHeight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxSize(),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                decorationBox = { inner ->
                    if (value.isBlank()) {
                        Text(
                            "Zápis…",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    inner()
                }
            )

            // Overlay render s inline náhledy (text + “miniatury” tokenů)
            Text(
                text = annotated,
                inlineContent = inlineContent,
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
