/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  jep.SharedInterpreter
 *  lombok.Generated
 *  net.minecraft.Text
 */
package moscow.rockstar.api.scripts;

import com.google.gson.JsonElement;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import jep.SharedInterpreter;
import lombok.Generated;
import moscow.rockstar.api.scripts.ScriptModule;
import moscow.rockstar.auth.AccountCredentials;
import moscow.rockstar.auth.MicrosoftClientConfiguration;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.ModuleContract;
import moscow.rockstar.modules.ModuleRegistry;
import moscow.rockstar.modules.config.ModuleConfigurationStore;
import moscow.rockstar.network.http.MediaType;
import moscow.rockstar.scripts.python.PythonRuntime;
import moscow.rockstar.render.esp.OverlayRegistry;
import moscow.rockstar.render.particles.AmbientParticleRenderer;
import moscow.rockstar.render.texture.TextureOverrideRegistry;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.localization.TranslationOverrideRegistry;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.text.Text;
import pyrock.Client;
import pyrock.GlobalVars;
import pyrock.classes.PyCommand;
import pyrock.classes.PyHudElement;
import pyrock.classes.PyIslandStatus;
import pyrock.classes.PyRotations;
import pyrock.classes.aura.PyAura;
import pyrock.events.PyEvents;
import pyrock.utility.render.PyShader;
import pyrock.utility.render.PyShaders;

public class ScriptDescriptor {
    private static final AtomicInteger scriptNamespaceCounter = new AtomicInteger();
    private final File scriptFile;
    private final String scriptName;
    private final String pythonModuleName;
    private final byte[] embeddedSourceBytes;
    private volatile PyEvents pythonEvents;
    private volatile boolean loaded;
    private volatile String moduleNamespace;
    private WatchService watchService;
    private WatchKey watchKey;
    private volatile boolean watching;
    private long lastModifiedTimestamp;
    private final List<ScriptModule> registeredModules = new ArrayList<ScriptModule>();
    private final List<PyHudElement> registeredHudElements = new ArrayList<PyHudElement>();
    private final List<PyCommand> registeredCommands = new ArrayList<PyCommand>();
    private final List<PyShader> registeredShaders = new ArrayList<PyShader>();
    private final Map<ModuleContract, List<Setting>> moduleSettings = new HashMap<ModuleContract, List<Setting>>();
    private final Map<Setting, JsonElement> settingValues = new HashMap<Setting, JsonElement>();
    private final Map<ModeSetting, List<ModeSetting.Option>> modeOptions = new HashMap<ModeSetting, List<ModeSetting.Option>>();
    private final Map<MultiBooleanSetting, List<MultiBooleanSetting.Option>> multiBooleanOptions = new HashMap<MultiBooleanSetting, List<MultiBooleanSetting.Option>>();
    private static ScriptDescriptor currentScript;

    public static AutoCloseable pushCurrentScript(ScriptDescriptor scriptDescriptor) {
        ScriptDescriptor scriptDescriptor2 = currentScript;
        currentScript = scriptDescriptor;
        return () -> {
            currentScript = scriptDescriptor2;
        };
    }

    public static ScriptDescriptor getCurrentScript() {
        return currentScript;
    }

    public ScriptDescriptor(String string) {
        this.scriptName = string;
        this.embeddedSourceBytes = null;
        this.pythonModuleName = ScriptDescriptor.createPythonModuleName(string);
        File file = new File(moscow.rockstar.core.ClientPaths.gameDirectory(), "scripts");
        if (!file.exists()) {
            file.mkdirs();
        }
        this.scriptFile = new File(file, string + ".py");
    }

    public ScriptDescriptor(String string, byte[] byArray) {
        this.scriptName = string;
        this.embeddedSourceBytes = byArray;
        this.pythonModuleName = ScriptDescriptor.createPythonModuleName(string);
        this.scriptFile = null;
    }

    private static String createPythonModuleName(String string) {
        return "__ns_" + string.replaceAll("[^A-Za-z0-9_]", "_") + "_" + scriptNamespaceCounter.incrementAndGet();
    }

    public static void registerModule(ScriptModule scriptModule) {
        if (currentScript != null) {
            currentScript.getRegisteredModules().add(scriptModule);
        }
    }

    public static void registerHudElement(PyHudElement pyHudElement) {
        if (currentScript != null && pyHudElement != null) {
            ScriptDescriptor.currentScript.registeredHudElements.add(pyHudElement);
        }
    }

