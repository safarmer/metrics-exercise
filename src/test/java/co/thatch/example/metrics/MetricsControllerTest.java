package co.thatch.example.metrics;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static com.google.common.truth.Truth.assertThat;

@MicronautTest
public class MetricsControllerTest {

    @Inject
    @Client("/metrics")
    HttpClient client;

    @Test
    public void getQPS() {
        var result = client.toBlocking().exchange("/");
        assertThat(result.status()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getHistogram() {
        var result = client.toBlocking().exchange("/histogram");
        assertThat(result.status()).isEqualTo(HttpStatus.OK);
    }

}
