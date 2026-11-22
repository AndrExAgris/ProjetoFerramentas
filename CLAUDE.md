# Projeto Ferramentas

App Android nativo (Kotlin + Jetpack Compose) com 128 ferramentas que usam sensores, rádios e APIs do celular. O briefing completo, com o catálogo, as permissões e o hardware dos aparelhos, está em `BRIEFING.md`: em caso de dúvida, ele decide.

## Resumo do briefing

- **Aparelhos-alvo:** Galaxy S20 FE (Android 13, `minSdk` 33) e Galaxy A24 (Android 15+). Os dois têm que funcionar. APIs do Android 14+ atrás de checagem de versão.
- **Ferramentas para aparelhos futuros** (barômetro, UWB, infravermelho, profundidade) entram no catálogo e aparecem em cinza com o motivo.
- **Navegação:** uma Activity, Navigation Compose. Barra inferior com Início (grade dos 13 grupos), Favoritos, Buscar e Mais (Configurações: Aparência primeiro, depois as opções do app, Sobre no fim). Caminho: Início → grupo → lista por seção → ferramenta, com Voltar funcionando em cada passo.
- **Dados:** DataStore para preferências, Room para históricos (quando a primeira ferramenta com histórico for feita), exportação em CSV.
- **Segundo plano:** foreground service para medições longas, WorkManager para tarefas periódicas (mínimo 15 min).
- **Concorrência:** coroutines e Flow; cada sensor exposto como `Flow`.
- **Rede:** sem análise de uso nem rastreadores. Internet só nas ferramentas que precisam (`usaInternet = true` no catálogo), e isso aparece na tela da ferramenta.
- **Interface em português do Brasil. Licença GPL-3.0.**
- **Em aberto:** quais ferramentas entram (coluna "Quero?" da planilha), distribuição (GitHub Releases, F-Droid ou Play Store).

## Estrutura

```
app/src/main/kotlin/io/github/andrexagris/ferramentas/
  MainActivity.kt            enableEdgeToEdge + OpenDesignTema(prefs)
  FerramentasApp.kt          Application: preferências e disponibilidade calculada uma vez
  catalogo/Modelo.kt         Grupo (13, com seções), Ferramenta, Requisito, Req, Permissao
  catalogo/Catalogo.kt       as 128 ferramentas, na ordem do briefing
  catalogo/Disponibilidade.kt verifica hardware, sensores e versão do Android
  dados/PreferenciasApp.kt   DataStore: favoritos e filtro "só o que funciona aqui"
  ui/App.kt                  NavHost: um grafo por aba (cada aba guarda a própria pilha)
  ui/Estado.kt               EstadoApp (favoritos, filtro, disponibilidade) e ícone de cada grupo
  ui/FtIcones.kt             GERADO por ferramentas/gerar_icones.py; não edite à mão
  ui/componentes/            componentes próprios (prefixo Ft)
  ui/telas/                  Início, Grupo, Favoritos, Buscar, Ferramenta, Licenças
opendesign/                  cópia do módulo Compose do Open Design
ferramentas/gerar_icones.py  gera FtIcones.kt
```

## Como adicionar uma ferramenta

1. A entrada já existe em `Catalogo.kt` (id, grupo, seção, ícone, requisitos, permissões). Ajuste os requisitos se o teste no aparelho mostrar outra coisa.
2. Crie a tela em `ui/ferramentas/<grupo>/` e troque o "Em construção" de `TelaFerramenta` por ela (um `when (ferramenta.id)`, ou um campo de tela no catálogo quando houver várias prontas).
3. Declare as permissões no `AndroidManifest.xml` só quando a ferramenta for implementada. Peça na hora em que ela abre, com `OdEstadoRecurso(OdTipoEstado.Permissao, …)` explicando o porquê antes. Nada é pedido na abertura do app.
4. Sensores como `Flow` (callbackFlow + awaitClose para desregistrar).

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
- Versões das bibliotecas em `gradle/libs.versions.toml`. AGP 9.5 (Kotlin embutido, não aplique `org.jetbrains.kotlin.android`), Kotlin 2.4, Compose BOM 2026.11, `compileSdk` 37.2, `targetSdk` 37, JDK 17 (toolchain baixado pelo foojay se faltar).
- `applicationId` provisório: `io.github.andrexagris.ferramentas` (o debug ganha `.debug`). Ainda em aberto no briefing.
- Antes de commitar: `./gradlew assembleDebug testDebugUnitTest lintDebug` (o mesmo que o CI roda). Os testes do catálogo conferem a contagem por grupo do briefing.

## Ordem de construção (do briefing)

1. Primeiras ferramentas, que validam as APIs mais arriscadas: painel ao vivo da bateria, corrente e potência, teste de consumo em standby, uso de dados por app e lista de sensores.
2. Testes de hardware: NFC no A24, MIFARE Classic, ciclos de carga, Wi-Fi RTT, dupla frequência e ARCore. O resultado atualiza `aparelhos` no catálogo.
3. Grupo por grupo, na ordem das marcadas "Sim" na planilha.
