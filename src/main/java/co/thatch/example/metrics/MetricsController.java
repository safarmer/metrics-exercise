package co.thatch.example.metrics;

import co.thatch.example.metrics.model.Metric;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;

import javax.inject.Inject;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.Duration;
import java.time.Instant;

@Controller("/metrics")
public class MetricsController {

    private final MetricsService metricsService;

    @Inject
    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Get("{?windowInSeconds}")
    public double getQPS(
            @QueryValue(defaultValue = "30") @Min(1) @Max(1800) Integer windowInSeconds
    ) {
        return metricsService.getQPS(Duration.ofSeconds(windowInSeconds));
    }


    @Get("/histogram{?key,windowInSeconds,intervalInSeconds}")
    public Metric getMetric(
            @Nullable @QueryValue String key,
            @QueryValue(defaultValue = "30") @Min(1) @Max(1800) Integer windowInSeconds,
            @QueryValue(defaultValue = "5") @Min(1) @Max(1800) Integer intervalInSeconds
    ) {
        return metricsService.getHistogram(key, Instant.now(), Duration.ofSeconds(windowInSeconds), Duration.ofSeconds(intervalInSeconds));
    }

}
