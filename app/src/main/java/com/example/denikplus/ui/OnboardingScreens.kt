package com.example.denikplus.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ConsentScreen(onAgree: () -> Unit) {
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkAccountSheet(
    onGoogle: () -> Unit,
    onEmail: () -> Unit,
    onPhone: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Propojit účet", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onGoogle, modifier = Modifier.fillMaxWidth()) { Text("Google") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onEmail, modifier = Modifier.fillMaxWidth()) { Text("E-mail / heslo") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onPhone, modifier = Modifier.fillMaxWidth()) { Text("Telefon") }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun PinAskDialog(
    onYes: () -> Unit,
    onNo: () -> Unit
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
    onConfirm: (String) -> Unit
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
    showBiometric: Boolean,
    onBiometricClick: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
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

            if (showBiometric) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onBiometricClick,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Odemknout biometrikou") }
            }
        }
    }
}
