package com.gathin.tvhome

import android.view.LayoutInflater
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.Density
import com.android.resources.ScreenOrientation
import com.android.resources.UiMode
import org.junit.Rule
import org.junit.Test

/**
 * Renders the real layouts in each Tenang palette at 1080p TV size, without a TV or emulator.
 * Logic in the activities does not run, so this shows structure, tokens, fonts and spacing.
 * Record: ./gradlew -Psnapshots :app:recordPaparazziDebug  (images in app/src/test/snapshots).
 */
class TenangSnapshots {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig(
            screenWidth = 1920, screenHeight = 1080, density = Density.XHIGH,
            uiMode = UiMode.TELEVISION, xdpi = 320, ydpi = 320,
            orientation = ScreenOrientation.LANDSCAPE, softButtons = false,
        ),
        theme = "Theme.TvHome",
        renderingMode = com.android.ide.common.rendering.api.SessionParams.RenderingMode.NORMAL,
        maxPercentDifference = 0.5,
    )

    private fun home(overlay: Int) {
        paparazzi.context.theme.applyStyle(overlay, true)
        Design.load(paparazzi.context)
        val view = LayoutInflater.from(paparazzi.context).inflate(R.layout.activity_main, null)
        paparazzi.snapshot(view)
    }

    @Test fun homeMalam() = home(R.style.ThemeOverlay_Tenang_Malam)
    @Test fun homeSenja() = home(R.style.ThemeOverlay_Tenang_Senja)
    @Test fun homeEmbun() = home(R.style.ThemeOverlay_Tenang_Embun)
}
