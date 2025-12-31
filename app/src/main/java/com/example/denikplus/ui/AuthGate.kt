package com.example.denikplus.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthGate() {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    var user by remember { mutableStateOf(auth.currentUser) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = FirebaseAuthUIActivityResultContract()
    ) { res ->
        val response: IdpResponse? = res.idpResponse
        if (res.resultCode == Activity.RESULT_OK) {
            user = auth.currentUser
            errorText = null
        } else {
            errorText = response?.error?.localizedMessage ?: "Přihlášení zrušeno."
        }
    }

    if (user == null) {
        Surface(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Deník Plus", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(16.dp))

                Button(onClick = {
                    val providers = arrayListOf(
                        AuthUI.IdpConfig.GoogleBuilder().build(),
                        AuthUI.IdpConfig.EmailBuilder().build(),  // email + heslo
                        AuthUI.IdpConfig.PhoneBuilder().build(),  // telefon
                    )

                    val intent = AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build()

                    signInLauncher.launch(intent)
                }) {
                    Text("Přihlásit se")
                }

                if (errorText != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(errorText!!, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    } else {
        // Přihlášen -> naše appka
        DenikPlusApp(
            uid = user!!.uid,
            onLogout = {
                AuthUI.getInstance().signOut(context).addOnCompleteListener {
                    user = null
                }
            }
        )
    }
}
