/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.ui.localization;

import lombok.Generated;
import java.util.Locale;

public enum Language {
    EN_US("en_us");
    // RU_RU("ru_ru"),
    // UK_UA("uk_ua"),
    // PL_PL("pl_pl");
    private final String localeCode;

    private Language(String string2) {
        this.localeCode = string2;
    }

    @Generated
    public String getLocaleCode() {
        return this.localeCode;
    }

    /** Resolve a locale code such as {@code en_us} or {@code en-US}. */
    public static Language fromLocaleCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        String normalized = code.trim().toLowerCase(Locale.ROOT).replace('-', '_');
        for (Language language : values()) {
            if (language.localeCode.equalsIgnoreCase(normalized)
                    || language.name().equalsIgnoreCase(normalized)) {
                return language;
            }
        }
        for (Language language : values()) {
            if (language.localeCode.startsWith(normalized + "_")) {
                return language;
            }
        }
        return null;
    }
}
