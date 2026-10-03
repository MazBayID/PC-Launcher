@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.pclauncher

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val WARNA_BAR = Color(0xD9181822)
private val WARNA_PANEL = Color(0xF2202030)
private val JAM = DateTimeFormatter.ofPattern("HH:mm")
private val TANGGAL = DateTimeFormatter.ofPattern("d/M/yyyy")

/** Ukuran satu sel grid desktop. */
private val SEL_L = 96.dp
private val SEL_T = 108.dp

@Composable
private fun layarLebar(): Boolean = LocalConfiguration.current.smallestScreenWidthDp >= 600

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

/** Menentukan sel grid tiap ikon. Posisi tersimpan dipakai bila valid, sisanya mengisi sel kosong. */
private fun hitungTempat(
    apps: List<AppInfo>,
    simpanan: Map<String, IntOffset>,
    kolom: Int,
    baris: Int,
): Map<String, IntOffset> {
    val hasil = LinkedHashMap<String, IntOffset>()
    val dipakai = HashSet<IntOffset>()
    apps.forEach { a ->
        val s = simpanan[a.pkg]
        if (s != null && s.x in 0 until kolom && s.y in 0 until baris && dipakai.add(s)) hasil[a.pkg] = s
    }
    var k = 0
    var b = 0
    apps.forEach { a ->
        if (a.pkg !in hasil) {
            while (IntOffset(k, b) in dipakai) {
                b++
                if (b >= baris) { b = 0; k++ }
            }
            val o = IntOffset(k, b)
            dipakai.add(o)
            hasil[a.pkg] = o
        }
    }
    return hasil
}

@Composable
fun Desktop(p: Pengaturan, versi: Int, sinyalHome: Int) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var start by remember { mutableStateOf(false) }
    var menuPos by remember { mutableStateOf<Offset?>(null) }
    var dialogWall by remember { mutableStateOf(false) }
    var dialogInfo by remember { mutableStateOf(false) }
    var foto by remember { mutableStateOf<ImageBitmap?>(null) }
    var fotoVersi by remember { mutableIntStateOf(0) }

    val pilihFoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) { Foto.simpan(ctx, uri) }
                if (ok) {
                    p.aturFoto(true)
                    fotoVersi++
                } else {
                    Toast.makeText(ctx, "Foto tidak bisa dipakai", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(versi) {
        apps = withContext(Dispatchers.IO) { Aplikasi.muat(ctx) }
        p.isiAwal(apps.map { it.pkg }.toSet())
    }
    LaunchedEffect(fotoVersi, p.pakaiFoto) {
        foto = if (p.pakaiFoto) withContext(Dispatchers.IO) { Foto.muat(ctx) } else null
    }
    LaunchedEffect(versi, sinyalHome) { start = false; menuPos = null }
    BackHandler { start = false; menuPos = null }

    val peta = remember(apps) { apps.associateBy { it.pkg } }
    val buka: (AppInfo, Boolean) -> Unit = { a, jendela ->
        start = false
        menuPos = null
        Peluncur.buka(ctx, a, jendela || p.selaluJendela)
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
        val gambar = foto
        if (p.pakaiFoto && gambar != null) {
            Image(gambar, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }

        BoxWithConstraints(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(bottom = 56.dp)
        ) {
            val kolom = maxOf(1, (maxWidth / SEL_L).toInt())
            val baris = maxOf(1, (maxHeight / SEL_T).toInt())
            val tempat = remember(ikonDesktop, p.posisi.toMap(), kolom, baris) {
                hitungTempat(ikonDesktop, p.posisi.toMap(), kolom, baris)
            }
            LaunchedEffect(tempat) {
                tempat.forEach { (pkg, o) -> if (pkg !in p.posisi) p.setPosisi(pkg, o.x, o.y) }
            }
            val pindah: (String, Int, Int) -> Unit = { pkg, c, r ->
                val asal = tempat[pkg]
                val tujuan = IntOffset(c.coerceIn(0, kolom - 1), r.coerceIn(0, baris - 1))
                val lain = tempat.entries.firstOrNull { it.key != pkg && it.value == tujuan }?.key
                p.setPosisi(pkg, tujuan.x, tujuan.y)
                if (lain != null && asal != null) p.setPosisi(lain, asal.x, asal.y)
            }
            ikonDesktop.forEach { a ->
                key(a.pkg) {
                    IkonDesktop(a, p, buka, tempat[a.pkg] ?: IntOffset(0, 0)) { c, r -> pindah(a.pkg, c, r) }
                }
            }
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
                    ItemMenu("Wallpaper dari foto…") { menuPos = null; pilihFoto.launch("image/*") }
                    ItemMenu("Info layar dan DPI") { menuPos = null; dialogInfo = true }
                    ItemMenu(if (p.selaluJendela) "Mode jendela: aktif ✓" else "Selalu buka dalam jendela") {
                        menuPos = null
                        p.aturJendela(!p.selaluJendela)
                    }
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
                        TextButton(onClick = { p.pilihWallpaper(i); dialogWall = false }) {
                            Text(w.nama + if (i == p.wallpaper && !p.pakaiFoto) "  ✓" else "")
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { dialogWall = false }) { Text("Tutup") } },
        )
    }
    if (dialogInfo) InfoLayar { dialogInfo = false }
}

@Composable
private fun InfoLayar(tutup: () -> Unit) {
    val res = LocalContext.current.resources
    val dm = res.displayMetrics
    val sw = res.configuration.smallestScreenWidthDp
    val sisiPendek = minOf(dm.widthPixels, dm.heightPixels)
    AlertDialog(
        onDismissRequest = tutup,
        title = { Text("Info layar dan DPI") },
        text = {
            SelectionContainer {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Lebar terkecil sekarang: $sw dp")
                    Text("Kerapatan: ${dm.densityDpi} dpi")
                    Text("Resolusi: ${dm.widthPixels} x ${dm.heightPixels} px")
                    Spacer(Modifier.height(8.dp))
                    Text("Perintah untuk mencapai lebar terkecil tertentu (jalankan lewat ADB):")
                    listOf(600, 720, 823).forEach { target ->
                        val dpi = sisiPendek * 160 / target
                        Text("$target dp: adb shell wm density $dpi")
                    }
                    Text("Pulihkan: adb shell wm density reset")
                }
            }
        },
        confirmButton = { TextButton(onClick = tutup) { Text("Tutup") } },
    )
}

