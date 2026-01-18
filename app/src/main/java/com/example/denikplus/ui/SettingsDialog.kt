// FILE: ui/SettingsDialog.kt
package com.example.denikplus.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.denikplus.data.AppLanguage
import com.example.denikplus.data.AppPrefs
import com.example.denikplus.data.ThemeMode
import kotlinx.coroutines.launch

@Composable
fun SettingsDialog(
    prefs: AppPrefs,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    // PIN
    val pinEnabled by prefs.pinEnabled.collectAsState(initial = false)
    val hasPinSet by prefs.hasPinSet.collectAsState(initial = false)

    // Language + Theme (podle tvého AppPrefs.kt)
    val lang by prefs.appLanguage.collectAsState(initial = AppLanguage.SYSTEM)
    val theme by prefs.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

    var showPinSetup by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nastavení") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // ===== Jazyk =====
                Surface(
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null)
                            Text("Jazyk", style = MaterialTheme.typography.titleMedium)
                        }

                        Divider()

                        LanguageRow(
                            label = "Čeština",
                            selected = lang == AppLanguage.CS,
                            onClick = { scope.launch { prefs.setAppLanguage(AppLanguage.CS) } }
                        )
                        LanguageRow(
                            label = "Slovenčina",
                            selected = lang == AppLanguage.SK,
                            onClick = { scope.launch { prefs.setAppLanguage(AppLanguage.SK) } }
                        )
                        LanguageRow(
                            label = "English",
                            selected = lang == AppLanguage.EN,
                            onClick = { scope.launch { prefs.setAppLanguage(AppLanguage.EN) } }
                        )
                    }
                }

                // ===== Vzhled =====
                Surface(
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                when (theme) {
                                    ThemeMode.DARK -> Icons.Default.DarkMode
                                    else -> Icons.Default.LightMode
                                },
                                contentDescription = null
                            )
                            Text("Vzhled", style = MaterialTheme.typography.titleMedium)
                        }

                        Divider()

                        ThemeRow(
                            label = "Systémový",
                            selected = theme == ThemeMode.SYSTEM,
                            onClick = { scope.launch { prefs.setThemeMode(ThemeMode.SYSTEM) } }
                        )
                        ThemeRow(
                            label = "Světlý",
                            selected = theme == ThemeMode.LIGHT,
                            onClick = { scope.launch { prefs.setThemeMode(ThemeMode.LIGHT) } }
                        )
                        ThemeRow(
                            label = "Tmavý",
                            selected = theme == ThemeMode.DARK,
                            onClick = { scope.launch { prefs.setThemeMode(ThemeMode.DARK) } }
                        )
                    }
                }

                // ===== Zabezpečení / PIN =====
                Surface(
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null)
                            Text("Zabezpečení", style = MaterialTheme.typography.titleMedium)
                        }

                        Divider()

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null)
                            Spacer(Modifier.padding(4.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Používat PIN")
                                Text(
                                    when {
                                        !hasPinSet && pinEnabled -> "PIN není nastavený"
                                        pinEnabled -> "Zapnuto"
                                        else -> "Vypnuto"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = pinEnabled,
                                onCheckedChange = { checked ->
                                    if (checked && !hasPinSet) {
                                        // když zapínáš PIN a žádný není, otevři setup
                                        showPinSetup = true
                                    } else {
                                        scope.launch { prefs.setPinEnabled(checked) }
                                    }
                                }
                            )
                        }

                        if (!hasPinSet) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "TIP: Zapnutí PINu vyžaduje jeho nastavení.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Zavřít") }
        }
    )

    if (showPinSetup) {
        PinSetupDialog(
            onDismiss = { showPinSetup = false },
            onConfirm = { pin ->
                scope.launch {
                    prefs.setPin(pin)          // už nastaví PIN_ENABLED = true
                    // prefs.setPinEnabled(true) // není nutné, ale může zůstat; setPinEnabled to jen potvrdí
                    prefs.setPinEnabled(true)
                }
                showPinSetup = false
            }
        )
    }
}

@Composable
private fun LanguageRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.padding(4.dp))
        Text(label)
    }
}

@Composable
private fun ThemeRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.padding(4.dp))
        Text(label)
    }
}
