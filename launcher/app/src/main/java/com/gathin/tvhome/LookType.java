package com.gathin.tvhome;

import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Small layout and type helpers the Arrangements are written with. {@link #apply} runs the current look's
 * arrangement on an inflated Home (MainActivity and the layout snapshots both call it).
 */
final class LookType {

    private LookType() {}

    static void apply(View home) {
        Arrangement.current().arrange(home);
    }

    /** Lautan's display type: the clock, period, date and greeting slanted, in Outfit Bold. */
    static void slantedDisplayType(View home) {
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

    static void smallClock(View home, float sp) {
        View v = home.findViewById(R.id.clock);
        if (!(v instanceof TextView)) return;
        ((TextView) v).setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        ((TextView) v).setTypeface(home.getResources().getFont(R.font.outfit_medium));
    }

    /** Moves views in the top RelativeLayout from the start edge to the end edge. */
    static void toEnd(View home, int... ids) {
        for (int id : ids) {
            View v = home.findViewById(id);
            if (v == null || !(v.getLayoutParams() instanceof RelativeLayout.LayoutParams)) continue;
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) v.getLayoutParams();
            lp.removeRule(RelativeLayout.ALIGN_PARENT_START);
            lp.removeRule(RelativeLayout.START_OF);
            lp.addRule(RelativeLayout.ALIGN_PARENT_END);
            v.setLayoutParams(lp);
        }
    }

    /** The rail's cards stacked (prayer times first, then the ayah), each full width. */
    static void stackRail(View home) {
        View shelf = home.findViewById(R.id.islamicShelf);
        if (!(shelf instanceof LinearLayout)) return;
        LinearLayout row = (LinearLayout) shelf;
        row.setOrientation(LinearLayout.VERTICAL);
        View ayah = home.findViewById(R.id.ayahColumn);
        if (ayah != null && ayah.getParent() == row) {
            row.removeView(ayah);
            row.addView(ayah);
        }
        float density = home.getResources().getDisplayMetrics().density;
        for (int i = 0; i < row.getChildCount(); i++) {
            View c = row.getChildAt(i);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) c.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.weight = 0;
            if (c.getId() == R.id.infoDivider) lp.height = Math.round(14 * density);
            c.setLayoutParams(lp);
        }
    }

    /** The information rail as a right-hand column, a share of the TV's long side wide. */
    static void rightColumn(View home, float share) {
        View shelf = home.findViewById(R.id.islamicShelf);
        if (shelf == null || !(shelf.getLayoutParams() instanceof FrameLayout.LayoutParams)) return;
        DisplayMetrics dm = home.getResources().getDisplayMetrics();
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) shelf.getLayoutParams();
        lp.width = Math.round(Math.max(dm.widthPixels, dm.heightPixels) * share);
        lp.gravity = Gravity.END | Gravity.TOP;
        lp.setMarginStart(0);
        lp.setMarginEnd(Math.round(56 * dm.density));
        shelf.setLayoutParams(lp);
    }

    static void maxLines(View home, int id, int lines) {
        View v = home.findViewById(id);
        if (v instanceof TextView) ((TextView) v).setMaxLines(lines);
    }

    static void maxWidth(View home, int id, int dp) {
        View v = home.findViewById(id);
        if (v instanceof TextView) ((TextView) v).setMaxWidth(Math.round(dp * home.getResources().getDisplayMetrics().density));
    }

    static void gone(View home, int id) {
        View v = home.findViewById(id);
        if (v != null) v.setVisibility(View.GONE);
    }

    static void visible(View home, int id) {
        View v = home.findViewById(id);
        if (v != null) v.setVisibility(View.VISIBLE);
    }
}
