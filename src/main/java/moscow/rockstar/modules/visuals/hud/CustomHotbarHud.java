package moscow.rockstar.modules.visuals.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.FontRenderContext;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.item.ItemRenderStateRenderer;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.ui.hud.HudElementRegistry;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.CursorManager;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.layout.WindowMetricsProvider;
import moscow.rockstar.ui.text.Font;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;

/** Custom hotbar, hearts, armour, food and air rows. 1:1 with rockstar/ilIlil/IiIiIiIIi. */
public class CustomHotbarHud extends HudElement {
    private static final Identifier AIR_TEXTURE = Identifier.ofVanilla("hud/air");
    private static final Identifier AIR_BURSTING_TEXTURE = Identifier.ofVanilla("hud/air_bursting");
    private static final Identifier AIR_EMPTY_TEXTURE = Identifier.ofVanilla("hud/air_empty");
    private static final int HEART_ANIMATION_COUNT = 40;
    private static final float HOTBAR_BOTTOM_OFFSET = 29.0f;

    private final Random random = Random.create();
    private int ticks;
    private long lastWorldTime;
    private int lastHealthValue;
    private int lastHealth;
    private long lastHealthCheckTime;
    private long heartJumpEndTick;
    private int lastBubbleIndex;
    private final Animation[] heartAnimations = new Animation[HEART_ANIMATION_COUNT];
    private final int[] heartTypes = new int[HEART_ANIMATION_COUNT];
    private final Animation[] slotSelected = new Animation[9];
    private final Animation[] slotNeighbour = new Animation[9];

    public CustomHotbarHud() {
        super("hud.custom_hotbar", "hud/hotbar");
        int index;
        this.y = -1.0f;
        for (index = 0; index < this.slotSelected.length; ++index) {
            this.slotSelected[index] = new Animation(300L, Easing.easeOutBack);
            this.slotNeighbour[index] = new Animation(300L, Easing.easeOutBack);
        }
        for (index = 0; index < this.heartAnimations.length; ++index) {
            this.heartAnimations[index] = new Animation(700L, 0.0f, Easing.easeOutQuad);
        }
    }

    @Override
    protected boolean anchorsRightEdge() {
        return false;
    }

    @Override
    public void update(RockstarDrawContext drawContext) {
        boolean inactive = !this.isActive();
        if (this.y < 0.0f) {
            this.y = WindowMetricsProvider.INSTANCE.height() - HOTBAR_BOTTOM_OFFSET;
        }
        if (inactive && !this.isDragging()) {
            this.applyDefaultBounds();
        }
        super.update(drawContext);
        if (this.isDragging()) {
            this.x = WindowMetricsProvider.INSTANCE.width() / 2.0f - (float)(inactive ? 91 : 100);
            this.y = Math.min(this.y, WindowMetricsProvider.INSTANCE.height() - HOTBAR_BOTTOM_OFFSET);
        }
        if (inactive && this.isHovered(drawContext)) {
            CursorManager.request(Cursor.HAND);
        }
    }

    @Override
    public void pos(float x, float y) {
        super.pos(x, y);
        if (!this.isShowing()) {
            this.y = -1.0f;
        }
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, PointerAction action) {
        if (this.isActive()) {
            super.onMouseClicked(mouseX, mouseY, action);
            return;
        }
        if (action == PointerAction.LEFT_CLICK && this.isHovered(mouseX, mouseY)) {
            this.beginDrag(mouseX, mouseY);
        }
    }

    private boolean isActive() {
        return this.isShowing() && this.show();
    }

    private void applyDefaultBounds() {
        this.width = 182.0f;
        this.height = 29.0f;
        this.x = WindowMetricsProvider.INSTANCE.width() / 2.0f - 91.0f;
    }

    /** rockstar/ilIlil/IiIiIiIIi#I ()F - the hotbar's offset above the screen bottom. */
    public static float verticalOffset() {
        HudElementRegistry registry = RockstarClient.create() == null ? null : RockstarClient.create().getHudElementRegistry();
        CustomHotbarHud hud = null;
        if (registry != null) {
            for (HudElement element : registry.elements()) {
                if (!(element instanceof CustomHotbarHud customHotbarHud)) continue;
                hud = customHotbarHud;
                break;
            }
        }
        if (hud == null || hud.y < 0.0f) {
            return 0.0f;
        }
        return WindowMetricsProvider.INSTANCE.height() - HOTBAR_BOTTOM_OFFSET - hud.y;
    }

