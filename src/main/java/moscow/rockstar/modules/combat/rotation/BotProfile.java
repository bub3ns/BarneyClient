package moscow.rockstar.modules.combat.rotation;

public record BotProfile(String fullTag, String prefix, boolean noPrefix) {
    public String getFullTag() {
        return this.fullTag;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public boolean isNoPrefix() {
        return this.noPrefix;
    }
}
