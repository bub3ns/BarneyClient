/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.ItemEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.ItemStack
 *  net.minecraft.RenderLayer
 *  net.minecraft.GameMode
 *  net.minecraft.ItemPickupAnimationS2CPacket
 *  net.minecraft.DrawContext
 *  net.minecraft.Screen
 *  net.minecraft.InventoryScreen
 *  net.minecraft.AbstractClientPlayerEntity
 *  net.minecraft.Registries
 */
package moscow.rockstar.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import moscow.rockstar.api.commands.CommandBuilder;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.world.GameMode;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.network.packet.Packet;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.registry.Registries;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.render.HudRenderEvent;
import ua.mintantileak.spk.Compile;

public class EventHandlerListener
implements ClientAccess {
    private PlayerEntity selectedPlayer;
    private final Map<UUID, InventorySnapshot> inventorySnapshots = new HashMap<UUID, InventorySnapshot>();
    private final EventListener<HudRenderEvent> hudRenderListener = hudRenderEvent -> {
        if (EventHandlerListener.minecraftClient.world != null) {
            for (AbstractClientPlayerEntity TrackedPosition : EventHandlerListener.minecraftClient.world.getPlayers()) {
                this.refreshPlayerInventory((PlayerEntity)TrackedPosition);
            }
        }
        if (this.selectedPlayer != null) {
            this.openPlayerInventory(this.selectedPlayer);
            this.selectedPlayer = null;
        }
    };
    private final EventListener<ReceivePacketEvent> inventoryPacketListener = receivePacketEvent -> {
        if (EventHandlerListener.minecraftClient.world == null) {
            return;
        }
        Packet<?> packet = receivePacketEvent.getPacket();
        if (!(packet instanceof ItemPickupAnimationS2CPacket pickupPacket)) {
            return;
        }
        Entity collector = EventHandlerListener.minecraftClient.world.getEntityById(pickupPacket.getCollectorEntityId());
        if (!(collector instanceof PlayerEntity player)) {
            return;
        }
        Entity collectedEntity = EventHandlerListener.minecraftClient.world.getEntityById(pickupPacket.getEntityId());
        if (!(collectedEntity instanceof ItemEntity itemEntity)) {
            return;
        }
        ItemStack collectedStack = itemEntity.getStack();
        if (collectedStack.isEmpty()) {
            return;
        }
        this.inventorySnapshots.computeIfAbsent(player.getUuid(), uuid -> new InventorySnapshot(player)).mergeItemStack(collectedStack);
    };

    public EventHandlerListener() {
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    @Compile
    public ServiceRegistry buildInvseeCommand() {
        return CommandBuilder.command("invsee")
            .aliases("\u0438\u043d\u0432\u0441\u0438", "seeinv")
            .description("commands.invsee.description")
            .argument("player", argument -> argument.validator(PluginResolver::resolveValue))
            .handler(this::selectPlayer)
            .build();
    }

    @Compile
    private void selectPlayer(DispatchContext dispatchContext) {
        if (EventHandlerListener.minecraftClient.world == null || EventHandlerListener.minecraftClient.player == null) {
            return;
        }
        String string = (String)dispatchContext.getArguments().getFirst();
        if (string == null || string.trim().isEmpty()) {
            return;
        }
        string = string.trim();
        String string2 = string.toLowerCase(Locale.ROOT);
        AbstractClientPlayerEntity TrackedPosition = null;
        for (AbstractClientPlayerEntity class_7423 : EventHandlerListener.minecraftClient.world.getPlayers()) {
            String string3 = class_7423.getName().getString();
            if (string3.equalsIgnoreCase(string)) {
                TrackedPosition = class_7423;
                break;
            }
            if (TrackedPosition != null || !string3.toLowerCase(Locale.ROOT).startsWith(string2)) continue;
            TrackedPosition = class_7423;
        }
        if (TrackedPosition == null) {
            return;
        }
        this.selectedPlayer = TrackedPosition;
    }

    private void openPlayerInventory(PlayerEntity class_16572) {
        ItemStack class_17992;
        int n;
        InventorySnapshot inventorySnapshot = this.inventorySnapshots.get(class_16572.getUuid());
        if (inventorySnapshot == null) {
            inventorySnapshot = new InventorySnapshot(class_16572);
            inventorySnapshot.captureInventory(class_16572);
            this.inventorySnapshots.put(class_16572.getUuid(), inventorySnapshot);
        }
        boolean bl = ServerDetector.isServerProfileSupported(ServerProfile.HOLYWORLD);
        AbstractClientPlayerEntity TrackedPosition = new AbstractClientPlayerEntity(EventHandlerListener.minecraftClient.world, class_16572.getGameProfile()){

            public boolean isSpectator() {
                return false;
            }

            public boolean isCreative() {
                return ClientAccess.minecraftClient.interactionManager != null && ClientAccess.minecraftClient.interactionManager.getCurrentGameMode() == GameMode.CREATIVE;
            }
        };
        List<ItemStack> list = inventorySnapshot.getInventoryEntries();
        int n2 = TrackedPosition.getInventory().main.size();
        for (n = 0; n < n2; ++n) {
            if (n < list.size()) {
                class_17992 = list.get(n).copy();
                if (bl && n == 0) {
                    class_17992.setCount(1);
                }
                TrackedPosition.getInventory().main.set(n, class_17992);
                continue;
            }
            TrackedPosition.getInventory().main.set(n, ItemStack.EMPTY);
        }
        for (n = 0; n < TrackedPosition.getInventory().armor.size(); ++n) {
            class_17992 = inventorySnapshot.getHotbarStack(n);
            if (class_17992.isEmpty()) {
                TrackedPosition.getInventory().armor.set(n, ItemStack.EMPTY);
                continue;
            }
            ItemStack class_17993 = class_17992.copy();
            if (bl) {
                class_17993.setCount(1);
            }
            TrackedPosition.getInventory().armor.set(n, class_17993);
        }
        ItemStack class_17994 = inventorySnapshot.getOffhandStack();
        if (class_17994.isEmpty()) {
            TrackedPosition.getInventory().offHand.set(0, ItemStack.EMPTY);
        } else {
            class_17992 = class_17994.copy();
            if (bl) {
                class_17992.setCount(1);
            }
            TrackedPosition.getInventory().offHand.set(0, class_17992);
        }
        TrackedPosition.getInventory().selectedSlot = 0;
        minecraftClient.send(() -> minecraftClient.setScreen((Screen)new PlayerInventoryScreen(TrackedPosition)));
    }

    private void refreshPlayerInventory(PlayerEntity class_16572) {
        InventorySnapshot inventorySnapshot = this.inventorySnapshots.computeIfAbsent(class_16572.getUuid(), uUID -> new InventorySnapshot(class_16572));
        inventorySnapshot.captureInventory(class_16572);
    }

    static String getItemIdentifier(ItemStack class_17992) {
        return Registries.ITEM.getId(class_17992.getItem()).toString();
    }

    static final class InventorySnapshot {
        private final LinkedHashMap<String, ItemStack> stacksByItemId = new LinkedHashMap();
        private final ItemStack[] hotbarStacks;
        private ItemStack offhandStack = ItemStack.EMPTY;
        private ItemStack mainHandStack = ItemStack.EMPTY;

        InventorySnapshot(PlayerEntity class_16572) {
            this.hotbarStacks = new ItemStack[class_16572.getInventory().armor.size()];
            Arrays.fill(this.hotbarStacks, ItemStack.EMPTY);
        }

        void captureInventory(PlayerEntity class_16572) {
            for (int i = 0; i < class_16572.getInventory().main.size(); ++i) {
                this.mergeItemStack((ItemStack)class_16572.getInventory().main.get(i));
            }
            ItemStack class_17992 = class_16572.getMainHandStack();
            if (!class_17992.isEmpty()) {
                this.mergeItemStack(class_17992);
                this.mainHandStack = class_17992.copy();
            }
            for (int i = 0; i < this.hotbarStacks.length && i < class_16572.getInventory().armor.size(); ++i) {
                ItemStack class_17993 = (ItemStack)class_16572.getInventory().armor.get(i);
                if (class_17993.isEmpty()) continue;
                this.hotbarStacks[i] = class_17993.copy();
            }
            ItemStack class_17994 = class_16572.getOffHandStack();
            if (!class_17994.isEmpty()) {
                this.offhandStack = class_17994.copy();
                this.mergeItemStack(class_17994);
            }
        }

        void mergeItemStack(ItemStack class_17992) {
            if (class_17992 == null || class_17992.isEmpty()) {
                return;
            }
            String string = EventHandlerListener.getItemIdentifier(class_17992);
            ItemStack class_17993 = this.stacksByItemId.get(string);
            if (class_17993 == null) {
                this.stacksByItemId.put(string, class_17992.copy());
            } else if (class_17992.getCount() > class_17993.getCount()) {
                class_17993.setCount(class_17992.getCount());
            }
        }

        List<ItemStack> getInventoryEntries() {
            String string;
            ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>();
            String string2 = string = this.mainHandStack.isEmpty() ? null : EventHandlerListener.getItemIdentifier(this.mainHandStack);
            if (string != null) {
                arrayList.add(this.mainHandStack.copy());
            }
            for (Map.Entry<String, ItemStack> entry : this.stacksByItemId.entrySet()) {
                if (entry.getKey().equals(string)) continue;
                arrayList.add(entry.getValue().copy());
            }
            return arrayList;
        }

        ItemStack getHotbarStack(int n) {
            return this.hotbarStacks[n] == null ? ItemStack.EMPTY : this.hotbarStacks[n];
        }

        ItemStack getOffhandStack() {
            return this.offhandStack == null ? ItemStack.EMPTY : this.offhandStack;
        }
    }

    static final class PlayerInventoryScreen
    extends InventoryScreen {
        private final AbstractClientPlayerEntity displayedPlayer;
        private float screenX;
        private float screenY;

        PlayerInventoryScreen(AbstractClientPlayerEntity TrackedPosition) {
            super((PlayerEntity)TrackedPosition);
            this.displayedPlayer = TrackedPosition;
        }

        public void render(DrawContext ServerConfigException, int n, int n2, float f) {
            this.screenX = n;
            this.screenY = n2;
            super.render(ServerConfigException, n, n2, f);
        }

        protected void drawBackground(DrawContext ServerConfigException, float f, int n, int n2) {
            ServerConfigException.drawTexture(RenderLayer::getGuiTextured, BACKGROUND_TEXTURE, this.x, this.y, 0.0f, 0.0f, this.backgroundWidth, this.backgroundHeight, 256, 256);
            InventoryScreen.drawEntity((DrawContext)ServerConfigException, (int)(this.x + 26), (int)(this.y + 8), (int)(this.x + 75), (int)(this.y + 78), (int)30, (float)0.0625f, (float)this.screenX, (float)this.screenY, (LivingEntity)this.displayedPlayer);
        }
    }
}
