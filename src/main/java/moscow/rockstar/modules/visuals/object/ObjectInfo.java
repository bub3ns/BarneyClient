/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.Vec3d
 *  net.minecraft.PlaySoundS2CPacket
 *  net.minecraft.BufferBuilder
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.Identifier
 *  net.minecraft.MatrixStack
 */
package moscow.rockstar.modules.visuals.object;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.modules.visuals.object.ObjectMarker;
import moscow.rockstar.modules.visuals.object.ObjectPosition;
import moscow.rockstar.modules.visuals.object.ObjectType;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.util.Timer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.events.game.WorldChangeEvent;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.render.PreHudRenderEvent;
import pyrock.events.render.Render3DEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Object Info", category=ModuleCategory.VISUALS, description="modules.descriptions.object_info")
public class ObjectInfo
extends Module {
    private ModeSetting particleMode;
    private ModeSetting.Option gravity;
    private ModeSetting.Option scatter;
    private final Map<BlockPos, ObjectPosition> savedObjectStates = new HashMap<BlockPos, ObjectPosition>();
    private final List<ObjectMarker> objectEntries = new ArrayList<ObjectMarker>();
    static final Random random = new Random();
    private final Timer cooldownTimer = new Timer();
    private final EventListener<ReceivePacketEvent> onReceivePacketEvent = receivePacketEvent -> {
        Object object = receivePacketEvent.getPacket();
        if (!(object instanceof PlaySoundS2CPacket)) {
            return;
        }
        PlaySoundS2CPacket class_27672 = (PlaySoundS2CPacket)object;
        object = class_27672.getSound().getIdAsString();
        if (ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME)) {
            BlockPos adminsky;
            if (!(!((String)object).contains("minecraft:block.anvil.place") || class_27672.getVolume() != 0.5f && class_27672.getVolume() != 0.7f || class_27672.getPitch() != 1.1f && class_27672.getPitch() != 0.5f)) {
                adminsky = new BlockPos((int)class_27672.getX(), (int)class_27672.getY(), (int)class_27672.getZ());
                this.savedObjectStates.put(adminsky, new ObjectPosition(adminsky.up().add(0, 0, 0), ObjectType.PLASTIC));
            }
            if ((((String)object).contains("minecraft:entity.wither.break_block") || ((String)object).contains("minecraft:block.anvil.place")) && class_27672.getVolume() == 0.7f || class_27672.getVolume() == 0.2f && class_27672.getPitch() == 1.0f) {
                adminsky = new BlockPos((int)class_27672.getX(), (int)class_27672.getY(), (int)class_27672.getZ());
                this.savedObjectStates.put(adminsky, new ObjectPosition(adminsky.up().add(0, 0, 0), ObjectType.TRAP));
            }
        } else if (ServerDetector.isInventoryServer()) {
            BlockPos adminsky;
            if (((String)object).contains("minecraft:entity.generic.explode") && (class_27672.getVolume() == 1.0f || class_27672.getPitch() == 1.0f)) {
                adminsky = new BlockPos((int)class_27672.getX(), (int)class_27672.getY(), (int)class_27672.getZ());
                this.savedObjectStates.put(adminsky, new ObjectPosition(adminsky, ObjectType.STUN));
                this.savedObjectStates.put(adminsky.up(), new ObjectPosition(adminsky.up(), ObjectType.BOOM_TRAP));
            }
            if (((String)object).contains("minecraft:block.beacon.deactivate") && ((double)class_27672.getVolume() == 1.5 || class_27672.getPitch() == 1.0f)) {
                adminsky = new BlockPos((int)class_27672.getX(), (int)class_27672.getY(), (int)class_27672.getZ());
                this.savedObjectStates.put(adminsky, new ObjectPosition(adminsky, ObjectType.STUN));
            }
        }
    };
    private final EventListener<PreHudRenderEvent> onPreHudRenderEvent = preHudRenderEvent -> {
        try {
            for (ObjectPosition objectPosition : this.savedObjectStates.values()) {
                objectPosition.renderHudTimer((PreHudRenderEvent)preHudRenderEvent);
            }
        }
        catch (ConcurrentModificationException concurrentModificationException) {
            // empty catch block
        }
    };
    private final EventListener<Render3DEvent> onRender3DEvent = render3DEvent -> {
        MatrixStack class_45872 = render3DEvent.getMatrices();
        class_45872.push();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        Identifier class_29602 = RockstarClient.resourceId("textures/bloom.png");
        RenderSystem.setShaderTexture((int)0, (Identifier)class_29602);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        BlockPos adminsky = null;
        try {
            for (Map.Entry object : this.savedObjectStates.entrySet()) {
                ObjectPosition objectPosition = (ObjectPosition)object.getValue();
                class_45872.push();
                objectPosition.onRender3D((Render3DEvent)render3DEvent, class_2872);
                class_45872.pop();
                if (!objectPosition.lifetimeTimer.hasElapsed(objectPosition.getObjectType().getLifetimeMillis())) continue;
                this.processObjectMarker(objectPosition);
                adminsky = (BlockPos)object.getKey();
            }
        }
        catch (ConcurrentModificationException concurrentModificationException) {
            // empty catch block
        }
        Iterator<ObjectMarker> iterator = this.objectEntries.iterator();
        while (iterator.hasNext()) {
            ObjectMarker objectMarker = iterator.next();
            if (objectMarker.isExpired()) {
                iterator.remove();
                continue;
            }
            objectMarker.updatePhysics();
            objectMarker.render((Render3DEvent)render3DEvent, class_2872);
        }
        if (adminsky != null) {
            this.savedObjectStates.remove(adminsky);
        }
        ItemRenderUtils.flushVertexConsumer(class_2872);
        RenderSystem.depthMask((boolean)true);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        class_45872.pop();
    };
    private final EventListener<WorldChangeEvent> onWorldChangeEvent = worldChangeEvent -> {
        this.savedObjectStates.clear();
        this.objectEntries.clear();
    };

    public ObjectInfo() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.particleMode = new ModeSetting(this, "modules.settings.object_info.particleMode");
        this.gravity = new ModeSetting.Option(this.particleMode, "modules.settings.object_info.particleMode.gravity");
        this.scatter = new ModeSetting.Option(this.particleMode, "modules.settings.object_info.particleMode.scatter");
    }

    private void processObjectMarker(ObjectPosition objectPosition) {
        float[][] fArrayArray;
        if (objectPosition.objectType != ObjectType.STUN) {
            return;
        }
        Vec3d VanillaChestLootTableGenerator = objectPosition.blockPosition.toCenterPos();
        for (float[] fArray : fArrayArray = new float[][]{{15.0f, -15.0f, 15.0f, 0.0f, 1.0f, 0.0f}, {-15.0f, -15.0f, 15.0f, 0.0f, 1.0f, 0.0f}, {15.0f, -15.0f, -15.0f, 0.0f, 1.0f, 0.0f}, {-15.0f, -15.0f, -15.0f, 0.0f, 1.0f, 0.0f}, {-15.0f, 15.0f, 15.0f, 1.0f, 0.0f, 0.0f}, {-15.0f, -15.0f, 15.0f, 1.0f, 0.0f, 0.0f}, {-15.0f, 15.0f, -15.0f, 1.0f, 0.0f, 0.0f}, {-15.0f, -15.0f, -15.0f, 1.0f, 0.0f, 0.0f}, {15.0f, 15.0f, -15.0f, 0.0f, 0.0f, 1.0f}, {-15.0f, 15.0f, -15.0f, 0.0f, 0.0f, 1.0f}, {15.0f, -15.0f, -15.0f, 0.0f, 0.0f, 1.0f}, {-15.0f, -15.0f, -15.0f, 0.0f, 0.0f, 1.0f}}) {
            for (float f = 0.0f; f < 30.0f; f += 0.2f) {
                float f2 = fArray[0] + f * fArray[3];
                float f3 = fArray[1] + f * fArray[4];
                float f4 = fArray[2] + f * fArray[5];
                float f5 = 0.005f;
                float f6 = (random.nextFloat() - 0.5f) * 2.0f * f5;
                float f7 = (random.nextFloat() - 0.5f) * 2.0f * f5;
                float f8 = (random.nextFloat() - 0.5f) * 2.0f * f5;
                this.objectEntries.add(new ObjectMarker(this, VanillaChestLootTableGenerator, f2, f3, f4, f6, f7, f8, this.particleMode.isSelected(this.gravity)));
            }
        }
    }
}
