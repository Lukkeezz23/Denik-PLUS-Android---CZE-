// FILE: ui/youtube/MusicPlayerSheet.kt
package com.example.denikplus.ui.youtube

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerSheet(
    videoId: String,
    title: String,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth()
        )

        val html = remember(videoId) {
            """
            <!DOCTYPE html>
            <html>
              <body style="margin:0;padding:0;background:#000;">
                <div id="player"></div>
                <iframe
                  width="100%"
                  height="100%"
                  src="https://www.youtube.com/embed/$videoId?autoplay=1&playsinline=1"
                  frameborder="0"
                  allow="autoplay; encrypted-media"
                  allowfullscreen>
                </iframe>
              </body>
            </html>
            """.trimIndent()
        }

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            factory = { ctx ->
                createWebView(ctx).apply {
                    loadDataWithBaseURL(
                        "https://www.youtube.com",
                        html,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            },
            update = { wv ->
                wv.loadDataWithBaseURL(
                    "https://www.youtube.com",
                    html,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun createWebView(ctx: android.content.Context): WebView {
    return WebView(ctx).apply {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        webViewClient = WebViewClient()
    }
}
