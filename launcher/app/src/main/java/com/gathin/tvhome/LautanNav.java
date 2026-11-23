package com.gathin.tvhome;

/**
 * The focus order of Lautan's open menu, kept apart from the views so it can be tested on its own.
 *
 * <p>Up and Down move one entry at a time and stop at both ends: the menu is a short list, and
 * wrapping from TUTUP back to APLIKASI would surprise a person pressing Down. A menu with no focus
 * yet starts at the top going down and at the bottom going up.
 */
final class LautanNav {

    private LautanNav() {}

    /**
     * The entry a single Up or Down press lands on.
     *
     * @param current index of the focused entry, or -1 when none is focused
     * @param count   number of entries in the menu
     * @param down    true for Down, false for Up
     * @return the entry to focus, or -1 when the menu has no entries
     */
    static int nextIndex(int current, int count, boolean down) {
        if (count <= 0) return -1;
        if (current < 0) return down ? 0 : count - 1;
        int step = down ? 1 : -1;
        return Math.max(0, Math.min(count - 1, current + step));
    }
}
