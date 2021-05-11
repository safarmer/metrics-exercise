package co.thatch.example.metrics;

import co.thatch.example.metrics.model.Metric;
import co.thatch.example.metrics.model.MetricBucket;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.LongStream;

/**
 * An implementation for an earlier version of the requirements, where there was a single metrics endpoint that
 * allowed breakdown by endpoint.
 * <p>
 * Leverages a `metrics` map, tracking the requests based on their epoc second. and a timestamps queue to
 * track what epoc second timestamps to evict based on retention limit (30min)
 */
//@Singleton
public class PerEndpointInMemoryMetricsService implements MetricsService {
    private static final int RETENTION_SIZE_IN_SECONDS = 1_800; // 30 min
    private final HashMap<Long, MetricHolder> metrics = new HashMap<>();
    private final PriorityQueue<Long> timestamps = new PriorityQueue<>();

    @Override
    public void incrementRequestCount(String key, Instant requestInstant) {
        CompletableFuture.runAsync(() -> {
            Long timestamp = requestInstant.getEpochSecond();
            synchronized (this) {
                MetricHolder metricsForTimestamp = metrics.get(timestamp);
                if (metricsForTimestamp != null) {
                    metricsForTimestamp.incrementRequestCountForKey(key);
                } else {
                    metrics.put(timestamp, new MetricHolder(key));
                    timestamps.add(timestamp);
                    if (timestamps.peek() < timestamp - RETENTION_SIZE_IN_SECONDS) {
                        metrics.remove(timestamps.poll());
                    }
                }
            }
        });
    }

    @Override
    public double getQPS(Instant end, Duration window) {
        throw new UnsupportedOperationException();
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
