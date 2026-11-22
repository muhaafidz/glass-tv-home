package com.gathin.tvhome;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

/**
 * Home component: the MET warning chip (views weatherWarningPill, weatherWarningIcon, weatherWarningText).
 * Shows a title with a slowly breathing dot (3.2 s a cycle), or hides. Which alert, and whether it was
 * dismissed, is decided by MainActivity.
 */
final class WarningChip {

    private final View pill, dot;
    private final TextView text;
    private ObjectAnimator breathing;

    WarningChip(View root) {
        pill = root.findViewById(R.id.weatherWarningPill);
        dot = root.findViewById(R.id.weatherWarningIcon);
        text = root.findViewById(R.id.weatherWarningText);
    }

    boolean exists() {
        return pill != null && text != null;
    }

    View view() {
        return pill;
    }

    boolean isShowing() {
        return pill != null && pill.getVisibility() == View.VISIBLE;
    }

    void show(CharSequence title, boolean animate) {
        if (!exists()) return;
        text.setText(title);
        text.setSelected(true);   // marquee when it is too long
        pill.setVisibility(View.VISIBLE);
        if (animate) startBreathing();
        else stopBreathing();
    }

    void hide() {
        if (pill != null) pill.setVisibility(View.GONE);
        stopBreathing();
    }

    /** Stops the dot (another app in front, or Home closing); show() starts it again. */
    void pause() {
        stopBreathing();
    }

    private void startBreathing() {
        if (dot == null) return;
        stopBreathing();
        breathing = ObjectAnimator.ofPropertyValuesHolder(dot,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 0.8f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 0.8f),
                PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, 0.35f));
        breathing.setDuration(1600);
        breathing.setRepeatMode(ValueAnimator.REVERSE);
        breathing.setRepeatCount(ValueAnimator.INFINITE);
        breathing.setInterpolator(new AccelerateDecelerateInterpolator());
        breathing.start();
    }

    private void stopBreathing() {
        if (breathing != null) {
            breathing.cancel();
            breathing = null;
        }
        if (dot != null) {
            dot.setScaleX(1f);
            dot.setScaleY(1f);
            dot.setAlpha(1f);
        }
    }
}
