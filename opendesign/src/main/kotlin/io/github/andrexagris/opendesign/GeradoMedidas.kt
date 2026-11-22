// Gerado por ferramentas/gerar_compose.py a partir de tokens-resolvidos.json. Não edite à mão.
package io.github.andrexagris.opendesign

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Escala de espaços (múltiplos de 4). Espaços internos de componentes acompanham a densidade: multiplique por [OdDensidade.fator]. */
object OdEspacos {
    val e1: Dp = 4.dp
    val e2: Dp = 8.dp
    val e3: Dp = 12.dp
    val e4: Dp = 16.dp
    val e5: Dp = 24.dp
    val e6: Dp = 32.dp
    val e7: Dp = 48.dp
    val e8: Dp = 64.dp
}

/** Raios de canto. */
object OdRaios {
    val tecnico: Dp = 0.dp
    val indicador: Dp = 4.dp
    val controle: Dp = 8.dp
    val painel: Dp = 10.dp
    val flutuante: Dp = 12.dp
    val total: Dp = 1000.dp
}

/** Medidas de controles, regiões e grade (densidade equilibrada). */
object OdMedidas {
    val alvoToque: Dp = 48.dp
    val alturaControle: Dp = 36.dp
    val alturaItemNav: Dp = 36.dp
    val larguraNav: Dp = 240.dp
    val larguraNavCompacta: Dp = 60.dp
    val alturaBarraSuperior: Dp = 56.dp
    val bordaFina: Dp = 1.dp
    val anelFoco: Dp = 3.dp
    const val espessuraIcone: Float = 1.75f
    const val gradeColunasCompacta: Int = 4
    const val gradeColunasMedia: Int = 8
    const val gradeColunasExpandida: Int = 12
    val gradeMargemCompacta: Dp = 16.dp
    val gradeMargemMedia: Dp = 24.dp
    val gradeMargemExpandida: Dp = 32.dp
    val gradeMedianizCompacta: Dp = 12.dp
    val gradeMedianizMedia: Dp = 16.dp
    val gradeMedianizExpandida: Dp = 24.dp
    val pontoMedia: Dp = 600.dp
    val pontoExpandida: Dp = 840.dp
    val alturaNavInferior: Dp = 64.dp
    val vidroDesfoque: Dp = 16.dp
}

/** Durações (ms) e curva padrão. Com reduzir animações, use 0 (veja OdTema.movimentoReduzido). */
object OdMovimento {
    const val duracaoRapida: Int = 120
    const val duracao: Int = 160
    const val duracaoLenta: Int = 240
    const val duracaoPulso: Int = 1600
    const val duracaoToast: Int = 5000
    val curva = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
}

/** Opacidades das camadas de interação. */
object OdOpacidades {
    const val hover: Float = 0.08f
    const val pressionado: Float = 0.08f
}

/** Densidade: muda espaços internos e alturas; nunca fontes, raios ou cores. */
enum class OdDensidade(val id: String, val fator: Float, val alturaControle: Dp, val alturaItemNav: Dp, val alturaBarraSuperior: Dp) {
    Compacta("compacta", 0.75f, 30.dp, 30.dp, 48.dp),
    Equilibrada("equilibrada", 1.0f, 36.dp, 36.dp, 56.dp),
    Arejada("arejada", 1.25f, 44.dp, 44.dp, 64.dp);

    companion object {
        fun deId(id: String?): OdDensidade? = entries.firstOrNull { it.id == id }
    }
}
