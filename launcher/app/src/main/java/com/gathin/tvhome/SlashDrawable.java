package com.gathin.tvhome;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import androidx.annotation.NonNull;

/** Lautan's focus: a slanted white bar with the accent's red edge to its right. */
final class SlashDrawable extends Drawable {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float density;

    SlashDrawable(float density) {
        this.density = density;
    }

    /** Clear normally, the slash when focused. */
    static Drawable states(float density) {
        StateListDrawable s = new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_focused}, new SlashDrawable(density));
        s.addState(new int[]{}, new ColorDrawable(0));
        return s;
    }

    @Override
    public void draw(@NonNull Canvas c) {
        Rect b = getBounds();
        c.save();
        c.skew(-0.21f, 0f);
        float shift = (b.top + b.height() / 2f) * 0.21f;   // undo the skew at the bar's middle
        float edge = 8 * density, top = 3 * density;
        paint.setColor(Design.ACCENT);
        c.drawRect(b.left + shift + edge, b.top + top, b.right + shift + edge, b.bottom, paint);
        paint.setColor(0xFFFFFFFF);
        c.drawRect(b.left + shift, b.top + top, b.right + shift, b.bottom, paint);
        c.restore();
    }

    @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
    @Override public void setColorFilter(ColorFilter cf) { paint.setColorFilter(cf); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
