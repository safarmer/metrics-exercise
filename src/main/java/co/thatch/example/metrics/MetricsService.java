package co.thatch.example.metrics;

import co.thatch.example.metrics.model.Metric;

import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;

@Singleton
public interface MetricsService {

    void incrementRequestCount(String key, Instant requestInstant);

    double getQPS(Instant end, Duration window);

    Metric getHistogram(String key, Instant end, Duration window, Duration interval);

    void reset();

}
