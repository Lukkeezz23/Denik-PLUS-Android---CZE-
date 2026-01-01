// FILE: ui/SimpleTwoActionDialog.kt
package com.example.denikplus.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

private val SkipColor = Color(0xFFFFB300)
private val ConfirmColor = Color(0xFF2E7D32)

@Composable
fun SimpleTwoActionDialog(
    title: String,
    message: String,
    dismissText: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText, color = SkipColor, fontWeight = FontWeight.SemiBold)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = ConfirmColor, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
