# Release notes

For whoever publishes (the release APK must be signed with the key behind 5.0.0 and 6.0.0, certificate
SHA-256 `f2ebc93c2a357a5948c2621177b8b1e17656973d8a2d38385bc311e76d9bf4b9`, or installed TVs refuse the update).

## 6.1.0 (versionCode 62)

Steps: `git pull` on `main` → `cd launcher && ./gradlew testReleaseUnitTest assembleRelease` with the release
key → copy `app/build/outputs/apk/release/app-release.apk` to `release/glass-tv-home.apk` (and into the
installer's assets, then rebuild `release/glass-tv-installer.apk`, as for 6.0.0) → update `release/version.json`
→ update the version numbers and sizes in README, USER_GUIDE, PANDUAN_PENGGUNA and `release/README.md` → push.

`release/version.json` changelog (Malay, as the app shows it):

```
✨ Glass TV v6.1.0 — Rupa baharu:
• Lautan: senarai aplikasi condong di kiri, jam, cuaca, waktu solat dan ayat di kanan; corak bintang hanyut perlahan dan sorotan merah-putih.
• Sorot: warna aplikasi yang dipilih memenuhi skrin; jalur waktu solat di atas, nama aplikasi besar dan ayat di sebelah kanan.
• Tukar di Tetapan → Penampilan → Tema (Automatik, Malam, Senja, Embun, Lautan, Sorot). Pilih mana-mana Tenang untuk kembali seperti asal.
• Kad ayat kini mengikut gaya Tenang (tulisan Arab lebih besar, warna lembut).
• Ringan untuk TV 1GB: tiada gambar dimuat turun, tiada blur; animasi berhenti apabila aplikasi lain dibuka.
```

English summary: two new looks, Lautan (deep sea; slanted app list left, Islamic info right) and Sorot (spotlight;
the focused app's colours fill the screen, prayer strip at the top), switchable in Settings → Appearance → Tema
with a way back to Tenang; the ayah card now uses Tenang's styling on the TV.

Check on one TV before publishing: Lautan ▲▼ through the whole list and ◀ / Menu for the menu; Sorot Up from the
app row to the chips and buttons, Down into the grid; both backdrops smooth (`dumpsys gfxinfo com.gathin.tvhome`).
