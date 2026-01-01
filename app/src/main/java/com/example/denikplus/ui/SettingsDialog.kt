// FILE: ui/SettingsDialog.kt
package com.example.denikplus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val themeMode by prefs.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val lang by prefs.appLanguage.collectAsState(initial = AppLanguage.SYSTEM)

    val useSystemTheme = themeMode == ThemeMode.SYSTEM
    val isDark = themeMode == ThemeMode.DARK

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nastavení") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // --- THEME ---
                Text("Vzhled", style = MaterialTheme.typography.titleMedium)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Použít nastavení systému", modifier = Modifier.weight(1f))
                    Switch(
                        checked = useSystemTheme,
                        onCheckedChange = { checked ->
                            scope.launch {
                                prefs.setThemeMode(if (checked) ThemeMode.SYSTEM else ThemeMode.LIGHT)
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tmavý režim", modifier = Modifier.weight(1f))
                    Switch(
                        checked = isDark,
                        enabled = !useSystemTheme,
                        onCheckedChange = { checked ->
                            scope.launch {
                                prefs.setThemeMode(if (checked) ThemeMode.DARK else ThemeMode.LIGHT)
                            }
                        }
                    )
                }

                Divider()

                // --- LANGUAGE ---
                Text("Jazyk", style = MaterialTheme.typography.titleMedium)

                LanguageRow(
                    title = "Podle systému",
                    selected = lang == AppLanguage.SYSTEM,
                    onClick = { scope.launch { prefs.setAppLanguage(AppLanguage.SYSTEM) } }
                )
                LanguageRow(
                    title = "Čeština",
                    selected = lang == AppLanguage.CS,
                    onClick = { scope.launch { prefs.setAppLanguage(AppLanguage.CS) } }
                )
                LanguageRow(
                    title = "Slovenština",
                    selected = lang == AppLanguage.SK,
                    onClick = { scope.launch { prefs.setAppLanguage(AppLanguage.SK) } }
                )
                LanguageRow(
                    title = "English",
                    selected = lang == AppLanguage.EN,
                    onClick = { scope.launch { prefs.setAppLanguage(AppLanguage.EN) } }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Zavřít") }
        }
    )
}

@Composable
private fun LanguageRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(title, modifier = Modifier.padding(start = 6.dp))
        Spacer(Modifier.weight(1f))
    }
}
