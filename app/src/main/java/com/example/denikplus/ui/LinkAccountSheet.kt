package com.example.denikplus.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.denikplus.R

enum class LinkMethod { GOOGLE, EMAIL, PHONE, ALL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkAccountSheet(
    onDismiss: () -> Unit,
    onPick: (LinkMethod) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Propojit účet", style = MaterialTheme.typography.titleLarge)
            Text(
                "Vyber způsob přihlášení / propojení účtu:",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProviderTile(
                    title = "Google",
                    subtitle = "Doporučeno",
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    onClick = { onPick(LinkMethod.GOOGLE) }
                )

                ProviderTile(
                    title = "E-mail",
                    subtitle = "Heslo / link",
                    icon = { Icon(Icons.Default.Email, contentDescription = "E-mail") },
                    modifier = Modifier.weight(1f),
                    onClick = { onPick(LinkMethod.EMAIL) }
                )
            }

            ProviderTile(
                title = "Telefon",
                subtitle = "SMS ověření",
                icon = { Icon(Icons.Default.PhoneAndroid, contentDescription = "Telefon") },
                modifier = Modifier.fillMaxWidth(),
                onClick = { onPick(LinkMethod.PHONE) }
            )

            Divider()

            TextButton(
                onClick = { onPick(LinkMethod.ALL) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Zobrazit všechny možnosti")
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ProviderTile(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            icon()
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
