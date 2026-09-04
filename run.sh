#!/usr/bin/env bash
# Runs the whole demo on one machine: tika-grpc, URLFrontier in Docker, the test web server, a
# StormCrawler topology for RUN_MINUTES, then the checker. Everything it writes goes to out/.
#
#   ./run.sh                     StormCrawler 3.7.0, Storm 2.8.9, URLFrontier 2.5
#   TIKA_WT=../tika ./run.sh     the Tika checkout where tika-grpc was built
#   TIKA_PORT=50060 ./run.sh     port for tika-grpc; moved automatically if the port is taken
#   ARCHIVE=1 ./run.sh           refuse to run if this checkout or TIKA_WT has uncommitted changes
#   SKIP_TIKA=1 ./run.sh         do not start tika-grpc; the bolt sends to parsebytes.target
#                                from crawler-conf.yaml
#   FIXTURE_HOST=myhost.lan FIXTURE_BIND=0.0.0.0 SKIP_TIKA=1 ./run.sh
#                                web server reachable from a ParseBytes server on another machine
#
# tika-grpc runs from the jars listed in tika-grpc/target/cp.txt; nothing is installed.
# The web server and the URLFrontier port bind 127.0.0.1 by default. tika-grpc listens on every
# interface, in plaintext.
set -euo pipefail
cd "$(dirname "$0")"

RUN_MINUTES="${RUN_MINUTES:-2}"
TIKA_WT="${TIKA_WT:-../tika-4795-demo}"
SKIP_TIKA="${SKIP_TIKA:-}"
# Maven properties of the pom, overridable here so the pom itself never changes.
SC_VERSION="${SC_VERSION:-3.7.0}"
STORM_VERSION="${STORM_VERSION:-2.8.9}"
URLFRONTIER_VERSION="${URLFRONTIER_VERSION:-2.5}"
FIXTURE_HOST="${FIXTURE_HOST:-parsebytes.127.0.0.1.nip.io}"
FIXTURE_BIND="${FIXTURE_BIND:-127.0.0.1}"
CP_FILE="$TIKA_WT/tika-grpc/target/cp.txt"
PORT="${TIKA_PORT:-50052}"
FRONTIER="tika-demo-frontier-$$"   # one container per run

dirty() { [ -z "$(git -C "$1" status --porcelain --untracked-files=all 2>/dev/null)" ] || echo "-dirty"; }

cleanup() {
    [ -n "${SERVER_PID:-}" ] && kill "$SERVER_PID" 2>/dev/null || true
    [ -n "${TIKA_PID:-}" ] && kill "$TIKA_PID" 2>/dev/null || true
    docker rm -f "$FRONTIER" >/dev/null 2>&1 || true
}
trap cleanup EXIT

# Checks before out/ is touched.
python3 -c 'import socket, sys; socket.gethostbyname(sys.argv[1])' "$FIXTURE_HOST" 2>/dev/null \
    || { echo "$FIXTURE_HOST does not resolve: nip.io names need DNS; offline, put a name in /etc/hosts and pass it as FIXTURE_HOST"; exit 2; }
if [ -n "${ARCHIVE:-}" ]; then
    [ -z "$(dirty .)" ] || { echo "ARCHIVE=1 but this checkout is dirty (git status --porcelain)"; exit 2; }
    [ -n "$SKIP_TIKA" ] || [ -z "$(dirty "$TIKA_WT")" ] || { echo "ARCHIVE=1 but $TIKA_WT is dirty"; exit 2; }
fi
if [ -z "$SKIP_TIKA" ]; then
    [ -f "$CP_FILE" ] || { echo "missing $CP_FILE: build tika-grpc in $TIKA_WT first"; exit 2; }
fi

rm -rf out && mkdir -p out
sed "s#parsebytes.127.0.0.1.nip.io#$FIXTURE_HOST#" testserver/seeds.txt > out/seeds.txt

if [ -z "$SKIP_TIKA" ]; then
    # 50052 is inside the ephemeral port range; move if something already holds it.
    if ! python3 -c 'import socket, sys; s = socket.socket(); s.bind(("", int(sys.argv[1]))); s.close()' "$PORT" 2>/dev/null; then
        PORT=$(python3 -c 'import socket; s = socket.socket(); s.bind(("", 0)); print(s.getsockname()[1])')
        echo ">>> :${TIKA_PORT:-50052} is in use on this host, tika-grpc will listen on :$PORT"
    fi
    { cat crawler-conf.yaml; echo "  parsebytes.target: \"localhost:$PORT\""; } > out/crawler-conf.yaml
    sed -e "s#JAVA_PATH#$(python3 -c 'import os, shutil; print(os.path.realpath(shutil.which("java")))')#" \
        -e "s#PLUGIN_ROOTS#$(cd "$TIKA_WT" && pwd)/tika-grpc/target/plugins#" \
        tika/config.template.json > out/tika-config.json
    echo ">>> starting tika-grpc from $TIKA_WT @ $(git -C "$TIKA_WT" rev-parse --short HEAD)"
    # cp.txt also lists plugin zips; plugins load from plugin-roots, so the zips are dropped.
    java -cp "$TIKA_WT/tika-grpc/target/classes:$(tr ':' '\n' < "$CP_FILE" | grep -v '\.zip$' | paste -s -d : -)" \
        org.apache.tika.pipes.grpc.TikaGrpcServer -c out/tika-config.json -p "$PORT" \
        > out/tika-server.log 2>&1 &
    TIKA_PID=$!
    for _ in $(seq 1 90); do (exec 3<>"/dev/tcp/127.0.0.1/$PORT") 2>/dev/null && break; sleep 1; done
    (exec 3<>"/dev/tcp/127.0.0.1/$PORT") 2>/dev/null || { echo "tika-grpc did not open :$PORT"; tail -40 out/tika-server.log; exit 2; }