    public static void registerShader(PyShader pyShader) {
        if (currentScript == null || pyShader == null || ScriptDescriptor.currentScript.registeredShaders.contains(pyShader)) {
            return;
        }
        if (ScriptDescriptor.currentScript.registeredShaders.size() >= 32) {
            throw new IllegalStateException("\u0441\u043a\u0440\u0438\u043f\u0442 \u0441\u043e\u0437\u0434\u0430\u043b \u0431\u043e\u043b\u044c\u0448\u0435 32 \u0448\u0435\u0439\u0434\u0435\u0440\u043e\u0432: \u0432\u0435\u0440\u043e\u044f\u0442\u043d\u043e, create() \u0437\u043e\u0432\u0451\u0442\u0441\u044f \u0432\u043d\u0443\u0442\u0440\u0438 \u043e\u0442\u0440\u0438\u0441\u043e\u0432\u043a\u0438");
        }
        ScriptDescriptor.currentScript.registeredShaders.add(pyShader);
    }

    public static void registerCommand(PyCommand pyCommand) {
        if (currentScript != null && pyCommand != null && !ScriptDescriptor.currentScript.registeredCommands.contains(pyCommand)) {
            ScriptDescriptor.currentScript.registeredCommands.add(pyCommand);
        }
    }

    public static void registerModuleSetting(ModuleContract moduleContract2, Setting setting) {
        if (currentScript != null) {
            ScriptDescriptor.currentScript.moduleSettings.computeIfAbsent(moduleContract2, moduleContract -> new ArrayList()).add(setting);
        }
    }

    public static void rememberSettingValue(Setting setting) {
        if (currentScript != null && !ScriptDescriptor.currentScript.settingValues.containsKey(setting)) {
            ScriptDescriptor.currentScript.settingValues.put(setting, setting.serialize());
        }
    }

    public static void registerModeOption(ModeSetting modeSetting2, ModeSetting.Option option) {
        if (currentScript != null) {
            ScriptDescriptor.currentScript.modeOptions.computeIfAbsent(modeSetting2, modeSetting -> new ArrayList()).add(option);
        }
    }

    public static boolean isModeOptionRegistered(ModeSetting modeSetting, ModeSetting.Option option) {
        if (currentScript == null || modeSetting == null || option == null) {
            return false;
        }
        List<ModeSetting.Option> list = ScriptDescriptor.currentScript.modeOptions.get(modeSetting);
        return list != null && list.contains(option);
    }

    public static void unregisterModeOption(ModeSetting modeSetting, ModeSetting.Option option) {
        if (currentScript == null || modeSetting == null || option == null) {
            return;
        }
        List<ModeSetting.Option> list = ScriptDescriptor.currentScript.modeOptions.get(modeSetting);
        if (list == null) {
            return;
        }
        list.remove(option);
        if (list.isEmpty()) {
            ScriptDescriptor.currentScript.modeOptions.remove(modeSetting);
        }
    }

    public static void registerMultiBooleanOption(MultiBooleanSetting multiBooleanSetting2, MultiBooleanSetting.Option option) {
        if (currentScript != null) {
            ScriptDescriptor.currentScript.multiBooleanOptions.computeIfAbsent(multiBooleanSetting2, multiBooleanSetting -> new ArrayList()).add(option);
        }
    }

    public final void loadScript() {
        if (this.loaded) {
            this.unloadScript();
        }
        if (!PythonRuntime.isAvailable()) {
            Notification.error(Text.of((String)Localization.translate("python.runtime_missing")));
            return;
        }
        PythonRuntime.execute(this::executeScript);
    }

