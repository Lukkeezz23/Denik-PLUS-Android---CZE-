// FILE: ui/youtube/MusicSearchDialog.kt
package com.example.denikplus.ui.youtube

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MusicSearchDialog(
    apiKey: String,
    onDismiss: () -> Unit,
    onPick: (videoId: String, title: String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var results by remember { mutableStateOf<List<YouTubeVideo>>(emptyList()) }
    var searchTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(searchTrigger) {
        if (searchTrigger == 0) return@LaunchedEffect
        val q = query.trim()
        if (q.isBlank()) return@LaunchedEffect

        loading = true
        error = null
        results = emptyList()

        val res = runCatching {
            withContext(Dispatchers.IO) {
                YouTubeApi.search(apiKey = apiKey, query = q)
            }
        }

        loading = false
        res.onSuccess { results = it }
            .onFailure { error = it.message ?: "Chyba vyhledávání" }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Přidat hudbu") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Název písničky / interpreta") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Hledat",
                            modifier = Modifier.clickable {
                                if (!loading && query.trim().isNotBlank()) searchTrigger++
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { searchTrigger++ },
                        enabled = query.trim().isNotBlank() && !loading
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Hledat")
                    }
                }

                if (loading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }

                if (!loading && results.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(380.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(results) { v ->
                            Surface(
                                shape = MaterialTheme.shapes.large,
                                tonalElevation = 1.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPick(v.videoId, v.title) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = v.thumbUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(72.dp)
                                    )
                                    Column(Modifier.weight(1f)) {
                                        Text(v.title, style = MaterialTheme.typography.titleSmall, maxLines = 2)
                                        Spacer(Modifier.size(4.dp))
                                        Text(
                                            v.channel,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Zavřít") }
        }
    )
}
