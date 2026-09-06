package moscow.rockstar.modules.visuals.esp.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.items.DonorItemParser;
import moscow.rockstar.items.ItemMetadataUtils;
import moscow.rockstar.modules.visuals.esp.targeting.PlayerTargetGroup;
import moscow.rockstar.modules.visuals.esp.targeting.TargetGroup;
import moscow.rockstar.modules.visuals.hud.NameProtect;
import moscow.rockstar.render.batch.OverlayBatchBuilder;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.esp.TargetRenderModule;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.text.TextCaptureController;
import moscow.rockstar.render.text.WorldTextBatch;
import moscow.rockstar.render.util.ProjectionUtils;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.social.FriendListManager;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ItemConvertible;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.render.PreHudRenderEvent;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;

/**
 * ESP nametag overlay - port of {@code rockstar/ilIlil/iIIIIiII}.
 *
 * <p>Registered as the fourth ESP overlay (after {@link Fill}, before
 * {@link EntityArrowRenderer}), matching the original registration order in
 * {@code rockstar/ilIlil/IiiiiiiI#I()V}.</p>
 *
 * <p>Everything that depended on {@code globals/shared/proto/Packets$Friend}
 * (the Rocknet friend cards, their avatars, badges and nick styles) and
 * everything that depended on the context-menu widget {@code IiIIiiIii} - which
 * has no remapped counterpart yet - is omitted; see the port report.</p>
 */
