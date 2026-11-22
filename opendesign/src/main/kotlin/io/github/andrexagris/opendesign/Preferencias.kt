package io.github.andrexagris.opendesign

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Guarda as preferências de aparência em SharedPreferences (sem dependências extras).
 * Mesmas chaves e valores da versão web (tema, primaria, secundaria, densidade, movimento).
 *
 *     var prefs by rememberOdPreferencias()
 *     OpenDesignTema(prefs) { OdTelaConfiguracoes(prefs, aoMudar = { prefs = it }) }
 */
@Composable
fun rememberOdPreferencias(chave: String = "open-design"): MutableState<OdPreferencias> {
    val contexto = LocalContext.current.applicationContext
    return remember(chave) { EstadoPreferencias(contexto, chave) }
}

private class EstadoPreferencias(contexto: Context, chave: String) : MutableState<OdPreferencias> {
    private val sp = contexto.getSharedPreferences(chave, Context.MODE_PRIVATE)
    private val estado = mutableStateOf(ler())

    private fun ler() = OdPreferencias(
        modo = OdModo.deId(sp.getString("tema", null)) ?: OdModo.Sistema,
        primaria = OdCor.deId(sp.getString("primaria", null)) ?: OdCor.Laranja,
        secundaria = OdCor.deId(sp.getString("secundaria", null)) ?: OdCor.Teal,
        densidade = OdDensidade.deId(sp.getString("densidade", null)) ?: OdDensidade.Equilibrada,
        reduzirAnimacoes = sp.getString("movimento", "normal") == "reduzido",
        corDoSistema = sp.getBoolean("cor-do-sistema", false),
    )

    override var value: OdPreferencias
        get() = estado.value
        set(v) {
            estado.value = v
            sp.edit()
                .putString("tema", v.modo.id)
                .putString("primaria", v.primaria.id)
                .putString("secundaria", v.secundaria.id)
                .putString("densidade", v.densidade.id)
                .putString("movimento", if (v.reduzirAnimacoes) "reduzido" else "normal")
                .putBoolean("cor-do-sistema", v.corDoSistema)
                .apply()
        }

    override fun component1() = value
    override fun component2(): (OdPreferencias) -> Unit = { value = it }
}
