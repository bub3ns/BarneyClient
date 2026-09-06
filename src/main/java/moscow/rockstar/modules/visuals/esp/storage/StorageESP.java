/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.Entity
 *  net.minecraft.ChestMinecartEntity
 *  net.minecraft.BlockView
 *  net.minecraft.Block
 *  net.minecraft.Box
 *  net.minecraft.Vec3d
 *  net.minecraft.ShulkerBoxBlock
 *  net.minecraft.BlockEntity
 *  net.minecraft.ChestBlockEntity
 *  net.minecraft.DispenserBlockEntity
 *  net.minecraft.DropperBlockEntity
 *  net.minecraft.EnderChestBlockEntity
 *  net.minecraft.HopperBlockEntity
 *  net.minecraft.ShulkerBoxBlockEntity
 *  net.minecraft.TrappedChestBlockEntity
 *  net.minecraft.VoxelShape
 *  net.minecraft.BlockState
 *  net.minecraft.BufferBuilder
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.BarrelBlockEntity
 *  net.minecraft.FurnaceBlockEntity
 *  net.minecraft.Camera
 *  net.minecraft.MatrixStack
 */
package moscow.rockstar.modules.visuals.esp.storage;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.util.RenderUtils;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.util.Timer;
import moscow.rockstar.entity.tracking.BlockEntityTracker;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.world.BlockView;
import net.minecraft.block.Block;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.util.DyeColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.events.game.WorldChangeEvent;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Storage ESP", category=ModuleCategory.VISUALS)
public class StorageESP
extends Module {
    private static final Box FULL_BLOCK_BOX = new Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
    private static final Box EMPTY_BLOCK_BOX = new Box(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    private static final long SCAN_INTERVAL_MILLIS = 1000L;
    private final Timer scanTimer = new Timer();
    private volatile List<StorageRenderEntry> storageRenderEntries = new ArrayList<StorageRenderEntry>();
    private volatile List<StorageBlockEntry> storageBlockEntries = new ArrayList<StorageBlockEntry>();
    private MultiBooleanSetting blockTypeSettings;
    private MultiBooleanSetting.Option chestOption;
    private MultiBooleanSetting.Option enderChestOption;
    private MultiBooleanSetting.Option trappedChestOption;
    private MultiBooleanSetting.Option furnaceOption;
    private MultiBooleanSetting.Option barrelOption;
    private MultiBooleanSetting.Option minecartOption;
    private MultiBooleanSetting.Option shulkerOption;
    private MultiBooleanSetting.Option dropperOption;
    private MultiBooleanSetting.Option dispenserOption;
    private MultiBooleanSetting.Option hopperOption;
    private MultiBooleanSetting renderModeSettings;
    private MultiBooleanSetting.Option fillRenderOption;
    private MultiBooleanSetting.Option outlineRenderOption;
    private MultiBooleanSetting.Option diagonalRenderOption;
    private MultiBooleanSetting.Option lineRenderOption;
    private NumberSetting maxDistanceSetting;
    private final EventListener<Render3DEvent> worldRenderListener = render3DEvent -> {
        if (StorageESP.minecraftClient.world == null || StorageESP.minecraftClient.player == null) {
            return;
        }
        MatrixStack class_45872 = render3DEvent.getMatrices();
        Camera class_41842 = StorageESP.minecraftClient.gameRenderer.getCamera();
        Vec3d VanillaChestLootTableGenerator = class_41842.getPos();
        List<StorageRenderEntry> list = this.storageRenderEntries;
        List<StorageBlockEntry> list2 = this.storageBlockEntries;
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (StorageRenderEntry iterator : list) {
            for (Box HorizontalFacingBlock : iterator.boundingBoxes) {
                if (!this.fillRenderOption.isSelected()) continue;
                RenderUtils.drawFilledBox(class_45872, class_2872, HorizontalFacingBlock.offset(-VanillaChestLootTableGenerator.getX(), -VanillaChestLootTableGenerator.getY(), -VanillaChestLootTableGenerator.getZ()), iterator.color.withAlpha(50.0f));
            }
        }
        for (StorageBlockEntry storageBlockEntry : list2) {
            if (!this.fillRenderOption.isSelected()) continue;
            RenderUtils.drawFilledBox(class_45872, class_2872, storageBlockEntry.boundingBox.offset(-VanillaChestLootTableGenerator.getX(), -VanillaChestLootTableGenerator.getY(), -VanillaChestLootTableGenerator.getZ()), storageBlockEntry.color.withAlpha(50.0f));
        }
        ItemRenderUtils.flushVertexConsumer(class_2872);
        BufferBuilder outlineBuffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (StorageRenderEntry storageRenderEntry : list) {
            for (Box InfestedBlock : storageRenderEntry.boundingBoxes) {
                if (this.diagonalRenderOption.isSelected()) {
                    RenderUtils.drawBoxCorners(class_45872, outlineBuffer, InfestedBlock.offset(-VanillaChestLootTableGenerator.getX(), -VanillaChestLootTableGenerator.getY(), -VanillaChestLootTableGenerator.getZ()), storageRenderEntry.color.withAlpha(100.0f));
                }
                if (this.outlineRenderOption.isSelected()) {
                    RenderUtils.drawBoxOutline(class_45872, outlineBuffer, InfestedBlock.offset(-VanillaChestLootTableGenerator.getX(), -VanillaChestLootTableGenerator.getY(), -VanillaChestLootTableGenerator.getZ()), storageRenderEntry.color.withAlpha(100.0f));
                }
                if (!this.lineRenderOption.isSelected()) continue;
                RenderUtils.drawWorldLineToPoint(class_45872, outlineBuffer, storageRenderEntry.centerPos, storageRenderEntry.color);
            }
        }
        for (StorageBlockEntry storageBlockEntry : list2) {
            if (this.diagonalRenderOption.isSelected()) {
                RenderUtils.drawBoxCorners(class_45872, outlineBuffer, storageBlockEntry.boundingBox.offset(-VanillaChestLootTableGenerator.getX(), -VanillaChestLootTableGenerator.getY(), -VanillaChestLootTableGenerator.getZ()), storageBlockEntry.color.withAlpha(100.0f));
            }
            if (this.outlineRenderOption.isSelected()) {
                RenderUtils.drawBoxOutline(class_45872, outlineBuffer, storageBlockEntry.boundingBox.offset(-VanillaChestLootTableGenerator.getX(), -VanillaChestLootTableGenerator.getY(), -VanillaChestLootTableGenerator.getZ()), storageBlockEntry.color.withAlpha(100.0f));
            }
            if (!this.lineRenderOption.isSelected()) continue;
            RenderUtils.drawWorldLineToPoint(class_45872, outlineBuffer, storageBlockEntry.position, storageBlockEntry.color);
        }
        ItemRenderUtils.flushVertexConsumer(outlineBuffer);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    };
    private final EventListener<WorldChangeEvent> worldChangeListener = worldChangeEvent -> {
        BlockEntityTracker.clear();
        this.storageRenderEntries = new ArrayList<StorageRenderEntry>();
        this.storageBlockEntries = new ArrayList<StorageBlockEntry>();
    };
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        Object object;
        ColorRGBA colorRGBA;
        if (StorageESP.minecraftClient.world == null || StorageESP.minecraftClient.player == null) {
            return;
        }
        if (!this.scanTimer.hasElapsed(1000L)) {
            return;
        }
        ArrayList<StorageRenderEntry> arrayList = new ArrayList<StorageRenderEntry>();
        for (BlockEntity object2 : BlockEntityTracker.getTrackedEntities()) {
            if (!this.shouldRenderStorageBlock((BlockEntity)object2)) continue;
            List<Box> boundingBoxes = this.getStorageBoundingBoxes(object2);
            colorRGBA = this.getStorageBlockColor((BlockEntity)object2);
            object = object2.getPos().toCenterPos();
            arrayList.add(new StorageRenderEntry(boundingBoxes, colorRGBA, (Vec3d)object));
        }
        ArrayList<StorageBlockEntry> arrayList2 = new ArrayList<StorageBlockEntry>();
        for (Entity class_12972 : StorageESP.minecraftClient.world.getEntities()) {
            if (!this.shouldRenderStorageEntity(class_12972)) continue;
            Box boundingBox = class_12972.getBoundingBox();
            colorRGBA = this.getStorageEntityColor(class_12972);
            Vec3d VanillaChestLootTableGenerator = class_12972.getPos();
            arrayList2.add(new StorageBlockEntry(boundingBox, colorRGBA, VanillaChestLootTableGenerator));
        }
        this.storageRenderEntries = arrayList;
        this.storageBlockEntries = arrayList2;
        this.scanTimer.reset();
    };

    public StorageESP() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.blockTypeSettings = new MultiBooleanSetting(this, "modules.settings.storage_esp.blocks");
        this.chestOption = new MultiBooleanSetting.Option(this.blockTypeSettings, "modules.settings.storage_esp.blocks.chests").select();
        this.enderChestOption = new MultiBooleanSetting.Option(this.blockTypeSettings, "modules.settings.storage_esp.blocks.ender_chests").select();
        this.trappedChestOption = new MultiBooleanSetting.Option(this.blockTypeSettings, "modules.settings.storage_esp.blocks.trapped_chests");
        this.furnaceOption = new MultiBooleanSetting.Option(this.blockTypeSettings, "modules.settings.storage_esp.blocks.furnaces");
        this.barrelOption = new MultiBooleanSetting.Option(this.blockTypeSettings, "modules.settings.storage_esp.blocks.barrels").select();
        this.minecartOption = new MultiBooleanSetting.Option(this.blockTypeSettings, "modules.settings.storage_esp.blocks.minecart").select();
        this.shulkerOption = new MultiBooleanSetting.Option(this.blockTypeSettings, "modules.settings.storage_esp.blocks.shulkers").select();
        this.dropperOption = new MultiBooleanSetting.Option(this.blockTypeSettings, "modules.settings.storage_esp.blocks.droppers");
        this.dispenserOption = new MultiBooleanSetting.Option(this.blockTypeSettings, "modules.settings.storage_esp.blocks.dispensers");
        this.hopperOption = new MultiBooleanSetting.Option(this.blockTypeSettings, "modules.settings.storage_esp.blocks.hoppers");
        this.renderModeSettings = new MultiBooleanSetting(this, "modules.settings.storage_esp.render");
        this.fillRenderOption = new MultiBooleanSetting.Option(this.renderModeSettings, "modules.settings.storage_esp.render.fill").select();
        this.outlineRenderOption = new MultiBooleanSetting.Option(this.renderModeSettings, "modules.settings.storage_esp.render.outline").select();
        this.diagonalRenderOption = new MultiBooleanSetting.Option(this.renderModeSettings, "modules.settings.storage_esp.render.diagonals").select();
        this.lineRenderOption = new MultiBooleanSetting.Option(this.renderModeSettings, "modules.settings.storage_esp.render.lines");
        this.maxDistanceSetting = new NumberSetting((SettingOwner)this, "modules.settings.storage_esp.max_distance", "modules.settings.storage_esp.max_distance.description").setMinValue(5.0f).setMaxValue(128.0f).setStep(1.0f).setValue(128.0f);
    }

    private List<Box> getStorageBoundingBoxes(BlockEntity class_25862) {
        if (StorageESP.minecraftClient.world == null) {
            return List.of(EMPTY_BLOCK_BOX);
        }
        BlockPos adminsky = class_25862.getPos();
        BlockState class_26802 = StorageESP.minecraftClient.world.getBlockState(adminsky);
        VoxelShape class_2652 = class_26802.getOutlineShape((BlockView)StorageESP.minecraftClient.world, adminsky);
        if (class_2652.isEmpty()) {
            return List.of(FULL_BLOCK_BOX.offset(adminsky));
        }
        return class_2652.getBoundingBoxes().stream().map(HorizontalFacingBlock -> HorizontalFacingBlock.offset(adminsky)).toList();
    }

    private boolean shouldRenderStorageBlock(BlockEntity class_25862) {
        double d = this.maxDistanceSetting.getValue() * this.maxDistanceSetting.getValue();
        if (StorageESP.minecraftClient.player == null || StorageESP.minecraftClient.player.squaredDistanceTo(class_25862.getPos().toCenterPos()) > d) {
            return false;
        }
        if (class_25862 instanceof ChestBlockEntity && this.chestOption.isSelected()) {
            return true;
        }
        if (class_25862 instanceof EnderChestBlockEntity && this.enderChestOption.isSelected()) {
            return true;
        }
        if (class_25862 instanceof TrappedChestBlockEntity && this.trappedChestOption.isSelected()) {
            return true;
        }
        if (class_25862 instanceof FurnaceBlockEntity && this.furnaceOption.isSelected()) {
            return true;
        }
        if (class_25862 instanceof BarrelBlockEntity && this.barrelOption.isSelected()) {
            return true;
        }
        if (class_25862 instanceof ShulkerBoxBlockEntity && this.shulkerOption.isSelected()) {
            return true;
        }
        if (class_25862 instanceof DropperBlockEntity && this.dropperOption.isSelected()) {
            return true;
        }
        if (class_25862 instanceof DispenserBlockEntity && this.dispenserOption.isSelected()) {
            return true;
        }
        return class_25862 instanceof HopperBlockEntity && this.hopperOption.isSelected();
    }

    private ColorRGBA getStorageBlockColor(BlockEntity class_25862) {
        if (class_25862 instanceof ChestBlockEntity) {
            return new ColorRGBA(255.0f, 131.0f, 54.0f);
        }
        if (class_25862 instanceof EnderChestBlockEntity) {
            return new ColorRGBA(121.0f, 54.0f, 255.0f);
        }
        if (class_25862 instanceof TrappedChestBlockEntity) {
            return new ColorRGBA(255.0f, 101.0f, 54.0f);
        }
        if (class_25862 instanceof FurnaceBlockEntity) {
            return new ColorRGBA(126.0f, 126.0f, 126.0f);
        }
        if (class_25862 instanceof BarrelBlockEntity) {
            return new ColorRGBA(255.0f, 185.0f, 54.0f);
        }
        if (class_25862 instanceof ShulkerBoxBlockEntity) {
            Block class_22482 = class_25862.getCachedState().getBlock();
            if (class_22482 instanceof ShulkerBoxBlock shulkerBox) {
                DyeColor dyeColor = shulkerBox.getColor();
                if (dyeColor != null) {
                    return ColorRGBA.fromInt(dyeColor.getEntityColor());
                }
            }
            return new ColorRGBA(181.0f, 54.0f, 255.0f);
        }
        if (class_25862 instanceof DropperBlockEntity) {
            return new ColorRGBA(100.0f, 100.0f, 100.0f);
        }
        if (class_25862 instanceof DispenserBlockEntity) {
            return new ColorRGBA(100.0f, 100.0f, 100.0f);
        }
        if (class_25862 instanceof HopperBlockEntity) {
            return new ColorRGBA(100.0f, 100.0f, 100.0f);
        }
        return ColorPalette.WHITE;
    }

    private ColorRGBA getStorageEntityColor(Entity class_12972) {
        if (class_12972 instanceof ChestMinecartEntity) {
            return new ColorRGBA(255.0f, 200.0f, 100.0f);
        }
        return ColorPalette.WHITE;
    }

    private boolean shouldRenderStorageEntity(Entity class_12972) {
        double d = this.maxDistanceSetting.getValue() * this.maxDistanceSetting.getValue();
        if (StorageESP.minecraftClient.player == null || StorageESP.minecraftClient.player.squaredDistanceTo(class_12972.getPos()) > d) {
            return false;
        }
        return class_12972 instanceof ChestMinecartEntity && this.minecartOption.isSelected();
    }

    static final class StorageRenderEntry {
        final List<Box> boundingBoxes;
        final ColorRGBA color;
        final Vec3d centerPos;

        StorageRenderEntry(List<Box> list, ColorRGBA colorRGBA, Vec3d VanillaChestLootTableGenerator) {
            this.boundingBoxes = list;
            this.color = colorRGBA;
            this.centerPos = VanillaChestLootTableGenerator;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "boundingBoxes", "color", "centerPos");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "boundingBoxes", "color", "centerPos");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "boundingBoxes", "color", "centerPos");
        }

        public List<Box> getBoundingBoxes() {
            return this.boundingBoxes;
        }

        public ColorRGBA getColor() {
            return this.color;
        }

        public Vec3d getCenterPos() {
            return this.centerPos;
        }
    }

    static final class StorageBlockEntry {
        final Box boundingBox;
        final ColorRGBA color;
        final Vec3d position;

        StorageBlockEntry(Box HorizontalFacingBlock, ColorRGBA colorRGBA, Vec3d VanillaChestLootTableGenerator) {
            this.boundingBox = HorizontalFacingBlock;
            this.color = colorRGBA;
            this.position = VanillaChestLootTableGenerator;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "boundingBox", "color", "position");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "boundingBox", "color", "position");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "boundingBox", "color", "position");
        }

        public Box getBoundingBox() {
            return this.boundingBox;
        }

        public ColorRGBA getColor() {
            return this.color;
        }

        public Vec3d getPosition() {
            return this.position;
        }
    }
}
