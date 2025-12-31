package com.example.denikplus.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.denikplus.data.AppPrefs
import com.example.denikplus.data.EntriesRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DenikPlusApp(
    uid: String,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { AppPrefs(context) }
    val scope = rememberCoroutineScope()

    val repo = remember { EntriesRepository() }
    val vm: CalendarViewModel = viewModel(factory = CalendarViewModelFactory(uid, repo))

    val year by vm.year.collectAsState()
    val counts by vm.counts.collectAsState()

    val selectedDate by vm.selectedDate.collectAsState()
    val dayEntries by vm.dayEntries.collectAsState()

    val pinEnabled by prefs.pinEnabled.collectAsState(initial = false)
    val biometricEnabled by prefs.biometricEnabled.collectAsState(initial = false)

    val activity = context as? FragmentActivity
    val biometricAvailable = remember(activity) { activity != null && canUseBiometrics(activity) }

    var createForDate by remember { mutableStateOf<LocalDate?>(null) }

    // Settings UI state
    var showSettings by remember { mutableStateOf(false) }
    var showChangePinVerify by remember { mutableStateOf(false) }
    var showPinSetup by remember { mutableStateOf(false) }
    var pinSetupReason by remember { mutableStateOf("enable") } // enable / change (zatím jen info)
    var pinVerifyError by remember { mutableStateOf<String?>(null) }
    var showBiometricOffer by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deník Plus") },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Nastavení")
                    }
                    TextButton(onClick = onLogout) { Text("Odhlásit") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { createForDate = LocalDate.now() }) {
                Icon(Icons.Default.Add, contentDescription = "Nový zápis")
            }
        }
    ) { padding ->
        YearCalendarScreen(
            year = year,
            entriesByDate = counts,
            onPrevYear = { vm.prevYear() },
            onNextYear = { vm.nextYear() },
            onDayClick = { date -> vm.openDay(date) },
            contentPadding = padding
        )
    }

    // --- Detail dne (list zápisů) ---
    if (selectedDate != null) {
        DayEntriesSheet(
            date = selectedDate!!,
            entries = dayEntries,
            onAddClick = { createForDate = selectedDate },
            onDismiss = { vm.closeDay() }
        )
    }

    // --- Mood picker (vytvoření rychlého záznamu) ---
    val date = createForDate
    if (date != null) {
        MoodPickerDialog(
            date = date,
            onDismiss = { createForDate = null },
            onConfirm = { mood ->
                val label = when (mood) {
                    is MoodChoice.Preset -> mood.label
                    is MoodChoice.Custom -> mood.text.ifBlank { "Jinak" }
                }
                vm.addEntry(date, label)
                createForDate = null
            }
        )
    }

    // --- Settings sheet ---
    if (showSettings) {
        SettingsSheet(
            onDismiss = { showSettings = false },
            onRequestPinSetup = {
                // otevři tvůj PinSetupDialog
                showPinSetup = true
            }
        )
    }


    // --- Ověření pro změnu PINu (starý PIN nebo biometrika) ---
    if (showChangePinVerify) {
        ChangePinVerifyDialog(
            canUseBiometrics = pinEnabled && biometricEnabled && biometricAvailable,
            errorText = pinVerifyError,
            onDismiss = {
                showChangePinVerify = false
                pinVerifyError = null
            },
            onBiometric = {
                val act = activity ?: return@ChangePinVerifyDialog
                showBiometricPrompt(
                    activity = act,
                    title = "Ověření",
                    subtitle = "Potvrď změnu PINu biometrikou",
                    onSuccess = {
                        showChangePinVerify = false
                        pinVerifyError = null
                        pinSetupReason = "change"
                        showPinSetup = true
                    },
                    onError = { msg -> pinVerifyError = msg }
                )
            },
            onVerifyPin = { input ->
                scope.launch {
                    val ok = prefs.verifyPin(input)
                    if (ok) {
                        showChangePinVerify = false
                        pinVerifyError = null
                        pinSetupReason = "change"
                        showPinSetup = true
                    } else {
                        pinVerifyError = "Nesprávný PIN."
                    }
                }
            }
        )
    }

    // --- Nastavení nového PINu (pro zapnutí i změnu) ---
    if (showPinSetup) {
        PinSetupDialog(
            onDismiss = { showPinSetup = false },
            onConfirm = { newPin ->
                scope.launch {
                    prefs.setPin(newPin)
                    showPinSetup = false

                    // nabídka biometriky hned po nastavení PINu
                    if (biometricAvailable && !biometricEnabled) {
                        showBiometricOffer = true
                    }
                }
            }
        )
    }

    // --- Nabídka zapnutí biometriky po nastavení PINu ---
    if (showBiometricOffer) {
        AlertDialog(
            onDismissRequest = { showBiometricOffer = false },
            title = { Text("Povolit biometriku?") },
            text = { Text("Chceš povolit odemykání deníčku pomocí otisku prstu / obličeje (nebo zámku zařízení)?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { prefs.setBiometricEnabled(true) }
                    showBiometricOffer = false
                }) { Text("ANO") }
            },
            dismissButton = {
                TextButton(onClick = { showBiometricOffer = false }) { Text("NE") }
            }
        )
    }
}

@Composable
private fun ChangePinVerifyDialog(
    canUseBiometrics: Boolean,
    errorText: String?,
    onDismiss: () -> Unit,
    onBiometric: () -> Unit,
    onVerifyPin: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
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

                if (errorText != null) {
                    Text(errorText, color = MaterialTheme.colorScheme.error)
                }

                if (canUseBiometrics) {
                    OutlinedButton(
                        onClick = onBiometric,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Ověřit biometrikou") }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = pin.length >= 4,
                onClick = { onVerifyPin(pin) }
            ) { Text("Pokračovat") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Zrušit") }
        }
    )
}
