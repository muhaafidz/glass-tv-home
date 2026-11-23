# 🤖 Glass TV v7 — Prompt Pemasangan AI (Android TV & Google TV)

> Salin dan tampal keseluruhan prompt di bawah ke pembantu AI yang boleh menjalankan arahan shell
> (**Claude Code**, **Google Antigravity**, **Cursor**) pada komputer yang bersambung ke Wi-Fi yang
> sama dengan TV. Prompt ini meminta AI mengikut playbook dalam repo, bukan mereka pemasangan sendiri.

```text
Saya mahu pasang Glass TV v7 pada Android TV rumah saya daripada repositori:
https://github.com/muhaafidz/glass-tv-home

Baca AGENTS.md dalam repo itu dan ikut langkah 1–7 sebagai playbook. Jangan reka launcher sendiri.
Jangan ubah apa-apa pada TV sebelum menyemak model dan mendapat kebenaran saya. Jika keluarga sedang
menonton, jangan tekan butang atau ambil tangkapan skrin tanpa bertanya.

FASA 1 — SEMAK MODEL, HIDUPKAN DEBUGGING, SAMBUNG
- Tanya jenama, model tepat dan versi Android (Settings → About). Semak docs/DEVICES.md.
- Samsung Tizen, Hisense/Toshiba VIDAA, Roku, Vizio, Panasonic My Home Screen dan Apple TV tidak boleh
  menjalankan apa-apa versi: beritahu saya terus dan berhenti. LG webOS ada aplikasi webOS berasingan
  (pratonton, bukan skrin utama): rujuk webos/README.md dan jangan guna langkah Android ini. Fire TV
  dan Android 5–7: debloat sahaja, tiada skrin utama Glass TV.
- Hidupkan debugging. Android TV biasa: Settings → Device Preferences → About → tekan OK pada Build
  7 kali → Developer options → USB debugging (dan Network/ADB debugging jika ada). Untuk Xiaomi
  MiTV-MOOQ0, ikut examples/xiaomi-mitv-mooq0/README.md.
- Jika IP TV tidak diketahui: lihat Settings → Network & Internet, senarai klien pada router, atau
  nama cast TV. Selepas debugging dihidupkan, port TCP 5555 terbuka; imbas LAN jika perlu.
- Pasang klien sekali sahaja:
    python3 -m venv tools/venv && tools/venv/bin/pip install -r tools/requirements.txt
    TV_HOST=<ip> tools/venv/bin/python -I tools/tv.py --probe
  Tandakan "Always allow" pada TV apabila prompt muncul. Jika SDK < 26, hentikan selepas debloat.

FASA 2 — BACKUP DAN DEBLOAT (PILIHAN, TANYA DAHULU)
- Backup senarai pakej, pakej disabled, getprop, settings system/secure/global dan semua app yang
  boleh menjadi HOME ke folder backup/.
- Gunakan examples/<model>/debloat.txt jika ada; tunjuk senarai kepada saya, komen apa yang saya guna,
  dan apply dalam batch ~10. Selepas setiap batch semak Home asal, HDMI, remote dan Wi-Fi.
- Jangan uninstall pakej sistem. Kekalkan TV/tuner/HDMI, menu input, remote/Bluetooth, keyboard,
  Settings, Play Store/Play Services dan stock launcher sehingga Glass TV terbukti berjalan.
  Pada Xiaomi, com.duokan.factorytest juga mendakwa HOME; jangan sentuh sehingga saya setuju.

FASA 3 — PASANG GLASS TV
- Pilih SATU sumber sahaja:
  A) APK rasmi v7: release/glass-tv-home.apk (~1.9 MB, versionCode 70). Tiada JDK/SDK diperlukan.
  B) Build sendiri: JDK 17 + Android SDK platform 34 dan build-tools 34.0.0;
     cd launcher && ./gradlew testReleaseUnitTest assembleRelease
     Jika host Linux ARM (Termux/proot, Raspberry Pi) gagal dengan "AAPT2 daemon startup failed",
     ikut docs/TROUBLESHOOTING.md bahagian Building. Jangan ganti aapt2 SDK secara membuta.
- Jangan campur APK rasmi dengan build sendiri. Kunci tandatangan berbeza, jadi `pm install -r` akan
  gagal dengan INSTALL_FAILED_UPDATE_INCOMPATIBLE. Jika bertukar sumber, uninstall dahulu (semua
  tetapan Glass TV hilang) atau guna kunci yang sama.
- Jika pilih B: TV_HOST=<ip> launcher/build-and-install.sh membina, memasang, mengesahkan Glass TV
  benar-benar berjalan, kemudian barulah disable stock launcher; jika apa-apa gagal ia rollback sendiri.
- Jika pilih A (tanpa JDK/SDK): pasang manual dengan b64push + pm install di bawah, uji Glass TV
  berjalan dan tidak restart, kemudian ikut AGENTS.md langkah 5 untuk menjadikannya Home. Jangan
  disable stock launcher sebelum Glass TV terbukti berjalan.
- Cara manual: TV_HOST=<ip> tools/venv/bin/python -I tools/b64push.py <apk> /data/local/tmp/tvhome.apk
  (push biasa boleh hang pada sesetengah TV; sahkan md5 dalam output), kemudian
  TV_HOST=<ip> tools/venv/bin/python -I tools/tv.py 'pm install -r /data/local/tmp/tvhome.apk',
  am start, dan semak Home.
- Aplikasi Glass TV Installer pada telefon boleh memasang APK yang sama tanpa komputer, tetapi ia
  hanya menetapkan keutamaan Home; stock launcher masih perlu di-disable ikut AGENTS.md langkah 5
  jika Home perlu kekal selepas reboot atau kemas kini sistem.
- Tetapkan lokasi Malaysia untuk JAS/IPU dan JAKIM:
    TV_HOST=<ip> tools/venv/bin/python -I tools/tv.py 'am broadcast -a com.gathin.tvhome.SET_LOCATION -p com.gathin.tvhome --es query "<LOKASI>"'
- Tema v7: Settings → Penampilan → Rupa (Tenang / Lautan / Sorot), Warna dan Latar. Tiada muat turun
  latar; "Foto saya" ialah foto sendiri melalui QR. Jika Android 10 baru memberi internet permission
  dan socket gagal EPERM, beritahu saya sebelum restart TV.

FASA 4 — SAHKAN DAN SERAHKAN
- Semak Home:
    cmd package resolve-activity --brief -c android.intent.category.HOME -a android.intent.action.MAIN
  mesti com.gathin.tvhome/.MainActivity.
- Tangkapan skrin: screencap -p /sdcard/s.png kemudian tarik fail itu. Jangan guna `exec-out` (rosak
  pada sesetengah MediaTek), dan skrin Netflix/HDMI/Live TV keluar hitam atau 0 bait — tekan Home
  dahulu, jangan salahkan sambungan ADB.
- Uji dengan remote sebenar: Home, Inputs → HDMI, main video, butang mic, dan tahan OK pada app → Move.
- Beri saya restore script, folder tema, dan pilihan debugging: kekal hidup untuk cache cleaning
  automatik, atau matikan dan cleaning berhenti (tiada perubahan lain).
```

## Nota untuk AI

- **Sumber kebenaran:** [AGENTS.md](AGENTS.md) mengandungi playbook penuh, had keselamatan dan nota
  model. Prompt ini ringkas; jika bercanggah, ikut AGENTS.md.
- **Jangan reka ciri:** v7 sudah ada tiga rupa (Tenang, Lautan, Sorot), palet, latar sendiri, waktu
  solat, cuaca/IPU, cache cleaning dan kemas kini OTA. Jangan tambah wallpaper online atau servis
  latar baharu.
- **Jangan samakan jenama dengan OS:** Hisense, Panasonic, Toshiba, TCL dan Philips menjual kedua-dua
  Android TV dan sistem lain. Yang penting ialah About/`ro.build.version.sdk`, bukan jenama.
- **Model yang diuji:** Xiaomi MiTV-MOOQ0 (Android 10) — lihat
  [examples/xiaomi-mitv-mooq0/README.md](examples/xiaomi-mitv-mooq0/README.md) untuk laluan
  debugging, IP, Home claimants dan nota tanda tangan.
