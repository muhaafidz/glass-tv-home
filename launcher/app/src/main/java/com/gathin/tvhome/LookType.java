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
        if (Design.BACKDROP == 2) {
            // Sorot: a small clock, no greeting; the app row and its hero take the space.
            TextView clock = home.findViewById(R.id.clock);
            if (clock != null) {
                clock.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 34);
                clock.setTypeface(home.getResources().getFont(R.font.outfit_medium));
            }
            View greet = home.findViewById(R.id.greeting);
            if (greet != null) greet.setVisibility(View.GONE);
            TextView ayah = home.findViewById(R.id.ayahText);
            if (ayah != null) ayah.setMaxLines(2);   // shorter cards along the bottom
            View hero = home.findViewById(R.id.sorotHero);
            if (hero != null) hero.setVisibility(View.VISIBLE);
            // Islamic content at the top or on the right, never along the bottom: the prayer times become a
            // strip under the chips, and the ayah card sits on the right beside the hero.
            View strip = home.findViewById(R.id.prayerStrip);
            if (strip != null) strip.setVisibility(View.VISIBLE);
            gone(home, R.id.prayerBlock);
            gone(home, R.id.infoDivider);
            rightColumn(home, 0.42f);
            return;
        }
        if (Design.TEXT_SKEW == 0f) return;
        if (Design.BACKDROP == 1) lautanColumns(home);
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

    /**
     * Lautan: the list owns the left; the clock, date and chips move to the right, the top buttons give way
     * to the list and menu, and the ayah and prayer cards stack in a right column (prayer times first).
     */
    private static void lautanColumns(View home) {
        for (int id : new int[]{R.id.leftBlock, R.id.weatherRow, R.id.nowPlayingCard}) {
            View v = home.findViewById(id);
            if (v == null || !(v.getLayoutParams() instanceof android.widget.RelativeLayout.LayoutParams)) continue;
            android.widget.RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) v.getLayoutParams();
            lp.removeRule(android.widget.RelativeLayout.ALIGN_PARENT_START);
            lp.removeRule(android.widget.RelativeLayout.START_OF);
            lp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_END);
            v.setLayoutParams(lp);
        }
        gone(home, R.id.topButtons);
        // Keep the chips in the right column: the warning text gets a shorter ceiling.
        View warn = home.findViewById(R.id.weatherWarningText);
        if (warn instanceof TextView) ((TextView) warn).setMaxWidth(Math.round(240 * home.getResources().getDisplayMetrics().density));
        View shelf = home.findViewById(R.id.islamicShelf);
        if (shelf instanceof android.widget.LinearLayout) {
            android.widget.LinearLayout row = (android.widget.LinearLayout) shelf;
            row.setOrientation(android.widget.LinearLayout.VERTICAL);
            View ayah = home.findViewById(R.id.ayahColumn);
            if (ayah != null && ayah.getParent() == row) {   // prayer times first, then the ayah
                row.removeView(ayah);
                row.addView(ayah);
            }
            for (int i = 0; i < row.getChildCount(); i++) {
                View c = row.getChildAt(i);
                android.widget.LinearLayout.LayoutParams lp = (android.widget.LinearLayout.LayoutParams) c.getLayoutParams();
                lp.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
                lp.weight = 0;
                if (c.getId() == R.id.infoDivider) lp.height = Math.round(14 * home.getResources().getDisplayMetrics().density);
                c.setLayoutParams(lp);
            }
        }
        View ayahText = home.findViewById(R.id.ayahText);
        if (ayahText instanceof TextView) ((TextView) ayahText).setMaxLines(2);   // the column must fit a 1080p screen
        rightColumn(home, 0.46f);
    }

    /** The information rail as a right-hand column of the given width share. */
    private static void rightColumn(View home, float share) {
        View shelf = home.findViewById(R.id.islamicShelf);
        if (shelf == null || !(shelf.getLayoutParams() instanceof android.widget.FrameLayout.LayoutParams)) return;
        android.util.DisplayMetrics dm = home.getResources().getDisplayMetrics();
        android.widget.FrameLayout.LayoutParams lp = (android.widget.FrameLayout.LayoutParams) shelf.getLayoutParams();
        lp.width = Math.round(Math.max(dm.widthPixels, dm.heightPixels) * share);   // the TV's long side
        lp.gravity = android.view.Gravity.END | android.view.Gravity.TOP;
        lp.setMarginStart(0);
        lp.setMarginEnd(Math.round(56 * dm.density));
        shelf.setLayoutParams(lp);
    }

    private static void gone(View home, int id) {
        View v = home.findViewById(id);
        if (v != null) v.setVisibility(View.GONE);
    }
}
