package co.thatch.example.metrics;

import static com.google.common.truth.Truth.assertThat;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;

@MicronautTest
public class FooControllerTest {
  @Inject
  @Client("/foo")
  HttpClient client;

  @Test
  public void getFoo() {
    var result = client.toBlocking().retrieve("/");
    assertThat(result).isEqualTo("foo");
  }

  @Test
  public void newFoo() {
    var result = client.toBlocking().retrieve(HttpRequest.POST("/", ""));
    assertThat(result.length()).isEqualTo(16);
  }
}
