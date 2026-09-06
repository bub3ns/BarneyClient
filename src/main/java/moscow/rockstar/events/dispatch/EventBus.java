/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.events.dispatch;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.Event;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.render.state.RenderEventScope;
import pyrock.events.render.ChatRenderEvent;
import pyrock.events.render.GameRendererEvent;
import pyrock.events.render.HandRenderEvent;
import pyrock.events.render.HudLayerRenderEvent;
import pyrock.events.render.HudRenderEvent;
import pyrock.events.render.MenuRenderEvent;
import pyrock.events.render.PostHudLayerRenderEvent;
import pyrock.events.render.PostHudRenderEvent;
import pyrock.events.render.PostMenuRenderEvent;
import pyrock.events.render.PreHudRenderEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.events.render.ScreenRenderEvent;

public class EventBus {
    private final ConcurrentHashMap<Type, CopyOnWriteArrayList<EventListener<?>>> listenersByEventType = new ConcurrentHashMap();
    private final Map<Class<?>, Field[]> listenerFieldsByClass = new HashMap();
    private final Comparator<EventListener<?>> listenerPriorityComparator = Comparator.comparingInt((EventListener<?> eventListener) -> eventListener.getPriority()).reversed();
    private final BiConsumer<List<EventListener<?>>, Comparator<EventListener<?>>> listenerListSorter = List::sort;
    /**
     * Keep the original console report, but also send the complete throwable to
     * the Minecraft logger so listener failures remain visible in latest.log.
     */
    private final Consumer<Throwable> exceptionReporter = throwable -> {
        throwable.printStackTrace();
        RockstarClient.LOGGER.error("[EventBus] event listener failed", throwable);
    };

    public void registerListeners(Object object) {
        this.visitListenerFields(object, (type2, eventListener) -> {
            this.listenersByEventType.computeIfAbsent((Type)type2, type -> new CopyOnWriteArrayList()).add(eventListener);
            this.listenerListSorter.accept((List)this.listenersByEventType.get(type2), this.listenerPriorityComparator);
        });
    }

    public void unregisterListeners(Object object) {
        this.visitListenerFields(object, (type, eventListener) -> {
            CopyOnWriteArrayList<EventListener<?>> copyOnWriteArrayList = this.listenersByEventType.get(type);
            if (copyOnWriteArrayList != null) {
                copyOnWriteArrayList.remove(eventListener);
                if (copyOnWriteArrayList.isEmpty()) {
                    this.listenersByEventType.remove(type);
                }
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public <T extends Event> void post(T t) {
        Class<?> clazz = t.getClass();
        List<EventListener<?>> listeners = this.listenersByEventType.get(clazz);
        boolean bl = this.isRenderEvent(t);
        if (bl) {
            RenderEventScope.begin();
        }
        try {
            RockstarClient.create().getScriptRegistry().dispatchEventToScripts(t);
            if (listeners != null && !RockstarClient.INSTANCE.isPanicMode()) {
                for (EventListener<?> listener : listeners) {
                    try {
                        @SuppressWarnings("unchecked")
                        EventListener<T> typedListener = (EventListener<T>) listener;
                        typedListener.onEvent(t);
                    }
                    catch (Throwable throwable) {
                        this.exceptionReporter.accept(throwable);
                    }
                }
            }
        }
        finally {
            if (bl) {
                RenderEventScope.end();
            }
        }
    }

    private boolean isRenderEvent(Event event) {
        return event instanceof PreHudRenderEvent || event instanceof HudRenderEvent || event instanceof PostHudRenderEvent || event instanceof ScreenRenderEvent || event instanceof MenuRenderEvent || event instanceof PostMenuRenderEvent || event instanceof HudLayerRenderEvent || event instanceof PostHudLayerRenderEvent || event instanceof ChatRenderEvent || event instanceof Render3DEvent || event instanceof HandRenderEvent || event instanceof GameRendererEvent;
    }

    private void visitListenerFields(Object object, BiConsumer<Type, EventListener<?>> biConsumer) {
        for (Field field : this.getListenerFields(object.getClass())) {
            EventListener<?> eventListener;
            if (field.getType() != EventListener.class || (eventListener = this.readEventListenerField(object, field)) == null) continue;
            Type type = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
            biConsumer.accept(type, eventListener);
        }
    }

    private Field[] getListenerFields(Class<?> clazz) {
        return this.listenerFieldsByClass.computeIfAbsent(clazz, Class::getDeclaredFields);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private EventListener<?> readEventListenerField(Object object, Field field) {
        boolean bl = field.canAccess(object);
        field.setAccessible(true);
        try {
            EventListener eventListener = (EventListener)field.get(object);
            return eventListener;
        }
        catch (IllegalAccessException illegalAccessException) {
            this.exceptionReporter.accept(illegalAccessException);
            EventListener<?> eventListener = null;
            return eventListener;
        }
        finally {
            field.setAccessible(bl);
        }
    }
}
