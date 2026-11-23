package com.gathin.tvhome;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Lautan's menu focus order: one entry per press, stopped at both ends (LautanNav). */
public class LautanNavTest {

    @Test
    public void stepsOneEntryAtATime() {
        assertEquals(1, LautanNav.nextIndex(0, 8, true));
        assertEquals(4, LautanNav.nextIndex(3, 8, true));
        assertEquals(2, LautanNav.nextIndex(3, 8, false));
    }

    @Test
    public void stopsAtTheEnds() {
        assertEquals(0, LautanNav.nextIndex(0, 8, false));
        assertEquals(7, LautanNav.nextIndex(7, 8, true));
    }

    @Test
    public void noFocusStartsAtTheNearEnd() {
        assertEquals(0, LautanNav.nextIndex(-1, 8, true));
        assertEquals(7, LautanNav.nextIndex(-1, 8, false));
    }

    @Test
    public void emptyMenuHasNoTarget() {
        assertEquals(-1, LautanNav.nextIndex(0, 0, true));
        assertEquals(-1, LautanNav.nextIndex(-1, 0, false));
    }
}
