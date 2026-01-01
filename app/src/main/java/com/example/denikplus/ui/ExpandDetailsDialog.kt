// FILE: ui/ExpandDetailsDialog.kt
package com.example.denikplus.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun ExpandDetailsDialog(
    onNo: () -> Unit,
    onYes: () -> Unit,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(visible = visible, enter = fadeIn() + scaleIn(initialScale = 0.96f)) {
            Surface(shape = RoundedCornerShape(20.dp), tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Chcete nyní rozbalit Rozšířený detailní zápis?", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Detailní položky které si nastavíte lze následně používat při textovém zápisu do deníku.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(
                            onClick = onNo,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFFB300))
                        ) { Text("Nyní ne") }

                        TextButton(
                            onClick = onYes,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2E7D32))
                        ) { Text("Ano") }
                    }
                }
            }
        }
    }
}