    @Override
    public void renderComponent(RockstarDrawContext drawContext) {
        MinecraftClient client = MinecraftClient.getInstance();
        this.width = 200.0f;
        this.height = 27.0f;
        ItemStack offhand = client.player.getOffHandStack();
        Arm offhandArm = client.player.getMainArm().getOpposite();
        boolean offhandLeft = false;
        boolean offhandRight = false;
        if (!offhand.isEmpty()) {
            if (offhandArm == Arm.LEFT) {
                offhandLeft = true;
            } else {
                offhandRight = true;
            }
        }
        this.x = WindowMetricsProvider.INSTANCE.width() / 2.0f - 100.0f;
        if (client.player == null) {
            return;
        }
        long worldTime;
        if (client.world != null && (worldTime = client.world.getTime()) != this.lastWorldTime) {
            this.lastWorldTime = worldTime;
            ++this.ticks;
        }
        int left = (int)this.x;
        int top = (int)this.y;
        int selectedSlot = client.player.getInventory().selectedSlot;
        if (client.interactionManager.hasStatusBars()) {
            this.renderStatusBars(drawContext);
        }
        int hotbarLeft = (int)(WindowMetricsProvider.INSTANCE.width() / 2.0f - 91.0f);
        this.renderExperienceLevel(drawContext, client.getRenderTickCounter());
        drawContext.drawClientRect((float)left - (offhandLeft ? 30.0f : 0.0f), top,
            offhandRight || offhandLeft ? 230.0f : 200.0f, 27.0f, 1.0f, 0.0f, 7.0f, 8.0f);
        float shaderAlpha = RenderSystem.getShaderColor()[3];
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        SlotEntry[] slots = new SlotEntry[9];
        // ORIGINAL: the hotbar's item batch runs inside iIiiiIiiI$I - the hotbar draws its
        // items with the vanilla layers, not the smooth-item ones.
        try (ItemRenderStateRenderer.SuppressScope smoothing = ItemRenderStateRenderer.suppressSmoothing();
             CustomDrawContext.ItemBatch itemBatch = drawContext.beginItemBatch();) {
            for (int slot = 0; slot < 9; ++slot) {
                Animation selected = this.slotSelected[slot];
                Animation neighbour = this.slotNeighbour[slot];
                selected.setReverse(selectedSlot == slot);
                neighbour.setReverse(Math.abs(selectedSlot - slot) <= 1);
                ItemStack stack = client.player.getInventory().main.get(slot);
                float scaleFactor = 0.1f * neighbour.getValue() + 0.25f * selected.getValue();
                float slotX = (float)(left + 6) + (float)slot * 21.5f;
                float slotY = (float)top + 5.5f - 12.0f * scaleFactor;
                slots[slot] = new SlotEntry(stack, slotX, slotY, scaleFactor);
                if (stack.isEmpty()) continue;
                ItemRenderUtils.translateAndScale(drawContext.getMatrices(), slotX + 8.0f, slotY + 8.0f, 1.0f + scaleFactor);
                drawContext.drawBatchItem(stack, slotX, slotY, 1);
                ItemRenderUtils.popMatrix(drawContext.getMatrices());
            }
            if (offhandLeft) {
                drawContext.drawBatchItem(offhand, (float)(left + 6) - 30.0f, (float)top + 5.5f, 1);
            } else if (offhandRight) {
                drawContext.drawBatchItem(offhand, (float)(left + 6) + 172.0f + 30.0f, (float)top + 5.5f, 1);
            }
        }
        SlotEntry offhandEntry = null;
        if (offhandLeft) {
            offhandEntry = new SlotEntry(offhand, (float)(left + 6) - 30.0f, (float)top + 5.5f, 0.0f);
        } else if (offhandRight) {
            offhandEntry = new SlotEntry(offhand, (float)(left + 6) + 172.0f + 30.0f, (float)top + 5.5f, 0.0f);
        }
        this.renderSlotNumbers(drawContext, slots);
        this.renderDurabilityBars(drawContext, slots, offhandEntry);
        this.renderStackCounts(drawContext, slots, offhandEntry);
        this.renderCooldowns(drawContext, slots, offhandEntry);
        this.renderSelectionDots(drawContext, slots);
        if (offhandLeft && offhandEntry != null) {
            drawContext.drawRoundedRect(offhandEntry.x() + 22.5f, offhandEntry.y(), 0.5f, 16.0f,
                WidgetState.NONE, ColorPalette.getPrimaryTextColor().mulAlpha(0.5f));
        } else if (offhandRight && offhandEntry != null) {
            drawContext.drawRoundedRect(offhandEntry.x() - 7.5f, offhandEntry.y(), 0.5f, 16.0f,
                WidgetState.NONE, ColorPalette.getPrimaryTextColor().mulAlpha(0.5f));
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, shaderAlpha);
    }

