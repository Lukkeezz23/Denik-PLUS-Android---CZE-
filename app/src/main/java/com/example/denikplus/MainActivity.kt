package com.example.denikplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.denikplus.ui.AuthGate
import com.example.denikplus.ui.theme.DenikPlusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DenikPlusTheme {
                AuthGate()
            }
        }
    }
}
