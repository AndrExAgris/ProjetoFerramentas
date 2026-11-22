// Gerado por ferramentas/gerar_icones.py a partir dos SVG da Tabler Icons (licença MIT). Não edite à mão.
package io.github.andrexagris.opendesign

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

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

/** Ícones Tabler usados pelo Open Design. Use com Icon(OdIcones.Seta, contentDescription = null): a cor vem do LocalContentColor. */
object OdIcones {
    val Voltar: ImageVector by lazy { tabler("arrow-left", false, "M5 12l14 0", "M5 12l6 6", "M5 12l6 -6") }
    val Seta: ImageVector by lazy { tabler("chevron-right", false, "M9 6l6 6l-6 6") }
    val Fechar: ImageVector by lazy { tabler("x", false, "M18 6l-12 12", "M6 6l12 12") }
    val Marca: ImageVector by lazy { tabler("check", false, "M5 12l5 5l10 -10") }
    val Sucesso: ImageVector by lazy { tabler("circle-check", false, "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0", "M9 12l2 2l4 -4") }
    val Erro: ImageVector by lazy { tabler("circle-x", false, "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0", "M10 10l4 4m0 -4l-4 4") }
    val Aviso: ImageVector by lazy { tabler("alert-triangle", false, "M12 9v4", "M10.363 3.591l-8.106 13.534a1.914 1.914 0 0 0 1.636 2.871h16.214a1.914 1.914 0 0 0 1.636 -2.87l-8.106 -13.536a1.914 1.914 0 0 0 -3.274 0", "M12 16h.01") }
    val Info: ImageVector by lazy { tabler("info-circle", false, "M3 12a9 9 0 1 0 18 0a9 9 0 0 0 -18 0", "M12 9h.01", "M11 12h1v4h1") }
    val Cadeado: ImageVector by lazy { tabler("lock", false, "M5 13a2 2 0 0 1 2 -2h10a2 2 0 0 1 2 2v6a2 2 0 0 1 -2 2h-10a2 2 0 0 1 -2 -2v-6", "M11 16a1 1 0 1 0 2 0a1 1 0 0 0 -2 0", "M8 11v-4a4 4 0 1 1 8 0v4") }
    val Indisponivel: ImageVector by lazy { tabler("device-mobile-off", false, "M7.159 3.185c.256 -.119 .54 -.185 .841 -.185h8a2 2 0 0 1 2 2v9m0 4v1a2 2 0 0 1 -2 2h-8a2 2 0 0 1 -2 -2v-13", "M11 4h2", "M3 3l18 18", "M12 17v.01") }
    val Parar: ImageVector by lazy { tabler("player-stop", false, "M5 7a2 2 0 0 1 2 -2h10a2 2 0 0 1 2 2v10a2 2 0 0 1 -2 2h-10a2 2 0 0 1 -2 -2l0 -10") }
    val Pausar: ImageVector by lazy { tabler("player-pause", false, "M6 6a1 1 0 0 1 1 -1h2a1 1 0 0 1 1 1v12a1 1 0 0 1 -1 1h-2a1 1 0 0 1 -1 -1l0 -12", "M14 6a1 1 0 0 1 1 -1h2a1 1 0 0 1 1 1v12a1 1 0 0 1 -1 1h-2a1 1 0 0 1 -1 -1l0 -12") }
    val Iniciar: ImageVector by lazy { tabler("player-play", false, "M7 4v16l13 -8l-13 -8") }
    val Paleta: ImageVector by lazy { tabler("palette", false, "M12 21a9 9 0 0 1 0 -18c4.97 0 9 3.582 9 8c0 1.06 -.474 2.078 -1.318 2.828c-.844 .75 -1.989 1.172 -3.182 1.172h-2.5a2 2 0 0 0 -1 3.75a1.3 1.3 0 0 1 -1 2.25", "M7.5 10.5a1 1 0 1 0 2 0a1 1 0 1 0 -2 0", "M11.5 7.5a1 1 0 1 0 2 0a1 1 0 1 0 -2 0", "M15.5 10.5a1 1 0 1 0 2 0a1 1 0 1 0 -2 0") }
    val Densidade: ImageVector by lazy { tabler("layout-grid", false, "M4 5a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v4a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1l0 -4", "M14 5a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v4a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1l0 -4", "M4 15a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v4a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1l0 -4", "M14 15a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v4a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1l0 -4") }
    val Animacoes: ImageVector by lazy { tabler("activity", false, "M3 12h4l3 8l4 -16l3 8h4") }
    val Restaurar: ImageVector by lazy { tabler("restore", false, "M3.06 13a9 9 0 1 0 .49 -4.087", "M3 4.001v5h5", "M11 12a1 1 0 1 0 2 0a1 1 0 1 0 -2 0") }
    val Tema: ImageVector by lazy { tabler("sun-moon", false, "M9.173 14.83a4 4 0 1 1 5.657 -5.657", "M11.294 12.707l.174 .247a7.5 7.5 0 0 0 8.845 2.492a9 9 0 0 1 -14.671 2.914", "M3 12h1", "M12 3v1", "M5.6 5.6l.7 .7", "M3 21l18 -18") }
    val Lixeira: ImageVector by lazy { tabler("trash", false, "M4 7l16 0", "M10 11l0 6", "M14 11l0 6", "M5 7l1 12a2 2 0 0 0 2 2h8a2 2 0 0 0 2 -2l1 -12", "M9 7v-3a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v3") }
    val Configuracoes: ImageVector by lazy { tabler("settings", false, "M10.325 4.317c.426 -1.756 2.924 -1.756 3.35 0a1.724 1.724 0 0 0 2.573 1.066c1.543 -.94 3.31 .826 2.37 2.37a1.724 1.724 0 0 0 1.065 2.572c1.756 .426 1.756 2.924 0 3.35a1.724 1.724 0 0 0 -1.066 2.573c.94 1.543 -.826 3.31 -2.37 2.37a1.724 1.724 0 0 0 -2.572 1.065c-.426 1.756 -2.924 1.756 -3.35 0a1.724 1.724 0 0 0 -2.573 -1.066c-1.543 .94 -3.31 -.826 -2.37 -2.37a1.724 1.724 0 0 0 -1.065 -2.572c-1.756 -.426 -1.756 -2.924 0 -3.35a1.724 1.724 0 0 0 1.066 -2.573c-.94 -1.543 .826 -3.31 2.37 -2.37c1 .608 2.296 .07 2.572 -1.065", "M9 12a3 3 0 1 0 6 0a3 3 0 0 0 -6 0") }
    val ConfiguracoesCheio: ImageVector by lazy { tabler("settings-filled", true, "M14.647 4.081a.724 .724 0 0 0 1.08 .448c2.439 -1.485 5.23 1.305 3.745 3.744a.724 .724 0 0 0 .447 1.08c2.775 .673 2.775 4.62 0 5.294a.724 .724 0 0 0 -.448 1.08c1.485 2.439 -1.305 5.23 -3.744 3.745a.724 .724 0 0 0 -1.08 .447c-.673 2.775 -4.62 2.775 -5.294 0a.724 .724 0 0 0 -1.08 -.448c-2.439 1.485 -5.23 -1.305 -3.745 -3.744a.724 .724 0 0 0 -.447 -1.08c-2.775 -.673 -2.775 -4.62 0 -5.294a.724 .724 0 0 0 .448 -1.08c-1.485 -2.439 1.305 -5.23 3.744 -3.745a.722 .722 0 0 0 1.08 -.447c.673 -2.775 4.62 -2.775 5.294 0zm-2.647 4.919a3 3 0 1 0 0 6a3 3 0 0 0 0 -6") }
    val Mais: ImageVector by lazy { tabler("dots", false, "M4 12a1 1 0 1 0 2 0a1 1 0 1 0 -2 0", "M11 12a1 1 0 1 0 2 0a1 1 0 1 0 -2 0", "M18 12a1 1 0 1 0 2 0a1 1 0 1 0 -2 0") }
    val MaisCheio: ImageVector by lazy { tabler("dots-filled", true, "M7 12a2 2 0 1 1 -4 0q 0 -.053 .005 -.102a1.996 1.996 0 0 1 1.995 -1.898a2 2 0 0 1 2 2", "M14 12a2 2 0 1 1 -4 0q 0 -.053 .005 -.102a1.996 1.996 0 0 1 1.995 -1.898a2 2 0 0 1 2 2", "M21 12a2 2 0 1 1 -4 0q 0 -.053 .005 -.102a1.996 1.996 0 0 1 1.995 -1.898a2 2 0 0 1 2 2") }
    val Inicio: ImageVector by lazy { tabler("home", false, "M5 12l-2 0l9 -9l9 9l-2 0", "M5 12v7a2 2 0 0 0 2 2h10a2 2 0 0 0 2 -2v-7", "M9 21v-6a2 2 0 0 1 2 -2h2a2 2 0 0 1 2 2v6") }
    val InicioCheio: ImageVector by lazy { tabler("home-filled", true, "M12.707 2.293l9 9c.63 .63 .184 1.707 -.707 1.707h-1v6a3 3 0 0 1 -3 3h-1v-7a3 3 0 0 0 -2.824 -2.995l-.176 -.005h-2a3 3 0 0 0 -3 3v7h-1a3 3 0 0 1 -3 -3v-6h-1c-.89 0 -1.337 -1.077 -.707 -1.707l9 -9a1 1 0 0 1 1.414 0m.293 11.707a1 1 0 0 1 1 1v7h-4v-7a1 1 0 0 1 .883 -.993l.117 -.007z") }
    val Medidor: ImageVector by lazy { tabler("gauge", false, "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0", "M11 12a1 1 0 1 0 2 0a1 1 0 1 0 -2 0", "M13.41 10.59l2.59 -2.59", "M7 12a5 5 0 0 1 5 -5") }
    val MedidorCheio: ImageVector by lazy { tabler("gauge-filled", true, "M17 3.34a10 10 0 1 1 -14.995 8.984l-.005 -.324l.005 -.324a10 10 0 0 1 14.995 -8.336zm-.293 3.953a1 1 0 0 0 -1.414 0l-2.59 2.59l-.083 .094l-.068 .1a2.001 2.001 0 0 0 -2.547 1.774l-.005 .149l.005 .15a2 2 0 1 0 3.917 -.701a.968 .968 0 0 0 .195 -.152l2.59 -2.59l.083 -.094a1 1 0 0 0 -.083 -1.32zm-4.707 -1.293a6 6 0 0 0 -6 6a1 1 0 0 0 2 0a4 4 0 0 1 4 -4a1 1 0 0 0 0 -2z") }
    val Bateria: ImageVector by lazy { tabler("battery-3", false, "M6 7h11a2 2 0 0 1 2 2v.5a.5 .5 0 0 0 .5 .5a.5 .5 0 0 1 .5 .5v3a.5 .5 0 0 1 -.5 .5a.5 .5 0 0 0 -.5 .5v.5a2 2 0 0 1 -2 2h-11a2 2 0 0 1 -2 -2v-6a2 2 0 0 1 2 -2", "M7 10l0 4", "M10 10l0 4", "M13 10l0 4") }
    val BateriaCheio: ImageVector by lazy { tabler("battery-3-filled", true, "M17 6a3 3 0 0 1 2.995 2.824l.005 .176v.086l.052 .019a1.5 1.5 0 0 1 .941 1.25l.007 .145v3a1.5 1.5 0 0 1 -.948 1.395l-.052 .018v.087a3 3 0 0 1 -2.824 2.995l-.176 .005h-11a3 3 0 0 1 -2.995 -2.824l-.005 -.176v-6a3 3 0 0 1 2.824 -2.995l.176 -.005h11zm-10 3a1 1 0 0 0 -1 1v4l.007 .117a1 1 0 0 0 1.993 -.117v-4l-.007 -.117a1 1 0 0 0 -.993 -.883zm3 0a1 1 0 0 0 -1 1v4l.007 .117a1 1 0 0 0 1.993 -.117v-4l-.007 -.117a1 1 0 0 0 -.993 -.883zm3 0a1 1 0 0 0 -1 1v4l.007 .117a1 1 0 0 0 1.993 -.117v-4l-.007 -.117a1 1 0 0 0 -.993 -.883z") }
    val Wifi: ImageVector by lazy { tabler("wifi", false, "M12 18l.01 0", "M9.172 15.172a4 4 0 0 1 5.656 0", "M6.343 12.343a8 8 0 0 1 11.314 0", "M3.515 9.515c4.686 -4.687 12.284 -4.687 17 0") }
    val Notificacoes: ImageVector by lazy { tabler("bell", false, "M10 5a2 2 0 1 1 4 0a7 7 0 0 1 4 6v3a4 4 0 0 0 2 3h-16a4 4 0 0 0 2 -3v-3a7 7 0 0 1 4 -6", "M9 17v1a3 3 0 0 0 6 0v-1") }
}
