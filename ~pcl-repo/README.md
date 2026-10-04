# PC Launcher

Launcher Android bergaya desktop PC, dibuat dengan Kotlin + Jetpack Compose. Tanpa root dan tanpa internet.
Dirancang untuk Advan X1 (Helio G100, Android 14, layar 1080x2460), tapi berjalan di Android 8.0+.

## Fitur versi 0.4
**Jendela milik launcher**
- Bilah judul bisa digeser; ukuran diubah dari sudut kanan bawah; diperkecil, dimaksimalkan (atau ketuk dua kali), dan ditutup.
- **Snap**: seret ke tepi kiri atau kanan untuk setengah layar, ke tepi atas untuk memaksimalkan.
- Jendela yang terbuka muncul di taskbar. Jendela yang diperkecil tetap hidup, jadi halaman web tidak hilang.

**Aplikasi bawaan** (Start menu, taskbar, dan menu klik kanan desktop)
- **Berkas**: pintasan folder, urutkan (nama, terbaru, ukuran), ganti nama, hapus, folder baru. Gambar dan teks dibuka di jendela bawaan.
- **Peramban**: maju, mundur, muat ulang, kolom alamat atau pencarian.
- **Catatan**: editor teks yang menyimpan ke berkas.
- **Gambar**: zoom cubit, geser, ketuk dua kali untuk mengembalikan, orientasi EXIF dikoreksi.
- **Pengaturan**: Personalisasi, Tampilan (DPI), Jendela, Sistem (RAM, penyimpanan), Tentang.

**Taskbar dan panel**
- Ketuk ikon status untuk **panel cepat**: volume, kecerahan, pintasan Internet, Bluetooth, dan Layar.
- Ketuk jam untuk **kalender** bulanan.
- Tombol Start, aplikasi tersemat, jendela terbuka, baterai, jam, tanggal. Transparansi dan warna bisa diatur.

**Start menu bergaya Windows 11**
- Bagian **Disematkan**, **Direkomendasikan** (terakhir dibuka), dan **Semua aplikasi**, plus pencarian aplikasi dan web.

**Desktop**
- Ikon bisa diseret ke sel mana pun, wallpaper gradien atau foto galeri, ganti nama aplikasi, klik kanan mouse atau tekan lama.

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
Untuk rilis, buat tag: `git tag v0.4 && git push origin v0.4`.
Atau buka folder ini di Android Studio dan tekan Run.

## Lisensi
MIT, lihat `LICENSE`.
