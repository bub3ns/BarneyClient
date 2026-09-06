/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.modules.player.farming.mining;

public class MineLocation {
    private String serverId;
    private String serverRuName;
    private String mineName;
    private String mineRarity;
    private String nextMineRarity;
    private long resetSecondsLeft;
    private long fetchTime;

    public String getServerId() {
        return this.serverId;
    }

    public void setServerId(String string) {
        this.serverId = string;
    }

    public String getServerName() {
        return this.serverRuName;
    }

    public void setServerName(String string) {
        this.serverRuName = string;
    }

    public String getMineName() {
        return this.mineName;
    }

    public void setMineName(String string) {
        this.mineName = string;
    }

    public String getMineRarity() {
        return this.mineRarity;
    }

    public void setMineRarity(String string) {
        this.mineRarity = string;
    }

    public String getNextMineRarity() {
        return this.nextMineRarity;
    }

    public void setNextMineRarity(String string) {
        this.nextMineRarity = string;
    }

    public long getResetSecondsLeft() {
        return this.resetSecondsLeft;
    }

    public void setResetSecondsLeft(long l) {
        this.resetSecondsLeft = l;
    }

    public long getFetchTime() {
        return this.fetchTime;
    }

    public void setFetchTime(long l) {
        this.fetchTime = l;
    }

    public long getRemainingResetSeconds() {
        long l = (System.currentTimeMillis() - this.fetchTime) / 1000L;
        return Math.max(0L, this.resetSecondsLeft - l);
    }

    public String toString() {
        return "FunTimeMine{serverId='" + this.serverId + "', serverRuName='" + this.serverRuName + "', mineName='" + this.mineName + "', mineRarity='" + this.mineRarity + "', nextMineRarity='" + this.nextMineRarity + "', secondsRemaining=" + this.getRemainingResetSeconds() + "}";
    }
}

