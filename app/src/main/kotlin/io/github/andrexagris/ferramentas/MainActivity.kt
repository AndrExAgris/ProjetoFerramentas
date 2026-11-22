package io.github.andrexagris.ferramentas

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import io.github.andrexagris.ferramentas.base.Teclas
import io.github.andrexagris.ferramentas.ui.App
import io.github.andrexagris.ferramentas.widgets.WidgetBateria
import io.github.andrexagris.opendesign.OpenDesignTema
import io.github.andrexagris.opendesign.rememberOdPreferencias
import kotlinx.coroutines.launch

/** Única Activity do app. O fundo vai atrás das barras do sistema; o OdTelaApp cuida das áreas seguras. */
class MainActivity : ComponentActivity() {
    /** Só no build de debug: abre uma ferramenta direto (adb shell am start ... --es ferramenta <id>), para testes. */
    private var ferramentaPedida by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lerPedido(intent)
        setContent {
            var prefs by rememberOdPreferencias()
            OpenDesignTema(prefs) { App(prefs, ferramentaPedida, { ferramentaPedida = null }) { prefs = it } }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        lerPedido(intent)
    }

    private fun lerPedido(i: Intent?) {
        if (BuildConfig.DEBUG) i?.getStringExtra("ferramenta")?.let { ferramentaPedida = it }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { runCatching { WidgetBateria().updateAll(this@MainActivity) } }
    }

    /** Teclas físicas passam primeiro pela ferramenta aberta (contador, teste de botões). */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean = Teclas.despachar(event) || super.onKeyDown(keyCode, event)
    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean = Teclas.despachar(event) || super.onKeyUp(keyCode, event)
}
