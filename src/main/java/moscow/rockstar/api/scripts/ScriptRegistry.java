/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.api.scripts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Generated;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.auth.MicrosoftClientConfiguration;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.Event;
import moscow.rockstar.scripts.python.PythonRuntime;

public class ScriptRegistry {
    private final List<ScriptDescriptor> scripts = new ArrayList<ScriptDescriptor>();
    private final Set<String> enabledScriptNames = ConcurrentHashMap.newKeySet();
    private final AtomicInteger reloadGeneration = new AtomicInteger();

    public ScriptRegistry() {
        PythonRuntime.initialize();
        this.discoverScripts();
    }

    void beginEventDispatch() {
        this.reloadGeneration.incrementAndGet();
    }

    void endEventDispatch() {
        this.reloadGeneration.updateAndGet(n -> n > 0 ? n - 1 : 0);
    }

    public final void dispatchEventToScripts(Event event) {
        if (this.reloadGeneration.get() == 0) {
            return;
        }
        for (ScriptDescriptor scriptDescriptor : this.scripts) {
            if (scriptDescriptor.getPythonEvents() == null) continue;
            scriptDescriptor.getPythonEvents().fire(event);
        }
    }

    public final void discoverScripts() {
        this.scripts.forEach(ScriptDescriptor::unloadScript);
        this.scripts.clear();
        Path path2 = Paths.get(moscow.rockstar.core.ClientPaths.gameDirectory().getPath(), "scripts");
        if (!Files.exists(path2, new LinkOption[0])) {
            try {
                Files.createDirectories(path2, new FileAttribute[0]);
            }
            catch (IOException iOException) {
                RockstarClient.LOGGER.error("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0437\u0434\u0430\u0442\u044c \u0434\u0438\u0440\u0435\u043a\u0442\u043e\u0440\u0438\u044e \u0441\u043a\u0440\u0438\u043f\u0442\u043e\u0432: {}", (Object)iOException.getMessage());
            }
        } else {
            try {
                try (java.util.stream.Stream<Path> paths = Files.list(path2)) {
                    paths.filter(path -> Files.isRegularFile(path, new LinkOption[0])).filter(path -> path.getFileName().toString().endsWith(".py")).forEach(path -> {
                        String string = path.getFileName().toString();
                        this.scripts.add(new ScriptDescriptor(string.substring(0, string.length() - 3)));
                    });
                }
            }
            catch (IOException iOException) {
                RockstarClient.LOGGER.error("\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u0441\u043a\u0430\u043d\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0438 \u0434\u0438\u0440\u0435\u043a\u0442\u043e\u0440\u0438\u0438 \u0441\u043a\u0440\u0438\u043f\u0442\u043e\u0432: {}", (Object)iOException.getMessage());
            }
        }
        this.loadEnabledScripts();
    }

    public void setScriptEnabled(String string, boolean bl) {
        String string2 = ScriptRegistry.normalizeScriptName(string);
        if (string2.isEmpty()) {
            return;
        }
        if (bl) {
            this.enabledScriptNames.add(string2);
        } else {
            this.enabledScriptNames.remove(string2);
        }
        ScriptDescriptor scriptDescriptor = this.findScript(string);
        if (scriptDescriptor == null) {
            return;
        }
        if (bl) {
            if (!scriptDescriptor.isLoaded()) {
                scriptDescriptor.loadScript();
            }
        } else if (scriptDescriptor.isLoaded()) {
            scriptDescriptor.unloadScript();
        }
    }

    public List<String> getEnabledScriptNames() {
        return new ArrayList<String>(this.enabledScriptNames);
    }

    public void setEnabledScripts(Collection<String> collection) {
        this.enabledScriptNames.clear();
        if (collection != null) {
            for (String object : collection) {
                String string = ScriptRegistry.normalizeScriptName(object);
                if (string.isEmpty()) continue;
                this.enabledScriptNames.add(string);
            }
        }
        for (ScriptDescriptor scriptDescriptor : this.scripts) {
            boolean bl = this.enabledScriptNames.contains(ScriptRegistry.normalizeScriptName(scriptDescriptor.getScriptName()));
            if (bl && !scriptDescriptor.isLoaded()) {
                scriptDescriptor.loadScript();
                continue;
            }
            if (bl || !scriptDescriptor.isLoaded()) continue;
            scriptDescriptor.unloadScript();
        }
    }

    private void loadEnabledScripts() {
        if (this.enabledScriptNames.isEmpty()) {
            return;
        }
        for (ScriptDescriptor scriptDescriptor : this.scripts) {
            if (scriptDescriptor.isLoaded() || !this.enabledScriptNames.contains(ScriptRegistry.normalizeScriptName(scriptDescriptor.getScriptName()))) continue;
            scriptDescriptor.loadScript();
        }
    }

    private ScriptDescriptor findScript(String string) {
        String string2 = ScriptRegistry.normalizeScriptName(string);
        for (ScriptDescriptor scriptDescriptor : this.scripts) {
            if (!ScriptRegistry.normalizeScriptName(scriptDescriptor.getScriptName()).equals(string2)) continue;
            return scriptDescriptor;
        }
        return null;
    }

    private static String normalizeScriptName(String string) {
        return string == null ? "" : string.trim().toLowerCase(Locale.ROOT);
    }

    @Generated
    public List<ScriptDescriptor> getScripts() {
        return this.scripts;
    }
}
