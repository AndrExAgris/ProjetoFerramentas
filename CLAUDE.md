# Projeto Ferramentas

App Android nativo (Kotlin + Jetpack Compose) com 128 ferramentas que usam sensores, rádios e APIs do celular. O briefing completo, com o catálogo, as permissões e o hardware dos aparelhos, está em `BRIEFING.md`: em caso de dúvida, ele decide.

## Resumo do briefing

- **Aparelhos-alvo:** Galaxy S20 FE (Android 13, `minSdk` 33) e Galaxy A24 (Android 15+). Os dois têm que funcionar. APIs do Android 14+ atrás de checagem de versão.
- **Ferramentas para aparelhos futuros** (barômetro, UWB, infravermelho, profundidade) entram no catálogo e aparecem em cinza com o motivo.
- **Navegação:** uma Activity, Navigation Compose. Barra inferior com Início (grade dos 13 grupos), Favoritos, Buscar e Mais (Configurações: Aparência primeiro, depois as opções do app, Sobre no fim). Caminho: Início → grupo → lista por seção → ferramenta, com Voltar funcionando em cada passo.
- **Dados:** DataStore para preferências, Room para históricos, exportação em CSV.
- **Segundo plano:** foreground service para medições longas, WorkManager para tarefas periódicas (mínimo 15 min).
- **Concorrência:** coroutines e Flow; cada sensor exposto como `Flow`.
- **Rede:** sem análise de uso nem rastreadores. Internet só nas ferramentas que precisam (`usaInternet = true` no catálogo), e isso aparece na tela da ferramenta.
- **Interface em português do Brasil. Licença GPL-3.0.**
- **Distribuição:** APK assinado nas Releases do GitHub, instalado e atualizado pelo Obtainium (ver Releases).
- **Em aberto:** quais ferramentas entram (coluna "Quero?" da planilha).

## Estrutura

```
app/src/main/kotlin/io/github/andrexagris/ferramentas/
  MainActivity.kt              enableEdgeToEdge + OpenDesignTema; teclas físicas para as ferramentas
  FerramentasApp.kt            canais de notificação, lanterna, agenda a tarefa periódica se preciso
  catalogo/                    Modelo (Grupo, Ferramenta, Requisito, Permissao), Catalogo (128), Disponibilidade
  base/                        infraestrutura comum das ferramentas:
    Sensores.kt                  fluxoSensor/rememberSensor (sensores como Flow), Serie, MinMax, orientação
    Permissoes.kt                PortaPermissoes (explica antes de pedir), rememberComPermissao (opcionais)
    Audio.kt                     fluxoMicrofone, FFT, ponderação A, YIN (nota), SaidaAudio (geração), bipe
    Camera.kt                    VistaCamera (CameraX: preview, análise, captura, Camera2 manual)
    Nfc.kt                       LeitorNfc (modo leitor enquanto a tela está aberta), hex
    Bateria.kt, Rede.kt, UsoDados.kt, Lanterna.kt, Codigos.kt (ZXing), Teclas.kt, Notificacoes.kt, Util.kt (CSV, compartilhar, formatos)
  dados/Banco.kt               Room: Amostra (série temporal genérica) e ItemSalvo (listas salvas); sem migração para ferramenta nova
  dados/PreferenciasApp.kt     DataStore: favoritos, filtro e ajustes por ferramenta (rememberAjusteFloat/Texto/Bool)
  servicos/                    ServicoMedicao (standby, trajeto, log celular), ServicoVelocidade, ServicoVpnDns,
                               TrabalhoPeriodico (WorkManager, 15 min), Blocos (painel rápido), AtividadeGatilho (NFC)
  widgets/                     Glance: bateria e lanterna
  ui/App.kt                    NavHost: um grafo por aba
  ui/ferramentas/Registro.kt   id do catálogo → tela (TelaDeFerramenta); um mapa por grupo
  ui/ferramentas/Kit.kt        FtColuna, FtPainel, FtInfos, FtCampo, FtBotaoMedir, FtDeslizante, FtGraficoHistorico, FtBarra, LocalToasts
  ui/ferramentas/<grupo>/      as telas das ferramentas de cada grupo
  ui/telas/                    Início, Grupo, Favoritos, Buscar, Ferramenta, Licenças
app/src/main/assets/           licencas/ (textos exibidos no app), tessdata/por.traineddata (OCR)
opendesign/                    cópia do módulo Compose do Open Design
ferramentas/gerar_icones.py    gera ui/FtIcones.kt
ferramentas/criar_chave.sh     cria a chave de assinatura e os segredos do GitHub (uma vez)
docs/versoes/vX.Y.Z.md         notas de cada release
```