    private void renderSlotNumbers(CustomDrawContext drawContext, SlotEntry[] slots) {
        boolean anyEmpty = false;
        for (SlotEntry entry : slots) {
            if (!entry.stack().isEmpty()) continue;
            anyEmpty = true;
            break;
        }
        if (!anyEmpty) {
            return;
        }
        FontRenderContext pass = new FontRenderContext(VertexFormats.POSITION_TEXTURE_COLOR_LIGHT, drawContext.getMatrices());
        for (int slot = 0; slot < slots.length; ++slot) {
            SlotEntry entry = slots[slot];
            if (!entry.stack().isEmpty()) continue;
            float scale = 1.0f + entry.scaleFactor();
            String label = String.valueOf(slot + 1);
            ItemRenderUtils.translateAndScale(drawContext.getMatrices(), entry.centerX(), entry.centerY(), scale);
            drawContext.drawText(Font.ROUND_BOLD.metrics(10.0f), label,
                entry.x() + 8.5f - Font.ROUND_BOLD.metrics(10.0f).measureText(label) / 2.0f,
                entry.y() + 4.5f,
                ColorPalette.getPrimaryTextColor().mulAlpha(0.5f + entry.scaleFactor() * 1.5f));
            ItemRenderUtils.popMatrix(drawContext.getMatrices());
        }
        pass.render();
    }

    private void renderDurabilityBars(CustomDrawContext drawContext, SlotEntry[] slots, SlotEntry offhandEntry) {
        for (SlotEntry entry : slots) {
            if (entry.stack().isEmpty()) continue;
            ItemRenderUtils.translateAndScale(drawContext.getMatrices(), entry.centerX(), entry.centerY(), 1.0f + entry.scaleFactor());
            this.renderDurabilityBar(drawContext, entry.stack(), entry.x(), entry.y());
            ItemRenderUtils.popMatrix(drawContext.getMatrices());
        }
        if (offhandEntry != null) {
            this.renderDurabilityBar(drawContext, offhandEntry.stack(), offhandEntry.x(), offhandEntry.y());
        }
    }

    private void renderStackCounts(CustomDrawContext drawContext, SlotEntry[] slots, SlotEntry offhandEntry) {
        boolean anyStacked = false;
        for (SlotEntry entry : slots) {
            if (!this.isStacked(entry.stack())) continue;
            anyStacked = true;
            break;
        }
        if (!(anyStacked || offhandEntry != null && this.isStacked(offhandEntry.stack()))) {
            return;
        }
        FontRenderContext pass = new FontRenderContext(VertexFormats.POSITION_TEXTURE_COLOR_LIGHT, drawContext.getMatrices());
        for (SlotEntry entry : slots) {
            if (!this.isStacked(entry.stack())) continue;
            ItemRenderUtils.translateAndScale(drawContext.getMatrices(), entry.centerX(), entry.centerY(), 1.0f + entry.scaleFactor());
            this.renderStackCount(drawContext, entry.stack(), entry.x(), entry.y());
            ItemRenderUtils.popMatrix(drawContext.getMatrices());
        }
        if (offhandEntry != null && this.isStacked(offhandEntry.stack())) {
            this.renderStackCount(drawContext, offhandEntry.stack(), offhandEntry.x(), offhandEntry.y());
        }
        pass.render();
    }

    private void renderCooldowns(CustomDrawContext drawContext, SlotEntry[] slots, SlotEntry offhandEntry) {
        for (SlotEntry entry : slots) {
            if (entry.stack().isEmpty()) continue;
            ItemRenderUtils.translateAndScale(drawContext.getMatrices(), entry.centerX(), entry.centerY(), 1.0f + entry.scaleFactor());
            this.renderCooldown(drawContext, entry.stack(), entry.x(), entry.y());
            ItemRenderUtils.popMatrix(drawContext.getMatrices());
        }
        if (offhandEntry != null) {
            this.renderCooldown(drawContext, offhandEntry.stack(), offhandEntry.x(), offhandEntry.y());
        }
    }

