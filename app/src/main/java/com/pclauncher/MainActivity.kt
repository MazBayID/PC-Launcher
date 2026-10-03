package com.pclauncher

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    private var versi by mutableIntStateOf(0)
    private var sinyalHome by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gelap = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = gelap, navigationBarStyle = gelap)
        val pengaturan = Pengaturan(applicationContext)
        setContent { PcTheme { Desktop(pengaturan, versi, sinyalHome) } }
    }

    override fun onResume() {
        super.onResume()
        versi++ // muat ulang daftar aplikasi (mis. setelah memasang atau mencopot)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        sinyalHome++ // tombol Home ditekan lagi: tutup menu
    }
}

@Composable
fun PcTheme(content: @Composable () -> Unit) {
    val gelap = isSystemInDarkTheme()
    val ctx = LocalContext.current
    val skema = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (gelap) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        gelap -> darkColorScheme(primary = Color(0xFF8FB8DE))
        else -> lightColorScheme(primary = Color(0xFF1E88E5))
    }
    MaterialTheme(colorScheme = skema, content = content)
}
