# Release notes

For whoever publishes (the release APK must be signed with the key behind 5.0.0 and 6.0.0, certificate
SHA-256 `f2ebc93c2a357a5948c2621177b8b1e17656973d8a2d38385bc311e76d9bf4b9`, or installed TVs refuse the update).

## 7.0.0 (versionCode 70)

The published version is 6.0.0 (Tenang). 7.0.0 is everything since: two new looks, five or more palettes per
look, backgrounds that belong to the look, and a set of small moments. (Internal milestones 6.1 to 6.3 were
never published; 7.0.0 includes them.)

Steps: `git pull` on `main` → `cd launcher && ./gradlew testReleaseUnitTest assembleRelease` with the release
key → copy `app/build/outputs/apk/release/app-release.apk` to `release/glass-tv-home.apk` (and into the
installer's assets, then rebuild `release/glass-tv-installer.apk`, as for 6.0.0) → update `release/version.json`
(versionName `7.0.0`, versionCode `70`) → update the version numbers and sizes in README, USER_GUIDE,
PANDUAN_PENGGUNA and `release/README.md` (the download badge and the "Next, v7.0.0" lines) → push.

`release/version.json` changelog (Malay, as the app shows it):

```
✨ Glass TV v7.0.0 — Tiga rupa, latar yang tenang:
• Tiga rupa: Tenang (menenangkan), Lautan (laut dalam, senarai condong di kiri, waktu solat dan ayat di kanan) dan Sorot (warna aplikasi yang dipilih memenuhi skrin). Tetapan → Penampilan → Rupa.
• Setiap rupa ada lima palet warna atau lebih (Tenang tujuh termasuk Automatik): Tetapan → Penampilan → Warna, dengan titik warna untuk dipilih.
• Latar milik setiap rupa: Bergerak, Pegun (paling ringan) atau Foto saya (foto anda di bawah warna lembut palet). Koleksi dalam talian, gambar tempat mingguan, penapis mod gelap, kapsyen dan kesan cuaca pada latar dibuang; fail lama dipadam sekali.
• Detik kecil: waktu solat tiba (sel solat bercahaya), ucapan Jumaat Mubarak / Selamat berpuasa / Selamat Hari Raya, penyelamat skrin mengikut rupa, aplikasi membesar daripada jubin, rupa bagi setiap profil.
• Ringan untuk TV 1GB: tiada muat turun untuk latar, tiada blur; animasi berhenti apabila aplikasi lain dibuka.
```

English summary: three looks (Tenang soothing, Lautan stylish and calm, Sorot a spotlight on the app), each with
five or more palettes chosen by colour dots; the background belongs to the look (moving, still, or your own
photos); online collections, weekly places, the dark-mode filter, captions and the weather overlay removed, old
downloads deleted once; small moments (a prayer arriving, special-day greetings, a screensaver per look, apps
growing out of the tile, a look per profile).

Check on one TV before publishing: Lautan ▲▼ through the whole list and ◀ / Menu for the menu; Sorot Up from the
app row to the chips and buttons, Down into the grid; the launch zoom and the focus sheen look right; switching
profiles switches the look; all backdrops smooth (`dumpsys gfxinfo com.gathin.tvhome`).
