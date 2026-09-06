package moscow.rockstar.network.http;

import java.util.Objects;

/** Connection/response retry limits and the policy used for Retry-After. */
public final class RetryPolicy {
    private int maxConnectionRetries;
    private int maxResponseRetries;
    private RetryAfterPolicy retryAfterPolicy;

    public RetryPolicy() {
        this(0, 0);
    }

    public RetryPolicy(int maxConnectionRetries, int maxResponseRetries) {
        this(maxConnectionRetries, maxResponseRetries, new DefaultRetryAfterPolicy());
    }

    public RetryPolicy(int maxConnectionRetries, int maxResponseRetries, RetryAfterPolicy retryAfterPolicy) {
        this.maxConnectionRetries = maxConnectionRetries;
        this.maxResponseRetries = maxResponseRetries;
        this.retryAfterPolicy = Objects.requireNonNull(retryAfterPolicy, "retryAfterPolicy");
    }

    public int getMaxConnectionRetries() {
        return maxConnectionRetries;
    }

    public RetryPolicy setMaxConnectionRetries(int value) {
        this.maxConnectionRetries = value;
        return this;
    }

    public int getMaxResponseRetries() {
        return maxResponseRetries;
    }

    public RetryPolicy setMaxResponseRetries(int value) {
        this.maxResponseRetries = value;
        return this;
    }

    public RetryAfterPolicy getRetryAfterPolicy() {
        return retryAfterPolicy;
    }

    public RetryPolicy setRetryAfterPolicy(RetryAfterPolicy policy) {
        this.retryAfterPolicy = Objects.requireNonNull(policy, "retryAfterPolicy");
        return this;
    }
}
