#!/usr/bin/env bash
# Runs the whole probe on one machine. Everything it produces lands in out/.
#
#   ./run.sh                     released stack: StormCrawler 3.7.0, Storm 2.8.9, URLFrontier 2.5
#   SKIP_TIKA=1 ./run.sh         do not start tika-grpc; the bolt talks to parsebytes.target
#                                from crawler-conf.yaml (another ParseBytes implementation)
#   TIKA_WT=../tika ./run.sh     where tika-grpc was built (README, step 1)
#   TIKA_PORT=50060 ./run.sh     port for tika-grpc (default 50052; moved automatically if taken)
#   ARCHIVE=1 ./run.sh           refuse to run from a dirty checkout (this one or TIKA_WT):
#                                a run kept as evidence must name code anyone can check out
#   FIXTURE_HOST=myhost.lan FIXTURE_BIND=0.0.0.0 SKIP_TIKA=1 ./run.sh
#                                ParseBytes server on another machine: the fixture must be
#                                reachable FROM that machine under FIXTURE_HOST, or "single
#                                acquisition" proves nothing (a re-fetch that cannot connect
#                                is never logged)
#
# The Tika server is started from the reactor jars listed in tika-grpc/target/cp.txt, not from
# ~/.m2, so the run is pinned to the commit that was built and nothing gets installed.
#
# Network: by default the fixture server and the URLFrontier port bind 127.0.0.1 only.
# tika-grpc has no bind-address option and listens on every interface, in plaintext. Run this
# on a machine or network you trust; the probe itself only ever talks to localhost.
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
FRONTIER="parsebytes-probe-frontier-$$"   # unique per run: never removes a container we did not start

# Staged and untracked files count: `git diff --quiet` alone is blind to the index.
dirty() { [ -z "$(git -C "$1" status --porcelain --untracked-files=all 2>/dev/null)" ] || echo "-dirty"; }

cleanup() {
    [ -n "${SERVER_PID:-}" ] && kill "$SERVER_PID" 2>/dev/null || true
    [ -n "${TIKA_PID:-}" ] && kill "$TIKA_PID" 2>/dev/null || true
    docker rm -f "$FRONTIER" >/dev/null 2>&1 || true
}
trap cleanup EXIT

# Preflight before anything is touched: a run that cannot happen leaves the previous out/ alone.
# nip.io answers only through DNS: offline, or with DNS filtered, nothing resolves and the crawl
# fetches nothing. Fail here with the reason, not two minutes later with an empty results file.
getent hosts "$FIXTURE_HOST" >/dev/null \
    || { echo "$FIXTURE_HOST does not resolve: nip.io needs DNS; offline, put a name in /etc/hosts and pass it as FIXTURE_HOST"; exit 2; }
if [ -n "${ARCHIVE:-}" ]; then
    [ -z "$(dirty .)" ] || { echo "ARCHIVE=1 but this checkout is dirty (git status --porcelain)"; exit 2; }
    [ -n "$SKIP_TIKA" ] || [ -z "$(dirty "$TIKA_WT")" ] || { echo "ARCHIVE=1 but $TIKA_WT is dirty"; exit 2; }
fi
if [ -z "$SKIP_TIKA" ]; then
    [ -f "$CP_FILE" ] || { echo "missing $CP_FILE: build tika-grpc in $TIKA_WT first (README, step 1)"; exit 2; }
fi

# Everything a run produces goes to out/ (gitignored), fresh every time so the verifier
# never sees lines from a previous run.
rm -rf out && mkdir -p out
sed "s#parsebytes.127.0.0.1.nip.io#$FIXTURE_HOST#" testserver/seeds.txt > out/seeds.txt

if [ -z "$SKIP_TIKA" ]; then
    # 50052 sits inside Linux's ephemeral range (32768-60999): any outbound connection on this
    # host may hold it as its local port, and tika-grpc binds without SO_REUSEADDR. Move if so.
    if ss -tan 2>/dev/null | grep -q ":$PORT "; then
        PORT=$(python3 -c 'import socket; s = socket.socket(); s.bind(("", 0)); print(s.getsockname()[1])')
        echo ">>> :${TIKA_PORT:-50052} is in use on this host, tika-grpc will listen on :$PORT"
    fi
    # The bolt reads its target from the crawler config: hand it this run's port.
    { cat crawler-conf.yaml; echo "  parsebytes.target: \"localhost:$PORT\""; } > out/crawler-conf.yaml
    # JSON has no comments and no variables: the template carries the two machine-specific
    # values as placeholders and we fill them in here.
    sed -e "s#JAVA_PATH#$(readlink -f "$(command -v java)")#" \
        -e "s#PLUGIN_ROOTS#$(cd "$TIKA_WT" && pwd)/tika-grpc/target/plugins#" \
        tika/config.template.json > out/tika-config.json
    echo ">>> starting tika-grpc from $TIKA_WT @ $(git -C "$TIKA_WT" rev-parse --short HEAD)"
    # dependency:build-classpath also lists the plugin zips tika-grpc declares for its tests. A zip
    # on the classpath exposes its META-INF/extensions.idx without the classes (they sit in lib/
    # inside it), and pf4j logs one ClassNotFoundException per entry at startup. Plugins load from
    # plugin-roots; the zips have no business here.
    java -cp "$TIKA_WT/tika-grpc/target/classes:$(tr ':' '\n' < "$CP_FILE" | grep -v '\.zip$' | paste -sd:)" \
        org.apache.tika.pipes.grpc.TikaGrpcServer -c out/tika-config.json -p "$PORT" \
        > out/tika-server.log 2>&1 &
    TIKA_PID=$!
    # The server forks its pipes JVMs at startup; wait until the port is actually open.
    for _ in $(seq 1 90); do (exec 3<>"/dev/tcp/127.0.0.1/$PORT") 2>/dev/null && break; sleep 1; done
    (exec 3<>"/dev/tcp/127.0.0.1/$PORT") 2>/dev/null || { echo "tika-grpc did not open :$PORT"; tail -40 out/tika-server.log; exit 2; }
