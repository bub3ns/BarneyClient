/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.network.bot;

import moscow.rockstar.network.bot.BotController;

public interface BotBehaviorStrategy {
    public void applyBehavior(BotController var1);

    default public String getDescription() {
        String string = this.getClass().getSimpleName();
        return string.endsWith("Behavior") ? string.substring(0, string.length() - "Behavior".length()) : string;
    }
}

