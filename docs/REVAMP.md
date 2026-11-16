# Tenang: the calm revamp (plan)

Status: **P0–P7 done; P8 (package) in progress; P9 needs the TV** (see the log at the end). Target release: **6.0.0**. Branch: `ccr-032626ce-bc4wjg`.
Reference mockup: the "Calm TV Home" artifact (1600×900 stage, themes Malam / Senja / Embun, lite mode).

This plan turns that mockup into the launcher's design language across every screen, without
breaking the rules in [AGENTS.md](../AGENTS.md): light on 1 GB TVs, nothing running while another app
is in front, no new background services, no new network use.

---

## 1. Goals and non-goals

**Goals**
- One calm design language, **Tenang**, on every screen: home, menus, settings, weather, recent apps,
  azan, dialogs, screensaver.
- Three palettes: **Malam** (night, default), **Senja** (dusk), **Embun** (light morning), plus **Auto**,
  which follows the prayer times.
- Slow, soft motion: a drifting ambient background, a glowing focus ring, crossfades. It is fully
  switchable off, and off by itself on weak TVs.
- Clearer Islamic content: a doa/ayah card with proper Arabic, and a prayer card with the next prayer
  glowing and a countdown bar.

**Non-goals**
- No change to the debloat tools, install scripts, OTA, places pictures or the data sources (JAKIM, APIMS,
  MET Malaysia).
- No blur, no `RenderEffect`, no video backgrounds, no new libraries.
- No rewrite of `MainActivity` for its own sake. It is split only where the revamp touches it (see 4.4).

## 2. What the audit found (facts this plan is built on)

| Finding | Where | Consequence |
|---|---|---|
| Colours are fixed constants | `Design.java`, `res/values/colors.xml` (`sinar_*`, `canvas_*`) | A runtime palette needs a new layer |
| **269 typed-in hex colours** in layouts and drawables; 91 resource files use `sinar_`/`canvas_` colours | `res/layout`, `res/drawable`, `res/color` | Light theme (Embun) means migrating all of them |
| The "Outfit" font is actually **Inter Medium**, one weight only | `res/font/outfit.ttf` (417 KB, name table says Inter Medium) | The thin weight-200 clock is impossible today; Tenang needs the real Outfit |
| `inter.ttf` is a byte-identical, unused copy, and still ships | `res/font/inter.ttf`, `res/SR.ttf` + `res/fK.ttf` in the APK | 417 KB saved for free |
| APK is **2.1 MB** but CI fails above **400 KB** | `release/glass-tv-home.apk`, `.github/workflows/ci.yml` | CI's size check already fails; it needs a decision (see 9) |
| Biggest APK parts: `assets/avatar_character.json` (2.1 MB raw, Lottie), `classes.dex` 1.4 MB (Lottie + AppCompat) | build output | Outside this revamp, but listed in 9 |
| Amiri Quran is already bundled (137 KB) | `res/font/amiri_quran.ttf` | Arabic for the doa card is free |
| Reusable pieces exist | `Glider` (focus highlight), `DevicePerformance.isLowSpec`, `reduced_motion`, `WeatherBackgroundView` (stars), `PrayerTimes.getNextPrayerSummary`, `DailyAyat`/`WeeklyAyah`/`MinbarManager`, weather warnings | Build on them, don't duplicate |
| `MainActivity.java` is 7,175 lines, about 210 methods | | Home changes go into new small classes, not into it |
| Languages: Malay (default), English, Indonesian | `AppLanguage.java` | Every new string in all three |

## 3. The Tenang design system

All sizes are given for the mockup's 1600 px stage and in **dp for a 1080p TV (960×540 dp)**. The factor
is 0.6.

### 3.1 Palettes (from the mockup)