fi

[ -f out/crawler-conf.yaml ] || cp crawler-conf.yaml out/crawler-conf.yaml

echo ">>> starting URLFrontier ${URLFRONTIER_VERSION}"
docker run -d --name "$FRONTIER" -p 127.0.0.1:7072:7071 crawlercommons/url-frontier:${URLFRONTIER_VERSION} >/dev/null
# A cold container can take a while to open its port; the seed injection gives up after 30 s.
for _ in $(seq 1 60); do (exec 3<>"/dev/tcp/127.0.0.1/7072") 2>/dev/null && break; sleep 1; done
(exec 3<>"/dev/tcp/127.0.0.1/7072") 2>/dev/null || { echo "URLFrontier did not open :7072"; docker logs "$FRONTIER" 2>&1 | tail -20; exit 2; }

echo ">>> starting fixture server"
python3 testserver/server.py out/access.log 8099 "$FIXTURE_BIND" &
SERVER_PID=$!
for _ in $(seq 1 40); do (exec 3<>"/dev/tcp/127.0.0.1/8099") 2>/dev/null && break; sleep 0.5; done
(exec 3<>"/dev/tcp/127.0.0.1/8099") 2>/dev/null || { echo "fixture server did not open :8099"; exit 2; }

echo ">>> running topology for ${RUN_MINUTES} minute(s) on stormcrawler ${SC_VERSION} / storm ${STORM_VERSION}"
# Full output goes to out/topology.log; a failing build or topology stops the run here
# instead of reaching the verifier with a half-empty results file.
MVN_EXIT=0
mvn -q -Dstormcrawler.version="${SC_VERSION}" -Dstorm.version="${STORM_VERSION}" compile exec:java \
    -Dexec.args="${RUN_MINUTES} out/seeds.txt out/crawler-conf.yaml" > out/topology.log 2>&1 || MVN_EXIT=$?
# AsyncLocalizer and ZooKeeper trying ::1 on hosts without IPv6 are LocalCluster noise, not
# the run's; anything else with ERROR in it stays visible.
grep -E ">>>|ERROR" out/topology.log | grep -v -E "AsyncLocalizer|ClientCnxn.*(Unable to open socket|Network is unreachable)" || true
[ "$MVN_EXIT" -eq 0 ] || { echo "topology run failed (exit $MVN_EXIT), see out/topology.log"; exit "$MVN_EXIT"; }

# Everything a reader needs to know which code produced the numbers below.
echo ">>> manifest"
{
    # The probe's own commit, once this folder is a git repository: evidence must name
    # the exact harness that produced it, not only the server.
    git rev-parse HEAD >/dev/null 2>&1 && echo "probe_sha=$(git rev-parse HEAD)$(dirty .)"
    if [ -z "$SKIP_TIKA" ]; then
        echo "tika_server_sha=$(git -C "$TIKA_WT" rev-parse HEAD)$(dirty "$TIKA_WT")"
        # Everything between HEAD and the working tree, index included. Full index lines,
        # fixed prefixes, no color, no external diff: the hash must not depend on the
        # reader's git config.
        echo "tika_server_uncommitted_patch_sha256=$(git -C "$TIKA_WT" diff HEAD --full-index --binary --no-textconv --no-ext-diff --no-color --src-prefix=a/ --dst-prefix=b/ | sha256sum | cut -d' ' -f1)"
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
    sha256sum proto/org/apache/tika/grpc/v2/*.proto tika/config.template.json testserver/server.py testserver/fixture.pdf testserver/docs/*.pdf testserver/seeds.txt | sed 's/^/sha256 /'
    echo "java=$(java -version 2>&1 | head -1)"
} | tee out/manifest.txt

echo ">>> verifying"
# Last command: with pipefail its status is the verifier's, and set -e turns a failure into
# this script's exit status.
python3 verify.py out/results.jsonl out/access.log out/seeds.txt | tee out/verify.txt
