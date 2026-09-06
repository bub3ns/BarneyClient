/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Item
 *  net.minecraft.Block
 *  net.minecraft.Identifier
 *  net.minecraft.Registries
 */
package pyrock.classes.settings;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.api.scripts.ScriptModule;
import moscow.rockstar.settings.BlockItemSetting;
import moscow.rockstar.settings.SettingOwner;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import pyrock.classes.PyEspElement;
import pyrock.classes.PyHudElement;
import pyrock.classes.PyModule;

public class PyBlockSetting {
    private final BlockItemSetting setting;

    public PyBlockSetting(PyModule pyModule, String string) {
        this.setting = new BlockItemSetting(pyModule.getModule(), string);
        if (!(pyModule.getModule() instanceof ScriptModule)) {
            ScriptDescriptor.registerModuleSetting(pyModule.getModule(), this.setting);
        }
    }

    public PyBlockSetting(PyHudElement pyHudElement, String string) {
        this.setting = new BlockItemSetting((SettingOwner)((Object)pyHudElement), string);
    }

    public PyBlockSetting(PyEspElement pyEspElement, String string) {
        this.setting = new BlockItemSetting((SettingOwner)((Object)pyEspElement.getElement()), string);
    }

    public PyBlockSetting(BlockItemSetting blockItemSetting) {
        this.setting = blockItemSetting;
    }

    public PyBlockSetting select(String string) {
        ScriptDescriptor.rememberSettingValue(this.setting);
        this.setting.selectRegistryId(PyBlockSetting.parse(string));
        return this;
    }

    public PyBlockSetting toggle(String string) {
        Block class_22482 = PyBlockSetting.block(string);
        if (class_22482 != null) {
            ScriptDescriptor.rememberSettingValue(this.setting);
            this.setting.toggleBlock(class_22482);
        }
        return this;
    }

    public boolean isSelected(String string) {
        return this.setting.isRegistryIdSelected(PyBlockSetting.parse(string));
    }

    public List<String> getSelected() {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (Identifier class_29602 : this.setting.getSelectedIds()) {
            arrayList.add(class_29602.toString());
        }
        return arrayList;
    }

    public int count() {
        return this.setting.getSelectedCount();
    }

    public PyBlockSetting allow(String string) {
        Block class_22482 = PyBlockSetting.block(string);
        if (class_22482 != null) {
            this.setting.includeBlock(class_22482);
        }
        return this;
    }

    public PyBlockSetting allowAll() {
        this.setting.includeBlocks(new Block[0]);
        return this;
    }

    public PyBlockSetting allowItem(String string) {
        Item class_17922;
        Identifier class_29602 = PyBlockSetting.parse(string);
        Item class_17923 = class_17922 = class_29602 == null ? null : (Item)Registries.ITEM.get(class_29602);
        if (class_17922 != null) {
            this.setting.includeItem(class_17922);
        }
        return this;
    }

    public PyBlockSetting allowAllItems() {
        this.setting.enableAllItems();
        return this;
    }

    private static Identifier parse(String string) {
        if (string == null || string.isBlank()) {
            return null;
        }
        String string2 = string.trim();
        return Identifier.tryParse((String)(string2.indexOf(58) < 0 ? "minecraft:" + string2 : string2));
    }

    private static Block block(String string) {
        Identifier class_29602 = PyBlockSetting.parse(string);
        return class_29602 == null ? null : (Block)Registries.BLOCK.get(class_29602);
    }

    @Generated
    public BlockItemSetting getSetting() {
        return this.setting;
    }
}

