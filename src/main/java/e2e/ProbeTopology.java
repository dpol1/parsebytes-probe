package e2e;

import crawlercommons.urlfrontier.URLFrontierGrpc;
import crawlercommons.urlfrontier.Urlfrontier.AckMessage;
import crawlercommons.urlfrontier.Urlfrontier.DiscoveredURLItem;
import crawlercommons.urlfrontier.Urlfrontier.URLInfo;
import crawlercommons.urlfrontier.Urlfrontier.URLItem;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.stormcrawler.bolt.FetcherBolt;
import org.apache.stormcrawler.bolt.JSoupParserBolt;
import org.apache.stormcrawler.bolt.URLPartitionerBolt;
import org.apache.stormcrawler.indexing.StdOutIndexer;
import org.apache.stormcrawler.urlfrontier.Spout;
import org.apache.stormcrawler.urlfrontier.StatusUpdaterBolt;
import org.yaml.snakeyaml.Yaml;

/**
 * The smallest topology that makes the probe meaningful. URLFrontier spout, URL partitioner,
 * FetcherBolt, then the fetched tuple goes to two bolts at once: JSoupParserBolt, StormCrawler's
 * usual HTML parser, so links get discovered and this behaves like a crawl; and ParseBytesBolt,
 * the probe. The status updater sends discovered links back to the frontier. Runs in a
 * LocalCluster for a fixed number of minutes, then exits.
 *
 * <p>Usage: {@code ProbeTopology [minutes] [seeds file]}
 */
public class ProbeTopology {

    public static void main(String[] args) throws Exception {
        int runMinutes = args.length > 0 ? Integer.parseInt(args[0]) : 2;
        Path seeds = Path.of(args.length > 1 ? args[1] : "testserver/seeds.txt");

        Config conf = new Config();
        // StormCrawler's defaults first (from the stormcrawler-core jar), then our overrides.
        conf.putAll(loadConfigSection("/crawler-default.yaml", true));
        conf.putAll(loadConfigSection("crawler-conf.yaml", false));

        injectSeeds(Files.readAllLines(seeds));

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("spout", new Spout(), 1);
        builder.setBolt("partitioner", new URLPartitionerBolt(), 1).shuffleGrouping("spout");
        builder.setBolt("fetcher", new FetcherBolt(), 1)
                .fieldsGrouping("partitioner", new Fields("key"));
        // Two subscribers on the fetcher's default stream: Storm hands the tuple to both.
        builder.setBolt("parse", new JSoupParserBolt(), 1).localOrShuffleGrouping("fetcher");
        builder.setBolt("parsebytes", new ParseBytesBolt(), 1).localOrShuffleGrouping("fetcher");
        builder.setBolt("index", new StdOutIndexer(), 1).localOrShuffleGrouping("parse");
        builder.setBolt("status", new StatusUpdaterBolt(), 1)
                .fieldsGrouping("fetcher", "status", new Fields("url"))
                .fieldsGrouping("parse", "status", new Fields("url"))
                .fieldsGrouping("index", "status", new Fields("url"));

        try (LocalCluster cluster = new LocalCluster()) {
            cluster.submitTopology("parsebytes-probe", conf, builder.createTopology());
            System.out.println(">>> topology running for " + runMinutes + " minutes");
            Thread.sleep(runMinutes * 60_000L);
            System.out.println(">>> run window elapsed, shutting down");
        }
        // LocalCluster leaves non-daemon threads behind; exit explicitly.
        System.exit(0);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadConfigSection(String path, boolean classpath)
            throws Exception {
        try (InputStream is =
                classpath
                        ? ProbeTopology.class.getResourceAsStream(path)
                        : new FileInputStream(path)) {
            Map<String, Object> raw = new Yaml().load(is);
            Object section = raw.get("config");
            return section instanceof Map ? (Map<String, Object>) section : raw;
        }
    }

    /** Puts the seed URLs into URLFrontier, the way an injector topology would. */
    private static void injectSeeds(List<String> seedUrls) throws Exception {
        ManagedChannel channel =
                ManagedChannelBuilder.forTarget("localhost:7072").usePlaintext().build();
        try {
            URLFrontierGrpc.URLFrontierStub stub = URLFrontierGrpc.newStub(channel);
            CountDownLatch done = new CountDownLatch(1);
            StreamObserver<URLItem> sender =
                    stub.putURLs(
                            new StreamObserver<AckMessage>() {
                                @Override
                                public void onNext(AckMessage value) {}

                                @Override
                                public void onError(Throwable t) {
                                    t.printStackTrace();
                                    done.countDown();
                                }

                                @Override
                                public void onCompleted() {
                                    done.countDown();
                                }
                            });
            int sent = 0;
            for (String url : seedUrls) {
                if (url.isBlank()) {
                    continue;
                }
                String host = URI.create(url).getHost();
                URLInfo info = URLInfo.newBuilder().setUrl(url).setKey(host).build();
                sender.onNext(
                        URLItem.newBuilder()
                                .setDiscovered(DiscoveredURLItem.newBuilder().setInfo(info).build())
                                .build());
                sent++;
            }
            sender.onCompleted();
            if (!done.await(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("seed injection timed out");
            }
            System.out.println(">>> seeds injected: " + sent + " URLs");
        } finally {
            channel.shutdownNow();
        }
    }

    private ProbeTopology() {}
}
