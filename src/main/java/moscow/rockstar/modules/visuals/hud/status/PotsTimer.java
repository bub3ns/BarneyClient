/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.Hand
 *  net.minecraft.ScreenHandler
 *  net.minecraft.BrewingStandScreenHandler
 *  net.minecraft.AbstractFurnaceScreenHandler
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Direction
 *  net.minecraft.Vec3i
 *  net.minecraft.Vec2f
 *  net.minecraft.Vec3d
 *  net.minecraft.BlockEntity
 *  net.minecraft.BrewingStandBlockEntity
 *  net.minecraft.Packet
 *  net.minecraft.CloseHandledScreenC2SPacket
 *  net.minecraft.MathHelper
 *  net.minecraft.BlastFurnaceBlockEntity
 *  net.minecraft.SmokerBlockEntity
 *  net.minecraft.FurnaceBlockEntity
 *  net.minecraft.BlastFurnaceScreen
 *  net.minecraft.FurnaceScreen
 *  net.minecraft.SmokerScreen
 *  net.minecraft.BlockHitResult
 *  net.minecraft.MatrixStack
 *  net.minecraft.BrewingStandScreen
 */
package moscow.rockstar.modules.visuals.hud.status;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.modules.visuals.hud.status.PotionCategory;
import moscow.rockstar.modules.visuals.hud.status.PotionEffectEntry;
import moscow.rockstar.modules.visuals.hud.status.PotionType;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.util.ProjectionUtils;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.util.Timer;
import net.minecraft.util.Hand;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.block.entity.SmokerBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.client.gui.screen.ingame.BlastFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.FurnaceScreen;
import net.minecraft.client.gui.screen.ingame.SmokerScreen;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.gui.screen.ingame.BrewingStandScreen;
import pyrock.events.game.WorldChangeEvent;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.render.PreHudRenderEvent;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Pots Timer", category=ModuleCategory.VISUALS, description="modules.descriptions.pots_timer")
public class PotsTimer
extends Module {
    private BooleanSetting furnaces;
    private BooleanSetting blastFurnaces;
    private BooleanSetting smokers;
    private BooleanSetting brewing;
    private NumberSetting range;
    private NumberSetting scale;
    private final Map<BlockPos, PotionEffectEntry> savedPotionTimers = new ConcurrentHashMap<BlockPos, PotionEffectEntry>();
    private final LinkedHashSet<BlockPos> trackedPotionBlocks = new LinkedHashSet();
    private PotionCategory potionCategory = PotionCategory.IDLE;
    private BlockPos currentPotionBlock;
    private long lastPotionUpdate;
    private long nextPotionUpdate;
    private int keyCode = -1;
    private final Timer cooldownTimer = new Timer();
    private final EventListener<WorldChangeEvent> onWorldChangeEvent = worldChangeEvent -> {
        this.resetPotionState();
        this.savedPotionTimers.clear();
        this.trackedPotionBlocks.clear();
    };
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = clientPlayerTickEvent -> {
        if (PotsTimer.minecraftClient.player == null || PotsTimer.minecraftClient.world == null) {
            return;
        }
        if (this.cooldownTimer.hasElapsed(800L)) {
            this.updatePotionTimers();
            this.cooldownTimer.reset();
        }
        long l = System.currentTimeMillis();
        long l2 = 20000L;
        for (Map.Entry<BlockPos, PotionEffectEntry> entry : this.savedPotionTimers.entrySet()) {
            PotionEffectEntry potionEffectEntry = entry.getValue();
            potionEffectEntry.updateTimestamp(l);
            if (l - potionEffectEntry.startTimeMillis <= l2) continue;
            this.trackedPotionBlocks.add(entry.getKey());
        }
        this.resetPotionTimers();
    };
    private final EventListener<PreHudRenderEvent> onPreHudRenderEvent = preHudRenderEvent -> {
        if (PotsTimer.minecraftClient.player == null || PotsTimer.minecraftClient.world == null) {
            return;
        }
        CustomDrawContext customDrawContext = preHudRenderEvent.getContext();
        for (Map.Entry<BlockPos, PotionEffectEntry> entry : this.savedPotionTimers.entrySet()) {
            BlockPos adminsky = entry.getKey();
            PotionEffectEntry potionEffectEntry = entry.getValue();
            this.drawPotionTimer(customDrawContext, adminsky, potionEffectEntry);
        }
    };

    public PotsTimer() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.furnaces = new BooleanSetting(this, "modules.settings.pots_timer.furnaces").enable();
        this.blastFurnaces = new BooleanSetting(this, "modules.settings.pots_timer.blast_furnaces").enable();
        this.smokers = new BooleanSetting(this, "modules.settings.pots_timer.smokers").enable();
        this.brewing = new BooleanSetting(this, "modules.settings.pots_timer.brewing").enable();
        this.range = new NumberSetting(this, "modules.settings.pots_timer.range").setMinValue(4.0f).setMaxValue(32.0f).setStep(1.0f).setValue(16.0f);
        this.scale = new NumberSetting(this, "modules.settings.pots_timer.scale").setMinValue(0.3f).setMaxValue(1.3f).setStep(0.05f).setValue(0.4f);
    }

    @Override
    public final void onDisable() {
        super.onDisable();
        this.resetPotionState();
        this.savedPotionTimers.clear();
        this.trackedPotionBlocks.clear();
    }

    private void updatePotionTimers() {
        int n = (int)Math.ceil(this.range.getValue());
        BlockPos adminsky2 = PotsTimer.minecraftClient.player.getBlockPos();
        double d = this.range.getValue() * this.range.getValue();
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        for (int i = -n; i <= n; ++i) {
            for (int j = -n; j <= n; ++j) {
                for (int k = -n; k <= n; ++k) {
                    BlockEntity class_25862;
                    BlockPos adminsky3 = adminsky2.add(i, j, k);
                    if (adminsky2.getSquaredDistance((Vec3i)adminsky3) > d || !this.isPotionContainer(class_25862 = PotsTimer.minecraftClient.world.getBlockEntity(adminsky3))) continue;
                    arrayList.add(adminsky3);
                    if (this.savedPotionTimers.containsKey(adminsky3)) continue;
                    PotionEffectEntry potionEffectEntry = new PotionEffectEntry(this.getPotionType(class_25862));
                    this.savedPotionTimers.put(adminsky3, potionEffectEntry);
                    this.trackedPotionBlocks.add(adminsky3);
                }
            }
        }
        this.savedPotionTimers.keySet().removeIf(adminsky -> {
            if (arrayList.contains(adminsky)) {
                return false;
            }
            this.trackedPotionBlocks.remove(adminsky);
            return true;
        });
    }

    private boolean isPotionContainer(BlockEntity class_25862) {
        if (class_25862 == null) {
            return false;
        }
        if (class_25862 instanceof FurnaceBlockEntity) {
            return this.furnaces.isEnabled();
        }
        if (class_25862 instanceof BlastFurnaceBlockEntity) {
            return this.blastFurnaces.isEnabled();
        }
        if (class_25862 instanceof SmokerBlockEntity) {
            return this.smokers.isEnabled();
        }
        if (class_25862 instanceof BrewingStandBlockEntity) {
            return this.brewing.isEnabled();
        }
        return false;
    }

    private PotionType getPotionType(BlockEntity class_25862) {
        if (class_25862 instanceof BlastFurnaceBlockEntity) {
            return PotionType.BLAST_FURNACE;
        }
        if (class_25862 instanceof SmokerBlockEntity) {
            return PotionType.SMOKER;
        }
        if (class_25862 instanceof BrewingStandBlockEntity) {
            return PotionType.BREWING_STAND;
        }
        return PotionType.FURNACE;
    }

    private void resetPotionTimers() {
        long l = System.currentTimeMillis();
        int n = -1;
        switch (this.potionCategory.ordinal()) {
            case 0: {
                if (PotsTimer.minecraftClient.currentScreen != null) {
                    return;
                }
                if (PotsTimer.minecraftClient.player.currentScreenHandler != PotsTimer.minecraftClient.player.playerScreenHandler) {
                    return;
                }
                if (this.trackedPotionBlocks.isEmpty()) {
                    return;
                }
                BlockPos adminsky = this.trackedPotionBlocks.getFirst();
                this.trackedPotionBlocks.remove(adminsky);
                PotionEffectEntry potionEffectEntry = this.savedPotionTimers.get(adminsky);
                if (potionEffectEntry == null) {
                    return;
                }
                if (!this.isPotionBlock(adminsky)) {
                    return;
                }
                this.currentPotionBlock = adminsky;
                this.lastPotionUpdate = l;
                this.nextPotionUpdate = 0L;
                this.keyCode = -1;
                n = -1;
                Vec3d VanillaChestLootTableGenerator = Vec3d.ofCenter((Vec3i)adminsky);
                BlockHitResult class_39652 = new BlockHitResult(VanillaChestLootTableGenerator, Direction.UP, adminsky, false);
                PotsTimer.minecraftClient.interactionManager.interactBlock(PotsTimer.minecraftClient.player, Hand.MAIN_HAND, class_39652);
                this.potionCategory = PotionCategory.OPENING;
                break;
            }
            case 1: {
                if (l - this.lastPotionUpdate > 1500L) {
                    this.resetPotionDisplay(false);
                    return;
                }
                if (!this.isPotionContainerOpen()) break;
                this.keyCode = this.getPotionDuration();
                this.nextPotionUpdate = l;
                this.potionCategory = PotionCategory.BREWING;
                break;
            }
            case 2: {
                if (l - this.lastPotionUpdate > 1500L) {
                    this.resetPotionDisplay(this.keyCode != -1);
                    return;
                }
                if (!this.isPotionContainerOpen()) {
                    this.resetPotionDisplay(false);
                    return;
                }
                if (l - this.nextPotionUpdate < 220L) {
                    return;
                }
                n = this.getPotionDuration();
                ItemStack class_17992 = this.getPotionStack();
                PotionEffectEntry potionEffectEntry = this.savedPotionTimers.get(this.currentPotionBlock);
                if (potionEffectEntry != null) {
                    potionEffectEntry.updateProgress(this.keyCode, n, class_17992, l);
                }
                this.potionCategory = PotionCategory.COLLECTING;
                break;
            }
            case 3: {
                if (PotsTimer.minecraftClient.player.networkHandler != null) {
                    PotsTimer.minecraftClient.player.networkHandler.sendPacket((Packet)new CloseHandledScreenC2SPacket(PotsTimer.minecraftClient.player.currentScreenHandler.syncId));
                    PotsTimer.minecraftClient.player.currentScreenHandler = PotsTimer.minecraftClient.player.playerScreenHandler;
                }
                this.potionCategory = PotionCategory.IDLE;
                this.currentPotionBlock = null;
            }
        }
    }

    private boolean isPotionContainerOpen() {
        if (PotsTimer.minecraftClient.player == null || PotsTimer.minecraftClient.player.currentScreenHandler == null) {
            return false;
        }
        PotionEffectEntry potionEffectEntry = this.savedPotionTimers.get(this.currentPotionBlock);
        if (potionEffectEntry == null) {
            return false;
        }
        return switch (potionEffectEntry.potionType.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0, 1, 2 -> PotsTimer.minecraftClient.player.currentScreenHandler instanceof AbstractFurnaceScreenHandler;
            case 3 -> PotsTimer.minecraftClient.player.currentScreenHandler instanceof BrewingStandScreenHandler;
        };
    }

    private int getPotionDuration() {
        PotionEffectEntry potionEffectEntry = this.savedPotionTimers.get(this.currentPotionBlock);
        if (potionEffectEntry == null) {
            return -1;
        }
        return switch (potionEffectEntry.potionType.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0, 1, 2 -> {
                ScreenHandler var3_2 = PotsTimer.minecraftClient.player.currentScreenHandler;
                if (var3_2 instanceof AbstractFurnaceScreenHandler) {
                    AbstractFurnaceScreenHandler var2_4 = (AbstractFurnaceScreenHandler)var3_2;
                    yield Math.round(var2_4.getCookProgress() * (float)potionEffectEntry.durationTicks);
                }
                yield -1;
            }
            case 3 -> {
                ScreenHandler var3_3 = PotsTimer.minecraftClient.player.currentScreenHandler;
                if (var3_3 instanceof BrewingStandScreenHandler) {
                    BrewingStandScreenHandler var2_5 = (BrewingStandScreenHandler)var3_3;
                    yield var2_5.getBrewTime();
                }
                yield -1;
            }
        };
    }

    private ItemStack getPotionStack() {
        PotionEffectEntry potionEffectEntry = this.savedPotionTimers.get(this.currentPotionBlock);
        if (potionEffectEntry == null) {
            return ItemStack.EMPTY;
        }
        try {
            return switch (potionEffectEntry.potionType.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0, 1, 2 -> {
                    ScreenHandler var3_2 = PotsTimer.minecraftClient.player.currentScreenHandler;
                    if (var3_2 instanceof AbstractFurnaceScreenHandler) {
                        AbstractFurnaceScreenHandler var2_4 = (AbstractFurnaceScreenHandler)var3_2;
                        ItemStack inputStack = var2_4.getSlot(0).getStack();
                        if (!inputStack.isEmpty()) {
                            yield inputStack.copy();
                        }
                        yield var2_4.getSlot(2).getStack().copy();
                    }
                    yield ItemStack.EMPTY;
                }
                case 3 -> {
                    ScreenHandler var3_3 = PotsTimer.minecraftClient.player.currentScreenHandler;
                    if (var3_3 instanceof BrewingStandScreenHandler) {
                        BrewingStandScreenHandler var2_5 = (BrewingStandScreenHandler)var3_3;
                        ItemStack ingredientStack = var2_5.getSlot(3).getStack();
                        if (!ingredientStack.isEmpty()) {
                            yield ingredientStack.copy();
                        }
                        yield var2_5.getSlot(0).getStack().copy();
                    }
                    yield ItemStack.EMPTY;
                }
            };
        }
        catch (Exception exception) {
            return ItemStack.EMPTY;
        }
    }

    private void resetPotionDisplay(boolean bl) {
        PotionEffectEntry potionEffectEntry;
        if (this.potionCategory == PotionCategory.IDLE) {
            return;
        }
        if (PotsTimer.minecraftClient.player != null && PotsTimer.minecraftClient.player.currentScreenHandler != PotsTimer.minecraftClient.player.playerScreenHandler) {
            if (PotsTimer.minecraftClient.player.networkHandler != null) {
                PotsTimer.minecraftClient.player.networkHandler.sendPacket((Packet)new CloseHandledScreenC2SPacket(PotsTimer.minecraftClient.player.currentScreenHandler.syncId));
            }
            PotsTimer.minecraftClient.player.currentScreenHandler = PotsTimer.minecraftClient.player.playerScreenHandler;
        }
        if (!bl && this.currentPotionBlock != null && (potionEffectEntry = this.savedPotionTimers.get(this.currentPotionBlock)) != null) {
            potionEffectEntry.startTimeMillis = System.currentTimeMillis();
            potionEffectEntry.active = true;
        }
        this.potionCategory = PotionCategory.IDLE;
        this.currentPotionBlock = null;
    }

    private void resetPotionState() {
        if (this.potionCategory != PotionCategory.IDLE) {
            this.resetPotionDisplay(false);
        }
        this.potionCategory = PotionCategory.IDLE;
        this.currentPotionBlock = null;
    }

    private boolean isPotionBlock(BlockPos adminsky) {
        double d = PotsTimer.minecraftClient.player.getBlockInteractionRange();
        return PotsTimer.minecraftClient.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter((Vec3i)adminsky)) <= d * d;
    }

    private void drawPotionTimer(CustomDrawContext customDrawContext, BlockPos adminsky, PotionEffectEntry potionEffectEntry) {
        boolean bl = potionEffectEntry.isActive();
        if (!bl || potionEffectEntry.active) {
            return;
        }
        Vec3d VanillaChestLootTableGenerator = Vec3d.ofCenter((Vec3i)adminsky).add(0.0, 1.1, 0.0);
        Vec2f VanillaAdventureTabAdvancementGenerator = ProjectionUtils.projectToScreen(VanillaChestLootTableGenerator);
        if (VanillaAdventureTabAdvancementGenerator == null) {
            return;
        }
        float f = (float)PotsTimer.minecraftClient.player.getPos().distanceTo(Vec3d.ofCenter((Vec3i)adminsky));
        float f2 = MathHelper.clamp((float)(1.0f - f / 24.0f), (float)0.45f, (float)1.0f) * this.scale.getValue();
        float f3 = potionEffectEntry.getProgressFraction();
        long l = potionEffectEntry.getRemainingSeconds();
        long l2 = l / 60L;
        long l3 = l % 60L;
        String string = String.format("%02d:%02d", l2, l3);
        float f4 = 110.0f;
        float f5 = 110.0f;
        MatrixStack class_45872 = customDrawContext.getMatrices();
        class_45872.push();
        class_45872.translate(VanillaAdventureTabAdvancementGenerator.x - f4 / 2.0f, VanillaAdventureTabAdvancementGenerator.y - f5 / 2.0f, 0.0f);
        ItemRenderUtils.translateAndScale(class_45872, f4 / 2.0f, f5 / 2.0f, f2);
        ColorRGBA colorRGBA = ColorPalette.getAccentColor();
        ColorRGBA colorRGBA2 = new ColorRGBA(9.0f, 9.0f, 11.0f).mulAlpha(0.55f);
        customDrawContext.drawBlurredRect(0.0f, 0.0f, f4, f5, 35.0f, 5.0f, WidgetState.uniform(22.0f), ColorPalette.WHITE.mulAlpha(0.35f));
        customDrawContext.drawSquircle(0.0f, 0.0f, f4, f5, 5.0f, WidgetState.uniform(22.0f), colorRGBA2);
        customDrawContext.drawCircleProgress(f4 / 2.0f, f5 / 2.0f, 40.0f, 5.0f, 1.0f, new ColorRGBA(255.0f, 255.0f, 255.0f).mulAlpha(0.12f));
        customDrawContext.drawCircleProgress(f4 / 2.0f, f5 / 2.0f, 40.0f, 5.0f, MathHelper.clamp((float)f3, (float)0.0f, (float)1.0f), colorRGBA);
        ItemStack class_17992 = potionEffectEntry.itemStack;
        if (class_17992 != null && !class_17992.isEmpty()) {
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            customDrawContext.drawItem(class_17992, f4 / 2.0f - 8.0f, f5 / 2.0f - 12.0f, 1.0f);
        } else {
            Item class_17922 = potionEffectEntry.potionType.getItem();
            customDrawContext.drawItem(class_17922, f4 / 2.0f - 8.0f, f5 / 2.0f - 12.0f, 1.0f);
        }
        customDrawContext.drawCenteredText(Font.ROUND_BOLD.metrics(13.0f), string, f4 / 2.0f, f5 / 2.0f + 10.0f, ColorPalette.getPrimaryTextColor());
        ItemRenderUtils.popMatrix(class_45872);
        class_45872.pop();
    }
}
