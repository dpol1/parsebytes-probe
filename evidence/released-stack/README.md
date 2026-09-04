# Archived run

One complete run, kept as produced: `manifest.txt` names the commits, versions and file hashes
behind it; `results.jsonl` has one line per document; `access.log` is the web server's request
log; `verify.txt` is the checker output.

Two things this run showed:

- Pages served as `text/html` without a charset, with ASCII bodies, come back as
  `text/html; charset=windows-1252`; the request has no field for a declared charset yet.
- `testPDF_childAttachments.pdf`: `parsers_used` ends with `OfficeParser`, so the embedded
  Office documents were parsed; the typed Document has no field for them yet.
