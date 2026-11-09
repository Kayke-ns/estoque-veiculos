#!/bin/zsh
setopt NULL_GLOB
# Compila, testa, sobe o sistema (perfil dev, H2 em memoria) e captura as telas do relatorio.
cd "$(dirname "$0")/.." || exit 1
mkdir -p docs .tools
exec > >(tee docs/execucao.log) 2>&1
echo "== Sistema de Estoque de Veiculos: execucao automatica =="

# ---------- Java 17+ ----------
JAVA_HOME_ENC=""
for c in "$(/usr/libexec/java_home -v 17+ 2>/dev/null)" \
         "/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
         "/Applications/Android Studio.app/Contents/jre/Contents/Home" \
         $HOME/Library/Java/JavaVirtualMachines/*/Contents/Home \
         /opt/homebrew/opt/openjdk*/libexec/openjdk.jdk/Contents/Home; do
  if [[ -n "$c" && -x "$c/bin/java" ]]; then
    v=$("$c/bin/java" -version 2>&1 | head -1 | sed -E 's/.*version "([0-9]+).*/\1/')
    if [[ "$v" -ge 17 ]]; then JAVA_HOME_ENC="$c"; break; fi
  fi
done
if [[ -z "$JAVA_HOME_ENC" ]]; then
  echo "Baixando JDK 21 (Temurin)..."
  curl -fsSL -o .tools/jdk.tar.gz "https://api.adoptium.net/v3/binary/latest/21/ga/mac/aarch64/jdk/hotspot/normal/eclipse" || exit 1
  mkdir -p .tools/jdk && tar -xzf .tools/jdk.tar.gz -C .tools/jdk --strip-components=1
  JAVA_HOME_ENC="$PWD/.tools/jdk/Contents/Home"
fi
export JAVA_HOME="$JAVA_HOME_ENC"
export PATH="$JAVA_HOME/bin:$PATH"
echo "Java: $JAVA_HOME"; java -version 2>&1 | head -1

# ---------- Maven ----------
if command -v mvn >/dev/null 2>&1; then MVN=mvn; else
  if [[ ! -x .tools/maven/bin/mvn ]]; then
    echo "Baixando Maven 3.9.9..."
    curl -fsSL -o .tools/maven.tar.gz "https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz" || exit 1
    mkdir -p .tools/maven && tar -xzf .tools/maven.tar.gz -C .tools/maven --strip-components=1
  fi
  MVN="$PWD/.tools/maven/bin/mvn"
fi
echo "Maven: $MVN"

# ---------- Testes ----------
echo "== Rodando testes =="
$MVN -B test 2>&1 | tee docs/testes.log | grep -E "Tests run|BUILD|ERROR"
grep -q "BUILD SUCCESS" docs/testes.log || { echo "FALHA NOS TESTES"; exit 1; }

# ---------- Build e execucao ----------
echo "== Empacotando =="
$MVN -B -q package -DskipTests || exit 1
lsof -ti tcp:8080 | xargs kill 2>/dev/null
java -jar target/estoque-veiculos-1.0.0.jar --spring.profiles.active=dev > docs/app.log 2>&1 &
APP=$!
for i in {1..60}; do curl -fs http://localhost:8080/api/veiculos >/dev/null && break; sleep 1; done
curl -fs http://localhost:8080/api/veiculos/resumo && echo

# ---------- Capturas ----------
echo "== Capturando telas =="
NODE=$(command -v node || ls /opt/homebrew/bin/node /usr/local/bin/node 2>/dev/null | head -1)
"$NODE" scripts/capturar-telas.mjs
echo "== Encerrando aplicacao =="
kill $APP
echo "FIM_OK"