    private void renderSelectionDots(CustomDrawContext drawContext, SlotEntry[] slots) {
        boolean anySelected = false;
        for (Animation animation : this.slotSelected) {
            if (!(animation.getValue() > 0.001f)) continue;
            anySelected = true;
            break;
        }
        if (!anySelected) {
            return;
        }
        for (int slot = 0; slot < slots.length; ++slot) {
            SlotEntry entry = slots[slot];
            float value = this.slotSelected[slot].getValue();
            drawContext.drawRoundedRect(entry.x() + 7.5f, entry.y() + 21.0f - value, 2.0f, 2.0f,
                WidgetState.uniform(0.5f), ColorPalette.getPrimaryTextColor().mulAlpha(value));
        }
    }

    private boolean isStacked(ItemStack stack) {
        return !stack.isEmpty() && stack.getCount() != 1;
    }

    /** rockstar/ilIlil/IiIiIiIIi#I (Lpyrock/utility/render/CustomDrawContext;Lnet/minecraft/class_1799;FF)V */
    public void renderItemOverlay(CustomDrawContext drawContext, ItemStack stack, float x, float y) {
        if (!stack.isEmpty()) {
            drawContext.getMatrices().push();
            this.renderDurabilityBar(drawContext, stack, x, y);
            this.renderStackCount(drawContext, stack, x, y);
            this.renderCooldown(drawContext, stack, x, y);
            drawContext.getMatrices().pop();
        }
    }

    private void renderDurabilityBar(CustomDrawContext drawContext, ItemStack stack, float x, float y) {
        if (stack.isItemBarVisible()) {
            float barX = x + 2.0f;
            float barY = y + 13.0f;
            drawContext.drawRoundedRect(barX, barY, 13.0f, 1.5f, WidgetState.uniform(0.25f), ColorRGBA.WHITE.mulAlpha(0.25f));
            drawContext.drawRoundedRect(barX, barY, (float)stack.getItemBarStep(), 1.5f, WidgetState.uniform(0.25f), ColorPalette.getAccentColor());
        }
    }

    private void renderStackCount(CustomDrawContext drawContext, ItemStack stack, float x, float y) {
        if (stack.getCount() != 1) {
            String label = String.valueOf(stack.getCount());
            drawContext.getMatrices().push();
            drawContext.getMatrices().translate(0.0f, 0.0f, 200.0f);
            drawContext.drawText(Font.ROUND_BOLD.metrics(8.0f), label,
                x + 19.0f + 0.5f - 2.0f - Font.ROUND_BOLD.metrics(8.0f).measureText(label),
                y + 6.0f + 3.5f, ColorPalette.BLACK.mulAlpha(0.4f));
            drawContext.drawText(Font.ROUND_BOLD.metrics(8.0f), label,
                x + 19.0f - 2.0f - Font.ROUND_BOLD.metrics(8.0f).measureText(label),
                y + 6.0f + 3.0f, ColorPalette.WHITE);
            drawContext.getMatrices().pop();
        }
    }

    private void renderCooldown(CustomDrawContext drawContext, ItemStack stack, float x, float y) {
        MinecraftClient client = MinecraftClient.getInstance();
        float progress = client.player == null
            ? 0.0f
            : client.player.getItemCooldownManager().getCooldownProgress(stack, client.getRenderTickCounter().getTickDelta(true));
        if (progress > 0.0f) {
            float barY = y + (float)MathHelper.floor(16.0f * (1.0f - progress));
            float barHeight = MathHelper.ceil(16.0f * progress);
            drawContext.drawRoundedRect(x, barY, 16.0f, barHeight, WidgetState.uniform(0.5f), ColorRGBA.WHITE.mulAlpha(0.35f));
        }
    }

