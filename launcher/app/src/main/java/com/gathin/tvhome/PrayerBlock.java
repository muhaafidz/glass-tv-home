package com.gathin.tvhome;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

/**
 * Home component (docs/LAYOUT-RESEARCH.md §3): the prayer-times block. Binds to the views with the
 * standard IDs in whatever layout a look uses (prayerHeader, lblSubuh…lblIsyak and timeSubuh…timeIsyak,
 * prayerPrev, prayerBarFill, prayerNextIn); any that a layout leaves out are skipped.
 * It only shows what it is given; MainActivity reads PrayerTimes and passes the result in.
 */
final class PrayerBlock {

    private static final int[] LABEL_IDS = {R.id.lblSubuh, R.id.lblSyuruk, R.id.lblZohor, R.id.lblAsar, R.id.lblMaghrib, R.id.lblIsyak};
    private static final int[] TIME_IDS = {R.id.timeSubuh, R.id.timeSyuruk, R.id.timeZohor, R.id.timeAsar, R.id.timeMaghrib, R.id.timeIsyak};

    private final TextView header, prev, nextIn;
    private final View barFill;
    private final TextView[] labels = new TextView[6], times = new TextView[6];
    private final Typeface font;
    private View nextCell;
    private ObjectAnimator glow;

    PrayerBlock(View root) {
        header = root.findViewById(R.id.prayerHeader);
        prev = root.findViewById(R.id.prayerPrev);
        nextIn = root.findViewById(R.id.prayerNextIn);
        barFill = root.findViewById(R.id.prayerBarFill);
        for (int i = 0; i < 6; i++) {
            labels[i] = root.findViewById(LABEL_IDS[i]);
            times[i] = root.findViewById(TIME_IDS[i]);
        }
        font = root.getResources().getFont(R.font.outfit);
    }

    void setHeader(CharSequence text) {
        if (header != null) header.setText(text);
    }

    /** The six prayers; the next one is outlined and, with animate, breathes slowly. */
    void setPrayers(PrayerTimes.PrayerItem[] items, boolean animate) {
        if (items == null || items.length < 6) return;
        View next = null;
        for (int i = 0; i < 6; i++) {
            if (times[i] != null) times[i].setText(items[i].timeStr);
            if (labels[i] == null) continue;
            labels[i].setText(items[i].name);
            boolean isNext = items[i].isNext;
            labels[i].setTextColor(isNext ? Design.ACCENT : Design.TEXT_MUTED);
            labels[i].setTypeface(font);
            Fonts.weight(labels[i], isNext ? 500 : 400);
            if (times[i] != null) {
                times[i].setTextColor(Design.TEXT);
                times[i].setTypeface(font);
                Fonts.weight(times[i], isNext ? 400 : 300);
            }
            View cell = (View) labels[i].getParent();
            if (isNext) next = cell;
            else cell.setBackground(null);
        }
        showNext(next, animate);
    }

    /** "Isyak ━━━ Subuh dalam 5j 36m". */
    void setCountdown(CharSequence previous, CharSequence nextInText, float progress) {
        if (prev != null) prev.setText(previous);
        if (nextIn != null) nextIn.setText(nextInText);
        if (barFill != null) barFill.setScaleX(progress);
    }

    /**
     * The moment a prayer arrives: its cell lifts to 1.12x and an accent outline fades in and out over 2.2 s
     * (scale and alpha only, once), then settles; the cell keeps its outline only if it is the next prayer.
     */
    void pulse(int index) {
        if (index < 0 || index > 5 || labels[index] == null) return;
        final View cell = (View) labels[index].getParent();
        final Drawable outline = cell.getContext().getDrawable(R.drawable.tn_prayer_next).mutate();
        final View keep = nextCell;
        cell.setBackground(outline);
        outline.setAlpha(0);
        cell.animate().cancel();
        cell.animate().scaleX(1.12f).scaleY(1.12f).setDuration(700)
                .setInterpolator(new android.view.animation.PathInterpolator(0.2f, 0.8f, 0.2f, 1f))
                .withEndAction(() -> cell.animate().scaleX(1f).scaleY(1f).setDuration(1500).start()).start();
        ValueAnimator a = ValueAnimator.ofInt(0, 255, 0);
        a.setDuration(2200);
        a.addUpdateListener(v -> outline.setAlpha((Integer) v.getAnimatedValue()));
        a.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (cell != nextCell && cell != keep) cell.setBackground(null);
            }
        });
        a.start();
    }

    /** Stops the breathing; the next setPrayers puts the outline and its breathing back. */
    void pause() {
        if (glow != null) {
            glow.cancel();
            glow = null;
        }
        nextCell = null;
    }

    private void showNext(View cell, boolean animate) {
        if (cell == nextCell && cell != null && cell.getBackground() != null) return;
        pause();
        nextCell = cell;
        if (cell == null) return;
        Drawable outline = cell.getContext().getDrawable(R.drawable.tn_prayer_next).mutate();
        cell.setBackground(outline);
        if (animate) {
            glow = ObjectAnimator.ofInt(outline, "alpha", 255, 90);
            glow.setDuration(2000);
            glow.setRepeatMode(ValueAnimator.REVERSE);
            glow.setRepeatCount(ValueAnimator.INFINITE);
            glow.setInterpolator(new AccelerateDecelerateInterpolator());
            glow.start();
        }
    }
}
