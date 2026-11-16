package com.gathin.tvhome;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** The Tenang canvas's drift arithmetic: positions depend only on the clock, so pauses resume smoothly. */
public class AmbientMathTest {

    @Test
    public void pingPongGoesThereAndBack() {
        assertEquals(0f, AmbientView.pingPong(0, 1000), 1e-6);
        assertEquals(.5f, AmbientView.pingPong(500, 1000), 1e-6);
        assertEquals(1f, AmbientView.pingPong(1000, 1000), 1e-6);
        assertEquals(.5f, AmbientView.pingPong(1500, 1000), 1e-6);
        assertEquals(0f, AmbientView.pingPong(2000, 1000), 1e-6);
        assertEquals(.25f, AmbientView.pingPong(2250, 1000), 1e-6);
    }

    @Test
    public void easeIsSymmetricAndBounded() {
        assertEquals(0f, AmbientView.ease(0f), 1e-6);
        assertEquals(.5f, AmbientView.ease(.5f), 1e-6);
        assertEquals(1f, AmbientView.ease(1f), 1e-6);
        assertEquals(1f - AmbientView.ease(.2f), AmbientView.ease(.8f), 1e-5);
    }
}
