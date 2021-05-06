package co.thatch.example.metrics;

import co.thatch.example.metrics.model.Metric;
import co.thatch.example.metrics.model.MetricBucket;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.annotation.Async;

import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.LongStream;


@Singleton
public class InMemoryMetricsService implements MetricsService {
    private static final int MAX_WINDOW_SIZE = 1_800;
    private static final int MAX_WINDOW_SIZE_WITH_BUFFER = MAX_WINDOW_SIZE + 1;

    private final DoubleAdder requestCounter = new DoubleAdder();
    private final double[] requestCountRecordings = new double[MAX_WINDOW_SIZE_WITH_BUFFER];
    private final HashMap<Long, MetricHolder> metrics = new HashMap<>();
    private final PriorityQueue<Long> timestamps = new PriorityQueue<>();

    @Async
    @EventListener
    public void onStartup(ServerStartupEvent event) throws InterruptedException {
        while (true) {
            requestCountRecordings[Math.toIntExact(Instant.now().getEpochSecond() % requestCountRecordings.length)] = requestCounter.doubleValue();
            Thread.sleep(50);
        }
    }

    @Override
    public void incrementRequestCount(String key, Instant requestInstant) {
//        CompletableFuture.runAsync(() -> {
            requestCounter.add(1.0);
            Long timestamp = requestInstant.getEpochSecond();
            synchronized (this) {
                MetricHolder metricsForTimestamp = metrics.get(timestamp);
                if (metricsForTimestamp != null) {
                    metricsForTimestamp.incrementRequestCountForKey(key);
                } else {
                    metrics.put(timestamp, new MetricHolder(key));
                    timestamps.add(timestamp);
                    if (timestamps.peek() < timestamp - 30) {
                        metrics.remove(timestamps.poll());
                    }
                }
            }
//        });
    }

    @Override
    public double getQPS(Duration window) {
        double startCount = requestCountRecordings[(Math.toIntExact(Instant.now().minus(window).minusSeconds(1).getEpochSecond()) % requestCountRecordings.length)];
        return (requestCounter.doubleValue() - startCount) / window.getSeconds();
    }

    @Override
    public Metric getHistogram(String key, Instant end, Duration window, Duration interval) {
        Instant start = end.minus(window);
        double totalRequestCount = 0.0;
        List<MetricBucket> buckets = new ArrayList<>();
        for (long rangeStart = start.getEpochSecond(); rangeStart <= end.getEpochSecond(); rangeStart += interval.getSeconds()) {
            long rangeEnd = rangeStart + interval.getSeconds();
            double requestCount = LongStream.range(rangeStart, rangeEnd).mapToDouble(ts -> {
                MetricHolder metric = metrics.get(ts);
                if (metric != null) {
                    return metric.getRequestCountForKey(key);
                } else {
                    return 0.0;
                }
            }).sum();
            totalRequestCount += requestCount;
            buckets.add(new MetricBucket(String.valueOf(rangeStart), requestCount / interval.getSeconds()));
        }
        double requestsPerSecond = totalRequestCount / window.getSeconds();
        return new Metric(requestsPerSecond, buckets);
    }

    @Override
    public void reset() {
        metrics.clear();
        timestamps.clear();
        requestCounter.reset();
        Arrays.fill(requestCountRecordings, 0.0);
    }

    private static class MetricHolder {
        private final HashMap<String, Double> measurements = new HashMap<>();

        public MetricHolder(String key) {
            this.incrementRequestCountForKey(key);
        }

        public void incrementRequestCountForKey(String key) {
            Double existingCount = measurements.get(key);
            if (existingCount == null) {
                measurements.put(key, 1.0);
            } else {
                measurements.put(key, existingCount + 1);
            }
        }

        private Double getRequestCountForKey(String key) {
            if (key != null) {
                return measurements.getOrDefault(key, 0.0);
            } else {
                return measurements.values().stream().reduce(0.0, Double::sum);
            }
        }
    }
}
