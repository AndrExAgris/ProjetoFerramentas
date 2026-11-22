// Gerado por ferramentas/gerar_compose.py a partir de tokens-resolvidos.json. Não edite à mão.
package io.github.andrexagris.opendesign

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Uma família de cor: preenchimento, versão para texto (4,5:1), fundo tingido e texto sobre o preenchimento. */
@Immutable
data class OdFamilia(val cor: Color, val texto: Color, val fundo: Color, val sobre: Color)

/** As 10 cores de destaque que a pessoa pode escolher como principal ou secundária. */
enum class OdCor(val id: String, val nome: String) {
    Laranja("laranja", "Laranja"),
    Teal("teal", "Teal"),
    Roxo("roxo", "Roxo"),
    Indigo("indigo", "Índigo"),
    Malva("malva", "Malva"),
    Lima("lima", "Lima"),
    Fucsia("fucsia", "Fúcsia"),
    Oliva("oliva", "Oliva"),
    Bronze("bronze", "Bronze"),
    Grafite("grafite", "Grafite");

    companion object {
        fun deId(id: String?): OdCor? = entries.firstOrNull { it.id == id }
    }
}

/** Todas as cores de um modo (escuro ou claro), já com a principal e a secundária escolhidas. */
@Immutable
data class OdCores(
    val claro: Boolean,
    val fundo: Color,
    val superficie: Color,
    val superficieElevada: Color,
    val superficieSutil: Color,
    val superficieCodigo: Color,
    val borda: Color,
    val bordaControle: Color,
    val texto: Color,
    val texto2: Color,
    val textoDesabilitado: Color,
    val sobreposicaoHover: Color,
    val sobreposicaoPressionado: Color,
    val sobreposicaoModal: Color,
    val vidro: Color,
    val vidroBorda: Color,
    val vidroTexto: Color,
    val foco: Color,
    val graficoX: Color,
    val graficoY: Color,
    val graficoZ: Color,
    val graficoResultante: Color,
    val graficoGrade: Color,
    val graficoEixo: Color,
    val graficoRotulo: Color,
    val escalaFaixaOk: Color,
    val escalaFaixaAtencao: Color,
    val escalaFaixaPerigo: Color,
    val escalaTrilho: Color,
    val escalaMarca: Color,
    val escalaMarcaMenor: Color,
    val escalaPonteiro: Color,
    val escalaDentro: Color,
    val escalaReferencia: Color,
    val primaria: OdFamilia,
    val secundaria: OdFamilia,
    val sucesso: OdFamilia,
    val erro: OdFamilia,
    val aviso: OdFamilia,
    val info: OdFamilia,
    /** As 10 famílias de destaque, na ordem de [OdCor] (amostras das configurações, séries de gráfico). */
    val familias: List<OdFamilia>,
)

private val familiasEscuro = listOf(
    OdFamilia(Color(0xFFFC6C12), Color(0xFFFF915E), Color(0xFF823B15), Color(0xFF1B2329)), // laranja,
    OdFamilia(Color(0xFF36D2BA), Color(0xFF2BCCB4), Color(0xFF066054), Color(0xFF1B2329)), // teal,
    OdFamilia(Color(0xFF8758C1), Color(0xFFBD92F9), Color(0xFF5D447E), Color(0xFFFFFFFF)), // roxo,
    OdFamilia(Color(0xFF5D6CD8), Color(0xFF91A3FF), Color(0xFF424D89), Color(0xFFFFFFFF)), // indigo,
    OdFamilia(Color(0xFFCF8FAA), Color(0xFFDE9CB8), Color(0xFF694755), Color(0xFF1B2329)), // malva,
    OdFamilia(Color(0xFFA8E051), Color(0xFF9AC757), Color(0xFF415D0D), Color(0xFF1B2329)), // lima,
    OdFamilia(Color(0xFFC23EB3), Color(0xFFE486D5), Color(0xFF75386C), Color(0xFFFFFFFF)), // fucsia,
    OdFamilia(Color(0xFFA7AC41), Color(0xFFB0B64B), Color(0xFF54571E), Color(0xFF1B2329)), // oliva,
    OdFamilia(Color(0xFFD48C4D), Color(0xFFE69D5E), Color(0xFF6F4928), Color(0xFF1B2329)), // bronze,
    OdFamilia(Color(0xFF8B9AAB), Color(0xFF9FAEBF), Color(0xFF4C535C), Color(0xFF1B2329)), // grafite,
)
private val sucessoEscuro = OdFamilia(Color(0xFF48C063), Color(0xFF61C674), Color(0xFF314239), Color(0xFF1B2329))
private val erroEscuro = OdFamilia(Color(0xFFF74E62), Color(0xFFFE898F), Color(0xFF493538), Color(0xFF1B2329))
private val avisoEscuro = OdFamilia(Color(0xFFFFCC60), Color(0xFFEEB737), Color(0xFF4B463C), Color(0xFF1B2329))
private val infoEscuro = OdFamilia(Color(0xFF40A2EF), Color(0xFF59B4FE), Color(0xFF303E4B), Color(0xFF1B2329))

