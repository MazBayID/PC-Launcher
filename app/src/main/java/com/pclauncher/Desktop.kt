@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.pclauncher

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val WARNA_BAR = Color(0xD9181822)
private val WARNA_PANEL = Color(0xF2202030)
private val JAM = DateTimeFormatter.ofPattern("HH:mm")
private val TANGGAL = DateTimeFormatter.ofPattern("d/M/yyyy")

/** Mendeteksi klik kanan mouse (tombol sekunder). */
fun Modifier.klikKanan(aksi: (Offset) -> Unit): Modifier = composed {
    val terbaru by rememberUpdatedState(aksi)
    pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val e = awaitPointerEvent()
                if (e.type == PointerEventType.Press && e.buttons.isSecondaryPressed) {
                    terbaru(e.changes.first().position)
                }
            }
        }
    }
}

@Composable
fun Desktop(p: Pengaturan, versi: Int, sinyalHome: Int) {
    val ctx = LocalContext.current
    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var start by remember { mutableStateOf(false) }
    var menuPos by remember { mutableStateOf<Offset?>(null) }
    var dialogWall by remember { mutableStateOf(false) }

    LaunchedEffect(versi) {
        apps = withContext(Dispatchers.IO) { Aplikasi.muat(ctx) }
        p.isiAwal(apps.map { it.pkg }.toSet())
    }
    LaunchedEffect(versi, sinyalHome) { start = false; menuPos = null }
    BackHandler { start = false; menuPos = null }

    val peta = remember(apps) { apps.associateBy { it.pkg } }
    val buka: (AppInfo, Boolean) -> Unit = { a, jendela ->
        start = false
        menuPos = null
        Peluncur.buka(ctx, a, jendela)
    }
    val wall = daftarWallpaper.getOrElse(p.wallpaper) { daftarWallpaper[0] }
    val ikonDesktop = p.desktop.mapNotNull { peta[it] }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(wall.warna))
            .klikKanan { menuPos = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { start = false; menuPos = null },
                    onLongPress = { menuPos = it },
                )
            }
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(88.dp),
            contentPadding = PaddingValues(12.dp),
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(bottom = 72.dp),
        ) {
            items(ikonDesktop, key = { it.pkg }) { a -> IkonApp(a, p, buka) }
        }

        if (apps.isNotEmpty() && ikonDesktop.isEmpty()) {
            Text(
                "Desktop masih kosong.\nBuka menu Start, tekan lama sebuah aplikasi,\nlalu pilih \"Taruh di desktop\".",
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center).padding(32.dp),
            )
        }

        val pos = menuPos
        if (pos != null) {
            Box(Modifier.offset { IntOffset(pos.x.roundToInt(), pos.y.roundToInt()) }) {
                DropdownMenu(expanded = true, onDismissRequest = { menuPos = null }) {
                    ItemMenu("Ganti wallpaper") { menuPos = null; dialogWall = true }
                    ItemMenu("Pengaturan layar") { menuPos = null; Peluncur.pengaturan(ctx, Settings.ACTION_DISPLAY_SETTINGS) }
                    ItemMenu("Pengaturan sistem") { menuPos = null; Peluncur.pengaturan(ctx, Settings.ACTION_SETTINGS) }
                    ItemMenu("Opsi pengembang (jendela bebas)") { menuPos = null; Peluncur.pengaturan(ctx, Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS) }
                    ItemMenu("Atur launcher default") { menuPos = null; Peluncur.pengaturan(ctx, Settings.ACTION_HOME_SETTINGS) }
                }
            }
        }

        Column(Modifier.align(Alignment.BottomCenter).imePadding()) {
            AnimatedVisibility(start) { StartMenu(apps, p, buka) }
            Taskbar(p.taskbar.mapNotNull { peta[it] }, p, start, { start = !start }, buka)
        }
    }

    if (dialogWall) {
        AlertDialog(
            onDismissRequest = { dialogWall = false },
            title = { Text("Wallpaper") },
            text = {
                Column {
                    daftarWallpaper.forEachIndexed { i, w ->
                        TextButton(onClick = { p.setWallpaper(i); dialogWall = false }) {
                            Text(w.nama + if (i == p.wallpaper) "  ✓" else "")
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { dialogWall = false }) { Text("Tutup") } },
        )
    }
}

@Composable
private fun ItemMenu(teks: String, aksi: () -> Unit) {
    DropdownMenuItem(text = { Text(teks) }, onClick = aksi)
}

/** Ikon aplikasi dengan menu klik kanan / tekan lama. Dipakai di desktop dan Start menu. */
@Composable
fun IkonApp(app: AppInfo, p: Pengaturan, buka: (AppInfo, Boolean) -> Unit, ukuran: Dp = 48.dp) {
    var menu by remember { mutableStateOf(false) }
    Box {
        Column(
            Modifier
                .padding(4.dp)
                .clip(RoundedCornerShape(10.dp))
                .klikKanan { menu = true }
                .combinedClickable(onClick = { buka(app, false) }, onLongClick = { menu = true })
                .padding(8.dp)
                .width(72.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(app.ikon, app.nama, Modifier.size(ukuran))
            Spacer(Modifier.height(4.dp))
            Text(
                app.nama,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    shadow = Shadow(Color.Black, Offset(0f, 1f), 4f),
                ),
            )
        }
        MenuAplikasi(app, p, menu, buka) { menu = false }
    }
}

@Composable
fun MenuAplikasi(app: AppInfo, p: Pengaturan, tampil: Boolean, buka: (AppInfo, Boolean) -> Unit, tutup: () -> Unit) {
    val ctx = LocalContext.current
    DropdownMenu(expanded = tampil, onDismissRequest = tutup) {
        ItemMenu("Buka") { tutup(); buka(app, false) }
        ItemMenu("Buka dalam jendela") { tutup(); buka(app, true) }
        ItemMenu(if (app.pkg in p.taskbar) "Lepas dari taskbar" else "Sematkan ke taskbar") { tutup(); p.toggleTaskbar(app.pkg) }
        ItemMenu(if (app.pkg in p.desktop) "Hapus dari desktop" else "Taruh di desktop") { tutup(); p.toggleDesktop(app.pkg) }
        ItemMenu("Info aplikasi") { tutup(); Peluncur.info(ctx, app) }
        ItemMenu("Copot pemasangan") { tutup(); Peluncur.copot(ctx, app) }
    }
}

@Composable
fun StartMenu(apps: List<AppInfo>, p: Pengaturan, buka: (AppInfo, Boolean) -> Unit) {
    var cari by remember { mutableStateOf("") }
    val hasil = remember(apps, cari) {
        if (cari.isBlank()) apps else apps.filter { it.nama.contains(cari, ignoreCase = true) }
    }
    Box(Modifier.fillMaxWidth().padding(8.dp)) {
        Surface(
            color = WARNA_PANEL,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
        ) {
            Column(Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = cari,
                    onValueChange = { cari = it },
                    singleLine = true,
                    placeholder = { Text("Cari aplikasi…") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { hasil.firstOrNull()?.let { buka(it, false) } }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedBorderColor = Color(0xFF4FC3F7),
                        unfocusedBorderColor = Color(0x66FFFFFF),
                        focusedPlaceholderColor = Color(0xAAFFFFFF),
                        unfocusedPlaceholderColor = Color(0xAAFFFFFF),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Text("Semua aplikasi (${hasil.size})", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(80.dp),
                    modifier = Modifier.heightIn(max = 440.dp),
                ) {
                    items(hasil, key = { it.pkg }) { a -> IkonApp(a, p, buka, 44.dp) }
                }
            }
        }
    }
}

@Composable
fun Taskbar(
    apps: List<AppInfo>,
    p: Pengaturan,
    startBuka: Boolean,
    onStart: () -> Unit,
    buka: (AppInfo, Boolean) -> Unit,
) {
    Surface(color = WARNA_BAR, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.navigationBarsPadding().height(56.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (startBuka) Color(0x33FFFFFF) else Color.Transparent)
                    .clickable { onStart() },
                contentAlignment = Alignment.Center,
            ) { LogoStart() }
            LazyRow(
                Modifier.weight(1f).padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items(apps, key = { it.pkg }) { a -> IkonTaskbar(a, p, buka) }
            }
            Tray()
        }
    }
}

