package co.thatch.example.metrics.model;

import java.util.List;

public class Metric {
    private final double requestsPerSecond;
    private final List<MetricBucket> buckets;

    public Metric(double requestsPerSecond, List<MetricBucket> buckets) {
        this.requestsPerSecond = requestsPerSecond;
        this.buckets = buckets;
    }

    public double getRequestsPerSecond() {
        return requestsPerSecond;
    }

    public List<MetricBucket> getBuckets() {
        return buckets;
    }
}
