package e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.apache.stormcrawler.Metadata;
import org.apache.tika.grpc.v2.Document;
import org.apache.tika.grpc.v2.ParseBytesReply;
import org.apache.tika.grpc.v2.ParseBytesRequest;
import org.apache.tika.grpc.v2.TikaV2Grpc;

/**
 * Sends the content of each tuple FetcherBolt emitted (url, content, metadata) to
 * TikaV2.ParseBytes and writes one JSON line per tuple with what came back. Acks every tuple,
 * records failures in the line, emits nothing downstream. Depends only on the generated
 * org.apache.tika.grpc.v2 stubs.
 */
public class ParseBytesBolt extends BaseRichBolt {

    private static final ObjectMapper JSON = new ObjectMapper();

    // Storm serializes the bolt; the non-serializable parts are created in prepare().
    private transient OutputCollector collector;
    private transient ManagedChannel channel;
    private transient TikaV2Grpc.TikaV2BlockingStub stub;
    private transient PrintWriter out;

    @Override
    public void prepare(Map<String, Object> conf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        String target = (String) conf.getOrDefault("parsebytes.target", "localhost:50052");
        String results = (String) conf.getOrDefault("parsebytes.results", "out/results.jsonl");
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = TikaV2Grpc.newBlockingStub(channel);
        try {
            // Append, flush per line.
            out = new PrintWriter(new FileWriter(results, true), true);
        } catch (IOException e) {
            throw new IllegalStateException("cannot open " + results, e);
        }
    }

    @Override
    public void execute(Tuple tuple) {
        String url = tuple.getStringByField("url");
        byte[] content = tuple.getBinaryByField("content");
        Metadata metadata = (Metadata) tuple.getValueByField("metadata");
        ObjectNode line = JSON.createObjectNode().put("url", url).put("bytes", content.length);
        long start = System.nanoTime();
        try {
            ParseBytesRequest request =
                    ParseBytesRequest.newBuilder()
                            .setCorrelationId("crawl:" + url)
                            .setContent(ByteString.copyFrom(content))
                            .setResourceName(resourceName(url))
                            .setSourceUri(url)
                            // set by StormCrawler when the body was cut at http.content.limit
                            .setTruncated("true".equals(metadata.getFirstValue("protocol.http.trimmed")))
                            .build();
            ParseBytesReply reply =
                    stub.withDeadlineAfter(30, TimeUnit.SECONDS).parseBytes(request);
            Document doc = reply.getDocument();
            line.put("correlation_id", request.getCorrelationId())
                    .put("doc_id", doc.getId())
                    .put("reply_correlation_id", reply.getCorrelationId())
                    .put("client_sha256", sha256(content))
                    .put("origin_sha256", doc.getOrigin().getSha256())
                    .put("origin_byte_size", doc.getOrigin().getByteSize())
                    .put("origin_source_uri", doc.getOrigin().getSourceUri())
                    .put("content_type", doc.getContentType())
                    .put("title", doc.getMetadata().getTitle())
                    .put("authors", String.join(";", doc.getMetadata().getAuthorsList()))
                    .put("created", doc.getMetadata().hasCreated()
                            ? Instant.ofEpochSecond(doc.getMetadata().getCreated().getSeconds()).toString()
                            : "")
                    .put("parsers_used", String.join(",", doc.getStatus().getParsersUsedList()))
                    .put("errors", String.join(" | ", doc.getStatus().getErrorsList()))
                    .put("pipes_status", doc.getStatus().getPipesStatus())
                    .put("tika_version", doc.getStatus().getTikaVersion())
                    .put("extra_fields", doc.getExtraCount())
                    .put("document_bytes", doc.getSerializedSize())
                    .put("truncated_sent", request.getTruncated())
                    .put("truncated", doc.getOrigin().getTruncated());
        } catch (StatusRuntimeException e) {
            line.put("error", e.getStatus().toString());
        } catch (Exception e) {
            line.put("error", e.toString());
        }
        line.put("elapsed_ms", (System.nanoTime() - start) / 1_000_000);
        out.println(line);
        System.out.println(">>> parsebytes " + line);
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // nothing goes downstream
    }

    @Override
    public void cleanup() {
        out.close();
        channel.shutdownNow();
    }

    private static String resourceName(String url) {
        String path = URI.create(url).getPath();
        int slash = path.lastIndexOf('/');
        return slash < 0 ? path : path.substring(slash + 1);
    }

    private static String sha256(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }
}
