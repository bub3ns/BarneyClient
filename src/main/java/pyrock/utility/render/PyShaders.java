/*
 * Decompiled with CFR 0.152.
 */
package pyrock.utility.render;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import pyrock.utility.render.PyAssets;
import pyrock.utility.render.PyShader;

public class PyShaders {
    private static final Map<String, PyShader> CACHE = new ConcurrentHashMap<String, PyShader>();

    public PyShader create(String string, String string2) {
        return this.create(string, null, string2);
    }

    public PyShader create(String string, String string2, String string3) {
        String string4 = ScriptDescriptor.getCurrentScript() == null ? "?" : ScriptDescriptor.getCurrentScript().getScriptName();
        String string5 = string4 + "\u0000" + string + "\u0000" + (string2 == null ? "" : string2).hashCode() + "\u0000" + string3.hashCode();
        PyShader pyShader = CACHE.get(string5);
        if (pyShader != null && pyShader.valid()) {
            return pyShader;
        }
        PyShader pyShader2 = new PyShader(string, string2, string3);
        CACHE.put(string5, pyShader2);
        ScriptDescriptor.registerShader(pyShader2);
        return pyShader2;
    }

    public PyShader load(String string, String string2) {
        return this.load(string, null, string2);
    }

    public PyShader load(String string, String string2, String string3) {
        return this.create(string, string2 == null ? null : PyShaders.read(string2), PyShaders.read(string3));
    }

    public void dispose(PyShader pyShader) {
        PyShaders.release(pyShader);
    }

    public static void release(PyShader pyShader) {
        if (pyShader == null) {
            return;
        }
        CACHE.values().removeIf(pyShader2 -> pyShader2 == pyShader);
        pyShader.dispose();
    }

    private static String read(String string) {
        Path path = PyAssets.resolve(string);
        if (!Files.isRegularFile(path, new LinkOption[0])) {
            throw new IllegalArgumentException("shader file not found: " + String.valueOf(path));
        }
        try {
            return Files.readString(path);
        }
        catch (Exception exception) {
            throw new RuntimeException("failed to read shader: " + String.valueOf(path), exception);
        }
    }
}

