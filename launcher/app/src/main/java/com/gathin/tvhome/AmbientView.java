package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.FrameLayout;

/**
 * The Tenang background (docs/REVAMP.md §4.3): the palette's gradient, two soft glows drifting
 * over 38 s and 46 s, a few stars and a slow wave line.
 *
 * Cheap on a 1 GB TV:
 * - No bitmaps and no hardware layers: the glows are radial GradientDrawables, the waves one Path.
 * - Only translation, scale and alpha change, so each step replays display lists; nothing re-lays out.
 * - It steps at 15 fps, not 60: the drift is a few pixels a second, so 15 steps look smooth and the
 *   GPU repaints the screen a quarter as often.
 * - start()/stop() follow Home's focus; with motion reduced it draws once and never steps.
 */
final class AmbientView extends FrameLayout {

    private static final long STEP_MS = 66;   // ~15 fps
    private static final long DRIFT_A_MS = 38_000, DRIFT_B_MS = 46_000, WAVE_MS = 70_000;
    private static final long TWINKLE_A_MS = 5_000, TWINKLE_B_MS = 7_000;

    /** Star positions as fractions of the screen, from the mockup; every other one twinkles. */
    private static final float[][] STARS = {
            {.46f, .07f}, {.52f, .15f}, {.58f, .06f}, {.63f, .13f}, {.41f, .16f}, {.69f, .04f},
            {.36f, .05f}, {.74f, .16f}, {.30f, .24f}, {.88f, .27f}, {.07f, .31f}, {.55f, .23f},
    };

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final View blobA, blobB, waves;
    private final View[] twinklers;
    private final float density;
    private boolean running, still;
    private long startedAt;