    private void executeScript() {
        try (AutoCloseable autoCloseable = ScriptDescriptor.pushCurrentScript(this);){
            boolean bl;
            boolean bl2 = bl = this.embeddedSourceBytes != null;
            if (!bl && !this.scriptFile.exists()) {
                RockstarClient.LOGGER.warn(Localization.translateFormatted("lua.script.file_missing", this.scriptName));
                this.ensureScriptFile();
            }
            this.moduleNamespace = null;
            PyEvents pyEvents = new PyEvents(this);
            pyEvents.setErrorHandler(exception -> {
                this.moduleNamespace = exception.getMessage() != null ? exception.getMessage() : exception.getClass().getSimpleName();
                Notification.error(Text.of((String)Localization.translateFormatted("lua.script_error", this.scriptName)));
                this.unloadScript();
            });
            this.pythonEvents = pyEvents;
            RockstarClient.LOGGER.info(Localization.translateFormatted("lua.script.execution.running", this.scriptName));
            SharedInterpreter sharedInterpreter = PythonRuntime.getInterpreter();
            if (sharedInterpreter == null || !PythonRuntime.isAvailable()) {
                Notification.error(Text.of((String)Localization.translateFormatted("python.init_failed", String.valueOf(PythonRuntime.getInitializationFailure()))));
                this.unloadScript();
                return;
            }
            sharedInterpreter.set("__ev", (Object)pyEvents);
            sharedInterpreter.set("__cl", (Object)new Client());
            sharedInterpreter.set("__mc", (Object)ClientAccess.minecraftClient);
            sharedInterpreter.set("__gv", (Object)new GlobalVars());
            sharedInterpreter.set("__pa", (Object)new PyAura());
            sharedInterpreter.set("__pr", (Object)new PyRotations());
            sharedInterpreter.exec(this.pythonModuleName + " = __rockstar_make_ns(__ev, __cl, __mc, __gv, __pa, __pr)");
            if (bl) {
                sharedInterpreter.set("__b64", (Object)Base64.getEncoder().encodeToString(this.embeddedSourceBytes));
                sharedInterpreter.exec("import marshal, base64");
                sharedInterpreter.exec("exec(marshal.loads(base64.b64decode(__b64)), " + this.pythonModuleName + ")");
                sharedInterpreter.exec("del __ev, __cl, __mc, __gv, __pa, __pr, __b64");
            } else {
                String source = Files.readString(this.scriptFile.toPath());
                sharedInterpreter.set("__code", (Object)source);
                sharedInterpreter.exec("exec(compile(__code, " + ScriptDescriptor.quotePythonString(this.scriptName) + ", 'exec'), " + this.pythonModuleName + ")");
                sharedInterpreter.exec("del __ev, __cl, __mc, __gv, __pa, __pr, __code");
            }
            this.registerHudElements();
            for (ScriptModule object : this.registeredModules) {
                object.saveSettings();
                ModuleConfigurationStore.applyPendingModuleConfiguration(object);
            }
            for (Map.Entry<ModuleContract, List<Setting>> entry : this.moduleSettings.entrySet()) {
                for (Setting setting : entry.getValue()) {
                    ModuleConfigurationStore.applyPendingSetting(entry.getKey(), setting);
                }
            }
            OverlayRegistry.getInstance().applyPendingConfigByOwner(this);
            this.loaded = true;
            RockstarClient.create().getScriptRegistry().beginEventDispatch();
            if (!bl) {
                this.lastModifiedTimestamp = this.scriptFile.lastModified();
                this.startFileWatcher();
            }
            RockstarClient.LOGGER.info(Localization.translateFormatted("lua.script.load.success_log", this.scriptName));
            Notification.info(Text.of((String)Localization.translateFormatted("lua.script.load.success", this.scriptName)));
        }
        catch (Exception exception2) {
            String string;
            this.moduleNamespace = string = exception2.getMessage() != null ? exception2.getMessage() : exception2.getClass().getSimpleName();
            Notification.error(Text.of((String)Localization.translateFormatted("lua.script.load.error", this.scriptName, string)));
            RockstarClient.LOGGER.error(Localization.translateFormatted("lua.script.load.error_log", this.scriptName), (Throwable)exception2);
            this.unloadScript();
        }
        catch (Throwable throwable) {
            String string;
            this.moduleNamespace = string = throwable.getMessage() != null ? throwable.getMessage() : throwable.getClass().getSimpleName();
            Notification.error(Text.of((String)Localization.translateFormatted("lua.script.load.error", this.scriptName, string)));
            RockstarClient.LOGGER.error(Localization.translateFormatted("lua.script.load.error_log", this.scriptName), throwable);
            this.unloadScript();
        }
    }

    private void registerHudElements() {
        if (this.registeredHudElements.isEmpty() || RockstarClient.create().getHudElementRegistry() == null) {
            return;
        }
        for (PyHudElement element : this.registeredHudElements) {
            RockstarClient.create().getHudElementRegistry().add(element);
        }
    }