public class Nametags
extends TargetRenderModule
implements ClientAccess {
    private static final int NAME_FONT_SIZE = 11;
    private static final int ITEM_LABEL_FONT_SIZE = 9;
    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 1.0f;
    private static final float BACKGROUND_HEIGHT = 20.0f;
    private static final float PLATE_HEIGHT = 22.0f;
    private static final float PADDING = 4.0f;
    private static final float ROW_SPACING = 3.0f;
    private static final float PREVIEW_RADIUS = 5.0f;
    private static final float HEAD_SIZE = 14.0f;
    private static final float HEAD_INSET = 4.0f;
    private static final float TEXT_INSET = 4.0f;
    private static final float ARMOR_SLOT_WIDTH = 18.0f;
    private static final float ARMOR_ROW_Y = -15.0f;
    private static final int SCALE_DISTANCE = 11;
    private static final float PREVIEW_SCALE = 0.6f;
    private static final float ITEM_LABEL_OFFSET = 9.0f;
    private static final float ROCKNET_PLATE_HEIGHT = 26.0f;

    private final List<Entity> trackedEntities = new ArrayList<Entity>();
    private final Map<ItemEntity, List<ItemGroupEntry>> itemGroupCache = new HashMap<ItemEntity, List<ItemGroupEntry>>();
    private final Map<Entity, Text> displayNameCache = new HashMap<Entity, Text>();
    private final Map<Entity, Float> nameWidthCache = new HashMap<Entity, Float>();
    private final Map<Entity, Float> originalNameWidthCache = new HashMap<Entity, Float>();

    private final BooleanSetting nametagsSetting;
    private final BooleanSetting showArmorSetting;
    private final BooleanSetting showItemUseSetting;
    private final BooleanSetting backgroundSetting;
    private final PlayerHeadAtlas headAtlas = new PlayerHeadAtlas();

    private String pendingInvseeTarget;
    private long pendingInvseeDeadline;

    private final EventListener<PreHudRenderEvent> preHudRenderListener = preHudRenderEvent -> {
        this.tickPendingInvsee();
        if (!this.isValid()) {
            return;
        }
        MatrixStack class_45872 = preHudRenderEvent.getContext().getMatrices();
        float f = preHudRenderEvent.getTickDelta();
        this.collectEntities();
        List<List<ItemEntity>> list = this.groupItemEntities();
        OverlayBatchBuilder nametagBatch = new OverlayBatchBuilder(Font.MEDIUM, 5.0f);
        OverlayBatchBuilder itemBatch = new OverlayBatchBuilder(Font.MEDIUM, 0.0f);
        this.drawBackgrounds(nametagBatch, itemBatch, class_45872, list, f);
        this.drawHeads(nametagBatch, class_45872, f);
        this.drawTexts(nametagBatch, class_45872, list, f);
        this.headAtlas.upload();
        nametagBatch.setTextureAndHeadSize(this.headAtlas.getTextureId(), 14.0f, 4.0f);
        itemBatch.flush();
        nametagBatch.flush();
        this.drawArmorRows((PreHudRenderEvent)preHudRenderEvent, class_45872, f);
        this.drawContainerItems((PreHudRenderEvent)preHudRenderEvent, class_45872, f);
        this.drawWorldNames((PreHudRenderEvent)preHudRenderEvent, class_45872, f);
        for (Entity class_12972 : this.trackedEntities) {
            PlayerEntity class_16572;
            BooleanSetting booleanSetting;
            Vec3d class_2432 = ProjectionUtils.interpolateEntityPosition(class_12972, f)
                .add(0.0, class_12972.getBoundingBox().getLengthY() / 2.0, 0.0);
            Vec2f class_2412 = ProjectionUtils.projectToScreen(class_2432);
            if (class_2412 == null || class_12972.getType() != EntityType.PLAYER
                || (booleanSetting = this.getSettingForScope("esp.nametags.show_item_use",
                    this.getPlayerGroup(class_16572 = (PlayerEntity)class_12972))) == null
                || !booleanSetting.isEnabled()) continue;
            this.drawItemUse((PreHudRenderEvent)preHudRenderEvent, class_45872, class_16572, class_2412);
        }
        DiffuseLighting.disableGuiDepthLighting();
        preHudRenderEvent.getContext().draw();
    };

    private final EventListener<ReceivePacketEvent> receivePacketListener = receivePacketEvent -> {
        Packet<?> class_25962;
        if (this.pendingInvseeTarget == null || !((class_25962 = receivePacketEvent.getPacket()) instanceof GameMessageS2CPacket)) {
            return;
        }
        GameMessageS2CPacket class_74392 = (GameMessageS2CPacket)class_25962;
        String string = class_74392.content().getString().toLowerCase(Locale.ROOT);
        if (string.contains("unknown command") || string.contains("unknown or incomplete command")
            || string.contains("command not found") || string.contains("no permission")
            || string.contains("not have permission") || string.contains("insufficient permission")
            || string.contains("not allowed to use") || string.contains("cannot use this command")
            || string.contains("can't use this command")
            || string.contains("\u043d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u0430\u044f \u043a\u043e\u043c\u0430\u043d\u0434\u0430")
            || string.contains("\u043a\u043e\u043c\u0430\u043d\u0434\u0430 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430")
            || string.contains("\u043d\u0435\u0442 \u043f\u0440\u0430\u0432")
            || string.contains("\u043d\u0435\u0434\u043e\u0441\u0442\u0430\u0442\u043e\u0447\u043d\u043e \u043f\u0440\u0430\u0432")
            || string.contains("\u043d\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442 \u043f\u0440\u0430\u0432")
            || string.contains("\u0434\u043e\u0441\u0442\u0443\u043f \u0437\u0430\u043f\u0440\u0435\u0449\u0435\u043d")
            || string.contains("\u0434\u043e\u0441\u0442\u0443\u043f \u0437\u0430\u043f\u0440\u0435\u0449\u0451\u043d")) {
            this.pendingInvseeDeadline = 0L;
        }
    };

    public Nametags() {
        super("nametags", new TargetGroup[]{TargetGroup.PLAYERS, TargetGroup.MOBS, TargetGroup.ANIMALS, TargetGroup.ITEMS});
        this.nametagsSetting = this.createSetting("esp.nametags");
        this.showArmorSetting = this.createPlayerScopedSetting("esp.nametags.show_armor");
        this.showItemUseSetting = this.createPlayerScopedSetting("esp.nametags.show_item_use");
        this.backgroundSetting = new BooleanSetting((SettingOwner)((Object)this), "esp.nametags.background",
            () -> !this.isValid2(TargetGroup.ITEMS));
        this.attachSettingToTargetGroups(this.backgroundSetting, TargetGroup.ITEMS);
        this.backgroundSetting.enable();
        this.enableTargetGroup(TargetGroup.ITEMS);
        this.enablePlayerGroup(PlayerTargetGroup.OTHERS, PlayerTargetGroup.FRIENDS, PlayerTargetGroup.ROCKSTAR_USERS);
        BooleanSetting booleanSetting = this.getSettingForScope("esp.nametags.show_armor", PlayerTargetGroup.OTHERS);
        BooleanSetting booleanSetting2 = this.getSettingForScope("esp.nametags.show_armor", PlayerTargetGroup.FRIENDS);
        BooleanSetting booleanSetting3 = this.getSettingForScope("esp.nametags.show_armor", PlayerTargetGroup.ROCKSTAR_USERS);
        if (booleanSetting != null) {
            booleanSetting.enable();
        }
        if (booleanSetting2 != null) {
            booleanSetting2.enable();
        }
        if (booleanSetting3 != null) {
            booleanSetting3.enable();
        }
    }

    /**
     * Remap of {@code IiiiIiiI.i (Ljava/util/function/Function;)Lrockstar/ilIlil/IIiiiIIII;}.
     *
     * <p>Unlike the base class factory overloads, that one walks the supported
     * <em>player</em> scopes only, so "show armor" and "item use" exist for the
     * four player scopes and for nothing else.</p>
     */
    private BooleanSetting createPlayerScopedSetting(String key) {
        BooleanSetting first = null;
        for (PlayerTargetGroup playerTargetGroup : PlayerTargetGroup.values()) {
            if (!this.supportsTargetGroup(playerTargetGroup)) continue;
            BooleanSetting booleanSetting = new BooleanSetting((SettingOwner)((Object)this), key);
            this.registerScopedSetting(key, playerTargetGroup, booleanSetting);
            if (first == null) {
                first = booleanSetting;
            }
        }
        return first;
    }

    @Override
    public void renderPreviewOverlay(RockstarDrawContext drawContext, Entity class_12972, float f, float f2, TargetGroup targetGroup, PlayerTargetGroup playerTargetGroup) {
        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(0.0f, -class_12972.getHeight() * 15.0f, 0.0f);
        drawContext.getMatrices().translate(f, f2, 0.0f);
        drawContext.getMatrices().scale(0.6f, 0.6f, 1.0f);
        drawContext.getMatrices().translate(-f, -f2, 0.0f);
        if (targetGroup == TargetGroup.PLAYERS) {
            this.renderPlayerPreview(drawContext, class_12972, f, f2, playerTargetGroup);
        } else if (targetGroup == TargetGroup.ITEMS && class_12972 instanceof ItemEntity) {
            ItemEntity class_15422 = (ItemEntity)class_12972;
            this.renderItemPreview(drawContext, class_15422, f, f2);
        } else if (targetGroup == TargetGroup.MOBS || targetGroup == TargetGroup.ANIMALS) {
            this.renderMobPreview(drawContext, class_12972, f, f2, targetGroup);
        }
        drawContext.getMatrices().pop();
    }

    private void renderPlayerPreview(RockstarDrawContext drawContext, Entity class_12972, float f, float f2, PlayerTargetGroup playerTargetGroup) {
        if (playerTargetGroup == PlayerTargetGroup.ROCKSTAR_USERS) {
            this.renderRockstarUserPreview(drawContext, class_12972, f, f2);
            return;
        }
        String string = class_12972.getName().getString();
        FontMetrics fontMetrics = Font.MEDIUM.metrics(11.0f);
        float f3 = fontMetrics.measureText(string + " ");
        float f4 = fontMetrics.measureText("[20]");
        float f5 = 22.0f + f3 + f4 + 4.0f;
        float f6 = f - f5 / 2.0f;
        float f7 = f2 - 11.0f;
        drawContext.drawRoundedRect(f6, f7, f5, 22.0f, WidgetState.uniform(5.0f), new ColorRGBA(12.0f, 12.0f, 12.0f, 235.0f));
        if (class_12972 instanceof AbstractClientPlayerEntity) {
            AbstractClientPlayerEntity class_7422 = (AbstractClientPlayerEntity)class_12972;
            drawContext.drawHead(class_7422, f6 + 4.0f, f7 + 4.0f, 14.0f, WidgetState.uniform(4.0f), ColorRGBA.WHITE);
        }
        float f8 = f6 + 4.0f + 14.0f + 4.0f;
        float f9 = f7 + (22.0f - fontMetrics.getFontMetricsFloat()) / 2.0f;
        drawContext.drawText(fontMetrics, string + " ", f8, f9, ColorRGBA.WHITE);
        drawContext.drawText(fontMetrics, "[20]", f8 + f3, f9, new ColorRGBA(255.0f, 85.0f, 85.0f));
        BooleanSetting booleanSetting = this.getSettingForScope("esp.nametags.show_armor", playerTargetGroup);
        if (booleanSetting != null && booleanSetting.isEnabled()) {
            this.renderArmorPreview(drawContext, f, f7 - 12.0f);
        }
    }

    private void renderRockstarUserPreview(RockstarDrawContext drawContext, Entity class_12972, float f, float f2) {
        String string = class_12972.getName().getString();
        float f3 = Font.MEDIUM.metrics(11.0f).measureText(string) + 31.0f;
        float f4 = 26.0f;
        float f5 = f - f3 / 2.0f;
        float f6 = f2 - f4 / 2.0f;
        drawContext.drawRoundedRect(f5, f6, f3, f4, WidgetState.uniform(7.0f), new ColorRGBA(12.0f, 12.0f, 12.0f, 255.0f));
        drawContext.drawRoundedTexture(RockstarClient.resourceId("rocknet/avatar.png"), f5 + 5.0f, f6 + 5.0f, 16.0f, 16.0f, WidgetState.uniform(7.0f));
        drawContext.drawText(Font.REGULAR.metrics(11.0f), string, f5 + 25.0f, f6 + 9.0f, ColorRGBA.WHITE);
        BooleanSetting booleanSetting = this.getSettingForScope("esp.nametags.show_armor", PlayerTargetGroup.ROCKSTAR_USERS);
        if (booleanSetting != null && booleanSetting.isEnabled()) {
            this.renderArmorPreview(drawContext, f, f6 - 20.0f);
        }
    }

    private void renderArmorPreview(RockstarDrawContext drawContext, float f, float f2) {
        ItemStack class_17992 = new ItemStack((ItemConvertible)Items.NETHERITE_HELMET);
        ItemStack class_17993 = new ItemStack((ItemConvertible)Items.NETHERITE_CHESTPLATE);
        ItemStack class_17994 = new ItemStack((ItemConvertible)Items.NETHERITE_LEGGINGS);
        ItemStack class_17995 = new ItemStack((ItemConvertible)Items.NETHERITE_BOOTS);
        ItemStack class_17996 = new ItemStack((ItemConvertible)Items.NETHERITE_SWORD);
        ItemStack class_17997 = new ItemStack((ItemConvertible)Items.TOTEM_OF_UNDYING);
        List<ItemStack> list = List.of(class_17992, class_17993, class_17994, class_17995, class_17996, class_17997);
        float f3 = (float)list.size() * 12.0f;
        float f4 = f - f3 / 2.0f;
        for (int j = 0; j < list.size(); ++j) {
            drawContext.drawItem(list.get(j), (float)((int)(f4 + (float)(j * 12))), (float)((int)f2), 0.75f);
        }
    }

    private void renderItemPreview(RockstarDrawContext drawContext, ItemEntity class_15422, float f, float f2) {
        String string = class_15422.getStack().getName().getString();
        float f3 = Font.MEDIUM.metrics(11.0f).measureText(string);
        float f4 = Font.MEDIUM.metrics(11.0f).getFontMetricsFloat();
        if (this.backgroundSetting.isEnabled()) {
            drawContext.drawRect(f - f3 / 2.0f - 3.0f, f2 - 3.0f, f3 + 6.0f, f4 + 6.0f, new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));
        }
        drawContext.drawText(Font.MEDIUM.metrics(11.0f), string, f - f3 / 2.0f, f2, ColorRGBA.WHITE);
    }

    private void renderMobPreview(RockstarDrawContext drawContext, Entity class_12972, float f, float f2, TargetGroup targetGroup) {
        if (!(class_12972 instanceof LivingEntity)) {
            return;
        }
        LivingEntity class_13092 = (LivingEntity)class_12972;
        String string = class_12972.getName().getString();
        int n = (int)class_13092.getHealth();
        String string2 = string + " [" + n + "]";
        float f3 = Font.MEDIUM.metrics(11.0f).measureText(string2);
        float f4 = Font.MEDIUM.metrics(11.0f).getFontMetricsFloat();
        drawContext.drawRect(f - f3 / 2.0f - 3.0f, f2 - 3.0f, f3 + 6.0f, f4 + 6.0f, new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));
        drawContext.drawText(Font.MEDIUM.metrics(11.0f), string + " ", f - f3 / 2.0f, f2, ColorRGBA.WHITE);
        drawContext.drawText(Font.MEDIUM.metrics(11.0f), "[" + n + "]",
            f - f3 / 2.0f + Font.MEDIUM.metrics(11.0f).measureText(string + " "), f2, new ColorRGBA(255.0f, 85.0f, 85.0f));
    }

    /** Remap of {@code iIIIIiII.I (Lnet/minecraft/class_1297;)Z} - the hit-test the vanilla label mixin asks. */
    public boolean handlesEntity(Entity class_12972) {
        if (class_12972 instanceof PlayerEntity) {
            PlayerEntity class_16572 = (PlayerEntity)class_12972;
            if (class_16572 == minecraftClient.player) {
                if (minecraftClient.options.getPerspective().isFirstPerson()) {
                    return false;
                }
                return this.isValid2(PlayerTargetGroup.LOCAL_PLAYER);
            }
            if (Nametags.isRockstarUser(class_16572)) {
                return this.isValid2(PlayerTargetGroup.ROCKSTAR_USERS);
            }
            if (RockstarClient.create().getFriendListManager().containsFriend(class_16572.getName().getString())) {
                return this.isValid2(PlayerTargetGroup.FRIENDS);
            }
            return this.isValid2(PlayerTargetGroup.OTHERS);
        }
        if (class_12972 instanceof ItemEntity) {
            return this.isValid2(TargetGroup.ITEMS);
        }
        if (class_12972 instanceof HostileEntity) {
            return this.isValid2(TargetGroup.MOBS);
        }
        if (class_12972 instanceof AnimalEntity) {
            return this.isValid2(TargetGroup.ANIMALS);
        }
        return false;
    }

    private float getScale(Entity class_12972) {
        if (minecraftClient.player.age % 25 == 0) {
            // the original leaves this branch empty
        }
        float f = class_12972.distanceTo((Entity)minecraftClient.player);
        return MathHelper.clamp((float)(1.0f - f / 20.0f), (float)0.5f, (float)1.0f);
    }

    private static boolean isRockstarUser(PlayerEntity player) {
        return RockstarClient.create().getFriendManager().isFriend(player.getName().getString());
    }

    private PlayerTargetGroup getPlayerGroup(PlayerEntity class_16572) {
        if (class_16572 == minecraftClient.player) {
            return PlayerTargetGroup.LOCAL_PLAYER;
        }
        if (RockstarClient.create().getFriendListManager().containsFriend(class_16572.getName().getString())) {
            return PlayerTargetGroup.FRIENDS;
        }
        return PlayerTargetGroup.OTHERS;
    }

    private Vec2f getScreenPosition(Entity class_12972, float f) {
        Vec3d class_2432 = ProjectionUtils.interpolateEntityPosition(class_12972, f)
            .add(0.0, class_12972.getBoundingBox().getLengthY() + 0.65, 0.0);
        return ProjectionUtils.projectToScreen(class_2432);
    }

    private void collectEntities() {
        this.trackedEntities.clear();
        this.itemGroupCache.clear();
        this.displayNameCache.clear();
        this.nameWidthCache.clear();
        this.originalNameWidthCache.clear();
        for (Entity class_12972 : minecraftClient.world.getEntities()) {
            if (!this.handlesEntity(class_12972)
                || class_12972.getType() != EntityType.PLAYER && class_12972.getType() != EntityType.ITEM
                    && !(class_12972 instanceof HostileEntity) && !(class_12972 instanceof AnimalEntity)) continue;
            this.trackedEntities.add(class_12972);
        }
    }

    private List<List<ItemEntity>> groupItemEntities() {
        LinkedList<List<ItemEntity>> linkedList = new LinkedList<List<ItemEntity>>();
        HashSet<ItemEntity> hashSet = new HashSet<ItemEntity>();
        for (Entity class_12972 : this.trackedEntities) {
            ItemEntity class_15422;
            if (!(class_12972 instanceof ItemEntity) || hashSet.contains(class_15422 = (ItemEntity)class_12972)) continue;
            LinkedList<ItemEntity> linkedList2 = new LinkedList<ItemEntity>();
            linkedList2.add(class_15422);
            hashSet.add(class_15422);
            for (Entity class_12973 : this.trackedEntities) {
                ItemEntity class_15423;
                if (!(class_12973 instanceof ItemEntity) || hashSet.contains(class_15423 = (ItemEntity)class_12973)
                    || !(class_15422.squaredDistanceTo((Entity)class_15423) < 1.0)) continue;
                linkedList2.add(class_15423);
                hashSet.add(class_15423);
            }
            linkedList.add(linkedList2);
        }
        return linkedList;
    }

    private void drawBackgrounds(OverlayBatchBuilder nametagBatch, OverlayBatchBuilder itemBatch, MatrixStack class_45872, List<List<ItemEntity>> list, float f) {
        Vec2f class_2412;
        for (Entity class_12972 : this.trackedEntities) {
            if (class_12972.getType() != EntityType.PLAYER && !(class_12972 instanceof HostileEntity) && !(class_12972 instanceof AnimalEntity)
                || (class_2412 = this.getScreenPosition(class_12972, f)) == null) continue;
            this.drawEntityBackground(nametagBatch, class_45872, class_12972, class_2412);
        }
        for (List<ItemEntity> list2 : list) {
            if (list2.isEmpty() || (class_2412 = this.getScreenPosition(list2.getFirst(), f)) == null
                || !this.backgroundSetting.isEnabled()) continue;
            this.drawItemGroupBackground(itemBatch, class_45872, list2, class_2412);
        }
        for (Entity class_12972 : this.trackedEntities) {
            if (class_12972.getType() != EntityType.ITEM || (class_2412 = this.getScreenPosition(class_12972, f)) == null) continue;
            this.drawContainerBackground(itemBatch, class_45872, (ItemEntity)class_12972, class_2412);
        }
    }

    private void drawTexts(OverlayBatchBuilder nametagBatch, MatrixStack class_45872, List<List<ItemEntity>> list, float f) {
        Vec2f class_2412;
        for (Entity class_12972 : this.trackedEntities) {
            if (class_12972.getType() != EntityType.PLAYER && !(class_12972 instanceof HostileEntity) && !(class_12972 instanceof AnimalEntity)
                || (class_2412 = this.getScreenPosition(class_12972, f)) == null) continue;
            this.drawEntityText(nametagBatch, class_45872, class_12972, class_2412);
        }
        for (List<ItemEntity> list2 : list) {
            if (list2.isEmpty() || (class_2412 = this.getScreenPosition(list2.getFirst(), f)) == null) continue;
            this.drawItemGroupText(nametagBatch, class_45872, list2, class_2412);
        }
        for (Entity class_12972 : this.trackedEntities) {
            if (class_12972.getType() != EntityType.ITEM || (class_2412 = this.getScreenPosition(class_12972, f)) == null) continue;
            this.drawContainerText(nametagBatch, class_45872, (ItemEntity)class_12972, class_2412);
        }
    }

    private Text getDisplayText(Entity class_12973) {
        return this.displayNameCache.computeIfAbsent(class_12973, class_12972 -> {
            Text class_25612 = this.buildDisplayText((Entity)class_12972);
            return class_12972 instanceof PlayerEntity && this.isReallyWorld()
                ? class_25612
                : NametagTextUtils.sanitize(class_25612, Font.MEDIUM);
        });
    }

    private float getNameWidth(Entity class_12972) {
        return this.nameWidthCache.computeIfAbsent(class_12972, this::measureNameWidth).floatValue();
    }

    private Float measureNameWidth(Entity class_12972) {
        return Float.valueOf(this.computeNameWidth(class_12972));
    }

    private float computeNameWidth(Entity class_12972) {
        Text class_25612 = this.getDisplayText(class_12972);
        if (class_12972 instanceof PlayerEntity && this.isReallyWorld()) {
            float f = WorldTextBatch.getTextWidthWithLayouts(Font.MEDIUM, Font.NOTO, class_25612.getString(), 11.0f);
            return 22.0f + f + 4.0f;
        }
        float f = OverlayBatchBuilder.measureTextComponentWithLayout(Font.MEDIUM, class_25612, 11.0f);
        if (class_12972 instanceof PlayerEntity) {
            return 22.0f + f + 4.0f;
        }
        return f + 8.0f;
    }

    private boolean isNameReplaced(Entity class_12972) {
        if (!TextCaptureController.isCaptureActive()) {
            return false;
        }
        String string = this.getDisplayText(class_12972).getString();
        return TextCaptureController.transform(string) != string;
    }

    private float getOriginalNameWidth(Entity class_12973) {
        if (!TextCaptureController.isCaptureActive()) {
            return this.getNameWidth(class_12973);
        }
        return this.originalNameWidthCache.computeIfAbsent(class_12973, class_12972 -> {
            float[] fArray = new float[1];
            TextCaptureController.runWithoutCapture(() -> {
                fArray[0] = this.computeNameWidth((Entity)class_12972);
            });
            return Float.valueOf(fArray[0]);
        }).floatValue();
    }

    private void drawHeads(OverlayBatchBuilder nametagBatch, MatrixStack class_45872, float f) {
        for (Entity class_12972 : this.trackedEntities) {
            int n;
            if (class_12972.getType() != EntityType.PLAYER || !(class_12972 instanceof AbstractClientPlayerEntity)) continue;
            AbstractClientPlayerEntity class_7422 = (AbstractClientPlayerEntity)class_12972;
            Vec2f class_2412 = this.getScreenPosition(class_12972, f);
            if (class_2412 == null || (n = this.headAtlas.getHeadIndex(class_7422.getSkinTextures().texture())) < 0) continue;
            float f2 = this.getScale(class_12972);
            float f3 = this.getNameWidth(class_12972);
            class_45872.push();
            class_45872.translate(class_2412.x, class_2412.y, 0.0f);
            class_45872.scale(f2, f2, 1.0f);
            float f4 = -f3 / 2.0f + 4.0f;
            float f5 = 7.0f;
            nametagBatch.queueTexturedQuad(class_45872.peek().getPositionMatrix(), f4, f5,
                this.headAtlas.getU(n), this.headAtlas.getV(n), this.headAtlas.getTileSize(),
                -this.getOriginalNameWidth(class_12972) / 2.0f + 4.0f);
            class_45872.pop();
        }
    }

    private void drawArmorRows(PreHudRenderEvent preHudRenderEvent, MatrixStack class_45872, float f) {
        for (Entity class_12972 : this.trackedEntities) {
            Vec2f class_2412;
            PlayerEntity class_16572;
            BooleanSetting booleanSetting;
            if (class_12972.getType() != EntityType.PLAYER
                || (booleanSetting = this.getSettingForScope("esp.nametags.show_armor",
                    this.getPlayerGroup(class_16572 = (PlayerEntity)class_12972))) == null
                || !booleanSetting.isEnabled()
                || (class_2412 = this.getScreenPosition(class_12972, f)) == null) continue;
            this.drawArmorRow(preHudRenderEvent, class_45872, class_16572, class_2412);
        }
    }

    private void drawContainerItems(PreHudRenderEvent preHudRenderEvent, MatrixStack class_45872, float f) {
        for (Entity class_12972 : this.trackedEntities) {
            Vec2f class_2412;
            if (class_12972.getType() != EntityType.ITEM || (class_2412 = this.getScreenPosition(class_12972, f)) == null) continue;
            this.drawContainerItemBatch(preHudRenderEvent, class_45872, (ItemEntity)class_12972, class_2412);
        }
    }

    private Text buildPlayerDisplayText(Entity class_12972) {
        NameProtect nameProtect = RockstarClient.create().getModuleRegistry().getModule(NameProtect.class);
        FriendListManager friendListManager = RockstarClient.create().getFriendListManager();
        boolean bl = nameProtect.getHideFriendsSetting().isEnabled();
        String string = nameProtect.getFriendFakeNameSetting().getValue();
        boolean bl2 = bl && friendListManager.containsFriend(class_12972.getName().getString()) && nameProtect.isEnabled();
        boolean bl3 = class_12972 == minecraftClient.player && nameProtect.isEnabled();
        boolean bl4 = TextCaptureController.isCaptureAvailable();
        if (bl4 && (bl2 || bl3)) {
            nameProtect.replacePlayerOrServerName(class_12972.getName().getString());
            bl2 = false;
            bl3 = false;
        }
        MutableText class_52502 = Text.empty();
        if (class_12972 instanceof PlayerEntity) {
            PlayerEntity class_16572 = (PlayerEntity)class_12972;
            int n = (int)EntityUtils.getPlayerHealth(class_16572);
            class_52502 = Text.of((String)(" [" + String.valueOf(n == 1000 ? "?" : Integer.valueOf(n)) + "]")).copy().withColor(-2142128);
        }
        if (bl2) {
            return Text.of((String)string).copy().append((Text)class_52502);
        }
        if (bl3) {
            return Text.of((String)nameProtect.getFakeNameSetting().getValue()).copy().append((Text)class_52502);
        }
        if (nameProtect.isEnabled() && nameProtect.getStreamerModeSetting().isEnabled()) {
            return Text.of((String)nameProtect.replacePlayerOrServerName(class_12972.getName().getString())).copy().append((Text)class_52502);
        }
        return class_12972.getDisplayName().copy().append((Text)class_52502);
    }

    private Text buildDisplayText(Entity class_12972) {
        if (class_12972 instanceof PlayerEntity) {
            return this.buildPlayerDisplayText(class_12972);
        }
        if (class_12972 instanceof LivingEntity) {
            LivingEntity class_13092 = (LivingEntity)class_12972;
            int n = (int)class_13092.getHealth();
            MutableText class_52502 = Text.of((String)(" [" + n + "]")).copy().withColor(-2142128);
            return class_12972.getDisplayName().copy().append((Text)class_52502);
        }
        return class_12972.getDisplayName().copy();
    }

    private void drawEntityBackground(OverlayBatchBuilder nametagBatch, MatrixStack class_45872, Entity class_12972, Vec2f class_2412) {
        ColorRGBA colorRGBA;
        if (!(class_12972 instanceof LivingEntity)) {
            return;
        }
        float f = this.getScale(class_12972);
        float f2 = this.getNameWidth(class_12972);
        class_45872.push();
        class_45872.translate(class_2412.x, class_2412.y, 0.0f);
        class_45872.scale(f, f, 1.0f);
        if (class_12972 instanceof PlayerEntity
            && RockstarClient.create().getFriendListManager().containsFriend(((PlayerEntity)class_12972).getName().getString())) {
            colorRGBA = new ColorRGBA(14.0f, 32.0f, 16.0f, 235.0f);
        } else {
            colorRGBA = new ColorRGBA(12.0f, 12.0f, 12.0f, 235.0f);
        }
        float f3 = this.getOriginalNameWidth(class_12972);
        nametagBatch.queueColoredQuad(class_45872.peek().getPositionMatrix(), -f2 / 2.0f, 3.0f, f2, 22.0f, colorRGBA, -f3 / 2.0f, f3);
        if (this.isNameReplaced(class_12972)) {
            float f4 = Math.max(f2, f3);
            nametagBatch.queueBackgroundQuad(class_45872.peek().getPositionMatrix(), -f4 / 2.0f, 3.0f, f4, 22.0f);
        }
        class_45872.pop();
    }

    private void drawEntityText(OverlayBatchBuilder nametagBatch, MatrixStack class_45872, Entity class_12972, Vec2f class_2412) {
        float f;
        float f2;
        if (!(class_12972 instanceof LivingEntity)) {
            return;
        }
        float f3 = this.getScale(class_12972);
        float f4 = this.getNameWidth(class_12972);
        class_45872.push();
        class_45872.translate(class_2412.x, class_2412.y, 0.0f);
        class_45872.scale(f3, f3, 1.0f);
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        Text class_25612 = this.getDisplayText(class_12972);
        float f5 = Font.MEDIUM.metrics(11.0f).getFontMetricsFloat();
        float f6 = 3.0f + (22.0f - f5) / 2.0f;
        float f7 = this.getOriginalNameWidth(class_12972);
        if (class_12972 instanceof PlayerEntity) {
            f2 = -f4 / 2.0f + 4.0f + 14.0f + 4.0f;
            f = -f7 / 2.0f + 4.0f + 14.0f + 4.0f;
        } else {
            f = f2 = -nametagBatch.measureTextComponent(class_25612, 11.0f) / 2.0f;
        }
        if (!(class_12972 instanceof PlayerEntity) || !this.isReallyWorld()) {
            nametagBatch.queueTextComponentWithShadow(matrix4f, class_25612, 11.0f, f2, f6, 0.0f, f);
        }
        if (class_12972 instanceof PlayerEntity) {
            ItemStack class_17992;
            DonorItemParser.DonorItem donorItem;
            PlayerEntity class_16572 = (PlayerEntity)class_12972;
            if (!this.isReallyWorld()
                && (donorItem = DonorItemParser.parseDonorItemFallback(class_17992 = class_16572.getOffHandStack())) != null
                && (donorItem.isSphereCategory()
                    || donorItem.isTalismanCategory() && donorItem.getMetadataSource() != DonorItemParser.MetadataSource.LORE_TEXT)) {
                Text class_25613 = this.buildItemLabel(class_17992, donorItem);
                float f8 = nametagBatch.measureTextComponent(class_25613, 9.0f);
                nametagBatch.queueTextComponent(matrix4f, class_25613, 9.0f, -f8 / 2.0f, 27.0f, 0.0f);
            }
        }
        class_45872.pop();
    }

    private boolean isReallyWorld() {
        return ServerDetector.isServerProfileSupported(ServerProfile.REALLYWORLD)
            || ServerDetector.isServerProfileSupported(ServerProfile.REALLYWORLD_VARIANTS);
    }

    private void drawWorldNames(PreHudRenderEvent preHudRenderEvent, MatrixStack class_45872, float f) {
        if (!this.isReallyWorld()) {
            return;
        }
        WorldTextBatch worldTextBatch = new WorldTextBatch(Font.MEDIUM, Font.NOTO);
        worldTextBatch.setDrawContext(preHudRenderEvent.getContext());
        for (Entity class_12972 : this.trackedEntities) {
            Vec2f class_2412;
            if (class_12972.getType() != EntityType.PLAYER || (class_2412 = this.getScreenPosition(class_12972, f)) == null) continue;
            float f2 = this.getScale(class_12972);
            float f3 = this.getNameWidth(class_12972);
            Text class_25612 = this.getDisplayText(class_12972);
            float f4 = Font.MEDIUM.metrics(11.0f).getFontMetricsFloat();
            float f5 = 3.0f + (22.0f - f4) / 2.0f;
            float f6 = -f3 / 2.0f + 4.0f + 14.0f + 4.0f;
            class_45872.push();
            class_45872.translate(class_2412.x, class_2412.y, 0.0f);
            class_45872.scale(f2, f2, 1.0f);
            worldTextBatch.queueText(class_45872.peek().getPositionMatrix(), class_25612, 11.0f, f6, f5, 0.0f);
            class_45872.pop();
        }
        worldTextBatch.flush();
    }

    private void drawArmorRow(PreHudRenderEvent preHudRenderEvent, MatrixStack class_45872, PlayerEntity class_16572, Vec2f class_2412) {
        float f = this.getScale((Entity)class_16572);
        class_45872.push();
        class_45872.translate(class_2412.x, class_2412.y, 0.0f);
        class_45872.scale(f, f, 1.0f);
        LinkedList<ItemStack> linkedList = new LinkedList<ItemStack>();
        linkedList.add(class_16572.getInventory().armor.get(3));
        linkedList.add(class_16572.getInventory().armor.get(2));
        linkedList.add(class_16572.getInventory().armor.get(1));
        linkedList.add(class_16572.getInventory().armor.get(0));
        linkedList.add(class_16572.getMainHandStack());
        linkedList.add(class_16572.getOffHandStack());
        linkedList.removeIf(ItemStack::isEmpty);
        if (!linkedList.isEmpty()) {
            float f2 = (float)(linkedList.size() - 1) * 18.0f + 16.0f;
            float f3 = -f2 / 2.0f;
            try (CustomDrawContext.ItemBatch itemBatch = preHudRenderEvent.getContext().beginItemBatch();){
                for (int j = 0; j < linkedList.size(); ++j) {
                    preHudRenderEvent.getContext().drawBatchItem((ItemStack)linkedList.get(j), f3 + (float)j * 18.0f, -15.0f);
                }
            }
        }
        class_45872.pop();
    }

    private void drawItemUse(PreHudRenderEvent preHudRenderEvent, MatrixStack class_45872, PlayerEntity class_16572, Vec2f class_2412) {
        if (!class_16572.isUsingItem()) {
            return;
        }
        ItemStack class_17992 = class_16572.getActiveItem();
        if (class_17992.isEmpty()) {
            return;
        }
        UseAction class_18392 = class_17992.getUseAction();
        if (class_18392 != UseAction.EAT && class_18392 != UseAction.DRINK) {
            return;
        }
        int n = class_17992.getMaxUseTime((LivingEntity)class_16572);
        int n2 = class_16572.getItemUseTimeLeft();
        if (n <= 0) {
            return;
        }
        float f = 1.0f - (float)n2 / (float)n;
        float f2 = class_16572.distanceTo((Entity)minecraftClient.player);
        float f3 = MathHelper.clamp((float)(1.0f - f2 / 20.0f), (float)0.5f, (float)1.0f) * 0.4f;
        float f4 = 80.0f;
        float f5 = 80.0f;
        class_45872.push();
        class_45872.translate(class_2412.x - f4 / 2.0f, class_2412.y - f5 / 2.0f, 0.0f);
        ItemRenderUtils.translateAndScale(class_45872, f4 / 2.0f, f5 / 2.0f, f3);
        preHudRenderEvent.getContext().drawBlurredRect(0.0f, 0.0f, f4, f5, 25.0f, 3.0f, WidgetState.uniform(14.0f), ColorPalette.WHITE);
        preHudRenderEvent.getContext().drawSquircle(0.0f, 0.0f, f4, f5, 3.0f, WidgetState.uniform(14.0f), new ColorRGBA(9.0f, 9.0f, 11.0f).mulAlpha(0.5f));
        preHudRenderEvent.getContext().drawCircleProgress(f4 / 2.0f, f5 / 2.0f, 28.0f, 4.0f, f, ColorPalette.getAccentColor());
        preHudRenderEvent.getContext().drawItem(class_17992, 24.0f, 24.0f, 2.0f);
        ItemRenderUtils.popMatrix(class_45872);
        class_45872.pop();
    }

    private void drawItemGroupBackground(OverlayBatchBuilder itemBatch, MatrixStack class_45872, List<ItemEntity> list, Vec2f class_2412) {
        if (list.isEmpty()) {
            return;
        }
        float f = this.getScale((Entity)list.getFirst());
        List<ItemGroupEntry> list2 = this.getItemGroupEntries(list);
        if (list2.isEmpty()) {
            return;
        }
        int n = (int)Font.MEDIUM.metrics(11.0f).getFontMetricsFloat();
        int n2 = 0;
        for (ItemGroupEntry itemGroupEntry : list2) {
            n2 = Math.max(n2, (int)this.measureItemGroupEntry(itemBatch, itemGroupEntry));
        }
        class_45872.push();
        class_45872.translate(class_2412.x, class_2412.y, 0.0f);
        class_45872.scale(f, f, 1.0f);
        int n3 = n * list2.size() + 3 * (list2.size() - 1);
        itemBatch.queueColoredOverlay(class_45872.peek().getPositionMatrix(), (float)(-n2) / 2.0f - 3.0f, 2.0f,
            (float)(n2 + 6), (float)(n3 + 6), new ColorRGBA(0.0f, 0.0f, 0.0f, 150.0f));
        class_45872.pop();
    }

    private void drawItemGroupText(OverlayBatchBuilder nametagBatch, MatrixStack class_45872, List<ItemEntity> list, Vec2f class_2412) {
        if (list.isEmpty()) {
            return;
        }
        float f = this.getScale((Entity)list.getFirst());
        List<ItemGroupEntry> list2 = this.getItemGroupEntries(list);
        if (list2.isEmpty()) {
            return;
        }
        int n = (int)Font.MEDIUM.metrics(11.0f).getFontMetricsFloat();
        class_45872.push();
        class_45872.translate(class_2412.x, class_2412.y, 0.0f);
        class_45872.scale(f, f, 1.0f);
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        for (int j = 0; j < list2.size(); ++j) {
            ItemGroupEntry itemGroupEntry = list2.get(j);
            float f2 = nametagBatch.measureTextComponent(itemGroupEntry.name(), 11.0f);
            float f3 = -this.measureItemGroupEntry(nametagBatch, itemGroupEntry) / 2.0f;
            float f4 = 5 + j * (n + 3);
            nametagBatch.queueTextComponent(matrix4f, itemGroupEntry.name(), 11.0f, f3, f4, 0.0f);
            nametagBatch.queueText(matrix4f, this.formatItemGroupCount(itemGroupEntry), 11.0f, f3 + f2, f4, 0.0f, ColorRGBA.WHITE.getRGB());
        }
        class_45872.pop();
    }

    private void drawContainerBackground(OverlayBatchBuilder itemBatch, MatrixStack class_45872, ItemEntity class_15422, Vec2f class_2412) {
        List<ItemStack> list = ItemMetadataUtils.getContainerContents(class_15422.getStack());
        if (list.isEmpty()) {
            return;
        }
        float f = this.getScale((Entity)class_15422);
        int n = Math.min(list.size(), 9);
        int n2 = (int)Math.ceil((float)list.size() / 9.0f);
        int n3 = n * 18 + 4;
        int n4 = n2 * 18 + 4;
        class_45872.push();
        class_45872.translate(class_2412.x, class_2412.y, 0.0f);
        class_45872.scale(f, f, 1.0f);
        itemBatch.queueColoredOverlay(class_45872.peek().getPositionMatrix(), (float)(-n3) / 2.0f, (float)(-n4) / 2.0f,
            (float)n3, (float)n4, new ColorRGBA(0.0f, 0.0f, 0.0f, 180.0f));
        class_45872.pop();
    }

    private void drawContainerItemBatch(PreHudRenderEvent preHudRenderEvent, MatrixStack class_45872, ItemEntity class_15422, Vec2f class_2412) {
        List<ItemStack> list = ItemMetadataUtils.getContainerContents(class_15422.getStack());
        if (list.isEmpty()) {
            return;
        }
        float f = this.getScale((Entity)class_15422);
        int n = Math.min(list.size(), 9);
        int n2 = (int)Math.ceil((float)list.size() / 9.0f);
        int n3 = n * 18 + 4;
        int n4 = n2 * 18 + 4;
        class_45872.push();
        class_45872.translate(class_2412.x, class_2412.y, 0.0f);
        class_45872.scale(f, f, 1.0f);
        try (CustomDrawContext.ItemBatch itemBatch = preHudRenderEvent.getContext().beginItemBatch();){
            for (int j = 0; j < list.size(); ++j) {
                int n5 = j % 9 * 18 - n3 / 2 + 3;
                int n6 = j / 9 * 18 - n4 / 2 + 3;
                preHudRenderEvent.getContext().drawBatchItem(list.get(j), n5, n6);
            }
        }
        class_45872.pop();
    }

    private void drawContainerText(OverlayBatchBuilder nametagBatch, MatrixStack class_45872, ItemEntity class_15422, Vec2f class_2412) {
        List<ItemStack> list = ItemMetadataUtils.getContainerContents(class_15422.getStack());
        if (list.isEmpty()) {
            return;
        }
        float f = this.getScale((Entity)class_15422);
        int n = Math.min(list.size(), 9);
        int n2 = (int)Math.ceil((float)list.size() / 9.0f);
        int n3 = n * 18 + 4;
        int n4 = n2 * 18 + 4;
        class_45872.push();
        class_45872.translate(class_2412.x, class_2412.y, 0.0f);
        class_45872.scale(f, f, 1.0f);
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        nametagBatch.queueText(matrix4f, class_15422.getDisplayName().getString(), 11.0f,
            (float)(-n3) / 2.0f + 0.5f, (float)(-n4) / 2.0f - 11.0f, 0.0f, ColorRGBA.WHITE.getRGB());
        for (int j = 0; j < list.size(); ++j) {
            ItemStack class_17992 = list.get(j);
            if (class_17992.getCount() <= 1) continue;
            float f2 = (float)(j % 9 * 18) - (float)n3 / 2.0f + 3.0f;
            float f3 = (float)(j / 9 * 18) - (float)n4 / 2.0f + 3.0f;
            String string = String.valueOf(class_17992.getCount());
            nametagBatch.queueText(matrix4f, string, 11.0f,
                f2 + 16.0f - Font.MEDIUM.metrics(11.0f).measureText(string), f3 + 9.0f, 0.0f, ColorRGBA.WHITE.getRGB());
        }
        class_45872.pop();
    }

    /** Remap of {@code iIIIIiII.I (DD)Z} - used by the HUD manager for nametag click-through. */
    public boolean isNametagAt(double d, double d2) {
        Vec2f class_2412;
        if (!this.isValid()) {
            return false;
        }
        for (Entity class_12972 : this.trackedEntities) {
            if (class_12972.getType() != EntityType.PLAYER || (class_2412 = this.getScreenPosition(class_12972, 1.0f)) == null
                || !this.isInsideNametag(class_12972, class_2412, d, d2)) continue;
            return true;
        }
        return false;
    }

    private boolean isInsideNametag(Entity class_12972, Vec2f class_2412, double d, double d2) {
        float f = this.getScale(class_12972);
        float f2 = this.getNameWidth(class_12972);
        float f3 = f2 * f;
        float f4 = 22.0f * f;
        float f5 = class_2412.x - f3 / 2.0f;
        float f6 = class_2412.y + 3.0f * f;
        return moscow.rockstar.ui.core.UiUtils.contains(f5, f6, f3, f4, d, d2);
    }

    private void requestInvsee(String string) {
        if (minecraftClient.player == null) {
            return;
        }
        this.pendingInvseeTarget = string;
        this.pendingInvseeDeadline = System.currentTimeMillis() + 1500L;
        try {
            minecraftClient.player.networkHandler.sendChatCommand("invsee " + string);
        }
        catch (RuntimeException runtimeException) {
            this.pendingInvseeDeadline = 0L;
        }
    }

    private void tickPendingInvsee() {
        if (this.pendingInvseeTarget == null) {
            return;
        }
        if (minecraftClient.currentScreen instanceof HandledScreen) {
            this.pendingInvseeTarget = null;
            this.pendingInvseeDeadline = 0L;
            return;
        }
        if (minecraftClient.player == null || minecraftClient.world == null) {
            this.pendingInvseeTarget = null;
            this.pendingInvseeDeadline = 0L;
            return;
        }
        if (System.currentTimeMillis() < this.pendingInvseeDeadline) {
            return;
        }
        String string = this.pendingInvseeTarget;
        this.pendingInvseeTarget = null;
        this.pendingInvseeDeadline = 0L;
        RockstarClient.create().getNavigationCommandService().executeCommand(
            RockstarClient.create().getNavigationCommandService().getCommandPrefix() + "invsee " + string);
    }

    private Text buildItemLabel(ItemStack class_17992, DonorItemParser.DonorItem donorItem) {
        MutableText class_52502;
        if ((donorItem == null || class_17992.contains(DataComponentTypes.CUSTOM_NAME))
            && !(class_52502 = NametagTextUtils.sanitize(class_17992.getName(), Font.MEDIUM)).getString().isBlank()) {
            return class_52502;
        }
        ColorRGBA colorRGBA = donorItem != null ? donorItem.getDisplayColor(class_17992) : null;
        String string = donorItem != null ? donorItem.getDisplayName(class_17992) : class_17992.getItem().getName().getString();
        return Text.literal((String)NametagTextUtils.smallCaps(string))
            .withColor((colorRGBA != null ? colorRGBA : ColorRGBA.WHITE).getRGB() & 0xFFFFFF);
    }

    private List<ItemGroupEntry> getItemGroupEntries(List<ItemEntity> list) {
        return this.itemGroupCache.computeIfAbsent(list.getFirst(), class_15422 -> {
            LinkedHashMap<String, ItemGroupEntry> linkedHashMap = new LinkedHashMap<String, ItemGroupEntry>();
            for (ItemEntity class_15423 : list) {
                ItemStack class_17992 = class_15423.getStack();
                Text class_25612 = this.buildItemLabel(class_17992, DonorItemParser.parseDonorItem(class_17992));
                ItemGroupEntry itemGroupEntry = linkedHashMap.get(class_25612.getString());
                int n = (itemGroupEntry == null ? 0 : itemGroupEntry.count()) + class_17992.getCount();
                linkedHashMap.put(class_25612.getString(), new ItemGroupEntry(class_25612, n));
            }
            return new ArrayList<ItemGroupEntry>(linkedHashMap.values());
        });
    }

    private float measureItemGroupEntry(OverlayBatchBuilder batchBuilder, ItemGroupEntry itemGroupEntry) {
        return batchBuilder.measureTextComponent(itemGroupEntry.name(), 11.0f)
            + Font.MEDIUM.metrics(11.0f).measureText(this.formatItemGroupCount(itemGroupEntry));
    }

    private String formatItemGroupCount(ItemGroupEntry itemGroupEntry) {
        return " " + itemGroupEntry.count() + "x";
    }

    /** Remap of the public static {@code iIIIIiII.I (Lnet/minecraft/class_1297;)Lnet/minecraft/class_2561;}. */
    public static Text getNametagText(Entity class_12972) {
        int n;
        if (class_12972.getDisplayName() == null) {
            return Text.empty();
        }
        NameProtect nameProtect = RockstarClient.create().getModuleRegistry().getModule(NameProtect.class);
        String string = nameProtect.isEnabled()
            ? nameProtect.replacePlayerOrServerName(class_12972.getName().getString())
            : class_12972.getDisplayName().getString();
        MutableText class_52502 = Text.of((String)string).copy();
        if (!(class_12972 instanceof LivingEntity)) {
            return class_52502;
        }
        LivingEntity class_13092 = (LivingEntity)class_12972;
        if (class_12972 instanceof PlayerEntity) {
            PlayerEntity class_16572 = (PlayerEntity)class_12972;
            n = (int)EntityUtils.getPlayerHealth(class_16572);
        } else {
            n = (int)class_13092.getHealth();
        }
        if (!class_52502.getString().endsWith(" ")) {
            class_52502.append(" ");
        }
        return class_52502.append((Text)Text.of((String)("[" + String.valueOf(n == 1000 ? "?" : Integer.valueOf(n)) + "]")).copy().withColor(-2142128));
    }

    @Generated
    public List<Entity> getTrackedEntities() {
        return this.trackedEntities;
    }

    @Generated
    public Map<ItemEntity, List<ItemGroupEntry>> getItemGroupCache() {
        return this.itemGroupCache;
    }

    @Generated
    public Map<Entity, Text> getDisplayNameCache() {
        return this.displayNameCache;
    }

    @Generated
    public Map<Entity, Float> getNameWidthCache() {
        return this.nameWidthCache;
    }

    @Generated
    public Map<Entity, Float> getOriginalNameWidthCache() {
        return this.originalNameWidthCache;
    }

    @Generated
    public BooleanSetting getNametagsSetting() {
        return this.nametagsSetting;
    }

    @Generated
    public BooleanSetting getShowArmorSetting() {
        return this.showArmorSetting;
    }

    @Generated
    public BooleanSetting getShowItemUseSetting() {
        return this.showItemUseSetting;
    }

    @Generated
    public BooleanSetting getBackgroundSetting() {
        return this.backgroundSetting;
    }

    @Generated
    public PlayerHeadAtlas getHeadAtlas() {
        return this.headAtlas;
    }

    @Generated
    public String getPendingInvseeTarget() {
        return this.pendingInvseeTarget;
    }

    @Generated
    public long getPendingInvseeDeadline() {
        return this.pendingInvseeDeadline;
    }

    @Generated
    public EventListener<PreHudRenderEvent> getPreHudRenderListener() {
        return this.preHudRenderListener;
    }

    @Generated
    public EventListener<ReceivePacketEvent> getReceivePacketListener() {
        return this.receivePacketListener;
    }

    /** Remap of the record {@code rockstar/ilIlil/iIIIIiII$I} (name, count). */
    public record ItemGroupEntry(Text name, int count) {
    }
}
