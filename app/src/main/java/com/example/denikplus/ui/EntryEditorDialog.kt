// FILE: ui/EntryEditorDialog.kt
package com.example.denikplus.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.layout.Layout
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
import coil.compose.AsyncImage
import com.example.denikplus.BuildConfig
import com.example.denikplus.data.DetailSelection
import com.example.denikplus.ui.audio.AudioRecordDialog
import com.example.denikplus.ui.audio.AudioRecorder
import com.example.denikplus.ui.youtube.MusicSearchDialog
import com.example.denikplus.ui.youtube.YouTubeApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.min
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EntryEditorDialog(
    date: LocalDate,
    dialogTitle: String,
    initialEntryTitle: String,
    moodLabel: String,
    initialText: String = "",
    initialDetails: List<DetailSelection> = emptyList(),
    readOnly: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (title: String, moodLabel: String, text: String, details: List<DetailSelection>) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var details by remember { mutableStateOf(initialDetails) }

    var value by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialText,
                selection = TextRange(initialText.length)
            )
        )
    }

    var entryTitle by remember { mutableStateOf(initialEntryTitle) }
    var mood by remember { mutableStateOf(moodLabel) }

    var showMoodPicker by remember { mutableStateOf(false) }
    var showTitleEdit by remember { mutableStateOf(false) }
    var showDetailsPicker by remember { mutableStateOf(false) }

    // ✅ pro readOnly: komentář k aktivitě
    var detailNote by remember { mutableStateOf<DetailSelection?>(null) }

    // ✅ edit dialog pro komentář aktivity (edit režim)
    var editActivityNoteFor by remember { mutableStateOf<DetailSelection?>(null) }

    fun insertToken(token: String) {
        if (readOnly) return
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

    val context = LocalContext.current

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null && !readOnly) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            insertToken(buildToken(InlineTokenType.IMG, uri.toString()))
        }
    }

    var showAudioRecord by remember { mutableStateOf(false) }
    val recorder = remember { AudioRecorder(context.applicationContext) }

    var showMusicSearch by remember { mutableStateOf(false) }

    var askMap by remember { mutableStateOf(false) }
    var inputLat by remember { mutableStateOf("") }
    var inputLon by remember { mutableStateOf("") }
    var inputLabel by remember { mutableStateOf("") }

    var previewImageUri by remember { mutableStateOf<Uri?>(null) }

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

    fun openMusic(videoId: String) {
        val ytMusic = Uri.parse("https://music.youtube.com/watch?v=$videoId")
        val ytWeb = Uri.parse("https://www.youtube.com/watch?v=$videoId")
        val intentMusic =
            Intent(Intent.ACTION_VIEW, ytMusic).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val intentWeb = Intent(Intent.ACTION_VIEW, ytWeb).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        runCatching { safeStartActivity(context, intentMusic) }
            .onFailure { runCatching { safeStartActivity(context, intentWeb) } }
    }

    val density = LocalDensity.current
    val inlineBuilt = remember(value.text, playingUri) {
        buildInline(
            raw = value.text,
            density = density,
            playingUri = playingUri,
            onImageClick = { uri -> previewImageUri = uri },
            onAudioClick = { uri -> togglePlay(uri) },
            onMapClick = { payload -> openMap(payload) },
            onDetailClick = { /* zatím nic */ },
            onMusicClick = { videoId, _ -> openMusic(videoId) }
        )
    }

    val dateTitle = remember(date) {
        date.format(DateTimeFormatter.ofPattern("d. M. yyyy"))
    }

    val baseTitleSize = MaterialTheme.typography.titleLarge.fontSize
    val moodFont = if (baseTitleSize != TextUnit.Unspecified) {
        (baseTitleSize.value * 1.15f).sp
    } else {
        26.sp
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                Text(
                    dialogTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = entryTitle,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.clickable(enabled = !readOnly) {
                                    showTitleEdit = true
                                }
                            )

                            Spacer(Modifier.size(10.dp))

                            Text(
                                text = mood,
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = moodFont),
                                modifier = Modifier.clickable(enabled = !readOnly) {
                                    showMoodPicker = true
                                }
                            )

                            if (!readOnly) {
                                Spacer(Modifier.size(6.dp))
                                Text(
                                    "›",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier
                                        .clickable { showMoodPicker = true }
                                        .padding(horizontal = 2.dp)
                                )
                                Spacer(Modifier.size(6.dp))

                                IconButton(
                                    onClick = { showDetailsPicker = true },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Upravit aktivity"
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    dateTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (details.isNotEmpty()) {
                    Text(
                        if (readOnly) "Aktivity: (klik zobrazí komentář)"
                        else "Aktivity: (klik otevře menu)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowWrapRow(horizontalGap = 10.dp, verticalGap = 10.dp) {
                        details.forEach { d ->
                            key(d.itemId) {
                                var menuExpanded by remember { mutableStateOf(false) }

                                Box {
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.clickable {
                                            if (readOnly) {
                                                if (d.note.isNotBlank()) detailNote = d
                                            } else {
                                                menuExpanded = true
                                            }
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(
                                                horizontal = 10.dp,
                                                vertical = 8.dp
                                            ),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = detailIconFor(d.itemId),
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            if (d.note.isNotBlank()) {
                                                Box(
                                                    Modifier
                                                        .size(6.dp)
                                                        .clip(RoundedCornerShape(99.dp))
                                                        .background(
                                                            MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                                                alpha = 0.6f
                                                            )
                                                        )
                                                )
                                            }
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Přidat") },
                                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                                            onClick = {
                                                menuExpanded = false
                                                scope.launch {
                                                    awaitFrame()
                                                    showDetailsPicker = true
                                                }
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Upravit") },
                                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                            onClick = {
                                                menuExpanded = false
                                                editActivityNoteFor = d
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = { Text("Odstranit") },
                                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                            onClick = {
                                                menuExpanded = false
                                                details = details.filterNot { it.itemId == d.itemId }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (!readOnly) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EditorActionIcon(Icons.Default.Image, "Vložit foto") {
                            pickImageLauncher.launch(arrayOf("image/*"))
                        }
                        Spacer(Modifier.size(12.dp))

                        EditorActionIcon(Icons.Default.Mic, "Nahrát hlasovku") {
                            showAudioRecord = true
                        }
                        Spacer(Modifier.size(12.dp))

                        EditorActionIcon(
                            Icons.Default.MusicNote,
                            "Přidat hudbu"
                        ) { showMusicSearch = true }
                        Spacer(Modifier.size(12.dp))

                        EditorActionIcon(Icons.Default.Place, "Vložit místo") {
                            inputLat = ""
                            inputLon = ""
                            inputLabel = ""
                            askMap = true
                        }
                    }
                }
            }
        },
        text = {
            TextAreaWithInlinePreviews(
                value = value,
                annotated = inlineBuilt.annotated,
                inlineContent = inlineBuilt.inlineContent,
                onValueChange = { if (!readOnly) value = it },
                minHeight = 320.dp,
                maxHeight = 560.dp,
                readOnly = readOnly
            )
        },
        confirmButton = {
            if (!readOnly) {
                TextButton(onClick = { onConfirm(entryTitle, mood, value.text, details) }) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Uložit")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(if (readOnly) "Zavřít" else "Zrušit")
            }
        }
    )

    // ✅ Dialog pro edit komentáře aktivity (edit režim)
    val editNoteTarget = editActivityNoteFor
    if (editNoteTarget != null) {
        var tmp by remember(editNoteTarget) { mutableStateOf(editNoteTarget.note) }

        AlertDialog(
            onDismissRequest = { editActivityNoteFor = null },
            title = { Text("Upravit komentář") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Aktivita: ${editNoteTarget.itemTitle.ifBlank { editNoteTarget.itemId }}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = tmp,
                        onValueChange = { tmp = it },
                        label = { Text("Komentář") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val newNote = tmp.trim()
                    details = details.map {
                        if (it.itemId == editNoteTarget.itemId) it.copy(note = newNote) else it
                    }
                    editActivityNoteFor = null
                }) { Text("Uložit") }
            },
            dismissButton = {
                TextButton(onClick = { editActivityNoteFor = null }) { Text("Zrušit") }
            }
        )
    }

    // ✅ Komentář aktivity (readOnly)
    val dn = detailNote
    if (dn != null) {
        AlertDialog(
            onDismissRequest = { detailNote = null },
            title = { Text("Komentář") },
            text = {
                Text(
                    dn.note.ifBlank { "Bez komentáře." },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { detailNote = null }) { Text("OK") }
            }
        )
    }

    // --- Title edit dialog ---
    if (!readOnly && showTitleEdit) {
        var tmp by remember(entryTitle) { mutableStateOf(entryTitle) }
        AlertDialog(
            onDismissRequest = { showTitleEdit = false },
            title = { Text("Upravit nadpis") },
            text = {
                OutlinedTextField(
                    value = tmp,
                    onValueChange = { tmp = it.take(40) },
                    label = { Text("Nadpis") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    entryTitle = tmp.trim().ifBlank { entryTitle }
                    showTitleEdit = false
                }) { Text("Uložit") }
            },
            dismissButton = {
                TextButton(onClick = { showTitleEdit = false }) { Text("Zrušit") }
            }
        )
    }

    // --- Mood picker ---
    if (!readOnly && showMoodPicker) {
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
                                    ) { Text(m, style = MaterialTheme.typography.titleLarge) }
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

    // --- Edit aktivit ---
    if (!readOnly && showDetailsPicker) {
        val categories = remember { defaultDetailCategories() }
        DetailsPickerSheet(
            categories = categories,
            initial = details,
            onConfirm = { picked ->
                details = picked
                showDetailsPicker = false
            },
            onDismiss = { showDetailsPicker = false }
        )
    }

    // --- Audio record dialog ---
    if (!readOnly && showAudioRecord) {
        AudioRecordDialog(
            recorder = recorder,
            onDismiss = { showAudioRecord = false },
            onRecorded = { file ->
                val uri = recorder.fileToUri(file)
                insertToken(buildToken(InlineTokenType.AUD, uri.toString()))
                showAudioRecord = false
            }
        )
    }

    // --- Music search dialog ---
    if (!readOnly && showMusicSearch) {
        val apiKey = BuildConfig.YOUTUBE_API_KEY
        MusicSearchDialog(
            apiKey = apiKey,
            onDismiss = { showMusicSearch = false },
            onPick = { videoId, titleText ->
                val encoded = Uri.encode(titleText)
                insertToken(buildToken(InlineTokenType.MUS, "$videoId|$encoded"))
                showMusicSearch = false
            }
        )
    }

    // --- Map input ---
    if (!readOnly && askMap) {
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
                    Text("Klik na ikonku v textu otevře mapu.")
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

    // --- Preview IMG (full) ---
    val imgUri = previewImageUri
    if (imgUri != null) {
        var bitmap by remember(imgUri) {
            mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(
                null
            )
        }
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

                                error != null -> Text(
                                    error!!,
                                    color = MaterialTheme.colorScheme.error
                                )

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
private fun EditorActionIcon(
    icon: ImageVector,
    desc: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .size(44.dp)
            .clip(shape)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = desc)
        }
    }
}

private data class InlineBuildResult(
    val annotated: AnnotatedString,
    val inlineContent: Map<String, InlineTextContent>,
)

private fun dpToSp(dp: Dp, density: Density): TextUnit =
    (dp.value / density.fontScale).sp

private fun buildInline(
    raw: String,
    density: Density,
    playingUri: Uri?,
    onImageClick: (Uri) -> Unit,
    onAudioClick: (Uri) -> Unit,
    onMapClick: (String) -> Unit,
    onDetailClick: (String) -> Unit,
    onMusicClick: (videoId: String, title: String) -> Unit,
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
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onImageClick(uri) }
                        )
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

                InlineTokenType.DET -> {
                    appendInlineContent(key, " ")
                    inlineMap[key] = chipInline(
                        density = density,
                        height = 30.dp,
                        text = "Detail",
                        icon = Icons.Default.Tune,
                        onClick = { onDetailClick(t.payload) }
                    )
                }

                InlineTokenType.MUS -> {
                    val parts = t.payload.split("|", limit = 2)
                    val videoId = parts.getOrNull(0).orEmpty()
                    val titleEnc = parts.getOrNull(1).orEmpty()
                    val title = Uri.decode(titleEnc)
                    val thumb = YouTubeApi.defaultThumb(videoId)

                    appendInlineContent(key, " ")
                    inlineMap[key] = InlineTextContent(
                        placeholder = Placeholder(
                            width = dpToSp(60.dp, density),
                            height = dpToSp(34.dp, density),
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 60.dp, height = 34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onMusicClick(
                                        videoId,
                                        if (title.isBlank()) "Hudba" else title
                                    )
                                }
                        ) {
                            AsyncImage(
                                model = thumb,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("♪", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
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
    onClick: () -> Unit,
): InlineTextContent {
    val widthDp = min(170, 70 + text.length * 6)

    return InlineTextContent(
        placeholder = Placeholder(
            width = dpToSp(widthDp.dp, density),
            height = dpToSp(height, density),
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
    value: TextFieldValue,
    annotated: AnnotatedString,
    inlineContent: Map<String, InlineTextContent>,
    onValueChange: (TextFieldValue) -> Unit,
    minHeight: Dp,
    maxHeight: Dp,
    readOnly: Boolean,
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
                enabled = !readOnly,
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

/**
 * Jednoduchý wrap layout bez extra závislostí.
 */
@Composable
private fun FlowWrapRow(
    horizontalGap: Dp,
    verticalGap: Dp,
    content: @Composable () -> Unit,
) {
    Layout(content = content) { measurables, constraints ->
        val maxWidth = constraints.maxWidth
        val hGap = horizontalGap.roundToPx()
        val vGap = verticalGap.roundToPx()

        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }

        val positions = ArrayList<Pair<Int, Int>>(placeables.size)

        var x = 0
        var y = 0
        var rowH = 0

        placeables.forEach { p ->
            if (x > 0 && x + p.width > maxWidth) {
                x = 0
                y += rowH + vGap
                rowH = 0
            }

            positions.add(x to y)
            x += p.width + hGap
            rowH = maxOf(rowH, p.height)
        }

        val height = (y + rowH).coerceIn(constraints.minHeight, constraints.maxHeight)

        layout(width = maxWidth, height = height) {
            placeables.forEachIndexed { i, p ->
                val (px, py) = positions[i]
                p.place(px, py)
            }
        }
    }
}

/**
 * Bezpečný startActivity – nezabije appku, když není handler.
 */
private fun safeStartActivity(context: android.content.Context, intent: Intent) {
    intent.resolveActivity(context.packageManager)?.let {
        context.startActivity(intent)
    } ?: throw IllegalStateException("No activity to handle intent")
}
