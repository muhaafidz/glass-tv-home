package com.gathin.tvhome;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * A horizontal row whose visible children all take the tallest one's height, so the Tenang cards
 * side by side (ayah and prayer times) line up. One extra measure pass; nothing else.
 */
public class EqualHeightRow extends LinearLayout {

    public EqualHeightRow(Context context) {
        super(context);
    }

    public EqualHeightRow(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int tallest = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View c = getChildAt(i);
            if (c.getVisibility() != GONE) tallest = Math.max(tallest, c.getMeasuredHeight());
        }
        if (tallest == 0) return;
        boolean changed = false;
        for (int i = 0; i < getChildCount(); i++) {
            View c = getChildAt(i);
            if (c.getVisibility() == GONE || c.getMeasuredHeight() == tallest || c.getMeasuredHeight() <= 1) continue;
            c.measure(MeasureSpec.makeMeasureSpec(c.getMeasuredWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(tallest, MeasureSpec.EXACTLY));
            changed = true;
        }
        if (changed || getMeasuredHeight() < tallest + getPaddingTop() + getPaddingBottom()) {
            setMeasuredDimension(getMeasuredWidth(), tallest + getPaddingTop() + getPaddingBottom());
        }
    }
}
