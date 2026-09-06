package moscow.rockstar.ui.localization;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Runtime translation overrides supplied by local scripts. */
public final class TranslationOverrideRegistry {
    private static final Map<Language, Map<String, TranslationOverride>> OVERRIDES = new ConcurrentHashMap<>();

    private TranslationOverrideRegistry() {
    }

    public static void register(Object owner, Language language, String key, String value) {
        if (language == null || key == null || key.isBlank() || value == null) {
            return;
        }
        OVERRIDES.computeIfAbsent(language, ignored -> new ConcurrentHashMap<>())
                .put(key, new TranslationOverride(owner, value));
        Localization.clearFormattedTranslations();
    }

    public static String lookup(String key) {
        if (key == null || OVERRIDES.isEmpty()) {
            return null;
        }
        String value = lookup(Localization.getLanguage(), key);
        if (value != null) {
            return value;
        }
        value = lookup(Language.EN_US, key);
        if (value != null) {
            return value;
        }
        /*
        value = lookup(Language.RU_RU, key);
        if (value != null) {
            return value;
        }
        */
        for (Map<String, TranslationOverride> languageOverrides : OVERRIDES.values()) {
            TranslationOverride override = languageOverrides.get(key);
            if (override != null) {
                return override.value;
            }
        }
        return null;
    }

    public static boolean contains(String key) {
        return lookup(key) != null;
    }

    public static void clearOwner(Object owner) {
        boolean changed = false;
        for (Map<String, TranslationOverride> languageOverrides : OVERRIDES.values()) {
            changed |= languageOverrides.entrySet().removeIf(entry -> entry.getValue().owner == owner);
        }
        if (changed) {
            Localization.clearFormattedTranslations();
        }
    }

    public static boolean isEmpty() {
        return OVERRIDES.isEmpty();
    }

    public static Language parseLanguage(String code) {
        return Language.fromLocaleCode(code);
    }

    private static String lookup(Language language, String key) {
        if (language == null) {
            return null;
        }
        Map<String, TranslationOverride> languageOverrides = OVERRIDES.get(language);
        TranslationOverride override = languageOverrides == null ? null : languageOverrides.get(key);
        return override == null ? null : override.value;
    }

    private static final class TranslationOverride {
        private final Object owner;
        private final String value;

        private TranslationOverride(Object owner, String value) {
            this.owner = owner;
            this.value = value;
        }
    }
}
