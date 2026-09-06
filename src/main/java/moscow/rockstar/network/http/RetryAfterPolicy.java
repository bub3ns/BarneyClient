package moscow.rockstar.network.http;

@FunctionalInterface
public interface RetryAfterPolicy {
    RetryDecision createDecision(HttpResponse response);
}
