package com.gathin.tvhome

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
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
        fill(view)
        paparazzi.snapshot(view)
    }

    /** Sample content, roughly what MainActivity shows, and the information rail where it sits above the dock. */
    private fun fill(root: View) {
        fun text(id: Int, value: CharSequence) { root.findViewById<TextView>(id)?.text = value }
        val density = root.resources.displayMetrics.density
        text(R.id.ampm, "MLM"); text(R.id.date, "16 Nov 2026"); text(R.id.greeting, "Selamat malam \u2726")
        text(R.id.weatherAirQuality, "24\u00b0  Hujan renyai")
        root.findViewById<TextView>(R.id.aqiChip).apply { text = "IPU 65 Sederhana"; visibility = View.VISIBLE }
        root.findViewById<View>(R.id.weatherWarningPill).visibility = View.VISIBLE
        text(R.id.weatherWarningText, "Amaran Ribut Petir \u00b7 Amaran Angin Kencang dan Laut Bergelora")
        root.findViewById<View>(R.id.ayahBlock).visibility = View.VISIBLE
        text(R.id.ayahLabel, "Doa harian  \u2022  Al-Baqarah 2:156")
        text(R.id.ayahArabic, "\u0671\u0644\u0651\u064e\u0630\u0650\u064a\u0646\u064e \u0625\u0650\u0630\u064e\u0622 \u0623\u064e\u0635\u064e\u0670\u0628\u064e\u062a\u0652\u0647\u064f\u0645 \u0645\u0651\u064f\u0635\u0650\u064a\u0628\u064e\u0629\u064c")
        text(R.id.ayahText, "(Iaitu) orang-orang yang apabila mereka ditimpa oleh sesuatu kesusahan, mereka berkata: \"Sesungguhnya kami adalah kepunyaan Allah dan kepada Allah jualah kami kembali.\"")
        text(R.id.prayerHeader, "Waktu solat  \u2022  Shah Alam  \u2022  6 Jamadilakhir 1448H")
        val names = listOf("Subuh", "Syuruk", "Zohor", "Asar", "Maghrib", "Isyak")
        val times = listOf("05:48", "06:55", "13:00", "16:20", "18:58", "20:10")
        val lbl = listOf(R.id.lblSubuh, R.id.lblSyuruk, R.id.lblZohor, R.id.lblAsar, R.id.lblMaghrib, R.id.lblIsyak)
        val tm = listOf(R.id.timeSubuh, R.id.timeSyuruk, R.id.timeZohor, R.id.timeAsar, R.id.timeMaghrib, R.id.timeIsyak)
        for (i in 0 until 6) {
            val l = root.findViewById<TextView>(lbl[i]); l.text = names[i]
            val t = root.findViewById<TextView>(tm[i]); t.text = times[i]; t.setTextColor(Design.TEXT)
            l.setTextColor(if (i == 0) Design.ACCENT else Design.TEXT_MUTED)
            if (i == 0) (l.parent as View).setBackgroundResource(R.drawable.tn_prayer_next)
        }
        text(R.id.prayerPrev, "Isyak"); text(R.id.prayerNextIn, "Subuh dalam 5j 36m")
        root.findViewById<View>(R.id.islamicShelf).translationY = 185 * density
        (root as android.widget.FrameLayout).addView(AmbientView(root.context), 0,
            android.widget.FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT))
    }

    @Test fun homeMalam() = home(R.style.ThemeOverlay_Tenang_Malam)
    @Test fun homeSenja() = home(R.style.ThemeOverlay_Tenang_Senja)
    @Test fun homeEmbun() = home(R.style.ThemeOverlay_Tenang_Embun)
}
