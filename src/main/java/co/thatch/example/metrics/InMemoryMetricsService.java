package co.thatch.example.metrics;

import co.thatch.example.metrics.model.Metric;
import co.thatch.example.metrics.model.MetricBucket;
import io.micronaut.scheduling.annotation.Scheduled;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.DoubleAdder;


@Singleton
public class InMemoryMetricsService implements MetricsService {
    private static final int MAX_WINDOW_SIZE_WITH_BUFFER = 1_800 + 1;

    private final Clock clock;

    private final DoubleAdder requestCounter = new DoubleAdder();
    private final double[] requestCountRecordings = new double[MAX_WINDOW_SIZE_WITH_BUFFER];

    @Inject
    public InMemoryMetricsService(Clock clock) {
        this.clock = clock;
    }

    @Scheduled(fixedRate = "100ms")
    public void pollAndRecordRequestCount() {
        recordRequestCount(Instant.now(clock));
    }

    @Override
    public void incrementRequestCount(String key, Instant requestInstant) {
        requestCounter.add(1.0);
        recordRequestCount(requestInstant);
    }

    private void recordRequestCount(Instant time) {
        requestCountRecordings[Math.toIntExact(time.getEpochSecond() % MAX_WINDOW_SIZE_WITH_BUFFER)] = requestCounter.doubleValue();
    }

    @Override
    public double getQPS(Instant end, Duration window) {
        double count = getCountForRange(end.minus(window).getEpochSecond(), end.getEpochSecond());
        return count / window.getSeconds();
    }

    private double getCountForRange(Long start, Long end) {
        double startCount = requestCountRecordings[(Math.toIntExact(start - 1) % MAX_WINDOW_SIZE_WITH_BUFFER)];
        double endCount = requestCountRecordings[(Math.toIntExact(end) % MAX_WINDOW_SIZE_WITH_BUFFER)];
        return endCount - startCount;
    }

    @Override
    public Metric getHistogram(String key, Instant end, Duration window, Duration interval) {
        Instant start = end.minus(window);
        List<MetricBucket> buckets = new ArrayList<>();
        for (long rangeStart = start.getEpochSecond(); rangeStart <= end.getEpochSecond(); rangeStart += interval.getSeconds()) {
            long rangeEnd = rangeStart + interval.getSeconds();
            double requestCount = getCountForRange(rangeStart, rangeEnd);
            buckets.add(new MetricBucket(String.valueOf(rangeStart), requestCount / interval.getSeconds()));
        }
        double requestsPerSecond = getQPS(end, window);
        return new Metric(requestsPerSecond, buckets);
    }

    @Override
    public void reset() {
        requestCounter.reset();
        Arrays.fill(requestCountRecordings, 0.0);
    }
}
