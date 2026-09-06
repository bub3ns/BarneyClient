/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.zxing.BarcodeFormat
 *  com.google.zxing.MultiFormatWriter
 *  com.google.zxing.client.j2se.MatrixToImageWriter
 *  com.google.zxing.common.BitMatrix
 *  lombok.Generated
 *  net.minecraft.NativeImage
 *  net.minecraft.NativeImageBackedTexture
 *  net.minecraft.AbstractTexture
 *  net.minecraft.Text
 *  net.minecraft.Identifier
 */
package moscow.rockstar.modules.other.games;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.awt.image.BufferedImage;
import java.nio.file.FileSystems;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.other.games.ActionRouletteMode;
import moscow.rockstar.modules.other.games.RouletteMode;
import moscow.rockstar.modules.other.games.SafeRouletteMode;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import pyrock.events.render.PreHudRenderEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Russian Roulette", category=ModuleCategory.OTHER, description="modules.descriptions.russian_roulette", hidden=true)
public class RussianRoulette
extends Module {
    private ModeSetting rouletteDifficultySetting;
    private RouletteMode safeRouletteOption;
    private RouletteMode actionRouletteOption;
    private final SecureRandom randomGenerator = new SecureRandom();
    private volatile Identifier resultTexture;
    private final Animation resultAnimation = new Animation(5000L, Easing.easeInOutCubicPolynomial);
    private volatile boolean gameInProgress;
    private final EventListener<PreHudRenderEvent> preHudRenderListener = preHudRenderEvent -> {
        if (this.resultTexture == null) {
            return;
        }
        if ((double)this.resultAnimation.getValue() == 1.0 && !this.gameInProgress) {
            this.gameInProgress = true;
        }
        this.resultAnimation.update(this.gameInProgress ? 0.0f : 1.0f);
    };

    public RussianRoulette() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.rouletteDifficultySetting = new ModeSetting((SettingOwner)this, "modules.settings.russian_roulette.difficulty", "modules.settings.russian_roulette.difficulty.description");
        this.safeRouletteOption = new SafeRouletteMode(this, this.rouletteDifficultySetting, "modules.settings.russian_roulette.easy");
        this.actionRouletteOption = new ActionRouletteMode(this, this.rouletteDifficultySetting, "modules.settings.russian_roulette.very_hard");
    }

    @Override
    public final void onEnable() {
        if (RussianRoulette.minecraftClient.world == null || RussianRoulette.minecraftClient.player == null) {
            return;
        }
        boolean bl = this.rollWinningChamber();
        this.displayRollResult(bl);
        this.applySafeRouletteMode();
        super.onEnable();
    }

    private boolean rollWinningChamber() {
        int[] nArray = new int[6];
        Arrays.setAll(nArray, n -> n == 5 ? 1 : 0);
        return nArray[this.randomGenerator.nextInt(nArray.length)] == 0;
    }

    private void displayRollResult(boolean bl) {
        boolean bl2 = this.actionRouletteOption.isSelected();
        String string = bl ? "rroulette.luck" : "rroulette.unlucky";
        Notification.info(Text.of((String)(bl2 ? Localization.translate(string + ".prize") : Localization.translate(string + ".simple"))));
        if (bl2) {
            this.showPrizeQrCode(bl ? "https://4lapy.ru/journal/info/taksa-osobennosti-porody-kharakter-soderzhanie/" : "https://pornhub.com");
        }
    }

    private void applySafeRouletteMode() {
        if (this.safeRouletteOption.isSelected()) {
            this.safeRouletteOption.applyMode();
        }
    }

    private void showPrizeQrCode(String string) {
        CompletableFuture.runAsync(() -> {
            try {
                BitMatrix bitMatrix = new MultiFormatWriter().encode(string, BarcodeFormat.QR_CODE, 300, 300);
                BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage((BitMatrix)bitMatrix);
                NativeImage LootTableData = this.createDynamicTexture(bufferedImage);
                String string2 = FileSystems.getDefault().getSeparator();
                minecraftClient.execute(() -> {
                    if (this.resultTexture != null) {
                        minecraftClient.getTextureManager().destroyTexture(this.resultTexture);
                    }
                    Identifier class_29602 = RockstarClient.resourceId("temp" + string2 + "qr" + string2 + String.valueOf(UUID.randomUUID()));
                    minecraftClient.getTextureManager().registerTexture(class_29602, (AbstractTexture)new NativeImageBackedTexture(LootTableData));
                    this.resultTexture = class_29602;
                    this.resultAnimation.update(1.0f);
                    this.gameInProgress = false;
                });
            }
            catch (Exception exception) {
                // empty catch block
            }
        });
    }

    private NativeImage createDynamicTexture(BufferedImage bufferedImage) {
        int n = bufferedImage.getWidth();
        int n2 = bufferedImage.getHeight();
        NativeImage LootTableData = new NativeImage(n, n2, true);
        int[] nArray = bufferedImage.getRGB(0, 0, n, n2, null, 0, n);
        for (int i = 0; i < nArray.length; ++i) {
            int n3 = i % n;
            int n4 = i / n;
            LootTableData.setColorArgb(n3, n4, nArray[i]);
        }
        return LootTableData;
    }

    @Generated
    public Identifier getRouletteTexture() {
        return this.resultTexture;
    }

    @Generated
    public Animation getGameAnimation() {
        return this.resultAnimation;
    }

    @Generated
    public boolean isGameActive() {
        return this.gameInProgress;
    }
}

