package moscow.rockstar.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.EnumSet;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.localization.Localization;
import org.jetbrains.annotations.NotNull;

/**
 * A duration setting split into hour/minute/second slots (original rockstar/ilIlil/IiIIIIIii).
 *
 * The stored value is always re-normalised through the clamp hook and the currently
 * enabled display units: a disabled unit contributes zero, an enabled one wraps
 * modulo its own maximum (24 hours / 60 minutes / 60 seconds by default).
 */
public class TimeSetting extends AbstractSetting {
    final EnumSet<Unit> displayUnits = EnumSet.allOf(Unit.class);
    private int maximumHours = 24;
    private int maximumMinutes = 60;
    private int totalSeconds;
    private ValueChangeListener<Integer> clamp = value -> value;

    public TimeSetting(@NotNull SettingOwner settingOwner, String string, String string2, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public TimeSetting(@NotNull SettingOwner settingOwner, String string, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public TimeSetting(@NotNull SettingOwner settingOwner, String string, String string2) {
        super(settingOwner, string);
    }

    public TimeSetting(@NotNull SettingOwner settingOwner, String string) {
        super(settingOwner, string);
    }

    /** Original I([Lrockstar/ilIlil/IiIIIIIii$I;) */
    public TimeSetting setDisplayUnits(Unit ... units) {
        this.displayUnits.clear();
        if (units != null) {
            for (Unit unit : units) {
                if (unit == null) continue;
                this.displayUnits.add(unit);
            }
        }
        if (this.displayUnits.isEmpty()) {
            this.displayUnits.add(Unit.SECONDS);
        }
        this.setSeconds(this.totalSeconds);
        return this;
    }

    /** Original I(Z) */
    public TimeSetting setHoursEnabled(boolean bl) {
        return this.setUnitEnabled(Unit.HOURS, bl);
    }

    /** Original i(Z) */
    public TimeSetting setMinutesEnabled(boolean bl) {
        return this.setUnitEnabled(Unit.MINUTES, bl);
    }

    /** Original II(Z) */
    public TimeSetting setSecondsEnabled(boolean bl) {
        return this.setUnitEnabled(Unit.SECONDS, bl);
    }

    /** Original I(Lrockstar/ilIlil/IiIIIIIii$I;Z) - the last enabled unit can never be removed. */
    public TimeSetting setUnitEnabled(Unit unit, boolean bl) {
        if (bl) {
            this.displayUnits.add(unit);
        } else if (this.displayUnits.size() > 1) {
            this.displayUnits.remove(unit);
        }
        this.setSeconds(this.totalSeconds);
        return this;
    }

    /** Original I(I)Lrockstar/ilIlil/IiIIIIIii; */
    public TimeSetting setMaximumHours(int n) {
        this.maximumHours = Math.max(1, n);
        this.setSeconds(this.totalSeconds);
        return this;
    }

    /** Original i(I)Lrockstar/ilIlil/IiIIIIIii; */
    public TimeSetting setMaximumMinutes(int n) {
        this.maximumMinutes = Math.max(1, Math.min(60, n));
        this.setSeconds(this.totalSeconds);
        return this;
    }

    /** Original I(Lrockstar/ilIlil/IIiiIiiii;) - installs the pre-normalisation clamp. */
    public TimeSetting setClamp(ValueChangeListener<Integer> valueChangeListener) {
        if (valueChangeListener != null) {
            this.clamp = valueChangeListener;
        }
        return this;
    }

    /** Original I(I)V, exposed fluently as II(I) - clamp, normalise, then notify on change. */
    public TimeSetting setSeconds(int n) {
        int normalized = this.normalize(this.clamp.changed(Math.max(0, n)));
        if (this.totalSeconds == normalized) {
            return this;
        }
        this.notifyChange();
        this.totalSeconds = normalized;
        return this;
    }

    /** Original I(Lrockstar/ilIlil/IiIIIIIii$I;)Z */
    public boolean hasUnit(Unit unit) {
        return this.displayUnits.contains(unit);
    }

    /** Original I()I */
    public int getHours() {
        return this.hasUnit(Unit.HOURS) ? this.totalSeconds / 3600 % this.maximumHours : 0;
    }

    /** Original i()I */
    public int getMinutes() {
        return this.hasUnit(Unit.MINUTES) ? this.totalSeconds / 60 % this.maximumMinutes : 0;
    }

    /** Original II()I */
    public int getSeconds() {
        return this.hasUnit(Unit.SECONDS) ? this.totalSeconds % 60 : 0;
    }

    /** Original i(I)V */
    public void setHours(int n) {
        this.setSeconds(Math.floorMod(n, this.maximumHours) * 3600 + this.getMinutes() * 60 + this.getSeconds());
    }

    /** Original II(I)V */
    public void setMinutes(int n) {
        this.setSeconds(this.getHours() * 3600 + Math.floorMod(n, this.maximumMinutes) * 60 + this.getSeconds());
    }

    /** Original Ii(I)V */
    public void setSecondsComponent(int n) {
        this.setSeconds(this.getHours() * 3600 + this.getMinutes() * 60 + Math.floorMod(n, 60));
    }

    /** Original I()J */
    public long getMilliseconds() {
        return (long)this.totalSeconds * 1000L;
    }

    /** Original Ii()I */
    public int getTicks() {
        return this.totalSeconds * 20;
    }

    /** Original II()Ljava/lang/String; - the enabled units joined with ':', zero padded after the first. */
    public String getFormattedValue() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Unit unit : this.displayUnits) {
            int n = switch (unit) {
                case HOURS -> this.getHours();
                case MINUTES -> this.getMinutes();
                case SECONDS -> this.getSeconds();
            };
            if (stringBuilder.isEmpty()) {
                stringBuilder.append(n);
                continue;
            }
            stringBuilder.append(':').append(n < 10 ? "0" + n : String.valueOf(n));
        }
        return stringBuilder.toString();
    }

