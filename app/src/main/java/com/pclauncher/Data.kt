package com.pclauncher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.unit.IntOffset
import androidx.core.graphics.drawable.toBitmap
import java.io.File

data class AppInfo(
    val nama: String,
    val pkg: String,
    val komponen: ComponentName,
    val ikon: ImageBitmap,
)

val WARNA_PILIHAN = listOf(Color(0xFF181822), Color(0xFF0D2B5E), Color(0xFF3A1466), Color(0xFF14421E))

data class Wallpaper(val nama: String, val warna: List<Color>)

val daftarWallpaper = listOf(
    Wallpaper("Langit Biru", listOf(Color(0xFF0D47A1), Color(0xFF42A5F5), Color(0xFF81C784))),
    Wallpaper("Senja", listOf(Color(0xFF311B92), Color(0xFFD81B60), Color(0xFFFFB74D))),
    Wallpaper("Malam", listOf(Color(0xFF000000), Color(0xFF0D1B2A), Color(0xFF1B3A57))),
    Wallpaper("Hutan", listOf(Color(0xFF004D40), Color(0xFF2E7D32), Color(0xFFC0CA33))),
)

/** Membaca daftar aplikasi yang bisa dibuka. Ikon disimpan di cache supaya muat ulang cepat. */
object Aplikasi {
    private val cacheIkon = HashMap<String, ImageBitmap>()

    fun muat(ctx: Context): List<AppInfo> {
        val pm = ctx.packageManager
        val q = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(q, 0).mapNotNull { ri ->
            val ai = ri.activityInfo ?: return@mapNotNull null
            if (ai.packageName == ctx.packageName) return@mapNotNull null
            val kunci = ai.packageName + "/" + ai.name
            val ikon = cacheIkon.getOrPut(kunci) { ri.loadIcon(pm).toBitmap(112, 112).asImageBitmap() }
            AppInfo(
                ri.loadLabel(pm).toString(),
                ai.packageName,
                ComponentName(ai.packageName, ai.name),
                ikon,
            )
        }.sortedBy { it.nama.lowercase() }
    }
}

/** Menyimpan aplikasi di taskbar, ikon desktop, dan pilihan wallpaper. */
class Pengaturan(context: Context) {
    private val sp = context.getSharedPreferences("pc_launcher", Context.MODE_PRIVATE)

    val taskbar = mutableStateListOf<String>().apply { addAll(baca("taskbar")) }
    val desktop = mutableStateListOf<String>().apply { addAll(baca("desktop")) }

    /** Posisi ikon desktop pada grid: kolom (x) dan baris (y). */
    val posisi = mutableStateMapOf<String, IntOffset>().apply {
        bacaPosisi().forEach { put(it.first, it.second) }
    }

    var wallpaper by mutableIntStateOf(sp.getInt("wallpaper", 0))
        private set

    var pakaiFoto by mutableStateOf(sp.getBoolean("foto", false))
        private set

    var selaluJendela by mutableStateOf(sp.getBoolean("jendela", false))
        private set

    var transparansi by mutableFloatStateOf(sp.getFloat("alpha", 0.85f))
        private set

    var warnaBar by mutableIntStateOf(sp.getInt("warna", 0))
        private set

    var taskbarTengah by mutableStateOf(sp.getBoolean("tengah", false))
        private set

    /** Nama aplikasi yang diganti pengguna (pkg -> nama). */
    val namaKustom = mutableStateMapOf<String, String>().apply {
        bacaNama().forEach { put(it.first, it.second) }
    }

    private fun bacaNama(): List<Pair<String, String>> =
        (sp.getString("nama", "") ?: "").split(",").mapNotNull { t ->
            val b = t.split(":")
            if (b.size == 2 && b[0].isNotBlank()) b[0] to Uri.decode(b[1]) else null
        }

    fun setNama(pkg: String, nama: String) {
        if (nama.isBlank()) {
            namaKustom.remove(pkg)
        } else {
            namaKustom[pkg] = nama.trim()
        }
        sp.edit().putString(
            "nama",
            namaKustom.entries.joinToString(",") { "${it.key}:${Uri.encode(it.value)}" },
        ).apply()
    }