| Token | Malam | Senja | Embun | Used for |
|---|---|---|---|---|
| `bg1` / `bg2` | `#0C1A26` / `#16303A` | `#221B30` / `#3A2A3E` | `#E3ECE8` / `#CDDFD9` | 160° background gradient |
| `blobA` | teal 34 % | coral 30 % | white 75 % | Ambient glow, top left |
| `blobB` | violet 26 % | violet 30 % | sage 45 % | Ambient glow, bottom right |
| `text` | `#E9F0F1` | `#F7EEF0` | `#22363A` | Primary text |
| `muted` | `#8FA6AB` | `#BBA8B5` | `#5F7679` | Labels, eyebrows |
| `accent` | `#9FD8C9` | `#F6B9A2` | `#357E74` (mockup `#3D8B80` failed 3:1 on `bg2`) | Focus ring, next prayer, highlights |
| `accentGlow` | accent 45 % | accent 45 % | accent 35 % | Glow under the focus ring |
| `warm` | `#F1D3A1` | `#F8DCAE` | `#9A6A32` | Arabic text, bar end |
| `card` | white 5 % | white 5.5 % | white 50 % | Card fill |
| `line` | white 9 % | white 10 % | ink 8 % | Hairlines, card borders |
| `chip` | white 7 % | white 8 % | white 60 % | Pills and buttons |
| `amber` / `amberBg` | `#F2C27B` / 12 % | (as Malam) | `#A8661A` / 14 % | Warnings |
| `good` | `#8FD6A5` | (as Malam) | `#2F8A55` | IPU good, status dots |
| `stars` | 1 | 0.5 | 0 | Star opacity |

Contrast check before merging: `text` and `muted` against `bg1`, `bg2` and `card` must reach **WCAG AA
(4.5:1) for body text, 3:1 for 20 sp and larger**. Embun's `muted` is the one most at risk.

### 3.2 Type
- **Outfit**, variable font (OFL, already credited in `launcher/FONT-LICENSE-OFL.txt`), subset to
  Latin plus Latin Extended for Malay and Indonesian. Weights: 200 clock, 300 body, 400 regular, 500 labels.
  Android 8+ supports `fontVariationSettings`, so one file covers every weight. Expected size **≈ 60–90
  KB** after subsetting, replacing the 417 KB Inter copy.
- **Amiri Quran** for Arabic only.
- Scale (sp at 1080p): clock 90 · date 19 · greeting 13 · card title (eyebrow, caps, 2 sp letter
  spacing) 12 · body 12 · prayer time 22 · prayer name 12 · tile name 13 · button 14.

### 3.3 Shape and space
- Side gutter 58 dp; gap between cards 14 dp.
- Radius: cards 18 dp, dock tiles 16 dp, pills and buttons fully round (height 37 dp), prayer cells 12 dp.
- Borders: 1 px `line` on every surface. Shadows: none, apart from the focus glow.