@Composable
private fun ItemMenu(teks: String, aksi: () -> Unit) {
    DropdownMenuItem(text = { Text(teks) }, onClick = aksi)
}

/** Ikon desktop yang bisa diseret. Dilepas di sel terdekat, bertukar tempat bila sel sudah terisi. */
@Composable
private fun IkonDesktop(
    app: AppInfo,
    p: Pengaturan,
    buka: (AppInfo, Boolean) -> Unit,
    sel: IntOffset,
    onPindah: (Int, Int) -> Unit,
) {
    val d = LocalDensity.current
    val lPx = with(d) { SEL_L.toPx() }
    val tPx = with(d) { SEL_T.toPx() }
    val dasarX = sel.x * lPx
    val dasarY = sel.y * tPx
    var geser by remember { mutableStateOf(Offset.Zero) }
    var seret by remember { mutableStateOf(false) }
    Box(
        Modifier
            .offset { IntOffset((dasarX + geser.x).roundToInt(), (dasarY + geser.y).roundToInt()) }
            .zIndex(if (seret) 1f else 0f)
            .pointerInput(sel) {
                detectDragGestures(
                    onDragStart = { seret = true },
                    onDragEnd = {
                        val c = ((dasarX + geser.x) / lPx).roundToInt()
                        val r = ((dasarY + geser.y) / tPx).roundToInt()
                        seret = false
                        geser = Offset.Zero
                        onPindah(c, r)
                    },
                    onDragCancel = { seret = false; geser = Offset.Zero },
                    onDrag = { change, delta ->
                        change.consume()
                        geser = geser + delta
                    },
                )
            }
    ) {
        IkonApp(app, p, buka, 48.dp, SEL_L)
    }
}

/** Ikon aplikasi dengan menu klik kanan / tekan lama. Dipakai di desktop dan Start menu. */
@Composable
fun IkonApp(app: AppInfo, p: Pengaturan, buka: (AppInfo, Boolean) -> Unit, ukuran: Dp = 48.dp, lebar: Dp = 88.dp) {
    var menu by remember { mutableStateOf(false) }
    Box {
        Column(
            Modifier
                .padding(4.dp)
                .clip(RoundedCornerShape(10.dp))
                .klikKanan { menu = true }
                .combinedClickable(onClick = { buka(app, false) }, onLongClick = { menu = true })
                .padding(8.dp)
                .width(lebar - 24.dp),
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
    val lebar = layarLebar()
    var cari by remember { mutableStateOf("") }
    val hasil = remember(apps, cari) {
        if (cari.isBlank()) apps else apps.filter { it.nama.contains(cari, ignoreCase = true) }
    }
    Box(
        Modifier.fillMaxWidth().padding(8.dp),
        contentAlignment = if (lebar) Alignment.Center else Alignment.CenterStart,
    ) {
        Surface(
            color = WARNA_PANEL,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.widthIn(max = if (lebar) 640.dp else 420.dp).fillMaxWidth(),
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
                    columns = GridCells.Adaptive(88.dp),
                    modifier = Modifier.heightIn(max = if (lebar) 520.dp else 440.dp),
                ) {
                    items(hasil, key = { it.pkg }) { a -> IkonApp(a, p, buka, 44.dp, 88.dp) }
                }
            }
        }
    }
}

@Composable
private fun TombolStart(aktif: Boolean, onStart: () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (aktif) Color(0x33FFFFFF) else Color.Transparent)
            .clickable { onStart() },
        contentAlignment = Alignment.Center,
    ) { LogoStart() }
}

@Composable
fun Taskbar(
    apps: List<AppInfo>,
    p: Pengaturan,
    startBuka: Boolean,
    onStart: () -> Unit,
    buka: (AppInfo, Boolean) -> Unit,
) {
    val lebar = layarLebar()
    Surface(color = WARNA_BAR, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.navigationBarsPadding().height(56.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (lebar) {
                // Layar lebar: tombol Start dan ikon di tengah, seperti Windows 11.
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        TombolStart(startBuka, onStart)
                        apps.forEach { a -> key(a.pkg) { IkonTaskbar(a, p, buka) } }
                    }
                }
            } else {
                TombolStart(startBuka, onStart)
                LazyRow(
                    Modifier.weight(1f).padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items(apps, key = { it.pkg }) { a -> IkonTaskbar(a, p, buka) }
                }
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
