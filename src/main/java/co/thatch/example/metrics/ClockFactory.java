package co.thatch.example.metrics;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

import java.time.Clock;

@Factory
public class ClockFactory {
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
