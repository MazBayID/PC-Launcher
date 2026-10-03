# PC Launcher

Launcher Android bergaya desktop PC, dibuat dengan Kotlin + Jetpack Compose. Tanpa root dan tanpa internet.
Dirancang untuk Advan X1 (Helio G100, Android 14, layar 1080x2460), tapi berjalan di Android 8.0+.

## Fitur versi 0.2
- **Desktop** dengan ikon yang bisa **diseret** ke sel mana pun (bertukar tempat bila sel terisi), posisinya tersimpan.
- **Wallpaper**: 4 gradien bawaan atau **foto dari galeri**.
- **Taskbar** di bawah: tombol Start, aplikasi tersemat, level baterai, jam, dan tanggal.
- **Layar lebar** (lebar terkecil 600 dp ke atas): taskbar rata tengah dan Start menu lebih besar.
- **Start menu** dengan pencarian (tekan Enter untuk membuka hasil pertama).
- **Klik kanan mouse atau tekan lama** pada desktop dan ikon: buka, buka dalam jendela, sematkan ke taskbar,
  taruh di desktop, info aplikasi, dan copot pemasangan.
- **Selalu buka dalam jendela**: satu pengaturan di menu desktop agar semua aplikasi diminta tampil sebagai jendela.
- **Info layar dan DPI**: menampilkan lebar terkecil sekarang dan perintah ADB `wm density` untuk mencapai lebar tertentu.

## Cara pakai
1. Pasang APK dari **Releases** atau dari artifact di tab **Actions**.
2. Tekan tombol Home, pilih **PC Launcher**, lalu **Selalu**.
   Atau: Pengaturan > Aplikasi > Aplikasi default > Aplikasi layar utama.
3. Buka Start, tekan lama sebuah aplikasi, lalu sematkan ke taskbar atau taruh di desktop.

## Jendela aplikasi (freeform)
Opsi "Buka dalam jendela" hanya berhasil kalau ROM mendukung freeform windows:
Opsi pengembang > **Enable freeform windows**, lalu restart. Ukuran dan pemindahan jendela
diatur oleh sistem Android, bukan launcher, dan hasilnya bergantung pada ROM.

## Batasan saat ini
- Taskbar belum menampilkan aplikasi yang sedang berjalan (butuh izin khusus).
- Orientasi foto (EXIF) belum dikoreksi, jadi foto kamera bisa tampil miring.
- Output ke monitor eksternal bergantung pada perangkat. Tidak semua HP dengan chip Helio mendukung video lewat USB-C.

## Build
Push ke GitHub, lalu tab **Actions** membuat `PC-Launcher-debug.apk` otomatis.
Untuk rilis, buat tag: `git tag v0.2 && git push origin v0.2`.
Atau buka folder ini di Android Studio dan tekan Run.

## Lisensi
MIT, lihat `LICENSE`.
