package com.example.denikplus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.denikplus.ui.theme.DenikPlusTheme

@Composable
fun ConsentScreen(onAgree: () -> Unit) {
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Deník Plus", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(14.dp))
            Text(
                "Aplikace vyžaduje propojení účtu (Google / e-mail / telefon), aby bylo možné bezpečně ukládat a synchronizovat tvoje zápisy.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(18.dp))
            Button(onClick = onAgree, modifier = Modifier.fillMaxWidth()) {
                Text("Souhlasím")
            }
        }
    }
}

@Composable
fun PinAskDialog(
    onYes: () -> Unit,
    onNo: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onNo,
        title = { Text("Bezpečnostní PIN") },
        text = { Text("Chceš nastavit bezpečnostní PIN pro přístup k deníčku při každém spuštění?") },
        confirmButton = { TextButton(onClick = onYes) { Text("ANO") } },
        dismissButton = { TextButton(onClick = onNo) { Text("NE") } }
    )
}

@Composable
fun PinSetupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var pin1 by remember { mutableStateOf("") }
    var pin2 by remember { mutableStateOf("") }
    val ok = pin1.length >= 4 && pin1 == pin2

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nastavit PIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = pin1,
                    onValueChange = { if (it.length <= 8) pin1 = it.filter(Char::isDigit) },
                    label = { Text("PIN (4–8 číslic)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = pin2,
                    onValueChange = { if (it.length <= 8) pin2 = it.filter(Char::isDigit) },
                    label = { Text("PIN znovu") },
                    singleLine = true
                )
                if (pin2.isNotEmpty() && pin1 != pin2) {
                    Text("PINy se neshodují.", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (ok) onConfirm(pin1) }, enabled = ok) { Text("Uložit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Zrušit") } }
    )
}

@Composable
fun PinUnlockScreen(
    errorText: String?,
    onSubmit: (String) -> Unit,
) {
    var pin by remember { mutableStateOf("") }

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Zadej PIN", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 8) pin = it.filter(Char::isDigit) },
                label = { Text("PIN") },
                singleLine = true
            )

            if (errorText != null) {
                Spacer(Modifier.height(8.dp))
                Text(errorText, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { onSubmit(pin) },
                enabled = pin.length >= 4,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Odemknout") }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConsentScreenPreview() {
    DenikPlusTheme {
        ConsentScreen(onAgree = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PinAskDialogPreview() {
    DenikPlusTheme {
        PinAskDialog(onYes = {}, onNo = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PinSetupDialogPreview() {
    DenikPlusTheme {
        PinSetupDialog(onDismiss = {}, onConfirm = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PinUnlockScreenPreview() {
    DenikPlusTheme {
        PinUnlockScreen(errorText = null, onSubmit = {})
    }
}
