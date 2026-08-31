#!/usr/bin/env python3
"""The checks. Reads the lines ParseBytesBolt wrote and the fixture server's access log, prints
one PASS/FAIL line per check, exits 1 if any failed.

    python3 verify.py [out/results.jsonl] [out/access.log]

The verifier knows the exact set of URLs a run must produce: the seeds plus the pages the
seeds link to. A missing, duplicated or unexpected URL fails the run, and so does any result
that is not a clean PARSE_SUCCESS. Each check below exists to catch one specific way the
claim "the crawler's bytes went through ParseBytes and came back as a typed Document, with
no second acquisition" could be false; the comments say which.
"""

import hashlib
import json
import os
import sys
from collections import Counter
from urllib.parse import urlparse

RESULTS = sys.argv[1] if len(sys.argv) > 1 else "out/results.jsonl"
LOG = sys.argv[2] if len(sys.argv) > 2 else "out/access.log"
SEEDS = "testserver/seeds.txt"

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
    pass  # "exact url set" below reports it

# What a run must produce: every seed, plus /p1../p9, which the generated pages link to
# (page N links to N+1 and N+2, up to 9) and the crawl discovers on its own.
with open(SEEDS) as f:
    seeds = [line.strip() for line in f if line.strip()]
expected = {path_of(u) for u in seeds} | {f"/p{i}" for i in range(1, 10)}

got = Counter(path_of(r["url"]) for r in results)
missing = sorted(expected - set(got))
unexpected = sorted(set(got) - expected)
duplicated = sorted(p for p, n in got.items() if n > 1)

# The oracle: the exact set, once each. An empty or partial results file, a bolt that ran
# twice, or a URL nobody asked for all fail here.
check(
    "exact url set",
    not missing and not unexpected and not duplicated and len(results) == len(expected),
    f"{len(results)} results for {len(expected)} expected URLs"
    + (f", missing {missing}" if missing else "")
    + (f", unexpected {unexpected}" if unexpected else "")
    + (f", duplicated {duplicated}" if duplicated else ""),
)

errors = [r for r in results if r.get("error")]
docs = [r for r in results if not r.get("error")]

# Every gRPC status the bolt recorded instead of a Document.
check("no rpc errors", not errors, "; ".join(f"{r['url']}: {r['error']}" for r in errors) or "none")

# A Document that came back is not enough: the pipes status must be a clean success and the
# parser error list empty, for every one of them.
not_clean = [(path_of(r["url"]), r.get("pipes_status"), r.get("errors")) for r in docs
             if r.get("pipes_status") != "PARSE_SUCCESS" or r.get("errors")]
check("all parse success", bool(docs) and not not_clean, not_clean or f"{len(docs)}/{len(docs)} PARSE_SUCCESS, no parser errors")

# The server echoes correlation_id as Document.id. If this fails, either the reply is not for
# our request or the server minted its own id, which breaks every caller keeping a map.
check(
    "id echo",
    bool(docs) and all(r["doc_id"] == r["correlation_id"] for r in docs),
    [(path_of(r["url"]), r["doc_id"]) for r in docs if r["doc_id"] != r["correlation_id"]] or f"{len(docs)}/{len(docs)}",
)

# Client and server hashed the same bytes independently. Non-empty matters: with the digester
# off both sides would agree on "" and the check would pass for nothing.
check(
    "digest parity",
    bool(docs) and all(r["origin_sha256"] and r["origin_sha256"] == r["client_sha256"] for r in docs),
    [(path_of(r["url"]), r["client_sha256"][:12], r["origin_sha256"][:12]) for r in docs
     if r["origin_sha256"] != r["client_sha256"]] or f"{len(docs)}/{len(docs)} sha256 equal, non-empty",
)

# The access log is the one witness that does not belong to the crawler. One GET per URL
# means the fetcher fetched once and nothing behind ParseBytes went back for the bytes,
# although it had the source_uri to do so.
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
    "single acquisition",
    bool(docs) and all(n == 1 for n in counts.values()),
    {p: n for p, n in counts.items() if n != 1} or f"1 GET each for {len(counts)} URLs",
)


# For the files we serve from disk the loop closes entirely: disk, crawler, Tika. The HTML
# pages are generated on the fly, so they only get the client/Tika comparison above.
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
check("bytes on disk == crawler bytes", bool(served) and not mismatch,
      mismatch or f"{len(served)} served files identical end to end")

by_path = {path_of(r["url"]): r for r in docs}

# Typed metadata against values asserted by Tika's own PDFParserTest, not by me.
real = by_path.get("/docs/testPDF.pdf")
check(
    "real pdf typed metadata",
    bool(real) and real["content_type"].startswith("application/pdf") and real["title"] == "Apache Tika - Apache Tika"
    and "Bertrand Delacrétaz" in real.get("authors", "") and bool(real.get("created")),
    {k: real.get(k) for k in ("content_type", "title", "authors", "created")} if real else "no result",
)

# Same bytes, no extension, declared as octet-stream: the request carries resource_name
# "blob", so neither the header nor the name can tell Tika it is a PDF. Only the bytes can.
octet = by_path.get("/octet/blob")
check(
    "detection from bytes alone",
    bool(real) and bool(octet) and octet["content_type"].startswith("application/pdf") and octet["origin_sha256"] == real["origin_sha256"],
    (octet or {}).get("content_type", "no result"),
)

# A 2.3 MB container with attachments through the unary call. Tika parses the attachments
# (an OfficeParser shows up in parsers_used) but v2 has no slot for embedded documents.
child = by_path.get("/docs/testPDF_childAttachments.pdf")
check("embedded container parsed", bool(child) and child["content_type"].startswith("application/pdf"),
      {k: child.get(k) for k in ("content_type", "extra_fields", "parsers_used")} if child else "no result")

# Owner password only, so it is readable. A user-password file would be the interesting
# negative case; this corpus has none.
prot = by_path.get("/docs/testPDF_protected.pdf")
check("owner-password pdf still parsed", bool(prot) and prot["content_type"].startswith("application/pdf") and bool(prot["title"]),
      {k: prot.get(k) for k in ("pipes_status", "title")} if prot else "no result")

fixture = by_path.get("/fixture.pdf")
check(
    "hand-written pdf typed metadata",
    bool(fixture) and fixture["content_type"].startswith("application/pdf") and fixture["title"] == "ParseBytes fixture",
    {k: fixture.get(k) for k in ("content_type", "title")} if fixture else "no result",
)

print()
if failures:
    print("FAILED:", ", ".join(failures))
    sys.exit(1)
print("ALL CHECKS PASSED")
