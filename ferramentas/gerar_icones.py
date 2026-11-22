#!/usr/bin/env python3
"""Gera app/.../ui/FtIcones.kt com os ícones Tabler que o app usa.

Procura todo `FtIcones.Nome` no código do app, converte o nome para o da Tabler
(BatteryCharging -> battery-charging, Ruler2 -> ruler-2, StarFilled -> star-filled, a versão preenchida)
e roda o gerador do próprio Open Design (android/ferramentas/gerar_icones.py), sem editar nada nele.
Os ícones que o Open Design já tem em OdIcones ficam de fora.

Uso (na raiz do repositório):
  python3 ferramentas/gerar_icones.py                      # acha o Open Design em ../open-design*/
  python3 ferramentas/gerar_icones.py --od ../open-design  # caminho explícito
Para um ícone novo: use FtIcones.NomeEmPascalCase no código e rode este script.
"""
import argparse
import re
import subprocess
import sys
import tempfile
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
FONTES = RAIZ / "app/src/main/kotlin"
SAIDA = FONTES / "io/github/andrexagris/ferramentas/ui/FtIcones.kt"
PACOTE = "io.github.andrexagris.ferramentas.ui"


def achar_open_design(explicito):
    candidatos = [Path(explicito)] if explicito else sorted(RAIZ.parent.glob("open-design*/")) + sorted(RAIZ.parent.glob("open-design*/open-design/"))
    for c in candidatos:
        if (c / "android/ferramentas/gerar_icones.py").is_file():
            return c.resolve()
    sys.exit("Não achei o Open Design. Passe --od com a pasta que contém android/ferramentas/gerar_icones.py.")


def nome_tabler(nome):
    partes = re.findall(r"[A-Z][a-z]*|[0-9]+[a-z]*", nome)
    if "".join(partes) != nome:
        sys.exit(f"Nome de ícone fora do padrão: {nome}")
    return "-".join(p.lower() for p in partes)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--od", default=None)
    a = ap.parse_args()
    od = achar_open_design(a.od)

    usados = set()
    for arq in FONTES.rglob("*.kt"):
        if arq != SAIDA:
            usados |= set(re.findall(r"\bFtIcones\.([A-Z][A-Za-z0-9]*)", arq.read_text()))
    if not usados:
        sys.exit("Nenhum FtIcones.Nome encontrado no código.")

    with tempfile.TemporaryDirectory() as tmp:
        bruto = Path(tmp) / "Icones.kt"
        extras = [f"{n}={nome_tabler(n)}" for n in sorted(usados)]
        subprocess.run([sys.executable, str(od / "android/ferramentas/gerar_icones.py"), *extras, "--saida", str(bruto)], check=True, stdout=subprocess.DEVNULL)
        linhas = bruto.read_text().splitlines()

    saida = []
    for linha in linhas:
        m = re.match(r"\s+val (\w+): ImageVector by lazy", linha)
        if m and m.group(1) not in usados:
            continue  # já está em OdIcones
        if linha.startswith("package "):
            saida.append(f"package {PACOTE}")
            continue
        if linha.startswith("import androidx.compose.ui.unit.dp"):
            saida.append(linha)
            saida.append("import io.github.andrexagris.opendesign.OdMedidas")
            continue
        linha = linha.replace("// Gerado por ferramentas/gerar_icones.py", "// Gerado por ferramentas/gerar_icones.py (do app, sobre o gerador do Open Design)")
        linha = linha.replace("/** Ícones Tabler usados pelo Open Design.", "/** Ícones Tabler do app (os dos componentes estão em OdIcones).")
        linha = linha.replace("Icon(OdIcones.Seta", "Icon(FtIcones.Star")
        linha = linha.replace("object OdIcones", "object FtIcones")
        saida.append(linha)
    SAIDA.parent.mkdir(parents=True, exist_ok=True)
    SAIDA.write_text("\n".join(saida) + "\n")
    print(f"{len(usados)} ícones em {SAIDA.relative_to(RAIZ)} (Open Design em {od})")


if __name__ == "__main__":
    main()