private val familiasClaro = listOf(
    OdFamilia(Color(0xFFFE864C), Color(0xFFB04907), Color(0xFFFEE5DA), Color(0xFF1B2329)), // laranja,
    OdFamilia(Color(0xFF3BE1C7), Color(0xFF077869), Color(0xFFC6F7EC), Color(0xFF1B2329)), // teal,
    OdFamilia(Color(0xFF8F5ECC), Color(0xFF784DAE), Color(0xFFEFE6FE), Color(0xFFFFFFFF)), // roxo,
    OdFamilia(Color(0xFF5C6BDC), Color(0xFF4D5BBA), Color(0xFFE5EAFE), Color(0xFFFFFFFF)), // indigo,
    OdFamilia(Color(0xFFDC98B5), Color(0xFF935872), Color(0xFFFFE2ED), Color(0xFF1B2329)), // malva,
    OdFamilia(Color(0xFFADE753), Color(0xFF517502), Color(0xFFE0F3CC), Color(0xFF1B2329)), // lima,
    OdFamilia(Color(0xFFEE64DC), Color(0xFF9E4692), Color(0xFFFFE1F8), Color(0xFF1B2329)), // fucsia,
    OdFamilia(Color(0xFFB0B645), Color(0xFF6B6F03), Color(0xFFEBEFC8), Color(0xFF1B2329)), // oliva,
    OdFamilia(Color(0xFFE09452), Color(0xFF9B570E), Color(0xFFFFE6D3), Color(0xFF1B2329)), // bronze,
    OdFamilia(Color(0xFF95A5B7), Color(0xFF5D6A7A), Color(0xFFE6ECF3), Color(0xFF1B2329)), // grafite,
)
private val sucessoClaro = OdFamilia(Color(0xFF3DBE5D), Color(0xFF007A2E), Color(0xFFE3F4E7), Color(0xFF1B2329))
private val erroClaro = OdFamilia(Color(0xFFE0284A), Color(0xFFAE3B47), Color(0xFFFCE2E3), Color(0xFFFFFFFF))
private val avisoClaro = OdFamilia(Color(0xFFFBBC09), Color(0xFF836105), Color(0xFFFBE9C3), Color(0xFF1B2329))
private val infoClaro = OdFamilia(Color(0xFF1A92E4), Color(0xFF006BAC), Color(0xFFDFEEFB), Color(0xFF1B2329))

/** Monta as cores de um modo com a principal e a secundária escolhidas. Tokens que apontam para a principal
 *  (foco, referência das escalas) acompanham a troca. */