    private void renderExperienceLevel(CustomDrawContext drawContext, RenderTickCounter renderTickCounter) {
        int level = MinecraftClient.getInstance().player.experienceLevel;
        if (this.hasExperienceBar() && level > 0) {
            String label = "" + level;
            float labelX = ((float)drawContext.getScaledWindowWidth() - Font.REGULAR.metrics(8.0f).measureText(label)) / 2.0f;
            int labelY = (int)this.y - 8;
            FontRenderContext pass = new FontRenderContext(VertexFormats.POSITION_TEXTURE_COLOR_LIGHT, drawContext.getMatrices());
            drawContext.drawText(Font.REGULAR.metrics(8.0f), label, labelX + 1.0f, labelY, ColorPalette.BLACK.mulAlpha(0.5f));
            drawContext.drawText(Font.REGULAR.metrics(8.0f), label, labelX - 1.0f, labelY, ColorPalette.BLACK.mulAlpha(0.5f));
            drawContext.drawText(Font.REGULAR.metrics(8.0f), label, labelX, labelY + 1, ColorPalette.BLACK.mulAlpha(0.5f));
            drawContext.drawText(Font.REGULAR.metrics(8.0f), label, labelX, labelY - 1, ColorPalette.BLACK.mulAlpha(0.5f));
            drawContext.drawText(Font.REGULAR.metrics(8.0f), label, labelX, labelY, new ColorRGBA(126.0f, 252.0f, 32.0f));
            pass.render();
        }
    }

