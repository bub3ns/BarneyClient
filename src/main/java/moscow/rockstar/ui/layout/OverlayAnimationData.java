/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.layout;

import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.screens.ModuleSettingsScreen;

public interface OverlayAnimationData {
    public float getAnimationProgress();

    public void renderOverlay(RockstarDrawContext var1, float var2, float var3, float var4);

    public boolean isPointerOver(ModuleSettingsScreen var1, double var2, double var4, int var6);
}

