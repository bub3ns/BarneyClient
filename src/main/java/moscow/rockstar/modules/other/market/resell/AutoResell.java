/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.PlayerEntity
 *  net.minecraft.SlotActionType
 *  net.minecraft.Screen
 *  net.minecraft.HandledScreen
 *  net.minecraft.GameMessageS2CPacket
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.modules.other.market.resell;

import moscow.rockstar.events.EventListener;
import moscow.rockstar.market.MarketInventoryAnalyzer;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.other.market.resell.ResellMode;
import moscow.rockstar.util.Timer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.jetbrains.annotations.NotNull;
import pyrock.events.network.ReceivePacketEvent;

@ModuleInfo(name="Auto Resell", category=ModuleCategory.OTHER, description="modules.descriptions.auto_resell")
public class AutoResell
extends Module {
    private ResellMode resellWorkflowState = ResellMode.WAITING;
    private final Timer resellTimer = new Timer();
    private long resellDelayMillis = 60000L;
    private final EventListener<ReceivePacketEvent> receivePacketListener = receivePacketEvent -> {
        Object object = receivePacketEvent.getPacket();
        if (object instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket class_74392 = (GameMessageS2CPacket)object;
            if (((String)(object = class_74392.content().getString())).contains("\u041f\u0440\u0435\u0434\u043c\u0435\u0442\u044b \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043f\u0435\u0440\u0435\u0432\u044b\u0441\u0442\u0430\u0432\u043b\u0435\u043d\u044b")) {
                this.resellDelayMillis = 60000L;
                this.resellWorkflowState = ResellMode.STORAGE_FULL;
                this.resellTimer.reset();
            }
            if (((String)object).contains("\u041f\u043e\u0434\u043e\u0436\u0434\u0438\u0442\u0435") && ((String)object).contains("\u0441\u0435\u043a")) {
                for (String string : ((String)object).split(" ")) {
                    if (!string.matches("\\d+")) continue;
                    this.resellDelayMillis = (long)Integer.parseInt(string) * 1000L + 500L;
                    this.resellWorkflowState = ResellMode.IDLE;
                    this.resellTimer.reset();
                    break;
                }
            }
        }
    };

    @Override
    public void onTick() {
        long l = (long)((float)AutoResell.minecraftClient.player.networkHandler.getPlayerListEntry(AutoResell.minecraftClient.player.getUuid()).getLatency() * 2.5f + MathUtils.interpolateRandomDouble(24.0, 59.0));
        switch (this.resellWorkflowState.ordinal()) {
            case 0: {
                this.processResellWorkflow();
                break;
            }
            case 1: {
                this.scheduleResellAction(l);
                break;
            }
            case 2: {
                this.sendResellCommand(l);
                break;
            }
            case 3: {
                this.inspectResellContainer();
            }
        }
    }

    private void processResellWorkflow() {
        if (this.resellTimer.hasElapsed(this.resellDelayMillis)) {
            this.resellWorkflowState = ResellMode.WAITING;
            this.resellTimer.reset();
        }
    }

    private void inspectResellContainer() {
        if (this.resellTimer.hasElapsed(500L)) {
            AutoResell.minecraftClient.player.closeHandledScreen();
            this.resellWorkflowState = ResellMode.IDLE;
            this.resellTimer.reset();
        }
    }

    private void scheduleResellAction(long l) {
        Screen class_4372 = AutoResell.minecraftClient.currentScreen;
        if (class_4372 instanceof HandledScreen) {
            HandledScreen BlockStateProviderType = (HandledScreen)class_4372;
            if (BlockStateProviderType.getTitle().getString().contains("\u0425\u0440\u0430\u043d\u0438\u043b\u0438\u0449\u0435")) {
                this.resellWorkflowState = ResellMode.SELLING;
                this.resellTimer.reset();
                return;
            }
            if (this.isResellStatusMessage(BlockStateProviderType.getTitle().getString())) {
                if (this.resellTimer.hasElapsed(l + 200L)) {
                    AutoResell.minecraftClient.interactionManager.clickSlot(BlockStateProviderType.getScreenHandler().syncId, 46, 0, SlotActionType.PICKUP, (PlayerEntity)AutoResell.minecraftClient.player);
                    this.resellTimer.reset();
                }
                return;
            }
            if (this.resellTimer.hasElapsed(l + 200L)) {
                AutoResell.minecraftClient.player.closeHandledScreen();
                this.resellTimer.reset();
            }
            return;
        }
        if (this.resellTimer.hasElapsed(l + 200L)) {
            AutoResell.minecraftClient.player.networkHandler.sendChatCommand("ah");
            this.resellTimer.reset();
        }
    }

    private void sendResellCommand(long l) {
        HandledScreen BlockStateProviderType;
        Screen class_4372 = AutoResell.minecraftClient.currentScreen;
        if (!(class_4372 instanceof HandledScreen) || !(BlockStateProviderType = (HandledScreen)class_4372).getTitle().getString().contains("\u0425\u0440\u0430\u043d\u0438\u043b\u0438\u0449\u0435")) {
            return;
        }
        if (this.resellTimer.hasElapsed(l + 200L)) {
            AutoResell.minecraftClient.interactionManager.clickSlot(BlockStateProviderType.getScreenHandler().syncId, 52, 0, SlotActionType.PICKUP, (PlayerEntity)AutoResell.minecraftClient.player);
            this.resellTimer.reset();
        }
    }

    public boolean isResellStatusMessage(@NotNull String string) {
        return MarketInventoryAnalyzer.isPriceOrMarketText(string);
    }
}
