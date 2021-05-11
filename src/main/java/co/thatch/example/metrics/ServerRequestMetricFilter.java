package co.thatch.example.metrics;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.OncePerRequestHttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import org.reactivestreams.Publisher;

import java.time.Instant;

@Filter("/**")
public class ServerRequestMetricFilter extends OncePerRequestHttpServerFilter {

    private final MetricsService metricsService;

    public ServerRequestMetricFilter(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilterOnce(HttpRequest<?> request, ServerFilterChain chain) {
        metricsService.incrementRequestCount(generateKey(request), Instant.now());
        return chain.proceed(request);
    }

    private String generateKey(HttpRequest<?> request) {
        return "%s_%s".formatted(request.getMethod().toString(), request.getPath());
    }
}
