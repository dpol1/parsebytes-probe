# Tika demo

StormCrawler crawls a few pages and PDFs from a local web server and hands each fetched
document to Apache Tika over gRPC with Tika's new `ParseBytes` call: the crawler already has
the bytes, so Tika parses exactly those bytes instead of fetching the URL itself. Tika returns
a typed `Document` (media type, title, authors, dates, and the remaining metadata as typed
fields). A checker then compares what the crawler sent with what Tika reported and counts the
requests the web server saw. The crawler side depends only on the classes generated from
Tika's proto files, not on Tika itself.

`ParseBytes` and the typed `Document` are new in Tika and not released yet. They live on the
[`TIKA-4795-parseBytes`](https://github.com/ai-pipestream/tika/tree/TIKA-4795-parseBytes)
branch of the ai-pipestream fork ([TIKA-4795](https://issues.apache.org/jira/browse/TIKA-4795),
[TIKA-4766](https://issues.apache.org/jira/browse/TIKA-4766)).

## What the checker verifies

- Every URL the crawl started from, and every page it discovered, produced exactly one result.
- For all 15 documents, Tika reported the same SHA-256 the crawler computed on the bytes it sent.
- The web server saw one GET per URL: Tika never fetched anything itself.
- The byte count, the source URL and the truncation flag sent with each document came back in
  the reply unchanged; the one page the crawler cut at its size limit is the only one flagged.
- Tika's own test PDFs came back with the title and author Tika's test suite expects, plus a creation date; a
  PDF served without a file extension and with a generic content type was still detected as a
  PDF; a PDF with attachments and a password-protected PDF parsed without errors.

What is checked, exactly: [`verify.py`](verify.py), one PASS or FAIL line per check. The
complete output of one run, with the commits, versions and file hashes that produced it, is in
[`evidence/released-stack/`](evidence/released-stack/).

## Run it

Linux or macOS (tested on Linux x86_64; on Windows use WSL2), Java 25, Maven, Docker, python3
and DNS: the web server is reached through a nip.io name. Offline, add a name for 127.0.0.1 to
`/etc/hosts` and pass it as `FIXTURE_HOST`.

Build tika-grpc at commit `a060ac5d35483b9f5f684b6c356332474494e2f8`, in a folder next to
this repository:

```sh
git clone https://github.com/dpol1/tika-demo.git
git clone https://github.com/ai-pipestream/tika.git tika-4795-demo
cd tika-4795-demo
git checkout a060ac5d35483b9f5f684b6c356332474494e2f8
./mvnw -q clean -pl tika-grpc -am -DskipTests -Dmaven.javadoc.skip=true -Drat.skip=true \
  -Dcheckstyle.skip=true -Dforbiddenapis.skip=true -Dspotless.check.skip=true \
  -Dmdep.includeScope=runtime -Dmdep.outputFile="$PWD/tika-grpc/target/cp.txt" \
  package dependency:build-classpath
```

Then run the demo:

```sh
cd ../tika-demo
./run.sh
```

The script starts tika-grpc from that build, URLFrontier in Docker and the local web server,
crawls for two minutes, then runs the checker. Exit code 0 means every check passed.
Everything a run writes goes to `out/`. Environment variables: `TIKA_WT` (the Tika checkout,
default `../tika-4795-demo`), `RUN_MINUTES` (default 2), `TIKA_PORT` (default 50052, moved
automatically if the port is taken), `ARCHIVE=1` (refuse to run from a checkout with
uncommitted changes), `SKIP_TIKA=1` (use a server you started yourself, see below).

A run takes about two minutes. The first call includes tika-grpc starting its parser process;
after that each document takes milliseconds to a few seconds.

## Using another ParseBytes server

```sh
echo '  parsebytes.target: "host:port"' >> crawler-conf.yaml
SKIP_TIKA=1 ./run.sh
```

Each request carries the bytes, a correlation id (`crawl:` followed by the URL, expected back
as `Document.id` and as the reply's `correlation_id`), the file name taken from the URL, the
URL itself as the source, and whether the crawler cut the document at its size limit.

If that server runs on another machine, start the web server on an address it can reach,
`FIXTURE_HOST=myhost.lan FIXTURE_BIND=0.0.0.0`; otherwise a request coming from that machine
would not appear in the access log the "one GET per URL" check reads.

## License

[Apache License 2.0](LICENSE). The protos, the generated classes and the three test PDFs come
from Apache Tika; see [NOTICE](NOTICE).