    public final boolean unloadScript() {
        if (!this.loaded && this.pythonEvents == null) {
            return false;
        }
        boolean bl = this.loaded;
        this.loaded = false;
        if (bl) {
            RockstarClient.create().getScriptRegistry().endEventDispatch();
        }
        for (ModuleContract object : this.registeredModules) {
            ModuleConfigurationStore.cacheModuleConfiguration(object);
        }
        for (ModuleContract moduleContract : this.registeredModules) {
            if (!moduleContract.isEnabled()) continue;
            moduleContract.setEnabled(false, true);
        }
        if (RockstarClient.create().getModuleRegistry().getModules().removeAll(this.registeredModules)) {
            ModuleRegistry.incrementRegistryCount();
        }
        this.registeredModules.clear();
        this.disposeHudElements();
        this.removeRegisteredCommands();
        this.releaseRegisteredShaders();
        TextureOverrideRegistry.removeOverridesOwnedBy(this);
        TranslationOverrideRegistry.clearOwner(this);
        OverlayRegistry.getInstance().unregisterOverlayByOwner(this);
        for (Map.Entry<ModuleContract, List<Setting>> entry : this.moduleSettings.entrySet()) {
            for (Setting setting : entry.getValue()) {
                ModuleConfigurationStore.cacheSettingConfiguration(entry.getKey(), setting);
            }
            entry.getKey().getSettings().removeAll(entry.getValue());
        }
        this.moduleSettings.clear();
        this.restoreModeOptions();
        this.modeOptions.clear();
        this.removeMultiBooleanOptions();
        this.multiBooleanOptions.clear();
        for (Map.Entry<Setting, JsonElement> entry : this.settingValues.entrySet()) {
            entry.getKey().deserialize(entry.getValue());
        }
        this.settingValues.clear();
        if (this.pythonEvents != null) {
            this.pythonEvents.dispose();
        }
        this.pythonEvents = null;
        this.stopFileWatcher();
        if (PythonRuntime.isAvailable()) {
            PythonRuntime.execute(() -> {
                try {
                    PythonRuntime.getInterpreter().exec("globals().pop('" + this.pythonModuleName + "', None)");
                }
                catch (Exception exception) {
                    // empty catch block
                }
            });
        }
        RockstarClient.LOGGER.info(Localization.translateFormatted("lua.script.unload", this.scriptName));
        return true;
    }

    private void restoreModeOptions() {
        for (Map.Entry<ModeSetting, List<ModeSetting.Option>> entry : this.modeOptions.entrySet()) {
            ModeSetting modeSetting = entry.getKey();
            List<ModeSetting.Option> list = entry.getValue();
            ModeSetting.Option option = modeSetting.getSelectedOption();
            boolean bl = option != null && list.contains(option);
            modeSetting.getOptions().removeIf(list::contains);
            if (!bl) continue;
            modeSetting.select(modeSetting.getOptions().isEmpty() ? null : modeSetting.getOptions().getFirst());
        }
    }

    private void removeRegisteredCommands() {
        for (PyCommand pyCommand : new ArrayList<PyCommand>(this.registeredCommands)) {
            try {
                pyCommand.remove();
            }
            catch (Throwable throwable) {}
        }
        this.registeredCommands.clear();
    }

    private void releaseRegisteredShaders() {
        for (PyShader pyShader : new ArrayList<PyShader>(this.registeredShaders)) {
            try {
                PyShaders.release(pyShader);
            }
            catch (Throwable throwable) {}
        }
        this.registeredShaders.clear();
    }

    private void disposeHudElements() {
        if (this.registeredHudElements.isEmpty() || RockstarClient.create().getHudElementRegistry() == null) {
            this.registeredHudElements.clear();
            return;
        }
        for (PyHudElement pyHudElement : new ArrayList<PyHudElement>(this.registeredHudElements)) {
            pyHudElement.dispose();
            RockstarClient.create().getHudElementRegistry().remove(pyHudElement);
        }
        this.registeredHudElements.clear();
    }

    private void removeMultiBooleanOptions() {
        for (Map.Entry<MultiBooleanSetting, List<MultiBooleanSetting.Option>> entry : this.multiBooleanOptions.entrySet()) {
            MultiBooleanSetting multiBooleanSetting = entry.getKey();
            List<MultiBooleanSetting.Option> list = entry.getValue();
            multiBooleanSetting.getSelectedOptions().removeIf(list::contains);
            multiBooleanSetting.getOptions().removeIf(list::contains);
        }
    }

    public final ScriptDescriptor ensureScriptFile() {
        if (this.scriptFile == null || this.scriptFile.exists()) {
            return this;
        }
        try {
            if (!this.scriptFile.createNewFile()) {
                throw new IOException(Localization.translateFormatted("lua.script.create_file_error", this.scriptFile.getAbsolutePath()));
            }
            try (FileWriter fileWriter = new FileWriter(this.scriptFile);){
                fileWriter.write(Localization.translate("lua.script.template"));
            }
        }
        catch (IOException iOException) {
            RockstarClient.LOGGER.error(Localization.translate("lua.script.save_error"), (Throwable)iOException);
        }
        return this;
    }

