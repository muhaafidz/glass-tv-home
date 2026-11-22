# Looks and layouts: research (Lautan, silhouette, vertical Home menu)

Status: **research, nothing built yet**. Follows [REVAMP.md](REVAMP.md) §11 and the mockup
[preview/lautan.html](../preview/lautan.html). Three questions: can Lautan have a blue anime-style silhouette
character, can Home be a vertical menu, and how do we keep one base layout that different looks restyle.

## 1. A blue silhouette character

**Yes, as an original character. Not the Persona protagonist.** The Persona 3 Reload menu's protagonist is a
character model made by Atlus specifically for that UI; the look, the hair over one eye and the headphones are
theirs. Shipping a likeness of him in a public APK is a copyright and trade-dress problem. An original
character (our own design, drawn as a flat blue silhouette with a few light cut-lines) gives the same feeling:
a figure suspended in the water beside the menu.

Who the character is, is your call (AGENTS.md rule 9). Some directions that fit the app and its audience:
- a Malaysian teen in a school uniform with a songkok or a tudung, eyes closed, drifting;
- a figure with headphones in a baju Melayu, side profile (headphones alone are generic; the combination of
  shape, hair and pose is what must be ours);
- a non-human mascot (a seahorse or a kingfisher, *pekaka*) for a family TV with children.
- Your own art: possible if you own it or have the rights.

**Cost on a 1 GB TV.** A large picture is the expensive part of that look, so we pick the format carefully:

| Format | APK | Memory on screen | First draw | Verdict |
|---|---|---|---|---|
| Full-colour PNG, 700×1080 | 150–400 KB | ~3 MB (ARGB) | fast | No: no palette tint, biggest |
| VectorDrawable at full height | 10–30 KB | ~3 MB bitmap cache at drawn size; rebuilt if the size changes | slow (ms per frame-size raster; Android advises ≤ 200 dp vectors) | Only for small icons |
| **One-colour PNG decoded as `ALPHA_8`, tinted by the palette** | 30–60 KB | **~0.75 MB** (1 byte per pixel) | fast | **Recommended** |

With `ALPHA_8` the silhouette takes the palette's colour (blue in Lautan, but it would also work in Senja or
Embun) and costs a quarter of a normal picture. Motion stays in the Lautan budget: a slow bob (translationY
~8 dp over ~6 s) and a slight sway, stepped by the same 15 fps tick as the water, stopped with Home not in front;
Lite shows it still.

## 2. A vertical Home menu

**Feasible, and it suits a TV remote.** Android's TV guidance: categories on the vertical axis and items within
a category on the horizontal axis, one obvious focus, and no deeply nested layouts. A vertical menu on the left,
with each entry's content to its right, follows that exactly.

The one risk is speed to the apps. Today an app is **one press** from Home (focus starts on the dock). A pure
Persona-style menu would make it APLIKASI → Right → app: three presses, every time. So:

**Recommended: a menu-led Home that still opens on the apps.**
- Left: the slanted vertical menu: APLIKASI, INPUT, WALLPAPER, TEMA, CUACA, SOLAT, PROFIL, TETAPAN.
- Each entry shows its content on the right when focused (APLIKASI: the favourite-apps row; INPUT: the HDMI
  list; SOLAT: the prayer card; CUACA: weather and IPU). Right moves into that content, Left or Back returns to
  the menu.
- Home opens (and Back on Home returns) with focus on **APLIKASI's first app**, so launching stays one press;
  Left from the first app goes to the menu.
- The top bar (Inputs, Wi-Fi, mic, profile, settings) folds into the menu; the mic stays a remote button.

The alternative, the mockup as it is (dock row at the bottom and a menu that opens with a key), is simpler but
less like the screenshot. Both use the same components (section 3).

## 3. One base layout, many looks

Today one class, `MainActivity` (7,375 lines, about 170 `findViewById` calls), both fetches the data (apps,
weather, IPU, warnings, prayer times, ayah, khutbah, now playing) and decides where each view sits. A second
layout would mean a second copy of that class. The fix is to separate the three things a look changes.

