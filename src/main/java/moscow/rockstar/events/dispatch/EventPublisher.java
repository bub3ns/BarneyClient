/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.events.dispatch;

import moscow.rockstar.api.plugins.PluginEntry;
import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.Event;
import moscow.rockstar.events.dispatch.CommandArgument;
import moscow.rockstar.events.dispatch.EventMessage;
import moscow.rockstar.events.dispatch.EventStatistics;
import moscow.rockstar.events.dispatch.MessagePair;
import pyrock.events.newton.NewtonFailedEvent;
import pyrock.events.newton.NewtonFinishedEvent;
import pyrock.events.newton.NewtonNodeEvent;
import pyrock.events.newton.NewtonPathEvent;
import pyrock.events.newton.NewtonStartedEvent;

public final class EventPublisher {
    private EventPublisher() {
    }

    public static void publishNewtonStarted(String string) {
        EventPublisher.postEvent(new NewtonStartedEvent(string));
    }

    public static void publishNewtonPathProgress(String string, int n) {
        EventPublisher.postEvent(new NewtonPathEvent(string, n));
        EventPublisher.publishPluginEntry(new CommandArgument(string, n));
    }

    public static void publishNewtonNodeProgress(String string, int n, int n2, int n3, int n4, int n5) {
        EventPublisher.postEvent(new NewtonNodeEvent(string, n, n2, n3, n4, n5));
        EventPublisher.publishPluginEntry(new EventStatistics(string, n, n2, n3, n4, n5));
    }

    public static void publishNewtonFinished(String string) {
        EventPublisher.postEvent(new NewtonFinishedEvent(string));
        EventPublisher.publishPluginEntry(new EventMessage(string));
    }

    public static void publishNewtonFailed(String string, String string2) {
        EventPublisher.postEvent(new NewtonFailedEvent(string, string2));
        EventPublisher.publishPluginEntry(new MessagePair(string, string2));
    }

    private static void postEvent(Event event) {
        try {
            RockstarClient rockstarClient = RockstarClient.create();
            if (rockstarClient != null && rockstarClient.getEventBus() != null) {
                rockstarClient.getEventBus().post(event);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void publishPluginEntry(PluginEntry pluginEntry) {
        try {
            if (ClientServiceRegistry.isInitialized()) {
                ClientServiceRegistry.getInstance().getNavigationService().publishPluginEntry(pluginEntry);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

