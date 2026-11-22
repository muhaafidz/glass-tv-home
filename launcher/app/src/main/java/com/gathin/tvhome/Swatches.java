package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

/**
 * Settings → Warna: a row of small colour dots, one per palette of the current look (its background colours and
 * a spot of its accent), the current one ringed, so a palette is chosen by sight. Read from the palette blocks
 * themselves (docs/PALETTES.md), so a new palette shows up with no extra work.
 */
final class Swatches {

    private Swatches() {}

    /** {deep background, upper background, accent} of a palette, straight from its theme overlay. */
    static int[] colours(Context ctx, String look, String palette) {
        int style;
        if (Tenang.LOOK_TENANG.equals(look)) {
            style = Tenang.AUTO.equals(palette) ? 0 : Tenang.overlayFor(palette);
        } else {
            style = Tenang.lookPaletteOverlay(palette);
            if (style == 0) style = Tenang.LOOK_LAUTAN.equals(look) ? R.style.ThemeOverlay_Look_Lautan : R.style.ThemeOverlay_Look_Sorot;
        }
        if (style == 0) {   // Automatik: the day, from morning to night
            int[] morning = colours(ctx, look, Tenang.EMBUN), dusk = colours(ctx, look, Tenang.SENJA), night = colours(ctx, look, Tenang.MALAM);
            return new int[]{morning[1], night[0], dusk[2]};
        }
        android.content.res.TypedArray a = ctx.obtainStyledAttributes(style, new int[]{R.attr.tnBg1, R.attr.tnBg2, R.attr.tnAccent});
        try {
            return new int[]{a.getColor(0, 0xFF000000), a.getColor(1, 0xFF222222), a.getColor(2, 0xFFFFFFFF)};
        } finally {
            a.recycle();
        }
    }

    /** The dots for a look's palettes; {@code current} is ringed. Not focusable: the card above it is the control. */
    static View row(Context ctx, String look, String current, float density) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, Math.round(10 * density), 0, 0);
        for (String palette : Tenang.palettesOf(look)) {
            View dot = new View(ctx);
            dot.setBackground(new Dot(colours(ctx, look, palette), palette.equals(current), density));
            int size = Math.round(32 * density);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.rightMargin = Math.round(12 * density);
            row.addView(dot, lp);
        }
        return row;
    }

    /** A circle shaded from the deep to the upper background, with a spot of accent; ringed when current. */
    private static final class Dot extends Drawable {
        private final int[] c;
        private final boolean current;
        private final float density;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        Dot(int[] colours, boolean current, float density) {
            this.c = colours;
            this.current = current;
            this.density = density;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            float w = getBounds().width(), h = getBounds().height(), r = Math.min(w, h) / 2f - 2 * density;
            float cx = getBounds().exactCenterX(), cy = getBounds().exactCenterY();
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(new LinearGradient(cx, cy - r, cx, cy + r, c[1], c[0], Shader.TileMode.CLAMP));
            canvas.drawCircle(cx, cy, r, paint);
            paint.setShader(null);
            paint.setColor(c[2]);
            canvas.drawCircle(cx + r * 0.30f, cy + r * 0.30f, r * 0.42f, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth((current ? 2.5f : 1f) * density);
            paint.setColor(current ? Design.TEXT : Design.alpha(Design.TEXT, 0x70));
            canvas.drawCircle(cx, cy, r + (current ? 2 : 0) * density, paint);
        }

        @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
        @Override public void setColorFilter(ColorFilter cf) { paint.setColorFilter(cf); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }
}
