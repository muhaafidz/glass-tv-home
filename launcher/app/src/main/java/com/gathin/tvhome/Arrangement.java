package com.gathin.tvhome;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

/**
 * Where a look puts things on Home (docs/LAYOUT-RESEARCH.md §3). Each look is one implementation; colours,
 * focus shape and backdrop come from its theme overlay. MainActivity asks the current arrangement instead of
 * checking which look is on, so a new look is a new class plus a theme overlay.
 *
 * All the views keep their IDs; an arrangement only moves, hides or resizes them.
 */
interface Arrangement {

    /** Rearranges the inflated Home once, before its first layout. */
    void arrange(View home);

    /** The grid's top padding that places the app row; {@code tenang} is the default (dock near the bottom). */
    default int dockPadding(int tenang, float density) {
        return tenang;
    }

    /** The information rail's top in root coordinates, or NaN for Tenang's (centred above the dock). */
    default float railTop(View root, int railHeight, float density) {
        return Float.NaN;
    }

    /** True when the look brings its own background, so photos are not loaded. */
    default boolean ownBackdrop() {
        return false;
    }

    /** Lautan: a vertical list replaces the grid, with the slanted menu. */
    default boolean sideList() {
        return false;
    }

    /** Sorot: the focused app colours the screen and its name becomes the hero. */
    default boolean appSpotlight() {
        return false;
    }

    /** Sorot: the prayer times as a strip at the top. */
    default boolean prayerStrip() {
        return false;
    }

    /** The arrangement for the look whose tokens are loaded (Design.BACKDROP: 0 Tenang, 1 Lautan, 2 Sorot). */
    static Arrangement current() {
        if (Design.BACKDROP == 1) return Lautan.INSTANCE;
        if (Design.BACKDROP == 2) return Sorot.INSTANCE;
        return Tenang.INSTANCE;
    }

    /** Tenang: the layout as written in activity_main.xml. */
    final class Tenang implements Arrangement {
        static final Tenang INSTANCE = new Tenang();

        @Override
        public void arrange(View home) {
        }
    }

    /**
     * Lautan: the slanted list owns the left; the clock, date and chips move right, the top buttons give way to
     * the list and its menu, and the prayer and ayah cards stack in a right column under the chips.
     */
    final class Lautan implements Arrangement {
        static final Lautan INSTANCE = new Lautan();

        @Override
        public void arrange(View home) {
            LookType.toEnd(home, R.id.leftBlock, R.id.weatherRow, R.id.nowPlayingCard);
            LookType.gone(home, R.id.topButtons);
            LookType.maxWidth(home, R.id.weatherWarningText, 240);
            LookType.stackRail(home);
            LookType.maxLines(home, R.id.ayahText, 2);
            LookType.rightColumn(home, 0.46f);
            LookType.slantedDisplayType(home);
        }

        @Override
        public float railTop(View root, int railHeight, float density) {
            View chips = root.findViewById(R.id.weatherRow);
            if (chips == null || chips.getHeight() == 0) return Float.NaN;
            Rect c = new Rect(0, 0, chips.getWidth(), chips.getHeight());
            ((ViewGroup) root).offsetDescendantRectToMyCoords(chips, c);
            return c.bottom + 18 * density;
        }

        @Override public boolean ownBackdrop() { return true; }
        @Override public boolean sideList() { return true; }
    }

    /**
     * Sorot: a small clock, the prayer strip under the chips, the app row at the top with a hero under it, and the
     * ayah card beside the hero on the right. Nothing Islamic along the bottom.
     */
    final class Sorot implements Arrangement {
        static final Sorot INSTANCE = new Sorot();

        @Override
        public void arrange(View home) {
            LookType.smallClock(home, 34);
            LookType.gone(home, R.id.greeting);
            LookType.maxLines(home, R.id.ayahText, 2);
            LookType.visible(home, R.id.sorotHero);
            LookType.visible(home, R.id.prayerStrip);
            LookType.gone(home, R.id.prayerBlock);
            LookType.gone(home, R.id.infoDivider);
            LookType.rightColumn(home, 0.42f);
        }

        @Override
        public int dockPadding(int tenang, float density) {
            return (int) (6 * density);
        }

        @Override
        public float railTop(View root, int railHeight, float density) {
            View hero = root.findViewById(R.id.sorotHero);
            return hero != null && hero.getVisibility() == View.VISIBLE && hero.getY() > 0 ? hero.getY() : Float.NaN;
        }

        @Override public boolean ownBackdrop() { return true; }
        @Override public boolean appSpotlight() { return true; }
        @Override public boolean prayerStrip() { return true; }
    }
}
