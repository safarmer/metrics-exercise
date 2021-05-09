package co.thatch.example.metrics;

import co.thatch.example.metrics.model.Metric;
import co.thatch.example.metrics.model.MetricBucket;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;

@MicronautTest
class MetricsServiceTest {

    @Inject
    MetricsService metricsService;

    @AfterEach
    void reset() {
        metricsService.reset();
    }

    @ParameterizedTest
    @MethodSource("requestRates")
    void basicQPSTest(List<Integer> requestOffsets, Double expectedRequestRate) {
        Instant now = Instant.now();
        requestOffsets.forEach(requestOffset -> {
            metricsService.incrementRequestCount("GET_hello", now.minusSeconds(requestOffset));
        });

        Double actualQPS = metricsService.getQPS(now, Duration.ofSeconds(20));
        assertThat(actualQPS).isEqualTo(expectedRequestRate);

        Metric result = metricsService.getHistogram(null, now, Duration.ofSeconds(20), Duration.ofSeconds(5));
        assertThat(result.getRequestsPerSecond()).isEqualTo(expectedRequestRate);
    }

    @Test
    void basicHistogramTest() {
        Instant time = Instant.ofEpochSecond(1000);
        List<Integer> requestOffsets = List.of(9, 8, 7, 6, 5, 4, 3, 2, 1, 0);
        requestOffsets.forEach(requestOffset -> {
            metricsService.incrementRequestCount("GET_hello", time.minusSeconds(requestOffset));
        });

        Metric result = metricsService.getHistogram(null, time, Duration.ofSeconds(20), Duration.ofSeconds(20));
        assertThat(result.getRequestsPerSecond()).isEqualTo(0.5);
        assertThat(result.getBuckets().get(0).getRequestsPerSecond()).isEqualTo(0.5);
    }

    private static Stream<Arguments> requestRates() {
        return Stream.of(
                Arguments.of(List.of(10, 0), 0.1),
                Arguments.of(List.of(3, 2, 1, 0), 0.2),
                Arguments.of(List.of(5, 4, 3, 2, 1, 0), 0.3)
        );
    }

}
