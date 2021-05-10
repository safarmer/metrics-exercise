package co.thatch.example.metrics.model;

import java.util.List;

public class Metric {
    private final double qps;
    private final List<MetricBucket> buckets;

    public Metric(double qps, List<MetricBucket> buckets) {
        this.qps = qps;
        this.buckets = buckets;
    }

    public double getQPS() {
        return qps;
    }

    public List<MetricBucket> getBuckets() {
        return buckets;
    }
}
