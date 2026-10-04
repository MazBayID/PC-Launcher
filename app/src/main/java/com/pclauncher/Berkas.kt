package com.pclauncher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.text.format.Formatter
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import java.io.File

private val EKS_GAMBAR = setOf("jpg", "jpeg", "png", "webp", "gif", "bmp")
private val EKS_TEKS = setOf("txt", "md", "log", "json", "xml", "csv", "kt", "java", "py", "html", "js", "css", "ini", "conf", "sh", "yml", "yaml")
private val NAMA_URUT = listOf("Nama", "Terbaru", "Ukuran")

private fun punyaAkses(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Environment.isExternalStorageManager() else true

@Composable
fun AplikasiBerkas(m: ManajerJendela) {
    val ctx = LocalContext.current
    var izin by remember { mutableStateOf(punyaAkses()) }
    if (izin) {
        DaftarBerkas(m)
    } else {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Berkas butuh izin \"Akses semua file\" untuk menampilkan penyimpananmu.",
                textAlign = TextAlign.Center,
            )
            Button(onClick = {
                try {
                    ctx.startActivity(
                        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:${ctx.packageName}"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                } catch (e: Exception) {
                    Peluncur.pengaturan(ctx, Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                }
            }) { Text("Buka pengaturan izin") }
            OutlinedButton(onClick = { izin = punyaAkses() }) { Text("Saya sudah mengizinkan") }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DaftarBerkas(m: ManajerJendela) {
    val ctx = LocalContext.current
    val akar = remember { Environment.getExternalStorageDirectory() }
    var dir by remember { mutableStateOf(akar) }
    var versi by remember { mutableIntStateOf(0) }
    var urut by remember { mutableIntStateOf(0) }
    var hapus by remember { mutableStateOf<File?>(null) }
    var ganti by remember { mutableStateOf<File?>(null) }
    var baru by remember { mutableStateOf(false) }
    val isi = remember(dir, versi, urut) {
        val pembanding: Comparator<File> = when (urut) {
            1 -> compareByDescending<File> { it.lastModified() }
            2 -> compareByDescending<File> { it.length() }
            else -> compareBy<File> { it.name.lowercase() }
        }
        (dir.listFiles() ?: emptyArray<File>()).toList().sortedWith(compareBy<File> { !it.isDirectory }.then(pembanding))
    }
    val pintasan = remember {
        listOf(
            "Penyimpanan" to akar,
            "Unduhan" to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Dokumen" to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "Gambar" to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "Kamera" to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "Musik" to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
        )
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 4.dp)) {
            pintasan.forEach { (nama, f) ->
                TextButton(onClick = { if (f.exists()) dir = f }) { Text(nama, fontSize = 12.sp) }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = { dir.parentFile?.let { dir = it } },
                enabled = dir.path != akar.path,
            ) { Text("↑ Naik") }
            Text(
                dir.path.removePrefix(akar.path).ifEmpty { "/" },
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
            )
            TextButton(onClick = { urut = (urut + 1) % 3 }) { Text("Urut: ${NAMA_URUT[urut]}", fontSize = 12.sp) }
            TextButton(onClick = { baru = true }) { Text("+ Folder", fontSize = 12.sp) }
        }
        HorizontalDivider()
        LazyColumn(Modifier.weight(1f)) {
            items(isi, key = { it.path }) { f ->
                var menu by remember { mutableStateOf(false) }
                Box {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { if (f.isDirectory) dir = f else bukaBerkasDi(m, ctx, f) },
                                onLongClick = { menu = true },
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            when {
                                f.isDirectory -> "📁"
                                f.extension.lowercase() in EKS_GAMBAR -> "🖼️"
                                f.extension.lowercase() in EKS_TEKS -> "📝"
                                else -> "📄"
                            },
                            fontSize = 22.sp,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(f.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                if (f.isDirectory) "${f.list()?.size ?: 0} item"
                                else Formatter.formatShortFileSize(ctx, f.length()),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("Ganti nama") }, onClick = { menu = false; ganti = f })
                        DropdownMenuItem(text = { Text("Hapus") }, onClick = { menu = false; hapus = f })
                    }
                }
            }
        }
    }

    hapus?.let { f ->
        AlertDialog(
            onDismissRequest = { hapus = null },
            title = { Text("Hapus?") },
            text = { Text(f.name) },
            confirmButton = {
                TextButton(onClick = {
                    if (f.isDirectory) f.deleteRecursively() else f.delete()
                    hapus = null
                    versi++
                }) { Text("Hapus") }
            },
            dismissButton = { TextButton(onClick = { hapus = null }) { Text("Batal") } },
        )
    }
    ganti?.let { f ->
        var nama by remember(f) { mutableStateOf(f.name) }
        AlertDialog(
            onDismissRequest = { ganti = null },
            title = { Text("Ganti nama") },
            text = { OutlinedTextField(value = nama, onValueChange = { nama = it }, singleLine = true) },
            confirmButton = {
                TextButton(onClick = {
                    val tujuan = File(f.parentFile, nama.trim())
                    if (nama.isBlank() || tujuan.exists() || !f.renameTo(tujuan)) {
                        Toast.makeText(ctx, "Tidak bisa mengganti nama", Toast.LENGTH_SHORT).show()
                    }
                    ganti = null
                    versi++
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { ganti = null }) { Text("Batal") } },
        )
    }
    if (baru) {
        var nama by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { baru = false },
            title = { Text("Folder baru") },
            text = { OutlinedTextField(value = nama, onValueChange = { nama = it }, singleLine = true) },
            confirmButton = {
                TextButton(onClick = {
                    if (nama.isNotBlank()) File(dir, nama.trim()).mkdirs()
                    baru = false
                    versi++
                }) { Text("Buat") }
            },
            dismissButton = { TextButton(onClick = { baru = false }) { Text("Batal") } },
        )
    }
}

/** Gambar dan teks dibuka di jendela bawaan, selebihnya lewat aplikasi lain. */
private fun bukaBerkasDi(m: ManajerJendela, ctx: Context, f: File) {
    val e = f.extension.lowercase()
    when {
        e in EKS_GAMBAR -> m.buka(Jenis.GAMBAR, f.path, f.name)
        e in EKS_TEKS -> m.buka(Jenis.CATATAN, f.path, f.name)
        else -> bukaBerkas(ctx, f)
    }
}

private fun bukaBerkas(ctx: Context, f: File) {
    try {
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", f)
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(f.extension.lowercase()) ?: "*/*"
        ctx.startActivity(
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, mime)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (e: Exception) {
        Toast.makeText(ctx, "Tidak ada aplikasi untuk membuka berkas ini", Toast.LENGTH_SHORT).show()
    }
}
