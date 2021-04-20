# Metrics Exercise

## Goal

To build a service that can track the average queries per-second (QPS) for a set of REST endpoints.

### Details

- There should be an endpoint to expose current values. We want to be able to expose:
    - the current/instantanious average QPS over the window period
    - values at fixed intervals over the window (e.g. 5 second buckets for a 30 second window)
- The resolution of the QPS average should be 1s. That is, we should be able to retrieve an updated value from the endpoint every second.
- The window size can be fixed (e.g. 30s) but it may be possible to provide the window size as a request paramater to the metrics endpoint.
- Recording metrics should not block/delay the incoming requests that are being measured.
- There should be some unit/integration tests to check results.

### Open Questions

- Can we expose total QPS for the system as well as QPS per endpoint/path?
- Single request for all metrics, or query params/different endpoints for different metrics?
- Is it possible to gather metrics for all endpoints without needing to ad code to each controller?
- How would we persist metrics if we wanted to store historic data? How would we ensure that this wouldn't impact performance of the instrumented endpoints?

### Notes

- The application has a basic REST controller that implements a GET and a POST endpoint. These can be instrumented to verif the integration of the metrics service.
- There is no assumed prior knowledge of the Micronaut framework. As such it is understood that there may be an element of research to some of the implementation. A basic starting point has been provided to demonstrate the basics of a REST endpoint in the framework.
- Micronaut tests spin up a complete application server so can be treated as integration tests if needed. Mockito is on the classpath and can be helpful for simulating time, but it is also fine for the tests to be slow and work on a smaller time scale.

## Helpful Documentation

### Micronaut 2.4.2 Documentation

- [User Guide](https://docs.micronaut.io/2.4.2/guide/index.html)
- [API Reference](https://docs.micronaut.io/2.4.2/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/2.4.2/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)

---

### Feature testcontainers documentation

- [https://www.testcontainers.org/](https://www.testcontainers.org/)

### Feature data-jdbc documentation

- [Micronaut Data JDBC documentation](https://micronaut-projects.github.io/micronaut-data/latest/guide/index.html#jdbc)

### Feature mockito documentation

- [https://site.mockito.org](https://site.mockito.org)

### Feature http-client documentation

- [Micronaut HTTP Client documentation](https://docs.micronaut.io/latest/guide/index.html#httpClient)
