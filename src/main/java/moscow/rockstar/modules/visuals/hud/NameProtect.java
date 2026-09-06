/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.PlayerListEntry
 */
package moscow.rockstar.modules.visuals.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.settings.StringSetting;
import moscow.rockstar.social.FriendListManager;
import moscow.rockstar.render.text.TextCaptureController;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Name Protect", category=ModuleCategory.OTHER, description="modules.descriptions.name_protect")
public class NameProtect
extends Module {
    private StringSetting fakeNameSetting;
    private BooleanSetting hideFriendsSetting;
    private StringSetting friendFakeNameSetting;
    private BooleanSetting streamerModeSetting;
    private BooleanSetting captureBypassSetting;
    private static final int CASE_INSENSITIVE_UNICODE_FLAGS = 66;
    private static final Pattern anarchyPattern = Pattern.compile("(\u0410\u043d\u0430\u0440\u0445\u0438[\u044f\u0438]\\s*(?:PvP\\s*)?(?:[#\u2116:\\-]|\\s)+)(\\d{1,4})", 66);
    private static final Pattern classicPattern = Pattern.compile("((?:\u041b\u0430\u0439\u0442|Lite|\u041a\u043b\u0430\u0441\u0441\u0438\u043a|Classic|Classik)\\s+\u0410\u043d\u0430\u0440\u0445\u0438[\u044f\u0438]\\s*[#\u2116:\\-]?\\s*|(?:\u041b\u0430\u0439\u0442|Lite|\u041a\u043b\u0430\u0441\u0441\u0438\u043a|Classic|Classik)\\s*[#\u2116]\\s*)(\\d{1,4})", 66);
    private static final Pattern lobbyPattern = Pattern.compile("(?:play\\.)?holyworld(?:\\.[a-z0-9_.-]+)*", 66);
    private static final Pattern serverPattern = Pattern.compile("(?:play\\.)?hollyworld(?:\\.[a-z0-9_.-]+)*", 66);
    private static final Pattern playerPattern = Pattern.compile("playhw(?:\\.[a-z0-9_.-]+)*", 66);
    private static final Pattern coordinatePattern = Pattern.compile("Holy\\s*World|HolyWorld|HollyWorld", 66);
    private static final Pattern numberPattern = Pattern.compile("/(?:anarchy|an|lite|classik|classic)\\s*[#\u2116\\-]?\\s*\\d{1,4}", 66);
    private static final Pattern chatPattern = Pattern.compile("\\b(?:anarchy|an|lite|classik|classic)\\s*[#\u2116\\-]?\\s*\\d{1,4}\\b", 66);
    private static final Pattern namePattern = Pattern.compile("\\b(?:Lite|Classic|Classik)\\b", 66);
    private static final Pattern addressPattern = Pattern.compile("(?<![\u0410-\u042f\u0430-\u044f\u0401\u0451])(?:\u041b\u0430\u0439\u0442|\u041a\u043b\u0430\u0441\u0441\u0438\u043a)(?![\u0410-\u042f\u0430-\u044f\u0401\u0451])", 66);
    private static final Pattern rankPattern = Pattern.compile("\u0410\u043d\u0430\u0440\u0445\u0438[\u044f\u0438]", 66);
    private static final Pattern locationPattern = Pattern.compile("\\bAnarchy\\b", 66);
    private static final Pattern channelPattern = Pattern.compile("(?<![A-Za-z0-9_-])(?:(?:[a-z0-9-]+\\.)+[a-z]{2,63}|(?:\\d{1,3}\\.){3}\\d{1,3})(?::\\d{1,5})?(?![A-Za-z0-9_-])", 66);
    private static final Pattern worldPattern = Pattern.compile("(?<![\\d.-])-?\\d{1,8}(?:\\.\\d+)?(?:\\s*(?:,|/|;|\\s)\\s*)-?\\d{1,8}(?:\\.\\d+)?(?:\\s*(?:,|/|;|\\s)\\s*)-?\\d{1,8}(?:\\.\\d+)?(?![\\d.])");
    private static final Pattern prefixPattern = Pattern.compile("(?:(?:[xXyYzZ])\\s*[:=]\\s*-?\\d{1,8}(?:\\.\\d+)?\\s*[,;/]?\\s*){3}");
    private static final Pattern fallbackPattern = Pattern.compile("((?:\u0410\u043d\u0430\u0440\u0445\u0438[\u044f\u0438]|Anarchy|\u041b\u0430\u0439\u0442|Lite|\u041a\u043b\u0430\u0441\u0441\u0438\u043a|Classic|Classik|\u0413\u0440\u0438\u0444(?:\u0435\u0440\u0441\u043a\u0438\u0439)?)\\s*(?:PvP\\s*)?(?:[#\u2116:\\-]|\\s)+)(\\d{1,4})", 66);
    private final Set<String> capturedNames = new HashSet<String>();
    private final Map<String, String> replacementNames = new HashMap<String, String>();
    private String lastServerAddress = "";
    private int lastProcessedTick = -1;
    private Pattern replacementPattern;
    private boolean replacementPatternDirty = true;

    public NameProtect() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.fakeNameSetting = new StringSetting(this, "modules.settings.name_protect.fake_name").setValue("Player");
        this.hideFriendsSetting = new BooleanSetting(this, "modules.settings.name_protect.hide_friends").enable();
        this.friendFakeNameSetting = new StringSetting((SettingOwner)this, "modules.settings.name_protect.friend_fake_name", () -> !this.hideFriendsSetting.isEnabled()).setValue("Friend");
        this.streamerModeSetting = new BooleanSetting(this, "modules.settings.name_protect.streamer_mode");
        this.captureBypassSetting = new BooleanSetting(this, "modules.settings.name_protect.capture_bypass");
    }

    public boolean containsProtectedText(CharSequence charSequence) {
        if (charSequence == null || charSequence.isEmpty()) {
            return false;
        }
        if (this.streamerModeSetting.isEnabled()) {
            return true;
        }
        if (this.containsName(charSequence, minecraftClient.getSession().getUsername())) {
            return true;
        }
        if (EntityUtils.isClientWorldReady() && this.containsName(charSequence, NameProtect.minecraftClient.player.getDisplayName().getString())) {
            return true;
        }
        if (this.hideFriendsSetting.isEnabled()) {
            for (String string : RockstarClient.create().getFriendListManager().getFriends()) {
                if (string == null || string.isEmpty() || !this.containsName(charSequence, string)) continue;
                return true;
            }
        }
        return false;
    }

    public boolean isStreamerModeActive() {
        return this.isEnabled() && this.streamerModeSetting.isEnabled();
    }

    public boolean containsCapturedName(CharSequence charSequence) {
        if (this.capturedNames.isEmpty() || !this.containsProtectedText(charSequence)) {
            return false;
        }
        for (String string : this.capturedNames) {
            if (!this.containsName(charSequence, string)) continue;
            return true;
        }
        return false;
    }

    public void clearCapturedNames() {
        this.capturedNames.clear();
    }

    private boolean containsName(CharSequence charSequence, String string) {
        if (string == null || string.isEmpty() || string.length() > charSequence.length()) {
            return false;
        }
        if (charSequence instanceof String) {
            String string2 = (String)charSequence;
            return string2.contains(string);
        }
        int n = 0;
        while (n + string.length() <= charSequence.length()) {
            block5: {
                for (int i = 0; i < string.length(); ++i) {
                    if (charSequence.charAt(n + i) == string.charAt(i)) {
                        continue;
                    }
                    break block5;
                }
                return true;
            }
            ++n;
        }
        return false;
    }

    public String replaceProtectedText(String string) {
        if (string == null || string.isEmpty()) {
            return string;
        }
        String string2 = minecraftClient.getSession().getUsername();
        String string3 = this.getConfiguredReplacement(this.fakeNameSetting.getValue(), "Player");
        if (EntityUtils.isClientWorldReady()) {
            string = string.replace(NameProtect.minecraftClient.player.getDisplayName().getString(), string3);
        }
        string = string.replace(string2, string3);
        if (this.hideFriendsSetting.isEnabled()) {
            FriendListManager friendListManager = RockstarClient.create().getFriendListManager();
            String string4 = this.getConfiguredReplacement(this.friendFakeNameSetting.getValue(), "Friend");
            for (String string5 : friendListManager.getFriends()) {
                if (string5 == null || string5.isEmpty()) continue;
                string = string.replace(string5, string4);
            }
        }
        if (this.streamerModeSetting.isEnabled()) {
            string = this.sanitizeServerAddressText(string);
        }
        return string;
    }

    public String replacePlayerOrServerName(String string) {
        if (string == null || string.isEmpty()) {
            return string;
        }
        if (TextCaptureController.shouldKeepOriginalText()) {
            this.capturedNames.add(string);
            return string;
        }
        String string2 = this.replaceExactName(string);
        if (!Objects.equals(string2, string)) {
            return string2;
        }
        if (this.streamerModeSetting.isEnabled()) {
            this.refreshServerNameCache();
            return this.replaceServerName(string);
        }
        return string;
    }

    public String replaceTextWithName(String string, String string2) {
        if (string == null || string.isEmpty()) {
            return string;
        }
        if (TextCaptureController.shouldKeepOriginalText()) {
            return string;
        }
        if (string2 == null || string2.isEmpty()) {
            return this.replaceProtectedText(string);
        }
        String string3 = this.replacePlayerOrServerName(string2);
        if (!Objects.equals(string3, string2)) {
            return this.replaceDelimitedName(string, string2, string3);
        }
        return this.replaceProtectedText(string);
    }

    private String sanitizeServerAddressText(String string) {
        this.refreshServerNameCache();
        string = this.replaceCachedChatNames(string);
        string = this.replaceServerIdentifiers(string);
        string = this.replaceRegexMatches(string, prefixPattern, "");
        string = this.replaceRegexMatches(string, worldPattern, "\u2014");
        string = this.replaceAnarchyCoordinates(string);
        return this.replaceServerContext(string);
    }

    private String replaceServerIdentifiers(String string) {
        String string2;
        String string3 = ServerDetector.getConnectedServerAddress();
        if (string3 != null && !string3.isBlank() && !string3.equalsIgnoreCase("single")) {
            string = string.replace(string3, "server.local");
        }
        if ((string2 = ServerDetector.getFormattedServerName(false)) != null && string2.length() > 2 && !string2.equalsIgnoreCase("single")) {
            string = this.replaceDelimitedName(string, string2, "Server");
        }
        return this.replaceRegexMatches(string, channelPattern, "server.local");
    }

    private String replaceAnarchyCoordinates(String string) {
        Matcher matcher = fallbackPattern.matcher(string);
        StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(stringBuffer, Matcher.quoteReplacement(matcher.group(1) + "\u2014"));
        }
        matcher.appendTail(stringBuffer);
        return this.replaceServerWorldNames(stringBuffer.toString());
    }

    private String replaceServerWorldNames(String string) {
        int n = ServerDetector.getDetectedServerMode();
        if (n <= 0) {
            return string;
        }
        string = this.replaceServerNumber(string, "#", n, "\u2014");
        return this.replaceServerNumber(string, "\u2116", n, "\u2014");
    }

    private void refreshServerNameCache() {
        if (NameProtect.minecraftClient.player == null || NameProtect.minecraftClient.player.networkHandler == null) {
            this.replacementNames.clear();
            this.lastServerAddress = "";
            this.lastProcessedTick = -1;
            this.replacementPatternDirty = true;
            return;
        }
        String string = ServerDetector.getConnectedServerAddress();
        if (!Objects.equals(this.lastServerAddress, string)) {
            this.replacementNames.clear();
            this.lastServerAddress = string;
            this.lastProcessedTick = -1;
            this.replacementPatternDirty = true;
        }
        if (this.lastProcessedTick == NameProtect.minecraftClient.player.age) {
            return;
        }
        this.lastProcessedTick = NameProtect.minecraftClient.player.age;
        for (PlayerListEntry ServerSamplerSource : NameProtect.minecraftClient.player.networkHandler.getPlayerList()) {
            String string2 = ServerSamplerSource.getProfile().getName();
            String string3 = ServerSamplerSource.getProfile().getId() != null ? ServerSamplerSource.getProfile().getId().toString() : string2.toLowerCase(Locale.ROOT);
            this.cacheServerNameReplacement(string2, string3);
        }
        if (NameProtect.minecraftClient.world == null) {
            return;
        }
        for (AbstractClientPlayerEntity player : NameProtect.minecraftClient.world.getPlayers()) {
            this.cacheServerNameReplacement(player.getName().getString(), player.getUuidAsString());
        }
    }

    private void cacheServerNameReplacement(String string, String string3) {
        if (!this.isValidServerName(string) || string.equalsIgnoreCase(minecraftClient.getSession().getUsername())) {
            return;
        }
        this.replacementNames.computeIfAbsent(string, string2 -> {
            this.replacementPatternDirty = true;
            return this.generateReplacementName(string3);
        });
    }

    private String replaceCachedChatNames(String string) {
        Pattern pattern = this.getCachedReplacementPattern();
        if (pattern == null) {
            return string;
        }
        Matcher matcher = pattern.matcher(string);
        if (!matcher.find()) {
            return string;
        }
        StringBuffer stringBuffer = new StringBuffer();
        do {
            String string2;
            String string3;
            matcher.appendReplacement(stringBuffer, Matcher.quoteReplacement((string3 = this.replacementNames.get(string2 = matcher.group())) != null ? string3 : string2));
        } while (matcher.find());
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    private Pattern getCachedReplacementPattern() {
        if (this.replacementPatternDirty) {
            this.replacementPattern = this.buildReplacementPattern();
            this.replacementPatternDirty = false;
        }
        return this.replacementPattern;
    }

    private Pattern buildReplacementPattern() {
        if (this.replacementNames.isEmpty()) {
            return null;
        }
        ArrayList<String> arrayList = new ArrayList<String>(this.replacementNames.keySet());
        arrayList.sort(Comparator.comparingInt(String::length).reversed());
        StringBuilder stringBuilder = new StringBuilder("(?<![A-Za-z0-9_])(?:");
        for (int i = 0; i < arrayList.size(); ++i) {
            if (i > 0) {
                stringBuilder.append('|');
            }
            stringBuilder.append(Pattern.quote(arrayList.get(i)));
        }
        stringBuilder.append(")(?![A-Za-z0-9_])");
        return Pattern.compile(stringBuilder.toString());
    }

    private String generateReplacementName(String string) {
        int n = Math.floorMod(string.hashCode(), 900) + 100;
        HashSet<String> hashSet = new HashSet<String>(this.replacementNames.values());
        for (int i = 0; i < 900; ++i) {
            String string2 = "P" + n;
            if (!hashSet.contains(string2)) {
                return string2;
            }
            if (++n <= 999) continue;
            n = 100;
        }
        return "P" + (this.replacementNames.size() + 1000);
    }

    private String replaceServerName(String string2) {
        if (!this.isValidServerName(string2) || string2.equalsIgnoreCase(minecraftClient.getSession().getUsername())) {
            return string2;
        }
        return this.replacementNames.computeIfAbsent(string2, string -> {
            this.replacementPatternDirty = true;
            return this.generateReplacementName(string.toLowerCase(Locale.ROOT));
        });
    }

    private String replaceServerContext(String string) {
        string = this.replaceAnarchyServerNumbers(string);
        string = this.replaceServerAddresses(string);
        string = this.replaceServerCommands(string);
        string = this.replaceModeNames(string);
        return string;
    }

    private String replaceAnarchyServerNumbers(String string) {
        string = this.replaceAnarchyMatches(string, anarchyPattern);
        string = this.replaceAnarchyMatches(string, classicPattern);
        int n = ServerDetector.getDetectedServerMode();
        if (n > 0) {
            int n2 = this.getReplacementNumber(n);
            string = this.replaceServerNumber(string, "#", n, Integer.toString(n2));
            string = this.replaceServerNumber(string, "\u2116", n, Integer.toString(n2));
        }
        return string;
    }

    private String replaceServerAddresses(String string) {
        string = this.replaceRegexMatches(string, lobbyPattern, "server.local");
        string = this.replaceRegexMatches(string, serverPattern, "server.local");
        string = this.replaceRegexMatches(string, playerPattern, "server.local");
        string = this.replaceRegexMatches(string, coordinatePattern, "Server");
        return string;
    }

    private String replaceServerCommands(String string) {
        string = this.replaceRegexMatches(string, numberPattern, "/server");
        string = this.replaceRegexMatches(string, chatPattern, "server");
        return string;
    }

    private String replaceModeNames(String string) {
        string = this.replaceRegexMatches(string, namePattern, "PvP");
        string = this.replaceRegexMatches(string, addressPattern, "PvP");
        string = this.replaceRegexMatches(string, rankPattern, "\u0420\u0435\u0436\u0438\u043c");
        string = this.replaceRegexMatches(string, locationPattern, "Mode");
        return string;
    }

    private String replaceAnarchyMatches(String string, Pattern pattern) {
        Matcher matcher = pattern.matcher(string);
        StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()) {
            int n = this.parseProtectedNameNumber(matcher.group(2));
            String string2 = n > 0 ? matcher.group(1) + this.getReplacementNumber(n) : matcher.group(0);
            matcher.appendReplacement(stringBuffer, Matcher.quoteReplacement(string2));
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    private String replaceServerNumber(String string, String string2, int n, String string3) {
        if (!string.contains(string2 + n)) {
            return string;
        }
        return Pattern.compile(Pattern.quote(string2) + n + "(?!\\d)").matcher(string).replaceAll(Matcher.quoteReplacement(string2 + string3));
    }

    private int getReplacementNumber(int n) {
        return n + 5;
    }

    private String replaceExactName(String string) {
        String string2 = minecraftClient.getSession().getUsername();
        if (string.equals(string2)) {
            return this.getConfiguredReplacement(this.fakeNameSetting.getValue(), "Player");
        }
        if (EntityUtils.isClientWorldReady() && string.equals(NameProtect.minecraftClient.player.getDisplayName().getString())) {
            return this.getConfiguredReplacement(this.fakeNameSetting.getValue(), "Player");
        }
        if (this.hideFriendsSetting.isEnabled()) {
            FriendListManager friendListManager = RockstarClient.create().getFriendListManager();
            for (String string3 : friendListManager.getFriends()) {
                if (string3 == null || string3.isEmpty() || !string3.equals(string)) continue;
                return this.getConfiguredReplacement(this.friendFakeNameSetting.getValue(), "Friend");
            }
        }
        return string;
    }

    private String replaceRegexMatches(String string, Pattern pattern, String string2) {
        return pattern.matcher(string).replaceAll(Matcher.quoteReplacement(string2));
    }

    private String replaceDelimitedName(String string, String string2, String string3) {
        if (string2 == null || string2.isEmpty() || string3 == null || string3.isEmpty() || !string.contains(string2)) {
            return string;
        }
        return Pattern.compile("(?<![A-Za-z0-9_])" + Pattern.quote(string2) + "(?![A-Za-z0-9_])").matcher(string).replaceAll(Matcher.quoteReplacement(string3));
    }

    private int parseProtectedNameNumber(String string) {
        try {
            return Integer.parseInt(string);
        }
        catch (NumberFormatException numberFormatException) {
            return -1;
        }
    }

    private boolean isValidServerName(String string) {
        if (string == null || string.length() < 3 || string.length() > 16) {
            return false;
        }
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_') continue;
            return false;
        }
        return true;
    }

    private String getConfiguredReplacement(String string, String string2) {
        return string == null || string.isEmpty() ? string2 : string;
    }

    @Generated
    public StringSetting getFakeNameSetting() {
        return this.fakeNameSetting;
    }

    @Generated
    public BooleanSetting getHideFriendsSetting() {
        return this.hideFriendsSetting;
    }

    @Generated
    public StringSetting getFriendFakeNameSetting() {
        return this.friendFakeNameSetting;
    }

    @Generated
    public BooleanSetting getStreamerModeSetting() {
        return this.streamerModeSetting;
    }

    @Generated
    public BooleanSetting getCaptureBypassSetting() {
        return this.captureBypassSetting;
    }

    @Generated
    public Set<String> getProtectedNames() {
        return this.capturedNames;
    }

    @Generated
    public Map<String, String> getReplacementNames() {
        return this.replacementNames;
    }

    @Generated
    public String getLastServerAddress() {
        return this.lastServerAddress;
    }

    /** NOT an override: the original {@code rockstar/ilIlil/IIIIiIiII} declares no keybind
     *  getter, so the inherited {@link Module#getSavedKeyBind()} must stay visible. */
    @Generated
    public int getLastProcessedTick() {
        return this.lastProcessedTick;
    }

    @Generated
    public Pattern getReplacementPattern() {
        return this.replacementPattern;
    }

    @Generated
    public boolean isReplacementPatternDirty() {
        return this.replacementPatternDirty;
    }
}
