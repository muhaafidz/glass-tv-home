package com.gathin.tvhome;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import java.util.ArrayList;
import java.util.List;

/**
 * The focus: one rounded shape that glides from whatever had focus to whatever has it now,
 * changing only its size and corners on the way. Around a tile it is an accent wash and edge in
 * the app's colour; on the top menu it is a dark fill with an accent edge. It sits behind what it
 * marks, so it never draws over an app, a pill's label or a row's text.
 */
final class Glider extends View {

    /** The top menu's look: a dark fill. */
    static final int DARK = 0;
    /** Between apps: an accent wash and edge. */
    static final int GLASS = 1;

    private static final class Edge extends FloatPropertyCompat<Glider> {
        private final int which;

        Edge(String name, int which) {
            super(name);
            this.which = which;
        }

        @Override
        public float getValue(Glider g) {
            return g.edges[which];
        }

        @Override
        public void setValue(Glider g, float value) {
            g.edges[which] = value;
            g.changed();
        }
    }

    private static final int LEFT = 0, TOP = 1, RIGHT = 2, BOTTOM = 3, RADIUS = 4;

    /** Left, top, right, bottom (top and bottom in scroll-free space) and corner radius. */
    private final float[] edges = new float[5];
    private final SpringAnimation[] springs = new SpringAnimation[5];
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF shape = new RectF(), glowRect = new RectF();
    private int style = DARK;
    private boolean paneHidden;
    private int accentColor = Design.ACCENT;
    private final float density;
    private final List<View> companions = new ArrayList<>();
    /** Per companion, the part of it the shape matters to (in its own coordinates), or null for all of it. */
    private final List<RectF> companionAreas = new ArrayList<>();
    /** Where the shape was last drawn and where it is now, in this view's coordinates. */
    private final RectF drawn = new RectF(), dirty = new RectF(), area = new RectF();
    private final int[] gliderAt = new int[2], companionAt = new int[2];
    private final float[] target = new float[5];
    private boolean reducedMotion;
    /** How far a scrolling list has carried the shape since its target was set. */
    private float yShift;
    private float opacity;
    private boolean shown;
    private ValueAnimator fade;
    /** Run once the shape has come to rest after a glide (see {@link #onArrive}). */
    private Runnable arrival;

    Glider(Context context, boolean reducedMotion) {
        super(context);
        this.reducedMotion = reducedMotion;
        float density = context.getResources().getDisplayMetrics().density;
        this.density = density;
        setFocusable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        String[] names = {"left", "top", "right", "bottom", "radius"};
        for (int i = 0; i < springs.length; i++) {
            springs[i] = new SpringAnimation(this, new Edge(names[i], i));
            // The same feel as the tiles have always had: critically damped, so it glides onto its
            // target and stops there; stiffer on GPUs that cannot afford long animations.
            if (reducedMotion) {
                springs[i].setSpring(new SpringForce().setStiffness(1400f).setDampingRatio(0.95f));
                springs[i].setMinimumVisibleChange(1.5f);
            } else {
                springs[i].setSpring(new SpringForce().setStiffness(900f).setDampingRatio(1.0f));
            }
            springs[i].addEndListener((anim, canceled, value, velocity) -> {
                if (!canceled) arriveIfResting();
            });
        }
    }

    void setReducedMotion(boolean reduced) {
        this.reducedMotion = reduced;
        for (int i = 0; i < springs.length; i++) {
            if (springs[i] != null) {
                if (reduced) {
                    springs[i].setSpring(new SpringForce().setStiffness(1400f).setDampingRatio(0.95f));
                    springs[i].setMinimumVisibleChange(1.5f);
                } else {
                    springs[i].setSpring(new SpringForce().setStiffness(900f).setDampingRatio(1.0f));
                    springs[i].setMinimumVisibleChange(1.0f);
                }
            }
        }
    }

    /**
     * Shows the shape at once on a rectangle, at full strength and with no fade, so it can glide on
     * from there: the Inputs menu's selection handing over to the top menu as the menu closes.
     */
    void appearAt(float l, float t, float r, float b, float radius) {
        float[] to = aim(l, t, r, b, radius);
        for (int i = 0; i < springs.length; i++) {
            springs[i].cancel();
            edges[i] = to[i];
        }
        if (fade != null) fade.cancel();
        shown = true;
        opacity = 1f;
        changed();
    }

