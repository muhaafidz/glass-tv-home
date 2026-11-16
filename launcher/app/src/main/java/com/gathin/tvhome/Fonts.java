package com.gathin.tvhome;

import android.graphics.Typeface;
import android.os.Build;
import android.widget.TextView;

/**
 * Outfit weights for code that set a variable-font weight. The bundled Outfit is a family of
 * static cuts (res/font/outfit.xml: 200, 300, 400, 500, 700), so a "'wght' N" setting is mapped to the
 * nearest cut instead: 550 and 600 become 500, 650 and up become 700.
 */
final class Fonts {

    private Fonts() {}

    /** Drop-in for TextView.setFontVariationSettings("'wght' N"). */
    static void variation(TextView view, String settings) {
        if (view == null || settings == null) return;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("'wght'\\s+(\\d+)").matcher(settings);
        if (m.find()) weight(view, Integer.parseInt(m.group(1)));
    }

    static int nearestCut(int weight) {
        if (weight < 250) return 200;
        if (weight < 350) return 300;
        if (weight < 450) return 400;
        if (weight < 650) return 500;
        return 700;
    }

    /** Sets the Outfit cut nearest to weight; on Android 8 (no weight API) only bold or regular. */
    static void weight(TextView view, int weight) {
        if (view == null) return;
        int cut = nearestCut(weight);
        Typeface base = view.getTypeface();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            view.setTypeface(Typeface.create(base, cut, false));
        } else {
            view.setTypeface(base, cut >= 700 ? Typeface.BOLD : Typeface.NORMAL);
        }
    }
}
