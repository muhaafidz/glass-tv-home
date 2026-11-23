package com.gathin.tvhome;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextClock;

/**
 * A clock that leaves room for its last digit to lean.
 *
 * <p>Lautan slants the display type with {@code Paint.setTextSkewX}. The slant moves the top of
 * the last digit to the right, but the text is laid out at its un-slanted advance, so the right
 * edge of that digit is cut off at the layout's width. Padding cannot fix this: it takes its space
 * out of the same layout width it is meant to protect.
 *
 * <p>This clock measures itself normally, then measures again with the extra room it was given, so
 * the text layout itself is wider and the slanted ink stays inside it. The extra room is the same
 * on every measure, so there is no growing width from layout pass to layout pass.
 */
public final class SkewClock extends TextClock {

    /** Extra pixels to the right of the text, set by the look that slants the clock. */
    private int overhang;

    public SkewClock(Context context) {
        super(context);
    }

    public SkewClock(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SkewClock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Reserves room for the slanted ink past the last digit.
     *
     * @param pixels the overhang in pixels; 0 for an upright clock
     */
    void setOverhang(int pixels) {
        int next = Math.max(0, pixels);
        if (next == overhang) return;
        overhang = next;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (overhang <= 0) return;
        int natural = getMeasuredWidth();
        int roomy = natural + overhang;
        // Never grow past the room the parent offered (a very long time on a very narrow box).
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
            roomy = Math.min(roomy, MeasureSpec.getSize(widthMeasureSpec));
        }
        if (roomy <= natural) return;
        // The second pass rebuilds the text layout at the wider size; the first pass alone would
        // keep the old layout and still cut the slant.
        super.onMeasure(MeasureSpec.makeMeasureSpec(roomy, MeasureSpec.EXACTLY), heightMeasureSpec);
    }
}
