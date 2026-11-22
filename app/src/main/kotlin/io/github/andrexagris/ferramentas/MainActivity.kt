package io.github.andrexagris.ferramentas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import io.github.andrexagris.ferramentas.ui.App
import io.github.andrexagris.opendesign.OpenDesignTema
import io.github.andrexagris.opendesign.rememberOdPreferencias

/** Única Activity do app. O fundo vai atrás das barras do sistema; o OdTelaApp cuida das áreas seguras. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var prefs by rememberOdPreferencias()
            OpenDesignTema(prefs) { App(prefs) { prefs = it } }
        }
    }
}
