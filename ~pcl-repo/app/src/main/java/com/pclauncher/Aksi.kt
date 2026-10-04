package com.pclauncher

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val ID = Locale("id", "ID")

private fun statusJaringan(ctx: Context): String {
    return try {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val c = cm.getNetworkCapabilities(cm.activeNetwork)
        when {
            c == null -> "Tidak terhubung"
            c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi terhubung"
            c.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Data seluler"
            c.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Terhubung"
        }
    } catch (e: Exception) {
        "Status jaringan tidak tersedia"
    }
}

private fun kecerahan(ctx: Context): Float =
    try { Settings.System.getInt(ctx.contentResolver, Settings.System.SCREEN_BRIGHTNESS).toFloat() } catch (e: Exception) { 128f }

/** Panel cepat: volume, kecerahan, dan pintasan jaringan, Bluetooth, serta layar. */
@Composable
fun PanelAksi(p: Pengaturan, onPengaturan: () -> Unit) {
    val ctx = LocalContext.current
    val audio = remember { ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maks = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
    var volume by remember { mutableFloatStateOf(audio.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()) }
    var terang by remember { mutableFloatStateOf(kecerahan(ctx)) }
    val bolehUbah = Settings.System.canWrite(ctx)
    val jaringan = remember { statusJaringan(ctx) }

    Surface(
        color = warnaPanelDari(p),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.widthIn(max = 380.dp).fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Ubin("📶", "Internet", Modifier.weight(1f)) {
                    Peluncur.pengaturan(ctx, if (Build.VERSION.SDK_INT >= 29) Settings.Panel.ACTION_INTERNET_CONNECTIVITY else Settings.ACTION_WIRELESS_SETTINGS)
                }
                Ubin("🔵", "Bluetooth", Modifier.weight(1f)) { Peluncur.pengaturan(ctx, Settings.ACTION_BLUETOOTH_SETTINGS) }
                Ubin("☀️", "Layar", Modifier.weight(1f)) { Peluncur.pengaturan(ctx, Settings.ACTION_DISPLAY_SETTINGS) }
                Ubin("⚙️", "Pengaturan", Modifier.weight(1f)) { onPengaturan() }
            }
            Text(jaringan, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔊", fontSize = 18.sp)
                Slider(
                    value = volume,
                    onValueChange = { volume = it; audio.setStreamVolume(AudioManager.STREAM_MUSIC, it.roundToInt(), 0) },
                    valueRange = 0f..maxOf(1f, maks),
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                )
            }
            if (bolehUbah) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("☀️", fontSize = 18.sp)
                    Slider(
                        value = terang,
                        onValueChange = {
                            terang = it
                            try {
                                Settings.System.putInt(ctx.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                                Settings.System.putInt(ctx.contentResolver, Settings.System.SCREEN_BRIGHTNESS, it.roundToInt())
                            } catch (e: Exception) { }
                        },
                        valueRange = 5f..255f,
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                    )
                }
            } else {
                TextButton(onClick = {
                    Peluncur.pengaturan(ctx, Settings.ACTION_MANAGE_WRITE_SETTINGS)
                }) { Text("Izinkan launcher mengubah kecerahan", color = Color(0xFF4FC3F7), fontSize = 12.sp) }
            }
        }
    }
}

@Composable
private fun Ubin(ikon: String, label: String, modifier: Modifier, aksi: () -> Unit) {
    Surface(onClick = aksi, color = Color(0x22FFFFFF), shape = RoundedCornerShape(10.dp), modifier = modifier) {
        Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(ikon, fontSize = 22.sp)
            Text(label, color = Color.White, fontSize = 11.sp, maxLines = 1)
        }
    }
}

/** Kalender bulanan yang muncul saat jam di taskbar diketuk. */
@Composable
fun PanelKalender(p: Pengaturan) {
    var bulan by remember { mutableStateOf(YearMonth.now()) }
    val hariIni = LocalDate.now()
    val awal = bulan.atDay(1).dayOfWeek.value
    val total = bulan.lengthOfMonth()
    val baris = (awal - 1 + total + 6) / 7

    Surface(
        color = warnaPanelDari(p),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.widthIn(max = 380.dp).fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", ID)),
                color = Color.White,
                fontSize = 15.sp,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { bulan = bulan.minusMonths(1) }) { Text("◀") }
                Text(
                    bulan.format(DateTimeFormatter.ofPattern("MMMM yyyy", ID)),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { bulan = bulan.plusMonths(1) }) { Text("▶") }
            }
            Row {
                listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min").forEach {
                    Text(it, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                }
            }
            for (r in 0 until baris) {
                Row {
                    for (c in 0 until 7) {
                        val hari = r * 7 + c - (awal - 1) + 1
                        val ini = bulan == YearMonth.from(hariIni) && hari == hariIni.dayOfMonth
                        Box(
                            Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(if (ini) Color(0xFF1E88E5) else Color.Transparent),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (hari in 1..total) Text("$hari", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
