# Archived run

One full run of the probe, kept as-is, from clean checkouts (`ARCHIVE=1`). `manifest.txt`
pins what produced it (probe and tika commits, proto, config and fixture hashes, versions);
`results.jsonl` has one line per document; `access.log` is the fixture server's request log
the checker counts GETs from; `verify.txt` is the checker output, 15/15.

Run notes:

- pages served as text/html with no charset, ascii bodies -> tika replies
  `charset=windows-1252`. a declared-charset hint in the request would have changed
  this; proto fields 7-10 are still unused, this is the first data point for them.
- testPDF_childAttachments.pdf: parsers_used ends with OfficeParser, so the embedded
  office docs were parsed. the v2 Document has no field for them, nothing comes back.
- /big is served as 4 MiB and cut by the crawler at http.content.limit (3 MiB): the bolt
  sends truncated=true, origin.truncated comes back true, origin.byte_size is the cut size.
- with pipes.maxInlineBytes at 1 MiB, /big and testPDF_childAttachments.pdf went through
  the spool fetcher and the rest inline; nothing in the Document tells the lanes apart,
  which is the point.
