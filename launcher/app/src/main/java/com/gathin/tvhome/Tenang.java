package com.gathin.tvhome;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;

/**
 * Chooses and applies the Tenang palette (docs/REVAMP.md §4.1–4.2).
 *
 * The mode comes from Settings (theme_prefs "palette"), else the theme's config.json "palette",
 * else "auto". Auto follows today's prayer times: Senja from Asar to Isyak, Malam the rest of the day,
 * and, once every screen supports it, Embun from Syuruk to Asar. Nothing runs in the background:
 * the palette is checked when an activity starts and when Home's minute tick runs.
 */
final class Tenang {

    static final String MALAM = "malam", SENJA = "senja", EMBUN = "embun", AUTO = "auto";
    /** Looks (docs/LAYOUT-RESEARCH.md): Tenang uses the palettes above; Lautan and Sorot have their own colours. */
    static final String LOOK_TENANG = "tenang", LOOK_LAUTAN = "lautan", LOOK_SOROT = "sorot";
    static final String[] LOOKS = {LOOK_TENANG, LOOK_LAUTAN, LOOK_SOROT};

    /**
     * Each look's palettes, in the order Settings → Warna cycles them. The first of Lautan and Sorot is the
     * look's own default; Tenang's Auto follows the prayer times between Embun, Senja and Malam.
     */
    static final String[] TENANG_PALETTES = {AUTO, MALAM, SENJA, EMBUN, "subuh", "hutan", "pasir"};
    static final String[] LAUTAN_PALETTES = {"biru", "karang", "bulan", "zamrud", "senjakala"};
    static final String[] SOROT_PALETTES = {"gelap", "arang", "hangat", "pudina", "mawar"};

    /**
     * Whether Auto may pick the light palette (Syuruk to Asar). Every screen reads the tokens since
     * REVAMP P6; the weather sky and the azan card stay dark (applyDark). Settings can turn it off
     * per TV ("never_light").
     */
    static final boolean EMBUN_IN_AUTO = true;

    /** Prayer-time indexes in PrayerTimes order: Subuh, Syuruk, Zohor, Asar, Maghrib, Isyak. */
    private static final int SYURUK = 1, ASAR = 3, ISYAK = 5;

    /** Fallback boundaries (minutes after midnight) when no prayer times are known. */
    static final int FALLBACK_SYURUK = 7 * 60, FALLBACK_ASAR = 16 * 60 + 30, FALLBACK_ISYAK = 20 * 60 + 30;

    private static String applied = MALAM;

    private Tenang() {}

    /** A known mode, or AUTO for anything else (missing, misspelt, wrong type). */
    static String normaliseMode(String mode) {
        if (mode == null) return AUTO;
        String m = mode.trim().toLowerCase(java.util.Locale.ROOT);
        for (String known : TENANG_PALETTES) if (known.equals(m)) return m;
        return AUTO;
    }

    /**
     * The palette for a mode at a time of day. Pure, for tests.
     *
     * @param minutes     minutes after local midnight, 0..1439
     * @param prayerMins  today's six prayer times as minutes after midnight, or null when unknown
     * @param embunInAuto whether Auto may choose the light palette
     */
    static String resolve(String mode, int minutes, int[] prayerMins, boolean embunInAuto) {
        String m = normaliseMode(mode);
        if (!AUTO.equals(m)) return m;
        int syuruk = FALLBACK_SYURUK, asar = FALLBACK_ASAR, isyak = FALLBACK_ISYAK;
        if (prayerMins != null && prayerMins.length >= 6
                && prayerMins[SYURUK] < prayerMins[ASAR] && prayerMins[ASAR] < prayerMins[ISYAK]) {
            syuruk = prayerMins[SYURUK];
            asar = prayerMins[ASAR];
            isyak = prayerMins[ISYAK];
        }
        if (minutes >= asar && minutes < isyak) return SENJA;
        if (embunInAuto && minutes >= syuruk && minutes < asar) return EMBUN;
        return MALAM;
    }