    private final Runnable step = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            apply(SystemClock.uptimeMillis() - startedAt);
            handler.postDelayed(this, STEP_MS);
        }
    };

    AmbientView(Context ctx) {
        super(ctx);
        density = getResources().getDisplayMetrics().density;
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        setFocusable(false);
        setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{Design.BG1, Design.BG2}));

        blobA = blob(Design.BLOB_A);
        blobB = blob(Design.BLOB_B);
        addView(blobA);
        addView(blobB);

        View starField = new StarField(ctx);
        starField.setAlpha(Design.STARS);
        addView(starField, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        twinklers = ((StarField) starField).twinklers;
        for (View t : twinklers) addView(t);

        waves = new Waves(ctx);
        addView(waves);
        setClipChildren(true);
    }

    private View blob(int color) {
        View v = new View(getContext());
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        g.setColors(new int[]{color, color & 0x00FFFFFF});
        v.setBackground(g);
        return v;
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        // Sizes and places from the mockup's 1600×900 stage, scaled to the screen.
        float k = w / 1600f;
        place(blobA, 980 * k, -260 * k, -380 * k);
        place(blobB, 1100 * k, w - (1100 - 380) * k, h - (1100 - 560) * k);
        LayoutParams wl = (LayoutParams) waves.getLayoutParams();
        wl.width = w * 2;
        wl.height = Math.round(200 * k);
        wl.topMargin = h - Math.round((120 + 200) * k);
        waves.setLayoutParams(wl);
        for (int i = 0, t = 0; i < STARS.length; i++) {
            if (i % 2 != 0) continue;
            View s = twinklers[t++];
            LayoutParams sl = (LayoutParams) s.getLayoutParams();
            sl.leftMargin = Math.round(STARS[i][0] * w);
            sl.topMargin = Math.round(STARS[i][1] * h);
            s.setLayoutParams(sl);
        }
        apply(running ? SystemClock.uptimeMillis() - startedAt : 0);
    }

    /** Home position by margins (negative is fine), so translation is free for the drift. */
    private void place(View v, float size, float x, float y) {
        int s = Math.round(size);
        LayoutParams lp = new LayoutParams(s, s);
        lp.leftMargin = Math.round(x);
        lp.topMargin = Math.round(y);
        v.setLayoutParams(lp);
        ((GradientDrawable) v.getBackground()).setGradientRadius(s / 2f);
        v.setTag(Boolean.TRUE);
    }

    /** Starts the drift; with still=true (reduced motion, low-spec) it only draws once. */
    void start(boolean still) {
        this.still = still;
        if (still) {
            stop();
            return;
        }
        if (running) return;
        running = true;
        if (startedAt == 0) startedAt = SystemClock.uptimeMillis();
        handler.post(step);
    }

    void stop() {
        running = false;
        handler.removeCallbacks(step);
    }

    boolean isRunning() {
        return running;
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }

    /** Positions everything for a time; pure arithmetic on the clock, so a pause resumes where it was. */
    private void apply(long t) {
        int w = getWidth();
        if (w == 0) return;
        float k = w / 1600f;
        float a = ease(pingPong(t, DRIFT_A_MS));
        float b = ease(pingPong(t, DRIFT_B_MS));
        if (blobA.getTag() != null) {
            blobA.setTranslationX(220 * k * a);
            blobA.setTranslationY(140 * k * a);
            blobA.setScaleX(1f + .08f * a);
            blobA.setScaleY(1f + .08f * a);
        }
        if (blobB.getTag() != null) {
            blobB.setTranslationX(-260 * k * b);
            blobB.setTranslationY(-120 * k * b);
            blobB.setScaleX(1f - .06f * b);
            blobB.setScaleY(1f - .06f * b);
        }
        waves.setTranslationX(-(t % WAVE_MS) / (float) WAVE_MS * w);
        if (Design.STARS > 0f) {
            for (int i = 0; i < twinklers.length; i++) {
                long period = i % 2 == 0 ? TWINKLE_A_MS : TWINKLE_B_MS;
                long offset = i % 2 == 0 ? 0 : 2_000;
                float p = ease(pingPong(t + offset, period / 2));
                twinklers[i].setAlpha(Design.STARS * (.5f - .42f * p));
            }
        }
    }

    /** 0→1→0 over two halves of the period. */
    static float pingPong(long t, long halfPeriod) {
        long m = t % (2 * halfPeriod);
        float f = m / (float) halfPeriod;
        return f <= 1f ? f : 2f - f;
    }

    /** CSS ease-in-out, close enough for drifting. */
    static float ease(float x) {
        return x < .5f ? 2 * x * x : 1 - (float) Math.pow(-2 * x + 2, 2) / 2;
    }

    /** The still stars, drawn once; the twinkling ones are separate tiny views so only their alpha changes. */
    private final class StarField extends View {
        final View[] twinklers;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        StarField(Context ctx) {
            super(ctx);
            paint.setColor(0x80FFFFFF);
            int n = (STARS.length + 1) / 2;
            twinklers = new View[n];
            int size = Math.max(2, Math.round(2 * density));
            for (int i = 0; i < n; i++) {
                View s = new View(ctx);
                GradientDrawable dot = new GradientDrawable();
                dot.setShape(GradientDrawable.OVAL);
                dot.setColor(0xFFFFFFFF);
                s.setBackground(dot);
                s.setLayoutParams(new LayoutParams(size, size));
                s.setAlpha(.5f);
                twinklers[i] = s;
            }
        }

        @Override
        protected void onDraw(Canvas c) {
            float r = Math.max(1f, density);
            for (int i = 0; i < STARS.length; i++) {
                if (i % 2 == 0) continue;
                c.drawCircle(STARS[i][0] * getWidth(), STARS[i][1] * getHeight(), r, paint);
            }
        }
    }

    /** Two gentle wave lines across twice the screen width, so a translation of one width loops. */
    private final class Waves extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();

        Waves(Context ctx) {
            super(ctx);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(1f, density));
            paint.setColor(Design.WAVE);
            setLayoutParams(new LayoutParams(1, 1));
        }

        @Override
        protected void onSizeChanged(int w, int h, int ow, int oh) {
            path.reset();
            float sx = w / 3200f, sy = h / 200f;
            // The mockup's two SVG paths, each repeated once (0..1600 and 1600..3200).
            for (int rep = 0; rep < 2; rep++) {
                float o = rep * 1600;
                path.moveTo((o) * sx, 120 * sy);
                path.cubicTo((o + 200) * sx, 70 * sy, (o + 400) * sx, 70 * sy, (o + 600) * sx, 110 * sy);
                path.cubicTo((o + 800) * sx, 150 * sy, (o + 1000) * sx, 160 * sy, (o + 1200) * sx, 110 * sy);
                path.cubicTo((o + 1400) * sx, 60 * sy, (o + 1500) * sx, 60 * sy, (o + 1600) * sx, 120 * sy);
                path.moveTo((o) * sx, 150 * sy);
                path.cubicTo((o + 250) * sx, 120 * sy, (o + 450) * sx, 180 * sy, (o + 700) * sx, 150 * sy);
                path.cubicTo((o + 950) * sx, 120 * sy, (o + 1150) * sx, 110 * sy, (o + 1350) * sx, 150 * sy);
                path.cubicTo((o + 1450) * sx, 170 * sy, (o + 1550) * sx, 170 * sy, (o + 1600) * sx, 150 * sy);
            }
        }

        @Override
        protected void onDraw(Canvas c) {
            c.drawPath(path, paint);
        }
    }
}
