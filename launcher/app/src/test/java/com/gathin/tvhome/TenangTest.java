package com.gathin.tvhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/** Tenang palette choice (Tenang.resolve) and the palettes' text contrast (docs/REVAMP.md §3.1). */
public class TenangTest {

    // Shah Alam-like day: Subuh 05:48, Syuruk 06:55, Zohor 13:00, Asar 16:20, Maghrib 18:58, Isyak 20:10.
    private static final int[] DAY = {5 * 60 + 48, 6 * 60 + 55, 13 * 60, 16 * 60 + 20, 18 * 60 + 58, 20 * 60 + 10};

    @Test
    public void explicitModesWin() {
        assertEquals("malam", Tenang.resolve("malam", 17 * 60, DAY, true));
        assertEquals("senja", Tenang.resolve("SENJA ", 3 * 60, DAY, true));
        assertEquals("embun", Tenang.resolve("embun", 23 * 60, DAY, false));
    }

    @Test
    public void unknownModeIsAuto() {
        assertEquals("auto", Tenang.normaliseMode(null));
        assertEquals("auto", Tenang.normaliseMode("sinar"));
        assertEquals("senja", Tenang.resolve("sinar", 17 * 60, DAY, false));
    }

    @Test
    public void autoFollowsAsarAndIsyak() {
        assertEquals("malam", Tenang.resolve("auto", 16 * 60 + 19, DAY, false));
        assertEquals("senja", Tenang.resolve("auto", 16 * 60 + 20, DAY, false));
        assertEquals("senja", Tenang.resolve("auto", 20 * 60 + 9, DAY, false));
        assertEquals("malam", Tenang.resolve("auto", 20 * 60 + 10, DAY, false));
        assertEquals("malam", Tenang.resolve("auto", 0, DAY, false));
    }

    @Test
    public void autoUsesEmbunOnlyWhenAllowed() {
        assertEquals("malam", Tenang.resolve("auto", 10 * 60, DAY, false));
        assertEquals("embun", Tenang.resolve("auto", 10 * 60, DAY, true));
        assertEquals("malam", Tenang.resolve("auto", 6 * 60 + 54, DAY, true));
        assertEquals("embun", Tenang.resolve("auto", 6 * 60 + 55, DAY, true));
        assertEquals("senja", Tenang.resolve("auto", 16 * 60 + 20, DAY, true));
    }

    @Test
    public void autoFallsBackWithoutPrayerTimes() {
        assertEquals("malam", Tenang.resolve("auto", Tenang.FALLBACK_ASAR - 1, null, false));
        assertEquals("senja", Tenang.resolve("auto", Tenang.FALLBACK_ASAR, null, false));
        assertEquals("malam", Tenang.resolve("auto", Tenang.FALLBACK_ISYAK, null, false));
        // Times out of order are ignored rather than trusted.
        int[] broken = {0, 0, 0, 23 * 60, 0, 1};
        assertEquals("senja", Tenang.resolve("auto", 17 * 60, broken, false));
    }

    @Test
    public void autoMayPickEmbunNowThatEveryScreenIsMigrated() {
        assertEquals(true, Tenang.EMBUN_IN_AUTO);
    }

    @Test
    public void temaCyclesThroughPalettesThenLooks() {
        assertEquals("malam", Tenang.nextChoice("auto"));
        assertEquals("embun", Tenang.nextChoice("senja"));
        assertEquals("lautan", Tenang.nextChoice("embun"));
        assertEquals("sorot", Tenang.nextChoice("lautan"));
        assertEquals("auto", Tenang.nextChoice("sorot"));
        assertEquals("malam", Tenang.nextChoice("rubbish"));
    }

    @Test
    public void everyLookHasAtLeastFivePalettes() {
        for (String look : Tenang.LOOKS) {
            assertTrue(look, Tenang.palettesOf(look).length >= 5);
        }
    }

    @Test
    public void palettesNormaliseAndCycle() {
        assertEquals("biru", Tenang.normalisePalette("lautan", null));
        assertEquals("bulan", Tenang.normalisePalette("lautan", " BULAN "));
        assertEquals("gelap", Tenang.normalisePalette("sorot", "malam"));
        assertEquals("hutan", Tenang.normalisePalette("tenang", "hutan"));
        assertEquals("auto", Tenang.normalisePalette("tenang", "karang"));
        assertEquals("karang", Tenang.next(Tenang.LAUTAN_PALETTES, "biru"));
        assertEquals("gelap", Tenang.next(Tenang.SOROT_PALETTES, "mawar"));
        assertEquals("lautan", Tenang.next(Tenang.LOOKS, "tenang"));
        assertEquals("tenang", Tenang.next(Tenang.LOOKS, "sorot"));
    }

