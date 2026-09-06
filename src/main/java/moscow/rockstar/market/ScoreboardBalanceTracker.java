/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ScoreboardObjective
 *  net.minecraft.Team
 *  net.minecraft.Scoreboard
 *  net.minecraft.ScoreboardDisplaySlot
 *  net.minecraft.ScoreboardEntry
 */
package moscow.rockstar.market;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import moscow.rockstar.core.ClientAccess;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;

public class ScoreboardBalanceTracker
implements ClientAccess {
    private static final Pattern BALANCE_AMOUNT_PATTERN = Pattern.compile("(\\d[\\d.,\\u00A0]*\\d|\\d)\\s*(kkkk|kkk|kk|k|\u043a\u043a\u043a|\u043a\u043a|\u043a|m|\u043c)?", 66);
    private long cachedBalance = -1L;

    public long getCachedBalance() {
        long l = this.readScoreboardBalance();
        if (l >= 0L) {
            this.cachedBalance = l;
        }
        return this.cachedBalance;
    }

    public boolean isBalanceCacheValid(long l) {
        long l2 = this.getCachedBalance();
        return l2 < 0L || l2 >= l;
    }

    public void clearCachedBalance() {
        this.cachedBalance = -1L;
    }

    private long readScoreboardBalance() {
        if (ScoreboardBalanceTracker.minecraftClient.world == null) {
            return -1L;
        }
        Scoreboard VehicleMoveS2CPacket = ScoreboardBalanceTracker.minecraftClient.world.getScoreboard();
        ScoreboardObjective class_2662 = VehicleMoveS2CPacket.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (class_2662 == null) {
            return -1L;
        }
        long l = -1L;
        long l2 = -1L;
        for (ScoreboardEntry class_90112 : VehicleMoveS2CPacket.getScoreboardEntries(class_2662)) {
            long l3;
            String string = class_90112.owner();
            String displayText = class_90112.display() == null ? string : class_90112.display().getString();
            Team EmptyBlockView = VehicleMoveS2CPacket.getScoreHolderTeam(string);
            if (EmptyBlockView != null) {
                displayText = EmptyBlockView.getPrefix().getString() + displayText + EmptyBlockView.getSuffix().getString();
            }
            if ((l3 = this.parseBalanceAmount(displayText)) < 0L) continue;
            l2 = Math.max(l2, l3);
            String string2 = displayText.toLowerCase(Locale.ROOT);
            if (!string2.contains("\u0431\u0430\u043b\u0430\u043d\u0441") && !string2.contains("\u043c\u043e\u043d\u0435\u0442") && !string2.contains("\u0434\u0435\u043d\u044c\u0433") && !string2.contains("balance") && !string2.contains("money") && !string2.contains("\u043a\u043e\u0438\u043d") && !string2.contains("$") && !string2.contains("\u26c1") && !string2.contains("bank")) continue;
            l = Math.max(l, l3);
        }
        return l >= 0L ? l : l2;
    }

    private long parseBalanceAmount(String string) {
        Matcher matcher = BALANCE_AMOUNT_PATTERN.matcher(string);
        long l = -1L;
        while (matcher.find()) {
            long l2;
            String string2 = matcher.group(1).replaceAll("[^\\d]", "");
            if (string2.isEmpty()) continue;
            try {
                l2 = Long.parseLong(string2);
            }
            catch (NumberFormatException numberFormatException) {
                continue;
            }
            String string3 = matcher.group(2);
            if (string3 != null) {
                l2 *= (switch (string3.toLowerCase(Locale.ROOT)) {
                    case "k", "\u043a" -> 1000L;
                    case "kk", "\u043a\u043a", "m", "\u043c" -> 1000000L;
                    case "kkk", "\u043a\u043a\u043a" -> 1000000000L;
                    case "kkkk" -> 1000000000000L;
                    default -> 1L;
                });
            }
            l = Math.max(l, l2);
        }
        return l;
    }
}
