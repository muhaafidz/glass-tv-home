package com.gathin.tvhome;

import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

/**
 * Home component: the ayah / khutbah card (views ayahBlock, ayahLabel, ayahArabic, ayahText). Shows a
 * daily ayah (Arabic in Amiri, right-aligned, two lines, warm colour) or a Khutbah Minbar title. It only
 * renders; MainActivity decides which, and when it is hidden.
 *
 * Sizes follow Tenang (docs/REVAMP.md §3.2); on 4K panels that report a 1080p-like density they are
 * scaled up by 1.3, as the launcher did before.
 */
final class AyahBlock {

    private final View block;
    private final TextView label, arabic, text;
    private final Typeface amiri, outfit;
    private final float scale;
    private final int maxWidth;

    AyahBlock(View root) {
        block = root.findViewById(R.id.ayahBlock);
        label = root.findViewById(R.id.ayahLabel);
        arabic = root.findViewById(R.id.ayahArabic);
        text = root.findViewById(R.id.ayahText);
        amiri = root.getResources().getFont(R.font.amiri_quran);
        outfit = root.getResources().getFont(R.font.outfit);
        android.util.DisplayMetrics dm = root.getResources().getDisplayMetrics();
        scale = (dm.widthPixels >= 3840 || dm.heightPixels >= 2160) ? 1.3f : 1f;
        maxWidth = Math.round((dm.widthPixels - (55 + 56) * dm.density) * 0.50f);
    }

    boolean exists() {
        return block != null;
    }

    void hide() {
        if (block != null) block.setVisibility(View.GONE);
    }

    void showAyah(CharSequence labelText, String arabicText, String translation) {
        if (block == null) return;
        if (label != null) setSize(label, 12f).setText(labelText);
        if (arabic != null) {
            arabic.setMaxWidth(maxWidth);
            arabic.setSingleLine(false);
            arabic.setMaxLines(2);
            arabic.setEllipsize(TextUtils.TruncateAt.END);
            arabic.setTextDirection(View.TEXT_DIRECTION_RTL);
            arabic.setGravity(Gravity.RIGHT);   // Arabic sits on the right edge; END would mean left in RTL
            arabic.setIncludeFontPadding(false);
            arabic.setTypeface(amiri, Typeface.NORMAL);
            arabic.setTextColor(Design.WARM);
            setSize(arabic, 22f).setText(arabicText.trim());
        }
        setBody(translation, 14f);
        block.setVisibility(View.VISIBLE);
    }

    void showKhutbah(CharSequence labelText, String title, String summary) {
        if (block == null) return;
        if (label != null) setSize(label, 12f).setText(labelText);
        if (arabic != null) {
            arabic.setMaxWidth(maxWidth);
            arabic.setSingleLine(true);
            arabic.setMaxLines(1);
            arabic.setEllipsize(TextUtils.TruncateAt.END);
            arabic.setTextDirection(View.TEXT_DIRECTION_LTR);
            arabic.setGravity(Gravity.START);
            arabic.setTypeface(outfit);
            Fonts.weight(arabic, 500);
            arabic.setTextColor(Design.WARM);
            // The title shrinks a little (down to 12 sp) rather than being cut off.
            float sp = 16f * scale, min = 12f * scale;
            android.text.TextPaint paint = new android.text.TextPaint(arabic.getPaint());
            float sd = arabic.getResources().getDisplayMetrics().scaledDensity;
            paint.setTextSize(sp * sd);
            while (paint.measureText(title) > maxWidth * 0.95f && sp > min) {
                sp -= 0.5f;
                paint.setTextSize(sp * sd);
            }
            arabic.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
            arabic.setText(title.trim());
        }
        setBody(summary, 13f);
        block.setVisibility(View.VISIBLE);
    }

    private void setBody(String body, float sp) {
        if (text == null) return;
        text.setMaxWidth(maxWidth);
        String t = body != null ? body.trim() : "";
        setSize(text, sp).setText(t);
        text.setVisibility(t.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private TextView setSize(TextView v, float sp) {
        v.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp * scale);
        return v;
    }
}
