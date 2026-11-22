package io.github.andrexagris.opendesign

import android.content.Context
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Cor principal a partir do papel de parede (Material You), Android 12 ou mais novo.
 *
 * Decisão do Open Design: o app ignora as cores dinâmicas por padrão. A pessoa pode escolher "Sistema" como
 * cor principal nas configurações, e só a principal muda. Neutros, estados (sucesso, erro, aviso, info),
 * cores de gráfico e escalas de instrumento continuam do Open Design, porque são elas que garantem o
 * contraste e a distância entre a cor de destaque e as cores de estado.
 *
 * Os papéis do Material 3 já seguem a mesma lógica do Open Design: primary é preenchimento e serve como texto
 * sobre as superfícies; onPrimary é o texto sobre ele; primaryContainer é o fundo tingido.
 * Devolve null abaixo do Android 12 (o tema cai na cor escolhida).
 */
fun familiaDoSistema(contexto: Context, claro: Boolean): OdFamilia? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    val m = if (claro) dynamicLightColorScheme(contexto) else dynamicDarkColorScheme(contexto)
    return OdFamilia(
        cor = m.primary,
        texto = m.primary,
        fundo = m.primaryContainer,
        sobre = if (contraste(m.onPrimary, m.primary) >= 4.5f) m.onPrimary else melhorTinta(m.primary),
    )
}

/** Contraste WCAG entre duas cores. */
fun contraste(a: Color, b: Color): Float {
    val la = a.luminance() + 0.05f
    val lb = b.luminance() + 0.05f
    return if (la > lb) la / lb else lb / la
}

/** Tinta escura do Open Design ou branco, o que contrastar mais com o preenchimento. */
fun melhorTinta(fundo: Color): Color {
    val escura = Color(0xFF1B2329)
    return if (contraste(escura, fundo) >= contraste(Color.White, fundo)) escura else Color.White
}