    fun aturTransparansi(f: Float) {
        transparansi = f
        sp.edit().putFloat("alpha", f).apply()
    }

    fun aturWarnaBar(i: Int) {
        warnaBar = i
        sp.edit().putInt("warna", i).apply()
    }

    fun aturTengah(aktif: Boolean) {
        taskbarTengah = aktif
        sp.edit().putBoolean("tengah", aktif).apply()
    }

    private fun bacaPosisi(): List<Pair<String, IntOffset>> =
        (sp.getString("posisi", "") ?: "").split(",").mapNotNull { teks ->
            val b = teks.split(":")
            val x = b.getOrNull(1)?.toIntOrNull()
            val y = b.getOrNull(2)?.toIntOrNull()
            if (b.size == 3 && x != null && y != null) b[0] to IntOffset(x, y) else null
        }

    fun setPosisi(pkg: String, kolom: Int, baris: Int) {
        posisi[pkg] = IntOffset(kolom, baris)
        sp.edit().putString(
            "posisi",
            posisi.entries.joinToString(",") { "${it.key}:${it.value.x}:${it.value.y}" },
        ).apply()
    }

    fun aturFoto(pakai: Boolean) {
        pakaiFoto = pakai
        sp.edit().putBoolean("foto", pakai).apply()
    }

    fun aturJendela(aktif: Boolean) {
        selaluJendela = aktif
        sp.edit().putBoolean("jendela", aktif).apply()
    }

    private fun baca(kunci: String): List<String> =
        (sp.getString(kunci, "") ?: "").split(",").filter { it.isNotBlank() }

    private fun simpan() {
        sp.edit()
            .putString("taskbar", taskbar.joinToString(","))
            .putString("desktop", desktop.joinToString(","))
            .apply()
    }

    fun toggleTaskbar(pkg: String) { if (!taskbar.remove(pkg)) taskbar.add(pkg); simpan() }
    fun toggleDesktop(pkg: String) { if (!desktop.remove(pkg)) desktop.add(pkg); simpan() }

    fun pilihWallpaper(i: Int) {
        wallpaper = i
        pakaiFoto = false
        sp.edit().putInt("wallpaper", i).putBoolean("foto", false).apply()
    }

    /** Pada pemakaian pertama, sematkan beberapa aplikasi umum yang terpasang. */
    fun isiAwal(terpasang: Set<String>) {
        if (sp.getBoolean("awal", false) || terpasang.isEmpty()) return
        val kandidat = listOf(
            "com.android.chrome", "com.android.settings", "com.google.android.youtube",
            "com.google.android.apps.messaging", "com.google.android.apps.photos",
            "com.android.vending",
        )
        val ada = kandidat.filter { it in terpasang }
        taskbar.addAll(ada)
        desktop.addAll(ada.take(2))
        simpan()
        sp.edit().putBoolean("awal", true).apply()
    }
}

/** Wallpaper dari foto pilihan pengguna, disimpan di penyimpanan internal aplikasi. */
object Foto {
    private fun berkas(ctx: Context) = File(ctx.filesDir, "wallpaper.jpg")

    fun simpan(ctx: Context, uri: Uri): Boolean {
        return try {
            val dm = ctx.resources.displayMetrics
            val maks = maxOf(dm.widthPixels, dm.heightPixels)
            val batas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, batas) }
            var skala = 1
            while (batas.outWidth / (skala * 2) >= maks && batas.outHeight / (skala * 2) >= maks) skala *= 2
            val opsi = BitmapFactory.Options().apply { inSampleSize = skala }
            val bmp = ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opsi) }
            if (bmp == null) {
                false
            } else {
                berkas(ctx).outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 90, it) }
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    fun muat(ctx: Context): ImageBitmap? {
        val f = berkas(ctx)
        if (!f.exists()) return null
        return BitmapFactory.decodeFile(f.path)?.asImageBitmap()
    }
}
