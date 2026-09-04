#!/usr/bin/env bash
# Regenerates src/main/java/org/apache/tika/grpc/v2 from the protos in proto/.
#
# protoc and the grpc-java plugin match the protobuf-java and grpc versions on the topology's
# classpath; both are fetched from Maven Central if missing. @generated=omit leaves out the
# javax.annotation.Generated annotation.
set -euo pipefail
cd "$(dirname "$0")"
M=~/.m2/repository
PROTOC_V=3.25.8
GRPC_V=1.76.0
case "$(uname -s)-$(uname -m)" in
    Linux-x86_64) ARCH=linux-x86_64 ;;
    Linux-aarch64) ARCH=linux-aarch_64 ;;
    Darwin-arm64) ARCH=osx-aarch_64 ;;
    Darwin-x86_64) ARCH=osx-x86_64 ;;
    *) echo "no protoc build for $(uname -s) $(uname -m)"; exit 2 ;;
esac
PROTOC="$M/com/google/protobuf/protoc/$PROTOC_V/protoc-$PROTOC_V-$ARCH.exe"
GRPC="$M/io/grpc/protoc-gen-grpc-java/$GRPC_V/protoc-gen-grpc-java-$GRPC_V-$ARCH.exe"
[ -f "$PROTOC" ] || mvn -q dependency:get -Dartifact="com.google.protobuf:protoc:$PROTOC_V:exe:$ARCH"
[ -f "$GRPC" ] || mvn -q dependency:get -Dartifact="io.grpc:protoc-gen-grpc-java:$GRPC_V:exe:$ARCH"
chmod +x "$PROTOC" "$GRPC"
rm -rf src/main/java/org/apache/tika
"$PROTOC" -Iproto --java_out=src/main/java \
    --plugin=protoc-gen-grpc-java="$GRPC" --grpc-java_out="@generated=omit:src/main/java" \
    org/apache/tika/grpc/v2/document.proto org/apache/tika/grpc/v2/tika_v2.proto
echo "stubs regenerated: $(find src/main/java/org/apache/tika -name '*.java' | wc -l) files"
