package com.pclauncher

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Penampil gambar dengan zoom cubit dan geser. Ketuk dua kali untuk mengembalikan. */
@Composable
fun AplikasiGambar(path: String) {
    var bmp by remember { mutableStateOf<ImageBitmap?>(null) }
    var gagal by remember { mutableStateOf(false) }
    var skala by remember { mutableFloatStateOf(1f) }
    var geser by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(path) {
        val hasil = withContext(Dispatchers.IO) { muatGambar(path) }
        if (hasil == null) gagal = true else bmp = hasil
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    skala = (skala * zoom).coerceIn(1f, 8f)
                    geser = if (skala == 1f) Offset.Zero else geser + pan
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { skala = 1f; geser = Offset.Zero })
            },
        contentAlignment = Alignment.Center,
    ) {
        val b = bmp
        if (b != null) {
            Image(
                b,
                null,
                Modifier.fillMaxSize().graphicsLayer(
                    scaleX = skala,
                    scaleY = skala,
                    translationX = geser.x,
                    translationY = geser.y,
                ),
                contentScale = ContentScale.Fit,
            )
        } else {
            Text(if (gagal) "Gambar tidak bisa dibuka" else "Memuat…", color = Color.White)
        }
    }
}

private fun muatGambar(path: String): ImageBitmap? {
    return try {
        val batas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, batas)
        var s = 1
        while (batas.outWidth / (s * 2) >= 2048 || batas.outHeight / (s * 2) >= 2048) s *= 2
        val bmp = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = s })
        if (bmp == null) {
            null
        } else {
            val putar = when (ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            val hasil = if (putar == 0f) bmp else Bitmap.createBitmap(
                bmp, 0, 0, bmp.width, bmp.height, Matrix().apply { postRotate(putar) }, true
            )
            hasil.asImageBitmap()
        }
    } catch (e: Exception) {
        null
    }
}
