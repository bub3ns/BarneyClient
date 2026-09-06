/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  lombok.Generated
 *  net.minecraft.PlayerEntity
 *  net.minecraft.ScreenHandler
 *  net.minecraft.Item$TooltipContext
 *  net.minecraft.ItemStack
 *  net.minecraft.TooltipType
 *  net.minecraft.World
 *  net.minecraft.NbtCompound
 *  net.minecraft.Text
 *  net.minecraft.HandledScreen
 *  net.minecraft.Registries
 */
package moscow.rockstar.modules.player.inventory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import moscow.rockstar.auth.MicrosoftClientConfiguration;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.items.DonorItemParser;
import moscow.rockstar.items.ItemMetadataUtils;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.ui.notifications.ItemNotification;
import moscow.rockstar.ui.notifications.Notification;
import moscow.rockstar.util.Timer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.world.World;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.registry.Registries;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Item Dumper", category=ModuleCategory.OTHER, description="modules.descriptions.item_dumper")
public class ItemDumper
extends Module {
    private static final File itemDumpFile = new File(moscow.rockstar.core.ClientPaths.gameDirectory(), "item_dump.json");
    private BooleanSetting onlyCustomItemsSetting;
    private BooleanSetting showAlertsSetting;
    private final Map<String, JsonObject> dumpedItems = new LinkedHashMap<String, JsonObject>();
    private final Timer scanTimer = new Timer();
    private final Timer saveTimer = new Timer();
    private boolean savePending;

    public ItemDumper() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.onlyCustomItemsSetting = new BooleanSetting(this, "modules.settings.item_dumper.only_custom");
        this.showAlertsSetting = new BooleanSetting(this, "modules.settings.item_dumper.alerts");
    }

    @Override
    public void onEnable() {
        this.dumpedItems.clear();
        this.savePending = false;
        Notification.info(Text.of((String)("\u0414\u0430\u043c\u043f \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432 \u043f\u0438\u0448\u0435\u0442\u0441\u044f \u0432 " + itemDumpFile.getName() + ", \u043f\u043e\u0445\u043e\u0434\u0438 \u043f\u043e \u0430\u0443\u043a\u0446\u0438\u043e\u043d\u0443")));
        super.onEnable();
    }

    @Override
    public void onTick() {
        ScreenHandler class_17032;
        if (ItemDumper.minecraftClient.player == null || ItemDumper.minecraftClient.world == null) {
            return;
        }
        if (!this.scanTimer.hasElapsed(200L)) {
            return;
        }
        this.scanTimer.reset();
        if (ItemDumper.minecraftClient.currentScreen instanceof HandledScreen && (class_17032 = ItemDumper.minecraftClient.player.currentScreenHandler) != null && class_17032 != ItemDumper.minecraftClient.player.playerScreenHandler) {
            int n = Math.max(0, class_17032.slots.size() - 36);
            for (int i = 0; i < n; ++i) {
                this.collectItemData(class_17032.getSlot(i).getStack());
            }
        }
        if (this.savePending && this.saveTimer.hasElapsed(2000L)) {
            this.saveItemDump();
            this.savePending = false;
            this.saveTimer.reset();
        }
    }

    private void collectItemData(ItemStack class_17992) {
        String string;
        boolean bl;
        if (class_17992 == null || class_17992.isEmpty()) {
            return;
        }
        List<Text> tooltipLines = class_17992.getTooltip(Item.TooltipContext.create((World)ItemDumper.minecraftClient.world), (PlayerEntity)ItemDumper.minecraftClient.player, (TooltipType)TooltipType.BASIC);
        NbtCompound class_24872 = ItemMetadataUtils.getCustomData(class_17992);
        DonorItemParser.DonorItem donorItem = DonorItemParser.parseDonorItem(class_17992);
        boolean bl2 = bl = donorItem != null || class_24872 != null && !class_24872.isEmpty() || class_17992.hasEnchantments() || tooltipLines.size() > 1;
        if (this.onlyCustomItemsSetting.isEnabled() && !bl) {
            return;
        }
        String string2 = Registries.ITEM.getId(class_17992.getItem()).toString();
        String string3 = class_17992.getName().getString();
        String string4 = ItemMetadataUtils.cleanDisplayName(class_17992);
        ArrayList<String> arrayList = new ArrayList<String>();
        for (int i = 1; i < tooltipLines.size(); ++i) {
            string = tooltipLines.get(i).getString();
            if (string.isBlank()) continue;
            arrayList.add(string);
        }
        String string5 = string2 + "|" + string3 + "|" + String.join((CharSequence)"\u0001", arrayList);
        JsonObject existingRecord = this.dumpedItems.get(string5);
        if (existingRecord != null) {
            existingRecord.addProperty("seen", existingRecord.get("seen").getAsInt() + 1);
            return;
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", string2);
        jsonObject.addProperty("name", string3);
        jsonObject.addProperty("clean", string4);
        jsonObject.addProperty("count", (Number)class_17992.getCount());
        jsonObject.addProperty("seen", (Number)1);
        jsonObject.addProperty("damageable", Boolean.valueOf(class_17992.isDamageable()));
        if (class_17992.isDamageable()) {
            jsonObject.addProperty("damage", (Number)class_17992.getDamage());
            jsonObject.addProperty("maxDamage", (Number)class_17992.getMaxDamage());
        }
        JsonArray jsonArray = new JsonArray();
        for (String string6 : arrayList) {
            jsonArray.add(string6);
        }
        jsonObject.add("lore", (JsonElement)jsonArray);
        if (class_24872 != null && !class_24872.isEmpty()) {
            jsonObject.addProperty("nbt", class_24872.toString());
        }
        if (donorItem != null) {
            JsonObject jsonObject2 = new JsonObject();
            jsonObject2.addProperty("category", donorItem.getCategory().name());
            jsonObject2.addProperty("display", donorItem.getDisplayName(class_17992));
            if (donorItem.getRawName() != null) {
                jsonObject2.addProperty("type", donorItem.getRawName());
            }
            if (donorItem.getRarity() != null) {
                jsonObject2.addProperty("rank", donorItem.getRarity().name());
            }
            jsonObject2.addProperty("server", donorItem.getMetadataSource().name());
            jsonObject.add("detected", (JsonElement)jsonObject2);
        }
        this.dumpedItems.put(string5, jsonObject);
        this.savePending = true;
        if (this.showAlertsSetting.isEnabled()) {
            RockstarClient.create().getUiComponentProcessor().enqueueNotification(new ItemNotification(string4, class_17992.getItem()));
        }
    }

    private void saveItemDump() {
        try {
            JsonArray jsonArray = new JsonArray();
            for (JsonObject jsonObject : this.dumpedItems.values()) {
                jsonArray.add((JsonElement)jsonObject);
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("total", (Number)this.dumpedItems.size());
            jsonObject.add("items", (JsonElement)jsonArray);
            moscow.rockstar.util.JsonFiles.write(itemDumpFile, jsonObject);
        }
        catch (Exception exception) {
            System.err.println("Error saving item dump: " + exception.getMessage());
        }
    }

    @Override
    public void onDisable() {
        if (!this.dumpedItems.isEmpty()) {
            this.saveItemDump();
            Notification.info(Text.of((String)("\u0421\u043e\u0431\u0440\u0430\u043d\u043e \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432: " + this.dumpedItems.size() + " -> " + itemDumpFile.getPath())));
        }
        super.onDisable();
    }

    @Generated
    public BooleanSetting getOnlyCustomItemsSetting() {
        return this.onlyCustomItemsSetting;
    }

    @Generated
    public BooleanSetting getShowAlertsSetting() {
        return this.showAlertsSetting;
    }

    @Generated
    public Map<String, JsonObject> getDumpedItems() {
        return this.dumpedItems;
    }

    @Generated
    public Timer getScanTimer() {
        return this.scanTimer;
    }

    @Generated
    public Timer getSaveTimer() {
        return this.saveTimer;
    }

    @Generated
    public boolean isSavePending() {
        return this.savePending;
    }
}
