package moscow.rockstar.render.esp;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.modules.visuals.esp.targeting.ItemTargetType;
import moscow.rockstar.modules.visuals.esp.targeting.PlayerTargetGroup;
import moscow.rockstar.modules.visuals.esp.targeting.TargetGroup;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.IntegerSetting;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.settings.SettingGroup;

/**
 * Scope-aware setting owner used by the ESP renderers.
 *
 * <p>The original client creates an independent setting set for every target
 * scope.  In particular, player subtypes and item subtypes are not aliases
 * for one global toggle.  Keeping that distinction is important both for the
 * ESP screen and for the render predicates.</p>
 */
public abstract class TargetRenderModule extends SettingGroup implements ClientAccess {
    private final String name;
    private final LinkedHashSet<TargetGroup> supportedTargetGroups;
    private final LinkedHashSet<PlayerTargetGroup> supportedPlayerGroups;
    private final LinkedHashSet<ItemTargetType> supportedItemTypes;

    private final Map<TargetGroup, BooleanSetting> targetActivationSettings = new LinkedHashMap<>();
    private final Map<PlayerTargetGroup, BooleanSetting> playerActivationSettings = new LinkedHashMap<>();
    private final Map<ItemTargetType, BooleanSetting> itemActivationSettings = new LinkedHashMap<>();
    private final Map<TargetGroup, IntegerSetting> targetBindingSettings = new LinkedHashMap<>();
    private final Map<PlayerTargetGroup, IntegerSetting> playerBindingSettings = new LinkedHashMap<>();
    private final Map<ItemTargetType, IntegerSetting> itemBindingSettings = new LinkedHashMap<>();

    private final Map<Setting, Set<TargetGroup>> settingDependencies = new IdentityHashMap<>();
    private final Map<String, Map<TargetGroup, Setting>> perTargetSettings = new LinkedHashMap<>();
    private final Map<String, Map<PlayerTargetGroup, Setting>> perPlayerSettings = new LinkedHashMap<>();
    private final Map<String, Map<ItemTargetType, Setting>> perItemSettings = new LinkedHashMap<>();

    /** Compatibility view for callers that need to inspect a setting by scope. */
    private final Map<String, Map<Object, Setting>> scopedSettingsByKey = new LinkedHashMap<>();
    private BooleanSetting firstEnabledSetting;

    protected TargetRenderModule(String name, TargetGroup[] targetGroups) {
        this(name, new ItemTargetType[0], targetGroups);
    }

    protected TargetRenderModule(String name, ItemTargetType[] itemTypes, TargetGroup[] targetGroups) {
        this.name = name;
        this.supportedTargetGroups = new LinkedHashSet<>();
        if (targetGroups == null || targetGroups.length == 0) {
            Collections.addAll(this.supportedTargetGroups, TargetGroup.values());
        } else {
            Collections.addAll(this.supportedTargetGroups, targetGroups);
        }

        this.supportedPlayerGroups = new LinkedHashSet<>();
        if (this.supportedTargetGroups.contains(TargetGroup.PLAYERS)) {
            Collections.addAll(this.supportedPlayerGroups, PlayerTargetGroup.values());
        }

        this.supportedItemTypes = new LinkedHashSet<>();
        if (this.supportedTargetGroups.contains(TargetGroup.ITEMS)
                && itemTypes != null && itemTypes.length > 0) {
            Collections.addAll(this.supportedItemTypes, itemTypes);
        }
    }

    public final String getName() {
        return this.name;
    }

    public final TargetGroup[] getSupportedTargetGroups() {
        return this.supportedTargetGroups.toArray(TargetGroup[]::new);
    }

    public final ItemTargetType[] getSupportedItemTypes() {
        return this.supportedItemTypes.toArray(ItemTargetType[]::new);
    }

    /** Returns the first scope toggle, matching the setting returned by createSetting(String). */
    public BooleanSetting getEnabledSetting() {
        return this.firstEnabledSetting;
    }

    public BooleanSetting getEnabledSetting(TargetGroup targetGroup) {
        return targetGroup == null ? null : this.targetActivationSettings.get(targetGroup);
    }

    public BooleanSetting getEnabledSetting(PlayerTargetGroup playerTargetGroup) {
        return playerTargetGroup == null ? null : this.playerActivationSettings.get(playerTargetGroup);
    }

    public BooleanSetting getEnabledSetting(ItemTargetType itemTargetType) {
        return itemTargetType == null ? null : this.itemActivationSettings.get(itemTargetType);
    }

