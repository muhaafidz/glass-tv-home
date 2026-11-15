package com.gathin.tvhome;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;

/** A flat Sinar menu surface, with the focus shape painted over it when a menu needs one. */
final class SinarPanelDrawable extends Drawable {

    private final View owner;
    private final float radius;
    private final float density;
    private final Glider highlight;
    private final boolean paintsShape;
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edge = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final int[] at = new int[2], highlightAt = new int[2];
    private float alpha = 1f;

    SinarPanelDrawable(View owner, float radius, float density, Glider highlight, boolean paintsShape) {
        this.owner = owner;
        this.radius = radius;
        this.density = density;
        this.highlight = highlight;
        this.paintsShape = paintsShape;
        edge.setStyle(Paint.Style.STROKE);
        edge.setStrokeWidth(1f * density);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        rect.set(getBounds());
        fill.setColor((Math.round(0xFF * alpha) << 24) | (Design.SURFACE & 0x00FFFFFF));
        canvas.drawRoundRect(rect, radius, radius, fill);
        edge.setColor((Math.round(0xFF * alpha) << 24) | (Design.HAIRLINE & 0x00FFFFFF));
        canvas.drawRoundRect(rect, radius, radius, edge);
        if (paintsShape && highlight != null && highlight.isShowing()) {
            owner.getLocationInWindow(at);
            highlight.getLocationInWindow(highlightAt);
            canvas.save();
            canvas.clipRect(getBounds());
            highlight.drawShape(canvas, highlightAt[0] - at[0], highlightAt[1] - at[1], owner);
            canvas.restore();
        }
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha / 255f;
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
