/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.impl.FabricLoaderImpl
 *  net.fabricmc.loader.impl.ModContainerImpl
 *  net.minecraft.ResourcePack
 *  net.minecraft.Icons
 */
package moscow.rockstar.modules.other.safety;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleContract;
import moscow.rockstar.modules.ModuleInfo;

@ModuleInfo(name="Panic", category=ModuleCategory.OTHER, description="modules.descriptions.panic")
public class Panic
extends Module {
    @Override
    public final void onEnable() {
        // Panic disables client features only. It must not delete files, mutate the
        // Fabric loader, or redirect Minecraft's run directory.
        RockstarClient.create().setPanicMode(true);
        for (ModuleContract object : RockstarClient.create().getModuleRegistry().getModules()) {
            object.setKeyBind(-1);
            object.disable();
        }
        super.onEnable();
    }
}
