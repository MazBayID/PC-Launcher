package com.pclauncher

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

/** Editor teks sederhana. [arg] adalah path berkas; kosong berarti catatan bawaan. */
@Composable
fun AplikasiCatatan(arg: String) {
    val ctx = LocalContext.current
    val berkas = remember { if (arg.isNotBlank()) File(arg) else File(ctx.filesDir, "catatan.txt") }
    val terlaluBesar = remember { berkas.exists() && berkas.length() > 1_000_000 }
    var teks by remember {
        mutableStateOf(
            try { if (berkas.exists() && !terlaluBesar) berkas.readText() else "" } catch (e: Exception) { "" }
        )
    }
    var tersimpan by remember { mutableStateOf(true) }

    if (terlaluBesar) {
        Text("Berkas terlalu besar untuk Catatan (lebih dari 1 MB).", Modifier.padding(16.dp))
        return
    }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = {
                try {
                    berkas.writeText(teks)
                    tersimpan = true
                    Toast.makeText(ctx, "Tersimpan", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(ctx, "Gagal menyimpan", Toast.LENGTH_SHORT).show()
                }
            }) { Text("💾 Simpan") }
            Text(
                berkas.name + if (tersimpan) "" else "  •",
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
            )
            Text("${teks.length} karakter  ", fontSize = 11.sp)
        }
        HorizontalDivider()
        OutlinedTextField(
            value = teks,
            onValueChange = { teks = it; tersimpan = false },
            modifier = Modifier.weight(1f).fillMaxWidth().padding(4.dp),
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
            keyboardOptions = KeyboardOptions(autoCorrectEnabled = false),
        )
    }
}
