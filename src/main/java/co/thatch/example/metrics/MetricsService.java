package co.thatch.example.metrics;

import co.thatch.example.metrics.model.Metric;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

public interface MetricsService {

  /**
   * Records a query occuring at the current timestamp.
   */
  void recordRequest();

  /**
   * Returns the average QPS for the last {@code window} seconds.
   */
  double getQps(int window);

  /**
   * Returns the request history for the last  {@code window} seconds with {@code bucketSize}
   * buckets in seconds.
   */
  Histogram history(int window, int bucketSize);

  /**
   * Models the request history over a given window with details of request buckets.
   *
   * @param timestamp  the start time of the history window.
   * @param window     the size of the history window in seconds.
   * @param bucketSize the number of seconds in each bucket.
   * @param qps        the average QPS over the history window.
   * @param buckets    list of buckets containing offset from {@see start} and number of requests.
   */
  record Histogram(
      long timestamp, int window, int bucketSize, double qps, List<Bucket> buckets
  ) {}

  /**
   * A history bucket with an offset and number of requests.
   */
  record Bucket(int offset, int requests) {}


  default void incrementRequestCount(String key, Instant requestInstant) {}

  default double getQPS(Instant end, Duration window) {
    return -1.0;
  }

  default Metric getHistogram(String key, Instant end, Duration window, Duration interval) {
    return new Metric(getQPS(end, window), Collections.emptyList());
  }

  default void reset() {}
}
