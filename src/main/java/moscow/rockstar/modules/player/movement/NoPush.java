/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.modules.player.movement;

import lombok.Generated;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.MultiBooleanSetting;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="No Push", category=ModuleCategory.PLAYER, description="modules.descriptions.no_push")
public class NoPush
extends Module {
    private MultiBooleanSetting removeFrom;
    private MultiBooleanSetting.Option entities;
    private MultiBooleanSetting.Option fluids;
    private MultiBooleanSetting.Option bubbleColumns;
    private MultiBooleanSetting.Option blocks;

    public NoPush() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.removeFrom = new MultiBooleanSetting(this, "modules.settings.no_push.remove_from");
        this.entities = new MultiBooleanSetting.Option(this.removeFrom, "modules.settings.no_push.remove_from.entities", "modules.settings.no_push.remove_from.entities.description").select();
        this.fluids = new MultiBooleanSetting.Option(this.removeFrom, "modules.settings.no_push.remove_from.fluids", "modules.settings.no_push.remove_from.fluids.description");
        this.bubbleColumns = new MultiBooleanSetting.Option(this.removeFrom, "modules.settings.no_push.remove_from.bubble_columns", "modules.settings.no_push.remove_from.bubble_columns.description");
        this.blocks = new MultiBooleanSetting.Option(this.removeFrom, "modules.settings.no_push.remove_from.blocks", "modules.settings.no_push.remove_from.blocks.description").select();
    }

    @Generated
    public MultiBooleanSetting getRemoveFrom() {
        return this.removeFrom;
    }

    @Generated
    public MultiBooleanSetting.Option getEntitiesOption() {
        return this.entities;
    }

    @Generated
    public MultiBooleanSetting.Option getFluids() {
        return this.fluids;
    }

    @Generated
    public MultiBooleanSetting.Option getBubbleColumns() {
        return this.bubbleColumns;
    }

    @Generated
    public MultiBooleanSetting.Option getBlocks() {
        return this.blocks;
    }
}

