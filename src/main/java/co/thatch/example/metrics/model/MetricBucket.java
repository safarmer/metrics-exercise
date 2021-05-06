package co.thatch.example.metrics.model;

public class MetricBucket {
    private final String key;
    private double requestsPerSecond;

    public MetricBucket(String key, double requestsPerSecond) {
        this.key = key;
        this.requestsPerSecond = requestsPerSecond;
    }

    public String getKey() {
        return key;
    }

    public double getRequestsPerSecond() {
        return requestsPerSecond;
    }
}
