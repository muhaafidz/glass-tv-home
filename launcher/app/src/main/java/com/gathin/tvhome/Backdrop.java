package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

/**
 * What sits behind Home (Settings → Appearance → Latar). The background belongs to the look: each look's own
 * moving background (Tenang's glows, Lautan's water and lattice, Sorot's app colours), the same still, or the
 * person's own photos under the palette's tint. Sorot always shows the focused app's colours.
 *
 * There are no downloaded collections any more: nothing is fetched for the background.
 */
final class Backdrop {

    static final String MOVING = "moving", STILL = "still", PHOTOS = "photos";
    static final String[] CHOICES = {MOVING, STILL, PHOTOS};

    private static final String PREFS = "theme_prefs", KEY = "backdrop", CLEANED = "old_backdrops_removed";

    private Backdrop() {}

    static String normalise(String v) {
        if (v == null) return MOVING;
        String s = v.trim().toLowerCase(java.util.Locale.ROOT);
        return STILL.equals(s) || PHOTOS.equals(s) ? s : MOVING;
    }

    /** The choice; on first run after the update, photos if the person had chosen their uploads, else moving. */
    static String get(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String v = p.getString(KEY, null);
        if (v != null) return normalise(v);
        String oldCategory = ctx.getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE).getString("active_category", "");
        String migrated = "Custom".equalsIgnoreCase(oldCategory) && !photos(ctx).isEmpty() ? PHOTOS : MOVING;
        p.edit().putString(KEY, migrated).apply();
        return migrated;
    }

    static void set(Context ctx, String v) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, normalise(v)).apply();
    }

    static String next(String v) {
        for (int i = 0; i < CHOICES.length; i++) if (CHOICES[i].equals(v)) return CHOICES[(i + 1) % CHOICES.length];
        return MOVING;
    }

    /** The person's uploaded photos (newest first). */
    static java.util.List<File> photos(Context ctx) {
        File dir = new File(ctx.getFilesDir(), "custom_wallpapers");
        File[] files = dir.listFiles((d, n) -> {
            String l = n.toLowerCase(java.util.Locale.ROOT);
            return l.endsWith(".jpg") || l.endsWith(".jpeg") || l.endsWith(".png");
        });
        java.util.List<File> out = new java.util.ArrayList<>();
        if (files == null) return out;
        java.util.Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        for (File f : files) if (f.length() > 0) out.add(f);
        return out;
    }

    static String label(Context ctx, String v) {
        boolean en = AppLanguage.LANG_EN.equals(AppLanguage.getLanguage(ctx));
        switch (normalise(v)) {
            case STILL: return en ? "Still" : "Pegun";
            case PHOTOS: return en ? "My photos" : "Foto saya";
            default: return en ? "Moving" : "Bergerak";
        }
    }

    static String describe(Context ctx, String v) {
        boolean en = AppLanguage.LANG_EN.equals(AppLanguage.getLanguage(ctx));
        switch (normalise(v)) {
            case STILL: return en ? "The look's own background, without motion: lightest on the TV"
                    : "Latar rupa ini tanpa gerakan: paling ringan untuk TV";
            case PHOTOS: return en ? "Your photos from your phone, under the palette's soft tint (not in Sorot)"
                    : "Foto anda dari telefon, di bawah warna lembut palet (bukan untuk Sorot)";
            default: return en ? "The look's own slow, calm background" : "Latar rupa ini yang bergerak perlahan dan tenang";
        }
    }

    /** Once: delete the collections and places pictures older versions downloaded, to give the space back. */
    static void dropOldDownloads(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (p.getBoolean(CLEANED, false)) return;
        File base = Config.dir(ctx);
        if (base != null) {
            deleteTree(new File(base, "online-wallpapers"));
            deleteTree(new File(base, "place-wallpapers"));
        }
        ctx.getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE).edit().remove("active_category").apply();
        p.edit().putBoolean(CLEANED, true).apply();
    }

    private static void deleteTree(File f) {
        if (f == null || !f.exists()) return;
        File[] kids = f.listFiles();
        if (kids != null) for (File k : kids) deleteTree(k);
        //noinspection ResultOfMethodCallIgnored
        f.delete();
    }
}
