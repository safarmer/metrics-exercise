package co.thatch.example.metrics;

import java.time.Clock;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.rnorth.ducttape.Preconditions;

@Singleton
public class SimpleMetricsService implements MetricsService {

  private static final int BUCKET_COUNT = (60 * 30) + 1;

  private static final Object lock = new Object();

  private final Clock clock;
  @GuardedBy("lock") private final AtomicInteger[] buckets = new AtomicInteger[BUCKET_COUNT];
  @GuardedBy("lock") private long timestamp;

  @Inject
  public SimpleMetricsService(Clock clock) {
    this.clock = clock;

    for (int i = 0; i < BUCKET_COUNT; i++) {
      buckets[i] = new AtomicInteger();
    }
    timestamp = clock.instant().getEpochSecond();
  }

  @Override
  public void recordRequest() {
    consolidateBuckets();
    buckets[toBucket(timestamp)].incrementAndGet();
  }

  @Override
  public double getQps(int window) {
    consolidateBuckets();
    int totalRequests = sum(timestamp - window, window);
    return totalRequests / (double) window;
  }

  @Override
  public Histogram history(int window, int bucketSize) {
    Preconditions.check("Window out of range", window > 0 && window < BUCKET_COUNT);
    Preconditions.check("Window not multiple of buckets", window % bucketSize == 0);
    consolidateBuckets();

    // Store the initial timestamp since it can change in another thread.
    final var start = timestamp;
    final var windowedBuckets = new ArrayList<Bucket>();

    int totalRequests = 0;

    for (long i = 0; i <= window; i += bucketSize) {
      final var bucketRequests = sum(start + i, bucketSize);
      totalRequests += bucketRequests;
      windowedBuckets.add(new Bucket(Math.toIntExact(i), bucketRequests));
    }

    final double qps = totalRequests / (double) window;
    return new Histogram(start, window, bucketSize, qps, windowedBuckets);
  }

  /** Returns the number of requests received in the {@code count} buckets from {@code start}. */
  private int sum(long start, int count) {
    int total = 0;
    for (long i = start; i < start + count; i++) {
      total += buckets[toBucket(i)].get();
    }
    return total;
  }

  /**
   * Updates the current timestamp if we are in a different second than the previous operation.
   *
   * <p>Any buckets in the half open interval (timestamp, now] will be reset to 0.
   */
  private void consolidateBuckets() {
    long now = clock.instant().getEpochSecond();
    if (timestamp < now) {
      synchronized (lock) {
        if (timestamp < now) {
          while (timestamp < now) {
            buckets[toBucket(++timestamp)].set(0);
          }
        }
      }
    }
  }

  /** Returns the provided value as an offset into the circular buffer. */
  private static int toBucket(long value) {
    return Math.toIntExact(value % BUCKET_COUNT);
  }
}
