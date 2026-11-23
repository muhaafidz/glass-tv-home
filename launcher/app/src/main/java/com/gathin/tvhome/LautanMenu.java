package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

/**
 * Lautan's Home menu (docs/LAYOUT-RESEARCH.md §2): stacked slanted words over a dark wash, each at its own
 * small angle and offset; the focused one sits on a white slash with a red edge. Opens with ◀ from the first
 * app or the Menu key; BACK closes. Every entry calls something Home already has.
 *
 * Light: a dozen TextViews and one wash, laid out once; opening animates translation and alpha only, with a
 * short stagger; reduced motion opens it at once.
 */
final class LautanMenu extends FrameLayout {

    static final class Entry {
        final String label, description;
        final Runnable action;

        Entry(String label, String description, Runnable action) {
            this.label = label;
            this.description = description;
            this.action = action;
        }
    }

    private static final float[] ANGLES = {-4f, -2f, -5f, -1f, -6f, -3f, -2f, -5f};
    private static final int[] OFFSETS_DP = {0, 24, 6, 42, 12, 36, 18, 0};
    private static final PathInterpolator EASE = new PathInterpolator(0.2f, 0.9f, 0.25f, 1f);

    private final View wash;
    private final LinearLayout list;
    private final TextView description, title;
    private final float density;
    private final boolean reducedMotion;
    private boolean open;

    LautanMenu(Context ctx, Entry[] entries, boolean reducedMotion) {
        super(ctx);
        this.reducedMotion = reducedMotion;
        density = getResources().getDisplayMetrics().density;
        setVisibility(GONE);
        setClickable(true);

        wash = new View(ctx);
        wash.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Design.alpha(Design.BG1, 0xF0), Design.alpha(Design.BG1, 0xD6)}));
        addView(wash, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        Typeface bold = getResources().getFont(R.font.outfit_bold);
        title = text(ctx, "MENU", 15, Design.TEXT_MUTED, bold);
        title.setLetterSpacing(0.3f);
        LayoutParams tl = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tl.leftMargin = dp(72);
        tl.topMargin = dp(40);
        addView(title, tl);

        list = new LinearLayout(ctx);
        list.setOrientation(LinearLayout.VERTICAL);
        LayoutParams ll = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        ll.leftMargin = dp(84);
        ll.topMargin = dp(82);
        addView(list, ll);

        description = text(ctx, "", 16, Design.TEXT_MUTED, getResources().getFont(R.font.outfit));
        description.setGravity(Gravity.END);
        description.setMaxWidth(dp(320));
        LayoutParams dl = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.END | Gravity.BOTTOM);
        dl.rightMargin = dp(56);
        dl.bottomMargin = dp(64);
        addView(description, dl);

        TextView hint = text(ctx, "OK  Pilih      BACK  Tutup", 14, Design.TEXT, bold);
        LayoutParams hl = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.END | Gravity.BOTTOM);
        hl.rightMargin = dp(56);
        hl.bottomMargin = dp(28);
        addView(hint, hl);

        for (int i = 0; i < entries.length; i++) {
            final Entry e = entries[i];
            TextView item = text(ctx, e.label, 40, Design.CYAN, bold);
            item.setFocusable(true);
            item.setClickable(true);
            item.setIncludeFontPadding(false);
            item.setPadding(dp(14), dp(2), dp(26), dp(2));
            item.setBackground(slashStates());
            item.setRotation(ANGLES[i % ANGLES.length]);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            lp.leftMargin = dp(OFFSETS_DP[i % OFFSETS_DP.length]);
            lp.topMargin = dp(2);
            item.setOnFocusChangeListener((v, has) -> {
                ((TextView) v).setTextColor(has ? Design.BG1 : Design.CYAN);
                if (has) {
                    description.setText(e.label + "\n" + e.description);
                    SoundEffects.playNav();
                }
            });
            item.setOnClickListener(v -> {
                SoundEffects.playSelect();
                close();
                e.action.run();
            });
            list.addView(item, lp);
        }
    }

    boolean isOpen() {
        return open;
    }

    /**
     * Keeps the D-pad inside the open menu. Up and Down step through the entries and stop at the
     * ends; Left and Right are swallowed so focus cannot slip to the screen under the wash. A menu
     * that is closed ignores every key, letting Home handle it as usual.
     *
     * @return true when the key belonged to the menu and Home must not act on it
     */
    boolean handleKeyEvent(KeyEvent event) {
        if (!open) return false;
        int key = event.getKeyCode();
        boolean vertical = key == KeyEvent.KEYCODE_DPAD_UP || key == KeyEvent.KEYCODE_DPAD_DOWN;
        boolean horizontal = key == KeyEvent.KEYCODE_DPAD_LEFT || key == KeyEvent.KEYCODE_DPAD_RIGHT;
        if (!vertical && !horizontal) return false;
        if (event.getAction() == KeyEvent.ACTION_DOWN && vertical) {
            View focused = findFocus();
            int current = focused == null ? -1 : list.indexOfChild(focused);
            int next = LautanNav.nextIndex(current, list.getChildCount(), key == KeyEvent.KEYCODE_DPAD_DOWN);
            if (next >= 0) list.getChildAt(next).requestFocus();
        }
        return true;
    }

    void open() {
        if (open) return;
        open = true;
        setVisibility(VISIBLE);
        bringToFront();
        if (reducedMotion) {
            wash.setAlpha(1f);
            for (int i = 0; i < list.getChildCount(); i++) {
                list.getChildAt(i).setAlpha(1f);
                list.getChildAt(i).setTranslationX(0f);
            }
        } else {
            wash.setAlpha(0f);
            wash.animate().alpha(1f).setDuration(220).start();
            for (int i = 0; i < list.getChildCount(); i++) {
                View v = list.getChildAt(i);
                v.setAlpha(0f);
                v.setTranslationX(-dp(60));
                v.animate().alpha(1f).translationX(0f).setStartDelay(i * 35L).setDuration(320)
                        .setInterpolator(EASE).start();
            }
        }
        if (list.getChildCount() > 0) list.getChildAt(0).requestFocus();
    }

    void close() {
        if (!open) return;
        open = false;
        if (reducedMotion) {
            setVisibility(GONE);
            return;
        }
        animate().alpha(0f).setDuration(160).withEndAction(() -> {
            setVisibility(GONE);
            setAlpha(1f);
        }).start();
    }

    private TextView text(Context ctx, String s, float sp, int color, Typeface face) {
        TextView t = new TextView(ctx);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        t.setTypeface(face);
        t.getPaint().setTextSkewX(-0.2f);
        return t;
    }

    private Drawable slashStates() {
        StateListDrawable s = new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_focused}, new SlashDrawable(density));
        s.addState(new int[]{}, new android.graphics.drawable.ColorDrawable(0));
        return s;
    }

    private int dp(float v) {
        return Math.round(v * density);
    }
}
