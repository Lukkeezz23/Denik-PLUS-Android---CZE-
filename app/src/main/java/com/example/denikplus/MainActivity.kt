package com.example.denikplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.denikplus.ui.AppRoot
import com.example.denikplus.ui.RootGate
import com.example.denikplus.ui.theme.DenikPlusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DenikPlusTheme {
                AppRoot()
            }
        }
    }
}
