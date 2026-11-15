package com.gathin.tvhome;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Draws the glass tray behind the first (dock) row and adds a gap below it: the blurred wallpaper
 * behind it, an even veil and a light rim, with no brighter top.
 */
public class DockDecoration extends RecyclerView.ItemDecoration {

    private final int dockSize;
    private final float density;
    private final Paint band = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edge = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Glider glider;
    private final int[] at = new int[2], gliderAt = new int[2];

    private final RectF rect = new RectF();
    /** The tray as last drawn, in the grid's coordinates: where the focus shape shows on it. */
    final RectF area = new RectF();

    DockDecoration(int dockSize, float density) {
        this.dockSize = dockSize;
        this.density = density;
        band.setColor(0xF214161D);
        edge.setStyle(Paint.Style.STROKE);
        edge.setStrokeWidth(1f * density);
        edge.setColor(Design.HAIRLINE);
    }

    /** The focus shape: the tray's glass would hide it, so the tray paints it over the glass itself. */
    void setGlider(Glider glider) {
        this.glider = glider;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        int pos = parent.getChildAdapterPosition(view);
        if (pos >= 0 && pos < dockSize) {
            outRect.top = (int) (6 * density);
            outRect.bottom = (int) (12 * density);
        }
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        boolean any = false;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int pos = parent.getChildAdapterPosition(child);
            if (pos < 0 || pos >= dockSize) continue;
            View card = child.findViewById(R.id.card);
            if (card == null) continue;
            float l = child.getLeft() + card.getLeft();
            float t = child.getTop() + card.getTop() + child.getTranslationY();
            float r = l + card.getWidth();
            float b = t + card.getHeight();
            if (!any) {
                rect.set(l, t, r, b);
                any = true;
            } else {
                rect.union(l, t, r, b);
            }
        }
        if (!any) return;
        float pad = 14 * density;
        rect.inset(-pad, -pad);
        area.set(rect);
        float radius = 24 * density;
        c.drawRoundRect(rect, radius, radius, band);
        c.drawRoundRect(rect, radius, radius, edge);
        // The focus frame of a dock tile sits on the tray's glass and under the tiles; the rest of
        // the shape, outside the tray, is the glider itself showing behind.
        if (glider != null && glider.isShowing()) {
            parent.getLocationInWindow(at);
            glider.getLocationInWindow(gliderAt);
            c.save();
            RectF clipArea = new RectF(rect);
            float shadowBleed = 36f * density;
            clipArea.inset(-shadowBleed, -shadowBleed);
            c.clipRect(clipArea);
            glider.drawShape(c, gliderAt[0] - at[0], gliderAt[1] - at[1], parent);
            c.restore();
        }

    }
}
