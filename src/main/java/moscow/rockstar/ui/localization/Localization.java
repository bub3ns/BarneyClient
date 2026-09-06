/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  lombok.Generated
 */
package moscow.rockstar.ui.localization;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.ui.localization.Language;
import pyrock.events.client.LanguageChangedEvent;
import ua.mintantileak.spk.Compile;

public final class Localization {
    private static final Language defaultLanguage;
    private static Language currentLanguage;
    private static final Map<String, String> translations;
    private static boolean loaded;
    private static final Map<String, String> formattedTranslations;

    public static void loadLanguageFile() {
        String string = "/assets/" + RockstarClient.RESOURCE_NAMESPACE + "/lang/" + currentLanguage.getLocaleCode() + ".lang";
        try {
            String string2;
            try (InputStream inputStream = Localization.class.getResourceAsStream(string);){
                if (inputStream == null) {
                    throw new RuntimeException("Language file not found: " + string);
                }
                string2 = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
            // Keep the original language-resource lookup path. The project resources
            // are offline-decrypted UTF-8 files; the original RSV1 decoder depended
            // on the online gate and is intentionally not part of this safe build.
            if (string2.startsWith("RSV1:")) {
                throw new IllegalStateException("Encrypted language resource is not supported: " + string);
            }
            string2 = string2.trim();
            loaded = false;
            translations.clear();
            int n = 0;
            for (String string3 : string2.split("\r?\n")) {
                ++n;
                String string4 = Localization.stripComment(string3).trim();
                if (string4.isEmpty()) continue;
                Localization.parseTranslationLine(string4, n, string);
            }
            loaded = true;
        }
        catch (IOException iOException) {
            throw new RuntimeException("Failed to load translations for language: " + currentLanguage.getLocaleCode(), iOException);
        }
    }

    public static void setLanguage(@Nonnull Language language) {
        currentLanguage = language;
        formattedTranslations.clear();
        Localization.loadLanguageFile();
        if (RockstarClient.create() != null && RockstarClient.create().getEventBus() != null) {
            RockstarClient.create().getEventBus().post(new LanguageChangedEvent(language.getLocaleCode()));
        }
    }

    public static void clearFormattedTranslations() {
        formattedTranslations.clear();
    }

    public static String translate(String string) {
        Localization.ensureLoaded();
        return formattedTranslations.computeIfAbsent(string, Localization::lookupTranslation);
    }

    public static String translateFormatted(String string, Object ... objectArray) {
        Localization.ensureLoaded();
        String string2 = formattedTranslations.computeIfAbsent(string, Localization::lookupTranslation);
        return String.format(string2, objectArray);
    }

    public static String translateOrBlank(String string) {
        Localization.ensureLoaded();
        String string2 = translations.get(string);
        if (string2 != null) {
            return string2;
        }
        String string3 = TranslationOverrideRegistry.lookup(string);
        return string3 != null ? string3 : " ";
    }

    private static String lookupTranslation(String string) {
        String string2 = translations.get(string);
        if (string2 != null) {
            return string2;
        }
        String string3 = TranslationOverrideRegistry.lookup(string);
        return string3 != null ? string3 : string;
    }

    @Compile
    private static void parseTranslationLine(String string, int n, String string2) {
        int n2 = string.indexOf(61);
        if (n2 == -1) {
            RockstarClient.LOGGER.warn("Warning: Invalid line format at line {} in {}: {}", new Object[]{n, string2, string});
            return;
        }
        String string3 = string.substring(0, n2).trim();
        String string4 = string.substring(n2 + 1).trim();
        if (string3.isEmpty()) {
            RockstarClient.LOGGER.warn("Warning: Empty key at line {} in {}", (Object)n, (Object)string2);
            return;
        }
        translations.put(string3, string4);
    }

    private static String stripComment(String string) {
        int n = string.indexOf("#");
        if (n != -1) {
            return string.substring(0, n);
        }
        return string;
    }

    private static void ensureLoaded() {
        if (!loaded) {
            Localization.loadLanguageFile();
        }
    }

    @Generated
    private Localization() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Generated
    public static Language getLanguage() {
        return currentLanguage;
    }

    static {
        currentLanguage = defaultLanguage = Language.EN_US;
        // currentLanguage = defaultLanguage = Language.RU_RU;
        translations = new HashMap<String, String>();
        formattedTranslations = new HashMap<String, String>();
    }
}
