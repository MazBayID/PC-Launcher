package com.pclauncher

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val HALAMAN = listOf("Personalisasi", "Tampilan", "Jendela", "Sistem", "Tentang")

/** Aplikasi Pengaturan PC: daftar halaman di kiri (layar lebar) atau di atas (layar sempit). */
@Composable
fun AplikasiPengaturan(p: Pengaturan) {
    var hal by remember { mutableIntStateOf(0) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth >= 520.dp) {
            Row(Modifier.fillMaxSize()) {
                Column(
                    Modifier.width(150.dp).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant).padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    HALAMAN.forEachIndexed { i, nama -> ItemHalaman(nama, i == hal) { hal = i } }
                }
                IsiHalaman(hal, p, Modifier.weight(1f))
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().horizontalScrollCompat().padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    HALAMAN.forEachIndexed { i, nama -> ItemHalaman(nama, i == hal) { hal = i } }
                }
                HorizontalDivider()
                IsiHalaman(hal, p, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Modifier.horizontalScrollCompat(): Modifier =
    this.then(Modifier.horizontalScroll(rememberScrollState()))

@Composable
private fun ItemHalaman(nama: String, aktif: Boolean, aksi: () -> Unit) {
    Text(
        nama,
        fontSize = 13.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (aktif) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable { aksi() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

@Composable
private fun IsiHalaman(hal: Int, p: Pengaturan, modifier: Modifier) {
    Column(
        modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (hal) {
            0 -> Personalisasi(p)
            1 -> Tampilan()
            2 -> HalamanJendela(p)
            3 -> Sistem()
            else -> Tentang()
        }
    }
}

@Composable
private fun Personalisasi(p: Pengaturan) {
    Text("Wallpaper", style = MaterialTheme.typography.titleMedium)
    daftarWallpaper.forEachIndexed { i, w ->
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { p.pilihWallpaper(i) }
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(width = 56.dp, height = 36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Brush.verticalGradient(w.warna))
            )
            Spacer(Modifier.width(12.dp))
            Text(w.nama + if (i == p.wallpaper && !p.pakaiFoto) "  ✓" else "", fontSize = 14.sp)
        }
    }
    Text(
        "Untuk memakai foto, klik kanan atau tekan lama desktop lalu pilih \"Wallpaper dari foto\".",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    HorizontalDivider()
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
}

@Composable
private fun Tampilan() {
    val res = LocalContext.current.resources
    val dm = res.displayMetrics
    val sw = res.configuration.smallestScreenWidthDp
    val sisiPendek = minOf(dm.widthPixels, dm.heightPixels)
    Text("Skala layar", style = MaterialTheme.typography.titleMedium)
    SelectionContainer {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Lebar terkecil sekarang: $sw dp", fontSize = 13.sp)
            Text("Kerapatan: ${dm.densityDpi} dpi", fontSize = 13.sp)
            Text("Resolusi: ${dm.widthPixels} x ${dm.heightPixels} px", fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Text("Perintah ADB untuk lebar terkecil tertentu:", fontSize = 13.sp)
            listOf(600, 720, 823).forEach { target ->
                Text("$target dp: adb shell wm density ${sisiPendek * 160 / target}", fontSize = 12.sp)
            }
            Text("Pulihkan: adb shell wm density reset", fontSize = 12.sp)
        }
    }
}

@Composable
private fun HalamanJendela(p: Pengaturan) {
    Text("Jendela", style = MaterialTheme.typography.titleMedium)
    Baris("Selalu buka aplikasi dalam jendela", p.selaluJendela) { p.aturJendela(it) }
    Text(
        "Aplikasi Android lain hanya tampil sebagai jendela bila ROM mendukung freeform. " +
            "Berkas, Peramban, Catatan, dan Gambar selalu berjalan di jendela milik launcher.",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
        "Tips: seret bilah judul ke tepi kiri atau kanan untuk setengah layar, dan ke tepi atas untuk memaksimalkan.",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun Sistem() {
    val ctx = LocalContext.current
    val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val mi = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
    val sf = StatFs(Environment.getDataDirectory().path)
    Text("Sistem", style = MaterialTheme.typography.titleMedium)
    SelectionContainer {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Perangkat: ${Build.MANUFACTURER} ${Build.MODEL}", fontSize = 13.sp)
            Text("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})", fontSize = 13.sp)
            Text("Arsitektur: ${Build.SUPPORTED_ABIS.firstOrNull() ?: "-"}", fontSize = 13.sp)
            Text("RAM: ${Formatter.formatFileSize(ctx, mi.availMem)} tersedia dari ${Formatter.formatFileSize(ctx, mi.totalMem)}", fontSize = 13.sp)
            Text("Penyimpanan: ${Formatter.formatFileSize(ctx, sf.availableBytes)} kosong dari ${Formatter.formatFileSize(ctx, sf.totalBytes)}", fontSize = 13.sp)
        }
    }
}

@Composable
private fun Tentang() {
    val ctx = LocalContext.current
    val versi = try { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName } catch (e: Exception) { "?" }
    Text("PC Launcher", style = MaterialTheme.typography.titleMedium)
    Text("Versi $versi", fontSize = 13.sp)
    Text("Launcher bergaya desktop PC untuk Android. Lisensi MIT.", fontSize = 13.sp)
}

@Composable
private fun Baris(label: String, nilai: Boolean, ubah: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), fontSize = 14.sp)
        Switch(checked = nilai, onCheckedChange = ubah)
    }
}
