/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.api.validation;

public interface RequestContext {
    public String getCommandName();

    public String getStatusMessage();

    public boolean isPaused();

    public void pause();

    public void resume();

    public void stop();
}

