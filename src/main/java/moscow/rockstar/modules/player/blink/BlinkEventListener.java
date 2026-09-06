/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.Packet
 *  net.minecraft.PlayerMoveC2SPacket
 *  net.minecraft.PlayerMoveC2SPacket$PositionAndOnGround
 *  net.minecraft.PlayerMoveC2SPacket$Full
 *  net.minecraft.PlayerMoveC2SPacket$LookAndOnGround
 *  net.minecraft.PlayerMoveC2SPacket$OnGroundOnly
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.BuiltBuffer
 */
package moscow.rockstar.modules.player.blink;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.EntityStateSnapshot;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.player.movement.teleport.Blink;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.util.RenderUtils;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.BuiltBuffer;
import pyrock.events.game.InternalAttackEvent;
import pyrock.events.network.SendPacketEvent;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.render.Render3DEvent;

public class BlinkEventListener {
    private int attackCount;
    private EntityStateSnapshot savedPlayerState;
    private final EventListener<Render3DEvent> renderListener = render3DEvent -> {
        if (ClientAccess.minecraftClient.world == null || ClientAccess.minecraftClient.player == null || this.savedPlayerState == null) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.lineWidth((float)10.0f);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        render3DEvent.getMatrices().push();
        ItemRenderUtils.translateToCamera(render3DEvent.getMatrices());
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        RenderUtils.drawBoxOutline(render3DEvent.getMatrices(), class_2872, this.savedPlayerState.getBox(), ColorPalette.getAccentColor());
        BuiltBuffer class_98012 = class_2872.endNullable();
        if (class_98012 != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_98012);
        }
        render3DEvent.getMatrices().pop();
    };
    private final EventListener<ClientPlayerTickEvent> tickListener = clientPlayerTickEvent -> {
        LivingEntity class_13092;
        LivingEntity class_13093;
        Entity class_12972 = RockstarClient.create().getFriendManager().getTargetEntity();
        LivingEntity class_13094 = class_13093 = class_12972 instanceof LivingEntity ? (class_13092 = (LivingEntity)class_12972) : null;
        if (class_13093 != null && ClientAccess.minecraftClient.player.fallDistance > 0.2f && ClientAccess.minecraftClient.player.distanceTo((Entity)class_13093) < 3.2f && this.savedPlayerState == null) {
            this.savedPlayerState = new EntityStateSnapshot(ClientAccess.minecraftClient.player.getPos(), ClientAccess.minecraftClient.player.getVelocity(), RockstarClient.create().getRotationManager().getEffectiveRotation(), ClientAccess.minecraftClient.player.isOnGround(), ClientAccess.minecraftClient.player.getBoundingBox());
        }
        if (this.savedPlayerState != null && this.savedPlayerState.getPosition().distanceTo(class_13093.getPos()) > 6.0 && ClientAccess.minecraftClient.player.isOnGround()) {
            this.savedPlayerState = null;
            ClientAccess.minecraftClient.player.setVelocity(this.savedPlayerState.getMotion());
            ClientAccess.minecraftClient.player.setPosition(this.savedPlayerState.getPosition());
            ClientAccess.minecraftClient.player.setOnGround(this.savedPlayerState.isOnGround());
            this.attackCount = 0;
        }
        if (ClientAccess.minecraftClient.player.isOnGround()) {
            // empty if block
        }
    };
    private final EventListener<InternalAttackEvent> attackListener = internalAttackEvent -> {
        if (internalAttackEvent.isCancelled()) {
            return;
        }
        ++this.attackCount;
    };
    private final EventListener<SendPacketEvent> movementPacketListener = sendPacketEvent -> {
        Packet<?> class_25962 = sendPacketEvent.getPacket();
        if (this.savedPlayerState != null && (class_25962 instanceof PlayerMoveC2SPacket || class_25962 instanceof PlayerMoveC2SPacket.Full || class_25962 instanceof PlayerMoveC2SPacket.PositionAndOnGround || class_25962 instanceof PlayerMoveC2SPacket.LookAndOnGround || class_25962 instanceof PlayerMoveC2SPacket.OnGroundOnly)) {
            sendPacketEvent.cancel();
        }
    };

    public void registerEventListeners() {
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    public void restoreStateAndUnregister() {
        if (this.savedPlayerState != null) {
            ClientAccess.minecraftClient.player.setVelocity(this.savedPlayerState.getMotion());
            ClientAccess.minecraftClient.player.setPosition(this.savedPlayerState.getPosition());
            ClientAccess.minecraftClient.player.setOnGround(this.savedPlayerState.isOnGround());
        }
        RockstarClient.create().getEventBus().unregisterListeners(this);
        this.savedPlayerState = null;
        this.attackCount = 0;
    }

    private Blink getBlinkModule() {
        return RockstarClient.create().getModuleRegistry().getModule(Blink.class);
    }

    @Generated
    public int getAttackCount() {
        return this.attackCount;
    }

    @Generated
    public EntityStateSnapshot getSavedPlayerState() {
        return this.savedPlayerState;
    }
}
