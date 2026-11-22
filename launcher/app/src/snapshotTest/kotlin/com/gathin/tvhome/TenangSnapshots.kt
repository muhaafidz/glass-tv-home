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
        // Through the same components MainActivity uses (L0), so the snapshot exercises their code.
        ClockBlock(root).set("MLM", "16 Nov 2026", "Selamat malam \u2726")
        StatusChips(root).set(HomeWeather.Line("24\u00b0  Hujan renyai", "IPU 65 Sederhana", Design.GOOD, 0))
        root.findViewById<View>(R.id.weatherWarningPill).visibility = View.VISIBLE
        text(R.id.weatherWarningText, "Amaran Ribut Petir \u00b7 Amaran Angin Kencang dan Laut Bergelora")
        AyahBlock(root).showAyah("Doa harian  \u2022  Al-Baqarah 2:156", "\u0671\u0644\u0651\u064e\u0630\u0650\u064a\u0646\u064e \u0625\u0650\u0630\u064e\u0622 \u0623\u064e\u0635\u064e\u0670\u0628\u064e\u062a\u0652\u0647\u064f\u0645 \u0645\u0651\u064f\u0635\u0650\u064a\u0628\u064e\u0629\u064c",
            "(Iaitu) orang-orang yang apabila mereka ditimpa oleh sesuatu kesusahan, mereka berkata: \"Sesungguhnya kami adalah kepunyaan Allah dan kepada Allah jualah kami kembali.\"")
        val prayer = PrayerBlock(root)
        prayer.setHeader("Waktu solat  \u2022  Shah Alam  \u2022  6 Jamadilakhir 1448H")
        val names = listOf("Subuh", "Syuruk", "Zohor", "Asar", "Maghrib", "Isyak")
        val times = listOf("05:48", "06:55", "13:00", "16:20", "18:58", "20:10")
        prayer.setPrayers(Array(6) { PrayerTimes.PrayerItem(names[it], times[it], it == 0, false) }, false)
        prayer.setCountdown("Isyak", "Subuh dalam 5j 36m", 0.13f)
        // Same rule as MainActivity.infoRailTop: centred between the chips and the dock (top at 409 dp on 1080p).
        val dockTop = 409 * density
        val shelf = root.findViewById<View>(R.id.islamicShelf)
        val weather = root.findViewById<View>(R.id.weatherRow)
        shelf.viewTreeObserver.addOnPreDrawListener {
            val a = android.graphics.Rect(0, 0, weather.width, weather.height)
            (root as android.view.ViewGroup).offsetDescendantRectToMyCoords(weather, a)
            val gap = maxOf(18 * density, (dockTop - a.bottom - shelf.height) / 2f)
            shelf.y = minOf(a.bottom + gap, dockTop - 18 * density - shelf.height)
            true
        }
        // Stand-in dock tiles, so the spacing reads as on the TV.
        val dock = android.widget.LinearLayout(root.context)
        for (i in 0 until 6) {
            val tile = View(root.context)
            tile.background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 12 * density; setColor(0x22FFFFFF)
            }
            dock.addView(tile, android.widget.LinearLayout.LayoutParams(0, (68 * density).toInt(), 1f).apply {
                marginEnd = (14 * density).toInt()
            })
        }
        (root as android.widget.FrameLayout).addView(dock, android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT, (68 * density).toInt()).apply {
            topMargin = dockTop.toInt(); leftMargin = (56 * density).toInt(); rightMargin = (42 * density).toInt()
        })
        (root as android.widget.FrameLayout).addView(AmbientView(root.context), 0,
            android.widget.FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT))
    }

    /** A look on top of Malam, as Tenang.applyLook does, placed as MainActivity places it. */
    private fun look(overlay: Int, art: Int?, palette: Int = 0, name: String? = null) {
        paparazzi.context.theme.applyStyle(R.style.ThemeOverlay_Tenang_Malam, true)
        paparazzi.context.theme.applyStyle(overlay, true)
        if (palette != 0) paparazzi.context.theme.applyStyle(palette, true)
        Design.load(paparazzi.context)
        val view = LayoutInflater.from(paparazzi.context).inflate(R.layout.activity_main, null) as android.widget.FrameLayout
        fill(view)
        val d = view.resources.displayMetrics.density
        val dock = view.getChildAt(view.childCount - 1)
        val shelf = view.findViewById<View>(R.id.islamicShelf)
        if (art == null) {
            // Lautan: the list on the left (stand-in rows), the grid and dock hidden, the rail under the chips.
            LookType.apply(view)
            dock.visibility = View.GONE
            val list = LautanList(view.context, object : LautanList.Host { override fun onLeftFromList() {}; override fun onAppFocused(position: Int) {} })
            val rows = listOf("YouTube", "Netflix", "HBO Max", "Media Player", "Live TV", "Spotify")
                .map { LautanList.Shortcut(it, Runnable {}) } + listOf("INPUT", "CUACA", "SOLAT", "TEMA", "TETAPAN").map { LautanList.Shortcut(it, Runnable {}) }
            list.rebuild(null, rows.toTypedArray())
            view.addView(list, android.widget.FrameLayout.LayoutParams((maxOf(view.resources.displayMetrics.widthPixels, view.resources.displayMetrics.heightPixels) * 0.40f).toInt(), -1).apply {
                leftMargin = (40 * d).toInt(); topMargin = (30 * d).toInt() })
            val chips = view.findViewById<View>(R.id.weatherRow)
            shelf.viewTreeObserver.addOnPreDrawListener {
                val c = android.graphics.Rect(0, 0, chips.width, chips.height)
                view.offsetDescendantRectToMyCoords(chips, c); shelf.y = c.bottom + 18 * d; true }
        } else {
            val ambient = view.getChildAt(0) as AmbientView
            // As AppAdapter.spotColoursAt gives them: the app's main colour and a second one.
            ambient.setSpot(intArrayOf(art, AppAdapter.secondColor(null, art)), false)
            LookType.apply(view)
            // Sorot: strip under the chips, the app row, the hero, and the ayah beside the hero on the right.
            view.findViewById<TextView>(R.id.prayerStrip).text =
                "SETERUSNYA  Maghrib 19:01  ·  dalam 2j 16m      Subuh 05:46    Zohor 13:02    Asar 16:22    Isyak 20:13      1 Jamadilakhir 1448H · Shah Alam"
            (dock.layoutParams as android.widget.FrameLayout.LayoutParams).topMargin = (180 * d).toInt()
            view.findViewById<TextView>(R.id.sorotTitle).text = "Netflix"
            val hero = view.findViewById<View>(R.id.sorotHero)
            hero.y = (180 + 68 + 20) * d
            shelf.viewTreeObserver.addOnPreDrawListener { shelf.y = hero.y; true }
        }
        if (name != null) paparazzi.snapshot(view, name) else paparazzi.snapshot(view)
    }

    /** The daytime screensaver: the look's background under the clock (black at night, not rendered here). */
    private fun screensaver(overlay: Int, spot: Int? = null) {
        paparazzi.context.theme.applyStyle(R.style.ThemeOverlay_Tenang_Malam, true)
        if (overlay != 0) paparazzi.context.theme.applyStyle(overlay, true)
        Design.load(paparazzi.context)
        val view = LayoutInflater.from(paparazzi.context).inflate(R.layout.view_nightstand, null) as android.widget.FrameLayout
        val ambient = AmbientView(paparazzi.context)
        view.addView(ambient, 0, android.widget.FrameLayout.LayoutParams(-1, -1))
        if (spot != null) ambient.setSpot(intArrayOf(spot, AppAdapter.secondColor(null, spot)), false)
        paparazzi.snapshot(view)
    }

    @Test fun screensaverTenang() = screensaver(0)
    @Test fun screensaverLautan() = screensaver(R.style.ThemeOverlay_Look_Lautan)
    @Test fun screensaverSorot() = screensaver(R.style.ThemeOverlay_Look_Sorot, 0xFF22B573.toInt())

    @Test fun lookLautan() = look(R.style.ThemeOverlay_Look_Lautan, null)

    // Every palette of every look (docs/PALETTES.md), one render each.
    @Test fun paletteTenangSubuh() = home(R.style.ThemeOverlay_Tenang_Subuh)
    @Test fun paletteTenangHutan() = home(R.style.ThemeOverlay_Tenang_Hutan)
    @Test fun paletteTenangPasir() = home(R.style.ThemeOverlay_Tenang_Pasir)
    @Test fun paletteLautanKarang() = look(R.style.ThemeOverlay_Look_Lautan, null, R.style.ThemeOverlay_Look_Lautan_Karang)
    @Test fun paletteLautanBulan() = look(R.style.ThemeOverlay_Look_Lautan, null, R.style.ThemeOverlay_Look_Lautan_Bulan)
    @Test fun paletteLautanZamrud() = look(R.style.ThemeOverlay_Look_Lautan, null, R.style.ThemeOverlay_Look_Lautan_Zamrud)
    @Test fun paletteLautanSenjakala() = look(R.style.ThemeOverlay_Look_Lautan, null, R.style.ThemeOverlay_Look_Lautan_Senjakala)
    @Test fun paletteSorotArang() = look(R.style.ThemeOverlay_Look_Sorot, 0xFF1F6FD6.toInt(), R.style.ThemeOverlay_Look_Sorot_Arang)
    @Test fun paletteSorotHangat() = look(R.style.ThemeOverlay_Look_Sorot, 0xFFC8101E.toInt(), R.style.ThemeOverlay_Look_Sorot_Hangat)
    @Test fun paletteSorotPudina() = look(R.style.ThemeOverlay_Look_Sorot, 0xFF22B573.toInt(), R.style.ThemeOverlay_Look_Sorot_Pudina)
    @Test fun paletteSorotMawar() = look(R.style.ThemeOverlay_Look_Sorot, 0xFF6C63FF.toInt(), R.style.ThemeOverlay_Look_Sorot_Mawar)
    @Test fun lookSorot() = look(R.style.ThemeOverlay_Look_Sorot, 0xFFC8101E.toInt())

    @Test fun lookLautanMenu() {
        paparazzi.context.theme.applyStyle(R.style.ThemeOverlay_Tenang_Malam, true)
        paparazzi.context.theme.applyStyle(R.style.ThemeOverlay_Look_Lautan, true)
        Design.load(paparazzi.context)
        val view = LayoutInflater.from(paparazzi.context).inflate(R.layout.activity_main, null) as android.widget.FrameLayout
        fill(view)
        LookType.apply(view)
        val names = listOf("APLIKASI", "INPUT", "CUACA", "SOLAT", "TEMA", "PROFIL", "TETAPAN", "TUTUP")
        val menu = LautanMenu(paparazzi.context, names.map { LautanMenu.Entry(it, "Penerangan $it", Runnable {}) }.toTypedArray(), true)
        view.addView(menu, android.widget.FrameLayout.LayoutParams(-1, -1))
        menu.open()
        paparazzi.snapshot(view)
    }

    @Test fun homeMalam() = home(R.style.ThemeOverlay_Tenang_Malam)
    @Test fun homeSenja() = home(R.style.ThemeOverlay_Tenang_Senja)
    @Test fun homeEmbun() = home(R.style.ThemeOverlay_Tenang_Embun)
}