fun odCores(claro: Boolean, primaria: OdCor = OdCor.Laranja, secundaria: OdCor = OdCor.Teal, primariaPersonalizada: OdFamilia? = null): OdCores {
    val fams = if (claro) familiasClaro else familiasEscuro
    val p = primariaPersonalizada ?: fams[primaria.ordinal]
    val s = fams[secundaria.ordinal]
    return if (claro) OdCores(
        claro = true,
        fundo = Color(0xFFEDF0F2),
        superficie = Color(0xFFFAFCFD),
        superficieElevada = Color(0xFFFFFFFF),
        superficieSutil = Color(0xFFEAEEF2),
        superficieCodigo = Color(0xFFF1F4F6),
        borda = Color(0xFFD8DEE3),
        bordaControle = Color(0xFF7E888F),
        texto = Color(0xFF1B2329),
        texto2 = Color(0xFF626D75),
        textoDesabilitado = Color(0xFF9EA6AB),
        sobreposicaoHover = Color(0x0D10181F),
        sobreposicaoPressionado = Color(0x1A10181F),
        sobreposicaoModal = Color(0x6610181F),
        vidro = Color(0x9EFAFCFD),
        vidroBorda = Color(0x99FFFFFF),
        vidroTexto = Color(0xFF10181F),
        foco = p.texto,
        graficoX = Color(0xFFB7191C),
        graficoY = Color(0xFF589E20),
        graficoZ = Color(0xFF1F6DD8),
        graficoResultante = Color(0xFF1B2329),
        graficoGrade = Color(0xFFD8DEE3),
        graficoEixo = Color(0xFF7E888F),
        graficoRotulo = Color(0xFF626D75),
        escalaFaixaOk = Color(0xFFC8EACE),
        escalaFaixaAtencao = Color(0xFFFCDEA1),
        escalaFaixaPerigo = Color(0xFFFBCCCD),
        escalaTrilho = Color(0xFFD8DEE3),
        escalaMarca = Color(0xFF626D75),
        escalaMarcaMenor = Color(0xFF7E888F),
        escalaPonteiro = Color(0xFF1B2329),
        escalaDentro = Color(0xFF007A2E),
        escalaReferencia = p.texto,
        primaria = p,
        secundaria = s,
        sucesso = sucessoClaro,
        erro = erroClaro,
        aviso = avisoClaro,
        info = infoClaro,
        familias = fams,
    ) else OdCores(
        claro = false,
        fundo = Color(0xFF181A1D),
        superficie = Color(0xFF292C30),
        superficieElevada = Color(0xFF373B40),
        superficieSutil = Color(0xFF303438),
        superficieCodigo = Color(0xFF111315),
        borda = Color(0xFF44484D),
        bordaControle = Color(0xFF888D92),
        texto = Color(0xFFEDEFF1),
        texto2 = Color(0xFFA9ADB2),
        textoDesabilitado = Color(0xFF66696E),
        sobreposicaoHover = Color(0x12FFFFFF),
        sobreposicaoPressionado = Color(0x21FFFFFF),
        sobreposicaoModal = Color(0xA6000000),
        vidro = Color(0x8C292C30),
        vidroBorda = Color(0x1FFFFFFF),
        vidroTexto = Color(0xFFFFFFFF),
        foco = p.texto,
        graficoX = Color(0xFFE64343),
        graficoY = Color(0xFFA8E051),
        graficoZ = Color(0xFF498BEB),
        graficoResultante = Color(0xFFEDEFF1),
        graficoGrade = Color(0xFF44484D),
        graficoEixo = Color(0xFF888D92),
        graficoRotulo = Color(0xFFA9ADB2),
        escalaFaixaOk = Color(0xFF385B43),
        escalaFaixaAtencao = Color(0xFF635843),
        escalaFaixaPerigo = Color(0xFF683C40),
        escalaTrilho = Color(0xFF44484D),
        escalaMarca = Color(0xFFA9ADB2),
        escalaMarcaMenor = Color(0xFF888D92),
        escalaPonteiro = Color(0xFFEDEFF1),
        escalaDentro = Color(0xFF61C674),
        escalaReferencia = p.texto,
        primaria = p,
        secundaria = s,
        sucesso = sucessoEscuro,
        erro = erroEscuro,
        aviso = avisoEscuro,
        info = infoEscuro,
        familias = fams,
    )
}
