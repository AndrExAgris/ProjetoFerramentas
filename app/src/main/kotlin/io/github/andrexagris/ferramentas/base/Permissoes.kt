package io.github.andrexagris.ferramentas.base

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.VpnService
import android.os.Environment
import android.os.Process
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.github.andrexagris.ferramentas.catalogo.ComoObter
import io.github.andrexagris.ferramentas.catalogo.Permissao
import io.github.andrexagris.opendesign.OdEstadoRecurso
import io.github.andrexagris.opendesign.OdTipoEstado

/** Se a permissão já foi concedida (cada tipo tem o seu jeito de conferir). */
fun Context.temPermissao(p: Permissao): Boolean = when (p) {
    Permissao.UsoDoApp -> {
        val ops = getSystemService(AppOpsManager::class.java)
        ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName) == AppOpsManager.MODE_ALLOWED
    }
    Permissao.Armazenamento -> Environment.isExternalStorageManager()
    Permissao.Vpn -> VpnService.prepare(this) == null
    Permissao.ListarApps, Permissao.AltaTaxa, Permissao.Infravermelho, Permissao.Nfc -> true // concedidas na instalação
    else -> p.manifesto.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
}

/** Texto padrão de por que cada permissão é pedida (usado quando a ferramenta não passa o seu). */
fun Permissao.motivoPadrao(): String = when (this) {
    Permissao.UsoDoApp -> "Para saber quanto cada app usa, o Android exige o \"Acesso ao uso\". Ative o Ferramentas na lista que vai abrir."
    Permissao.Localizacao -> "O Android só mostra Wi-Fi, rede celular e GPS com a permissão de localização. A posição não sai do aparelho."
    Permissao.WifiProximo -> "Para procurar redes Wi-Fi próximas, o Android pede acesso a dispositivos próximos."
    Permissao.Bluetooth -> "Para procurar e listar aparelhos Bluetooth, o Android pede acesso a dispositivos próximos."
    Permissao.Camera -> "Esta ferramenta usa a câmera. Nenhuma imagem é guardada ou enviada."
    Permissao.Microfone -> "Esta ferramenta mede o som pelo microfone. Nada é gravado."
    Permissao.Atividade -> "O contador de passos do Android exige a permissão de atividade física."
    Permissao.SensoresCorporais -> "O sensor de frequência cardíaca exige a permissão de sensores corporais."
    Permissao.Notificacoes -> "Esta ferramenta avisa por notificação. Sem a permissão, os avisos não aparecem."
    Permissao.Vpn -> "Para ver as conexões de cada app, o app cria uma VPN local, só neste aparelho. Nada passa por servidor externo."
    Permissao.Armazenamento -> "Para medir o tamanho das pastas, o Android exige \"Acesso a todos os arquivos\". Ative o Ferramentas na tela que vai abrir."
    Permissao.EstatisticasBateria -> "O Android só libera a saúde oficial da bateria com uma permissão dada uma vez pelo computador (ADB)."
    Permissao.Uwb -> "Para usar o UWB, o Android pede acesso a dispositivos próximos."
    Permissao.ListarApps, Permissao.AltaTaxa, Permissao.Infravermelho, Permissao.Nfc -> ""
}

/**
 * Mostra o conteúdo só quando todas as permissões estão concedidas. Antes, explica o porquê com
 * OdEstadoRecurso e pede (ou abre as configurações do Android). Confere de novo ao voltar para o app.
 * Nada é pedido sem a pessoa tocar no botão.
 */
@Composable
fun PortaPermissoes(permissoes: List<Permissao>, motivo: String? = null, conteudo: @Composable () -> Unit) {
    val contexto = LocalContext.current
    var verificacao by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { verificacao++ }
    val faltando = remember(verificacao, permissoes) { permissoes.filter { !contexto.temPermissao(it) } }
    var negadas by rememberSaveable { mutableStateOf(0) }
    val pedir = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        if (r.values.any { !it }) negadas++
        verificacao++
    }
    val vpn = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { verificacao++ }
    if (faltando.isEmpty()) {
        conteudo()
        return
    }
    val p = faltando.first()
    val texto = motivo ?: p.motivoPadrao()
    val activity = contexto as? Activity
    val negouDeVez = p.como == ComoObter.Pedido && negadas > 0 && activity != null &&
        p.manifesto.none { activity.shouldShowRequestPermissionRationale(it) }
    Column {
        when {
            p == Permissao.EstatisticasBateria -> OdEstadoRecurso(
                OdTipoEstado.Permissao, "Precisa de permissão pelo computador",
                "$texto Com o celular ligado ao computador por USB e a depuração USB ativa, rode:\nadb shell pm grant ${contexto.packageName} android.permission.BATTERY_STATS",
                acao = "Copiar comando", aoAgir = {
                    copiarTexto(contexto, "adb shell pm grant ${contexto.packageName} android.permission.BATTERY_STATS")
                },
            )
            p == Permissao.Vpn -> OdEstadoRecurso(OdTipoEstado.Permissao, "Precisa da VPN local", texto, acao = "Permitir VPN local", aoAgir = {
                VpnService.prepare(contexto)?.let { vpn.launch(it) } ?: verificacao++
            })
            p.como == ComoObter.Configuracoes -> OdEstadoRecurso(OdTipoEstado.Permissao, "Precisa de permissão", texto, acao = "Abrir configurações", aoAgir = {
                val acao = if (p == Permissao.UsoDoApp) Settings.ACTION_USAGE_ACCESS_SETTINGS else Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val intent = Intent(acao).apply { if (p == Permissao.Armazenamento) data = Uri.parse("package:${contexto.packageName}") }
                runCatching { contexto.startActivity(intent) }.onFailure { contexto.startActivity(Intent(Settings.ACTION_SETTINGS)) }
            })
            negouDeVez -> OdEstadoRecurso(
                OdTipoEstado.Permissao, "Permissão negada",
                "$texto Para usar, permita ${p.nome.lowercase()} nas configurações do app.", acao = "Abrir configurações",
                aoAgir = { abrirConfiguracoesDoApp(contexto) },
            )
            else -> OdEstadoRecurso(OdTipoEstado.Permissao, "Precisa de permissão", texto, acao = "Permitir ${p.nome.lowercase()}", aoAgir = {
                pedir.launch(faltando.filter { it.como == ComoObter.Pedido && it != Permissao.Vpn }.flatMap { it.manifesto }.toTypedArray())
            })
        }
    }
}

fun abrirConfiguracoesDoApp(contexto: Context) {
    contexto.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${contexto.packageName}")))
}

/**
 * Para permissões opcionais (notificações de um alarme, por exemplo): devolve uma função que pede a
 * permissão se ainda faltar e depois roda a ação, com ou sem a permissão.
 */
@Composable
fun rememberComPermissao(p: Permissao): (() -> Unit) -> Unit {
    val contexto = LocalContext.current
    var pendente by remember { mutableStateOf<(() -> Unit)?>(null) }
    val pedir = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        pendente?.invoke(); pendente = null
    }
    return { acao ->
        if (contexto.temPermissao(p) || p.como != ComoObter.Pedido) acao()
        else { pendente = acao; pedir.launch(p.manifesto.toTypedArray()) }
    }
}
