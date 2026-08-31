#!/usr/bin/env python3
"""Fixture HTTP server for the probe. Deliberately dumb: a few generated HTML pages, a handful
of PDFs, and one line per request appended to an access log:

    epoch host path status

That log is the only witness of what was fetched that does not belong to the crawler. It is
how verify.py knows each URL was requested exactly once, and therefore that nothing on the
Tika side went back to the network.

Routes (any host name; they all resolve to this process):
    /robots.txt      allow everything
    /p<N>            a small HTML page linking to /p<N+1> and /p<N+2>, up to /p9, so the crawl
                     finds work on its own
    /fixture.pdf     the hand-written 734-byte PDF, as application/pdf
    /docs/<file>     a file from testserver/docs/ with its real content type
    /octet/blob      the bytes of docs/testPDF.pdf with no extension and declared as
                     application/octet-stream: neither header nor name says PDF

Usage: server.py [access.log] [port]
"""

import os
import sys
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

LOG_PATH = sys.argv[1] if len(sys.argv) > 1 else "access.log"
PORT = int(sys.argv[2]) if len(sys.argv) > 2 else 8080
HERE = os.path.dirname(os.path.abspath(__file__))
DOCS_DIR = os.path.join(HERE, "docs")
with open(os.path.join(HERE, "fixture.pdf"), "rb") as fh:
    FIXTURE_PDF = fh.read()
logfile = open(LOG_PATH, "a", buffering=1)


def page_html(path):
    n = int(path.rsplit("p", 1)[-1]) if path[-1].isdigit() else 1
    links = "".join(f'<a href="/p{i}">p{i}</a> ' for i in (n + 1, n + 2) if i <= 9)
    return f"<html><body><h1>{path}</h1>{links}</body></html>".encode()


def doc_bytes(path):
    with open(os.path.join(DOCS_DIR, os.path.basename(path)), "rb") as fh:
        return fh.read()


class Handler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"

    def log_message(self, *args):
        pass  # the access log below is the one we want, in a format verify.py can parse

    def do_GET(self):
        host = (self.headers.get("Host") or "").split(":")[0]
        status, body, ctype = 200, b"", "text/html"
        try:
            if self.path == "/robots.txt":
                body, ctype = b"User-agent: *\nDisallow:\n", "text/plain"
            elif self.path == "/fixture.pdf":
                body, ctype = FIXTURE_PDF, "application/pdf"
            elif self.path.startswith("/docs/"):
                body, ctype = doc_bytes(self.path), "application/pdf"
            elif self.path == "/octet/blob":
                body, ctype = doc_bytes("testPDF.pdf"), "application/octet-stream"
            else:
                body = page_html(self.path)
        except FileNotFoundError:
            status, body, ctype = 404, b"not found", "text/plain"

        logfile.write(f"{time.time():.3f} {host} {self.path} {status}\n")
        self.send_response(status)
        self.send_header("Content-Type", ctype)
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)


if __name__ == "__main__":
    print(f"fixture server on :{PORT}, logging to {LOG_PATH}", flush=True)
    ThreadingHTTPServer(("127.0.0.1", PORT), Handler).serve_forever()
