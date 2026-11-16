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
    public void autoNeverPicksEmbunUntilEveryScreenIsMigrated() {
        assertEquals(false, Tenang.EMBUN_IN_AUTO);
    }

    @Test
    public void palettesMeetTextContrast() throws Exception {
        Map<String, Map<String, Integer>> palettes = readPalettes();
        assertEquals(3, palettes.size());
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
        }
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
        Matcher style = Pattern.compile("<style name=\"ThemeOverlay\\.Tenang\\.(\\w+)\"[^>]*>(.*?)</style>", Pattern.DOTALL).matcher(xml);
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
