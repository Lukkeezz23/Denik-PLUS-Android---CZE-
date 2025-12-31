package com.example.denikplus.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.denikplus.data.AppPrefs
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    onDismiss: () -> Unit,
    onRequestPinSetup: () -> Unit, // <-- parent otevře PinSetupDialog
) {
    val context = LocalContext.current
    val prefs = remember { AppPrefs(context) }
    val scope = rememberCoroutineScope()

    val pinEnabled by prefs.pinEnabled.collectAsState(initial = false)
    val biometricEnabled by prefs.biometricEnabled.collectAsState(initial = false)

    val activity = context as? FragmentActivity
    val biometricAvailable = remember(activity) { activity != null && canUseBiometrics(activity) }

    var showChangePin by remember { mutableStateOf(false) }
    var info by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Nastavení", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            // PIN toggle
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("Bezpečnostní PIN", style = MaterialTheme.typography.titleMedium)
                    Text("Při spuštění vyžadovat odemknutí.", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = pinEnabled,
                    onCheckedChange = { checked ->
                        scope.launch {
                            if (!checked) {
                                prefs.disablePin()
                            } else {
                                // zapnutí PINu -> rovnou nastav nový PIN
                                onDismiss()
                                onRequestPinSetup()
                            }
                        }
                    }
                )
            }

            Spacer(Modifier.height(10.dp))

            // Biometrika toggle (jen když je PIN aktivní)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("Biometrika", style = MaterialTheme.typography.titleMedium)
                    Text("Rychlé odemknutí otiskem/obličejem.", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = biometricEnabled,
                    enabled = pinEnabled && biometricAvailable,
                    onCheckedChange = { checked ->
                        scope.launch { prefs.setBiometricEnabled(checked) }
                    }
                )
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = { showChangePin = true },
                enabled = pinEnabled,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Změnit PIN") }

            if (info != null) {
                Spacer(Modifier.height(10.dp))
                Text(info!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(20.dp))
        }
    }

    if (showChangePin) {
        ChangePinDialog(
            canUseBiometrics = pinEnabled && biometricAvailable && biometricEnabled,
            onBiometric = {
                val act = activity ?: return@ChangePinDialog
                showBiometricPrompt(
                    activity = act,
                    title = "Ověření",
                    subtitle = "Potvrď změnu PINu biometrikou",
                    onSuccess = {
                        showChangePin = false
                        info = null
                        onDismiss()
                        onRequestPinSetup()
                    },
                    onError = { msg -> info = msg }
                )
            },
            onCancel = { showChangePin = false },
            onVerified = {
                // ověřeno starým PINem -> otevřeme setup nového PINu
                showChangePin = false
                info = null
                onDismiss()
                onRequestPinSetup()
            },
            verifyPin = { input, callback ->
                scope.launch {
                    val ok = prefs.verifyPin(input)
                    callback(ok)
                }
            }
        )
    }
}

@Composable
private fun ChangePinDialog(
    canUseBiometrics: Boolean,
    onBiometric: () -> Unit,
    onCancel: () -> Unit,
    onVerified: () -> Unit,
    verifyPin: (String, (Boolean) -> Unit) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Ověř stávající PIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 8) pin = it.filter(Char::isDigit) },
                    label = { Text("Stávající PIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (err != null) {
                    Text(err!!, color = MaterialTheme.colorScheme.error)
                }

                if (canUseBiometrics) {
                    OutlinedButton(
                        onClick = onBiometric,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ověřit biometrikou")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = pin.length >= 4,
                onClick = {
                    verifyPin(pin) { ok ->
                        if (ok) onVerified() else err = "Nesprávný PIN."
                    }
                }
            ) { Text("Pokračovat") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Zrušit") } }
    )
}
