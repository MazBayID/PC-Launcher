# PC Launcher

Launcher Android bergaya desktop PC, dibuat dengan Kotlin + Jetpack Compose. Tanpa root dan tanpa internet.
Dirancang untuk Advan X1 (Helio G100, Android 14, layar 1080x2460), tapi berjalan di Android 8.0+.

## Fitur versi 0.3
- **Jendela melayang milik launcher**: bisa digeser lewat bilah judul, diubah ukurannya dari sudut kanan bawah,
  diperkecil, dimaksimalkan (atau ketuk dua kali bilah judul), dan ditutup. Jendela yang terbuka muncul di taskbar.
- **Aplikasi bawaan** (di Start menu dan menu klik kanan desktop):
  - **Berkas**: jelajah penyimpanan, buka berkas, buat folder, hapus. Butuh izin "Akses semua file".
  - **Peramban**: WebView dengan tombol maju, mundur, muat ulang, dan kolom alamat atau pencarian.
  - **Pengaturan PC**: transparansi dan warna taskbar, taskbar rata tengah, mode jendela, dan info DPI.
- **Ganti nama aplikasi** lewat menu klik kanan ikon.
- **Desktop**: ikon bisa diseret ke sel mana pun dan posisinya tersimpan; wallpaper gradien atau foto galeri.
- **Taskbar**: tombol Start, aplikasi tersemat, jendela terbuka, baterai, jam, dan tanggal.
- **Layar lebar** (lebar terkecil 600 dp ke atas): taskbar rata tengah dan Start menu lebih besar.
- **Start menu** dengan pencarian. Hasil kosong menawarkan pencarian web; Enter membuka hasil pertama.
- **Klik kanan mouse atau tekan lama** pada desktop dan ikon: buka, buka dalam jendela, sematkan ke taskbar,
  taruh di desktop, ganti nama, info aplikasi, dan copot pemasangan.

## Cara pakai
1. Pasang APK dari **Releases** atau dari artifact di tab **Actions**.
2. Tekan tombol Home, pilih **PC Launcher**, lalu **Selalu**.
   Atau: Pengaturan > Aplikasi > Aplikasi default > Aplikasi layar utama.
3. Buka Start, tekan lama sebuah aplikasi, lalu sematkan ke taskbar atau taruh di desktop.

## Jendela aplikasi lain (freeform)
Jendela milik launcher (Berkas, Peramban, Pengaturan PC) selalu berfungsi. Untuk aplikasi Android lain, opsi "Buka dalam jendela" hanya berhasil kalau ROM mendukung freeform windows:
Opsi pengembang > **Enable freeform windows**, lalu restart. Ukuran dan pemindahan jendela
diatur oleh sistem Android, bukan launcher, dan hasilnya bergantung pada ROM.

## Batasan saat ini
- Taskbar menampilkan jendela milik launcher, belum aplikasi Android lain yang sedang berjalan (butuh izin khusus).
- Orientasi foto (EXIF) belum dikoreksi, jadi foto kamera bisa tampil miring.
- Output ke monitor eksternal bergantung pada perangkat. Tidak semua HP dengan chip Helio mendukung video lewat USB-C.

## Build
Push ke GitHub, lalu tab **Actions** membuat `PC-Launcher-debug.apk` otomatis.
Untuk rilis, buat tag: `git tag v0.3 && git push origin v0.3`.
Atau buka folder ini di Android Studio dan tekan Run.

## Lisensi
MIT, lihat `LICENSE`.
