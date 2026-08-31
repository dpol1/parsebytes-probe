# ParseBytes probe

ParseBytes probe is a small end-to-end check for [Apache StormCrawler](https://stormcrawler.apache.org/)
talking to [Apache Tika](https://tika.apache.org/) over gRPC. A local crawl fetches
fourteen URLs and a bolt hands the exact fetched bytes to `TikaV2.ParseBytes`, the
parse-only call from the [`TIKA-4795-parseBytes`](https://github.com/ai-pipestream/tika/tree/TIKA-4795-parseBytes)
branch of the [ai-pipestream/tika](https://github.com/ai-pipestream/tika) fork
([TIKA-4795](https://issues.apache.org/jira/browse/TIKA-4795); apache/tika does not ship it
yet). An independent verifier, [`verify.py`](verify.py), re-derives every claim from the
outputs: same sha256 on the crawler's bytes and in Tika's reply, exactly one GET per URL in
the fixture server's log, and Tika's own test PDFs typed the way Tika's test suite expects.
Anything missing, duplicated or failed turns the run red.

The bolt depends only on the classes generated from the two protos, plus grpc and protobuf,
which StormCrawler's URLFrontier client already ships. It calls no Tika runtime APIs.
Point it at any other server that implements the same service and the same checks rerun
unchanged.

Tika already supports pluggable Fetchers for fetch-and-parse inputs such as files, HTTP and
S3. This probe exercises the complementary parse-only path: StormCrawler already owns the
fetched bytes and sends them directly to ParseBytes. It does not install a StormCrawler
fetcher inside Tika.

## Quickstart

You need Java 25 (the pom targets 17, but that is untested), Maven, Docker and python3.

Build tika-grpc at the commit the protos were copied from. That commit does not compile
against the tika-core it merged in, so apply the three-hunk patch first (two `Property`
compile fixes and the digest key derived from `DigestDef`, without which `origin.sha256`
comes back empty). Clone it next to this repository: the run script's default
`TIKA_WT` is the sibling folder `../tika-4795-demo`:

```sh
git clone https://github.com/dpol1/parsebytes-probe.git
git clone https://github.com/ai-pipestream/tika.git tika-4795-demo
cd tika-4795-demo
git checkout 3cfa638409f16f8e89df5d04a31c72892d25cb32
git apply ../parsebytes-probe/tika/mapper-compile-fixes.patch
./mvnw -q clean -pl tika-grpc -am -DskipTests -Dmaven.javadoc.skip=true -Drat.skip=true \
  -Dcheckstyle.skip=true -Dforbiddenapis.skip=true -Dspotless.check.skip=true \
  -Dmdep.includeScope=runtime -Dmdep.outputFile="$PWD/tika-grpc/target/cp.txt" \
  package dependency:build-classpath
```

Nothing is installed to `~/.m2`; the server runs from the reactor jars listed in `cp.txt`.
Then:

```sh
cd ../parsebytes-probe
./run.sh
```

Two minutes of crawl, then the verifier; exit 0 means all twelve checks passed. Everything
a run produces (results, manifest with server, proto and fixture hashes, logs) lands in
`out/`, which git ignores. On exit the script kills what it started, its own URLFrontier
container included. `TIKA_WT=path` points at a Tika checkout elsewhere; `RUN_MINUTES=1`
is enough on a fast machine.

The fixtures: nine generated HTML pages the crawl discovers on its own, plus five PDF URLs
backed by four distinct files.
Three come verbatim from Tika's test corpus, `testPDF.pdf` (whose expected metadata is
asserted by Tika's `PDFParserTest`, not by me), the 2.3 MB `testPDF_childAttachments.pdf`
and the owner-password `testPDF_protected.pdf`; `testPDF.pdf` is also served a second time
at `/octet/blob`, no extension, declared `application/octet-stream`, so neither header nor
name can tell the parser what it is. The last one is a 734-byte PDF written by hand with no
dates inside, so its hash never changes.

## Numbers

From the archived run in [`evidence/released-stack/`](evidence/released-stack/):
StormCrawler 3.7.0, Storm 2.8.9, one laptop, nothing tuned. `elapsed` wraps the blocking
call in the bolt: wire, forked parse and mapping to the typed Document.

| URL | Bytes | Detected | Elapsed |
| --- | ---: | --- | ---: |
| `/docs/testPDF.pdf` | 34,824 | `application/pdf` | 7,375 ms |
| `/docs/testPDF_childAttachments.pdf` | 2,318,262 | `application/pdf` | 4,051 ms |
| `/docs/testPDF_protected.pdf` | 506,064 | `application/pdf` | 483 ms |
| `/octet/blob` | 34,824 | `application/pdf` | 47 ms |
| `/fixture.pdf` | 734 | `application/pdf` | 57 ms |
| `/p1` to `/p9` | 38 to 80 | `text/html; charset=windows-1252` | 19 to 93 ms |

The seven seconds are the first call of the run, when tika-grpc forks its pipes JVM and the
parsers initialize; the same PDF bytes a few seconds later, as `/octet/blob`, take 47 ms.
Two unexpected results from this run are noted in
[`evidence/released-stack/`](evidence/released-stack/).

## Trying another ParseBytes server

```sh
echo '  parsebytes.target: "host:port"' >> crawler-conf.yaml
SKIP_TIKA=1 ./run.sh
```

The request carries the bytes, a correlation id (the URL, echoed back as `Document.id`),
the last path segment as a detection hint, the URL as provenance that must never be
dereferenced, and StormCrawler's truncation flag. Nothing else. If your server needs more
than that to do its job, I want to hear about it.

## What this is not

A direct unary probe, on purpose: no Kafka, no S3, no durable queue, no retry or
dead-letter handling, no external-parser SPI. Every document here was parsed by Tika's
built-in parsers; the probe only shows the bolt would not notice a different server. It
does not assert on extracted text (the v2 Document does not carry it yet), does not
measure throughput (these are single-call latencies behind a politeness delay), and has
run only on Linux x86_64.

## License

[Apache License 2.0](LICENSE). The protos, the generated classes and the three test PDFs
are redistributed from Apache Tika; see [NOTICE](NOTICE). The crawl side is Apache
StormCrawler with crawler-commons URLFrontier, and they do the actual work.
