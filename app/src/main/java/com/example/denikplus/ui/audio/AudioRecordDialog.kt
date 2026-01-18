// FILE: ui/audio/AudioRecordDialog.kt
package com.example.denikplus.ui.audio

import android.Manifest
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun AudioRecordDialog(
    recorder: AudioRecorder,
    onDismiss: () -> Unit,
    onRecorded: (file: File) -> Unit
) {
    var hasPerm by remember { mutableStateOf(false) }
    var recording by remember { mutableStateOf(false) }
    var startAt by remember { mutableLongStateOf(0L) }
    var lastFile by remember { mutableStateOf<File?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPerm = granted
        if (!granted) error = "Bez povolení mikrofonu nelze nahrávat."
    }

    LaunchedEffect(Unit) {
        permLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    fun startRec() {
        if (!hasPerm) {
            permLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        error = null
        recording = true
        startAt = SystemClock.elapsedRealtime()
        lastFile = runCatching { recorder.start() }.getOrNull()
    }

    fun stopRec() {
        recording = false
        val f = runCatching { recorder.stop() }.getOrNull()
        if (f != null) lastFile = f
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nahrát hlasovku") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    if (!recording) "Podrž mikrofon pro nahrávání." else "Nahrávám… pusť pro zastavení.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(hasPerm, recording) {
                            detectTapGestures(
                                onPress = {
                                    startRec()
                                    try {
                                        awaitRelease()
                                    } finally {
                                        if (recording) stopRec()
                                    }
                                }
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.size(10.dp))
                        Text(if (recording) "Nahrávám…" else "Podrž a mluv")
                    }
                }

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }

                val f = lastFile
                if (f != null && !recording) {
                    Text(
                        "Nahrávka připravena (${f.length() / 1024} KB)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val f = lastFile
                    if (f != null && f.exists() && f.length() > 0) onRecorded(f)
                    else onDismiss()
                },
                enabled = !recording
            ) { Text("Použít") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !recording) { Text("Zrušit") }
        }
    )
}
