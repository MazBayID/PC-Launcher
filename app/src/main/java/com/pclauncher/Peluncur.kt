package com.pclauncher

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

object Peluncur {

    /** Buka aplikasi. Jika [jendela] true, minta ukuran jendela 70% x 60% layar (butuh freeform windows aktif). */
    fun buka(ctx: Context, app: AppInfo, jendela: Boolean = false) {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(app.komponen)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        try {
            if (jendela) {
                val dm = ctx.resources.displayMetrics
                val w = (dm.widthPixels * 0.7f).toInt()
                val h = (dm.heightPixels * 0.6f).toInt()
                val kiri = (dm.widthPixels - w) / 2
                val atas = (dm.heightPixels - h) / 2
                val opsi = ActivityOptions.makeBasic().setLaunchBounds(Rect(kiri, atas, kiri + w, atas + h))
                ctx.startActivity(intent, opsi.toBundle())
            } else {
                ctx.startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(ctx, "Tidak bisa membuka ${app.nama}", Toast.LENGTH_SHORT).show()
        }
    }

    fun info(ctx: Context, app: AppInfo) = lewat(ctx) {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.pkg}"))
    }

    fun copot(ctx: Context, app: AppInfo) = lewat(ctx) {
        Intent(Intent.ACTION_DELETE, Uri.parse("package:${app.pkg}"))
    }

    fun pengaturan(ctx: Context, aksi: String) = lewat(ctx) { Intent(aksi) }

    private fun lewat(ctx: Context, buat: () -> Intent) {
        try {
            ctx.startActivity(buat().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            Toast.makeText(ctx, "Layar ini tidak tersedia di perangkatmu", Toast.LENGTH_SHORT).show()
        }
    }
}