    /** Original I(I)I */
    private int normalize(int n) {
        n = Math.max(0, n);
        int hours = this.hasUnit(Unit.HOURS) ? n / 3600 % this.maximumHours : 0;
        int minutes = this.hasUnit(Unit.MINUTES) ? n / 60 % this.maximumMinutes : 0;
        int seconds = this.hasUnit(Unit.SECONDS) ? n % 60 : 0;
        return hours * 3600 + minutes * 60 + seconds;
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(this.totalSeconds);
    }

    @Override
    public void deserialize(JsonElement jsonElement) {
        double d;
        if (jsonElement != null && jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()
                && Double.isFinite(d = jsonElement.getAsDouble()) && d >= 0.0 && d <= 2.147483647E9) {
            this.setSeconds((int)d);
        }
    }

    @Override
    public boolean isValidJson(JsonElement jsonElement) {
        if (jsonElement == null || !jsonElement.isJsonPrimitive() || !jsonElement.getAsJsonPrimitive().isNumber()) {
            return false;
        }
        double d = jsonElement.getAsDouble();
        return Double.isFinite(d) && d >= 0.0 && d <= 2.147483647E9;
    }

    /**
     * ORIGINAL: I()Lrockstar/ilIlil/iii; builds a label row plus a column of scroll
     * wheels (rockstar/ilIlil/IIiIi), one per enabled unit.
     *
     * OMITTED: the wheel widget IIiIi has no remapped counterpart, so no editor is
     * produced here. Every non-UI behaviour of the setting is ported above.
     */
    @Override
    public Component buildComponent() {
        return new Component();
    }

    public EnumSet<Unit> getDisplayUnits() {
        return this.displayUnits;
    }

    public int getMaximumHours() {
        return this.maximumHours;
    }

    public int getMaximumMinutes() {
        return this.maximumMinutes;
    }

    public int getTotalSeconds() {
        return this.totalSeconds;
    }

    public ValueChangeListener<Integer> getClamp() {
        return this.clamp;
    }

    /** Original pyrock/classes/settings/PyTimeSetting#unit(String), hoisted here by the remap. */
    public static Unit parseUnit(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "h", "hour", "hours", "час", "часы" -> Unit.HOURS;
            case "s", "sec", "second", "seconds", "сек", "секунды" -> Unit.SECONDS;
            default -> Unit.MINUTES;
        };
    }

    public enum Unit {
        HOURS(3600, "time.unit.hours"),
        MINUTES(60, "time.unit.minutes"),
        SECONDS(1, "time.unit.seconds");

        private final int seconds;
        private final String localizationKey;

        Unit(int seconds, String localizationKey) {
            this.seconds = seconds;
            this.localizationKey = localizationKey;
        }

        /** Original I()I */
        public int getSeconds() {
            return this.seconds;
        }

        /** Original I()Ljava/lang/String; - the TRANSLATED unit name, not the raw key. */
        public String getDisplayName() {
            return Localization.translate(this.localizationKey);
        }
    }
}
