package com.gathin.tvhome;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Keeps the second app row below Sorot's hero band.
 *
 * <p>Sorot parks one row of apps under the header, then shows the focused app's name and the ayah
 * card in a band underneath it; the remaining apps start below that band. Without the gap the
 * second row was laid out directly under the first and drew on top of the hero and the card.
 *
 * <p>Only the row after the dock is shifted, and only by the height Home measures for the band, so
 * later rows keep their normal spacing. The height changes when the ayah text or the hero name
 * changes; Home calls {@link #setHeight(int)} and then asks the parent to re-run its decorations.
 */
final class SorotBandDecoration extends RecyclerView.ItemDecoration {

    private final int dockSize;
    private int height;

    /**
     * @param dockSize number of apps in the top row; the next row is the one that moves down
     */
    SorotBandDecoration(int dockSize) {
        this.dockSize = dockSize;
    }

    /** The band's height in pixels, measured by Home from the hero and the ayah card. */
    void setHeight(int height) {
        this.height = Math.max(0, height);
    }

    /** The height last set, used to skip a re-layout when nothing changed. */
    int height() {
        return height;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position >= dockSize && position < dockSize * 2) outRect.top = height;
    }
}
