package com.pclauncher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

data class AppInfo(
    val nama: String,
    val pkg: String,
    val komponen: ComponentName,
    val ikon: ImageBitmap,
)

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

    var wallpaper by mutableIntStateOf(sp.getInt("wallpaper", 0))
        private set

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
        sp.edit().putInt("wallpaper", i).apply()
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
