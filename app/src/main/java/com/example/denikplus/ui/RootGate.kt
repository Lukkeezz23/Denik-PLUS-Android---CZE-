package com.example.denikplus.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.example.denikplus.data.AppPrefs
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

private fun providersFor(method: LinkMethod) = when (method) {
    LinkMethod.GOOGLE -> arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
    LinkMethod.EMAIL -> arrayListOf(AuthUI.IdpConfig.EmailBuilder().build())
    LinkMethod.PHONE -> arrayListOf(AuthUI.IdpConfig.PhoneBuilder().build())
    LinkMethod.ALL -> arrayListOf(
        AuthUI.IdpConfig.GoogleBuilder().build(),
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.PhoneBuilder().build(),
    )
}

@Composable
fun RootGate() {
    val context = LocalContext.current
    val prefs = remember { AppPrefs(context) }
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }

    val consentGiven by prefs.consentGiven.collectAsState(initial = false)
    val pinPromptDone by prefs.pinPromptDone.collectAsState(initial = false)
    val pinEnabled by prefs.pinEnabled.collectAsState(initial = false)
    val biometricEnabled by prefs.biometricEnabled.collectAsState(initial = false)

    val activity = context as? FragmentActivity
    val biometricAvailable = remember(activity) { activity != null && canUseBiometrics(activity) }

    var user by remember { mutableStateOf(auth.currentUser) }

    // PIN unlock state
    var pinUnlocked by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf<String?>(null) }

    // UI state
    var showLinkSheet by remember { mutableStateOf(false) }
    var showPinAsk by remember { mutableStateOf(false) }
    var showPinSetup by remember { mutableStateOf(false) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = FirebaseAuthUIActivityResultContract()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            user = auth.currentUser
            showLinkSheet = false

            // po prvním úspěšném propojení se zeptáme na PIN (jen jednou)
            scope.launch {
                if (!pinPromptDone) {
                    showPinAsk = true
                    prefs.setPinPromptDone(true)
                }
            }
        } else {
            // zrušeno / fail -> pokud user stále null, necháme sheet zůstat otevřený
            if (auth.currentUser == null) showLinkSheet = true
        }
    }

    // 1) Souhlas obrazovka
    if (!consentGiven) {
        ConsentScreen(
            onAgree = {
                scope.launch { prefs.setConsentGiven(true) }
                showLinkSheet = true
            }
        )
    } else {
        // 2) Pokud není user, vyžadujeme propojení přes sheet
        if (user == null) {
            LaunchedEffect(Unit) { showLinkSheet = true }
        }

        // 3) PIN unlock
        if (user != null && pinEnabled && !pinUnlocked) {
            PinUnlockScreen(
                errorText = pinError,
                showBiometric = biometricEnabled && biometricAvailable,
                onBiometricClick = {
                    val act = activity ?: return@PinUnlockScreen
                    showBiometricPrompt(
                        activity = act,
                        title = "Odemknout deníček",
                        subtitle = "Použij biometriku",
                        onSuccess = {
                            pinUnlocked = true
                            pinError = null
                        },
                        onError = { msg -> pinError = msg }
                    )
                },
                onSubmit = { input ->
                    scope.launch {
                        val ok = prefs.verifyPin(input)
                        if (ok) {
                            pinUnlocked = true
                            pinError = null
                        } else {
                            pinError = "Nesprávný PIN."
                        }
                    }
                }
            )
        } else if (user != null) {
            // 4) Appka
            DenikPlusApp(
                uid = user!!.uid,
                onLogout = {
                    AuthUI.getInstance().signOut(context).addOnCompleteListener {
                        user = null
                        pinUnlocked = false
                        pinError = null
                        showLinkSheet = true
                    }
                }
            )
        }
    }

    // --- Modal: výběr metody přihlášení (ikonky) ---
    if (showLinkSheet) {
        LinkAccountSheet(
            onDismiss = {
                // když user není přihlášený, nenecháme sheet zavřít (account linking je required)
                showLinkSheet = auth.currentUser == null
            },
            onPick = { method ->
                val intent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providersFor(method))
                    .build()

                signInLauncher.launch(intent)
            }
        )
    }

    // --- Dialog: ptáme se na PIN (jen po prvním propojení) ---
    if (showPinAsk) {
        PinAskDialog(
            onYes = {
                showPinAsk = false
                showPinSetup = true
            },
            onNo = {
                showPinAsk = false
                scope.launch { prefs.disablePin() }
            }
        )
    }

    // --- Dialog: nastavení PINu ---
    if (showPinSetup) {
        PinSetupDialog(
            onDismiss = { showPinSetup = false },
            onConfirm = { pin ->
                scope.launch {
                    prefs.setPin(pin)
                    pinUnlocked = true
                    pinError = null
                    showPinSetup = false
                }
            }
        )
    }
}