```
 data (once)            components (shared)            a look (per style)
 ───────────            ───────────────────            ──────────────────
 HomeWeather    ─┐      ClockBlock                      layout XML: which components, where
 PrayerTimes     │      StatusList (weather/IPU/warn)   theme overlay: colours, type, corners,
 WeeklyAyah      ├──►   PrayerBlock (next+list+count)     skew, focus shape (tokens)
 MinbarManager   │      AyahBlock                       backdrop: Tenang glows | Lautan water
 apps / inputs   │      AppRow (RecyclerView)             (+ silhouette)
 now playing    ─┘      MenuList (vertical, staggered)  focus shape: ring | slash
   = HomeState          FocusHighlight (Glider)
```

- **HomeState**: an immutable snapshot built from the existing data classes on the minute tick and on
  events. Components only render it; nothing fetches twice.
- **Components** are plain Views with the same IDs everywhere, styled only by tokens. The current Tenang tokens
  grow a few *shape* tokens: `tnSkew` (0° Tenang, −12° Lautan), `tnCorner`, `tnFocusShape` (ring | slash),
  `tnDisplayFont` and `tnDisplayWeight`.
- **A look** = one layout XML (`home_tenang.xml`, `home_lautan.xml`) + one theme overlay + a backdrop
  renderer. Most new looks need no Java: copy an XML, move the components, change tokens.
- **Switching**: `theme_prefs` `look` (`tenang` | `lautan`), separate from the Tenang `palette`, so going back
  to Tenang returns exactly the old look. Changing look reuses the palette switch (fade or slash wipe, recreate).
- **Safety net**: the Paparazzi snapshots already render Home. Extraction must keep Tenang's snapshots
  unchanged, pixel for pixel; that is the test that the base layout did not move.

## 4. Plan and size

| Step | Work | Sessions |
|---|---|---|
| L0 | Extract HomeState and the components from `MainActivity`, Tenang unchanged (snapshots identical) | 3–4 |
| L1 | Look switch: `look` pref and theme key, Tema menu with LAUTAN and the Tenang entries, wipe | 1 |
| L2 | Lautan backdrop (water, light shafts, shards) and the `ALPHA_8` silhouette with bob and sway | 1–2 |
| L3 | Lautan layout: vertical menu with per-entry content, slash focus, slanted blocks | 2–3 |
| L4 | Polish, Lite tier, real-TV check (gfxinfo budget from REVAMP §4.5) | 1 |

L0 is the investment; after it, Lautan and later looks are mostly XML and tokens.

## 5. Decisions needed

1. **Character**: which direction (section 1), or your own art? I can draft 2–3 original silhouettes as an
   update to the mockup for you to choose from.
2. **Home**: menu-led with focus starting on the apps (recommended), or the mockup's dock plus a pop-up menu?
3. **Order**: start with L0 (the shared base) before any Lautan code? Recommended, because it is what keeps
   both looks maintainable.

## Sources