    @Test
    public void everyPaletteHasAnOverlay() {
        for (String p : Tenang.LAUTAN_PALETTES) {
            if (!p.equals("biru")) assertTrue(p, Tenang.lookPaletteOverlay(p) != 0);
        }
        for (String p : Tenang.SOROT_PALETTES) {
            if (!p.equals("gelap")) assertTrue(p, Tenang.lookPaletteOverlay(p) != 0);
        }
    }

    @Test
    public void unknownLookIsTenang() {
        assertEquals("tenang", Tenang.normaliseLook(null));
        assertEquals("tenang", Tenang.normaliseLook("ps5"));
        assertEquals("lautan", Tenang.normaliseLook(" LAUTAN "));
        assertEquals("sorot", Tenang.normaliseLook("sorot"));
    }

    @Test
    public void settingsCycleThroughEveryMode() {
        assertEquals("malam", Tenang.nextMode("auto"));
        assertEquals("senja", Tenang.nextMode("malam"));
        assertEquals("embun", Tenang.nextMode("senja"));
        assertEquals("auto", Tenang.nextMode("embun"));
        assertEquals("malam", Tenang.nextMode("rubbish"));
    }

    @Test
    public void palettesMeetTextContrast() throws Exception {
        Map<String, Map<String, Integer>> palettes = readPalettes();
        assertEquals("6 Tenang, 5 Lautan, 5 Sorot", 16, palettes.size());
        for (Map.Entry<String, Map<String, Integer>> e : palettes.entrySet()) {
            Map<String, Integer> p = e.getValue();
            int bg1 = p.get("tnBg1"), bg2 = p.get("tnBg2"), surface = p.get("tnSurface");
            int card = over(p.get("tnCard"), bg2);
            for (int bg : new int[]{bg1, bg2, surface, card}) {
                assertContrast(e.getKey() + " text", p.get("tnText"), bg, 4.5);
                assertContrast(e.getKey() + " muted", p.get("tnMuted"), bg, 3.0);
                assertContrast(e.getKey() + " accent", p.get("tnAccent"), bg, 3.0);
            }
            assertContrast(e.getKey() + " on-accent", p.get("tnOnAccent"), p.get("tnAccent"), 3.0);
            if (e.getKey().startsWith("Look.Lautan")) {
                // Lautan's clock sits over the light water at the top of the screen.
                assertContrast(e.getKey() + " clock on shallow water", p.get("tnText"), p.get("tnBlobA") | 0xFF000000, 4.5);
            }
        }
    }

    /** Each look's mood as numbers (docs/PALETTES.md, "Mood first"). */
    @Test
    public void palettesFitTheirLookMood() throws Exception {
        for (Map.Entry<String, Map<String, Integer>> e : readPalettes().entrySet()) {
            String name = e.getKey();
            Map<String, Integer> p = e.getValue();
            double[] bg1 = hsv(p.get("tnBg1")), bg2 = hsv(p.get("tnBg2")), accent = hsv(p.get("tnAccent"));
            double contrast = contrast(p.get("tnText"), p.get("tnBg1"));
            if (name.startsWith("Look.Lautan")) {
                // Stylish and calm: deep cool water, one vivid accent, nothing neon.
                assertTrue(name + " deep water brightness", bg1[2] <= 0.25);
                assertTrue(name + " deep water saturation", bg1[1] >= 0.60);
                assertTrue(name + " water hue " + bg1[0], bg1[0] >= 150 && bg1[0] <= 290);
                assertTrue(name + " shallow water brightness", hsv(p.get("tnBlobA") | 0xFF000000)[2] <= 0.85);
                assertTrue(name + " accent saturation " + accent[1], accent[1] >= 0.55 && accent[1] <= 0.85);
                double apart = Math.abs(accent[0] - bg1[0]);
                assertTrue(name + " accent hue apart from the water", Math.min(apart, 360 - apart) >= 60);
            } else if (name.startsWith("Look.Sorot")) {
                // A spotlight on the app: a near-black, almost neutral stage and a tinted-white ring.
                assertTrue(name + " stage brightness", bg1[2] <= 0.12);
                assertTrue(name + " stage saturation " + bg1[1], bg1[1] <= 0.45);
                assertTrue(name + " upper stage brightness", bg2[2] <= 0.22);
                assertTrue(name + " ring saturation " + accent[1], accent[1] <= 0.20);
                assertTrue(name + " ring brightness", accent[2] >= 0.92);
            } else {
                // Tenang, soothing. Light palette: soft morning. Dark palettes: low stimulation.
                boolean light = bg1[2] >= 0.5;
                if (light) {
                    assertTrue(name + " light brightness", bg1[2] >= 0.85);
                    assertTrue(name + " light saturation", bg1[1] <= 0.15);
                } else {
                    assertTrue(name + " brightness", bg1[2] <= 0.35);
                    assertTrue(name + " saturation " + bg1[1], bg1[1] <= 0.70);
                    assertTrue(name + " pastel accent " + accent[1], accent[1] <= 0.50);
                    assertTrue(name + " text not glaring " + contrast, contrast <= 14.0);
                }
                assertTrue(name + " text readable", contrast >= 4.5);
            }
        }
    }

