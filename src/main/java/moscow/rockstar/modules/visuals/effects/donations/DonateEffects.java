/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.SoundInstance
 *  net.minecraft.Entity
 *  net.minecraft.TntEntity
 *  net.minecraft.PotionEntity
 *  net.minecraft.Vec3d
 *  net.minecraft.ExplosionS2CPacket
 *  net.minecraft.PlaySoundS2CPacket
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.Identifier
 *  net.minecraft.MatrixStack
 *  net.minecraft.BuiltBuffer
 */
package moscow.rockstar.modules.visuals.effects.donations;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.items.DonorItemParser;
import moscow.rockstar.mixin.accessors.AbstractSoundInstanceAccessor;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.modules.visuals.effects.donations.DonationEffect;
import moscow.rockstar.modules.visuals.effects.donations.DonationItem;
import moscow.rockstar.modules.visuals.effects.donations.DonationType;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.world.WorldEffectDispatcher;
import moscow.rockstar.render.world.BlockParticleRenderer;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.SettingOwner;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.BuiltBuffer;
import pyrock.events.game.SoundEvent;
import pyrock.events.game.WorldChangeEvent;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Donate Effects", category=ModuleCategory.VISUALS, disableLocked=true, description="modules.descriptions.donate_effects")
public class DonateEffects
extends Module {
    private BooleanSetting sonar;
    private BooleanSetting shockwave;
    private BooleanSetting potions;
    private static final float donationEffectScale = 8.0f;
    private static final float donationEffectSpeed = 1.0f;
    private static final float donationEffectRadius = 0.5f;
    private MultiBooleanSetting targets;
    private MultiBooleanSetting.Option dez;
    private MultiBooleanSetting.Option aura;
    private MultiBooleanSetting.Option pil;
    private MultiBooleanSetting.Option fire;
    private MultiBooleanSetting.Option boom;
    private MultiBooleanSetting.Option trapka;
    private MultiBooleanSetting.Option stun;
    private final Map<Integer, DonationItem> savedDonationEffects = new HashMap<Integer, DonationItem>();
    private final List<DonationType> donationEntries = new CopyOnWriteArrayList<DonationType>();
    private static final long lastDonationTime = 1000L;
    private static final double effectRadius = 4.0;
    private final Map<Integer, DonationEffect> donationValuesById = new ConcurrentHashMap<Integer, DonationEffect>();
    private static final Map<String, Identifier> donationColorsById = Map.of("potion-radiation", RockstarClient.resourceId("icons/potions/radio.png"), "potion-paladin", RockstarClient.resourceId("icons/potions/shield.png"), "potion-assassin", RockstarClient.resourceId("icons/potions/sword.png"), "potion-holy-water", RockstarClient.resourceId("icons/potions/holy.png"), "potion-popper", RockstarClient.resourceId("icons/potions/bomb.png"), "potion-drowsiness", RockstarClient.resourceId("icons/potions/moon.png"), "potion-rage", RockstarClient.resourceId("icons/potions/angry.png"));
    private final EventListener<ReceivePacketEvent> onReceivePacketEvent = receivePacketEvent -> {
        if (!this.sonar.isEnabled() && !this.shockwave.isEnabled()) {
            return;
        }
        Object packet = receivePacketEvent.getPacket();
        if (packet instanceof ExplosionS2CPacket explosionPacket && this.isDonationPositionValid(explosionPacket.center())) {
            this.updateDonationEffectColor(this.trapka.isSelected(), explosionPacket.center(), 8.0f, 3.0f, 2.0f, new ColorRGBA(255.0f, 155.0f, 0.0f));
        }
        if (!(packet instanceof PlaySoundS2CPacket soundPacket)) {
            return;
        }
        String soundId = soundPacket.getSound().getIdAsString();
        if (ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME)) {
            if (soundId.contains("minecraft:entity.illusioner.mirror_move") && (soundPacket.getVolume() == 1.0f || soundPacket.getPitch() == 0.0f)) {
                this.updateDonationEffect(this.dez.isSelected(), DonateEffects.getDonationPosition(soundPacket), 20.0f, 1.5f, 2.0f);
            }
            if ((soundId.contains("minecraft:entity.wither.break_block") || soundId.contains("minecraft:block.piston.extend")) && soundPacket.getVolume() == 0.7f || soundPacket.getVolume() == 0.2f && soundPacket.getPitch() == 1.0f || (double)soundPacket.getPitch() == 0.5) {
                this.updateDonationEffectColor(this.trapka.isSelected(), DonateEffects.getDonationPosition(soundPacket), 5.0f, 3.0f, 2.0f, new ColorRGBA(255.0f, 255.0f, 255.0f));
            }
            if (soundId.contains("minecraft:entity.illusioner.cast_spell") && (soundPacket.getVolume() == 1.0f || soundPacket.getPitch() == 1.0f)) {
                this.updateDonationEffectColor(this.aura.isSelected(), DonateEffects.getDonationPosition(soundPacket), 5.0f, 3.0f, 2.0f, new ColorRGBA(255.0f, 255.0f, 255.0f));
            }
            if (soundId.contains("minecraft:entity.illusioner.prepare_blindness") && (soundPacket.getVolume() == 1.0f || soundPacket.getPitch() == 0.0f)) {
                this.updateDonationEffectColor(this.pil.isSelected(), DonateEffects.getDonationPosition(soundPacket), 15.0f, 1.5f, 2.0f, new ColorRGBA(255.0f, 255.0f, 255.0f));
            }
        } else if (ServerDetector.isServerProfileSupported(ServerProfile.FUNSKY)) {
            if (soundId.contains("minecraft:item.firecharge.use") && ((double)soundPacket.getVolume() == 0.5 || soundPacket.getPitch() == 1.0f)) {
                this.updateDonationEffectColor(this.fire.isSelected(), DonateEffects.getDonationPosition(soundPacket), 20.0f, 1.5f, 2.0f, new ColorRGBA(255.0f, 155.0f, 0.0f));
            }
            if (soundId.contains("minecraft:block.beacon.activate") && ((double)soundPacket.getVolume() == 0.5 || soundPacket.getPitch() == 1.0f)) {
                this.updateDonationEffectColor(this.aura.isSelected(), DonateEffects.getDonationPosition(soundPacket), 5.0f, 3.0f, 2.0f, new ColorRGBA(255.0f, 255.0f, 255.0f));
            }
            if (soundId.contains("minecraft:entity.illusioner.mirror_move") && ((double)soundPacket.getVolume() == 0.5 || soundPacket.getPitch() == 1.0f)) {
                this.updateDonationEffectColor(this.pil.isSelected(), DonateEffects.getDonationPosition(soundPacket), 15.0f, 1.5f, 2.0f, new ColorRGBA(255.0f, 255.0f, 255.0f));
            }
            if (soundId.contains("minecraft:entity.illusioner.prepare_blindness") && ((double)soundPacket.getVolume() == 0.5 || soundPacket.getPitch() == 1.0f)) {
                this.updateDonationEffect(this.dez.isSelected(), DonateEffects.getDonationPosition(soundPacket), 20.0f, 1.5f, 2.0f);
            }
        } else if (ServerDetector.isInventoryServer()) {
            if (soundId.contains("minecraft:entity.generic.explode") && (soundPacket.getVolume() == 1.0f || soundPacket.getPitch() == 1.0f)) {
                this.updateDonationEffectColor(this.trapka.isSelected(), DonateEffects.getDonationPosition(soundPacket), 8.0f, 3.0f, 2.0f, new ColorRGBA(255.0f, 155.0f, 0.0f));
            }
            if (soundId.contains("minecraft:block.beacon.deactivate") && ((double)soundPacket.getVolume() == 1.5 || soundPacket.getPitch() == 1.0f)) {
                this.updateDonationEffectColor(this.stun.isSelected(), DonateEffects.getDonationPosition(soundPacket), 25.0f, 1.5f, 2.0f, new ColorRGBA(255.0f, 255.0f, 255.0f));
            }
        }
    };
    private final EventListener<SoundEvent> onSoundEvent = soundEvent -> {
        if (!this.sonar.isEnabled() && !this.shockwave.isEnabled()) {
            return;
        }
        if (!ServerDetector.isInventoryServer()) {
            return;
        }
        SoundInstance class_11132 = soundEvent.getSound();
        if (class_11132 == null) {
            return;
        }
        String string = class_11132.getId().toString();
        float f = ((AbstractSoundInstanceAccessor)class_11132).rockstar$getVolume();
        if (string.equals("minecraft:entity.generic.explode") && f == 4.0f) {
            BlockPos adminsky = new BlockPos((int)class_11132.getX(), (int)class_11132.getY(), (int)class_11132.getZ());
            this.updateDonationEffectColor(this.boom.isSelected(), adminsky.toCenterPos(), 4.0f, 1.5f, 2.0f, new ColorRGBA(255.0f, 155.0f, 0.0f));
        }
    };
    private final EventListener<WorldChangeEvent> onWorldChangeEvent = worldChangeEvent -> {
        this.savedDonationEffects.clear();
        this.donationEntries.clear();
        this.donationValuesById.clear();
    };
    private final EventListener<Render3DEvent> onRender3DEvent = render3DEvent -> {
        if (this.donationEntries.isEmpty()) {
            return;
        }
        if (minecraftClient == null || DonateEffects.minecraftClient.world == null) {
            return;
        }
        MatrixStack class_45872 = render3DEvent.getMatrices();
        class_45872.push();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        HashMap<Identifier, List<DonationType>> hashMap = new HashMap<Identifier, List<DonationType>>();
        for (DonationType object : this.donationEntries) {
            hashMap.computeIfAbsent(object.texture, class_29602 -> new ArrayList<DonationType>()).add(object);
        }
        for (Map.Entry<Identifier, List<DonationType>> entry : hashMap.entrySet()) {
            Identifier class_29603 = entry.getKey();
            List<DonationType> list = entry.getValue();
            RenderSystem.setShaderTexture((int)0, (Identifier)class_29603);
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
            BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            for (DonationType donationType : list) {
                if (donationType.isExpired()) continue;
                donationType.updatePosition();
                donationType.render((Render3DEvent)render3DEvent, class_2872);
            }
            BuiltBuffer class_98012 = class_2872.endNullable();
            if (class_98012 == null) continue;
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_98012);
        }
        this.donationEntries.removeIf(DonationType::isExpired);
        RenderSystem.depthMask((boolean)true);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        class_45872.pop();
    };

    public DonateEffects() {
        this.initializeSettings();
        WorldEffectDispatcher.INSTANCE.initializeRenderer();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.sonar = new BooleanSetting(this, "modules.settings.donate_effects.sonar");
        this.shockwave = new BooleanSetting(this, "modules.settings.donate_effects.shockwave").enable();
        this.potions = new BooleanSetting(this, "modules.settings.donate_effects.potions").enable();
        this.targets = new MultiBooleanSetting((SettingOwner)this, "modules.settings.donate_effects.targets", () -> !this.sonar.isEnabled() && !this.shockwave.isEnabled());
        this.dez = new MultiBooleanSetting.Option(this.targets, "modules.settings.donate_effects.targets.dez").select();
        this.aura = new MultiBooleanSetting.Option(this.targets, "modules.settings.donate_effects.targets.aura").select();
        this.pil = new MultiBooleanSetting.Option(this.targets, "modules.settings.donate_effects.targets.pil").select();
        this.fire = new MultiBooleanSetting.Option(this.targets, "modules.settings.donate_effects.targets.fire").select();
        this.boom = new MultiBooleanSetting.Option(this.targets, "modules.settings.donate_effects.targets.boom").select();
        this.trapka = new MultiBooleanSetting.Option(this.targets, "modules.settings.donate_effects.targets.trapka").select();
        this.stun = new MultiBooleanSetting.Option(this.targets, "modules.settings.donate_effects.targets.stun").select();
    }

    private boolean isDonationPositionValid(Vec3d VanillaChestLootTableGenerator) {
        long l = System.currentTimeMillis();
        for (DonationEffect donationEffect : this.donationValuesById.values()) {
            if (donationEffect.getExpiresAt() <= l || !(donationEffect.getPosition().squaredDistanceTo(VanillaChestLootTableGenerator) <= 16.0)) continue;
            return true;
        }
        return false;
    }

    private void resetDonationState() {
        if (DonateEffects.minecraftClient.world == null) {
            if (!this.donationValuesById.isEmpty()) {
                this.donationValuesById.clear();
            }
            return;
        }
        long l = System.currentTimeMillis();
        for (Entity class_12972 : DonateEffects.minecraftClient.world.getEntities()) {
            if (!(class_12972 instanceof TntEntity)) continue;
            TntEntity class_15412 = (TntEntity)class_12972;
            this.donationValuesById.put(class_15412.getId(), new DonationEffect(class_15412.getPos(), l + 1000L));
        }
        this.donationValuesById.values().removeIf(donationEffect -> donationEffect.getExpiresAt() <= l);
    }

    private static Vec3d getDonationPosition(PlaySoundS2CPacket class_27672) {
        return new BlockPos((int)class_27672.getX(), (int)class_27672.getY(), (int)class_27672.getZ()).toCenterPos();
    }

    private void updateDonationEffect(boolean bl, Vec3d VanillaChestLootTableGenerator, float f, float f2, float f3) {
        this.updateDonationEffectColor(bl, VanillaChestLootTableGenerator, f, f2, f3, ColorPalette.getAccentColor());
    }

    private void updateDonationEffectColor(boolean bl, Vec3d VanillaChestLootTableGenerator, float f, float f2, float f3, ColorRGBA colorRGBA) {
        if (this.sonar.isEnabled() && bl) {
            BlockParticleRenderer.INSTANCE.enqueueBlockParticlesWithColor(VanillaChestLootTableGenerator, f, f2, f3, colorRGBA);
        }
        if (this.shockwave.isEnabled()) {
            WorldEffectDispatcher.INSTANCE.addWorldEffectWithOpacity(VanillaChestLootTableGenerator, 8.0f, 1.0f, colorRGBA, 0.5f);
        }
    }

    @Override
    public void onTick() {
        Object object;
        DonationItem donationItem;
        super.onTick();
        this.resetDonationState();
        if (!this.potions.isEnabled()) {
            if (!this.savedDonationEffects.isEmpty()) {
                this.savedDonationEffects.clear();
            }
            return;
        }
        if (minecraftClient == null || DonateEffects.minecraftClient.player == null || DonateEffects.minecraftClient.world == null) {
            return;
        }
        HashSet<Integer> hashSet = new HashSet<Integer>();
        for (Object object2 : DonateEffects.minecraftClient.world.getEntities()) {
            if (!(object2 instanceof PotionEntity)) continue;
            PotionEntity class_16862 = (PotionEntity)object2;
            int n = class_16862.getId();
            hashSet.add(n);
            donationItem = this.savedDonationEffects.get(n);
            object = class_16862.getPos();
            if (donationItem != null) {
                this.savedDonationEffects.put(n, new DonationItem((Vec3d)object, donationItem.getItemStack()));
                continue;
            }
            this.savedDonationEffects.put(n, new DonationItem((Vec3d)object, class_16862.getStack().copy()));
        }
        Iterator<Map.Entry<Integer, DonationItem>> iterator = this.savedDonationEffects.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, DonationItem> entry = iterator.next();
            int n = entry.getKey();
            if (hashSet.contains(n)) continue;
            DonationItem donationItem2 = entry.getValue();
            Vec3d donationPosition = donationItem2.getPosition();
            String donationLabel = DonorItemParser.getDonorItemLabel(donationItem2.getItemStack());
            if (donationLabel == null) {
                iterator.remove();
                continue;
            }
            Identifier class_29602 = donationColorsById.getOrDefault(donationLabel, RockstarClient.resourceId("icons/add.png"));
            ColorRGBA colorRGBA = switch (donationLabel) {
                case "potion-radiation" -> new ColorRGBA(99.0f, 255.0f, 0.0f);
                case "potion-paladin" -> new ColorRGBA(74.0f, 180.0f, 255.0f);
                case "potion-assassin" -> new ColorRGBA(255.0f, 68.0f, 68.0f);
                case "potion-holy-water" -> new ColorRGBA(255.0f, 255.0f, 255.0f);
                case "potion-popper" -> new ColorRGBA(255.0f, 170.0f, 0.0f);
                case "potion-drowsiness" -> new ColorRGBA(136.0f, 85.0f, 255.0f);
                case "potion-rage" -> new ColorRGBA(255.0f, 69.0f, 0.0f);
                default -> ColorPalette.getAccentColor();
            };
            for (int i = 0; i < 20; ++i) {
                double d = 1.5;
                double d2 = Math.random() * Math.PI * 2.0;
                double d3 = Math.sqrt(Math.random()) * d;
                double d4 = Math.cos(d2) * d3;
                double d5 = Math.sin(d2) * d3;
                Vec3d VanillaChestLootTableGenerator = donationPosition.add(d4, 0.0, d5);
                boolean bl = "potion-popper".equals(donationLabel) || "potion-paladin".equals(donationLabel);
                this.donationEntries.add(new DonationType(VanillaChestLootTableGenerator, colorRGBA, class_29602, bl));
            }
            iterator.remove();
        }
    }
}