- Android TV navigation: [Navigation on TV](https://developer.android.com/design/ui/tv/guides/foundations/navigation-on-tv),
  [TV navigation (D-pad)](https://developer.android.com/training/tv/get-started/navigation),
  [Focus system](https://d.android.com/design/ui/tv/guides/styles/focus-system)
- Vector cost: [VectorDrawable reference (one bitmap cache per drawable)](https://developer.android.com/reference/android/graphics/drawable/VectorDrawable),
  [VectorDrawableCompat (rebuilt on size change)](https://developer.android.com/reference/androidx/vectordrawable/graphics/drawable/VectorDrawableCompat),
  [Optimizing the performance of vector drawables](https://upday.github.io/blog/vector_drawables_optimisation)
- The Persona 3 Reload menu: [Automaton: what ideas went into its design](https://automaton-media.com/en/news/20240207-26873/),
  [GamesRadar: Persona 3 Reload's UI](https://www.gamesradar.com/persona-3-reloads-ui-goes-hard-and-one-indie-dev-reckons-theyve-figured-out-why)

## 6. Progress

Decisions (2026-11-22): mockup the characters first (done: A songkok, B tudung, C pekaka in
[preview/lautan.html](../preview/lautan.html)), then start L0.

**L0, slice 1: the first shared components**
- [x] `PrayerBlock` (header, six prayers with the next outlined and breathing, countdown), `ClockBlock`
  (period, date, greeting) and `StatusChips` (weather and IPU chips). Each binds by the standard view IDs and
  skips any a layout leaves out, so a Lautan layout can reuse them with different XML.
- [x] `MainActivity` delegates to them (110 lines and 16 fields fewer); its data reading is unchanged.
- [x] The Home snapshots now render through these components with sample data. The baseline changed in two
  places only, both now matching the app: the IPU chip's status dot and the prayer font weights, which the
  old snapshot code set by hand and skipped. From here on, extraction must keep these images identical.
- [x] 66 unit tests pass; release build green.
**L0, slice 2: the ayah / khutbah card**
- [x] `AyahBlock` renders the daily ayah or the Khutbah Minbar title; `MainActivity` keeps only the choice and
  the hiding rules.
- [x] Fixed on the way: the runtime code still set the Sinar-era styling (Arabic 16 sp in amber), overriding
  Tenang's card on the TV. It now uses Tenang's values (Arabic 22 sp in `warm`, label 12 sp, body 14 sp;
  ×1.3 on 4K panels with a 1080p-like density).
- [x] Snapshots render the card through `AyahBlock`; the only change is the Arabic line's tighter font padding,
  which the app always applied, so the cards are a few pixels shorter, as on the TV.
- Next slices: the warning chip, the top buttons, then the app row, so `MainActivity` keeps only data and
  wiring.

**Decision (2026-11-22): no character; Islamic geometry instead.** The silhouettes were dropped. Lautan's
centrepiece is now an 8-fold medallion (two interlaced 8-point stars, a ring of squares, eight petals, a small
star at the centre) turning very slowly, its two rings in opposite directions (180 s and 240 s a turn), over an
8-point-star lattice that drifts one tile every 90 s and loops. Corak A = medallion + lattice, Corak B = lattice
only. On the TV: the lattice is one ~120 px `ALPHA_8` tile repeated by a `BitmapShader` (a few KB of memory),
the medallion one ~700 px `ALPHA_8` image per ring (~0.5 MB each), both tinted by the palette and moved by
rotation and translation only, on the shared 15 fps tick. Lite shows them still.

**Chosen (2026-11-22): Corak B**, the drifting 8-point-star lattice alone. The medallion stays in the mockup as
Corak A but is not planned for the launcher: one tiled `ALPHA_8` image, translated at 15 fps.

## 7. Next look: Sorot (console-style home), mockup only

[preview/sorot.html](../preview/sorot.html). The mechanics of a console home in an original design (no console
maker's logos, fonts, icons or sounds): one row of apps at the top; the focused app grows, shows its name, and
takes over the screen with its own colours and a big hero ("Buka" and "⋯"); the launcher's widgets (next
prayer, weather and IPU, MET warning, daily ayah) become cards along the bottom. Left/Right move along a row,
Down goes row → hero buttons → cards.

**Light on a 1 GB TV.** The full-screen "art" is the focused app's own banner shrunk to 32×18 px and stretched
across the screen; the GPU's bilinear smoothing turns it into a soft colour field, so there is no blur and no
large decode (about 2 KB per layer). Changing app crossfades two such layers (opacity only, 0.6 s) and slides
the hero 24 px. Lite switches instantly.

**On the shared base (L0).** Reuses `PrayerBlock`, `StatusChips`, `AyahBlock` and `ClockBlock` in card-shaped
layouts, the app row (the existing `AppAdapter`, one row), and one new piece, the `HeroBackdrop` (two 32×18
layers). It is a third entry in Settings → Tema next to Lautan and the Tenang palettes.

**Flag: the inherited sound profiles.** `res/raw/ps5_*.wav`, `switch_*.wav` and `xbox_*.wav` (from upstream
Glass TV; PS5 is the default in `SoundEffects`) are named after consoles. If they are the consoles' own sounds,
they belong to Sony, Nintendo and Microsoft; before Sorot ships they should be replaced with original sounds and
the profiles renamed (for example Lembut, Jernih, Ringkas).

## 8. L1–L2: looks in the launcher (2026-11-22)

Direction from the owner: Lautan and Sorot are *inspired by* those homes, not copies; the inherited sound
profiles stay.

- [x] **Switching, with a way back.** `theme_prefs` `look` (`tenang` | `lautan` | `sorot`; theme key `"look"`),
  separate from the Tenang `palette`. Settings → Appearance → **🎨 Tema** cycles Automatik → Malam → Senja →
  Embun → Lautan → Sorot. Choosing Lautan or Sorot keeps the palette, so any Tenang entry restores exactly the
  old look. Home recreates with its fade when the choice changes (`Tenang.currentKey`).
- [x] **Look tokens**: `tnFocusShape`, `tnBackdrop`, `tnTextSkew`, plus full colour sets
  (`ThemeOverlay.Look.Lautan`, `ThemeOverlay.Look.Sorot`) applied over Malam.
- [x] **Lautan**: water gradient (light at the top, deep at the bottom), the Corak B star lattice (one ALPHA_8
  tile with a BitmapShader, drifting a tile every 90 s), two light shafts swaying over 26 s; the slash focus (a
  slanted white frame with a red one offset, outlines only, no glow); the clock, period, date and greeting in
  slanted Outfit Bold (`LookType`). Steps at 15 fps, stops with Home not in front; Lite keeps it still.
- [x] **Sorot**: the focused app's banner drawn into a cached 32×18 bitmap (`AppAdapter.artAt`, ~2 KB per app)
  and stretched full screen with filtering, two layers crossfading over 0.6 s, under top, bottom and side shades;
  no ticking at all; a white focus ring. Photos return when you go back to Tenang.
- [x] Snapshots `lookLautan` and `lookSorot`; `TenangTest` covers the Tema cycle and unknown looks. 68 tests.
- Not yet: the layout changes (Lautan's vertical menu; Sorot's app row at the top, hero and cards). They need the
  rest of L0 (warning chip, top buttons, app row as components) so a look can bring its own layout XML.

## 9. L3: each look's own arrangement (2026-11-22)

- [x] **Lautan menu** (`LautanMenu`): the slanted stacked words over a dark wash, the focused one on a white
  slash with a red edge. ◀ from the first app or the Menu key opens it, BACK closes. Entries call Home's own
  actions: APLIKASI, INPUT, CUACA, SOLAT, TEMA (next look), PROFIL, TETAPAN, TUTUP. Snapshot `lookLautanMenu`.
- [x] **Sorot arrangement**: a small clock (34 sp, no greeting), the app row parked at the top
  (`parkDock`), a hero under it with the focused app's name (44 sp) and "OK untuk buka", sliding 24 dp as focus
  moves along the first row and hidden below it, and the ayah and prayer cards along the bottom
  (`infoRailTop`), the ayah trimmed to two lines. Snapshot `lookSorot`.
- Done without new layout files: the looks reuse `activity_main.xml` and its IDs, so Home's focus code stays
  one. Remaining L0 slices (warning chip, top buttons, app row as components) are still worth doing before a
  fourth look; they are no longer blocking these two.
- Needs a real TV: D-pad paths in Sorot (Up from the app row to the chips and top buttons, Down into the
  grid), the Lautan menu's focus order, and the gfxinfo budget for both backdrops.

## 10. Revised placement (2026-11-22, mockups only)

Owner's direction: the Islamic content (prayer times, ayah) is not placed at the bottom of the screen; it
belongs at the top or on the right.

- **Lautan** ([preview/lautan.html](../preview/lautan.html)): the left side is one slanted vertical list that
  scrolls (the apps, then INPUT, CUACA, TEMA, TETAPAN), the focused entry on the slash; the right side, top
  to bottom: the clock and date, weather/IPU/warning chips, prayer times (next one on the slash bar), the ayah.
  ▲▼ moves, OK opens, ☰ still opens the full menu.
- **Sorot** ([preview/sorot.html](../preview/sorot.html)): a prayer strip right under the top bar (next prayer
  and countdown, the others, Hijri date and zone), the app row, the hero on the left with the ayah card beside it
  on the right, and only weather, air quality and MET warning cards along the bottom.
- In the launcher this replaces §9's arrangements: Lautan needs a one-column app list (a list item layout for
  the existing adapter) and the rail moved to a right column; Sorot moves the prayer card into a one-line strip
  under the top bar and the ayah card to the right of the hero.
