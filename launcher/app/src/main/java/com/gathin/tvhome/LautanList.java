package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Lautan's Home list (docs/LAYOUT-RESEARCH.md §10): the apps, then a few shortcuts, as one slanted vertical
 * list on the left that scrolls; the focused entry sits on the slash. It reads the same AppAdapter as the grid
 * (which Lautan hides), so app order, hidden apps and cards stay one source.
 *
 * Light: one row view per app (a small banner chip and a word), built when the apps change; ScrollView moves
 * focus into view; nothing animates on its own.
 */
final class LautanList extends FrameLayout {

    interface Host {
        void onLeftFromList();
    }

    static final class Shortcut {
        final String label;
        final Runnable action;

        Shortcut(String label, Runnable action) {
            this.label = label;
            this.action = action;
        }
    }

    private static final float[] ANGLES = {-3f, -2f, -4f, -1f, -3f, -2f, -4f, -1f};
    private static final int[] OFFSETS_DP = {0, 16, 6, 24, 10, 20, 4, 14};

    private final ScrollView scroll;
    private final LinearLayout column;
    private final float density;
    private final Typeface bold;
    private final Host host;

    LautanList(Context ctx, Host host) {
        super(ctx);
        this.host = host;
        density = getResources().getDisplayMetrics().density;
        bold = getResources().getFont(R.font.outfit_bold);

        TextView title = new TextView(ctx);
        title.setText("APLIKASI");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        title.setLetterSpacing(0.3f);
        title.setTypeface(bold);
        title.setTextColor(Design.TEXT_MUTED);
        title.getPaint().setTextSkewX(-0.2f);
        LayoutParams tl = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tl.leftMargin = dp(22);
        addView(title, tl);

        scroll = new ScrollView(ctx);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setClipToPadding(false);
        scroll.setPadding(0, dp(8), 0, dp(80));
        scroll.setFadingEdgeLength(dp(40));
        scroll.setVerticalFadingEdgeEnabled(true);
        column = new LinearLayout(ctx);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setClipChildren(false);
        scroll.addView(column, new ScrollView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        LayoutParams sl = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        sl.topMargin = dp(26);
        addView(scroll, sl);
    }

    /** Rebuilds the rows from the adapter (apps) followed by the shortcuts. */
    void rebuild(AppAdapter apps, Shortcut[] shortcuts) {
        View focused = column.findFocus();
        int focusedIndex = focused != null ? column.indexOfChild(focused) : -1;
        column.removeAllViews();
        int n = apps != null ? apps.getItemCount() : 0;
        for (int i = 0; i < n; i++) {
            final int pos = i;
            Drawable art = apps.bannerAt(i) != null ? apps.bannerAt(i) : apps.iconAt(i);
            addRow(apps.nameAt(i), art, apps.bannerAt(i) != null, () -> apps.launchAt(pos), false);
        }
        for (int i = 0; i < shortcuts.length; i++) {
            addRow(shortcuts[i].label, null, false, shortcuts[i].action, i == 0);
        }
        if (focusedIndex >= 0 && focusedIndex < column.getChildCount()) column.getChildAt(focusedIndex).requestFocus();
    }

    /** Focus the n-th row (apps first). */
    void focus(int index) {
        if (column.getChildCount() == 0) return;
        View v = column.getChildAt(Math.max(0, Math.min(index, column.getChildCount() - 1)));
        v.requestFocus();
    }

    private void addRow(String label, Drawable art, boolean isBanner, Runnable action, boolean gapAbove) {
        int i = column.getChildCount();
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setFocusable(true);
        row.setClickable(true);
        row.setBackground(SlashDrawable.states(density));
        row.setPadding(dp(10), dp(4), dp(22), dp(4));
        row.setRotation(ANGLES[i % ANGLES.length]);

        ImageView chip = new ImageView(getContext());
        chip.setScaleType(isBanner ? ImageView.ScaleType.CENTER_CROP : ImageView.ScaleType.FIT_CENTER);
        GradientDrawable chipBg = new GradientDrawable();
        chipBg.setCornerRadius(dp(4));
        chipBg.setColor(Design.alpha(Design.INK, 0x1F));
        chip.setBackground(chipBg);
        chip.setClipToOutline(true);
        int pad = isBanner ? 0 : dp(6);
        chip.setPadding(pad, pad, pad, pad);
        if (art != null) chip.setImageDrawable(art.getConstantState() != null ? art.getConstantState().newDrawable() : art);
        LinearLayout.LayoutParams cl = new LinearLayout.LayoutParams(dp(50), dp(28));
        cl.rightMargin = dp(14);
        row.addView(chip, cl);

        TextView word = new TextView(getContext());
        word.setText(label);
        word.setSingleLine(true);
        word.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        word.setTypeface(bold);
        word.setIncludeFontPadding(false);
        word.setTextColor(Design.CYAN);
        word.getPaint().setTextSkewX(-0.2f);
        row.addView(word);

        row.setOnFocusChangeListener((v, has) -> {
            word.setTextColor(has ? Design.BG1 : Design.CYAN);
            if (has) SoundEffects.playNav();
        });
        row.setOnClickListener(v -> {
            SoundEffects.playSelect();
            action.run();
        });
        row.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                host.onLeftFromList();
                return true;
            }
            // Right stays in the list: nothing to its right takes focus in Lautan.
            return event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT;
        });
        LinearLayout.LayoutParams rl = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rl.leftMargin = dp(OFFSETS_DP[i % OFFSETS_DP.length] + 12);
        rl.topMargin = dp(gapAbove ? 16 : 2);
        column.addView(row, rl);
    }

    private int dp(float v) {
        return Math.round(v * density);
    }
}
