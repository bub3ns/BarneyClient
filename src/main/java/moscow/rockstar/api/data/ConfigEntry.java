/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.api.data;

import java.io.File;
import lombok.Generated;

/**
 * One persisted client document.  The annotation on the concrete subclass names
 * both the document and the file extension, and the file itself always lives in
 * {@link ClientConfigManager#CONFIG_DIRECTORY}.
 *
 * <p>ORIGINAL: {@code rockstar/ilIlil/IiIIIiii}.</p>
 */
public abstract class ConfigEntry {
    public final ConfigName config = this.getClass().getAnnotation(ConfigName.class);
    public final File file = new File(ClientConfigManager.CONFIG_DIRECTORY, this.config.value() + "." + this.config.extension());

    /** ORIGINAL: {@code I()V} */
    public abstract void save();

    /** ORIGINAL: {@code i()V} */
    public abstract void load();

    @Generated
    public ConfigName getConfig() {
        return this.config;
    }

    @Generated
    public File getFile() {
        return this.file;
    }
}
