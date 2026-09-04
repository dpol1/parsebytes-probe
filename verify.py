#!/usr/bin/env python3
"""Reads the lines ParseBytesBolt wrote and the web server's access log, prints one PASS or FAIL
line per check, exits 1 if any check failed.

    python3 verify.py [out/results.jsonl] [out/access.log] [seeds file]
"""

import hashlib
import json
import os
import re
import sys
from collections import Counter
from urllib.parse import urlparse

RESULTS = sys.argv[1] if len(sys.argv) > 1 else "out/results.jsonl"
LOG = sys.argv[2] if len(sys.argv) > 2 else "out/access.log"
SEEDS = sys.argv[3] if len(sys.argv) > 3 else "testserver/seeds.txt"
with open("crawler-conf.yaml") as f:
    limit = re.search(r"^\s*http\.content\.limit:\s*(\d+)", f.read(), re.M)
if not limit:
    sys.exit("crawler-conf.yaml: http.content.limit is missing, and the truncation check needs it")
CONTENT_LIMIT = int(limit.group(1))

failures = []


def check(name, ok, detail):
    print(f"{'PASS' if ok else 'FAIL'}  {name}: {detail}")
    if not ok:
        failures.append(name)


def path_of(url):
    return urlparse(url).path


results = []
try:
    with open(RESULTS) as f:
        results = [json.loads(line) for line in f if line.strip()]
except FileNotFoundError:
    pass  # reported by "every URL once"

# Every seed plus /p1../p9, which the generated pages link to.
with open(SEEDS) as f:
    seeds = [line.strip() for line in f if line.strip()]
expected = {path_of(u) for u in seeds} | {f"/p{i}" for i in range(1, 10)}

got = Counter(path_of(r["url"]) for r in results)
missing = sorted(expected - set(got))
unexpected = sorted(set(got) - expected)
duplicated = sorted(p for p, n in got.items() if n > 1)

check(
    "every URL once",
    not missing and not unexpected and not duplicated and len(results) == len(expected),
    f"{len(results)} results for {len(expected)} expected URLs"
    + (f", missing {missing}" if missing else "")
    + (f", unexpected {unexpected}" if unexpected else "")
    + (f", duplicated {duplicated}" if duplicated else ""),
)

errors = [r for r in results if r.get("error")]
docs = [r for r in results if not r.get("error")]

check("no rpc errors", not errors, "; ".join(f"{r['url']}: {r['error']}" for r in errors) or "none")

not_clean = [(path_of(r["url"]), r.get("pipes_status"), r.get("errors")) for r in docs
             if r.get("pipes_status") != "PARSE_SUCCESS" or r.get("errors")]
check("all parse success", bool(docs) and not not_clean, not_clean or f"{len(docs)}/{len(docs)} PARSE_SUCCESS, no parser errors")

# The bolt sends "crawl:" + URL as the correlation id; it must come back as Document.id and as
# the reply's correlation_id, and it is never the bare URL.
check(
    "correlation id echoed",
    bool(docs) and all(r["doc_id"] == r["correlation_id"] == r.get("reply_correlation_id") != r["url"]
                       for r in docs),
    [(path_of(r["url"]), r["doc_id"], r.get("reply_correlation_id")) for r in docs
     if not (r["doc_id"] == r["correlation_id"] == r.get("reply_correlation_id") != r["url"])]
    or f"{len(docs)}/{len(docs)}, none equal to the URL",
)

check(
    "source url echoed",
    bool(docs) and all(r.get("origin_source_uri") == r["url"] for r in docs),
    [(path_of(r["url"]), r.get("origin_source_uri")) for r in docs if r.get("origin_source_uri") != r["url"]]
    or f"{len(docs)}/{len(docs)} origin.source_uri == url",
)

check(
    "byte size echoed",
    bool(docs) and all(r.get("origin_byte_size") == r["bytes"] for r in docs),
    [(path_of(r["url"]), r["bytes"], r.get("origin_byte_size")) for r in docs
     if r.get("origin_byte_size") != r["bytes"]] or f"{len(docs)}/{len(docs)} origin.byte_size == bytes sent",
)

