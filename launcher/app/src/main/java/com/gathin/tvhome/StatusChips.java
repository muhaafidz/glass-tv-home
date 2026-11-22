package com.gathin.tvhome;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

/**
 * Home component: the weather chip and the air-quality chip (views weatherAirQuality, aqiChip).
 * Without an aqiChip in the layout, weather and IPU share one line, as before Tenang.
 */
final class StatusChips {

    private final TextView weather, aqi;
    private final float density;

    StatusChips(View root) {
        weather = root.findViewById(R.id.weatherAirQuality);
        aqi = root.findViewById(R.id.aqiChip);
        density = root.getResources().getDisplayMetrics().density;
    }

    TextView weatherView() {
        return weather;
    }

    /** Fallback text (no weather line yet): only the weather chip, with no icon. */
    void setPlain(CharSequence text) {
        if (weather == null) return;
        weather.setText(text);
    }

    void set(HomeWeather.Line line) {
        if (weather == null || line == null) return;
        if (aqi != null) {
            weather.setText(line.weather.isEmpty() ? line.api : line.weather);
            if (!line.weather.isEmpty() && !line.api.isEmpty()) {
                aqi.setText(line.api);
                Drawable dot = aqi.getContext().getDrawable(R.drawable.tn_dot).mutate();
                dot.setTint(line.apiColor);
                int dotPx = Math.round(7 * density);
                dot.setBounds(0, 0, dotPx, dotPx);
                aqi.setCompoundDrawables(dot, null, null, null);
                aqi.setCompoundDrawablePadding(Math.round(7 * density));
                aqi.setVisibility(View.VISIBLE);
            } else {
                aqi.setVisibility(View.GONE);
            }
        } else {
            String sep = line.weather.isEmpty() || line.api.isEmpty() ? "" : "     ·     ";
            android.text.SpannableString text = new android.text.SpannableString(line.weather + sep + line.api);
            if (!line.api.isEmpty()) {
                text.setSpan(new android.text.style.ForegroundColorSpan(line.apiColor), text.length() - line.api.length(),
                        text.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            weather.setText(text);
        }
        if (line.icon != 0) {
            Drawable d = weather.getContext().getDrawable(line.icon);
            int px = Math.round(17 * density);
            d.setBounds(0, 0, px, px);
            weather.setCompoundDrawables(d, null, null, null);
            weather.setCompoundDrawablePadding(Math.round(7 * density));
        } else {
            weather.setCompoundDrawables(null, null, null, null);
        }
    }
}
