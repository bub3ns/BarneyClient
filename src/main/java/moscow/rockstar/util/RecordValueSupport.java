package moscow.rockstar.util;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Source-level equivalent of the record methods CFR prints as direct calls
 * to {@code ObjectMethods.bootstrap}.
 */
public final class RecordValueSupport {
    private RecordValueSupport() {
    }

    public static String toString(Object value, String... fields) {
        StringBuilder result = new StringBuilder(value.getClass().getSimpleName()).append('[');
        for (int i = 0; i < fields.length; i++) {
            if (i != 0) {
                result.append(", ");
            }
            result.append(fields[i]).append('=').append(read(value, fields[i]));
        }
        return result.append(']').toString();
    }

    public static int hashCode(Object value, String... fields) {
        int result = 1;
        for (String field : fields) {
            result = 31 * result + Objects.hashCode(read(value, field));
        }
        return result;
    }

    public static boolean equals(Object value, Object other, String... fields) {
        if (value == other) {
            return true;
        }
        if (other == null || value.getClass() != other.getClass()) {
            return false;
        }
        for (String field : fields) {
            if (!Objects.equals(read(value, field), read(other, field))) {
                return false;
            }
        }
        return true;
    }

    private static Object read(Object value, String fieldName) {
        try {
            Field field = value.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Record component is missing: " + fieldName, exception);
        }
    }
}
