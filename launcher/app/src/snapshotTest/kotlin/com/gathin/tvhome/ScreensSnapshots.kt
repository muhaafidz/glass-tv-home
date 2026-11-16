package com.gathin.tvhome

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.Density
import com.android.resources.ScreenOrientation
import com.android.resources.UiMode
import org.junit.Rule
import org.junit.Test

/** The other screens' layouts in each Tenang palette (docs/REVAMP.md P5, P6). Structure and tokens only. */
class ScreensSnapshots {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig(
            screenWidth = 1920, screenHeight = 1080, density = Density.XHIGH,
            uiMode = UiMode.TELEVISION, xdpi = 320, ydpi = 320,
            orientation = ScreenOrientation.LANDSCAPE, softButtons = false,
        ),
        theme = "Theme.TvHome",
        maxPercentDifference = 0.5,
    )

    private val layouts = mapOf(
        "recent" to R.layout.activity_recent_apps,
        "islamicDetail" to R.layout.dialog_islamic_detail_16_9,
        "islamicChoice" to R.layout.dialog_islamic_choice_16_9,
        "weatherWarning" to R.layout.dialog_weather_warning_16_9,
        "ota" to R.layout.dialog_ota_coffee,
        "mediaHub" to R.layout.dialog_media_hub,
        "inputs" to R.layout.popup_inputs,
        "cardPrompt" to R.layout.dialog_card_prompt,
    )

    private fun all(overlay: Int) {
        paparazzi.context.theme.applyStyle(overlay, true)
        Design.load(paparazzi.context)
        for ((name, id) in layouts) {
            val frame = FrameLayout(paparazzi.context)
            frame.setBackgroundColor(Design.BG1)
            val v = LayoutInflater.from(paparazzi.context).inflate(id, frame, false)
            frame.addView(v, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, android.view.Gravity.CENTER))
            paparazzi.snapshot(frame, name)
        }
    }

    @Test fun malam() = all(R.style.ThemeOverlay_Tenang_Malam)
    @Test fun embun() = all(R.style.ThemeOverlay_Tenang_Embun)
}
