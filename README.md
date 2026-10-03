# PC Launcher

Launcher Android bergaya desktop PC, dibuat dengan Kotlin + Jetpack Compose. Tanpa root dan tanpa internet.
Dirancang untuk Advan X1 (Helio G100, Android 14, layar 1080x2460), tapi berjalan di Android 8.0+.

## Fitur versi 0.1
- **Desktop** dengan ikon aplikasi dan wallpaper gradien (4 pilihan).
- **Taskbar** di bawah: tombol Start, aplikasi tersemat, level baterai, jam, dan tanggal.
- **Start menu** dengan pencarian (tekan Enter untuk membuka hasil pertama).
- **Klik kanan mouse atau tekan lama** pada desktop dan ikon: buka, buka dalam jendela, sematkan ke taskbar,
  taruh di desktop, info aplikasi, dan copot pemasangan.
- Menu desktop: ganti wallpaper, pintasan ke pengaturan layar, opsi pengembang, dan launcher default.

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
- Ikon desktop belum bisa diseret dan disusun ulang.
- Taskbar belum menampilkan aplikasi yang sedang berjalan (butuh izin khusus).
- Wallpaper berupa gradien bawaan, belum bisa memakai foto.
- Output ke monitor eksternal bergantung pada perangkat. Tidak semua HP dengan chip Helio mendukung video lewat USB-C.

## Build
Push ke GitHub, lalu tab **Actions** membuat `PC-Launcher-debug.apk` otomatis.
Untuk rilis, buat tag: `git tag v0.1 && git push origin v0.1`.
Atau buka folder ini di Android Studio dan tekan Run.

## Lisensi
MIT, lihat `LICENSE`.
