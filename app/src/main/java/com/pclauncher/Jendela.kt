package com.pclauncher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

enum class Jenis(val ikon: String, val judul: String) {
    BERKAS("📁", "Berkas"),
    PERAMBAN("🌐", "Peramban"),
    PENGATURAN("⚙️", "Pengaturan PC"),
}

/** Satu jendela melayang. Ukuran dan posisi dalam dp. */
class Jendela(
    val id: Int,
    val jenis: Jenis,
    val judul: String,
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    val urlAwal: String = "",
) {
    var x by mutableFloatStateOf(x)
    var y by mutableFloatStateOf(y)
    var w by mutableFloatStateOf(w)
    var h by mutableFloatStateOf(h)
    var z by mutableIntStateOf(0)
    var diperkecil by mutableStateOf(false)
    var maksimal by mutableStateOf(false)
}

class ManajerJendela {
    val daftar = mutableStateListOf<Jendela>()
    var areaW = 360f
    var areaH = 600f
    private var nomor = 1
    private var zMax = 0

    fun aktifId(): Int = daftar.filter { !it.diperkecil }.maxByOrNull { it.z }?.id ?: -1

    fun buka(jenis: Jenis, url: String = "") {
        val ada = if (jenis == Jenis.PERAMBAN) null else daftar.firstOrNull { it.jenis == jenis }
        if (ada != null) {
            fokus(ada)
            return
        }
        val lebar = if (areaW >= 600f) 560f else areaW - 16f
        val tinggi = if (areaW >= 600f) 420f else areaH * 0.7f
        val geser = (daftar.size % 5) * 24f
        val j = Jendela(nomor++, jenis, jenis.judul, (areaW - lebar) / 2f + geser, 12f + geser, lebar, tinggi, url)
        daftar.add(j)
        fokus(j)
    }

    fun fokus(j: Jendela) {
        if (!j.diperkecil && j.z == zMax) return
        zMax++
        j.z = zMax
        j.diperkecil = false
    }

    fun tutup(j: Jendela) {
        daftar.remove(j)
    }

    fun ketukTaskbar(j: Jendela) {
        if (j.diperkecil) fokus(j)
        else if (j.id == aktifId()) j.diperkecil = true
        else fokus(j)
    }
}

/** Lapisan tempat semua jendela digambar, di atas desktop dan di bawah taskbar. */
@Composable
fun LapisanJendela(m: ManajerJendela, p: Pengaturan) {
    BoxWithConstraints(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(bottom = 56.dp)
    ) {
        val aw = maxWidth.value
        val ah = maxHeight.value
        LaunchedEffect(aw, ah) { m.areaW = aw; m.areaH = ah }
        val aktifId = m.aktifId()
        m.daftar.forEach { j -> key(j.id) { KomponenJendela(j, j.id == aktifId, aw, ah, m, p) } }
    }
}

@Composable
private fun KomponenJendela(j: Jendela, aktif: Boolean, aw: Float, ah: Float, m: ManajerJendela, p: Pengaturan) {
    val dens = LocalDensity.current.density
    val maks = j.maksimal
    val lebar = if (maks) aw else j.w.coerceIn(240f, maxOf(240f, aw))
    val tinggi = if (maks) ah else j.h.coerceIn(180f, maxOf(180f, ah))
    val px = if (maks) 0f else j.x.coerceIn(0f, maxOf(0f, aw - lebar))
    val py = if (maks) 0f else j.y.coerceIn(0f, maxOf(0f, ah - tinggi))
    // Jendela yang diperkecil tetap hidup (ukuran 0) agar isinya, misalnya halaman web, tidak hilang.
    val ukuran = if (j.diperkecil) Modifier.size(0.dp) else Modifier.size(lebar.dp, tinggi.dp)

    Surface(
        modifier = Modifier
            .offset { IntOffset((px * dens).roundToInt(), (py * dens).roundToInt()) }
            .then(ukuran)
            .zIndex(j.z.toFloat())
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val e = awaitPointerEvent(PointerEventPass.Initial)
                        if (e.type == PointerEventType.Press) m.fokus(j)
                    }
                }
            },
        shape = RoundedCornerShape(if (maks) 0.dp else 10.dp),
        shadowElevation = 12.dp,
        color = MaterialTheme.colorScheme.surface,
        border = if (aktif) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(
                        if (aktif) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .pointerInput(maks, aw, lebar) {
                        if (!maks) {
                            detectDragGestures { change, d ->
                                change.consume()
                                j.x = (j.x + d.x / dens).coerceIn(0f, maxOf(0f, aw - lebar))
                                j.y = (j.y + d.y / dens).coerceAtLeast(0f)
                            }
                        }
                    }
                    .pointerInput(Unit) { detectTapGestures(onDoubleTap = { j.maksimal = !j.maksimal }) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${j.jenis.ikon}  ${j.judul}",
                    modifier = Modifier.weight(1f).padding(start = 10.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp,
                )
                TombolJ("—") { j.diperkecil = true }
                TombolJ(if (maks) "❐" else "☐") { j.maksimal = !j.maksimal }
                TombolJ("✕") { m.tutup(j) }
            }
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (j.jenis) {
                    Jenis.BERKAS -> AplikasiBerkas()
                    Jenis.PERAMBAN -> AplikasiPeramban(j.urlAwal)
                    Jenis.PENGATURAN -> AplikasiPengaturan(p)
                }
                if (!maks) {
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .pointerInput(aw, ah) {
                                detectDragGestures { change, d ->
                                    change.consume()
                                    j.w = (j.w + d.x / dens).coerceIn(240f, maxOf(240f, aw))
                                    j.h = (j.h + d.y / dens).coerceIn(180f, maxOf(180f, ah))
                                }
                            },
                        contentAlignment = Alignment.BottomEnd,
                    ) {
                        Text("◢", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun TombolJ(teks: String, aksi: () -> Unit) {
    Box(
        Modifier.width(40.dp).fillMaxHeight().clickable(onClick = aksi),
        contentAlignment = Alignment.Center,
    ) { Text(teks, fontSize = 14.sp) }
}
