package com.example.denikplus.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
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

private tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}

@Composable
fun RootGate(prefs: AppPrefs) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }

    val consentGiven by prefs.consentGiven.collectAsState(initial = false)
    val pinPromptDone by prefs.pinPromptDone.collectAsState(initial = false)
    val pinEnabled by prefs.pinEnabled.collectAsState(initial = false)
    val hasPinSet by prefs.hasPinSet.collectAsState(initial = false)

    var user by remember { mutableStateOf(auth.currentUser) }

    // PIN unlock state
    var pinUnlocked by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf<String?>(null) }

    // UI state
    var showLinkSheet by remember { mutableStateOf(false) }
    var showPinAsk by remember { mutableStateOf(false) }
    var showPinSetup by remember { mutableStateOf(false) }

    // Když se PIN zapne/vypne, uprav zamčení
    LaunchedEffect(pinEnabled, hasPinSet) {
        if (pinEnabled && hasPinSet) {
            pinUnlocked = false
            pinError = null
        } else {
            pinUnlocked = true
            pinError = null
        }
    }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = FirebaseAuthUIActivityResultContract()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            user = auth.currentUser
            Toast.makeText(context, "Účet propojen.", Toast.LENGTH_SHORT).show()
            showLinkSheet = false

            // Po propojení – pokud ještě nebyl prompt na PIN, ukaž dotaz
            scope.launch {
                if (!pinPromptDone) showPinAsk = true
            }
        } else {
            if (auth.currentUser == null) showLinkSheet = true
        }
    }

    // 1) Consent gate
    if (!consentGiven) {
        ConsentScreen(
            onAgree = {
                scope.launch { prefs.setConsentGiven(true) }
                showLinkSheet = true
            }
        )
        return
    }

    // 2) Pokud nejsi přihlášený, vynucuj link sheet
    if (user == null) {
        LaunchedEffect(Unit) { showLinkSheet = true }
    }

    // 3) Link sheet
    if (showLinkSheet) {
        LinkAccountSheet(
            onDismiss = { showLinkSheet = auth.currentUser == null },
            onPick = { method ->
                val intent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providersFor(method))
                    .build()
                signInLauncher.launch(intent)
            }
        )
    }

    // 4) Dotaz “chceš PIN?”
    if (showPinAsk) {
        PinAskDialog(
            onYes = {
                showPinAsk = false
                showPinSetup = true
            },
            onNo = {
                showPinAsk = false
                scope.launch {
                    prefs.disablePin()
                    prefs.setPinPromptDone(true)
                }
            }
        )
    }

    // 5) Setup PIN
    if (showPinSetup) {
        PinSetupDialog(
            onDismiss = { showPinSetup = false },
            onConfirm = { pin ->
                scope.launch {
                    prefs.setPin(pin)
                    prefs.setPinPromptDone(true)
                    pinUnlocked = true
                    pinError = null
                }
                showPinSetup = false
            }
        )
    }

    // 6) PIN unlock (jen pokud user != null a PIN je zapnutý a existuje)
    if (user != null && pinEnabled && hasPinSet && !pinUnlocked) {
        PinUnlockScreen(
            errorText = pinError,
            onSubmit = { pin ->
                scope.launch {
                    val ok = prefs.verifyPin(pin)
                    if (ok) {
                        pinUnlocked = true
                        pinError = null
                    } else {
                        pinError = "Nesprávný PIN."
                    }
                }
            }
        )
        return
    }

    // 7) Až když je user a gate odemčená -> main app
    val uid = user?.uid
    if (uid != null && (pinUnlocked || !pinEnabled || !hasPinSet)) {
        DenikPlusApp(
            uid = uid,
            prefs = prefs,
            onLogout = {
                auth.signOut()
                user = null
                showLinkSheet = true
                pinUnlocked = false
                pinError = null
            }
        )
    }
}
