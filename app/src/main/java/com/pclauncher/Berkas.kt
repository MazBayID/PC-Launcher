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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

private fun punyaAkses(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Environment.isExternalStorageManager() else true

@Composable
fun AplikasiBerkas() {
    val ctx = LocalContext.current
    var izin by remember { mutableStateOf(punyaAkses()) }
    if (izin) {
        DaftarBerkas()
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
private fun DaftarBerkas() {
    val ctx = LocalContext.current
    val akar = remember { Environment.getExternalStorageDirectory() }
    var dir by remember { mutableStateOf(akar) }
    var versi by remember { mutableIntStateOf(0) }
    var hapus by remember { mutableStateOf<File?>(null) }
    var baru by remember { mutableStateOf(false) }
    val isi = remember(dir, versi) {
        (dir.listFiles() ?: emptyArray<File>())
            .sortedWith(compareBy<File>({ !it.isDirectory }, { it.name.lowercase() }))
    }

    Column(Modifier.fillMaxSize()) {
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
            TextButton(onClick = { baru = true }) { Text("+ Folder") }
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
                                onClick = { if (f.isDirectory) dir = f else bukaBerkas(ctx, f) },
                                onLongClick = { menu = true },
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(if (f.isDirectory) "📁" else "📄", fontSize = 22.sp)
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
