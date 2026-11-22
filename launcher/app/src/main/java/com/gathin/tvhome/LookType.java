package com.gathin.tvhome;

import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

/**
 * Look typography (docs/LAYOUT-RESEARCH.md): Lautan slants the clock, period, date and greeting and sets
 * them in Outfit Bold; Tenang and Sorot keep the layout's type. Paint-level, so it costs nothing per frame.
 */
final class LookType {

    private LookType() {}

    static void apply(View home) {
        if (Design.TEXT_SKEW == 0f) return;
        Typeface bold = home.getResources().getFont(R.font.outfit_bold);
        for (int id : new int[]{R.id.clock, R.id.ampm, R.id.date, R.id.greeting}) {
            View v = home.findViewById(id);
            if (!(v instanceof TextView)) continue;
            TextView t = (TextView) v;
            t.setTypeface(bold);
            t.getPaint().setTextSkewX(Design.TEXT_SKEW);
            if (id == R.id.clock) t.setLetterSpacing(-0.04f);
            t.invalidate();
        }
    }
}