    /** {hue 0..360, saturation 0..1, brightness 0..1} of an opaque colour. */
    private static double[] hsv(int c) {
        double r = ((c >> 16) & 0xFF) / 255.0, g = ((c >> 8) & 0xFF) / 255.0, b = (c & 0xFF) / 255.0;
        double max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b)), d = max - min, h;
        if (d == 0) h = 0;
        else if (max == r) h = 60 * (((g - b) / d) % 6);
        else if (max == g) h = 60 * ((b - r) / d + 2);
        else h = 60 * ((r - g) / d + 4);
        if (h < 0) h += 360;
        return new double[]{h, max == 0 ? 0 : d / max, max};
    }

    private static void assertContrast(String what, int fg, int bg, double min) {
        double ratio = contrast(fg, bg);
        assertTrue(what + String.format(" contrast %.2f < %.1f", ratio, min), ratio >= min);
    }

    /** Alpha-composites a translucent colour over an opaque one. */
    private static int over(int top, int bottom) {
        float a = (top >>> 24) / 255f;
        int r = Math.round(((top >> 16) & 0xFF) * a + ((bottom >> 16) & 0xFF) * (1 - a));
        int g = Math.round(((top >> 8) & 0xFF) * a + ((bottom >> 8) & 0xFF) * (1 - a));
        int b = Math.round((top & 0xFF) * a + (bottom & 0xFF) * (1 - a));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static double contrast(int a, int b) {
        double la = luminance(a), lb = luminance(b);
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    private static double luminance(int c) {
        return 0.2126 * channel((c >> 16) & 0xFF) + 0.7152 * channel((c >> 8) & 0xFF) + 0.0722 * channel(c & 0xFF);
    }

    private static double channel(int v) {
        double s = v / 255.0;
        return s <= 0.03928 ? s / 12.92 : Math.pow((s + 0.055) / 1.055, 2.4);
    }

    private static Map<String, Map<String, Integer>> readPalettes() throws Exception {
        File f = new File("src/main/res/values/themes_tenang.xml");
        if (!f.isFile()) f = new File("app/src/main/res/values/themes_tenang.xml");
        String xml = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
        Map<String, Map<String, Integer>> out = new HashMap<>();
        Matcher style = Pattern.compile("<style name=\"ThemeOverlay\\.((?:Tenang|Look)\\.[\\w.]+)\"[^>]*>(.*?)</style>", Pattern.DOTALL).matcher(xml);
        Pattern item = Pattern.compile("<item name=\"(tn\\w+)\">#([0-9A-Fa-f]{6,8})</item>");
        while (style.find()) {
            Map<String, Integer> tokens = new HashMap<>();
            Matcher m = item.matcher(style.group(2));
            while (m.find()) {
                String hex = m.group(2);
                long v = Long.parseLong(hex, 16);
                tokens.put(m.group(1), (int) (hex.length() == 6 ? 0xFF000000L | v : v));
            }
            out.put(style.group(1), tokens);
        }
        return out;
    }
}
