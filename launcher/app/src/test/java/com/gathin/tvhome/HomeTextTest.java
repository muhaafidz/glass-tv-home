package com.gathin.tvhome;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Tenang home words and numbers (HomeText). */
public class HomeTextTest {

    private static final long MIN = 60_000L, DAY = 24 * 60 * MIN;
    // Subuh 05:48, Syuruk 06:55, Zohor 13:00, Asar 16:20, Maghrib 18:58, Isyak 20:10 on a day starting at 0.
    private static final long[] T = {348 * MIN, 415 * MIN, 780 * MIN, 980 * MIN, 1138 * MIN, 1210 * MIN};

    @Test
    public void greetingsByHour() {
        assertEquals("Selamat pagi", HomeText.greeting("ms", 7));
        assertEquals("Selamat tengah hari", HomeText.greeting("ms", 13));
        assertEquals("Selamat petang", HomeText.greeting("ms", 17));
        assertEquals("Selamat malam", HomeText.greeting("ms", 0));
        assertEquals("Selamat malam", HomeText.greeting("ms", 21));
        assertEquals("Good morning", HomeText.greeting("en", 9));
        assertEquals("Good evening", HomeText.greeting("en", 19));
        assertEquals("Good night", HomeText.greeting("en", 2));
        assertEquals("Selamat siang", HomeText.greeting("id", 12));
        assertEquals("Selamat sore", HomeText.greeting("id", 16));
    }

    @Test
    public void dateInEachLanguage() {
        assertEquals("16 Nov 2026", HomeText.date("ms", 16, 10, 2026));
        assertEquals("1 Ogo 2026", HomeText.date("ms", 1, 7, 2026));
        assertEquals("1 Aug 2026", HomeText.date("en", 1, 7, 2026));
        assertEquals("1 Agu 2026", HomeText.date("id", 1, 7, 2026));
    }

    @Test
    public void durations() {
        assertEquals("5j 36m", HomeText.duration("ms", 336));
        assertEquals("5h 36m", HomeText.duration("en", 336));
        assertEquals("36m", HomeText.duration("id", 36));
        assertEquals("0m", HomeText.duration("ms", -3));
        assertEquals("Subuh dalam 5j 36m", HomeText.nextIn("ms", "Subuh", 336));
        assertEquals("Fajr in 1h 0m", HomeText.nextIn("en", "Fajr", 60));
    }

    @Test
    public void windowAfterIsyakRunsToTomorrowsSubuh() {
        long now = 1300 * MIN;   // 21:40
        assertArrayEquals(new long[]{5, 0, T[5], T[0] + DAY}, HomeText.window(T, now));
    }

    @Test
    public void windowBeforeSubuhStartsAtYesterdaysIsyak() {
        long now = 120 * MIN;    // 02:00
        assertArrayEquals(new long[]{5, 0, T[5] - DAY, T[0]}, HomeText.window(T, now));
    }

    @Test
    public void windowSkipsSyuruk() {
        long now = 420 * MIN;    // 07:00, after Syuruk: previous is still Subuh, next is Zohor
        assertArrayEquals(new long[]{0, 2, T[0], T[2]}, HomeText.window(T, now));
        long early = 400 * MIN;  // 06:40, before Syuruk: next is Zohor, not Syuruk
        assertArrayEquals(new long[]{0, 2, T[0], T[2]}, HomeText.window(T, early));
    }

    @Test
    public void progressIsClamped() {
        assertEquals(0.5f, HomeText.progress(0, 100, 50), 1e-6);
        assertEquals(0f, HomeText.progress(0, 100, -5), 1e-6);
        assertEquals(1f, HomeText.progress(0, 100, 500), 1e-6);
        assertEquals(0f, HomeText.progress(100, 100, 100), 1e-6);
    }

    @Test
    public void fontCuts() {
        assertEquals(200, Fonts.nearestCut(100));
        assertEquals(400, Fonts.nearestCut(400));
        assertEquals(500, Fonts.nearestCut(600));
        assertEquals(700, Fonts.nearestCut(650));
    }
}
