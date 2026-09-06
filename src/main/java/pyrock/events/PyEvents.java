/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jep.python.PyCallable
 *  lombok.Generated
 *  net.minecraft.Text
 */
package pyrock.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import jep.python.PyCallable;
import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.Event;
import moscow.rockstar.network.session.BotPacketListener;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.text.Text;
import pyrock.events.PyEvent;

public class PyEvents {
    private final ScriptDescriptor owner;
    private final Map<String, List<PyCallable>> listeners = new HashMap<String, List<PyCallable>>();
    private final Map<String, PyEvent> eventObjects = new HashMap<String, PyEvent>();
    private final Map<Class<?>, String> eventNameCache = new ConcurrentHashMap();
    private Consumer<Exception> errorHandler;
    private volatile boolean disposed;

    public PyEvents() {
        this(null);
    }

    public PyEvents(ScriptDescriptor scriptDescriptor) {
        this.owner = scriptDescriptor;
    }

    public void register(String string2, PyCallable pyCallable) {
        if (this.disposed) {
            return;
        }
        this.listeners.computeIfAbsent(string2.toLowerCase(), string -> new ArrayList()).add(pyCallable);
    }

    public void fire(Event event) {
        if (!this.isAlive()) {
            return;
        }
        String string = this.getEventName(event.getClass());
        if (string == null) {
            return;
        }
        List<PyCallable> list = this.listeners.get(string);
        if (list == null || list.isEmpty()) {
            return;
        }
        if (!moscow.rockstar.scripts.python.PythonRuntime.isAvailable()) {
            ClientAccess.minecraftClient.execute(() -> {
                if (this.isAlive()) {
                    this.fireOnOwnerThread(string, event);
                }
            });
            return;
        }
        this.fireOnOwnerThread(string, event);
    }

    private void fireOnOwnerThread(String string, Event event) {
        if (!this.isAlive()) {
            return;
        }
        List<PyCallable> list = this.listeners.get(string);
        if (list == null || list.isEmpty()) {
            return;
        }
        for (PyCallable pyCallable : list) {
            try {
                AutoCloseable autoCloseable = ScriptDescriptor.pushCurrentScript(this.owner);
                try {
                    pyCallable.call(new Object[]{event});
                }
                finally {
                    if (autoCloseable == null) continue;
                    autoCloseable.close();
                }
            }
            catch (Exception exception) {
                String string2 = exception.getMessage();
                if (string2 != null && string2.contains(":")) {
                    string2 = string2.substring(string2.indexOf(":") + 1).trim();
                }
                Notification.error(Text.of((String)("[Python Error] " + string2)));
                RockstarClient.LOGGER.error("Python error in event '" + string + "':", (Throwable)exception);
                if (this.errorHandler == null) break;
                this.errorHandler.accept(exception);
                break;
            }
        }
    }

    private String getEventName(Class<?> clazz) {
        String string = this.eventNameCache.get(clazz);
        if (string != null) {
            return string.isEmpty() ? null : string;
        }
        ScreenController screenController = clazz.getAnnotation(ScreenController.class);
        String string2 = screenController == null ? "" : screenController.description().toLowerCase();
        this.eventNameCache.put(clazz, string2);
        return string2.isEmpty() ? null : string2;
    }

    public PyEvent getEvent(String string2) {
        return this.eventObjects.computeIfAbsent(string2, string -> new PyEvent((String)string, this));
    }

    public void dispose() {
        this.disposed = true;
        this.listeners.clear();
        this.eventObjects.clear();
        this.errorHandler = null;
    }

    private boolean isAlive() {
        return !this.disposed && (this.owner == null || this.owner.isLoaded());
    }

    @Generated
    public void setErrorHandler(Consumer<Exception> consumer) {
        this.errorHandler = consumer;
    }
}