    /** Registers this renderer's event-listener fields with the client event bus. */
    public void registerListeners() {
        if (moscow.rockstar.core.RockstarClient.create().getEventBus() != null) {
            moscow.rockstar.core.RockstarClient.create().getEventBus().registerListeners(this);
        }
    }

    /**
     * Draws this overlay into the ESP settings screen's entity preview.
     *
     * <p>The original base class declares this as an empty virtual hook and the
     * settings screen dispatches through it for every registered overlay.</p>
     */
    public void renderPreviewOverlay(moscow.rockstar.render.core.RockstarDrawContext drawContext,
                                     net.minecraft.entity.Entity entity, float x, float y,
                                     TargetGroup targetGroup, PlayerTargetGroup playerTargetGroup) {
    }

    /**
     * Applies a key/button binding to every matching target scope.
     *
     * <p>The original target-render base class checks the three binding maps
     * independently, so one input can legitimately toggle more than one
     * scope.  Keeping that behavior is important for the ESP overlays: the
     * registry forwards the raw input to each overlay, rather than toggling a
     * single global module flag.</p>
     */
    public boolean handleInput(int inputCode) {
        boolean toggled = false;
        for (Map.Entry<TargetGroup, IntegerSetting> entry : this.targetBindingSettings.entrySet()) {
            BooleanSetting setting = this.targetActivationSettings.get(entry.getKey());
            if (setting == null || !entry.getValue().isIntValid(inputCode)) {
                continue;
            }
            setting.toggle();
            toggled = true;
        }
        for (Map.Entry<PlayerTargetGroup, IntegerSetting> entry : this.playerBindingSettings.entrySet()) {
            BooleanSetting setting = this.playerActivationSettings.get(entry.getKey());
            if (setting == null || !entry.getValue().isIntValid(inputCode)) {
                continue;
            }
            setting.toggle();
            toggled = true;
        }
        for (Map.Entry<ItemTargetType, IntegerSetting> entry : this.itemBindingSettings.entrySet()) {
            BooleanSetting setting = this.itemActivationSettings.get(entry.getKey());
            if (setting == null || !entry.getValue().isIntValid(inputCode)) {
                continue;
            }
            setting.toggle();
            toggled = true;
        }
        return toggled;
    }

    public boolean supportsTargetGroup(TargetGroup targetGroup) {
        return targetGroup != null && this.supportedTargetGroups.contains(targetGroup);
    }

    public boolean supportsTargetGroup(PlayerTargetGroup playerTargetGroup) {
        return playerTargetGroup != null && this.supportedPlayerGroups.contains(playerTargetGroup);
    }

    public boolean supportsItemType(ItemTargetType itemTargetType) {
        return itemTargetType != null && this.supportedItemTypes.contains(itemTargetType);
    }

    /** Scope support only; this is intentionally separate from isValid2. */
    public boolean isValid(TargetGroup targetGroup) {
        return this.supportsTargetGroup(targetGroup);
    }

