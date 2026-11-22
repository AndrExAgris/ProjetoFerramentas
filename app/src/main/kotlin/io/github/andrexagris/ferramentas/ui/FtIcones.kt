// Gerado por ferramentas/gerar_icones.py (do app, sobre o gerador do Open Design) a partir dos SVG da Tabler Icons (licença MIT). Não edite à mão.
package io.github.andrexagris.ferramentas.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import io.github.andrexagris.opendesign.OdMedidas

private fun tabler(nome: String, cheio: Boolean, vararg caminhos: String): ImageVector {
    val b = ImageVector.Builder(name = nome, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
    for (d in caminhos) {
        if (cheio) b.addPath(pathData = addPathNodes(d), fill = SolidColor(Color.Black))
        else b.addPath(
            pathData = addPathNodes(d), stroke = SolidColor(Color.Black), strokeLineWidth = OdMedidas.espessuraIcone,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
        )
    }
    return b.build()
}

/** Ícones Tabler do app (os dos componentes estão em OdIcones). Use com Icon(FtIcones.Star, contentDescription = null): a cor vem do LocalContentColor. */
object FtIcones {
    val ActivityHeartbeat: ImageVector by lazy { tabler("activity-heartbeat", false, "M3 12h4.5l1.5 -6l4 12l2 -9l1.5 3h4.5") }
    val Alarm: ImageVector by lazy { tabler("alarm", false, "M5 13a7 7 0 1 0 14 0a7 7 0 1 0 -14 0", "M12 10l0 3l2 0", "M7 4l-2.75 2", "M17 4l2.75 2") }
    val Angle: ImageVector by lazy { tabler("angle", false, "M21 19h-18l9 -15", "M20.615 15.171h.015", "M19.515 11.771h.015", "M17.715 8.671h.015", "M15.415 5.971h.015") }
    val Antenna: ImageVector by lazy { tabler("antenna", false, "M20 4v8", "M16 4.5v7", "M12 5v16", "M8 5.5v5", "M4 6v4", "M20 8h-16") }
    val AntennaBars5: ImageVector by lazy { tabler("antenna-bars-5", false, "M6 18l0 -3", "M10 18l0 -6", "M14 18l0 -9", "M18 18l0 -12") }
    val Aperture: ImageVector by lazy { tabler("aperture", false, "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0", "M3.6 15h10.55", "M6.551 4.938l3.26 10.034", "M17.032 4.636l-8.535 6.201", "M20.559 14.51l-8.535 -6.201", "M12.257 20.916l3.261 -10.034") }
    val Apps: ImageVector by lazy { tabler("apps", false, "M4 5a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v4a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1l0 -4", "M4 15a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v4a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1l0 -4", "M14 15a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v4a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1l0 -4", "M14 7l6 0", "M17 4l0 6") }
    val ArrowsDownUp: ImageVector by lazy { tabler("arrows-down-up", false, "M17 3l0 18", "M10 18l-3 3l-3 -3", "M7 21l0 -18", "M20 6l-3 -3l-3 3") }
    val ArrowsExchange: ImageVector by lazy { tabler("arrows-exchange", false, "M7 10h14l-4 -4", "M17 14h-14l4 4") }
    val ArrowsMove: ImageVector by lazy { tabler("arrows-move", false, "M18 9l3 3l-3 3", "M15 12h6", "M6 9l-3 3l3 3", "M3 12h6", "M9 18l3 3l3 -3", "M12 15v6", "M15 6l-3 -3l-3 3", "M12 3v6") }
    val AugmentedReality: ImageVector by lazy { tabler("augmented-reality", false, "M4 8v-2a2 2 0 0 1 2 -2h2", "M4 16v2a2 2 0 0 0 2 2h2", "M16 4h2a2 2 0 0 1 2 2v2", "M16 20h2a2 2 0 0 0 2 -2v-2", "M12 12.5l4 -2.5", "M8 10l4 2.5v4.5l4 -2.5v-4.5l-4 -2.5l-4 2.5", "M8 10v4.5l4 2.5") }
    val Barbell: ImageVector by lazy { tabler("barbell", false, "M2 12h1", "M6 8h-2a1 1 0 0 0 -1 1v6a1 1 0 0 0 1 1h2", "M6 7v10a1 1 0 0 0 1 1h1a1 1 0 0 0 1 -1v-10a1 1 0 0 0 -1 -1h-1a1 1 0 0 0 -1 1", "M9 12h6", "M15 7v10a1 1 0 0 0 1 1h1a1 1 0 0 0 1 -1v-10a1 1 0 0 0 -1 -1h-1a1 1 0 0 0 -1 1", "M18 8h2a1 1 0 0 1 1 1v6a1 1 0 0 1 -1 1h-2", "M22 12h-1") }
    val Barcode: ImageVector by lazy { tabler("barcode", false, "M4 7v-1a2 2 0 0 1 2 -2h2", "M4 17v1a2 2 0 0 0 2 2h2", "M16 4h2a2 2 0 0 1 2 2v1", "M16 20h2a2 2 0 0 0 2 -2v-1", "M5 11h1v2h-1l0 -2", "M10 11l0 2", "M14 11h1v2h-1l0 -2", "M19 11l0 2") }
    val Battery2: ImageVector by lazy { tabler("battery-2", false, "M6 7h11a2 2 0 0 1 2 2v.5a.5 .5 0 0 0 .5 .5a.5 .5 0 0 1 .5 .5v3a.5 .5 0 0 1 -.5 .5a.5 .5 0 0 0 -.5 .5v.5a2 2 0 0 1 -2 2h-11a2 2 0 0 1 -2 -2v-6a2 2 0 0 1 2 -2", "M7 10l0 4", "M10 10l0 4") }
    val BatteryCharging: ImageVector by lazy { tabler("battery-charging", false, "M16 7h1a2 2 0 0 1 2 2v.5a.5 .5 0 0 0 .5 .5a.5 .5 0 0 1 .5 .5v3a.5 .5 0 0 1 -.5 .5a.5 .5 0 0 0 -.5 .5v.5a2 2 0 0 1 -2 2h-2", "M8 7h-2a2 2 0 0 0 -2 2v6a2 2 0 0 0 2 2h1", "M12 8l-2 4h3l-2 4") }
    val Binary: ImageVector by lazy { tabler("binary", false, "M11 10v-5h-1m8 14v-5h-1", "M15 5.5a.5 .5 0 0 1 .5 -.5h2a.5 .5 0 0 1 .5 .5v4a.5 .5 0 0 1 -.5 .5h-2a.5 .5 0 0 1 -.5 -.5l0 -4", "M10 14.5a.5 .5 0 0 1 .5 -.5h2a.5 .5 0 0 1 .5 .5v4a.5 .5 0 0 1 -.5 .5h-2a.5 .5 0 0 1 -.5 -.5l0 -4", "M6 10h.01m-.01 9h.01") }
    val Bluetooth: ImageVector by lazy { tabler("bluetooth", false, "M7 8l10 8l-5 4l0 -16l5 4l-10 8") }
    val BluetoothConnected: ImageVector by lazy { tabler("bluetooth-connected", false, "M7 8l10 8l-5 4l0 -16l5 4l-10 8", "M4 12l1 0", "M18 12l1 0") }
    val Bolt: ImageVector by lazy { tabler("bolt", false, "M13 3l0 7l6 0l-8 11l0 -7l-6 0l8 -11") }
    val BrandSpeedtest: ImageVector by lazy { tabler("brand-speedtest", false, "M5.636 19.364a9 9 0 1 1 12.728 0", "M16 9l-4 4") }
    val Bubble: ImageVector by lazy { tabler("bubble", false, "M12.4 3a5.34 5.34 0 0 1 4.906 3.239a5.333 5.333 0 0 1 -1.195 10.6a4.26 4.26 0 0 1 -5.28 1.863l-3.831 2.298v-3.134a2.668 2.668 0 0 1 -1.795 -3.773a4.8 4.8 0 0 1 2.908 -8.933a5.33 5.33 0 0 1 4.287 -2.16") }
    val BuildingSkyscraper: ImageVector by lazy { tabler("building-skyscraper", false, "M3 21l18 0", "M5 21v-14l8 -4v18", "M19 21v-10l-6 -4", "M9 9l0 .01", "M9 12l0 .01", "M9 15l0 .01", "M9 18l0 .01") }
    val Bulb: ImageVector by lazy { tabler("bulb", false, "M3 12h1m8 -9v1m8 8h1m-15.4 -6.4l.7 .7m12.1 -.7l-.7 .7", "M9 16a5 5 0 1 1 6 0a3.5 3.5 0 0 0 -1 3a2 2 0 0 1 -4 0a3.5 3.5 0 0 0 -1 -3", "M9.7 17l4.6 0") }
    val Calculator: ImageVector by lazy { tabler("calculator", false, "M4 5a2 2 0 0 1 2 -2h12a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-12a2 2 0 0 1 -2 -2l0 -14", "M8 8a1 1 0 0 1 1 -1h6a1 1 0 0 1 1 1v1a1 1 0 0 1 -1 1h-6a1 1 0 0 1 -1 -1l0 -1", "M8 14l0 .01", "M12 14l0 .01", "M16 14l0 .01", "M8 17l0 .01", "M12 17l0 .01", "M16 17l0 .01") }
    val Camera: ImageVector by lazy { tabler("camera", false, "M5 7h1a2 2 0 0 0 2 -2a1 1 0 0 1 1 -1h6a1 1 0 0 1 1 1a2 2 0 0 0 2 2h1a2 2 0 0 1 2 2v9a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-9a2 2 0 0 1 2 -2", "M9 13a3 3 0 1 0 6 0a3 3 0 0 0 -6 0") }
    val CameraRotate: ImageVector by lazy { tabler("camera-rotate", false, "M5 7h1a2 2 0 0 0 2 -2a1 1 0 0 1 1 -1h6a1 1 0 0 1 1 1a2 2 0 0 0 2 2h1a2 2 0 0 1 2 2v9a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-9a2 2 0 0 1 2 -2", "M11.245 15.904a3 3 0 0 0 3.755 -2.904m-2.25 -2.905a3 3 0 0 0 -3.75 2.905", "M14 13h2v2", "M10 13h-2v-2") }
    val Cards: ImageVector by lazy { tabler("cards", false, "M3.604 7.197l7.138 -3.109a.96 .96 0 0 1 1.27 .527l4.924 11.902a1 1 0 0 1 -.514 1.304l-7.137 3.109a.96 .96 0 0 1 -1.271 -.527l-4.924 -11.903a1 1 0 0 1 .514 -1.304l0 .001", "M15 4h1a1 1 0 0 1 1 1v3.5", "M20 6c.264 .112 .52 .217 .768 .315a1 1 0 0 1 .53 1.311l-2.298 5.374") }
    val Certificate: ImageVector by lazy { tabler("certificate", false, "M12 15a3 3 0 1 0 6 0a3 3 0 1 0 -6 0", "M13 17.5v4.5l2 -1.5l2 1.5v-4.5", "M10 19h-5a2 2 0 0 1 -2 -2v-10c0 -1.1 .9 -2 2 -2h14a2 2 0 0 1 2 2v10a2 2 0 0 1 -1 1.73", "M6 9l12 0", "M6 12l3 0", "M6 15l2 0") }
    val ChartBar: ImageVector by lazy { tabler("chart-bar", false, "M3 13a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v6a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1l0 -6", "M15 9a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v10a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1l0 -10", "M9 5a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v14a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1l0 -14", "M4 20h14") }
    val ChartLine: ImageVector by lazy { tabler("chart-line", false, "M4 19l16 0", "M4 15l4 -6l4 2l4 -5l4 4") }
    val ChartPie: ImageVector by lazy { tabler("chart-pie", false, "M10 3.2a9 9 0 1 0 10.8 10.8a1 1 0 0 0 -1 -1h-6.8a2 2 0 0 1 -2 -2v-7a.9 .9 0 0 0 -1 -.8", "M15 3.5a9 9 0 0 1 5.5 5.5h-4.5a1 1 0 0 1 -1 -1v-4.5") }
    val CircleDashed: ImageVector by lazy { tabler("circle-dashed", false, "M8.56 3.69a9 9 0 0 0 -2.92 1.95", "M3.69 8.56a9 9 0 0 0 -.69 3.44", "M3.69 15.44a9 9 0 0 0 1.95 2.92", "M8.56 20.31a9 9 0 0 0 3.44 .69", "M15.44 20.31a9 9 0 0 0 2.92 -1.95", "M20.31 15.44a9 9 0 0 0 .69 -3.44", "M20.31 8.56a9 9 0 0 0 -1.95 -2.92", "M15.44 3.69a9 9 0 0 0 -3.44 -.69") }
    val ClockHour3: ImageVector by lazy { tabler("clock-hour-3", false, "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0", "M12 12h3.5", "M12 7v5") }
    val ClockPlay: ImageVector by lazy { tabler("clock-play", false, "M12 7v5l2 2", "M17 22l5 -3l-5 -3l0 6", "M13.017 20.943a9 9 0 1 1 7.831 -7.292") }
    val CloudRain: ImageVector by lazy { tabler("cloud-rain", false, "M7 18a4.6 4.4 0 0 1 0 -9a5 4.5 0 0 1 11 2h1a3.5 3.5 0 0 1 0 7", "M11 13v2m0 3v2m4 -5v2m0 3v2") }
    val ColorPicker: ImageVector by lazy { tabler("color-picker", false, "M11 7l6 6", "M4 16l11.7 -11.7a1 1 0 0 1 1.4 0l2.6 2.6a1 1 0 0 1 0 1.4l-11.7 11.7h-4v-4") }
    val Compass: ImageVector by lazy { tabler("compass", false, "M8 16l2 -6l6 -2l-2 6l-6 2", "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0", "M12 3l0 2", "M12 19l0 2", "M3 12l2 0", "M19 12l2 0") }
    val Copy: ImageVector by lazy { tabler("copy", false, "M7 9.667a2.667 2.667 0 0 1 2.667 -2.667h8.666a2.667 2.667 0 0 1 2.667 2.667v8.666a2.667 2.667 0 0 1 -2.667 2.667h-8.666a2.667 2.667 0 0 1 -2.667 -2.667l0 -8.666", "M4.012 16.737a2.005 2.005 0 0 1 -1.012 -1.737v-10c0 -1.1 .9 -2 2 -2h10c.75 0 1.158 .385 1.5 1") }
    val Cpu: ImageVector by lazy { tabler("cpu", false, "M5 6a1 1 0 0 1 1 -1h12a1 1 0 0 1 1 1v12a1 1 0 0 1 -1 1h-12a1 1 0 0 1 -1 -1l0 -12", "M9 9h6v6h-6l0 -6", "M3 10h2", "M3 14h2", "M10 3v2", "M14 3v2", "M21 10h-2", "M21 14h-2", "M14 21v-2", "M10 21v-2") }
    val CreditCard: ImageVector by lazy { tabler("credit-card", false, "M3 8a3 3 0 0 1 3 -3h12a3 3 0 0 1 3 3v8a3 3 0 0 1 -3 3h-12a3 3 0 0 1 -3 -3l0 -8", "M3 10l18 0", "M7 15l.01 0", "M11 15l2 0") }
    val Cube3dSphere: ImageVector by lazy { tabler("cube-3d-sphere", false, "M6 17.6l-2 -1.1v-2.5", "M4 10v-2.5l2 -1.1", "M10 4.1l2 -1.1l2 1.1", "M18 6.4l2 1.1v2.5", "M20 14v2.5l-2 1.12", "M14 19.9l-2 1.1l-2 -1.1", "M12 12l2 -1.1", "M18 8.6l2 -1.1", "M12 12l0 2.5", "M12 18.5l0 2.5", "M12 12l-2 -1.12", "M6 8.6l-2 -1.1") }
    val Database: ImageVector by lazy { tabler("database", false, "M4 6a8 3 0 1 0 16 0a8 3 0 1 0 -16 0", "M4 6v6a8 3 0 0 0 16 0v-6", "M4 12v6a8 3 0 0 0 16 0v-6") }
    val DeviceAirpods: ImageVector by lazy { tabler("device-airpods", false, "M6 4a4 4 0 0 1 4 3.8l0 .2v10.5a1.5 1.5 0 0 1 -3 0v-6.5h-1a4 4 0 0 1 -4 -3.8l0 -.2a4 4 0 0 1 4 -4", "M18 4a4 4 0 0 0 -4 3.8l0 .2v10.5a1.5 1.5 0 0 0 3 0v-6.5h1a4 4 0 0 0 4 -3.8l0 -.2a4 4 0 0 0 -4 -4") }
    val DeviceMobile: ImageVector by lazy { tabler("device-mobile", false, "M6 5a2 2 0 0 1 2 -2h8a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-8a2 2 0 0 1 -2 -2v-14", "M11 4h2", "M12 17v.01") }
    val DeviceMobileVibration: ImageVector by lazy { tabler("device-mobile-vibration", false, "M3 5a2 2 0 0 1 2 -2h8a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-8a2 2 0 0 1 -2 -2l0 -14", "M8 4l2 0", "M9 17l0 .01", "M21 6l-2 3l2 3l-2 3l2 3") }
    val DeviceRemote: ImageVector by lazy { tabler("device-remote", false, "M10 10a2 2 0 1 0 4 0a2 2 0 1 0 -4 0", "M7 5a2 2 0 0 1 2 -2h6a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-6a2 2 0 0 1 -2 -2l0 -14", "M12 3v2", "M10 15v.01", "M10 18v.01", "M14 18v.01", "M14 15v.01") }
    val DevicesSearch: ImageVector by lazy { tabler("devices-search", false, "M13 13v-4a1 1 0 0 1 1 -1h6a1 1 0 0 1 1 1v2.5", "M18 8v-3a1 1 0 0 0 -1 -1h-13a1 1 0 0 0 -1 1v12a1 1 0 0 0 1 1h7", "M15 18a3 3 0 1 0 6 0a3 3 0 1 0 -6 0", "M20.2 20.2l1.8 1.8", "M16 9h2") }
    val Droplet: ImageVector by lazy { tabler("droplet", false, "M7.502 19.423c2.602 2.105 6.395 2.105 8.996 0c2.602 -2.105 3.262 -5.708 1.566 -8.546l-4.89 -7.26c-.42 -.625 -1.287 -.803 -1.936 -.397a1.376 1.376 0 0 0 -.41 .397l-4.893 7.26c-1.695 2.838 -1.035 6.441 1.567 8.546") }
    val Ear: ImageVector by lazy { tabler("ear", false, "M6 10a7 7 0 1 1 13 3.6a10 10 0 0 1 -2 2a8 8 0 0 0 -2 3a4.5 4.5 0 0 1 -6.8 1.4", "M10 10a3 3 0 1 1 5 2.2") }
    val Eraser: ImageVector by lazy { tabler("eraser", false, "M19 20h-10.5l-4.21 -4.3a1 1 0 0 1 0 -1.41l10 -10a1 1 0 0 1 1.41 0l5 5a1 1 0 0 1 0 1.41l-9.2 9.3", "M18 13.3l-6.3 -6.3") }
    val FileSpreadsheet: ImageVector by lazy { tabler("file-spreadsheet", false, "M14 3v4a1 1 0 0 0 1 1h4", "M17 21h-10a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2h7l5 5v11a2 2 0 0 1 -2 2", "M8 11h8v7h-8l0 -7", "M8 15h8", "M11 11v7") }
    val FileText: ImageVector by lazy { tabler("file-text", false, "M14 3v4a1 1 0 0 0 1 1h4", "M17 21h-10a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2h7l5 5v11a2 2 0 0 1 -2 2", "M9 9l1 0", "M9 13l6 0", "M9 17l6 0") }
    val FileTypePdf: ImageVector by lazy { tabler("file-type-pdf", false, "M14 3v4a1 1 0 0 0 1 1h4", "M5 12v-7a2 2 0 0 1 2 -2h7l5 5v4", "M5 18h1.5a1.5 1.5 0 0 0 0 -3h-1.5v6", "M17 18h2", "M20 15h-3v6", "M11 15v6h1a2 2 0 0 0 2 -2v-2a2 2 0 0 0 -2 -2h-1") }
    val Filter: ImageVector by lazy { tabler("filter", false, "M4 4h16v2.172a2 2 0 0 1 -.586 1.414l-4.414 4.414v7l-6 2v-8.5l-4.48 -4.928a2 2 0 0 1 -.52 -1.345v-2.227") }
    val Fingerprint: ImageVector by lazy { tabler("fingerprint", false, "M18.9 7a8 8 0 0 1 1.1 5v1a6 6 0 0 0 .8 3", "M8 11a4 4 0 0 1 8 0v1a10 10 0 0 0 2 6", "M12 11v2a14 14 0 0 0 2.5 8", "M8 15a18 18 0 0 0 1.8 6", "M4.9 19a22 22 0 0 1 -.9 -7v-1a8 8 0 0 1 12 -6.95") }
    val Flag: ImageVector by lazy { tabler("flag", false, "M5 5a5 5 0 0 1 7 0a5 5 0 0 0 7 0v9a5 5 0 0 1 -7 0a5 5 0 0 0 -7 0v-9", "M5 21v-7") }
    val Flame: ImageVector by lazy { tabler("flame", false, "M12 10.941c2.333 -3.308 .167 -7.823 -1 -8.941c0 3.395 -2.235 5.299 -3.667 6.706c-1.43 1.408 -2.333 3.294 -2.333 5.588c0 3.704 3.134 6.706 7 6.706c3.866 0 7 -3.002 7 -6.706c0 -1.712 -1.232 -4.403 -2.333 -5.588c-2.084 3.353 -3.257 3.353 -4.667 2.235") }
    val Folders: ImageVector by lazy { tabler("folders", false, "M9 3h3l2 2h5a2 2 0 0 1 2 2v7a2 2 0 0 1 -2 2h-10a2 2 0 0 1 -2 -2v-9a2 2 0 0 1 2 -2", "M17 16v2a2 2 0 0 1 -2 2h-10a2 2 0 0 1 -2 -2v-9a2 2 0 0 1 2 -2h2") }
    val Gauge: ImageVector by lazy { tabler("gauge", false, "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0", "M11 12a1 1 0 1 0 2 0a1 1 0 1 0 -2 0", "M13.41 10.59l2.59 -2.59", "M7 12a5 5 0 0 1 5 -5") }
    val GuitarPick: ImageVector by lazy { tabler("guitar-pick", false, "M16 18.5c2 -2.5 4 -6.5 4 -10.5c0 -2.946 -2.084 -4.157 -4.204 -4.654c-.864 -.23 -2.13 -.346 -3.796 -.346c-1.667 0 -2.932 .115 -3.796 .346c-2.12 .497 -4.204 1.708 -4.204 4.654c0 3.312 2 8 4 10.5c.297 .37 .618 .731 .963 1.081l.354 .347a3.9 3.9 0 0 0 5.364 0a14.05 14.05 0 0 0 1.319 -1.428") }
    val HandClick: ImageVector by lazy { tabler("hand-click", false, "M8 13v-8.5a1.5 1.5 0 0 1 3 0v7.5", "M11 11.5v-2a1.5 1.5 0 0 1 3 0v2.5", "M14 10.5a1.5 1.5 0 0 1 3 0v1.5", "M17 11.5a1.5 1.5 0 0 1 3 0v4.5a6 6 0 0 1 -6 6h-2h.208a6 6 0 0 1 -5.012 -2.7l-.196 -.3c-.312 -.479 -1.407 -2.388 -3.286 -5.728a1.5 1.5 0 0 1 .536 -2.022a1.867 1.867 0 0 1 2.28 .28l1.47 1.47", "M5 3l-1 -1", "M4 7h-1", "M14 3l1 -1", "M15 6h1") }
    val HandFinger: ImageVector by lazy { tabler("hand-finger", false, "M8 13v-8.5a1.5 1.5 0 0 1 3 0v7.5", "M11 11.5v-2a1.5 1.5 0 1 1 3 0v2.5", "M14 10.5a1.5 1.5 0 0 1 3 0v1.5", "M17 11.5a1.5 1.5 0 0 1 3 0v4.5a6 6 0 0 1 -6 6h-2h.208a6 6 0 0 1 -5.012 -2.7a69.74 69.74 0 0 1 -.196 -.3c-.312 -.479 -1.407 -2.388 -3.286 -5.728a1.5 1.5 0 0 1 .536 -2.022a1.867 1.867 0 0 1 2.28 .28l1.47 1.47") }
    val Haze: ImageVector by lazy { tabler("haze", false, "M3 12h1", "M12 3v1", "M20 12h1", "M5.6 5.6l.7 .7", "M18.4 5.6l-.7 .7", "M8 12a4 4 0 1 1 8 0", "M3 16h18", "M3 20h18") }
    val Heart: ImageVector by lazy { tabler("heart", false, "M19.5 12.572l-7.5 7.428l-7.5 -7.428a5 5 0 1 1 7.5 -6.566a5 5 0 1 1 7.5 6.572") }
    val HeartRateMonitor: ImageVector by lazy { tabler("heart-rate-monitor", false, "M3 5a1 1 0 0 1 1 -1h16a1 1 0 0 1 1 1v10a1 1 0 0 1 -1 1h-16a1 1 0 0 1 -1 -1l0 -10", "M7 20h10", "M9 16v4", "M15 16v4", "M7 10h2l2 3l2 -6l1 3h3") }
    val Heartbeat: ImageVector by lazy { tabler("heartbeat", false, "M19.5 13.572l-7.5 7.428l-2.896 -2.868m-6.117 -8.104a5 5 0 0 1 9.013 -3.022a5 5 0 1 1 7.5 6.572", "M3 13h2l2 3l2 -6l1 3h3") }
    val Hourglass: ImageVector by lazy { tabler("hourglass", false, "M6.5 7h11", "M6.5 17h11", "M6 20v-2a6 6 0 1 1 12 0v2a1 1 0 0 1 -1 1h-10a1 1 0 0 1 -1 -1", "M6 4v2a6 6 0 1 0 12 0v-2a1 1 0 0 0 -1 -1h-10a1 1 0 0 0 -1 1") }
    val IdBadge: ImageVector by lazy { tabler("id-badge", false, "M5 6a3 3 0 0 1 3 -3h8a3 3 0 0 1 3 3v12a3 3 0 0 1 -3 3h-8a3 3 0 0 1 -3 -3l0 -12", "M10 13a2 2 0 1 0 4 0a2 2 0 1 0 -4 0", "M10 6h4", "M9 18h6") }
    val Key: ImageVector by lazy { tabler("key", false, "M16.555 3.843l3.602 3.602a2.877 2.877 0 0 1 0 4.069l-2.643 2.643a2.877 2.877 0 0 1 -4.069 0l-.301 -.301l-6.558 6.558a2 2 0 0 1 -1.239 .578l-.175 .008h-1.172a1 1 0 0 1 -.993 -.883l-.007 -.117v-1.172a2 2 0 0 1 .467 -1.284l.119 -.13l.414 -.414h2v-2h2v-2l2.144 -2.144l-.301 -.301a2.877 2.877 0 0 1 0 -4.069l2.643 -2.643a2.877 2.877 0 0 1 4.069 0", "M15 9h.01") }
    val License: ImageVector by lazy { tabler("license", false, "M15 21h-9a3 3 0 0 1 -3 -3v-1h10v2a2 2 0 0 0 4 0v-14a2 2 0 1 1 2 2h-2m2 -4h-11a3 3 0 0 0 -3 3v11", "M9 7l4 0", "M9 11l4 0") }
    val ListDetails: ImageVector by lazy { tabler("list-details", false, "M13 5h8", "M13 9h5", "M13 15h8", "M13 19h5", "M3 5a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v4a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1l0 -4", "M3 15a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v4a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1l0 -4") }
    val LockOpen: ImageVector by lazy { tabler("lock-open", false, "M5 13a2 2 0 0 1 2 -2h10a2 2 0 0 1 2 2v6a2 2 0 0 1 -2 2h-10a2 2 0 0 1 -2 -2l0 -6", "M11 16a1 1 0 1 0 2 0a1 1 0 1 0 -2 0", "M8 11v-5a4 4 0 0 1 8 0") }
    val Magnet: ImageVector by lazy { tabler("magnet", false, "M4 13v-8a2 2 0 0 1 2 -2h1a2 2 0 0 1 2 2v8a2 2 0 0 0 6 0v-8a2 2 0 0 1 2 -2h1a2 2 0 0 1 2 2v8a8 8 0 0 1 -16 0", "M4 8l5 0", "M15 8l4 0") }
    val MapPin: ImageVector by lazy { tabler("map-pin", false, "M9 11a3 3 0 1 0 6 0a3 3 0 0 0 -6 0", "M17.657 16.657l-4.243 4.243a2 2 0 0 1 -2.827 0l-4.244 -4.243a8 8 0 1 1 11.314 0") }
    val MapPinSearch: ImageVector by lazy { tabler("map-pin-search", false, "M14.916 11.707a3 3 0 1 0 -2.916 2.293", "M11.991 21.485a1.994 1.994 0 0 1 -1.404 -.585l-4.244 -4.243a8 8 0 1 1 13.651 -5.351", "M15 18a3 3 0 1 0 6 0a3 3 0 1 0 -6 0", "M20.2 20.2l1.8 1.8") }
    val Metronome: ImageVector by lazy { tabler("metronome", false, "M14.153 8.188l-.72 -3.236a2.493 2.493 0 0 0 -4.867 0l-3.025 13.614a2 2 0 0 0 1.952 2.434h7.014a2 2 0 0 0 1.952 -2.434l-.524 -2.357m-4.935 1.791l9 -13", "M19 5a1 1 0 1 0 2 0a1 1 0 1 0 -2 0") }
    val Moon: ImageVector by lazy { tabler("moon", false, "M12 3c.132 0 .263 0 .393 0a7.5 7.5 0 0 0 7.92 12.446a9 9 0 1 1 -8.313 -12.454l0 .008") }
    val Mountain: ImageVector by lazy { tabler("mountain", false, "M3 20h18l-6.921 -14.612a2.3 2.3 0 0 0 -4.158 0l-6.921 14.612", "M7.5 11l2 2.5l2.5 -2.5l2 3l2.5 -2") }
    val Music: ImageVector by lazy { tabler("music", false, "M3 17a3 3 0 1 0 6 0a3 3 0 0 0 -6 0", "M13 17a3 3 0 1 0 6 0a3 3 0 0 0 -6 0", "M9 17v-13h10v13", "M9 8h10") }
    val Network: ImageVector by lazy { tabler("network", false, "M6 9a6 6 0 1 0 12 0a6 6 0 0 0 -12 0", "M12 3c1.333 .333 2 2.333 2 6s-.667 5.667 -2 6", "M12 3c-1.333 .333 -2 2.333 -2 6s.667 5.667 2 6", "M6 9h12", "M3 20h7", "M14 20h7", "M10 20a2 2 0 1 0 4 0a2 2 0 0 0 -4 0", "M12 15v3") }
    val Nfc: ImageVector by lazy { tabler("nfc", false, "M11 20a3 3 0 0 1 -3 -3v-11l5 5", "M13 4a3 3 0 0 1 3 3v11l-5 -5", "M4 7a3 3 0 0 1 3 -3h10a3 3 0 0 1 3 3v10a3 3 0 0 1 -3 3h-10a3 3 0 0 1 -3 -3l0 -10") }
    val Package: ImageVector by lazy { tabler("package", false, "M12 3l8 4.5l0 9l-8 4.5l-8 -4.5l0 -9l8 -4.5", "M12 12l8 -4.5", "M12 12l0 9", "M12 12l-8 -4.5", "M16 5.25l-8 4.5") }
    val PlugConnected: ImageVector by lazy { tabler("plug-connected", false, "M7 12l5 5l-1.5 1.5a3.536 3.536 0 1 1 -5 -5l1.5 -1.5", "M17 12l-5 -5l1.5 -1.5a3.536 3.536 0 1 1 5 5l-1.5 1.5", "M3 21l2.5 -2.5", "M18.5 5.5l2.5 -2.5", "M10 11l-2 2", "M13 14l-2 2") }
    val Polygon: ImageVector by lazy { tabler("polygon", false, "M10 5a2 2 0 1 0 4 0a2 2 0 1 0 -4 0", "M17 8a2 2 0 1 0 4 0a2 2 0 1 0 -4 0", "M3 11a2 2 0 1 0 4 0a2 2 0 1 0 -4 0", "M13 19a2 2 0 1 0 4 0a2 2 0 1 0 -4 0", "M6.5 9.5l3.5 -3", "M14 5.5l3 1.5", "M18.5 10l-2.5 7", "M13.5 17.5l-7 -5") }
    val Power: ImageVector by lazy { tabler("power", false, "M7 6a7.75 7.75 0 1 0 10 0", "M12 4l0 8") }
    val Printer: ImageVector by lazy { tabler("printer", false, "M17 17h2a2 2 0 0 0 2 -2v-4a2 2 0 0 0 -2 -2h-14a2 2 0 0 0 -2 2v4a2 2 0 0 0 2 2h2", "M17 9v-4a2 2 0 0 0 -2 -2h-6a2 2 0 0 0 -2 2v4", "M7 15a2 2 0 0 1 2 -2h6a2 2 0 0 1 2 2v4a2 2 0 0 1 -2 2h-6a2 2 0 0 1 -2 -2l0 -4") }
    val Qrcode: ImageVector by lazy { tabler("qrcode", false, "M4 5a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v4a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1l0 -4", "M7 17l0 .01", "M14 5a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v4a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1l0 -4", "M7 7l0 .01", "M4 15a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v4a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1l0 -4", "M17 7l0 .01", "M14 14l3 0", "M20 14l0 .01", "M14 14l0 3", "M14 20l3 0", "M17 17l3 0", "M20 17l0 3") }
    val Radar: ImageVector by lazy { tabler("radar", false, "M21 12h-8a1 1 0 1 0 -1 1v8a9 9 0 0 0 9 -9", "M16 9a5 5 0 1 0 -7 7", "M20.486 9a9 9 0 1 0 -11.482 11.495") }
    val Radar2: ImageVector by lazy { tabler("radar-2", false, "M11 12a1 1 0 1 0 2 0a1 1 0 1 0 -2 0", "M15.51 15.56a5 5 0 1 0 -3.51 1.44", "M18.832 17.86a9 9 0 1 0 -6.832 3.14", "M12 12v9") }
    val Refresh: ImageVector by lazy { tabler("refresh", false, "M20 11a8.1 8.1 0 0 0 -15.5 -2m-.5 -4v4h4", "M4 13a8.1 8.1 0 0 0 15.5 2m.5 4v-4h-4") }
    val Ripple: ImageVector by lazy { tabler("ripple", false, "M3 7c3 -2 6 -2 9 0s6 2 9 0", "M3 17c3 -2 6 -2 9 0s6 2 9 0", "M3 12c3 -2 6 -2 9 0s6 2 9 0") }
    val RotateClockwise2: ImageVector by lazy { tabler("rotate-clockwise-2", false, "M9 4.55a8 8 0 0 1 6 14.9m0 -4.45v5h5", "M5.63 7.16l0 .01", "M4.06 11l0 .01", "M4.63 15.1l0 .01", "M7.16 18.37l0 .01", "M11 19.94l0 .01") }
    val Route: ImageVector by lazy { tabler("route", false, "M3 19a2 2 0 1 0 4 0a2 2 0 0 0 -4 0", "M19 7a2 2 0 1 0 0 -4a2 2 0 0 0 0 4", "M11 19h5.5a3.5 3.5 0 0 0 0 -7h-8a3.5 3.5 0 0 1 0 -7h4.5") }
    val Route2: ImageVector by lazy { tabler("route-2", false, "M3 19a2 2 0 1 0 4 0a2 2 0 0 0 -4 0", "M19 7a2 2 0 1 0 0 -4a2 2 0 0 0 0 4", "M14 5a2 2 0 0 0 -2 2v10a2 2 0 0 1 -2 2") }
    val Router: ImageVector by lazy { tabler("router", false, "M3 15a2 2 0 0 1 2 -2h14a2 2 0 0 1 2 2v4a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2l0 -4", "M17 17l0 .01", "M13 17l0 .01", "M15 13l0 -2", "M11.75 8.75a4 4 0 0 1 6.5 0", "M8.5 6.5a8 8 0 0 1 13 0") }
    val Ruler: ImageVector by lazy { tabler("ruler", false, "M5 4h14a1 1 0 0 1 1 1v5a1 1 0 0 1 -1 1h-7a1 1 0 0 0 -1 1v7a1 1 0 0 1 -1 1h-5a1 1 0 0 1 -1 -1v-14a1 1 0 0 1 1 -1", "M4 8l2 0", "M4 12l3 0", "M4 16l2 0", "M8 4l0 2", "M12 4l0 3", "M16 4l0 2") }
    val Ruler2: ImageVector by lazy { tabler("ruler-2", false, "M17 3l4 4l-14 14l-4 -4l14 -14", "M16 7l-1.5 -1.5", "M13 10l-1.5 -1.5", "M10 13l-1.5 -1.5", "M7 16l-1.5 -1.5") }
    val Ruler3: ImageVector by lazy { tabler("ruler-3", false, "M19.875 8c.621 0 1.125 .512 1.125 1.143v5.714c0 .631 -.504 1.143 -1.125 1.143h-15.875a1 1 0 0 1 -1 -1v-5.857c0 -.631 .504 -1.143 1.125 -1.143h15.75", "M9 8v2", "M6 8v3", "M12 8v3", "M18 8v3", "M15 8v2") }
    val RulerMeasure: ImageVector by lazy { tabler("ruler-measure", false, "M19.875 12c.621 0 1.125 .512 1.125 1.143v5.714c0 .631 -.504 1.143 -1.125 1.143h-15.875a1 1 0 0 1 -1 -1v-5.857c0 -.631 .504 -1.143 1.125 -1.143h15.75", "M9 12v2", "M6 12v3", "M12 12v3", "M18 12v3", "M15 12v2", "M3 3v4", "M3 5h18", "M21 3v4") }
    val RulerMeasure2: ImageVector by lazy { tabler("ruler-measure-2", false, "M12 19.875c0 .621 -.512 1.125 -1.143 1.125h-5.714a1.134 1.134 0 0 1 -1.143 -1.125v-15.875a1 1 0 0 1 1 -1h5.857c.631 0 1.143 .504 1.143 1.125l0 15.75", "M12 9h-2", "M12 6h-3", "M12 12h-3", "M12 18h-3", "M12 15h-2", "M21 3h-4", "M19 3v18", "M21 21h-4") }
    val Run: ImageVector by lazy { tabler("run", false, "M11.007 5a2 2 0 1 0 4 0a2 2 0 1 0 -4 0", "M4 17l5 1l.75 -1.5", "M15 21v-4l-4 -3l1 -6", "M7 12v-3l5 -1l3 3l3 1") }
    val Satellite: ImageVector by lazy { tabler("satellite", false, "M3.707 6.293l2.586 -2.586a1 1 0 0 1 1.414 0l5.586 5.586a1 1 0 0 1 0 1.414l-2.586 2.586a1 1 0 0 1 -1.414 0l-5.586 -5.586a1 1 0 0 1 0 -1.414", "M6 10l-3 3l3 3l3 -3", "M10 6l3 -3l3 3l-3 3", "M12 12l1.5 1.5", "M14.5 17a2.5 2.5 0 0 0 2.5 -2.5", "M15 21a6 6 0 0 0 6 -6") }
    val Scan: ImageVector by lazy { tabler("scan", false, "M5 12h14", "M3 7v-2a2 2 0 0 1 2 -2h2", "M3 17v2a2 2 0 0 0 2 2h2", "M17 3h2a2 2 0 0 1 2 2v2", "M17 21h2a2 2 0 0 0 2 -2v-2") }
    val Search: ImageVector by lazy { tabler("search", false, "M3 10a7 7 0 1 0 14 0a7 7 0 1 0 -14 0", "M21 21l-6 -6") }
    val SearchOff: ImageVector by lazy { tabler("search-off", false, "M5.039 5.062a7 7 0 0 0 9.91 9.89m1.584 -2.434a7 7 0 0 0 -9.038 -9.057", "M3 3l18 18") }
    val ServerCog: ImageVector by lazy { tabler("server-cog", false, "M3 7a3 3 0 0 1 3 -3h12a3 3 0 0 1 3 3v2a3 3 0 0 1 -3 3h-12a3 3 0 0 1 -3 -3v-2", "M12 20h-6a3 3 0 0 1 -3 -3v-2a3 3 0 0 1 3 -3h10.5", "M16 18a2 2 0 1 0 4 0a2 2 0 1 0 -4 0", "M18 14.5v1.5", "M18 20v1.5", "M21.032 16.25l-1.299 .75", "M16.27 19l-1.3 .75", "M14.97 16.25l1.3 .75", "M19.733 19l1.3 .75", "M7 8v.01", "M7 16v.01") }
    val Speakerphone: ImageVector by lazy { tabler("speakerphone", false, "M18 8a3 3 0 0 1 0 6", "M10 8v11a1 1 0 0 1 -1 1h-1a1 1 0 0 1 -1 -1v-5", "M12 8l4.524 -3.77a.9 .9 0 0 1 1.476 .692v12.156a.9 .9 0 0 1 -1.476 .692l-4.524 -3.77h-8a1 1 0 0 1 -1 -1v-4a1 1 0 0 1 1 -1h8") }
    val StairsUp: ImageVector by lazy { tabler("stairs-up", false, "M22 6h-5v5h-5v5h-5v5h-5", "M6 10v-7", "M3 6l3 -3l3 3") }
    val Star: ImageVector by lazy { tabler("star", false, "M12 17.75l-6.172 3.245l1.179 -6.873l-5 -4.867l6.9 -1l3.086 -6.253l3.086 6.253l6.9 1l-5 4.867l1.179 6.873l-6.158 -3.245") }
    val StarFilled: ImageVector by lazy { tabler("star-filled", true, "M8.243 7.34l-6.38 .925l-.113 .023a1 1 0 0 0 -.44 1.684l4.622 4.499l-1.09 6.355l-.013 .11a1 1 0 0 0 1.464 .944l5.706 -3l5.693 3l.1 .046a1 1 0 0 0 1.352 -1.1l-1.091 -6.355l4.624 -4.5l.078 -.085a1 1 0 0 0 -.633 -1.62l-6.38 -.926l-2.852 -5.78a1 1 0 0 0 -1.794 0l-2.853 5.78z") }
    val Stopwatch: ImageVector by lazy { tabler("stopwatch", false, "M5 13a7 7 0 1 0 14 0a7 7 0 0 0 -14 0", "M14.5 10.5l-2.5 2.5", "M17 8l1 -1", "M14 3h-4") }
    val SunHigh: ImageVector by lazy { tabler("sun-high", false, "M14.828 14.828a4 4 0 1 0 -5.656 -5.656a4 4 0 0 0 5.656 5.656", "M6.343 17.657l-1.414 1.414", "M6.343 6.343l-1.414 -1.414", "M17.657 6.343l1.414 -1.414", "M17.657 17.657l1.414 1.414", "M4 12h-2", "M12 4v-2", "M20 12h2", "M12 20v2") }
    val SunWind: ImageVector by lazy { tabler("sun-wind", false, "M14.468 10a4 4 0 1 0 -5.466 5.46", "M2 12h1", "M11 3v1", "M11 20v1", "M4.6 5.6l.7 .7", "M17.4 5.6l-.7 .7", "M5.3 17.7l-.7 .7", "M15 13h5a2 2 0 1 0 0 -4", "M12 16h5.714l.253 0a2 2 0 0 1 2.033 2a2 2 0 0 1 -2 2h-.286") }
    val Tallymarks: ImageVector by lazy { tabler("tallymarks", false, "M6 5l0 14", "M10 5l0 14", "M14 5l0 14", "M18 5l0 14", "M3 17l18 -10") }
    val TargetArrow: ImageVector by lazy { tabler("target-arrow", false, "M11 12a1 1 0 1 0 2 0a1 1 0 1 0 -2 0", "M12 7a5 5 0 1 0 5 5", "M13 3.055a9 9 0 1 0 7.941 7.945", "M15 6v3h3l3 -3h-3v-3l-3 3", "M15 9l-3 3") }
    val Temperature: ImageVector by lazy { tabler("temperature", false, "M10 13.5a4 4 0 1 0 4 0v-8.5a2 2 0 0 0 -4 0v8.5", "M10 9l4 0") }
    val Terminal2: ImageVector by lazy { tabler("terminal-2", false, "M8 9l3 3l-3 3", "M13 15l3 0", "M3 6a2 2 0 0 1 2 -2h14a2 2 0 0 1 2 2v12a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2l0 -12") }
    val TextRecognition: ImageVector by lazy { tabler("text-recognition", false, "M4 8v-2a2 2 0 0 1 2 -2h2", "M4 16v2a2 2 0 0 0 2 2h2", "M16 4h2a2 2 0 0 1 2 2v2", "M16 20h2a2 2 0 0 0 2 -2v-2", "M12 16v-7", "M9 9h6") }
    val Thermometer: ImageVector by lazy { tabler("thermometer", false, "M19 5a2.828 2.828 0 0 1 0 4l-8 8h-4v-4l8 -8a2.828 2.828 0 0 1 4 0", "M16 7l-1.5 -1.5", "M13 10l-1.5 -1.5", "M10 13l-1.5 -1.5", "M7 17l-3 3") }
    val Tools: ImageVector by lazy { tabler("tools", false, "M3 21h4l13 -13a1.5 1.5 0 0 0 -4 -4l-13 13v4", "M14.5 5.5l4 4", "M12 8l-5 -5l-4 4l5 5", "M7 8l-1.5 1.5", "M16 12l5 5l-4 4l-5 -5", "M16 17l-1.5 1.5") }
    val Volume: ImageVector by lazy { tabler("volume", false, "M15 8a5 5 0 0 1 0 8", "M17.7 5a9 9 0 0 1 0 14", "M6 15h-2a1 1 0 0 1 -1 -1v-4a1 1 0 0 1 1 -1h2l3.5 -4.5a.8 .8 0 0 1 1.5 .5v14a.8 .8 0 0 1 -1.5 .5l-3.5 -4.5") }
    val Walk: ImageVector by lazy { tabler("walk", false, "M12 4a1 1 0 1 0 2 0a1 1 0 1 0 -2 0", "M7 21l3 -4", "M16 21l-2 -4l-3 -3l1 -6", "M6 12l2 -3l4 -1l3 3l3 1") }
    val WaveSine: ImageVector by lazy { tabler("wave-sine", false, "M21 12h-2c-.894 0 -1.662 -.857 -1.761 -2c-.296 -3.45 -.749 -6 -2.749 -6s-2.5 3.582 -2.5 8s-.5 8 -2.5 8s-2.452 -2.547 -2.749 -6c-.1 -1.147 -.867 -2 -1.763 -2h-2") }
    val WaveSquare: ImageVector by lazy { tabler("wave-square", false, "M3 12h5v8h4v-16h4v8h5") }
    val Waveform: ImageVector by lazy { tabler("waveform", false, "M4 10.5v3", "M8 7v10", "M12 8v8", "M16 4v16", "M20 10v4") }
    val World: ImageVector by lazy { tabler("world", false, "M3 12a9 9 0 1 0 18 0a9 9 0 0 0 -18 0", "M3.6 9h16.8", "M3.6 15h16.8", "M11.5 3a17 17 0 0 0 0 18", "M12.5 3a17 17 0 0 1 0 18") }
    val WorldSearch: ImageVector by lazy { tabler("world-search", false, "M21 12a9 9 0 1 0 -9 9", "M3.6 9h16.8", "M3.6 15h7.9", "M11.5 3a17 17 0 0 0 0 18", "M12.5 3a16.984 16.984 0 0 1 2.574 8.62", "M15 18a3 3 0 1 0 6 0a3 3 0 1 0 -6 0", "M20.2 20.2l1.8 1.8") }
    val Writing: ImageVector by lazy { tabler("writing", false, "M20 17v-12c0 -1.121 -.879 -2 -2 -2s-2 .879 -2 2v12l2 2l2 -2", "M16 7h4", "M18 19h-13a2 2 0 1 1 0 -4h4a2 2 0 1 0 0 -4h-3") }
    val ZoomIn: ImageVector by lazy { tabler("zoom-in", false, "M3 10a7 7 0 1 0 14 0a7 7 0 1 0 -14 0", "M7 10l6 0", "M10 7l0 6", "M21 21l-6 -6") }
}
