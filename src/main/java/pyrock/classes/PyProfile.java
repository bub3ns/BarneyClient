/*
 * Decompiled with CFR 0.152.
 */
package pyrock.classes;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.Module;
import net.minecraft.util.Identifier;
import ua.mintantileak.profile.Profile;
import ua.mintantileak.profile.Role;

/**
 * ORIGINAL: {@code pyrock/classes/PyProfile} (not obfuscated).
 *
 * <p>Everything that reads {@link Profile} / {@link Role} and the two {@code Module} statics is a
 * literal port. Three things the original does are DROPPED because they live in the excluded
 * {@code globals} package:</p>
 * <ul>
 *   <li>{@code forever()} also returns true when {@code ProfileSync.subscriptionEnd() > 0} and is
 *       more than {@link #PERMANENT_MS} away; without the cloud value that branch is unreachable.</li>
 *   <li>{@code subscriptionEnd()} short-circuits on {@code ProfileSync.subscriptionEnd()} before
 *       falling back to parsing {@link #DATE}; only the parse path survives.</li>
 *   <li>{@code avatar()} / {@code avatarOf(name)} call {@code Information.getSelfAvatar()} /
 *       {@code Information.getAvatar(name)} and only fall back to the bundled texture on throw;
 *       here the fallback is all that is left. The fallback identifier itself is 1:1
 *       ({@code Ii.I("rocknet/avatar.png")}).</li>
 * </ul>
 */
public class PyProfile {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ROOT);
    private static final String FOREVER = "Навсегда";
    /** ORIGINAL constant, only reachable through the dropped {@code ProfileSync} branch. */
    private static final long PERMANENT_MS = 94608000000L;

    public String username() {
        String string = Profile.getUsername();
        return string == null ? "" : string;
    }

    public int uid() {
        return Profile.getUid();
    }

    public String role() {
        Role role = Profile.getRole();
        return role == null ? "default" : role.name().toLowerCase(Locale.ROOT);
    }

    public boolean hasRole(String string) {
        return string != null && this.role().equalsIgnoreCase(string.trim());
    }

    public boolean staff() {
        Role role = Profile.getRole();
        return role == Role.ADMIN || role == Role.MODERATOR || role == Role.OWNER;
    }

    /** ORIGINAL: {@code iiIIiIii.iI()} = {@link Module#isAdminOrOwner()}. */
    public boolean admin() {
        return Module.isAdminOrOwner();
    }

    /** ORIGINAL: {@code iiIIiIii.ii()} = {@link Module#isLoggedIn()}. */
    public boolean loaded() {
        return Module.isLoggedIn();
    }

    public String subscription() {
        String string = Profile.getSubscriptionEndDate();
        return string == null ? "" : string;
    }

    public boolean forever() {
        return FOREVER.equalsIgnoreCase(this.subscription());
    }

    public long subscriptionEnd() {
        String string = this.subscription();
        if (string.isBlank() || FOREVER.equalsIgnoreCase(string)) {
            return 0L;
        }
        try {
            return LocalDate.parse(string, DATE).plusDays(1L).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        catch (Exception exception) {
            return 0L;
        }
    }

    public long subscriptionLeft() {
        long l = this.subscriptionEnd();
        if (l <= 0L) {
            return 0L;
        }
        return Math.max(0L, l - System.currentTimeMillis());
    }

    public int daysLeft() {
        long l = this.subscriptionLeft();
        if (this.subscriptionEnd() <= 0L) {
            return -1;
        }
        return (int)Duration.ofMillis(l).toDays();
    }

    public boolean expired() {
        long l = this.subscriptionEnd();
        return !this.forever() && l > 0L && l < System.currentTimeMillis();
    }

    public String avatarUrl() {
        String string = Profile.getAvatarUrl();
        return string == null ? "" : string;
    }

    public Identifier avatar() {
        return PyProfile.fallbackAvatar();
    }

    public Identifier avatarOf(String string) {
        if (string == null || string.isBlank()) {
            return PyProfile.fallbackAvatar();
        }
        return PyProfile.fallbackAvatar();
    }

    /** ORIGINAL: {@code Ii.I("rocknet/avatar.png")}. */
    private static Identifier fallbackAvatar() {
        return RockstarClient.resourceId("rocknet/avatar.png");
    }

    public String clientName() {
        return "Barney";
    }

    public String clientVersion() {
        return "2.1";
    }
}
