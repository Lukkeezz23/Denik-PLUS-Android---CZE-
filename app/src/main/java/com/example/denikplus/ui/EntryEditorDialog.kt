// FILE: ui/EntryEditorDialog.kt
package com.example.denikplus.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditorDialog(
    date: LocalDate,
    title: String,
    moodLabel: String,
    initialText: String = "",
    initialDetails: List<com.example.denikplus.data.DetailSelection> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (moodLabel: String, text: String, details: List<com.example.denikplus.data.DetailSelection>) -> Unit
) {
    var details by remember { mutableStateOf(initialDetails) }
    var value by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialText,
                selection = TextRange(initialText.length)
            )
        )
    }

    var mood by remember { mutableStateOf(moodLabel) }
    var showMoodPicker by remember { mutableStateOf(false) }

    fun insertToken(token: String) {
        val text = value.text
        val sel = value.selection
        val start = sel.start.coerceIn(0, text.length)
        val end = sel.end.coerceIn(0, text.length)

        val before = text.substring(0, start)
        val after = text.substring(end, text.length)

        val prefix = if (before.isNotEmpty() && !before.last().isWhitespace()) " " else ""
        val suffix = if (after.isNotEmpty() && !after.first().isWhitespace()) " " else ""

        val inserted = prefix + token + suffix
        val newText = before + inserted + after
        val cursor = (before.length + inserted.length).coerceIn(0, newText.length)

        value = value.copy(text = newText, selection = TextRange(cursor))
    }

    var askImage by remember { mutableStateOf(false) }
    var askAudio by remember { mutableStateOf(false) }
    var askMap by remember { mutableStateOf(false) }

    var inputUri by remember { mutableStateOf("") }
    var inputLat by remember { mutableStateOf("") }
    var inputLon by remember { mutableStateOf("") }
    var inputLabel by remember { mutableStateOf("") }

    var previewImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    var playingUri by remember { mutableStateOf<Uri?>(null) }
    val player = remember { MediaPlayer() }

    DisposableEffect(Unit) {
        onDispose {
            runCatching {
                player.reset()
                player.release()
            }
        }
    }

    fun togglePlay(uri: Uri) {
        if (uri == Uri.EMPTY) return

        if (playingUri == uri && player.isPlaying) {
            runCatching { player.pause() }
            return
        }

        runCatching {
            player.reset()
            player.setDataSource(context, uri)
            player.setOnPreparedListener {
                playingUri = uri
                it.start()
            }
            player.setOnCompletionListener {
                if (playingUri == uri) playingUri = null
            }
            player.prepareAsync()
        }
    }

    fun openMap(payload: String) {
        val parts = payload.split("|", limit = 2)
        val coords = parts.firstOrNull().orEmpty()
        val label = parts.getOrNull(1).orEmpty()
        val c = coords.split(",", limit = 2)
        if (c.size != 2) return
        val lat = c[0].trim().toDoubleOrNull() ?: return
        val lon = c[1].trim().toDoubleOrNull() ?: return

        val q = if (label.isNotBlank()) "$lat,$lon($label)" else "$lat,$lon"
        val uri = Uri.parse("geo:$lat,$lon?q=$q")
        val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    val density = LocalDensity.current
    val inlineBuilt = remember(value.text, playingUri) {
        buildInline(
            raw = value.text,
            density = density,
            playingUri = playingUri,
            onImageClick = { uri -> previewImageUri = uri },
            onAudioClick = { uri -> togglePlay(uri) },
            onMapClick = { payload -> openMap(payload) }
        )
    }

    val dateTitle = remember(date) {
        date.format(DateTimeFormatter.ofPattern("d. M. yyyy"))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("$title • $mood", style = MaterialTheme.typography.titleLarge)
                Text(dateTitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (details.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Detaily: (klik pro odebrání)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (d in details.take(6)) { // zatím max 6, později uděláme hezčí layout
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { details = details.filterNot { it.itemId == d.itemId } }
                            ) {
                                Text(
                                    text = if (d.note.isBlank()) d.itemTitle else "${d.itemTitle} • ${d.note}",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                        if (details.size > 6) {
                            Text("… a další (${details.size - 6})", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    EditorActionIcon(Icons.Default.Image, "Vložit foto") {
                        inputUri = ""
                        askImage = true
                    }
                    EditorActionIcon(Icons.Default.Mic, "Vložit audio") {
                        inputUri = ""
                        askAudio = true
                    }
                    EditorActionIcon(Icons.Default.Place, "Vložit místo") {
                        inputLat = ""
                        inputLon = ""
                        inputLabel = ""
                        askMap = true
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "Změnit náladu",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { showMoodPicker = true }
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                    )
                }
            }
        },
        text = {
            TextAreaWithInlinePreviews(
                value = value,
                annotated = inlineBuilt.annotated,
                inlineContent = inlineBuilt.inlineContent,
                onValueChange = { value = it },
                minHeight = 320.dp,
                maxHeight = 560.dp
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(mood, value.text, details) }) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Uložit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Zrušit")
            }
        }
    )

    if (showMoodPicker) {
        val moods = listOf("😁", "🙂", "😐", "😟", "😡", "🥳", "😴", "🤒")
        AlertDialog(
            onDismissRequest = { showMoodPicker = false },
            title = { Text("Vyber náladu") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (row in moods.chunked(4)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            for (m in row) {
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = if (m == mood) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            mood = m
                                            showMoodPicker = false
                                        }
                                ) {
                                    Box(
                                        Modifier.padding(vertical = 14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(m, style = MaterialTheme.typography.titleLarge)
                                    }
                                }
                            }
                            if (row.size < 4) repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showMoodPicker = false }) { Text("Zavřít") } }
        )
    }

    if (askImage) {
        AlertDialog(
            onDismissRequest = { askImage = false },
            title = { Text("Vložit foto") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Vlož URI obrázku (content://, file:// ...).")
                    OutlinedTextField(
                        value = inputUri,
                        onValueChange = { inputUri = it },
                        singleLine = true,
                        label = { Text("URI") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val u = inputUri.trim()
                    if (u.isNotEmpty()) insertToken(buildToken(InlineTokenType.IMG, u))
                    askImage = false
                }) { Text("Vložit") }
            },
            dismissButton = { TextButton(onClick = { askImage = false }) { Text("Zrušit") } }
        )
    }

    if (askAudio) {
        AlertDialog(
            onDismissRequest = { askAudio = false },
            title = { Text("Vložit hlasovku") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Vlož URI audio souboru (content://, file:// ...).")
                    OutlinedTextField(
                        value = inputUri,
                        onValueChange = { inputUri = it },
                        singleLine = true,
                        label = { Text("URI") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val u = inputUri.trim()
                    if (u.isNotEmpty()) insertToken(buildToken(InlineTokenType.AUD, u))
                    askAudio = false
                }) { Text("Vložit") }
            },
            dismissButton = { TextButton(onClick = { askAudio = false }) { Text("Zrušit") } }
        )
    }

    if (askMap) {
        AlertDialog(
            onDismissRequest = { askMap = false },
            title = { Text("Vložit místo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = inputLat,
                            onValueChange = { inputLat = it },
                            singleLine = true,
                            label = { Text("Lat") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = inputLon,
                            onValueChange = { inputLon = it },
                            singleLine = true,
                            label = { Text("Lon") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = inputLabel,
                        onValueChange = { inputLabel = it },
                        singleLine = true,
                        label = { Text("Popisek (volitelně)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Klik na hvězdu v textu otevře mapu.")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val lat = inputLat.trim()
                    val lon = inputLon.trim()
                    if (lat.isNotEmpty() && lon.isNotEmpty()) {
                        val payload = buildString {
                            append(lat); append(","); append(lon)
                            if (inputLabel.trim().isNotEmpty()) {
                                append("|"); append(inputLabel.trim())
                            }
                        }
                        insertToken(buildToken(InlineTokenType.MAP, payload))
                    }
                    askMap = false
                }) { Text("Vložit") }
            },
            dismissButton = { TextButton(onClick = { askMap = false }) { Text("Zrušit") } }
        )
    }

    val imgUri = previewImageUri
    if (imgUri != null) {
        var bitmap by remember(imgUri) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
        var error by remember(imgUri) { mutableStateOf<String?>(null) }

        LaunchedEffect(imgUri) {
            bitmap = null
            error = null
            runCatching {
                val cr = context.contentResolver
                cr.openInputStream(imgUri)?.use { input ->
                    val bmp = BitmapFactory.decodeStream(input) ?: error("Decode failed")
                    bitmap = bmp.asImageBitmap()
                } ?: error("Nelze otevřít stream.")
            }.onFailure { e ->
                error = e.message ?: "Chyba načtení obrázku"
            }
        }

        AlertDialog(
            onDismissRequest = { previewImageUri = null },
            title = { Text("Foto") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        tonalElevation = 1.dp,
                        border = ButtonDefaults.outlinedButtonBorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp, max = 520.dp)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            when {
                                bitmap != null -> {
                                    Image(
                                        bitmap = bitmap!!,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
                                else -> Text("Načítám…")
                            }
                        }
                    }
                    Text("URI:\n$imgUri", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { TextButton(onClick = { previewImageUri = null }) { Text("Zavřít") } }
        )
    }
}

@Composable
private fun EditorActionIcon(icon: ImageVector, desc: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = desc)
        }
    }
}

private data class InlineBuildResult(
    val annotated: AnnotatedString,
    val inlineContent: Map<String, InlineTextContent>
)

private fun dpToSp(dp: Dp, density: Density): TextUnit =
    (dp.value / density.fontScale).sp

private fun buildInline(
    raw: String,
    density: Density,
    playingUri: Uri?,
    onImageClick: (Uri) -> Unit,
    onAudioClick: (Uri) -> Unit,
    onMapClick: (String) -> Unit
): InlineBuildResult {
    val tokens = findInlineTokens(raw)
    if (tokens.isEmpty()) return InlineBuildResult(AnnotatedString(raw), emptyMap())

    val inlineMap = LinkedHashMap<String, InlineTextContent>()
    val annotated = buildAnnotatedString {
        var cursor = 0
        for ((idx, t) in tokens.withIndex()) {
            if (t.start > cursor) append(raw.substring(cursor, t.start))

            val key = "tok_${idx}_${UUID.randomUUID()}"
            when (t.type) {
                InlineTokenType.IMG -> {
                    val uri = runCatching { Uri.parse(t.payload) }.getOrNull() ?: Uri.EMPTY
                    appendInlineContent(key, " ")
                    inlineMap[key] = InlineTextContent(
                        placeholder = Placeholder(
                            width = dpToSp(34.dp, density),
                            height = dpToSp(34.dp, density),
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                        )
                    ) { _: String ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .clickable { onImageClick(uri) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null)
                        }
                    }
                }

                InlineTokenType.AUD -> {
                    val uri = runCatching { Uri.parse(t.payload) }.getOrNull() ?: Uri.EMPTY
                    val isPlaying = (playingUri == uri)

                    appendInlineContent(key, " ")
                    inlineMap[key] = chipInline(
                        density = density,
                        height = 30.dp,
                        text = "Hlasovka",
                        icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        onClick = { onAudioClick(uri) }
                    )
                }

                InlineTokenType.MAP -> {
                    val label = t.payload.split("|", limit = 2).getOrNull(1).orEmpty()
                    val text = if (label.isBlank()) "Místo" else "Místo: ${label.take(10)}"
                    appendInlineContent(key, " ")
                    inlineMap[key] = chipInline(
                        density = density,
                        height = 30.dp,
                        text = text,
                        icon = Icons.Default.Place,
                        onClick = { onMapClick(t.payload) }
                    )
                }
            }

            cursor = t.endExclusive
        }
        if (cursor < raw.length) append(raw.substring(cursor))
    }

    return InlineBuildResult(annotated, inlineMap)
}

private fun chipInline(
    density: Density,
    height: Dp,
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
): InlineTextContent {
    val widthDp = min(170, 70 + text.length * 6)

    return InlineTextContent(
        placeholder = Placeholder(
            width = dpToSp(widthDp.dp, density),
            height = dpToSp(height, density),
            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
        )
    ) { _: String ->
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
    value: TextFieldValue,
    annotated: AnnotatedString,
    inlineContent: Map<String, InlineTextContent>,
    onValueChange: (TextFieldValue) -> Unit,
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
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Transparent),
                decorationBox = { inner ->
                    if (value.text.isBlank()) {
                        Text(
                            "Zápis…",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    inner()
                }
            )

            Text(
                text = annotated,
                inlineContent = inlineContent,
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
