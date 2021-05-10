package co.thatch.example.metrics.model;

public class MetricBucket {
    private final String key;
    private final double qps;

    public MetricBucket(String key, double qps) {
        this.key = key;
        this.qps = qps;
    }

    public String getKey() {
        return key;
    }

    public double getQPS() {
        return qps;
    }
}
