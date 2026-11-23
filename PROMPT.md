# The Prompt / Arahan AI (v7.0.0)

> 💡 **PENTING / CRITICAL**:
> Jangan pasang APK secara sideload biasa lalu berharap butang **HOME** berpindah sendiri. Pada
> Android TV / Google TV, aplikasi biasa akan kalah kepada pelancar asal. Gunakan
> [launcher/build-and-install.sh](launcher/build-and-install.sh) atau ikut [AGENTS.md](AGENTS.md):
> pasang dan uji Glass TV dahulu, kemudian barulah disable stock launcher.
>
> Playbook penuh ada dalam **[AGENTS.md](AGENTS.md)**. Prompt di bawah hanya memulakan kerja; jika
> bercanggah, AGENTS.md yang betul.

---

### ⚡ 1-Line Quick Prompt (Paling Ringkas)

Paste this single prompt into any AI coding agent (Claude Code, Google Antigravity, Cursor):

```text
install ni dekat tv rumah saya: https://github.com/muhaafidz/glass-tv-home
```
*(English alias: `install this on my home tv: https://github.com/muhaafidz/glass-tv-home`)*

When an AI assistant receives this prompt, it **must**:

1. **Semak model dahulu** — jenama, model tepat dan versi Android/`ro.build.version.sdk`. Tizen,
   VIDAA, Roku, Vizio, My Home Screen dan Apple TV tidak disokong; LG webOS guna aplikasi webOS
   berasingan ([webos/README.md](webos/README.md), pratonton, bukan skrin utama); Fire TV dan
   Android 5–7 hanya boleh debloat. Jangan mula "untuk mencuba".
2. **Sambung dengan selamat** — hidupkan USB/Network debugging, cari IP TV, dan sahkan dengan
   `tools/tv.py --probe`. Minta izin sebelum menekan butang atau mengambil tangkapan skrin.
3. **Backup dan debloat** — ikut AGENTS.md langkah 2–3; jangan uninstall pakej sistem, jangan sentuh
   HDMI/remote/Wi-Fi/Play Store, dan uji selepas setiap batch kecil.
4. **Pasang Glass TV v7** — APK rasmi: `TV_HOST=<ip> tools/venv/bin/python -I tools/b64push.py`
   untuk push, kemudian `pm install -r`; jika build sendiri: `TV_HOST=<ip> launcher/build-and-install.sh`
   (build, pasang, sahkan, rollback sendiri). Jangan disable stock launcher sebelum Glass TV terbukti
   berjalan.
5. **Sahkan** — Home mesti `com.gathin.tvhome/.MainActivity`; uji Home, Inputs → HDMI, video, mic dan
   tahan OK → Move. Tetapkan lokasi JAS/JAKIM dan tunjukkan rupa (Tenang / Lautan / Sorot).

---

### 📋 Full Detailed Prompt (Detailed Alternative)

Paste this into Claude Code (or another coding assistant with shell access) on a computer on the
same Wi-Fi as the TV. It works on Android TV / Google TV (Xiaomi, TCL, Sony, OnePlus, NVIDIA Shield
and most others, including Hisense and Toshiba models that run Android TV). It does **not** work on
Samsung Tizen, Hisense or Toshiba VIDAA, Roku, Vizio, Panasonic My Home Screen, or Apple TV. On Fire
TV only the debloat steps apply, and LG webOS has its own app instead:
[webos/README.md](webos/README.md).

Before you start, on the TV: Settings → Device Preferences → About → press OK on "Build" 7 times,
then Developer options → USB debugging on (and Network/ADB debugging if your TV shows it). On the
tested Xiaomi MiTV-MOOQ0, see [examples/xiaomi-mitv-mooq0/README.md](examples/xiaomi-mitv-mooq0/README.md).
Keep the TV on. Some boxes cut power to their USB port while USB debugging is on, so a USB speaker or
drive plugged into it goes dead until you switch debugging off again.

---

I want to install **Glass TV v7** on my Android TV and make it the home screen. Use the launcher in
this repository; do **not** design or build a new launcher from scratch. My TV model is:
`<MODEL NAME HERE>`. Work on the real device, step by step, and verify what you do with screenshots
pulled from the TV. Ask me before anything that depends on my preferences.

**Sources of truth**
- Read [AGENTS.md](AGENTS.md) and follow steps 1–7. It has the safety rules, model notes and checks.
- Use [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) before improvising a fix.

**Check the model first**
- Before connecting or downloading anything, check my model and Android version. If it runs Samsung
  Tizen, VIDAA, Roku, Vizio, My Home Screen or Apple TV, tell me plainly it cannot run this and stop.
  LG webOS uses its own app (preview), not these steps. Fire TV or Android 5–7: debloat only, no
  Glass TV home screen; suggest a Google TV stick if I want the full experience.