    /** The palette that should be on screen now. */
    static String current(Context ctx) {
        java.util.Calendar now = java.util.Calendar.getInstance();
        int minutes = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE);
        int[] prayer = null;
        try {
            long[] t = PrayerTimes.todayTimes(ctx);
            if (t != null && t.length >= 6) {
                prayer = new int[6];
                java.util.Calendar c = java.util.Calendar.getInstance();
                for (int i = 0; i < 6; i++) {
                    c.setTimeInMillis(t[i]);
                    prayer[i] = c.get(java.util.Calendar.HOUR_OF_DAY) * 60 + c.get(java.util.Calendar.MINUTE);
                }
            }
        } catch (RuntimeException ignored) {
            // No zone or no cache yet: the fixed hours apply.
        }
        boolean embunInAuto = EMBUN_IN_AUTO && !ctx.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
                .getBoolean("never_light", false);
        return resolve(mode(ctx), minutes, prayer, embunInAuto);
    }

    /** A known look, or Tenang for anything else. */
    static String normaliseLook(String look) {
        if (look == null) return LOOK_TENANG;
        String l = look.trim().toLowerCase(java.util.Locale.ROOT);
        return LOOK_LAUTAN.equals(l) || LOOK_SOROT.equals(l) ? l : LOOK_TENANG;
    }

    /** The look: Settings (theme_prefs "look"), else the theme's config.json "look", else Tenang. */
    static String look(Context ctx) {
        String pref = ctx.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE).getString("look", null);
        return normaliseLook(pref != null ? pref : Config.lookKey(ctx));
    }

    /**
     * What should be on screen now, as one key: "lautan", "sorot", or the Tenang palette due now. Home
     * recreates when this changes.
     */
    static String currentKey(Context ctx) {
        String look = look(ctx);
        return LOOK_TENANG.equals(look) ? current(ctx) : look + ":" + paletteOf(ctx, look);
    }

    /**
     * The single Tema choice in Settings: Tenang's modes then the other looks, in the order OK cycles.
     * Picking a Tenang mode sets look=tenang and the palette; picking a look keeps the palette as it was,
     * so going back to Tenang returns exactly the old look.
     */
    static final String[] CHOICES = {AUTO, MALAM, SENJA, EMBUN, LOOK_LAUTAN, LOOK_SOROT};

    static String choice(Context ctx) {
        String look = look(ctx);
        return LOOK_TENANG.equals(look) ? mode(ctx) : look;
    }

    static String nextChoice(String choice) {
        for (int i = 0; i < CHOICES.length; i++) {
            if (CHOICES[i].equals(choice)) return CHOICES[(i + 1) % CHOICES.length];
        }
        return CHOICES[1];
    }

    static void setChoice(Context ctx, String choice) {
        android.content.SharedPreferences.Editor e = ctx.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE).edit();
        if (LOOK_LAUTAN.equals(choice) || LOOK_SOROT.equals(choice)) {
            e.putString("look", choice);
        } else {
            e.putString("look", LOOK_TENANG).putString("palette", normaliseMode(choice));
        }
        e.apply();
    }

    static String mode(Context ctx) {
        String pref = ctx.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE).getString("palette", null);
        return pref != null ? normaliseMode(pref) : normaliseMode(Config.paletteMode(ctx));
    }

    /** The Settings row cycles through the modes in this order. */
    static String nextMode(String mode) {
        switch (normaliseMode(mode)) {
            case AUTO: return MALAM;
            case MALAM: return SENJA;
            case SENJA: return EMBUN;
            default: return AUTO;
        }
    }

    static String label(Context ctx, String mode) {
        boolean en = AppLanguage.LANG_EN.equals(AppLanguage.getLanguage(ctx));
        if (mode == null) return "";
        if (AUTO.equals(mode)) return en ? "Auto" : "Automatik";
        switch (mode) {
            case LOOK_TENANG: return "Tenang";
            case "senjakala": return "Senja Kala";
            default: return Character.toUpperCase(mode.charAt(0)) + mode.substring(1);
        }
    }

    static String describe(Context ctx, String mode) {
        boolean en = AppLanguage.LANG_EN.equals(AppLanguage.getLanguage(ctx));
        if (mode == null) return "";
        switch (mode) {
            case AUTO: return en ? "Follows prayer times: Embun by day, Senja from Asar, Malam from Isyak"
                    : "Ikut waktu solat: Embun siang, Senja dari Asar, Malam dari Isyak";
            case MALAM: return en ? "Deep teal night" : "Biru kehijauan malam";
            case SENJA: return en ? "Warm dusk" : "Senja hangat";
            case EMBUN: return en ? "Light morning dew" : "Embun pagi yang cerah";
            case "subuh": return en ? "Pre-dawn indigo and lavender" : "Nila dan ungu lembut sebelum subuh";
            case "hutan": return en ? "Quiet forest greens" : "Hijau hutan yang tenang";
            case "pasir": return en ? "Warm sand and coffee" : "Pasir dan kopi yang hangat";
            case LOOK_TENANG: return en ? "Calm: colours that follow the day" : "Tenang: warna yang mengikut hari";
            case LOOK_LAUTAN: return en ? "Deep sea: slanted list left, Islamic info right" : "Laut dalam: senarai condong kiri, maklumat Islam kanan";
            case LOOK_SOROT: return en ? "Spotlight: the focused app colours the screen" : "Sorot: warna aplikasi memenuhi skrin";
            case "karang": return en ? "Reef teal with coral" : "Biru karang dengan oren karang";
            case "bulan": return en ? "Midnight navy with gold" : "Biru tengah malam dengan emas";
            case "zamrud": return en ? "Emerald with yellow" : "Zamrud dengan kuning";
            case "senjakala": return en ? "Violet dusk with pink" : "Ungu senja dengan merah jambu";
            case "biru": return en ? "Deep blue with red" : "Biru laut dalam dengan merah";
            case "gelap": return en ? "Black with a white ring" : "Hitam dengan cincin putih";
            case "arang": return en ? "Charcoal blue with sky" : "Arang kebiruan dengan biru langit";
            case "hangat": return en ? "Warm dark with amber" : "Gelap hangat dengan ambar";
            case "pudina": return en ? "Dark mint" : "Pudina gelap";
            case "mawar": return en ? "Dark rose" : "Mawar gelap";
            default: return "";
        }
    }

    static int overlayFor(String palette) {
        switch (palette == null ? "" : palette) {
            case SENJA: return R.style.ThemeOverlay_Tenang_Senja;
            case EMBUN: return R.style.ThemeOverlay_Tenang_Embun;
            case "subuh": return R.style.ThemeOverlay_Tenang_Subuh;
            case "hutan": return R.style.ThemeOverlay_Tenang_Hutan;
            case "pasir": return R.style.ThemeOverlay_Tenang_Pasir;
            default: return R.style.ThemeOverlay_Tenang_Malam;
        }
    }

    /** A Lautan or Sorot palette's colour overlay, or 0 for the look's own default colours. */
    static int lookPaletteOverlay(String palette) {
        switch (palette == null ? "" : palette) {
            case "karang": return R.style.ThemeOverlay_Look_Lautan_Karang;
            case "bulan": return R.style.ThemeOverlay_Look_Lautan_Bulan;
            case "zamrud": return R.style.ThemeOverlay_Look_Lautan_Zamrud;
            case "senjakala": return R.style.ThemeOverlay_Look_Lautan_Senjakala;
            case "arang": return R.style.ThemeOverlay_Look_Sorot_Arang;
            case "hangat": return R.style.ThemeOverlay_Look_Sorot_Hangat;
            case "pudina": return R.style.ThemeOverlay_Look_Sorot_Pudina;
            case "mawar": return R.style.ThemeOverlay_Look_Sorot_Mawar;
            default: return 0;
        }
    }

    static String[] palettesOf(String look) {
        if (LOOK_LAUTAN.equals(look)) return LAUTAN_PALETTES;
        if (LOOK_SOROT.equals(look)) return SOROT_PALETTES;
        return TENANG_PALETTES;
    }

    /** One of the look's palettes, or its first (the default) for anything unknown. */
    static String normalisePalette(String look, String palette) {
        if (LOOK_TENANG.equals(look)) return normaliseMode(palette);
        String[] list = palettesOf(look);
        String p = palette == null ? "" : palette.trim().toLowerCase(java.util.Locale.ROOT);
        for (String known : list) if (known.equals(p)) return p;
        return list[0];
    }

    /** The next item in a list, wrapping; the first for anything not in it. */
    static String next(String[] list, String current) {
        for (int i = 0; i < list.length; i++) if (list[i].equals(current)) return list[(i + 1) % list.length];
        return list[0];
    }

    /** The chosen palette of a look (each look remembers its own). */
    static String paletteOf(Context ctx, String look) {
        if (LOOK_TENANG.equals(look)) return mode(ctx);
        return normalisePalette(look, ctx.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE).getString(look + "_palette", null));
    }

    static void setLook(Context ctx, String look) {
        ctx.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE).edit().putString("look", normaliseLook(look)).apply();
    }

    static void setPalette(Context ctx, String look, String palette) {
        String key = LOOK_TENANG.equals(look) ? "palette" : look + "_palette";
        ctx.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE).edit().putString(key, normalisePalette(look, palette)).apply();
    }

    /**
     * Applies the current palette to an activity. Call in onCreate before setContentView, so every
     * layout and XML drawable resolves ?attr/tn… against it. Returns the palette applied.
     */
    static String apply(Activity activity) {
        String key = currentKey(activity);
        if (key.indexOf(':') > 0) return applyLook(activity, key);
        return apply(activity, key);
    }

    /** Malam's tokens as the base, then the look's overlay on top (its colours, focus shape, backdrop). */
    /** Applies a key's overlays to a theme: "look:palette" (Malam's tokens, the look, its palette) or a Tenang palette. */
    static void overlays(android.content.res.Resources.Theme theme, String key) {
        int colon = key.indexOf(':');
        if (colon > 0) {
            String look = key.substring(0, colon);
            theme.applyStyle(R.style.ThemeOverlay_Tenang_Malam, true);
            theme.applyStyle(LOOK_LAUTAN.equals(look) ? R.style.ThemeOverlay_Look_Lautan : R.style.ThemeOverlay_Look_Sorot, true);
            int colours = lookPaletteOverlay(key.substring(colon + 1));
            if (colours != 0) theme.applyStyle(colours, true);
        } else {
            theme.applyStyle(overlayFor(key), true);
        }
    }

    /** The screensaver's key: the current look's (Lautan, Sorot), or Malam for Tenang (bright is not calm at rest). */
    static String screensaverKey(Context ctx) {
        String key = currentKey(ctx);
        return key.indexOf(':') > 0 ? key : MALAM;
    }

    /** key = "look:palette": Malam's tokens as the base, the look's overlay, then its palette's colours. */
    private static String applyLook(Activity activity, String key) {
        overlays(activity.getTheme(), key);
        Design.load(activity);
        applied = key;
        if (activity.getWindow() != null) activity.getWindow().setBackgroundDrawable(new ColorDrawable(Design.BG1));
        return key;
    }

    /** For screens drawn for the dark (the weather sky, the azan card): Embun becomes Malam. */
    static String applyDark(Activity activity) {
        String key = currentKey(activity);
        if (key.indexOf(':') > 0) return applyLook(activity, key);   // Lautan and Sorot palettes are all dark
        String p = current(activity);
        return apply(activity, EMBUN.equals(p) ? MALAM : p);
    }

    static String apply(Activity activity, String palette) {
        activity.getTheme().applyStyle(overlayFor(palette), true);
        Design.load(activity);
        applied = palette;
        if (activity.getWindow() != null) {
            android.content.res.TypedArray a = activity.obtainStyledAttributes(new int[]{android.R.attr.windowIsTranslucent});
            boolean translucent = a.getBoolean(0, false);
            a.recycle();
            if (!translucent) activity.getWindow().setBackgroundDrawable(new ColorDrawable(Design.BG1));
        }
        return palette;
    }

    /** The palette most recently applied in this process. */
    static String applied() {
        return applied;
    }
}
