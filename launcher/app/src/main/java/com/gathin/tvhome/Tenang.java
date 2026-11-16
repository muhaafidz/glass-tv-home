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
        return MALAM.equals(m) || SENJA.equals(m) || EMBUN.equals(m) ? m : AUTO;
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
        switch (normaliseMode(mode)) {
            case MALAM: return "Malam";
            case SENJA: return "Senja";
            case EMBUN: return "Embun";
            default: return en ? "Auto" : "Automatik";
        }
    }

    static String describe(Context ctx, String mode) {
        boolean en = AppLanguage.LANG_EN.equals(AppLanguage.getLanguage(ctx));
        switch (normaliseMode(mode)) {
            case MALAM: return en ? "Deep teal night, always" : "Biru kehijauan malam, sepanjang masa";
            case SENJA: return en ? "Warm dusk, always" : "Senja hangat, sepanjang masa";
            case EMBUN: return en ? "Light morning dew, always" : "Embun pagi yang cerah, sepanjang masa";
            default: return en ? "Follows prayer times: Embun by day, Senja from Asar, Malam from Isyak • OK to change"
                    : "Ikut waktu solat: Embun siang, Senja dari Asar, Malam dari Isyak • Tekan OK untuk tukar";
        }
    }

    static int overlayFor(String palette) {
        if (SENJA.equals(palette)) return R.style.ThemeOverlay_Tenang_Senja;
        if (EMBUN.equals(palette)) return R.style.ThemeOverlay_Tenang_Embun;
        return R.style.ThemeOverlay_Tenang_Malam;
    }

    /**
     * Applies the current palette to an activity. Call in onCreate before setContentView, so every
     * layout and XML drawable resolves ?attr/tn… against it. Returns the palette applied.
     */
    static String apply(Activity activity) {
        return apply(activity, current(activity));
    }

    /** For screens drawn for the dark (the weather sky, the azan card): Embun becomes Malam. */
    static String applyDark(Activity activity) {
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