# Both sides hashed the bytes independently; an empty hash on the server side does not count.
check(
    "same sha256",
    bool(docs) and all(r["origin_sha256"] and r["origin_sha256"] == r["client_sha256"] for r in docs),
    [(path_of(r["url"]), r["client_sha256"][:12], r["origin_sha256"][:12]) for r in docs
     if r["origin_sha256"] != r["client_sha256"]] or f"{len(docs)}/{len(docs)} sha256 equal, non-empty",
)

gets = Counter()
try:
    with open(LOG) as f:
        for line in f:
            parts = line.split()
            if len(parts) != 4 or parts[2] == "/robots.txt":
                continue
            gets[(parts[1], parts[2])] += 1
except FileNotFoundError:
    pass


def key(r):
    u = urlparse(r["url"])
    return (u.hostname, u.path)  # the log records the Host header without the port


counts = {path_of(r["url"]): gets[key(r)] for r in docs}
check(
    "one GET per URL",
    bool(docs) and all(n == 1 for n in counts.values()),
    {p: n for p, n in counts.items() if n != 1} or f"1 GET each for {len(counts)} URLs",
)


# Files served from disk; the HTML pages are generated and have no file to compare with.
def on_disk(path):
    if path == "/fixture.pdf":
        return os.path.join("testserver", "fixture.pdf")
    if path == "/octet/blob":
        return os.path.join("testserver", "docs", "testPDF.pdf")
    if path.startswith("/docs/"):
        return os.path.join("testserver", "docs", os.path.basename(path))
    return None


def sha_file(p):
    with open(p, "rb") as fh:
        return hashlib.sha256(fh.read()).hexdigest()


served = [r for r in results if on_disk(path_of(r["url"]))]
mismatch = [path_of(r["url"]) for r in served if r.get("client_sha256") != sha_file(on_disk(path_of(r["url"])))]
check("served files unchanged", bool(served) and not mismatch,
      mismatch or f"{len(served)} served files identical end to end")

by_path = {path_of(r["url"]): r for r in docs}

# The values Tika's own PDFParserTest asserts for this file.
real = by_path.get("/docs/testPDF.pdf")
check(
    "test pdf metadata",
    bool(real) and real["content_type"].startswith("application/pdf") and real["title"] == "Apache Tika - Apache Tika"
    and "Bertrand Delacrétaz" in real.get("authors", "") and bool(real.get("created")),
    {k: real.get(k) for k in ("content_type", "title", "authors", "created")} if real else "no result",
)

# Same bytes as testPDF.pdf, served as "blob" with content type application/octet-stream.
octet = by_path.get("/octet/blob")
check(
    "detected without name or type",
    bool(real) and bool(octet) and octet["content_type"].startswith("application/pdf") and octet["origin_sha256"] == real["origin_sha256"],
    (octet or {}).get("content_type", "no result"),
)

child = by_path.get("/docs/testPDF_childAttachments.pdf")
check("pdf with attachments parsed", bool(child) and child["content_type"].startswith("application/pdf"),
      {k: child.get(k) for k in ("content_type", "extra_fields", "parsers_used")} if child else "no result")

# Owner password only, so the file is readable.
prot = by_path.get("/docs/testPDF_protected.pdf")
check("protected pdf parsed", bool(prot) and prot["content_type"].startswith("application/pdf") and bool(prot["title"]),
      {k: prot.get(k) for k in ("pipes_status", "title")} if prot else "no result")

# /big is the one page the crawler cut at http.content.limit; the flag must be true on both
# sides for it and false on both sides for every other document.
big = by_path.get("/big")
others_flagged = [path_of(r["url"]) for r in docs if r is not big and (r.get("truncated") or r.get("truncated_sent"))]
check(
    "truncation flag",
    bool(big) and big.get("truncated_sent") and big.get("truncated") and big["bytes"] == CONTENT_LIMIT
    and not others_flagged,
    ({k: big.get(k) for k in ("bytes", "truncated_sent", "truncated")} if big else "no result")
    if not others_flagged else f"flagged without a cut: {others_flagged}",
)

fixture = by_path.get("/fixture.pdf")
check(
    "small pdf metadata",
    bool(fixture) and fixture["content_type"].startswith("application/pdf") and fixture["title"] == "ParseBytes fixture",
    {k: fixture.get(k) for k in ("content_type", "title")} if fixture else "no result",
)

print()
if failures:
    print("FAILED:", ", ".join(failures))
    sys.exit(1)
print("ALL CHECKS PASSED")