- Look up developer settings for my exact model. Do not send me through menus written for another TV.
- Audit the display (`dumpsys display`, `wm size`, `wm density`) and the local LAN IP.

**Connect**
- Find the TV on my network (router client list, hostname, cast name, or ask me for the IP). Once
  debugging is on, TCP 5555 is the sign; scan the subnet if needed.
- Set up the repo client once:
  `python3 -m venv tools/venv && tools/venv/bin/pip install -r tools/requirements.txt`, then
  `TV_HOST=<ip> tools/venv/bin/python -I tools/tv.py --probe`.
- Tell me when the "Allow USB debugging?" prompt will appear so I can tick Always allow. If the
  stock `adb` is blocked by macOS Local Network privacy, use `tools/tv.py` (it avoids the binary).
- If a file push hangs, use `TV_HOST=<ip> tools/venv/bin/python -I tools/b64push.py <apk>
  /data/local/tmp/tvhome.apk` and verify the md5 it prints. If `adb install` hangs on Android
  14 (on-screen confirmation), push the APK and run `pm install -r` over the shell.

**Back up first**
- Save the package list, disabled packages, `getprop`, `settings list system/secure/global`, and every
  app that claims HOME to `backup/`.
- Ask what is plugged into the USB ports; some TVs cut their power while debugging is on.

**Debloat (optional, ask me)**
- Sort packages into telemetry/analytics, manufacturer content and ad rows, background services,
  preloaded apps and streaming apps. Only the first groups are candidates without asking.
- Use `examples/<model>/debloat.txt` when one exists, comment out anything I use, and apply in small
  batches. Never uninstall system packages; disable for user 0 with `pm disable-user --user 0`.
- Never disable the TV/tuner, HDMI input services, input/source menu, remote and Bluetooth pairing,
  on-screen keyboard, Settings, Play Store/Play Services, or the stock launcher until Glass TV is
  proven to run.
- On Xiaomi, `com.duokan.factorytest` also claims HOME; handle it before disabling the stock launcher.
- Run `pm` commands one per shell call and check Home, HDMI, remote and Wi-Fi after each batch.

**Install Glass TV**
- Choose **one** source: the published `release/glass-tv-home.apk` (v7, ~1.9 MB, versionCode 70), or
  a build from this checkout (JDK 17 + SDK 34). Do not mix them: the published APK and a local build
  use different debug keys, and `pm install -r` fails with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`.
  Switching sources means uninstalling first (launcher settings are lost) or signing with the same key.
- On an ARM Linux host, if the build fails with `AAPT2 ... Daemon startup failed`, follow
  "Building" in `docs/TROUBLESHOOTING.md`; do not replace the SDK's aapt2 blindly.
- If you build from this checkout (JDK 17 + SDK 34): `TV_HOST=<ip> launcher/build-and-install.sh`.
  It installs, checks Glass TV is in front and not restarting, then disables the stock launcher and
  sets Home. On any failure it rolls back.
- If you use the published APK (no JDK/SDK needed): push it with
  `TV_HOST=<ip> tools/venv/bin/python -I tools/b64push.py <apk> /data/local/tmp/tvhome.apk`, run
  `pm install -r`, start it, and prove it stays in front before disabling the stock launcher
  (AGENTS.md step 5). Do not disable the stock launcher first.
- Ask my location in Malaysia and configure JAS/IPU + JAKIM:
  `TV_HOST=<ip> tools/venv/bin/python -I tools/tv.py 'am broadcast -a com.gathin.tvhome.SET_LOCATION -p com.gathin.tvhome --es query "<LOCATION>"'`

**Theme, not wallpapers from the internet**
- v7 has three looks: Tenang, Lautan and Sorot. Set them in Settings → Appearance → Rupa, Warna and
  Latar. There are no weekly/online picture downloads; "Foto saya" is my own photos uploaded by QR.
- On Android 10, the first internet permission can leave sockets failing with EPERM until the TV
  restarts. Ask me before restarting.

**Verify**
- Home must be `com.gathin.tvhome/.MainActivity` (`cmd package resolve-activity --brief -c
  android.intent.category.HOME -a android.intent.action.MAIN`).
- Screenshot with `screencap -p /sdcard/s.png` and pull the file; `exec-out` breaks on some MediaTek
  sets. Netflix, HDMI and Live TV screenshots are black or 0 bytes — press Home first.
- Test with the real remote: Home, Inputs → HDMI, play a video, the mic button, and hold OK on an app
  to Move it. An injected key is not proof for accessibility-backed buttons.
- Finish with a restore script, the theme folder, and the debugging choice: leave it on for automatic
  cache cleaning, or switch it off and cleaning stops.

---

Expect to answer a few checklist questions: which apps you use, which apps go in the dock (as many
as the theme's `columns`, five by default), whether to keep the voice assistant/Chromecast/firmware
updates, and where in Malaysia you are. Everything is reversible.