fi

[ -f out/crawler-conf.yaml ] || cp crawler-conf.yaml out/crawler-conf.yaml

echo ">>> starting URLFrontier ${URLFRONTIER_VERSION}"
docker run -d --name "$FRONTIER" -p 127.0.0.1:7072:7071 crawlercommons/url-frontier:${URLFRONTIER_VERSION} >/dev/null
for _ in $(seq 1 60); do (exec 3<>"/dev/tcp/127.0.0.1/7072") 2>/dev/null && break; sleep 1; done
(exec 3<>"/dev/tcp/127.0.0.1/7072") 2>/dev/null || { echo "URLFrontier did not open :7072"; docker logs "$FRONTIER" 2>&1 | tail -20; exit 2; }

echo ">>> starting fixture server"
python3 testserver/server.py out/access.log 8099 "$FIXTURE_BIND" &
SERVER_PID=$!
for _ in $(seq 1 40); do (exec 3<>"/dev/tcp/127.0.0.1/8099") 2>/dev/null && break; sleep 0.5; done
(exec 3<>"/dev/tcp/127.0.0.1/8099") 2>/dev/null || { echo "fixture server did not open :8099"; exit 2; }

echo ">>> running topology for ${RUN_MINUTES} minute(s) on stormcrawler ${SC_VERSION} / storm ${STORM_VERSION}"
MVN_EXIT=0
mvn -q -Dstormcrawler.version="${SC_VERSION}" -Dstorm.version="${STORM_VERSION}" compile exec:java \
    -Dexec.args="${RUN_MINUTES} out/seeds.txt out/crawler-conf.yaml" > out/topology.log 2>&1 || MVN_EXIT=$?
# AsyncLocalizer and ZooKeeper-on-::1 lines are Storm LocalCluster noise.
grep -E ">>>|ERROR" out/topology.log | grep -v -E "AsyncLocalizer|ClientCnxn.*(Unable to open socket|Network is unreachable)" || true
[ "$MVN_EXIT" -eq 0 ] || { echo "topology run failed (exit $MVN_EXIT), see out/topology.log"; exit "$MVN_EXIT"; }

echo ">>> manifest"
{
    git rev-parse HEAD >/dev/null 2>&1 && echo "demo_sha=$(git rev-parse HEAD)$(dirty .)"
    if [ -z "$SKIP_TIKA" ]; then
        echo "tika_server_sha=$(git -C "$TIKA_WT" rev-parse HEAD)$(dirty "$TIKA_WT")"
        echo "tika_server_uncommitted_patch_sha256=$(git -C "$TIKA_WT" diff HEAD --full-index --binary --no-textconv --no-ext-diff --no-color --src-prefix=a/ --dst-prefix=b/ | python3 -c 'import hashlib, sys; print(hashlib.sha256(sys.stdin.buffer.read()).hexdigest())')"
        echo "tika_port=$PORT"
    else
        echo "parse_server=external ($(grep -E '^[[:space:]]*parsebytes.target:' crawler-conf.yaml || echo localhost:50052))"
    fi
    echo "tika_version_reported=$(grep -o '"tika_version":"[^"]*"' out/results.jsonl 2>/dev/null | head -1 | cut -d'"' -f4)"
    echo "stormcrawler_version=${SC_VERSION}"
    echo "storm_version=${STORM_VERSION}"
    echo "urlfrontier_version=${URLFRONTIER_VERSION}"
    echo "fixture_host=${FIXTURE_HOST} fixture_bind=${FIXTURE_BIND}"
    echo "protoc=3.25.8 protoc-gen-grpc-java=1.76.0 (runtime: protobuf-java 3.25.8, grpc 1.76.0)"
    python3 -c 'import hashlib, sys
for f in sys.argv[1:]:
    print("sha256", hashlib.sha256(open(f, "rb").read()).hexdigest(), "", f)' proto/org/apache/tika/grpc/v2/*.proto tika/config.template.json testserver/server.py testserver/fixture.pdf testserver/docs/*.pdf testserver/seeds.txt
    echo "java=$(java -version 2>&1 | head -1)"
} | tee out/manifest.txt

echo ">>> verifying"
python3 verify.py out/results.jsonl out/access.log out/seeds.txt | tee out/verify.txt
