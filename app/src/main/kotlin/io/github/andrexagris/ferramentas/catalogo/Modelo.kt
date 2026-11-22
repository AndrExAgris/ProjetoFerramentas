package io.github.andrexagris.ferramentas.catalogo

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/** Os 13 grupos da tela Início, na ordem do README, com as seções de cada um. */
enum class Grupo(val id: String, val nome: String, val secoes: List<String>) {
    Bateria("bateria", "Bateria", listOf("Ao vivo", "Saúde", "Testes", "Histórico e alertas")),
    Internet("internet", "Internet", listOf("Uso de dados", "Wi-Fi", "Diagnóstico")),
    Sinais("sinais", "Sinais", listOf("Rede celular", "Bluetooth", "Outros rádios")),
    Nfc("nfc", "NFC", listOf("Ler", "Gravar", "Proteger", "Avançado", "Automação")),
    Localizacao("localizacao", "Localização", listOf("Posição", "Direção", "Trajeto")),
    Medicao("medicao", "Medição", listOf("Tela", "Inclinação", "Distância e altura")),
    Movimento("movimento", "Movimento", listOf("Sensores", "Experimentos")),
    Ambiente("ambiente", "Ambiente", listOf("Magnetismo", "Luz", "Clima")),
    Som("som", "Som", listOf("Medir", "Música", "Gerar", "Testes", "Experimentos")),
    Camera("camera", "Câmera", listOf("Códigos", "Imagem", "Documentos", "Flash")),
    Saude("saude", "Saúde", listOf("Coração", "Atividade")),
    Aparelho("aparelho", "Aparelho", listOf("Informações", "Testes", "Desempenho", "Uso e armazenamento")),
    Utilidades("utilidades", "Utilidades", listOf("Geral"));

    companion object {
        fun deId(id: String?): Grupo? = entries.firstOrNull { it.id == id }
    }
}

/** Coluna "Seus aparelhos" do README: se roda no Galaxy S20 FE e no Galaxy A24. */
enum class SeusAparelhos(val rotulo: String) {
    OsDois("Os dois"),
    OsDoisLimitado("Os dois (limitado)"),
    SoA24("Só A24"),
    Verificar("Verificar no aparelho"),
    AparelhoFuturo("Aparelho futuro"),
}

enum class Dificuldade(val rotulo: String) { Facil("Fácil"), Media("Média"), Dificil("Difícil") }

/**
 * O que a ferramenta precisa para funcionar no aparelho. Se faltar, ela aparece em cinza com [falta].
 * Permissão não entra aqui: ela é pedida quando a ferramenta abre (veja [Permissao]).
 */
sealed interface Requisito {
    /** Frase curta que diz o que falta, já no formato da tela ("Este aparelho não tem barômetro."). */
    val falta: String

    /** Recurso de hardware declarado pelo fabricante (PackageManager.hasSystemFeature). */
    data class Recurso(val recurso: String, override val falta: String) : Requisito

    /** Sensor físico ou virtual (SensorManager.getDefaultSensor). */
    data class Sensor(val tipo: Int, override val falta: String) : Requisito

    /** Versão mínima do Android (API). */
    data class AndroidMinimo(val api: Int, val versao: String) : Requisito {
        override val falta = "Precisa do Android $versao ou mais novo."
    }

    /** Câmera com saída de profundidade (DEPTH_OUTPUT do Camera2), como um sensor ToF. */
    data object CameraProfundidade : Requisito {
        override val falta = "Este aparelho não tem sensor de profundidade."
    }

    /** Motor de vibração (VibratorManager). */
    data object Vibrador : Requisito {
        override val falta = "Este aparelho não tem motor de vibração."
    }

    /** O Android não tem API padrão para isso: fica em cinza em todo aparelho até haver suporte. */
    data class SemApiPadrao(override val falta: String) : Requisito
}

