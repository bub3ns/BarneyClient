package moscow.rockstar.ui.search;

import java.text.Normalizer;
import java.util.Locale;

/** Normalization and ranking used by the module/settings search fields. */
public final class SearchMatcher {
    public static final int NO_MATCH = Integer.MAX_VALUE;

    private SearchMatcher() {
    }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFKD).toLowerCase(Locale.ROOT);
        StringBuilder normalized = new StringBuilder(decomposed.length());
        boolean separator = false;
        for (int index = 0; index < decomposed.length(); index++) {
            char character = decomposed.charAt(index);
            if (Character.getType(character) == Character.NON_SPACING_MARK) {
                continue;
            }
            if (Character.isLetterOrDigit(character)) {
                if (separator && !normalized.isEmpty()) {
                    normalized.append(' ');
                }
                normalized.append(character);
                separator = false;
            } else {
                separator = true;
            }
        }
        return normalized.toString();
    }

    public static int score(String candidate, String query) {
        return scoreNormalized(normalize(candidate), normalize(query));
    }

    public static int scoreNormalized(String candidate, String query) {
        if (candidate == null || query == null || candidate.isEmpty() || query.isEmpty()) {
            return NO_MATCH;
        }
        if (candidate.equals(query)) {
            return 0;
        }
        String compactCandidate = removeSpaces(candidate);
        String compactQuery = removeSpaces(query);
        if (compactCandidate.equals(compactQuery)) {
            return 10;
        }
        if (candidate.startsWith(query)) {
            return 20 + candidate.length() - query.length();
        }
        if (compactCandidate.startsWith(compactQuery)) {
            return 40 + compactCandidate.length() - compactQuery.length();
        }
        int wordStart = findWordStart(candidate, query);
        if (wordStart >= 0) {
            return 80 + wordStart;
        }
        int directStart = candidate.indexOf(query);
        if (directStart >= 0) {
            return 120 + directStart;
        }
        int compactStart = compactCandidate.indexOf(compactQuery);
        if (compactStart >= 0) {
            return 160 + compactStart;
        }
        int tokenPosition = findTokenPosition(candidate, query);
        if (tokenPosition != NO_MATCH) {
            return 220 + tokenPosition;
        }
        int lengthDifference = Math.abs(compactCandidate.length() - compactQuery.length());
        int allowedDistance = compactQuery.length() >= 8 ? 2 : compactQuery.length() >= 4 ? 1 : 0;
        if (allowedDistance == 0 || lengthDifference > allowedDistance) {
            return NO_MATCH;
        }
        int editDistance = boundedEditDistance(compactCandidate, compactQuery, allowedDistance);
        return editDistance <= allowedDistance
                ? 300 + editDistance * 10 + lengthDifference
                : NO_MATCH;
    }

    private static String removeSpaces(String value) {
        return value.indexOf(' ') < 0 ? value : value.replace(" ", "");
    }

    private static int findWordStart(String candidate, String query) {
        int index = candidate.indexOf(query);
        while (index >= 0) {
            if (index == 0 || candidate.charAt(index - 1) == ' ') {
                return index;
            }
            index = candidate.indexOf(query, index + 1);
        }
        return -1;
    }

    private static int findTokenPosition(String candidate, String query) {
        int total = 0;
        int offset = 0;
        while (offset < query.length()) {
            int end = query.indexOf(' ', offset);
            if (end < 0) {
                end = query.length();
            }
            String token = query.substring(offset, end);
            int tokenIndex = candidate.indexOf(token);
            if (tokenIndex < 0) {
                return NO_MATCH;
            }
            total += tokenIndex;
            offset = end + 1;
        }
        return total;
    }

    private static int boundedEditDistance(String candidate, String query, int limit) {
        int[] previousPrevious = null;
        int[] previous = new int[query.length() + 1];
        int[] current = new int[query.length() + 1];
        for (int column = 0; column <= query.length(); column++) {
            previous[column] = column;
        }
        for (int row = 1; row <= candidate.length(); row++) {
            current[0] = row;
            for (int column = 1; column <= query.length(); column++) {
                int distance = previous[column - 1]
                        + (candidate.charAt(row - 1) == query.charAt(column - 1) ? 0 : 1);
                current[column] = Math.min(Math.min(previous[column] + 1, current[column - 1] + 1), distance);
                if (previousPrevious != null && row > 1 && column > 1
                        && candidate.charAt(row - 1) == query.charAt(column - 2)
                        && candidate.charAt(row - 2) == query.charAt(column - 1)) {
                    current[column] = Math.min(current[column], previousPrevious[column - 2] + 1);
                }
            }
            int[] oldPreviousPrevious = previousPrevious;
            previousPrevious = previous;
            previous = current;
            current = oldPreviousPrevious == null ? new int[query.length() + 1] : oldPreviousPrevious;
        }
        return previous[query.length()];
    }
}
