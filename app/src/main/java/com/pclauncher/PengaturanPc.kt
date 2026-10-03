package com.pclauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun AplikasiPengaturan(p: Pengaturan) {
    val res = LocalContext.current.resources
    val dm = res.displayMetrics
    val sw = res.configuration.smallestScreenWidthDp
    val sisiPendek = minOf(dm.widthPixels, dm.heightPixels)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Taskbar", style = MaterialTheme.typography.titleMedium)
        Text("Transparansi: ${(p.transparansi * 100).roundToInt()}%", fontSize = 13.sp)
        Slider(value = p.transparansi, onValueChange = { p.aturTransparansi(it) }, valueRange = 0.3f..1f)
        Text("Warna", fontSize = 13.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WARNA_PILIHAN.forEachIndexed { i, w ->
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(w)
                        .border(if (p.warnaBar == i) 3.dp else 1.dp, if (p.warnaBar == i) Color.White else Color.Gray, CircleShape)
                        .clickable { p.aturWarnaBar(i) }
                )
            }
        }
        Baris("Taskbar rata tengah", p.taskbarTengah) { p.aturTengah(it) }

        HorizontalDivider()
        Text("Jendela", style = MaterialTheme.typography.titleMedium)
        Baris("Selalu buka aplikasi dalam jendela", p.selaluJendela) { p.aturJendela(it) }
        Text(
            "Aplikasi Android lain hanya tampil sebagai jendela bila ROM mendukung freeform. " +
                "Berkas, Peramban, dan Pengaturan PC selalu berjalan di jendela milik launcher.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        HorizontalDivider()
        Text("Layar dan DPI", style = MaterialTheme.typography.titleMedium)
        SelectionContainer {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Lebar terkecil sekarang: $sw dp", fontSize = 13.sp)
                Text("Kerapatan: ${dm.densityDpi} dpi", fontSize = 13.sp)
                Text("Resolusi: ${dm.widthPixels} x ${dm.heightPixels} px", fontSize = 13.sp)
                Text("Perintah ADB untuk lebar terkecil tertentu:", fontSize = 13.sp)
                listOf(600, 720, 823).forEach { target ->
                    Text("$target dp: adb shell wm density ${sisiPendek * 160 / target}", fontSize = 12.sp)
                }
                Text("Pulihkan: adb shell wm density reset", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun Baris(label: String, nilai: Boolean, ubah: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), fontSize = 14.sp)
        Switch(checked = nilai, onCheckedChange = ubah)
    }
}
