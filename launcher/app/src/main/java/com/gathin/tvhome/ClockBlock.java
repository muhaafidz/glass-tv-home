package com.gathin.tvhome;

import android.view.View;
import android.widget.TextView;

/**
 * Home component: the period (PG/PTG/MLM), the date and the greeting next to the clock. The clock
 * itself is a TextClock and needs nothing. Views: ampm, date, greeting; any may be missing.
 */
final class ClockBlock {

    private final TextView period, date, greeting;

    ClockBlock(View root) {
        period = root.findViewById(R.id.ampm);
        date = root.findViewById(R.id.date);
        greeting = root.findViewById(R.id.greeting);
    }

    void set(CharSequence periodText, CharSequence dateText, CharSequence greetingText) {
        if (period != null) period.setText(periodText);
        if (date != null) date.setText(dateText);
        if (greeting != null) greeting.setText(greetingText);
    }
}
