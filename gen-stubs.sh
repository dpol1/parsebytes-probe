#!/usr/bin/env bash
# Regenerates src/main/java/org/apache/tika/grpc/v2 from proto/.
#
# The protos are verbatim copies from the Tika commit named in the README (tika-grpc-api and
# tika-grpc). timestamp.proto is the well-known type, pulled out of the protobuf-java jar
# because the protoc binary from Maven Central ships without its includes.
#
# Versions are pinned to what the topology already has on its classpath through the
# URLFrontier client (protobuf-java 3.25.8, grpc 1.76.0): generated code and runtime have to
# agree, and this way the pom gains nothing. @generated=omit keeps the stubs free of the
# javax.annotation.Generated annotation, which is not on the classpath either.
#
# Both binaries are fetched from Maven Central if they are not in ~/.m2 yet. Linux x86_64;
# change ARCH for another platform.
set -euo pipefail
cd "$(dirname "$0")"
M=~/.m2/repository
PROTOC_V=3.25.8
GRPC_V=1.76.0
ARCH=linux-x86_64
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
