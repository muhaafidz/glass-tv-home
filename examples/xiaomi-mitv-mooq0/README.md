# Xiaomi MiTV-MOOQ0 ("croods") — model notes

Tested end to end in November 2026 with **Glass TV 7.0.0** (versionCode 70), installed over Wi-Fi.

| Item | Value |
|---|---|
| Model / codename | MiTV-MOOQ0 / `croods` |
| OS | Android TV 10 (API 29), firmware `QTG3.200305.006.5135` |
| Panel | 1080p, MediaTek m7632, 1.8 GB RAM, low-RAM mode |
| Cast / mDNS name | `Living Room TV` |
| Published APK certificate | SHA-256 `f2ebc93c2a357a5948c2621177b8b1e17656973d8a2d38385bc311e76d9bf4b9` |

## Developer options and the IP

1. Settings → Device Preferences → About → press **OK seven times** on **Build**.
2. Settings → Device Preferences → **Developer options** → **USB debugging** on, and **Network debugging**
   on if the entry is present. USB debugging alone is not enough on this set.
3. Accept **Allow USB debugging?** with **Always allow**. The prompt appears the first time `tools/tv.py`
   connects; without it the client times out.
4. The IP is under Settings → Network & Internet → the connected Wi-Fi network. The TV also answers mDNS
   as `Living Room TV`; before debugging, ports 6466 (remote) and 8009 (cast) are open. After Network
   debugging is on, **5555** is the one that matters.

## Home claimants

`cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME` on
this set listed:

- `com.duokan.factorytest/.FactoryLauncher` — a factory test app that also claims Home. Disable it
  (after asking) before disabling the stock launcher, or Home can land on it.
- `com.gathin.tvhome/.MainActivity` — Glass TV.
- `com.android.tv.settings/.system.FallbackHome` — Android's fallback.

The debloat list [debloat.txt](debloat.txt) keeps the factory test package so `restore.sh` can re-enable
it. `com.google.android.tvlauncher` is deliberately left out of that list: `launcher/build-and-install.sh`
disables it only after Glass TV is proven to run, and puts it back if anything fails.

## Install and transfer quirks

- `tools/tv.py --push` timed out after 300 s for the 1.9 MB APK on this set, even though `--probe`,
  shell commands and `--pull` worked. `tools/b64push.py` transferred the same APK with a matching md5.
  `launcher/build-and-install.sh` already uses `b64push.py`, so prefer the script.
- `pm install -r` over the existing app worked and kept its data. Installing a locally built APK over the
  published one fails with `INSTALL_FAILED_UPDATE_INCOMPATIBLE` because the debug keys differ; uninstall
  first (launcher settings are lost) or build with the published key.
- After the first internet permission, Android 10 can fail sockets with `EPERM` until the TV restarts.
  Ask before restarting; the launcher works offline until then.

## Screenshots and checks

- `screencap -p /sdcard/s.png` then `tv.py --pull` produces a valid PNG. Do not use `exec-out`.
- Netflix, HDMI and Live TV captures are black or **0 bytes**; press Home before capturing. A 0-byte
  capture does not mean ADB is broken.
- Glass TV is a per-profile look: after Home restarts, the active profile can restore its own Rupa
  (Tenang / Lautan / Sorot). Check Settings → Penampilan → Rupa if the look changes by itself.

## Useful commands

```bash
TV_HOST=<ip> tools/venv/bin/python -I tools/tv.py --probe
TV_HOST=<ip> tools/disable.sh examples/xiaomi-mitv-mooq0/debloat.txt
TV_HOST=<ip> launcher/build-and-install.sh
TV_HOST=<ip> tools/restore.sh examples/xiaomi-mitv-mooq0/debloat.txt
```
