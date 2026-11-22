package com.gathin.tvhome;

/**
 * Words and numbers for the Tenang home screen: greeting, date, prayer countdown. Pure functions,
 * so they are unit-tested on the JVM (HomeTextTest). Languages follow AppLanguage: ms, en, id.
 */
final class HomeText {

    private static final long DAY_MS = 24L * 60 * 60 * 1000;
    /** Prayer index of Syuruk: a time, not a prayer, so never the "next" or "previous" one. */
    private static final int SYURUK = 1;

    private HomeText() {}

    /** "Selamat pagi", "Good evening", "Selamat siang"... for an hour 0..23. */
    static String greeting(String lang, int hour) {
        if (AppLanguage.LANG_EN.equals(lang)) {
            if (hour >= 5 && hour < 12) return "Good morning";
            if (hour >= 12 && hour < 18) return "Good afternoon";
            if (hour >= 18 && hour < 22) return "Good evening";
            return "Good night";
        }
        if (AppLanguage.LANG_ID.equals(lang)) {
            if (hour >= 4 && hour < 11) return "Selamat pagi";
            if (hour >= 11 && hour < 15) return "Selamat siang";
            if (hour >= 15 && hour < 18) return "Selamat sore";
            return "Selamat malam";
        }
        if (hour >= 5 && hour < 12) return "Selamat pagi";
        if (hour >= 12 && hour < 14) return "Selamat tengah hari";
        if (hour >= 14 && hour < 19) return "Selamat petang";
        return "Selamat malam";
    }

    /**
     * The greeting, with special days first: Raya (Syawal 1-3), Aidiladha (Zulhijjah 10-12), Ramadan, then
     * Friday; otherwise the time-of-day greeting. dayOfWeek is Calendar.DAY_OF_WEEK (Friday = 6); hijriMonth 0 means unknown.
     */
    static String greeting(String lang, int hour, int dayOfWeek, int hijriMonth, int hijriDay) {
        boolean en = AppLanguage.LANG_EN.equals(lang), id = AppLanguage.LANG_ID.equals(lang);
        if (hijriMonth == 10 && hijriDay >= 1 && hijriDay <= 3) return en ? "Eid Mubarak" : id ? "Selamat Idul Fitri" : "Selamat Hari Raya";
        if (hijriMonth == 12 && hijriDay >= 10 && hijriDay <= 12) return en ? "Eid al-Adha Mubarak" : id ? "Selamat Idul Adha" : "Selamat Hari Raya Haji";
        if (hijriMonth == 9) return en ? "Ramadan Kareem" : "Selamat berpuasa";
        if (dayOfWeek == 6 && hour >= 5 && hour < 19) return en ? "Jumu'ah Mubarak" : id ? "Jumat Berkah" : "Jumaat Mubarak";
        return greeting(lang, hour);
    }

    /** "Selamat petang, Hafiz": the profile's name after the greeting, or the greeting alone when there is no name. */
    static String withName(String greeting, String name) {
        String n = name == null ? "" : name.trim();
        if (n.isEmpty()) return greeting;
        if (n.length() > 14) n = n.substring(0, 13) + "\u2026";
        return greeting + ", " + n;
    }

    /**
     * The prayer that arrived within the last {@code windowMs} (Subuh, Zohor, Asar, Maghrib, Isyak; Syuruk is a
     * time, not a prayer), as its index, or -1. Home pulses that prayer's cell once.
     */
    static int arrivedPrayer(long[] times, long now, long windowMs) {
        if (times == null || times.length < 6) return -1;
        for (int i = 0; i < 6; i++) {
            if (i == SYURUK) continue;
            if (now >= times[i] && now - times[i] < windowMs) return i;
        }
        return -1;
    }

    /** "16 Nov 2026" with the month in the language's short form. */
    static String date(String lang, int day, int month, int year) {
        String[] m;
        if (AppLanguage.LANG_EN.equals(lang)) {
            m = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        } else if (AppLanguage.LANG_ID.equals(lang)) {
            m = new String[]{"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};
        } else {
            m = new String[]{"Jan", "Feb", "Mac", "Apr", "Mei", "Jun", "Jul", "Ogo", "Sep", "Okt", "Nov", "Dis"};
        }
        return day + " " + (month >= 0 && month < 12 ? m[month] : "") + " " + year;
    }

    /** "5j 36m" (ms, id) or "5h 36m" (en); under an hour just "36m". */
    static String duration(String lang, long minutes) {
        if (minutes < 0) minutes = 0;
        long h = minutes / 60, m = minutes % 60;
        String hour = AppLanguage.LANG_EN.equals(lang) ? "h" : "j";
        return h > 0 ? h + hour + " " + m + "m" : m + "m";
    }

    /** "Subuh dalam 5j 36m" / "Subuh in 5h 36m". */
    static String nextIn(String lang, String name, long minutes) {
        String word = AppLanguage.LANG_EN.equals(lang) ? " in " : " dalam ";
        return name + word + duration(lang, minutes);
    }

    /**
     * The prayer window around now: {previous index, next index, previous ms, next ms}. Syuruk is
     * skipped. Before Subuh the previous one is yesterday's Isyak; after Isyak the next one is
     * tomorrow's Subuh (both estimated from today's times, close enough for a progress bar).
     */
    static long[] window(long[] times, long now) {
        int next = -1;
        for (int i = 0; i < 6; i++) {
            if (i == SYURUK) continue;
            if (times[i] > now) { next = i; break; }
        }
        long nextMs;
        if (next == -1) {
            next = 0;
            nextMs = times[0] + DAY_MS;
        } else {
            nextMs = times[next];
        }
        int prev = -1;
        for (int i = 5; i >= 0; i--) {
            if (i == SYURUK) continue;
            if (times[i] <= now) { prev = i; break; }
        }
        long prevMs;
        if (prev == -1) {
            prev = 5;
            prevMs = times[5] - DAY_MS;
        } else {
            prevMs = times[prev];
        }
        return new long[]{prev, next, prevMs, nextMs};
    }

    /** How far now is from the previous prayer to the next, 0..1. */
    static float progress(long prevMs, long nextMs, long now) {
        if (nextMs <= prevMs) return 0f;
        float f = (now - prevMs) / (float) (nextMs - prevMs);
        return f < 0f ? 0f : (f > 1f ? 1f : f);
    }
}