### 3.4 Motion
- One curve everywhere: `PathInterpolator(0.22f, 1f, 0.36f, 1f)` (a soft ease-out).
- Focus: 320 ms scale (buttons ×1.08, tiles ×1.07 with an 6 dp lift) and the ring fading in.
- Entrance on Home start: rise 11 dp plus fade, 900 ms, staggered 80–480 ms. Not replayed when coming
  back from an app (same rule as today's `reduced_motion`).
- Crossfades: 450 ms veil (theme change, app launch).
- Ambient: glows drift over 38 s and 46 s, back and forth; the wave line loops every 70 s; a few stars
  twinkle over 5–7 s; the next-prayer glow and the warning dot breathe over 3–4 s.
- **Only `translation`, `scale` and `alpha` are animated.** Nothing re-lays out or redraws per frame.

### 3.5 Components (each built once, reused everywhere)
`TenangPill` (chip/button) · `TenangCard` · `FocusRing` (accent border + glow, replaces app-coloured edge;
the app colour stays as a faint tile tint) · `Eyebrow` (caps label with dot separators) · `PrayerRow`
(6 cells, next glows) · `CountdownBar` · `WarningChip` (breathing dot, ✕, slides away) · `Veil`
(fade overlay with optional "MEMBUKA <app>") · `AmbientView`.

## 4. Architecture

### 4.1 Palette at runtime
- New `Palette` class: the 16 tokens above as ints, with one builder per palette. It replaces the
  constants in `Design.java` (kept as a thin alias during migration, then removed).
- Three Android themes `Theme.Tenang.Malam / .Senja / .Embun` define the same tokens as
  **theme attributes** (`?attr/tnText`, `?attr/tnCard` …). Layouts and XML drawables use `?attr/`, so
  one layout serves all three palettes.
- Java-drawn things (`Glider`, `SinarPanelDrawable`, custom weather views) read `Palette.current()`.
- Switching palette: veil fades in, `setTheme` + `recreate()`, veil fades out. Wallpaper, focus
  position and dialogs are restored from saved state, so the person sees only a soft fade.

### 4.2 Choosing the palette
- Theme key `"palette": "malam" | "senja" | "embun" | "auto"` (default `"auto"`), and the same choice
  in Settings → Appearance.
- **Auto**: Embun from Syuruk to Asar, Senja from Asar to Isyak, Malam from Isyak to Syuruk. It uses
  the cached JAKIM times; without them, fixed hours (07:00 / 16:30 / 20:30).
  It checks only when Home comes to the front and on the existing minute tick: **no alarms, no service**.
- A "Never use light theme" switch for people who find Embun too bright at any hour (Auto then uses only
  Malam and Senja).

### 4.3 The ambient background (`AmbientView`)
- Two child views, each a `GradientDrawable` with a radial gradient (no bitmap, no hardware layer:
  a 1,200 px layer would cost about 5.5 MB per glow on a 1 GB TV). Their `translationX/Y` and `scale`
  are animated with `ViewPropertyAnimator`, so the GPU replays one draw op per frame and the UI thread
  does nothing.
- Stars: about 12 small dots; 5 of them twinkle (alpha only). Waves: one `Path`, drawn once, translated.
- Sits under the wallpaper layer. With a picture wallpaper it is hidden and a `bg1` scrim at 35 %
  keeps text readable instead.
- Stops (`cancel()`, views gone) in `onPause`, while the screensaver runs, while any dialog covers Home,
  with `reduced_motion`, and with Mod ringan.

### 4.4 Code structure for Home
New small classes, called from `MainActivity` and moving code out of it rather than adding to it:
`HomeClock` (clock, date, greeting) · `InfoRow` (weather, IPU, warning chip) · `DoaCard` · `PrayerCard`
· `HomeMotion` (entrance, veil, focus scale) · `AmbientView`. Target: `MainActivity` gets *shorter*.

### 4.5 Performance tiers
| Tier | When | Ambient | Focus | Entrance |
|---|---|---|---|---|
| Full | default | drifting glows, stars, waves | ring + glow, scale | staggered rise |
| Lite ("Mod ringan") | `DevicePerformance.isLowSpec`, `reduced_motion`, or the Settings switch | static gradient, no stars twinkling | ring, no glow animation | fade only |

**Budget**, measured with `dumpsys gfxinfo com.gathin.tvhome` on the reference TVs:
janky frames < 5 % while idle on Home and while moving focus; no new bitmap allocations; Java heap
within +2 MB of 5.0.0; idle CPU on Home < 3 %; 0 % with another app in front.

## 5. Screens: what changes on each

| Screen | Files | Changes |
|---|---|---|
| **Home** | `activity_main.xml`, `MainActivity`, new classes 4.4 | Thin clock + greeting; control pills; info chips; two cards; prayer glow + countdown; ambient; focus ring; launch veil; dock tiles keep app banners on a faint tint of the app's colour |
| Inputs menu | `popup_inputs.xml`, `item_input.xml` | Tenang card, pill rows, focus ring |
| Cards prompt / module cards | `dialog_card_prompt.xml`, `popup_cards.xml`, `item_card_row.xml` | Tenang card and buttons |
| **Settings** | `ModernSettingsDialog` (1,986 lines) | Tenang panel, tabs as pills, new Appearance rows: Tema, Never light, Mod ringan, Ambient on/off |
| **Weather** | `activity_weather.xml`, `WeatherActivity`, graph/gauge/compass/arc views | Palette-driven colours in every custom view; cards as `TenangCard` |
| Weather warning dialog | `dialog_weather_warning_16_9.xml` | Amber chip style, Tenang buttons |
| **Islamic** dialogs | `dialog_islamic_detail_16_9.xml`, `dialog_islamic_choice_16_9.xml` | Amiri in `warm`, eyebrow headers |
| **Azan** | `AzanActivity` | Full-screen calm card, Malam/Senja palette, breathing accent |
| **Recent apps** | `activity_recent_apps.xml`, `RecentAppsAdapter` | Cards, focus ring, close-all pill |
| Profiles / Who's watching / avatar / add profile | `WhoIsWatchingDialog`, `ProfileSettingsDialog`, `AvatarCustomizerDialog`, `AddProfileDialog` | Tenang surfaces and focus |
| Child mode | `ChildModeManager` | Tenang lock screen |
| Media hub / now playing | `dialog_media_hub.xml`, `NowPlayingManager` | Pills and card |
| Uploads, GIF search, OTA | `MobileUploadDialog`, `GifSearchDialog`, `dialog_ota_coffee.xml` | Tenang card and buttons |
| **Screensaver** | `ClockDream`, `view_nightstand.xml` | Thin Outfit clock, always Malam, very slow ambient (screen-safe: content shifts a few dp every minute) |
| Location, Home settings | `LocationActivity`, `HomeSettingsActivity` | Tenang list rows |

## 6. Phases

Each phase ends with: unit tests and release build passing, desktop preview screenshots of the changed
screens in all palettes built so far, and one commit (or a few) on the branch. Nothing is pushed to `main`
without your OK.

| # | Phase | Delivers | Check |
|---|---|---|---|
| **P0** | Groundwork | Remove the duplicate `inter.ttf`; add the real Outfit (subset, variable); `preview/tenang.html` (the mockup adapted, the visual reference from now on); "before" screenshots of every screen in 5.0.0; decision on the CI size check (9.1) | APK about 350 KB smaller; build green |
| **P1** | Tokens and theming engine | `Palette`, three `Theme.Tenang.*` themes, `?attr/` tokens, palette switching with the veil, `"palette"` key | Switching Malam ↔ Senja on Home by theme key works; no visual change otherwise |
| **P2** | Home layout | Classes from 4.4; clock, greeting, pills, info chips, warning chip, doa card, prayer card + countdown, dock tint | Home matches `preview/tenang.html` in Malam and Senja at 720p, 1080p and 4K; all three languages fit without cutting text |
| **P3** | Motion and focus | `FocusRing`, scale-on-focus, entrance, launch veil, breathing elements; all off in Lite | Focus moves feel the same speed as 5.0.0 or faster; gfxinfo budget met |
| **P4** | Ambient background | `AmbientView`, wallpaper interplay, lifecycle stops | 0 % CPU with an app in front; budget met on the weakest test TV |
| **P5** | Every other screen, dark palettes | Section 5 rows except Home | Each screen screenshotted in Malam and Senja; no `sinar_` colour left |
| **P6** | Embun (light) | Migrate the remaining hex colours to tokens; light variants of icons that are white today; contrast check | Every screen screenshotted in Embun; AA contrast met |
| **P7** | Auto and settings | Auto palette by prayer times, Appearance rows, Mod ringan, Never light | Clock-forward test: palette changes at Asar and Isyak without a restart |
| **P8** | Package and release | `themes/tenang/`, README, USER_GUIDE, PANDUAN_PENGGUNA, themes/README rows, screenshots, changelog in all three languages, version 6.0.0, `release/` APK + `version.json` | Docs tests pass (`CLEANYOURTV_STRICT=1`) |
| **P9** | Real-TV check | With the person at the TV, following AGENTS.md: install, Home, Inputs → HDMI, remote, mic, mirroring, a video, azan popup, screensaver | Everything on the AGENTS.md step 7 list works |

**Size of the work:** roughly 10–14 working sessions. P5 and P6 are the largest, each about 3 sessions.
P0–P4 alone (about 5 sessions) already give the full new Home in the dark palettes, and can ship as
**6.0.0-beta** if you want something on the TV sooner.

## 7. Testing

- Unit tests (JVM, `src/test`): `Palette` (all tokens, contrast ratios), auto-palette selection for times
  around each prayer boundary and with missing data, greeting by hour in all three languages, countdown
  text and bar fraction, warning-chip dismiss state.
- Layout checks without a TV: the desktop preview, plus Robolectric-free screenshot passes on an
  emulator at 720p, 1080p and 4K, in all three languages.
- Performance: `gfxinfo` and `meminfo` before and after on the reference TVs, recorded in the PR.
- Regression list from AGENTS.md step 7 before each release.

## 8. Risks

| Risk | Mitigation |
|---|---|
| Embun too bright in a dark room at night | Auto never picks Embun after Asar; "Never use light theme" switch |
| Ambient motion makes weak TVs stutter | Lite tier automatic on low-spec; budget checked on the weakest TV before P4 merges |
| `recreate()` on palette change loses state or flashes | Veil hides it; state saved; tested from inside every dialog |
| Long Malay warning texts and khutbah titles don't fit the chips | Ellipsis plus the existing detail dialog on OK; checked in all three languages |
| Soft accent less visible from the sofa than today's mint | Ring is 2 px with glow; check at 3 m on a real TV in P9; ring width a token if it needs to grow |
| Large diff in `MainActivity` conflicts with other work | Small classes, phase-by-phase commits; no unrelated refactors |
| App banners clash with pastel cards | Banners stay as they are; only the tile frame is tinted |

## 9. Decisions needed (with a recommendation for each)

1. **CI's 400 KB limit.** The APK is 2.1 MB because of the Lottie avatar (2.1 MB JSON) and Lottie in the
   dex. *Recommendation:* raise the check to 2.5 MB now so CI means something again, and open a separate
   task to slim the avatar (compress or download on demand). Tenang itself should make the APK smaller,
   not bigger.
2. **Default palette.** *Recommendation:* `auto`, with Malam where no prayer times are known.
3. **Sinar.** *Recommendation:* Tenang replaces Sinar as the default look in 6.0.0. The `sinar`
   wallpaper theme stays available.
4. **Wallpapers.** *Recommendation:* with the Tenang theme, the ambient background is the default and
   the picture wallpapers stay available in the Wallpaper menu, under a palette scrim.
5. **Dock tiles.** *Recommendation:* keep the real app banners (recognisable), with the mockup's soft
   tint behind them. No monochrome-icon mode.
6. **Hints row** (T / L keys in the mockup). *Recommendation:* drop it: remotes have no such keys; the
   choices live in Settings → Appearance.
7. **Embun scope.** *Recommendation:* do it (P6), but after P0–P5 ship, so the dark look reaches the TV
   first.
8. **Name.** "Tenang" for the design language and version 6.0.0. Change it if you prefer another word.

## 10. Progress log

Decisions in section 9: all accepted as recommended (2026-11-16).

**P0 (groundwork)**
- [x] Removed `res/font/inter.ttf` (an unused, byte-identical copy of the font shipped as `outfit.ttf`).
- [x] Replaced `outfit.ttf`, which was really Inter Medium, with the real Outfit (OFL, google/fonts
  `ofl/outfit/Outfit[wght].ttf`): static cuts at 200, 300, 400, 500 and 700, hinting removed, about
  52 KB each, collected in the `res/font/outfit.xml` family. `@font/outfit` and `R.font.outfit` keep
  working; fonts go from 834 KB to 261 KB.
- [x] CI's APK limit raised from 400 KB to 2.5 MB; AGENTS.md step 4 updated to match.
- [x] `preview/tenang.html`: the mockup adapted as the visual reference (bundled fonts, banner-style
  dock tiles, preview-only palette and Lite toolbar).
- [x] "Before" baseline: the 5.0.0 screenshots already in `docs/screenshots/sinar-*.jpg`.
- [x] Release build and unit tests with the new fonts: 48 tests pass; release APK 1.88 MB (was 2.14 MB).

**P0 done.** Next: P1, tokens and theming engine.

**P1 (tokens and theming engine)**
- [x] 28 tokens as theme attributes (`res/values/attrs_tenang.xml`), three overlays
  (`themes_tenang.xml`); `Theme.TvHome` carries Malam for contexts without an activity.
- [x] 224 `@color/sinar_*` / `canvas_*` references in 90 layout, drawable and colour files now use
  `?attr/tn…`. The legacy colour names stay, holding Malam values, for anything missed.
- [x] `Design` loads the tokens from the activity theme (same field names, so existing Java works);
  `Tenang` resolves the palette (Settings pref `palette`, theme key `palette`, default `auto`) and applies it
  in all six activities before their layouts inflate.
- [x] Home re-checks the palette on resume and on the minute tick (only while Home has focus), fades out
  450 ms, recreates and fades back in. `reduced_motion` skips the fades.
- [x] `background_color` now defaults to the palette background.
- [x] Embun accent darkened from the mockup's `#3D8B80` to `#357E74`: the contrast test found 2.91:1
  against `bg2`.
- [x] `TenangTest`: palette choice at every Auto boundary, fallbacks, and WCAG contrast for all three
  palettes. 55 tests pass; APK 1.88 MB.
- Not yet: the 684 colour literals in Java (custom weather views, dialogs built in code) still use
  Sinar-era values. Those are P5 (dark) and P6 (Embun).

**P2 (home layout)**
- [x] Font weights: the app's 122 XML and 47 Java "variable-font weight" settings never worked (the old
  file had one weight). They now pick real Outfit cuts: `android:textFontWeight` in layouts,
  `Fonts.weight()` in code (550/600 → 500).