    private boolean hasExperienceBar() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player.getJumpingMount() == null && client.interactionManager.hasExperienceBar();
    }

    private void renderStatusBars(RockstarDrawContext drawContext) {
        PlayerEntity player = this.cameraPlayer();
        if (player == null) {
            return;
        }
        int health = MathHelper.ceil(player.getHealth());
        boolean blinking = this.heartJumpEndTick > (long)this.ticks && (this.heartJumpEndTick - (long)this.ticks) / 3L % 2L == 1L;
        long now = Util.getMeasuringTimeMs();
        if (health < this.lastHealthValue && player.timeUntilRegen > 0) {
            this.lastHealthCheckTime = now;
            this.heartJumpEndTick = this.ticks + 20;
        } else if (health > this.lastHealthValue && player.timeUntilRegen > 0) {
            this.lastHealthCheckTime = now;
            this.heartJumpEndTick = this.ticks + 10;
        }
        if (now - this.lastHealthCheckTime > 1000L) {
            this.lastHealth = health;
            this.lastHealthCheckTime = now;
        }
        int index;
        if (health < this.lastHealthValue) {
            for (index = 0; index < HEART_ANIMATION_COUNT && index * 2 < this.lastHealthValue; ++index) {
                int previous = MathHelper.clamp(this.lastHealthValue - index * 2, 0, 2);
                int current = MathHelper.clamp(health - index * 2, 0, 2);
                if (previous <= current) continue;
                this.heartTypes[index] = previous - current == 2 ? 3 : (previous == 2 ? 2 : 1);
                this.heartAnimations[index].setValue(1.0f);
                this.heartAnimations[index].update(0.0f);
            }
        } else if (health > this.lastHealthValue) {
            for (index = 0; index < HEART_ANIMATION_COUNT && index * 2 < health; ++index) {
                this.heartAnimations[index].reset();
                this.heartTypes[index] = 0;
            }
        }
        this.lastHealthValue = health;
        int displayHealth = this.lastHealth;
        this.random.setSeed((long)(this.ticks * 312871));
        int left = drawContext.getScaledWindowWidth() / 2 - 100;
        int right = drawContext.getScaledWindowWidth() / 2 + 100;
        int heartY = (int)this.y - 11;
        float maxHealth = Math.max((float)player.getAttributeValue(EntityAttributes.MAX_HEALTH), (float)Math.max(displayHealth, health));
        int absorption = MathHelper.ceil(player.getAbsorptionAmount());
        int rows = MathHelper.ceil((maxHealth + (float)absorption) / 2.0f / 10.0f);
        int rowHeight = Math.max(10 - (rows - 2), 3);
        int sideRowY = heartY - 10;
        int regenIndex = -1;
        if (player.hasStatusEffect(StatusEffects.REGENERATION)) {
            regenIndex = this.ticks % MathHelper.ceil(maxHealth + 5.0f);
        }
        CustomHotbarHud.renderArmorRow(drawContext, player, heartY, rows, rowHeight, left);
        this.renderHearts(drawContext, player, left, heartY, rowHeight, regenIndex, maxHealth, health, displayHealth, absorption, blinking);
        LivingEntity mount = this.ridingEntity();
        int mountRows = this.mountHealthRows(mount);
        if (mountRows == 0) {
            this.renderFood(drawContext, player, heartY, right);
            sideRowY -= 10;
        }
        this.renderAir(drawContext, player, mountRows, sideRowY, right);
    }

    private static ColorRGBA emptyIconColor() {
        return ColorPalette.getPanelColor().mulAlpha(MathUtils.interpolateDouble(
            ColorPalette.getThemeColorSettings().getOverlayAlphaMinimum(),
            ColorPalette.getThemeColorSettings().getOverlayAlphaMaximum(),
            Interface.getLiquidGlassAlpha()));
    }

    private static void renderArmorRow(RockstarDrawContext drawContext, PlayerEntity player, int heartY, int rows, int rowHeight, int left) {
        int index;
        int iconX;
        ColorRGBA armorColor = new ColorRGBA(223.0f, 223.0f, 223.0f);
        int armor = player.getArmor();
        if (armor <= 0) {
            return;
        }
        int iconY = heartY - (rows - 1) * rowHeight - 10;
        for (index = 0; index < 10; ++index) {
            iconX = left + index * 9;
            if (index * 2 + 1 < armor) {
                drawContext.drawRoundedRect(iconX, iconY, 8.0f, 8.0f, WidgetState.uniform(1.5f), armorColor);
            }
            if (index * 2 + 1 <= armor) continue;
            drawContext.drawRoundedRect(iconX, iconY, 8.0f, 8.0f, WidgetState.uniform(1.5f), CustomHotbarHud.emptyIconColor());
        }
        for (index = 0; index < 10; ++index) {
            iconX = left + index * 9;
            if (index * 2 + 1 != armor) continue;
            drawContext.drawRoundedRect(iconX + 4, iconY, 4.0f, 8.0f, new WidgetState(0.0f, 1.5f, 1.5f, 0.0f), CustomHotbarHud.emptyIconColor());
            drawContext.drawRoundedRect(iconX, iconY, 4.0f, 8.0f, new WidgetState(1.5f, 0.0f, 0.0f, 1.5f), armorColor);
        }
    }

    private void renderHearts(RockstarDrawContext drawContext, PlayerEntity player, int left, int heartY, int rowHeight,
                              int regenIndex, float maxHealth, int health, int displayHealth, int absorption, boolean blinking) {
        HeartType heartType = HeartType.forPlayer(player);
        boolean hardcore = player.getWorld().getLevelProperties().isHardcore();
        int healthHearts = MathHelper.ceil((double)maxHealth / 2.0);
        int absorptionHearts = MathHelper.ceil((double)absorption / 2.0);
        int healthPoints = healthHearts * 2;
        for (int heart = healthHearts + absorptionHearts - 1; heart >= 0; --heart) {
            int row = heart / 10;
            int column = heart % 10;
            int iconX = left + column * 9;
            int iconY = heartY - row * rowHeight;
            if (health + absorption <= 4) {
                iconY += this.random.nextInt(2);
            }
            if (heart < healthHearts && heart == regenIndex) {
                iconY -= 2;
            }
            this.drawHeart(drawContext, HeartType.CONTAINER, iconX, iconY, false);
            int value = heart * 2;
            if (heart >= healthHearts) {
                int absorbed = value - healthPoints;
                if (absorbed < absorption) {
                    boolean half = absorbed + 1 == absorption;
                    this.drawHeart(drawContext, heartType == HeartType.WITHERED ? heartType : HeartType.ABSORBING, iconX, iconY, half);
                }
            }
            if (value < health) {
                this.drawHeart(drawContext, heartType, iconX, iconY, value + 1 == health);
            }
            float pop;
            if (heart >= HEART_ANIMATION_COUNT || !((pop = this.heartAnimations[heart].update(0.0f)) > 0.001f)) continue;
            float scale = 1.0f + (1.0f - pop);
            ItemRenderUtils.translateAndScale(drawContext.getMatrices(), (float)iconX + 4.0f, (float)iconY + 4.0f, scale);
            ColorRGBA popColor = ColorRGBA.WHITE.mulAlpha(pop);
            if (this.heartTypes[heart] == 1) {
                drawContext.drawRoundedRect(iconX, iconY, 4.0f, 8.0f, new WidgetState(2.0f, 0.0f, 0.0f, 2.0f), popColor);
            } else if (this.heartTypes[heart] == 2) {
                drawContext.drawRoundedRect(iconX + 4, iconY, 4.0f, 8.0f, new WidgetState(0.0f, 2.0f, 2.0f, 0.0f), popColor);
            } else {
                drawContext.drawRoundedRect(iconX, iconY, 8.0f, 8.0f, WidgetState.uniform(2.0f), popColor);
            }
            ItemRenderUtils.popMatrix(drawContext.getMatrices());
        }
    }

    private void drawHeart(RockstarDrawContext drawContext, HeartType heartType, int x, int y, boolean half) {
        heartType.draw(drawContext, x, y, 8.0f, 8.0f, half);
    }

    private void renderAir(RockstarDrawContext drawContext, PlayerEntity player, int mountRows, int rowY, int right) {
        int maxAir = player.getMaxAir();
        int air = Math.clamp((long)player.getAir(), 0, maxAir);
        boolean submerged = player.isSubmergedIn(FluidTags.WATER);
        if (!submerged && air >= maxAir) {
            return;
        }
        int iconY = this.airRowY(mountRows, rowY);
        int fullBubbles = CustomHotbarHud.bubbleCount(air, maxAir, -2);
        int currentBubble = CustomHotbarHud.bubbleCount(air, maxAir, 0);
        int poppedBubbles = 10 - CustomHotbarHud.bubbleCount(air, maxAir, CustomHotbarHud.bubbleOffset(air, submerged));
        boolean bursting = fullBubbles != currentBubble;
        if (!submerged) {
            this.lastBubbleIndex = 0;
        }
        for (int bubble = 1; bubble <= 10; ++bubble) {
            int iconX = right - (bubble - 1) * 9 - 10;
            if (bubble <= fullBubbles) {
                drawContext.drawGuiTexture(RenderLayer::getGuiTextured, AIR_TEXTURE, iconX, iconY, 9, 9);
                continue;
            }
            if (bursting && bubble == currentBubble && submerged) {
                drawContext.drawGuiTexture(RenderLayer::getGuiTextured, AIR_BURSTING_TEXTURE, iconX, iconY, 9, 9);
                this.playBubblePop(bubble, player, poppedBubbles);
                continue;
            }
            if (bubble <= 10 - poppedBubbles) continue;
            int jitter = poppedBubbles == 10 && this.ticks % 2 == 0 ? this.random.nextInt(2) : 0;
            drawContext.drawGuiTexture(RenderLayer::getGuiTextured, AIR_EMPTY_TEXTURE, iconX, iconY + jitter, 9, 9);
        }
    }

    private int airRowY(int mountRows, int rowY) {
        return rowY - (this.mountRowCount(mountRows) - 1) * 10;
    }

    private static int bubbleCount(int air, int maxAir, int offset) {
        return MathHelper.ceil((float)((air + offset) * 10) / (float)maxAir);
    }

    private static int bubbleOffset(int air, boolean submerged) {
        return air == 0 || !submerged ? 0 : 1;
    }

    private void playBubblePop(int bubble, PlayerEntity player, int poppedBubbles) {
        if (this.lastBubbleIndex != bubble) {
            float volume = 0.5f + 0.1f * (float)Math.max(0, poppedBubbles - 3 + 1);
            float pitch = 1.0f + 0.1f * (float)Math.max(0, poppedBubbles - 5 + 1);
            player.playSound(SoundEvents.UI_HUD_BUBBLE_POP, volume, pitch);
            this.lastBubbleIndex = bubble;
        }
    }

    private void renderFood(RockstarDrawContext drawContext, PlayerEntity player, int rowY, int right) {
        int index;
        int iconX;
        ColorRGBA foodColor = new ColorRGBA(184.0f, 132.0f, 88.0f);
        HungerManager hungerManager = player.getHungerManager();
        int foodLevel = hungerManager.getFoodLevel();
        int[] foodY = new int[10];
        for (index = 0; index < 10; ++index) {
            int jitteredY = rowY;
            if (hungerManager.getSaturationLevel() <= 0.0f && this.ticks % (foodLevel * 3 + 1) == 0) {
                jitteredY += this.random.nextInt(3) - 1;
            }
            foodY[index] = jitteredY;
        }
        for (index = 0; index < 10; ++index) {
            iconX = right - index * 9 - 10;
            drawContext.drawRoundedRect(iconX, foodY[index], 8.0f, 8.0f, WidgetState.uniform(1.5f), CustomHotbarHud.emptyIconColor());
        }
        for (index = 0; index < 10; ++index) {
            if (index * 2 + 1 >= foodLevel) continue;
            iconX = right - index * 9 - 10;
            drawContext.drawRoundedRect(iconX, foodY[index], 8.0f, 8.0f, WidgetState.uniform(1.5f), foodColor);
        }
        for (index = 0; index < 10; ++index) {
            if (index * 2 + 1 != foodLevel) continue;
            iconX = right - index * 9 - 10;
            drawContext.drawRoundedRect(iconX + 4, foodY[index], 4.0f, 8.0f, new WidgetState(0.0f, 1.5f, 1.5f, 0.0f), foodColor);
        }
        ColorRGBA saturationColor = new ColorRGBA(251.0f, 170.0f, 56.0f);
        int saturation = (int)hungerManager.getSaturationLevel();
        int[] saturationY = new int[10];
        for (index = 0; index < 10; ++index) {
            int jitteredY = rowY;
            if (hungerManager.getSaturationLevel() <= 0.0f && this.ticks % (saturation * 3 + 1) == 0) {
                jitteredY += this.random.nextInt(3) - 1;
            }
            saturationY[index] = jitteredY;
        }
        for (index = 0; index < 10; ++index) {
            iconX = right - index * 9 - 10;
            if (index * 2 + 1 >= saturation) continue;
            drawContext.drawRoundedRect(iconX, saturationY[index], 8.0f, 8.0f, WidgetState.uniform(1.5f), saturationColor);
        }
        for (index = 0; index < 10; ++index) {
            if (index * 2 + 1 != saturation) continue;
            iconX = right - index * 9 - 10;
            drawContext.drawRoundedRect(iconX + 4, saturationY[index], 4.0f, 8.0f, new WidgetState(0.0f, 1.5f, 1.5f, 0.0f), saturationColor);
        }
    }

    private PlayerEntity cameraPlayer() {
        Entity entity = MinecraftClient.getInstance().getCameraEntity();
        return entity instanceof PlayerEntity playerEntity ? playerEntity : null;
    }

    private LivingEntity ridingEntity() {
        PlayerEntity player = this.cameraPlayer();
        if (player == null) {
            return null;
        }
        Entity vehicle = player.getVehicle();
        return vehicle instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    private int mountHealthRows(LivingEntity mount) {
        if (mount == null || !mount.isLiving()) {
            return 0;
        }
        int hearts = (int)(mount.getMaxHealth() + 0.5f) / 2;
        return Math.min(hearts, 30);
    }

    private int mountRowCount(int hearts) {
        return (int)Math.ceil((double)hearts / 10.0);
    }

    /** rockstar/ilIlil/IiIiIiIIi$i */
    record SlotEntry(ItemStack stack, float x, float y, float scaleFactor) {
        float centerX() {
            return this.x + 8.0f;
        }

        float centerY() {
            return this.y + 8.0f;
        }
    }

    /** rockstar/ilIlil/IiIiIiIIi$I */
    enum HeartType {
        CONTAINER(new ColorRGBA(200.0f, 200.0f, 200.0f, 200.0f)),
        NORMAL(new ColorRGBA(255.0f, 81.0f, 81.0f)),
        POISONED(new ColorRGBA(169.0f, 202.0f, 23.0f)),
        WITHERED(new ColorRGBA(66.0f, 66.0f, 66.0f)),
        ABSORBING(new ColorRGBA(251.0f, 170.0f, 56.0f)),
        FROZEN(new ColorRGBA(163.0f, 246.0f, 255.0f));

        private final ColorRGBA color;

        private HeartType(ColorRGBA color) {
            this.color = color;
        }

        public void draw(CustomDrawContext drawContext, float x, float y, float width, float height, boolean half) {
            if (half) {
                drawContext.drawRoundedRect(x, y, width / 2.0f, height, new WidgetState(2.0f, 0.0f, 0.0f, 2.0f),
                    this == CONTAINER ? CustomHotbarHud.emptyIconColor() : this.color);
            } else {
                drawContext.drawRoundedRect(x, y, width, height, WidgetState.uniform(2.0f),
                    this == CONTAINER ? CustomHotbarHud.emptyIconColor() : this.color);
            }
        }

        static HeartType forPlayer(PlayerEntity player) {
            if (player.hasStatusEffect(StatusEffects.POISON)) {
                return POISONED;
            }
            if (player.hasStatusEffect(StatusEffects.WITHER)) {
                return WITHERED;
            }
            if (player.isFrozen()) {
                return FROZEN;
            }
            return NORMAL;
        }
    }
}