## Como adicionar ou mudar uma ferramenta

1. Entrada em `Catalogo.kt` (id, grupo, seção, ícone, requisitos, permissões, `internet`). Os requisitos decidem o cinza; as permissões aparecem nos detalhes e formam a porta de permissões.
2. Tela em `ui/ferramentas/<grupo>/<Grupo>.kt` e uma linha no mapa `telas<Grupo>()`: `"id" com { Tela() }`. A `TelaFerramenta` já põe barra superior, detalhes, favorito, toasts (`LocalToasts`) e a porta de permissões. Use `"id".com(lista) { … }` para escolher outra porta (ex.: `emptyList()` quando a permissão é opcional e a tela pede com `rememberComPermissao`).
3. Permissão nova: declarar no `AndroidManifest.xml` e em `Permissao` (com `motivoPadrao`). Nada é pedido na abertura do app.
4. Sensores como `Flow` (`fluxoSensor`); histórico em `Amostra` (série nova em `Series`); listas salvas em `ItemSalvo` (tipo novo em `Tipos`); tarefa com o app fechado em `TrabalhoPeriodico` (chave em `ChavesPeriodicas`); medição longa em `ServicoMedicao` (nova `TarefaMedicao`).
5. Os testes conferem que toda ferramenta do catálogo tem tela e a contagem por grupo do briefing.

## Teste

- `./gradlew assembleDebug testDebugUnitTest lintDebug` (o mesmo do CI).
- No build de debug, `adb shell am start -n io.github.andrexagris.ferramentas.debug/io.github.andrexagris.ferramentas.MainActivity --es ferramenta <id>` abre uma ferramenta direto. Dá para percorrer as 128 assim (conceda permissões com `pm grant` e `appops set`; use `</dev/null` nos `adb` dentro de laços).
- Testado em emulador Android 13 e Android 15 (as 128 telas sem travar; serviços, VPN, áudio, OCR, exportação e teclas de volume exercitados).

## Releases (Obtainium)

- O Obtainium acompanha as Releases do GitHub. Para publicar: escreva `docs/versoes/vX.Y.Z.md`, commit, `git tag vX.Y.Z && git push origin vX.Y.Z`. O workflow `release.yml` testa, gera o APK assinado (`ferramentas-vX.Y.Z.apk`) e cria a release.
- A versão vem da tag: `versionName` = X.Y.Z e `versionCode` = X·10000 + Y·100 + Z (sempre crescente). Localmente, sem tag, é 0.1.0.
- A chave fica em `~/.android-chaves/` (fora do repositório) e nos segredos `FT_KEYSTORE_BASE64`, `FT_KEYSTORE_SENHA`, `FT_CHAVE_ALIAS`, `FT_CHAVE_SENHA`. Trocar a chave quebra as atualizações no celular.
- Fluxo combinado: uma versão grande por vez; o André testa no celular e volta com um pedido de correções para a versão seguinte.

## Open Design

- **Origem da cópia em `opendesign/`:** pacote `open-design-1.1.zip` (pasta `../open-design-1.1/open-design/android/opendesign/`), copiado em 22/11/2026. Nada em `../open-design-1.1/` é editado; quando o design system mudar, copie de novo e reaplique os ajustes abaixo.
- **Ajustes feitos na cópia** (marcados com `[Projeto Ferramentas]` no código, nenhum muda o visual):
  - `opendesign/build.gradle.kts`: sem o plugin `org.jetbrains.kotlin.android` (o AGP 9 traz o Kotlin embutido e recusa o plugin) e `compileSdk` 37.2 (era 36; o Compose 1.13 pede 37.1+).
  - `Configuracoes.kt` linha 272: `toasts?.mostrar(…, aoAgir = { … })` com argumento nomeado. Sem o nome, a lambda caía no parâmetro `duracaoMs` e não compilava. Cuidado com o mesmo padrão no app: o exemplo da documentação (`mostrar("…", acao = "Desfazer") { … }`) não compila.
