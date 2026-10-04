package com.pclauncher

import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

private fun normalisasi(teks: String): String {
    val t = teks.trim()
    return when {
        t.isEmpty() -> "https://duckduckgo.com"
        t.startsWith("http://") || t.startsWith("https://") -> t
        !t.contains(" ") && t.contains(".") -> "https://$t"
        else -> "https://duckduckgo.com/?q=" + Uri.encode(t)
    }
}

@Composable
fun AplikasiPeramban(urlAwal: String) {
    var alamat by remember { mutableStateOf(urlAwal) }
    val ref = remember { arrayOfNulls<WebView>(1) }
    DisposableEffect(Unit) { onDispose { ref[0]?.destroy() } }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { ref[0]?.goBack() }, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("◀") }
            TextButton(onClick = { ref[0]?.goForward() }, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("▶") }
            TextButton(onClick = { ref[0]?.reload() }, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("⟳") }
            OutlinedTextField(
                value = alamat,
                onValueChange = { alamat = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { ref[0]?.loadUrl(normalisasi(alamat)) }),
                modifier = Modifier.weight(1f),
            )
        }
        AndroidView(
            factory = { c ->
                WebView(c).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            if (url != null) alamat = url
                        }
                    }
                    loadUrl(normalisasi(urlAwal))
                    ref[0] = this
                }
            },
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )
    }
}
