# Changing a look's colours

Every look (Tenang Malam, Senja, Embun; Lautan; Sorot) takes all its colours from **one block** in
[`launcher/app/src/main/res/values/themes_tenang.xml`](../launcher/app/src/main/res/values/themes_tenang.xml):
`ThemeOverlay.Tenang.Malam`, `.Senja`, `.Embun`, `ThemeOverlay.Look.Lautan`, `ThemeOverlay.Look.Sorot`.
Layouts read them as `?attr/tn…`, Java as `Design.…`, and the moving backgrounds from the same tokens, so
changing a value there changes it everywhere in that look. Rebuild the APK afterwards (`./gradlew assembleRelease`).

| Token | What it colours |
|---|---|
| `tnBg1`, `tnBg2` | The background (gradient from one to the other) |
| `tnBlobA`, `tnBlobB` | Tenang's two drifting glows; Lautan's light water at the top (`tnBlobA`) |
| `tnSurface`, `tnSurfaceRaised`, `tnSurfaceFocus`, `tnHairline` | Solid surfaces: dialogs, Settings, menus |
| `tnCard`, `tnChip`, `tnLine` | See-through cards, chips and their borders on Home |
| `tnText`, `tnMuted` | Main and secondary text |
| `tnAccent`, `tnAccentSoft`, `tnAccentDim`, `tnAccentGlow`, `tnOnAccent` | Focus ring or slash, highlights, next prayer, text on the accent |
| `tnWarm` | The Arabic text and the end of the countdown bar |
| `tnAmber`, `tnAmberBg`, `tnGood`, `tnCoral` | Warning, warning chip fill, good status, errors |
| `tnCyan`, `tnViolet` | Secondary accents (Lautan's list words, light shafts) |
| `tnWave`, `tnStars`, `tnInk` | Tenang's wave line / Lautan's lattice colour, star opacity, the base of see-through washes |
| `tnIsLight`, `tnFocusShape`, `tnBackdrop`, `tnTextSkew` | Look switches, not colours |

**Check the contrast.** `TenangTest.palettesMeetTextContrast` checks every `ThemeOverlay.Tenang.*` block against
WCAG (text 4.5:1, secondary text and accent 3:1) and fails the build's tests if a change makes text hard to read.

**Kept on purpose** (not palette colours): air-quality levels, weather pictures, the VPN badge, the avatar, brand
colours (Spotify green), the red night mode of the screensaver, the white behind the DuitNow QR, scrims and shadows.

**From a theme folder (no rebuild)** you can choose *which* look and palette (`"look"`, `"palette"`) but not
define new colours: the tokens are compiled into the APK. A new palette is a new block in `themes_tenang.xml`
plus an entry in `Tenang` (see `docs/LAYOUT-RESEARCH.md` §12 for a whole new look).
