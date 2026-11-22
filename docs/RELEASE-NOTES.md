# Release notes

For whoever publishes (the release APK must be signed with the key behind 5.0.0 and 6.0.0, certificate
SHA-256 `f2ebc93c2a357a5948c2621177b8b1e17656973d8a2d38385bc311e76d9bf4b9`, or installed TVs refuse the update).

## 6.3.0 (versionCode 64)

Same publishing steps as below; 6.3.0 includes everything in 6.2.0 and 6.1.0.

`release/version.json` changelog:

```
✨ Glass TV v6.3.0 — Detik-detik kecil yang menyenangkan:
• Waktu solat tiba: sel solat itu terangkat dan bercahaya sekali; dalam Lautan, corak bintang turut bercahaya.
• Ucapan khas: Jumaat Mubarak (siang Jumaat), Selamat berpuasa (Ramadan), Selamat Hari Raya (Syawal dan Aidiladha).
• Penyelamat skrin mengikut rupa: latar Tenang, Lautan atau Sorot di bawah jam pada waktu siang; hitam dengan jam merah pada 10 malam hingga 6 pagi.
• Aplikasi membesar keluar daripada jubin yang ditekan.
• Setiap profil mengingati rupa dan warnanya sendiri; ucapan menyebut nama profil apabila ada lebih satu.
• Tetapan → Warna menunjukkan titik warna bagi setiap palet; kilauan lembut melintasi jubin sekali apabila fokus tiba.
```

English summary: a calm moment when a prayer time arrives; Friday, Ramadan and Raya greetings; a screensaver in
each look; apps grow out of the pressed tile; a look and palette per profile (the greeting uses the profile's
name when there are several); palette colour dots in Settings; a one-off sheen on focus. All event-driven, movement
and fade only, nothing running while another app is in front.

## 6.2.0 (versionCode 63)

Same publishing steps as 6.1.0 below. 6.2.0 includes everything in 6.1.0, so publishing 6.2.0 alone is fine.

`release/version.json` changelog:

```
✨ Glass TV v6.2.0 — Latar yang tenang:
• Latar kini milik setiap rupa: Bergerak (latar perlahan rupa itu), Pegun (sama, tanpa gerakan, paling ringan) atau Foto saya (foto anda dari telefon, di bawah warna lembut palet). Tetapan → Penampilan → Latar.
• Lebih banyak warna: Tenang (Subuh, Hutan, Pasir), Lautan (Biru, Karang, Bulan, Zamrud, Senja Kala), Sorot (Gelap, Arang, Hangat, Pudina, Mawar). Tetapan → Penampilan → Rupa / Warna.
• Sorot: latar warna aplikasi yang lebih lembut dan licin.
• Dibuang: koleksi wallpaper dalam talian dan gambar tempat mingguan (tiada lagi muat turun untuk latar), penapis mod gelap, kapsyen tempat dan kesan cuaca pada latar. Fail lama dipadam sekali untuk kosongkan storan.
```

English summary: the background belongs to the look (moving, still, or your own photos under the palette's tint);
five or more palettes per look; a smoother Sorot background; online collections, weekly places, the dark-mode
filter, place captions and the weather overlay removed, with their old downloads deleted once.

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
