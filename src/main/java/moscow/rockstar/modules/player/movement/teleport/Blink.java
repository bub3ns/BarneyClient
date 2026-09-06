/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.Vec3d
 *  net.minecraft.Packet
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.MatrixStack
 *  net.minecraft.Perspective
 *  net.minecraft.BuiltBuffer
 */
package moscow.rockstar.modules.player.movement.teleport;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.combat.defense.AutoTotem;
import moscow.rockstar.render.util.RenderUtils;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.util.Timer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.BuiltBuffer;
import pyrock.events.game.AfterAttackEvent;
import pyrock.events.game.WorldChangeEvent;
import pyrock.events.network.SendPacketEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Blink", category=ModuleCategory.PLAYER)
public class Blink
extends Module {
    private final List<Packet<?>> queuedPackets = new ArrayList();
    private final Timer pulseTimer = new Timer();
    private BooleanSetting pulseSetting;
    private NumberSetting pulseIntervalSetting;
    private BooleanSetting renderPositionSetting;
    private BooleanSetting hideInFirstPersonSetting;
    private Vec3d blinkStartPosition;
    private boolean sendingQueuedPackets;
    private final EventListener<SendPacketEvent> sendPacketListener = this::onSendPacket;
    private final EventListener<AfterAttackEvent> afterAttackListener = afterAttackEvent -> {
        this.onDisable();
        this.onEnable();
        this.pulseTimer.reset();
    };
    private final EventListener<Render3DEvent> render3DListener = render3DEvent -> {
        if (this.renderPositionSetting.isEnabled() && this.blinkStartPosition != null && (Blink.minecraftClient.options.getPerspective() != Perspective.FIRST_PERSON || !this.hideInFirstPersonSetting.isEnabled())) {
            MatrixStack class_45872 = render3DEvent.getMatrices();
            BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            Vec3d VanillaChestLootTableGenerator = Blink.minecraftClient.gameRenderer.getCamera().getPos();
            class_45872.push();
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderUtils.drawBoxOutline(class_45872, class_2872, Blink.minecraftClient.player.getBoundingBox().offset(this.blinkStartPosition.subtract(Blink.minecraftClient.player.getPos())).offset(-VanillaChestLootTableGenerator.x, -VanillaChestLootTableGenerator.y, -VanillaChestLootTableGenerator.z), ColorRGBA.WHITE.withAlpha(180.0f));
            BuiltBuffer class_98012 = class_2872.endNullable();
            if (class_98012 != null) {
                BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_98012);
            }
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            class_45872.pop();
        }
    };
    private final EventListener<WorldChangeEvent> worldChangeListener = worldChangeEvent -> this.disable();

    public Blink() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.pulseSetting = new BooleanSetting(this, "modules.settings.blink.pulse");
        this.pulseIntervalSetting = new NumberSetting((SettingOwner)this, "modules.settings.blink.time", () -> !this.pulseSetting.isEnabled()).setMinValue(1.0f).setMaxValue(40.0f).setStep(1.0f).setValue(12.0f);
        this.renderPositionSetting = new BooleanSetting(this, "modules.settings.blink.display");
        this.hideInFirstPersonSetting = new BooleanSetting((SettingOwner)this, "modules.settings.blink.hide_first_person", () -> !this.renderPositionSetting.isEnabled());
    }

    public void onSendPacket(SendPacketEvent sendPacketEvent) {
        if (this.sendingQueuedPackets || !EntityUtils.isClientWorldReady() || RockstarClient.create().getModuleRegistry().getModule(AutoTotem.class).shouldEquipTotem()) {
            return;
        }
        this.queuedPackets.add(sendPacketEvent.getPacket());
        sendPacketEvent.cancel();
        if (this.pulseSetting.isEnabled() && this.pulseTimer.hasElapsed((long)(this.pulseIntervalSetting.getValue() * 50.0f))) {
            this.onDisable();
            this.onEnable();
            this.pulseTimer.reset();
        }
    }

    @Override
    public void onEnable() {
        if (Blink.minecraftClient.player == null) {
            return;
        }
        this.queuedPackets.clear();
        this.blinkStartPosition = Blink.minecraftClient.player.getPos();
        this.pulseTimer.reset();
        this.sendingQueuedPackets = false;
    }

    @Override
    public void onDisable() {
        if (Blink.minecraftClient.player == null) {
            return;
        }
        this.sendingQueuedPackets = true;
        for (Packet<?> class_25962 : this.queuedPackets) {
            Blink.minecraftClient.player.networkHandler.sendPacket(class_25962);
        }
        this.sendingQueuedPackets = false;
        this.queuedPackets.clear();
        this.blinkStartPosition = null;
    }

    @Generated
    public Timer getPulseTimer() {
        return this.pulseTimer;
    }

    @Generated
    public BooleanSetting getPulseSetting() {
        return this.pulseSetting;
    }

    @Generated
    public NumberSetting getPulseIntervalSetting() {
        return this.pulseIntervalSetting;
    }
}

