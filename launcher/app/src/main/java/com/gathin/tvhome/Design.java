package com.gathin.tvhome;

import android.content.Context;
import android.content.res.TypedArray;

/**
 * The active Tenang palette for code that draws in Java. Values come from the activity's theme
 * (the ?attr/tn… tokens in themes_tenang.xml), so Java and XML never disagree. Tenang.apply()
 * loads them in each activity's onCreate; until then they hold Malam.
 *
 * The old Sinar names (CANVAS, SURFACE, HAIRLINE, ...) are kept as fields so existing code works
 * unchanged while the screens are migrated.
 */
final class Design {

    static int BG1 = 0xFF0C1A26, BG2 = 0xFF16303A, BLOB_A = 0x5758AAAA, BLOB_B = 0x427C74C8;
    static int SURFACE = 0xFF182531, SURFACE_RAISED = 0xFF1F2C37, SURFACE_FOCUS = 0xFF27333E, HAIRLINE = 0xFF222F3A;
    static int CARD = 0x0DFFFFFF, CHIP = 0x12FFFFFF, LINE = 0x17FFFFFF;
    static int TEXT = 0xFFE9F0F1, TEXT_MUTED = 0xFF8FA6AB;
    static int ACCENT = 0xFF9FD8C9, ACCENT_SOFT = 0x1F9FD8C9, ACCENT_DIM = 0xFF7FC4B3, ACCENT_GLOW = 0x739FD8C9;
    static int ON_ACCENT = 0xFF0C1A26, WARM = 0xFFF1D3A1;
    static int AMBER = 0xFFF2C27B, AMBER_BG = 0x1FF2C27B, GOOD = 0xFF8FD6A5, CORAL = 0xFFF08A8A;
    static int CYAN = 0xFF8CCFE6, VIOLET = 0xFFA79BE8, WAVE = 0x12FFFFFF;
    static float STARS = 1f;
    static boolean IS_LIGHT = false;
    /** Sinar name for the screen background. */
    static int CANVAS = BG1;

    private static final int[] ATTRS = {
            R.attr.tnBg1, R.attr.tnBg2, R.attr.tnBlobA, R.attr.tnBlobB,
            R.attr.tnSurface, R.attr.tnSurfaceRaised, R.attr.tnSurfaceFocus, R.attr.tnHairline,
            R.attr.tnCard, R.attr.tnChip, R.attr.tnLine, R.attr.tnText, R.attr.tnMuted,
            R.attr.tnAccent, R.attr.tnAccentSoft, R.attr.tnAccentDim, R.attr.tnAccentGlow,
            R.attr.tnOnAccent, R.attr.tnWarm, R.attr.tnAmber, R.attr.tnAmberBg, R.attr.tnGood,
            R.attr.tnCoral, R.attr.tnCyan, R.attr.tnViolet, R.attr.tnWave, R.attr.tnStars, R.attr.tnIsLight,
    };

    /** Reads every token from the context's theme; a missing token keeps its current value. */
    static void load(Context ctx) {
        TypedArray a = ctx.obtainStyledAttributes(ATTRS);
        try {
            int i = 0;
            BG1 = a.getColor(i++, BG1); BG2 = a.getColor(i++, BG2);
            BLOB_A = a.getColor(i++, BLOB_A); BLOB_B = a.getColor(i++, BLOB_B);
            SURFACE = a.getColor(i++, SURFACE); SURFACE_RAISED = a.getColor(i++, SURFACE_RAISED);
            SURFACE_FOCUS = a.getColor(i++, SURFACE_FOCUS); HAIRLINE = a.getColor(i++, HAIRLINE);
            CARD = a.getColor(i++, CARD); CHIP = a.getColor(i++, CHIP); LINE = a.getColor(i++, LINE);
            TEXT = a.getColor(i++, TEXT); TEXT_MUTED = a.getColor(i++, TEXT_MUTED);
            ACCENT = a.getColor(i++, ACCENT); ACCENT_SOFT = a.getColor(i++, ACCENT_SOFT);
            ACCENT_DIM = a.getColor(i++, ACCENT_DIM); ACCENT_GLOW = a.getColor(i++, ACCENT_GLOW);
            ON_ACCENT = a.getColor(i++, ON_ACCENT); WARM = a.getColor(i++, WARM);
            AMBER = a.getColor(i++, AMBER); AMBER_BG = a.getColor(i++, AMBER_BG); GOOD = a.getColor(i++, GOOD);
            CORAL = a.getColor(i++, CORAL); CYAN = a.getColor(i++, CYAN); VIOLET = a.getColor(i++, VIOLET);
            WAVE = a.getColor(i++, WAVE);
            STARS = a.getFloat(i++, STARS);
            IS_LIGHT = a.getBoolean(i, IS_LIGHT);
            CANVAS = BG1;
        } finally {
            a.recycle();
        }
    }

    private Design() {}
}
