package io.github.andrexagris.ferramentas.servicos

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import io.github.andrexagris.ferramentas.base.Lanterna

/**
 * Executa a ação de uma tag NFC gravada pela ferramenta "Tag como gatilho de ação"
 * (ferramentas://acao/<ação>?v=<valor>) e fecha na hora, sem mostrar tela.
 */
class AtividadeGatilho : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent?.data
        runCatching { executar(uri) }.onFailure { Toast.makeText(this, "Não foi possível executar a ação da tag.", Toast.LENGTH_SHORT).show() }
        finish()
    }

    private fun executar(uri: Uri?) {
        val acao = uri?.pathSegments?.firstOrNull() ?: return
        val valor = uri.getQueryParameter("v").orEmpty()
        when (acao) {
            "lanterna" -> { Lanterna.observar(this); Lanterna.alternar(this) }
            "wifi" -> startActivity(Intent(Settings.Panel.ACTION_WIFI).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            "bluetooth" -> startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            "app" -> packageManager.getLaunchIntentForPackage(valor)?.let { startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                ?: Toast.makeText(this, "App $valor não instalado.", Toast.LENGTH_SHORT).show()
            "link" -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(valor)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            "velocidade" -> if (ServicoVelocidade.ativo.value) ServicoVelocidade.parar(this) else ServicoVelocidade.iniciar(this)
        }
    }
}