    public final boolean deleteScriptFile() {
        this.unloadScript();
        if (this.scriptFile == null) {
            RockstarClient.create().getScriptRegistry().getScripts().remove(this);
            return true;
        }
        if (this.scriptFile.exists() && this.scriptFile.delete()) {
            RockstarClient.create().getScriptRegistry().getScripts().remove(this);
            RockstarClient.LOGGER.info(Localization.translateFormatted("lua.script.delete.success", this.scriptFile.getAbsolutePath()));
            return true;
        }
        RockstarClient.LOGGER.warn(Localization.translateFormatted("lua.script.delete.error", this.scriptFile.getAbsolutePath()));
        return false;
    }

    private static String quotePythonString(String string) {
        return "'" + string.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }

    private void startFileWatcher() {
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            Path path = this.scriptFile.toPath().getParent();
            this.watchKey = path.register(this.watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            this.watching = true;
            Thread thread = new Thread(() -> {
                block4: {
                    try {
                        while (this.watching) {
                            WatchKey watchKey = this.watchService.poll(500L, TimeUnit.MILLISECONDS);
                            if (watchKey == null) continue;
                            for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                                long l;
                                Path changedPath = (Path)watchEvent.context();
                                if (!changedPath.toString().equals(this.scriptFile.getName()) || (l = this.scriptFile.lastModified()) == this.lastModifiedTimestamp) continue;
                                this.lastModifiedTimestamp = l;
                                Thread.sleep(100L);
                                ClientAccess.minecraftClient.execute(this::reloadAfterFileChange);
                            }
                            watchKey.reset();
                        }
                    }
                    catch (Exception exception) {
                        if (!this.watching) break block4;
                        RockstarClient.LOGGER.error("File watcher error: {}", (Object)exception.getMessage());
                    }
                }
            }, "Python-Watcher-" + this.scriptName);
            thread.setDaemon(true);
            thread.start();
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.error("Failed to start file watcher: {}", (Object)exception.getMessage());
        }
    }

    private void stopFileWatcher() {
        this.watching = false;
        try {
            if (this.watchKey != null) {
                this.watchKey.cancel();
                this.watchKey = null;
            }
            if (this.watchService != null) {
                this.watchService.close();
                this.watchService = null;
            }
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.error("Failed to stop file watcher: {}", (Object)exception.getMessage());
        }
    }

    private void reloadAfterFileChange() {
        this.unloadScript();
        this.loadScript();
    }

    @Generated
    public File getScriptFile() {
        return this.scriptFile;
    }

    @Generated
    public String getScriptName() {
        return this.scriptName;
    }

    @Generated
    public String getPythonModuleName() {
        return this.pythonModuleName;
    }

    @Generated
    public byte[] getEmbeddedSource() {
        return this.embeddedSourceBytes;
    }

    @Generated
    public PyEvents getPythonEvents() {
        return this.pythonEvents;
    }

    @Generated
    public boolean isLoaded() {
        return this.loaded;
    }

    @Generated
    public String getLoadError() {
        return this.moduleNamespace;
    }

    @Generated
    public WatchService getWatchService() {
        return this.watchService;
    }

    @Generated
    public WatchKey getWatchKey() {
        return this.watchKey;
    }

    @Generated
    public boolean isWatching() {
        return this.watching;
    }

    @Generated
    public long getLastModifiedTimestamp() {
        return this.lastModifiedTimestamp;
    }

    @Generated
    public List<ScriptModule> getRegisteredModules() {
        return this.registeredModules;
    }

    @Generated
    public List<PyHudElement> getRegisteredHudElements() {
        return this.registeredHudElements;
    }

    @Generated
    public List<PyCommand> getRegisteredCommands() {
        return this.registeredCommands;
    }

    @Generated
    public List<PyShader> getRegisteredShaders() {
        return this.registeredShaders;
    }

    @Generated
    public Map<ModuleContract, List<Setting>> getModuleSettings() {
        return this.moduleSettings;
    }

    @Generated
    public Map<Setting, JsonElement> getSettingValues() {
        return this.settingValues;
    }

    @Generated
    public Map<ModeSetting, List<ModeSetting.Option>> getModeOptions() {
        return this.modeOptions;
    }

    @Generated
    public Map<MultiBooleanSetting, List<MultiBooleanSetting.Option>> getMultiBooleanOptions() {
        return this.multiBooleanOptions;
    }
}
