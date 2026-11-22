#!/usr/bin/env bash
# Cria a chave de assinatura das releases (uma vez só) e cadastra os segredos no GitHub para o CI assinar.
# Uso, na raiz do repositório:  bash ferramentas/criar_chave.sh
# A chave fica em ~/.android-chaves/ (fora do repositório). FAÇA BACKUP dela: sem a mesma chave,
# o Obtainium não consegue atualizar o app e é preciso desinstalar e instalar de novo.
set -euo pipefail

PASTA="$HOME/.android-chaves"
JKS="$PASTA/ferramentas-release.jks"
TXT="$PASTA/ferramentas-release.txt"
ALIAS="ferramentas"

mkdir -p "$PASTA"; chmod 700 "$PASTA"
if [ -f "$JKS" ]; then
  echo "Já existe $JKS: reaproveitando (nada foi sobrescrito)."
  SENHA=$(sed -n 's/^Senha (keystore e chave): //p' "$TXT")
else
  SENHA=$(openssl rand -base64 24 | tr -d '/+=' | head -c 28)
  keytool -genkeypair -keystore "$JKS" -storetype PKCS12 -alias "$ALIAS" -keyalg RSA -keysize 4096 -validity 36500 \
    -storepass "$SENHA" -keypass "$SENHA" -dname "CN=Projeto Ferramentas" >/dev/null
  cat > "$TXT" <<EOF
Chave de assinatura do Projeto Ferramentas (io.github.andrexagris.ferramentas)
Guarde este arquivo e o .jks com backup fora deste computador.
Arquivo: $JKS
Alias: $ALIAS
Senha (keystore e chave): $SENHA
EOF
  chmod 600 "$JKS" "$TXT"
  echo "Chave criada em $JKS (senha em $TXT)."
fi

# Segredos do repositório, usados pelo workflow .github/workflows/release.yml
base64 -w0 "$JKS" | gh secret set FT_KEYSTORE_BASE64
printf %s "$SENHA" | gh secret set FT_KEYSTORE_SENHA
printf %s "$ALIAS" | gh secret set FT_CHAVE_ALIAS
printf %s "$SENHA" | gh secret set FT_CHAVE_SENHA

# Para gerar a release assinada localmente também (arquivo ignorado pelo git).
printf 'storeFile=%s\nstorePassword=%s\nkeyAlias=%s\nkeyPassword=%s\n' "$JKS" "$SENHA" "$ALIAS" "$SENHA" > keystore.properties
chmod 600 keystore.properties

echo "Segredos cadastrados no GitHub e keystore.properties criado. Pronto para publicar releases."
