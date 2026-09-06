/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Text
 */
package moscow.rockstar.api.registry;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.text.Text;

public sealed interface PluginResolver {
    public static <T> ValueResult<T> resolveValue(T t) {
        return new ValueResult<T>(t);
    }

    public static MessageResult resolveMessage(String string) {
        Notification.error(Text.of((String)string));
        return new MessageResult(string);
    }

    public static final class ValueResult<T>
    implements PluginResolver {
        private final T value;

        public ValueResult(T t) {
            this.value = t;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "value");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "value");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "value");
        }

        public T getValue() {
            return this.value;
        }
    }

    public static final class MessageResult
    implements PluginResolver {
        private final String message;

        public MessageResult(String string) {
            this.message = string;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "message");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "message");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "message");
        }

        public String getMessage() {
            return this.message;
        }
    }
}

