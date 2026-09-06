/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.server;

public enum ServerProfile {
    FUNTIME("funtime", "playft"),
    SPOOKY("spooky"),
    REALLYWORLD("reallyworld", "playrw"),
    HOLYWORLD("holy", "holly", "playhw"),
    CHERRY_PIZZA("cherry.pizza"),
    MODERN_NETWORKS("funtime", "playft", "reallyworld", "playrw", "funsky", "slimeworld"),
    MINEBLAZE_DEXLAND("mineblaze", "dexland"),
    COMMON_NETWORKS("funtime", "playft", "spooky", "funsky", "holytime"),
    SATURN("saturn"),
    FUNSKY("funsky"),
    SUPPORTED_NETWORKS("funtime", "playft", "spooky", "funsky", "slimeworld", "cakeworld", "holytime"),
    HOLYWORLD_VARIANTS("holyworld", "playhw", "hollyworld"),
    REALLYWORLD_VARIANTS("reallyworld", "playrw", "slimeworld", "cakeworld");
    private final String[] addressAliases;

    private ServerProfile(String ... stringArray) {
        this.addressAliases = stringArray;
    }

    public boolean matchesAddress(String string) {
        string = string.toLowerCase();
        for (String string2 : this.addressAliases) {
            if (!string.contains(string2)) continue;
            return true;
        }
        return false;
    }
}