    /** Runs {@code then} once the shape comes to rest from the glide now under way. */
    void onArrive(Runnable then) {
        arrival = then;
        // Nothing may be moving at all (already in place): settle on the next frame.
        post(this::arriveIfResting);
    }

    private void arriveIfResting() {
        if (arrival == null) return;
        for (SpringAnimation sp : springs) if (sp.isRunning()) return;
        Runnable then = arrival;
        arrival = null;
        then.run();
    }

    /** Something that paints part of the shape itself, or fades for it, and so redraws as it moves. */
    void addCompanion(View v) {
        addCompanion(v, null);
    }

    /**
     * As {@link #addCompanion(View)}, for a view where only {@code area} (in its own coordinates, kept
     * up to date by its owner) shows the shape: it redraws only when the shape is, or was, over that area.
     */
    void addCompanion(View v, RectF area) {
        companions.add(v);
        companionAreas.add(area);
    }

    void removeCompanion(View v) {
        int i = companions.indexOf(v);
        if (i < 0) return;
        companions.remove(i);
        companionAreas.remove(i);
    }

    /** The target edges, in scroll-free space, in one reused array. */
    private float[] aim(float l, float t, float r, float b, float radius) {
        target[LEFT] = l;
        target[TOP] = t - yShift;
        target[RIGHT] = r;
        target[BOTTOM] = b - yShift;
        target[RADIUS] = radius;
        return target;
    }

    /**
     * Sends the shape to the rectangle (in this view's coordinates as they are now) with the given
     * corner radius: a glide when it is showing, an appearance in place when it is not.
     */
    void moveTo(float l, float t, float r, float b, float radius) {
        float[] to = aim(l, t, r, b, radius);
        if (!shown) {
            for (int i = 0; i < springs.length; i++) {
                springs[i].cancel();
                edges[i] = to[i];
            }
            shown = true;
            fadeTo(1f, reducedMotion ? 100 : 180);
            changed();
            return;
        }
        for (int i = 0; i < springs.length; i++) springs[i].animateToFinalPosition(to[i]);
    }

    void setPaneHidden(boolean hidden) {
        if (paneHidden == hidden) return;
        paneHidden = hidden;
        changed();
    }

    /** Switches look at once; there is no blend between the two. */
    void setStyle(int style) {
        if (this.style == style) return;
        this.style = style;
        changed();
    }

    /**
     * Tenang: the ring is always the palette's accent, so focus reads the same on every tile and in
     * every menu. The app's colour is ignored here (callers still pass it).
     */
    void setAccentColor(int color) {
        color = Design.ACCENT;
        if (accentColor == color) return;
        accentColor = color;
        changed();
    }

    void hide() {
        arrival = null;
        if (!shown) return;
        shown = false;
        fadeTo(0f, reducedMotion ? 100 : 160);
    }

    /** Gone at once, for when the screen is rebuilt underneath it. */
    void hideNow() {
        arrival = null;
        shown = false;
        if (fade != null) fade.cancel();
        opacity = 0f;
        changed();
    }

    /** A list scrolled by {@code dy}: the shape rides along with what it marks. */
    void scrolled(float dy) {
        yShift -= dy;
        changed();
    }

    boolean isShowing() {
        return opacity > 0f && edges[RIGHT] > edges[LEFT];
    }

    /**
     * How much of a {@code w} by {@code h} area the shape covers, 0 to 1, for an area whose origin is
     * (dx, dy) from this view's own. Fading in or out counts for less.
     */
    float coverage(float dx, float dy, float w, float h) {
        if (!isShowing() || w <= 0f || h <= 0f) return 0f;
        float ox = Math.min(edges[RIGHT] + dx, w) - Math.max(edges[LEFT] + dx, 0f);
        float oy = Math.min(edges[BOTTOM] + yShift + dy, h) - Math.max(edges[TOP] + yShift + dy, 0f);
        if (ox <= 0f || oy <= 0f) return 0f;
        return Math.min(1f, (ox * oy) / (w * h)) * opacity;
    }

