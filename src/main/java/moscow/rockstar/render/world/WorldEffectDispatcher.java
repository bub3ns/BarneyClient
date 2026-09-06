/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Vec3d
 *  net.minecraft.MathHelper
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package moscow.rockstar.render.world;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.render.postprocess.JumpCirclePostProcessor;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import pyrock.events.game.WorldChangeEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

public class WorldEffectDispatcher
implements ClientAccess {
    public static final WorldEffectDispatcher INSTANCE = new WorldEffectDispatcher();
    private final CopyOnWriteArrayList<WorldEffectState> activeEffects = new CopyOnWriteArrayList();
    private final JumpCirclePostProcessor jumpCircleRenderer = new JumpCirclePostProcessor();
    private final EventListener<WorldChangeEvent> worldChangeListener = worldChangeEvent -> this.activeEffects.clear();
    private final EventListener<Render3DEvent> render3DListener = render3DEvent -> {
        if (this.activeEffects.isEmpty()) {
            return;
        }
        List<JumpCirclePostProcessor.JumpCircleInstance> list = this.buildJumpCircleInstances((Render3DEvent)render3DEvent);
        if (list.isEmpty()) {
            return;
        }
        Matrix4f matrix4f = new Matrix4f((Matrix4fc)render3DEvent.getProjectionMatrix()).mul((Matrix4fc)render3DEvent.getPositionMatrix()).invert();
        this.jumpCircleRenderer.renderJumpCircles(matrix4f, list);
    };

    private WorldEffectDispatcher() {
        this.jumpCircleRenderer.refreshRenderOutput();
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    @Compile(obfuscation=4)
    public void initializeRenderer() {
    }

    public void addWorldEffect(Vec3d VanillaChestLootTableGenerator, float f, float f2) {
        this.addWorldEffectWithOpacity(VanillaChestLootTableGenerator, f, f2, null, 1.0f);
    }

    public void addColoredWorldEffect(Vec3d VanillaChestLootTableGenerator, float f, float f2, ColorRGBA colorRGBA) {
        this.addWorldEffectWithOpacity(VanillaChestLootTableGenerator, f, f2, colorRGBA, 1.0f);
    }

    public void addWorldEffectWithOpacity(Vec3d VanillaChestLootTableGenerator, float f, float f2, ColorRGBA colorRGBA, float f3) {
        if (WorldEffectDispatcher.minecraftClient.world == null) {
            return;
        }
        long l = (long)(f2 * 1000.0f);
        long l2 = (long)((float)l * 0.3f);
        long l3 = Math.max(1L, l - l2);
        this.activeEffects.add(new WorldEffectState(VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.y, VanillaChestLootTableGenerator.z, f, l2, l3, f3));
    }

    private List<JumpCirclePostProcessor.JumpCircleInstance> buildJumpCircleInstances(Render3DEvent render3DEvent) {
        ArrayList<JumpCirclePostProcessor.JumpCircleInstance> arrayList = new ArrayList<JumpCirclePostProcessor.JumpCircleInstance>();
        long l = System.currentTimeMillis();
        Vec3d VanillaChestLootTableGenerator = render3DEvent.getCamera().getPos();
        this.activeEffects.removeIf(worldEffectState -> l - worldEffectState.createdAtMillis > worldEffectState.fadeInDurationMillis + worldEffectState.displayDurationMillis);
        for (WorldEffectState worldEffectState2 : this.activeEffects) {
            float f;
            float f2;
            float f3;
            float f4;
            float f5;
            if (arrayList.size() >= 12) break;
            long l2 = l - worldEffectState2.createdAtMillis;
            long l3 = worldEffectState2.fadeInDurationMillis + worldEffectState2.displayDurationMillis;
            if (l3 <= 0L || (f5 = worldEffectState2.startRadius * (f4 = (float)Math.pow(f3 = MathHelper.clamp((float)((float)l2 / (float)l3), (float)0.0f, (float)1.0f), 0.6f))) <= 0.001f || (f2 = (f = 1.0f - MathHelper.clamp((float)((f3 - 0.4f) / 0.6f), (float)0.0f, (float)1.0f)) * worldEffectState2.opacity) <= 0.001f) continue;
            float f6 = MathHelper.clamp((float)(f5 * 0.12f), (float)0.5f, (float)1.2f);
            arrayList.add(new JumpCirclePostProcessor.JumpCircleInstance((float)(worldEffectState2.worldX - VanillaChestLootTableGenerator.x), (float)(worldEffectState2.worldY - VanillaChestLootTableGenerator.y), (float)(worldEffectState2.worldZ - VanillaChestLootTableGenerator.z), f5, f6, 1.0f, 1.0f, 1.0f, f, f2));
        }
        return arrayList;
    }

    static final class WorldEffectState {
        final double worldX;
        final double worldY;
        final double worldZ;
        final float startRadius;
        final long fadeInDurationMillis;
        final long displayDurationMillis;
        final float opacity;
        final long createdAtMillis;

        WorldEffectState(double d, double d2, double d3, float f, long l, long l2, float f2) {
            this.worldX = d;
            this.worldY = d2;
            this.worldZ = d3;
            this.startRadius = f;
            this.fadeInDurationMillis = l;
            this.displayDurationMillis = l2;
            this.opacity = f2;
            this.createdAtMillis = System.currentTimeMillis();
        }
    }
}