    public boolean isValid() {
        if (!OverlayRegistry.isEspEnabled()) {
            return false;
        }
        for (BooleanSetting setting : this.targetActivationSettings.values()) {
            if (setting.isEnabled()) {
                return true;
            }
        }
        for (BooleanSetting setting : this.playerActivationSettings.values()) {
            if (setting.isEnabled()) {
                return true;
            }
        }
        for (BooleanSetting setting : this.itemActivationSettings.values()) {
            if (setting.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    public boolean isValid2(TargetGroup targetGroup) {
        return OverlayRegistry.isEspEnabled() && this.isTargetGroupEnabled(targetGroup);
    }

    public boolean isValid2(PlayerTargetGroup playerTargetGroup) {
        return OverlayRegistry.isEspEnabled() && this.isPlayerGroupEnabled(playerTargetGroup);
    }

    public boolean isValid2(ItemTargetType itemTargetType) {
        return OverlayRegistry.isEspEnabled() && this.isItemTypeEnabled(itemTargetType);
    }

    public boolean isTargetGroupEnabled(TargetGroup targetGroup) {
        BooleanSetting setting = this.getTargetActivationSetting(targetGroup);
        return this.supportsTargetGroup(targetGroup) && setting != null && setting.isEnabled();
    }

    public boolean isPlayerGroupEnabled(PlayerTargetGroup playerTargetGroup) {
        BooleanSetting setting = this.getPlayerActivationSetting(playerTargetGroup);
        return this.supportsTargetGroup(playerTargetGroup) && setting != null && setting.isEnabled();
    }

    public boolean isItemTypeEnabled(ItemTargetType itemTargetType) {
        BooleanSetting setting = this.getItemActivationSetting(itemTargetType);
        return this.supportsItemType(itemTargetType) && setting != null && setting.isEnabled();
    }

    public void enableTargetGroup(TargetGroup... targetGroups) {
        if (targetGroups == null) {
            return;
        }
        for (TargetGroup targetGroup : targetGroups) {
            if (!this.supportsTargetGroup(targetGroup)) {
                continue;
            }
            BooleanSetting setting = this.getTargetActivationSetting(targetGroup);
            if (setting != null) {
                setting.enable();
            }
        }
    }

    public void enablePlayerGroup(PlayerTargetGroup... playerTargetGroups) {
        if (playerTargetGroups == null) {
            return;
        }
        for (PlayerTargetGroup playerTargetGroup : playerTargetGroups) {
            if (!this.supportsTargetGroup(playerTargetGroup)) {
                continue;
            }
            BooleanSetting setting = this.getPlayerActivationSetting(playerTargetGroup);
            if (setting != null) {
                setting.enable();
            }
        }
    }

    public void enableItemType(ItemTargetType... itemTargetTypes) {
        if (itemTargetTypes == null) {
            return;
        }
        for (ItemTargetType itemTargetType : itemTargetTypes) {
            if (!this.supportsItemType(itemTargetType)) {
                continue;
            }
            BooleanSetting setting = this.getItemActivationSetting(itemTargetType);
            if (setting != null) {
                setting.enable();
            }
        }
    }

    /** Creates one boolean toggle and bind setting for every supported scope. */
    public BooleanSetting createSetting(String key) {
        BooleanSetting first = null;
        for (Scope scope : this.scopes()) {
            BooleanSetting setting = new BooleanSetting(this, key);
            IntegerSetting binding = new IntegerSetting(this, key + ".bind");
            this.storeActivation(scope, setting, binding);
            if (first == null) {
                first = setting;
            }
        }
        if (this.firstEnabledSetting == null) {
            this.firstEnabledSetting = first;
        }
        return first;
    }

    public <T extends Setting> T createSetting(BasicSettingFactory<T> factory) {
        ScopedSettings scoped = new ScopedSettings();
        T first = null;
        for (Scope scope : this.scopes()) {
            T setting = factory.create(this, scope.enabled());
            scoped.put(scope, setting);
            if (first == null) {
                first = setting;
            }
        }
        this.publishScopedSettings(first, scoped);
        return first;
    }

    public <T extends Setting> T createSetting(String dependencyKey, ConditionalSettingFactory<T> factory) {
        ScopedSettings scoped = new ScopedSettings();
        T first = null;
        for (Scope scope : this.scopes()) {
            T setting = factory.create(this, scope.enabled(), this.booleanDependency(dependencyKey, scope));
            scoped.put(scope, setting);
            if (first == null) {
                first = setting;
            }
        }
        this.publishScopedSettings(first, scoped);
        return first;
    }

    public <T extends Setting> T createSetting(String firstKey, String secondKey, DependentSettingFactory<T> factory) {
        ScopedSettings scoped = new ScopedSettings();
        T first = null;
        for (Scope scope : this.scopes()) {
            T setting = factory.create(this,
                    scope.enabled(),
                    this.booleanDependency(firstKey, scope),
                    this.booleanDependency(secondKey, scope));
            scoped.put(scope, setting);
            if (first == null) {
                first = setting;
            }
        }
        this.publishScopedSettings(first, scoped);
        return first;
    }

    public <T extends Setting> T createSetting(String firstKey, String secondKey, String thirdKey,
            MultiConditionSettingFactory<T> factory) {
        ScopedSettings scoped = new ScopedSettings();
        T first = null;
        for (Scope scope : this.scopes()) {
            T setting = factory.create(this,
                    scope.enabled(),
                    this.booleanDependency(firstKey, scope),
                    this.booleanDependency(secondKey, scope),
                    this.booleanDependency(thirdKey, scope));
            scoped.put(scope, setting);
            if (first == null) {
                first = setting;
            }
        }
        this.publishScopedSettings(first, scoped);
        return first;
    }

    public <T extends Setting> T createSetting(TargetSettingFactory<T> factory) {
        ScopedSettings scoped = new ScopedSettings();
        T first = null;
        for (Scope scope : this.scopes()) {
            T setting = factory.create(this, scope.enabled(), scope.targetGroup());
            scoped.put(scope, setting);
            if (first == null) {
                first = setting;
            }
        }
        this.publishScopedSettings(first, scoped);
        return first;
    }

    public <T extends Setting> T getSettingForScope(String key, TargetGroup targetGroup) {
        return this.getSettingForScope(key, (Object)targetGroup);
    }

    public <T extends Setting> T getSettingForScope(String key, PlayerTargetGroup playerTargetGroup) {
        return this.getSettingForScope(key, (Object)playerTargetGroup);
    }

    public <T extends Setting> T getSettingForScope(String key, ItemTargetType itemTargetType) {
        return this.getSettingForScope(key, (Object)itemTargetType);
    }

    /**
     * ORIGINAL: rockstar/ilIlil/IiiiIiiI#I(String, IiiiiIiI|IiiiiIII|IiiiIiii) - three
     * separate one-liners, each a plain map.get(key) then get(scope) against its own
     * per-scope map.  There is no cross-scope fallback of any kind: a miss returns null.
     */
    @SuppressWarnings("unchecked")
    public <T extends Setting> T getSettingForScope(String key, Object scope) {
        return (T)this.findScopedSetting(key, scope);
    }

    /** Returns the settings applicable to the selected target scope in owner order. */
    public List<Setting> getSettingsForScope(TargetGroup targetGroup) {
        return this.collectSettingsForScope(targetGroup);
    }

    public List<Setting> getSettingsForScope(PlayerTargetGroup playerTargetGroup) {
        return this.collectSettingsForScope(playerTargetGroup);
    }

    public List<Setting> getSettingsForScope(ItemTargetType itemTargetType) {
        return this.collectSettingsForScope(itemTargetType);
    }

    public Map<String, Map<Object, Setting>> getScopedSettingsByKey() {
        return this.scopedSettingsByKey;
    }

    /**
     * Publishes one factory call's per-scope maps under a SINGLE lookup key.
     *
     * <p>ORIGINAL: the tail of every IiiiIiiI factory overload is
     * {@code String key = first != null ? first.getName() : ""; IIi.put(key, targetMap);
     * IiI.put(key, playerMap); Iii.put(key, itemMap);} - the name of the FIRST setting the
     * scope loop created, applied to all three maps.  A setting is NEVER indexed under its
     * own name when that differs from the first one.</p>
     *
     * <p>Glow depends on this exactly: its 3-arg factory names the toggle
     * "esp.glow.item_color" for the item scopes and "esp.glow.entity_color" everywhere else,
     * and PLAYERS is its first supported group, so all of "esp.glow.color"'s dependants
     * resolve their dependency with the literal key "esp.glow.entity_color" - including in
     * the Items scope, where it yields the item_color toggle.</p>
     */
    private void publishScopedSettings(Setting first, ScopedSettings scoped) {
        String key = first != null ? first.getName() : "";
        this.perTargetSettings.put(key, scoped.byTargetGroup);
        this.perPlayerSettings.put(key, scoped.byPlayerGroup);
        this.perItemSettings.put(key, scoped.byItemType);

        Map<Object, Setting> combined = new LinkedHashMap<>();
        combined.putAll(scoped.byTargetGroup);
        combined.putAll(scoped.byPlayerGroup);
        combined.putAll(scoped.byItemType);
        this.scopedSettingsByKey.put(key, combined);
    }

    public void registerScopedSetting(String key, Object scope, Setting setting) {
        if (key == null || scope == null || setting == null) {
            return;
        }
        this.scopedSettingsByKey.computeIfAbsent(key, ignored -> new LinkedHashMap<>()).put(scope, setting);
        if (scope instanceof TargetGroup targetGroup) {
            this.perTargetSettings.computeIfAbsent(key, ignored -> new LinkedHashMap<>()).put(targetGroup, setting);
        } else if (scope instanceof PlayerTargetGroup playerTargetGroup) {
            this.perPlayerSettings.computeIfAbsent(key, ignored -> new LinkedHashMap<>()).put(playerTargetGroup, setting);
        } else if (scope instanceof ItemTargetType itemTargetType) {
            this.perItemSettings.computeIfAbsent(key, ignored -> new LinkedHashMap<>()).put(itemTargetType, setting);
        }
    }

    /**
     * Marks a shared setting as applicable to the supplied target groups.
     *
     * <p>ORIGINAL: IiiiIiiI#I(IIiiiIIII, IiiiiIiI[]) is a single
     * settingDependencies.put(setting, new HashSet&lt;&gt;(Arrays.asList(targetGroups))).
     * It does not filter by supported group and it does not touch the per-scope maps - the
     * setting stays UNSCOPED and is picked up by unscopedSettingsForTargetGroup.</p>
     */
    public void attachSettingToTargetGroups(Setting setting, TargetGroup... targetGroups) {
        if (setting == null || targetGroups == null) {
            return;
        }
        this.settingDependencies.put(setting, new LinkedHashSet<>(Arrays.asList(targetGroups)));
    }

    protected BooleanSetting getTargetActivationSetting(TargetGroup targetGroup) {
        return targetGroup == null ? null : this.targetActivationSettings.get(targetGroup);
    }

    protected BooleanSetting getPlayerActivationSetting(PlayerTargetGroup playerTargetGroup) {
        return playerTargetGroup == null ? null : this.playerActivationSettings.get(playerTargetGroup);
    }

    protected BooleanSetting getItemActivationSetting(ItemTargetType itemTargetType) {
        return itemTargetType == null ? null : this.itemActivationSettings.get(itemTargetType);
    }

    protected IntegerSetting getTargetBindingSetting(TargetGroup targetGroup) {
        return targetGroup == null ? null : this.targetBindingSettings.get(targetGroup);
    }

    protected IntegerSetting getPlayerBindingSetting(PlayerTargetGroup playerTargetGroup) {
        return playerTargetGroup == null ? null : this.playerBindingSettings.get(playerTargetGroup);
    }

    protected IntegerSetting getItemBindingSetting(ItemTargetType itemTargetType) {
        return itemTargetType == null ? null : this.itemBindingSettings.get(itemTargetType);
    }

    public IntegerSetting getBindingSetting(TargetGroup targetGroup) {
        return this.getTargetBindingSetting(targetGroup);
    }

    public IntegerSetting getBindingSetting(PlayerTargetGroup playerTargetGroup) {
        return this.getPlayerBindingSetting(playerTargetGroup);
    }

    public IntegerSetting getBindingSetting(ItemTargetType itemTargetType) {
        return this.getItemBindingSetting(itemTargetType);
    }

    /** Serializes the same per-scope shape used by the original ESP configuration. */
    public JsonObject serializeConfiguration() {
        JsonObject configuration = new JsonObject();
        configuration.addProperty("name", this.name);
        configuration.add("enabledByType", this.serializeScopeMap(this.targetActivationSettings));
        configuration.add("enabledByPlayerSubType", this.serializeScopeMap(this.playerActivationSettings));
        configuration.add("enabledByItemSubType", this.serializeScopeMap(this.itemActivationSettings));
        configuration.add("bindsByType", this.serializeScopeMap(this.targetBindingSettings));
        configuration.add("bindsByPlayerSubType", this.serializeScopeMap(this.playerBindingSettings));
        configuration.add("bindsByItemSubType", this.serializeScopeMap(this.itemBindingSettings));
        configuration.add("perTypeSettings", this.serializePerScopeMap(this.perTargetSettings));
        configuration.add("perPlayerSubTypeSettings", this.serializePerScopeMap(this.perPlayerSettings));
        configuration.add("perItemSubTypeSettings", this.serializePerScopeMap(this.perItemSettings));

        JsonObject globalSettings = new JsonObject();
        Set<Setting> scoped = this.allScopedSettings();
        for (Setting setting : this.settings) {
            if (scoped.contains(setting)) {
                continue;
            }
            globalSettings.add(setting.getName(), setting.serialize());
        }
        configuration.add("globalSettings", globalSettings);
        return configuration;
    }

    public void applyConfiguration(JsonObject configuration) {
        if (configuration == null) {
            return;
        }
        this.applyScopeMap(configuration, "enabledByType", this.targetActivationSettings);
        this.applyScopeMap(configuration, "enabledByPlayerSubType", this.playerActivationSettings);
        this.applyScopeMap(configuration, "enabledByItemSubType", this.itemActivationSettings);
        this.applyScopeMap(configuration, "bindsByType", this.targetBindingSettings);
        this.applyScopeMap(configuration, "bindsByPlayerSubType", this.playerBindingSettings);
        this.applyScopeMap(configuration, "bindsByItemSubType", this.itemBindingSettings);
        this.applyPerScopeMap(configuration, "perTypeSettings", this.perTargetSettings);
        this.applyPerScopeMap(configuration, "perPlayerSubTypeSettings", this.perPlayerSettings);
        this.applyPerScopeMap(configuration, "perItemSubTypeSettings", this.perItemSettings);

        JsonElement globalElement = configuration.get("globalSettings");
        if (globalElement != null && globalElement.isJsonObject()) {
            JsonObject globalSettings = globalElement.getAsJsonObject();
            Set<Setting> scoped = this.allScopedSettings();
            for (Setting setting : this.settings) {
                if (!scoped.contains(setting) && globalSettings.has(setting.getName())) {
                    this.deserialize(setting, globalSettings.get(setting.getName()));
                }
            }
        }

        // Accept the intermediate source-export format so existing local
        // configs remain usable after switching back to per-scope settings.
        JsonElement legacyElement = configuration.get("settings");
        if (legacyElement != null && legacyElement.isJsonObject()) {
            JsonObject legacySettings = legacyElement.getAsJsonObject();
            for (Setting setting : this.settings) {
                if (legacySettings.has(setting.getName())) {
                    this.deserialize(setting, legacySettings.get(setting.getName()));
                }
            }
        }
    }

    private void storeActivation(Scope scope, BooleanSetting setting, IntegerSetting binding) {
        Object value = scope.value();
        if (value instanceof TargetGroup targetGroup) {
            this.targetActivationSettings.put(targetGroup, setting);
            this.targetBindingSettings.put(targetGroup, binding);
        } else if (value instanceof PlayerTargetGroup playerTargetGroup) {
            this.playerActivationSettings.put(playerTargetGroup, setting);
            this.playerBindingSettings.put(playerTargetGroup, binding);
        } else if (value instanceof ItemTargetType itemTargetType) {
            this.itemActivationSettings.put(itemTargetType, setting);
            this.itemBindingSettings.put(itemTargetType, binding);
        }
    }

    private List<Scope> scopes() {
        List<Scope> scopes = new ArrayList<>();
        for (TargetGroup targetGroup : this.supportedTargetGroups) {
            if (targetGroup == TargetGroup.PLAYERS) {
                for (PlayerTargetGroup playerTargetGroup : this.supportedPlayerGroups) {
                    scopes.add(new Scope(playerTargetGroup, TargetGroup.PLAYERS,
                            this.playerActivationSettings.get(playerTargetGroup)));
                }
            } else if (targetGroup == TargetGroup.ITEMS && !this.supportedItemTypes.isEmpty()) {
                for (ItemTargetType itemTargetType : this.supportedItemTypes) {
                    scopes.add(new Scope(itemTargetType, TargetGroup.ITEMS,
                            this.itemActivationSettings.get(itemTargetType)));
                }
            } else {
                scopes.add(new Scope(targetGroup, targetGroup,
                        this.targetActivationSettings.get(targetGroup)));
            }
        }
        return scopes;
    }

    /**
     * ORIGINAL: inside every IiiiIiiI factory overload the dependency is
     * "aload key; aload scope; invokevirtual I(String, scopeType); checkcast IIiiiIiii"
     * - the raw lookup result, cast, with no fallback.  Because every factory publishes all
     * of its scopes under ONE key (see publishScopedSettings) the lookup always resolves,
     * so the original's predicates dereference the result without a null check.
     */
    private BooleanSetting booleanDependency(String key, Scope scope) {
        return (BooleanSetting)this.findScopedSetting(key, scope.value());
    }

    /**
     * ORIGINAL: the ESP screen (rockstar/ilIlil/IiiiiIIi#I(IiiiIiiI)) concatenates two lists
     * per scope - first the SCOPED settings for that scope, then the module's UNSCOPED
     * settings filtered by their attached target groups.  Reproduced here so that
     * getSettingsForScope keeps a single entry point.
     */
    private List<Setting> collectSettingsForScope(Object scope) {
        TargetGroup targetGroup = scope instanceof PlayerTargetGroup
                ? TargetGroup.PLAYERS
                : scope instanceof ItemTargetType ? TargetGroup.ITEMS : (TargetGroup)scope;
        List<Setting> result = new ArrayList<>(this.scopedSettingsForScope(scope));
        result.addAll(this.unscopedSettingsForTargetGroup(targetGroup));
        return result;
    }

    /**
     * ORIGINAL: IiiiIiiI#i(IiiiiIiI) / #I(IiiiiIII) / #I(IiiiIiii) - walk the values of the
     * matching per-scope map in insertion order and keep the non-null entry for this scope.
     */
    private List<Setting> scopedSettingsForScope(Object scope) {
        List<Setting> result = new ArrayList<>();
        if (scope instanceof TargetGroup targetGroup) {
            for (Map<TargetGroup, Setting> values : this.perTargetSettings.values()) {
                Setting setting = values.get(targetGroup);
                if (setting != null) {
                    result.add(setting);
                }
            }
        } else if (scope instanceof PlayerTargetGroup playerTargetGroup) {
            for (Map<PlayerTargetGroup, Setting> values : this.perPlayerSettings.values()) {
                Setting setting = values.get(playerTargetGroup);
                if (setting != null) {
                    result.add(setting);
                }
            }
        } else if (scope instanceof ItemTargetType itemTargetType) {
            for (Map<ItemTargetType, Setting> values : this.perItemSettings.values()) {
                Setting setting = values.get(itemTargetType);
                if (setting != null) {
                    result.add(setting);
                }
            }
        }
        return result;
    }

    /**
     * ORIGINAL: IiiiIiiI#I(IiiiiIiI) - every setting that is not scoped at all, kept when it
     * has no attached target groups or when its attached set contains this target group.
     */
    private List<Setting> unscopedSettingsForTargetGroup(TargetGroup targetGroup) {
        List<Setting> result = new ArrayList<>();
        Set<Setting> scoped = this.allScopedSettings();
        for (Setting setting : this.settings) {
            if (scoped.contains(setting)) {
                continue;
            }
            Set<TargetGroup> dependencies = this.settingDependencies.get(setting);
            if (dependencies == null || dependencies.contains(targetGroup)) {
                result.add(setting);
            }
        }
        return result;
    }

    private Setting findScopedSetting(String key, Object scope) {
        if (scope instanceof TargetGroup targetGroup) {
            Map<TargetGroup, Setting> values = this.perTargetSettings.get(key);
            return values == null ? null : values.get(targetGroup);
        }
        if (scope instanceof PlayerTargetGroup playerTargetGroup) {
            Map<PlayerTargetGroup, Setting> values = this.perPlayerSettings.get(key);
            return values == null ? null : values.get(playerTargetGroup);
        }
        if (scope instanceof ItemTargetType itemTargetType) {
            Map<ItemTargetType, Setting> values = this.perItemSettings.get(key);
            return values == null ? null : values.get(itemTargetType);
        }
        return null;
    }

    /** ORIGINAL: IiiiIiiI#Ii()Ljava/util/Set; - activation + binding + every per-scope map. */
    private Set<Setting> allScopedSettings() {
        Set<Setting> result = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Map<?, ? extends Setting> values : List.of(this.targetActivationSettings,
                this.playerActivationSettings, this.itemActivationSettings,
                this.targetBindingSettings, this.playerBindingSettings, this.itemBindingSettings)) {
            result.addAll(values.values());
        }
        for (Map<?, ? extends Setting> values : this.perTargetSettings.values()) {
            result.addAll(values.values());
        }
        for (Map<?, ? extends Setting> values : this.perPlayerSettings.values()) {
            result.addAll(values.values());
        }
        for (Map<?, ? extends Setting> values : this.perItemSettings.values()) {
            result.addAll(values.values());
        }
        return result;
    }

    private JsonObject serializeScopeMap(Map<?, ? extends Setting> values) {
        JsonObject result = new JsonObject();
        for (Map.Entry<?, ? extends Setting> entry : values.entrySet()) {
            result.add(this.scopeKey(entry.getKey()), entry.getValue().serialize());
        }
        return result;
    }

    private JsonObject serializePerScopeMap(Map<String, ? extends Map<?, ? extends Setting>> values) {
        JsonObject result = new JsonObject();
        for (Map.Entry<String, ? extends Map<?, ? extends Setting>> entry : values.entrySet()) {
            JsonObject scoped = new JsonObject();
            for (Map.Entry<?, ? extends Setting> scopedEntry : entry.getValue().entrySet()) {
                scoped.add(this.scopeKey(scopedEntry.getKey()), scopedEntry.getValue().serialize());
            }
            result.add(entry.getKey(), scoped);
        }
        return result;
    }

    private void applyScopeMap(JsonObject configuration, String key, Map<?, ? extends Setting> values) {
        JsonElement element = configuration.get(key);
        if (element == null || !element.isJsonObject()) {
            return;
        }
        JsonObject serialized = element.getAsJsonObject();
        for (Map.Entry<?, ? extends Setting> entry : values.entrySet()) {
            JsonElement value = serialized.get(this.scopeKey(entry.getKey()));
            if (value != null) {
                this.deserialize(entry.getValue(), value);
            }
        }
    }

    private void applyPerScopeMap(JsonObject configuration, String key,
            Map<String, ? extends Map<?, ? extends Setting>> values) {
        JsonElement element = configuration.get(key);
        if (element == null || !element.isJsonObject()) {
            return;
        }
        JsonObject serialized = element.getAsJsonObject();
        for (Map.Entry<String, ? extends Map<?, ? extends Setting>> entry : values.entrySet()) {
            JsonElement scopedElement = serialized.get(entry.getKey());
            if (scopedElement == null || !scopedElement.isJsonObject()) {
                continue;
            }
            JsonObject scoped = scopedElement.getAsJsonObject();
            for (Map.Entry<?, ? extends Setting> scopedEntry : entry.getValue().entrySet()) {
                JsonElement value = scoped.get(this.scopeKey(scopedEntry.getKey()));
                if (value != null) {
                    this.deserialize(scopedEntry.getValue(), value);
                }
            }
        }
    }

    private void deserialize(Setting setting, JsonElement value) {
        if (setting.isValidJson(value)) {
            setting.deserialize(value);
        }
    }

    private String scopeKey(Object scope) {
        if (scope instanceof TargetGroup targetGroup) {
            return targetGroup.getTargetKey();
        }
        if (scope instanceof PlayerTargetGroup playerTargetGroup) {
            return playerTargetGroup.getTargetKey();
        }
        if (scope instanceof ItemTargetType itemTargetType) {
            return itemTargetType.getTargetKey();
        }
        return String.valueOf(scope);
    }

    @FunctionalInterface
    public interface BasicSettingFactory<T extends Setting> {
        T create(TargetRenderModule owner, BooleanSetting moduleEnabled);
    }

    @FunctionalInterface
    public interface TargetSettingFactory<T extends Setting> {
        T create(TargetRenderModule owner, BooleanSetting moduleEnabled, TargetGroup targetGroup);
    }

    /**
     * ORIGINAL: rockstar/ilIlil/IiiiIiiI$i#create
     * (Lrockstar/ilIlil/IiiiIiiI;Lrockstar/ilIlil/IIiiiIiii;Lrockstar/ilIlil/IIiiiIiii;)
     * Lrockstar/ilIlil/IIiiiIIII;. The scope's own enable toggle is passed FIRST and the
     * looked-up dependency SECOND - see IiiiIiiI#I(Ljava/lang/String;Lrockstar/ilIlil/IiiiIiiI$i;)
     * which loads the activation map entry before calling I(String, scope).
     */
    @FunctionalInterface
    public interface ConditionalSettingFactory<T extends Setting> {
        T create(TargetRenderModule owner, BooleanSetting moduleEnabled, BooleanSetting dependency);
    }

    /**
     * ORIGINAL: rockstar/ilIlil/IiiiIiiI$I#create - (owner, moduleEnabled, dep1, dep2).
     */
    @FunctionalInterface
    public interface DependentSettingFactory<T extends Setting> {
        T create(TargetRenderModule owner, BooleanSetting moduleEnabled,
                BooleanSetting firstDependency, BooleanSetting secondDependency);
    }

    /**
     * ORIGINAL: rockstar/ilIlil/IiiiIiiI$II#create - (owner, moduleEnabled, dep1, dep2, dep3).
     */
    @FunctionalInterface
    public interface MultiConditionSettingFactory<T extends Setting> {
        T create(TargetRenderModule owner, BooleanSetting moduleEnabled,
                BooleanSetting firstDependency, BooleanSetting secondDependency,
                BooleanSetting thirdDependency);
    }

    private record Scope(Object value, TargetGroup targetGroup, BooleanSetting enabled) {
    }

    /**
     * The three per-scope maps a single factory call fills before publishing them.
     *
     * <p>ORIGINAL: every IiiiIiiI factory overload allocates exactly three local HashMaps
     * (target / player subtype / item subtype) at the top of the method.</p>
     */
    private static final class ScopedSettings {
        private final Map<TargetGroup, Setting> byTargetGroup = new LinkedHashMap<>();
        private final Map<PlayerTargetGroup, Setting> byPlayerGroup = new LinkedHashMap<>();
        private final Map<ItemTargetType, Setting> byItemType = new LinkedHashMap<>();

        private void put(Scope scope, Setting setting) {
            Object value = scope.value();
            if (value instanceof TargetGroup targetGroup) {
                this.byTargetGroup.put(targetGroup, setting);
            } else if (value instanceof PlayerTargetGroup playerTargetGroup) {
                this.byPlayerGroup.put(playerTargetGroup, setting);
            } else if (value instanceof ItemTargetType itemTargetType) {
                this.byItemType.put(itemTargetType, setting);
            }
        }
    }
}