/** Requisitos usados por mais de uma ferramenta. */
object Req {
    val Wifi = Requisito.Recurso(PackageManager.FEATURE_WIFI, "Este aparelho não tem Wi-Fi.")
    val WifiRtt = Requisito.Recurso(PackageManager.FEATURE_WIFI_RTT, "Este aparelho não tem Wi-Fi RTT.")
    val Telefonia = Requisito.Recurso(PackageManager.FEATURE_TELEPHONY, "Este aparelho não tem rede celular.")
    val Bluetooth = Requisito.Recurso(PackageManager.FEATURE_BLUETOOTH, "Este aparelho não tem Bluetooth.")
    val BluetoothLe = Requisito.Recurso(PackageManager.FEATURE_BLUETOOTH_LE, "Este aparelho não tem Bluetooth LE.")
    val Infravermelho = Requisito.Recurso(PackageManager.FEATURE_CONSUMER_IR, "Este aparelho não tem emissor infravermelho.")
    // Texto literal: a constante FEATURE_UWB só existe a partir da API 34, e o minSdk é 33.
    val Uwb = Requisito.Recurso("android.hardware.uwb", "Este aparelho não tem UWB.")
    val Nfc = Requisito.Recurso(PackageManager.FEATURE_NFC, "Este aparelho não tem NFC.")
    val MifareClassic = Requisito.Recurso("com.nxp.mifare", "O NFC deste aparelho não lê MIFARE Classic.")
    val Gps = Requisito.Recurso(PackageManager.FEATURE_LOCATION_GPS, "Este aparelho não tem GPS.")
    val Camera = Requisito.Recurso(PackageManager.FEATURE_CAMERA_ANY, "Este aparelho não tem câmera.")
    val Flash = Requisito.Recurso(PackageManager.FEATURE_CAMERA_FLASH, "Este aparelho não tem flash.")
    val ArCore = Requisito.Recurso(PackageManager.FEATURE_CAMERA_AR, "Este aparelho não é compatível com o ARCore.")
    val Microfone = Requisito.Recurso(PackageManager.FEATURE_MICROPHONE, "Este aparelho não tem microfone.")
    val Acelerometro = Requisito.Sensor(Sensor.TYPE_ACCELEROMETER, "Este aparelho não tem acelerômetro.")
    val Giroscopio = Requisito.Sensor(Sensor.TYPE_GYROSCOPE, "Este aparelho não tem giroscópio.")
    val Magnetometro = Requisito.Sensor(Sensor.TYPE_MAGNETIC_FIELD, "Este aparelho não tem magnetômetro.")
    val Luz = Requisito.Sensor(Sensor.TYPE_LIGHT, "Este aparelho não tem sensor de luz.")
    val Pressao = Requisito.Sensor(Sensor.TYPE_PRESSURE, "Este aparelho não tem barômetro.")
    val TemperaturaAmbiente = Requisito.Sensor(Sensor.TYPE_AMBIENT_TEMPERATURE, "Este aparelho não tem termômetro ambiente.")
    val Umidade = Requisito.Sensor(Sensor.TYPE_RELATIVE_HUMIDITY, "Este aparelho não tem sensor de umidade.")
    val Passos = Requisito.Sensor(Sensor.TYPE_STEP_COUNTER, "Este aparelho não tem contador de passos.")
    val FrequenciaCardiaca = Requisito.Sensor(Sensor.TYPE_HEART_RATE, "Este aparelho não tem sensor de frequência cardíaca.")
    val Android14 = Requisito.AndroidMinimo(34, "14")
}

/** Como a permissão é obtida (coluna "Como se obtém" do README). */
enum class ComoObter(val rotulo: String) {
    Pedido("Pedido na hora"),
    Configuracoes("Liberada nas configurações do Android"),
    Automatica("Automática"),
    Adb("Uma vez, por ADB ou Shizuku"),
}

/** Permissões do README. Cada uma é pedida só quando a ferramenta que precisa dela abre, com explicação antes. */
enum class Permissao(val nome: String, val como: ComoObter, val manifesto: List<String>) {
    UsoDoApp("Acesso ao uso", ComoObter.Configuracoes, listOf(Manifest.permission.PACKAGE_USAGE_STATS)),
    Localizacao("Localização", ComoObter.Pedido, listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)),
    WifiProximo("Dispositivos Wi-Fi próximos", ComoObter.Pedido, listOf(Manifest.permission.NEARBY_WIFI_DEVICES)),
    Bluetooth("Dispositivos Bluetooth próximos", ComoObter.Pedido, listOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)),
    Camera("Câmera", ComoObter.Pedido, listOf(Manifest.permission.CAMERA)),
    Microfone("Microfone", ComoObter.Pedido, listOf(Manifest.permission.RECORD_AUDIO)),
    Atividade("Atividade física", ComoObter.Pedido, listOf(Manifest.permission.ACTIVITY_RECOGNITION)),
    SensoresCorporais("Sensores corporais", ComoObter.Pedido, listOf(Manifest.permission.BODY_SENSORS)),
    AltaTaxa("Sensores em alta frequência", ComoObter.Automatica, listOf(Manifest.permission.HIGH_SAMPLING_RATE_SENSORS)),
    Notificacoes("Notificações", ComoObter.Pedido, listOf(Manifest.permission.POST_NOTIFICATIONS)),
    Vpn("VPN local", ComoObter.Pedido, listOf(Manifest.permission.BIND_VPN_SERVICE)),
    ListarApps("Ver os apps instalados", ComoObter.Automatica, listOf(Manifest.permission.QUERY_ALL_PACKAGES)),
    Armazenamento("Acesso a todos os arquivos", ComoObter.Configuracoes, listOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE)),
    EstatisticasBateria("Estatísticas da bateria", ComoObter.Adb, listOf(Manifest.permission.BATTERY_STATS)),
    Infravermelho("Infravermelho", ComoObter.Automatica, listOf(Manifest.permission.TRANSMIT_IR)),
    Uwb("UWB", ComoObter.Pedido, listOf(Manifest.permission.UWB_RANGING)),
    Nfc("NFC", ComoObter.Automatica, listOf(Manifest.permission.NFC)),
}

/**
 * Uma ferramenta do catálogo. Adicionar uma ferramenta = criar a tela e uma entrada em [Catalogo].
 * nome: curto, para listas. descricao: o detalhe da coluna "Ferramenta" do README.
 */
@Immutable
data class Ferramenta(
    val id: String,
    val grupo: Grupo,
    val secao: String,
    val nome: String,
    val descricao: String?,
    val icone: ImageVector,
    val aparelhos: SeusAparelhos,
    val dificuldade: Dificuldade,
    val api: String,
    val observacao: String?,
    val requisitos: List<Requisito>,
    val permissoes: List<Permissao>,
    val usaInternet: Boolean,
)