    /**
     * Draws the shape on {@code canvas}, whose origin sits at (dx, dy) from this view's own.
     * {@code canvasOwner} is the view the canvas belongs to, which the glass needs to line up the
     * picture behind it.
     */
    void drawShape(Canvas canvas, float dx, float dy, View canvasOwner) {
        if (!isShowing()) return;
        float t = edges[TOP] + yShift, b = edges[BOTTOM] + yShift;
        // Never rounder than the shape allows: a pill's ends stay round as it shrinks to a circle.
        float r = Math.min(edges[RADIUS], Math.min(b - t, edges[RIGHT] - edges[LEFT]) / 2f);
        shape.set(edges[LEFT] + dx, t + dy, edges[RIGHT] + dx, b + dy);
        if (paneHidden) return;
        int alpha = Math.round(255 * opacity);
        int rgb = accentColor & 0x00FFFFFF;
        if (style == DARK) {
            // Menus: a raised fill under the accent ring.
            paint.setStyle(Paint.Style.FILL);
            paint.setColor((Math.round(0xF0 * opacity) << 24) | (Design.SURFACE_RAISED & 0x00FFFFFF));
            canvas.drawRoundRect(shape, r, r, paint);
            drawRing(canvas, r, rgb, alpha, 2f);
            return;
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor((Math.round(0x14 * opacity) << 24) | rgb);
        canvas.drawRoundRect(shape, r, r, paint);
        drawRing(canvas, r, rgb, alpha, 2f);
    }

    /**
     * The Tenang focus ring: a solid accent stroke with a soft glow outside it. The glow is three
     * faint strokes stepping outwards; no blur, so it costs the same on every GPU. Reduced motion
     * keeps the ring and drops the glow.
     */
    private void drawRing(Canvas canvas, float r, int rgb, int alpha, float widthDp) {
        paint.setStyle(Paint.Style.STROKE);
        if (!reducedMotion) {
            final float[] step = {3.5f, 6.5f, 9.5f};
            final int[] glow = {0x40, 0x22, 0x10};
            for (int i = 0; i < step.length; i++) {
                float o = step[i] * density;
                paint.setStrokeWidth(3f * density);
                paint.setColor((Math.round(glow[i] * alpha / 255f) << 24) | rgb);
                glowRect.set(shape.left - o, shape.top - o, shape.right + o, shape.bottom + o);
                canvas.drawRoundRect(glowRect, r + o, r + o, paint);
            }
        }
        paint.setStrokeWidth(widthDp * density);
        paint.setColor((alpha << 24) | rgb);
        canvas.drawRoundRect(shape, r, r, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void fadeTo(float to, long ms) {
        if (fade != null) fade.cancel();
        fade = ValueAnimator.ofFloat(opacity, to);
        fade.setDuration(ms);
        fade.addUpdateListener(a -> {
            opacity = (float) a.getAnimatedValue();
            changed();
        });
        fade.start();
    }

    /**
     * Redraws the shape, and each companion it is over or has just left: a glide among the lower
     * rows of apps leaves the tray and the top-bar pills alone.
     */
    private void changed() {
        invalidate();
        if (companions.isEmpty()) return;
        dirty.set(drawn);
        if (isShowing()) {
            float reach = 13f * density + 2f;   // ring plus its outermost glow stroke
            drawn.set(edges[LEFT] - reach, edges[TOP] + yShift - reach,
                    edges[RIGHT] + reach, edges[BOTTOM] + yShift + reach);
            dirty.union(drawn);
        } else {
            drawn.setEmpty();
        }
        if (dirty.isEmpty()) return;
        getLocationInWindow(gliderAt);
        for (int i = 0; i < companions.size(); i++) {
            View v = companions.get(i);
            RectF only = companionAreas.get(i);
            v.getLocationInWindow(companionAt);
            float dx = companionAt[0] - gliderAt[0], dy = companionAt[1] - gliderAt[1];
            if (only == null || only.isEmpty()) area.set(0, 0, v.getWidth(), v.getHeight());
            else area.set(only);
            area.offset(dx, dy);
            if (RectF.intersects(area, dirty)) v.invalidate();
        }
    }

    /**
     * Takes no room of its own: in a list that sizes itself to its rows (the Inputs list), filling
     * whatever height was offered made the list stretch down the screen. Given an exact size, as in
     * Home's full-screen root, it takes that.
     */
    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        setMeasuredDimension(exactOrNothing(widthSpec), exactOrNothing(heightSpec));
    }

    private static int exactOrNothing(int spec) {
        return MeasureSpec.getMode(spec) == MeasureSpec.EXACTLY ? MeasureSpec.getSize(spec) : 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawShape(canvas, 0f, 0f, this);
    }
}