- [x] Clock in Outfit ExtraLight at 90 sp; date in Light ("16 Nov 2026"); new greeting line in the accent
  colour, by hour and language (`HomeText`).
- [x] Info row as chips: weather, a separate IPU chip with a status dot in the API colour, and the warning
  chip (amber wash, slowly breathing dot, round ✕ inside).
- [x] Information rail as two cards (`tn_card`) of equal height (`EqualHeightRow`): ayah card with an
  eyebrow, Amiri in `warm`, right-aligned; prayer card with six equal cells, the next one outlined and
  breathing, and a countdown row ("Isyak ━━━ Subuh dalam 5j 36m", the bar is a `scaleX`).
- [x] Breathing stops in `onPause`, so nothing animates behind another app.
- [x] `HomeTextTest` (greetings, dates, durations, prayer window across midnight and Syuruk, progress,
  font cuts): 63 tests pass. APK 1.89 MB.
- [x] Snapshots of Home in all three palettes in `launcher/app/src/test/snapshots/images/`.
- Moved to P3: the dock tiles' faint app tint and the focus ring/glow on tiles (they belong with the focus
  work).

**P3 (motion and focus)**
- [x] Focus ring: always the palette accent (no longer the app's colour), 2 dp, with a soft glow of three
  faint outer strokes. No blur, so it costs the same on every GPU; reduced motion keeps the ring only.
  `Glider` redraws 13 dp around the shape, so the glow leaves no trails.
- [x] Tiles zoom to ×1.07 on the Tenang curve (320 ms in, 240 ms out) instead of bouncing.
- [x] Entrance once per boot: clock, top buttons and info row rise 11 dp and fade in, staggered 0/80/180 ms.
- Dropped after weighing it: the "MEMBUKA <app>" veil, because it would delay every app launch by about
  0.5 s; the app-coloured tile frame, because it would shrink the banners. The ring and glow carry the look.

**P4 (ambient background)**
- [x] `AmbientView`: the palette gradient, two radial glows drifting over 38 s and 46 s, 12 stars (6 twinkle
  by alpha) and the two wave lines looping over 70 s. No bitmaps and no hardware layers; only translation,
  scale and alpha change.
- [x] It steps at **15 fps** instead of 60. The drift is a few pixels a second, so it still looks smooth and
  the GPU repaints a quarter as often. Positions come from the clock alone, so a pause resumes in place.
- [x] It runs only while Home has window focus: it stops for dialogs, menus, other apps, the in-app
  screensaver and `onPause`. With reduced motion (or a low-spec TV) it draws once and never steps.
- [x] "Tenang" is now the default wallpaper category and the first option in the Wallpaper menu (offline,
  nothing downloaded). Choosing photos replaces it, and a picture and the canvas never stack. A pushed
  theme with its own wallpaper, or uploaded photos, still win.
- [x] `AmbientMathTest`; 65 tests pass; APK 1.89 MB.
- To measure on a real TV (P9): `dumpsys gfxinfo com.gathin.tvhome` idle on Home, against the §4.5 budget.

**P5 (every other screen, dark palettes)** and **P6 (Embun)**
- [x] 350 colour literals in 19 Java files now read the tokens: the old Sinar values map one to one
  (`0xFF2EF2C0` → `Design.ACCENT` …), translucent whites become `Design.ink(alpha)` (white on dark
  palettes, ink on Embun), and white text and icon tints become `Design.TEXT`. Focused text on the accent
  fill is `ON_ACCENT`; focus strokes in Settings are the accent.
- [x] 106 XML files: white icons and text use `?attr/tnText`, translucent white text `?attr/tnMuted`, old
  Sinar surfaces `?attr/tnSurface`, and the recent-apps backdrop the palette gradient. The notification
  icon, banner and launcher icon keep fixed colours (the system draws them without our theme).
- Kept on purpose: semantic colours (air-quality levels, weather visuals, VPN badge, avatar, brand colours
  such as Spotify green), scrims and shadows.
- [x] The weather screen and the azan card always use a dark palette (`Tenang.applyDark`); the
  screensavers are always Malam. Home reloads its own palette on resume.
- [x] Snapshots of eight secondary layouts in Malam and Embun
  (`launcher/app/src/snapshotTest/.../ScreensSnapshots.kt`).

**P7 (Auto and Settings)**
- [x] Auto now includes Embun from Syuruk to Asar (`EMBUN_IN_AUTO = true`).
- [x] Settings → Appearance: "🎨 Tema Tenang" (OK cycles Automatik → Malam → Senja → Embun) and
  "🌙 Jangan guna tema cerah" (Auto uses only Malam and Senja). Home switches with its fade as soon as
  Settings closes. "Mod Kelancaran TV Rendah" remains the Lite switch.
- [x] 66 tests pass.
