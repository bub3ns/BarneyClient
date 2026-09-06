package moscow.rockstar.network.http;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Parses the HTTP Retry-After header as a date or a number of seconds. */
public final class DefaultRetryAfterPolicy implements RetryAfterPolicy {
    @Override
    public RetryDecision createDecision(HttpResponse response) {
        String value = response.getFirstHeader("Retry-After").orElse(null);
        Long delay = value == null ? null : parseDelay(value);
        return delay != null && delay > 0L
            ? RetryDecision.afterMillis(delay)
            : RetryDecision.DO_NOT_RETRY;
    }

    private Long parseDelay(String value) {
        try {
            return Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(value)).toEpochMilli()
                - Instant.now().toEpochMilli();
        } catch (DateTimeParseException ignored) {
            try {
                return Long.parseLong(value) * 1000L;
            } catch (NumberFormatException ignoredNumber) {
                return null;
            }
        }
    }
}