- O pacote está em `../open-design-1.1/open-design/` (o briefing fala em `../open-design/`). O script de ícones acha qualquer `../open-design*/`.

### Regras de design

- Nenhuma cor, tamanho ou fonte escrita à mão: sempre `OdTema.cores`, `OdTema.tipografia`, `OdTema.densidade`, `OdTema.sombras` e as medidas `OdEspacos`, `OdRaios`, `OdMedidas`. Exceção única: `res/values*/colors.xml`, só para a janela antes do Compose e o ícone do app, com valores de `tokens-resolvidos.json`.
- Use os componentes `Od*` (barra inferior, `OdTelaApp`, `OdGrade`, `OdCartaoLeitura`/`OdLeitura`/`odFormatar`, `OdEstadoRecurso`/`OdChipEstado`/`OdPontoAoVivo`, `OdGraficoTempoReal`, escalas e instrumentos, `OdTelaConfiguracoes`/`OdGrupoConfig`, `OdDialogo(destrutivo = true)`, `OdFolhaInferior`, `OdSegmentado`, `OdInterruptor`, `OdBotao`, `rememberOdToasts`). O que não existir: componente no app com prefixo `Ft`, só com tokens.
- Ícones: só Tabler, traço por padrão (preenchido só no item ativo). Use `FtIcones.NomeEmPascalCase` (ex.: `FtIcones.BatteryCharging` = `battery-charging`, `FtIcones.StarFilled` = versão preenchida de `star`) e rode `python3 ferramentas/gerar_icones.py`. Os de `OdIcones` já existem.
- Cores dinâmicas do Android desligadas; "Sistema" só como opção de cor principal (já no `OdConteudoAparencia`). Tema claro, escuro ou sistema só em Configurações > Aparência.
- `primaria` para ação principal e navegação ativa; `secundaria` para categorias e etiquetas (ex.: ícone dos grupos).
- Estado nunca só por cor: sempre ícone e texto. Área de toque de pelo menos 48 dp.
- Escrita: botões dizem o que acontece ("Iniciar teste", "Gravar na tag"), maiúscula só no início da frase, toasts curtos no passado ("Medição salva."), erros dizem como resolver, sem emoji. Títulos de diálogo em pergunta que nomeia o objeto.
- Testar em modo claro e escuro, com fonte grande e com TalkBack.

## Código

- Nomes em português, como no Open Design (`aoClicar`, `aoVoltar`, `estado`). Composables de tela começam com `Tela`.
- Versões das bibliotecas em `gradle/libs.versions.toml`. AGP 9.5 (Kotlin embutido, não aplique `org.jetbrains.kotlin.android`), Kotlin 2.4, KSP 2.3 (Room), Compose BOM 2026.11, CameraX, WorkManager, Glance, ZXing, dnsjava, Tesseract4Android (JitPack), `compileSdk` 37.2, `targetSdk` 37, JDK 17.
- Release sem R8 (dnsjava e Tesseract usam reflexão/JNI). APK de release só com arm64-v8a; o de debug também tem x86_64 (emulador).
- APIs acima do Android 13 sempre atrás de `Build.VERSION.SDK_INT` (o lint acusa `NewApi`/`InlinedApi`). Ex.: o tipo `specialUse` de serviço só existe no 14+ (`TIPO_USO_ESPECIAL`).
- Teste de velocidade usa os servidores da Cloudflare (o briefing citava LibreSpeed, que não tem lista pública estável). IP externo usa ipinfo.io.
- `applicationId` provisório: `io.github.andrexagris.ferramentas` (o debug ganha `.debug`). Ainda em aberto no briefing.
- Antes de commitar: `./gradlew assembleDebug testDebugUnitTest lintDebug` (o mesmo que o CI roda). Os testes do catálogo conferem a contagem por grupo do briefing.

## Situação (v0.1.0)

Todas as 128 ferramentas têm tela funcional. Parciais: trena AR (só confere o ARCore), scanner 3D e UWB (só aviso; nenhum dos aparelhos tem o hardware), infravermelho (sem base de códigos), scanner de documentos (sem correção de perspectiva), sonar (experimental). Próximo: correções a partir dos testes do André nos aparelhos e os testes de hardware do briefing (NFC no A24, MIFARE, ciclos, Wi-Fi RTT, dupla frequência, ARCore), que atualizam `aparelhos` no catálogo.
