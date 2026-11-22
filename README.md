# Projeto Ferramentas

App Android nativo que reúne mais de 120 ferramentas que usam os sensores, rádios e APIs do próprio celular: bateria, internet, sinais, NFC, localização, medição, movimento, ambiente, som, câmera, saúde e aparelho. Tudo funciona só com o aparelho, sem acessórios e sem servidor próprio, e sem análise de uso nem rastreadores.

Feito em Kotlin com Jetpack Compose, com o visual do Open Design. Aparelhos-alvo: Galaxy S20 FE (Android 13) e Galaxy A24 (Android 15+). Ferramentas que pedem hardware que o aparelho não tem aparecem em cinza, com o motivo.

**Situação:** esqueleto pronto (navegação, grupos, busca, favoritos, configurações e o catálogo das 128 ferramentas). As ferramentas em si ainda abrem uma tela "Em construção".

## Compilar

Requisitos: JDK 17 ou mais novo (o Gradle baixa o 17 se faltar) e o Android SDK com a plataforma 37.2.

```bash
./gradlew assembleDebug          # APK em app/build/outputs/apk/debug/
./gradlew installDebug           # instala no celular conectado por USB (depuração USB ligada)
./gradlew testDebugUnitTest      # testes
```

O módulo `opendesign/` é uma cópia do Open Design (veja `CLAUDE.md`). O briefing completo do projeto está em `BRIEFING.md`.

## Licença

GPL-3.0 (arquivo `LICENSE`). Fontes Geist, Literata e JetBrains Mono sob SIL OFL 1.1 e ícones Tabler sob MIT; os textos ficam em `app/src/main/assets/licencas/` e na tela Mais > Sobre o app > Licenças.
