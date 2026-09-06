/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Sprite
 *  net.minecraft.StatusEffect
 *  net.minecraft.RegistryEntry
 */
package moscow.rockstar.ui.notifications;

import net.minecraft.client.MinecraftClient;
import moscow.rockstar.ui.notifications.NotificationRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;

public class StatusEffectNotification
extends NotificationRenderer {
    private final RegistryEntry<StatusEffect> statusEffect;

    public StatusEffectNotification(String string, RegistryEntry<StatusEffect> class_68802) {
        super(string, null, ColorRGBA.fromInt(class_68802.value().getColor()));
        this.statusEffect = class_68802;
    }

    public StatusEffectNotification withHighlightedText(String string) {
        this.highlightedText = string;
        return this;
    }

    public StatusEffectNotification withHighlightColor(ColorRGBA colorRGBA) {
        this.highlightColor = colorRGBA;
        return this;
    }

    @Override
    protected void renderContent(CustomDrawContext customDrawContext, float f, float f2, float f3) {
        Sprite class_10582 = MinecraftClient.getInstance().getStatusEffectSpriteManager().getSprite(this.statusEffect);
        customDrawContext.drawTexture(class_10582.getAtlasId(), f, f2, 10.0f, 10.0f, class_10582.getMinU(), class_10582.getMaxU(), class_10582.getMinV(), class_10582.getMaxV(), ColorRGBA.WHITE.withAlpha(255.0f * f3));
    }
}