@Composable
private fun LogoStart() {
    Canvas(Modifier.size(22.dp)) {
        val s = size.minDimension
        val celah = s * 0.08f
        val kotak = (s - celah) / 2f
        for (i in 0..1) for (j in 0..1) {
            drawRect(
                Color(0xFF4FC3F7),
                topLeft = Offset(i * (kotak + celah), j * (kotak + celah)),
                size = Size(kotak, kotak),
            )
        }
    }
}

@Composable
private fun IkonTaskbar(app: AppInfo, p: Pengaturan, buka: (AppInfo, Boolean) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Box(
        Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .klikKanan { menu = true }
            .combinedClickable(onClick = { buka(app, false) }, onLongClick = { menu = true }),
        contentAlignment = Alignment.Center,
    ) {
        Image(app.ikon, app.nama, Modifier.size(30.dp))
        MenuAplikasi(app, p, menu, buka) { menu = false }
    }
}

@Composable
private fun Tray() {
    val ctx = LocalContext.current
    var waktu by remember { mutableStateOf(LocalDateTime.now()) }
    var bat by remember { mutableStateOf(baterai(ctx)) }
    LaunchedEffect(Unit) {
        while (true) {
            waktu = LocalDateTime.now()
            bat = baterai(ctx)
            delay(15_000)
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("${bat.first}%" + if (bat.second) " ⚡" else "", color = Color.White, fontSize = 12.sp)
        Column(horizontalAlignment = Alignment.End) {
            Text(waktu.format(JAM), color = Color.White, fontSize = 13.sp)
            Text(waktu.format(TANGGAL), color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
        }
    }
}

private fun baterai(ctx: Context): Pair<Int, Boolean> {
    val i = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = i?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val skala = i?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
    val status = i?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val mengisi = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    val persen = if (level >= 0 && skala > 0) level * 100 / skala else 0
    return persen to mengisi
}
