# BB10 Launcher (Android)

Launcher Android yang meniru alur & tata letak home screen BlackBerry 10.
Kotlin + Jetpack Compose, minSdk 26, targetSdk 35.

## Fitur saat ini
- Didaftarkan sebagai launcher (`CATEGORY_HOME`)
- Daftar app via `LauncherApps` (termasuk Work Profile, auto-update saat install/uninstall)
- Grid 4 x 6 per halaman, label putih ber-shadow
- Indikator halaman: amplop (Hub) + nomor halaman aktif
- Dock bawah: Telepon | Search | Kamera
- Tap = buka app, long-press = App info

### Hub (revisi 2)
- Active Frames dihapus: swipe ke kanan dari halaman app pertama langsung ke Hub
- Animasi **peek** ala BB10: lapisan grid app digeser ke kanan, Hub (putih) diam di bawahnya
  - strip akun (ikon app + jumlah belum dibaca) menempel di tepi lapisan grid saat peek
  - lepas jari di bawah 35% lebar layar → kembali; lewat dari itu / fling → Hub terbuka penuh
  - swipe ke kiri di Hub → kembali ke grid
  - tap ikon akun di strip → Hub langsung terfilter ke app itu
- Hub: header putih, pemisah per hari (Today / Yesterday / tanggal), baris pesan
  (ikon, judul tebal bila belum dibaca, cuplikan, jam), garis biru penanda unread
- Action bar gelap: Hub (hapus filter) | Search | Mark read
- Data dari `NotificationListenerService` (perlu izin *Notification access*, ada tombolnya di Hub)
- Tap pesan → membuka pesan tersebut di app asalnya

File penting: `ui/hub/HubRevealState.kt` (logika animasi peek), `ui/hub/HubScreen.kt`,
`ui/hub/AccountStrip.kt`, `hub/HubNotificationService.kt`.

### Icon pack
- Long-press area kosong di home screen → pilih icon pack (atau "System icons")
- Mendukung pack format Nova / ADW / Apex / GO (`appfilter.xml` di `res/xml` atau `assets`)
- `item` (per app), `calendar` (ikon tanggal hari ini), serta `iconback` / `iconmask` / `iconupon` / `scale`
  untuk membungkus app yang tidak ada di pack
- Ikon Hub & strip akun ikut memakai icon pack
- Pilihan disimpan permanen; kalau pack di-uninstall otomatis kembali ke ikon sistem

File: `iconpack/IconPack.kt`, `iconpack/IconPackDiscovery.kt`, `ui/IconPackSheet.kt`, `data/LauncherPrefs.kt`.

### Revisi 4: tampilan BB10.3
- Status bar ala BB10 (jam di tengah, indikator di kiri, sinyal & baterai di kanan);
  status bar Android disembunyikan (bisa dimatikan di pengaturan home screen)
- Hub tema terang: title bar putih + judul biru + dropdown, date bar abu dengan tanggal lengkap,
  ikon berwarna = belum dibaca / monokrom = sudah dibaca
- Action bar 10.3: latar putih, signature action (lingkaran biru menonjol) di tengah:
  Accounts | Search | (Compose) | Mark read | More
- Daftar akun (panel gelap) dari kiri: tap judul, tombol Accounts, atau swipe kanan di Hub
- Action menu (panel gelap) dari kanan lewat tombol More
- Hub sedikit gelap selama peek, terang penuh saat terbuka
- Long-press area kosong → "Home screen": toggle status bar BB10 + pilih icon pack
- Font: BB10 memakai Slate Pro (berlisensi). Kalau punya lisensinya, taruh di `res/font`
  dan pasang di `Theme.kt`; sementara memakai sans-serif sistem.

## Cara menjalankan
1. Buka folder ini di Android Studio (Ladybug 2024.2 atau lebih baru), tunggu Gradle sync.
   - Kalau ingin build dari terminal, jalankan sekali `gradle wrapper` untuk membuat `gradlew`.
2. Run ke HP / emulator.
3. Tekan tombol Home → pilih **BB10 Launcher** → "Always".
4. Swipe ke kanan → di Hub tekan **Enable notification access** → aktifkan BB10 Launcher.

## Roadmap
- Gesture swipe-up untuk keluar dari app
- Folder, drag & drop ikon
- Ganti ikon per app secara manual dari icon pack
- Hub: balas cepat (RemoteInput), hapus pesan, MessagingStyle multi-pesan

> Catatan: gunakan ikon/wallpaper buatan sendiri; jangan pakai aset & logo BlackBerry asli bila dirilis publik.
