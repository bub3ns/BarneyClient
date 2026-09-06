/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.ArmorStandEntity
 *  net.minecraft.ChestMinecartEntity
 *  net.minecraft.MinecartEntity
 *  net.minecraft.FurnaceMinecartEntity
 *  net.minecraft.CommandBlockMinecartEntity
 *  net.minecraft.HopperMinecartEntity
 *  net.minecraft.TntMinecartEntity
 *  net.minecraft.BlockItem
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.AnvilBlock
 *  net.minecraft.BeaconBlock
 *  net.minecraft.BedBlock
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.BrewingStandBlock
 *  net.minecraft.AbstractCauldronBlock
 *  net.minecraft.ChestBlock
 *  net.minecraft.CommandBlock
 *  net.minecraft.CraftingTableBlock
 *  net.minecraft.DispenserBlock
 *  net.minecraft.DropperBlock
 *  net.minecraft.EnchantingTableBlock
 *  net.minecraft.EnderChestBlock
 *  net.minecraft.AbstractFurnaceBlock
 *  net.minecraft.HopperBlock
 *  net.minecraft.JukeboxBlock
 *  net.minecraft.LoomBlock
 *  net.minecraft.AbstractSignBlock
 *  net.minecraft.ShulkerBoxBlock
 *  net.minecraft.SkullBlock
 *  net.minecraft.TrappedChestBlock
 *  net.minecraft.TrapdoorBlock
 *  net.minecraft.TripwireBlock
 *  net.minecraft.CobwebBlock
 *  net.minecraft.BarrelBlock
 *  net.minecraft.BellBlock
 *  net.minecraft.CartographyTableBlock
 *  net.minecraft.GrindstoneBlock
 *  net.minecraft.LecternBlock
 *  net.minecraft.SmithingTableBlock
 *  net.minecraft.StonecutterBlock
 *  net.minecraft.ComposterBlock
 *  net.minecraft.RespawnAnchorBlock
 *  net.minecraft.Registries
 */
package moscow.rockstar.modules.player.movement;

import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.combat.attacks.Aura;
import moscow.rockstar.modules.player.interaction.InteractionListener;
import moscow.rockstar.settings.BlockItemSetting;
import moscow.rockstar.settings.BooleanSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BeaconBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DropperBlock;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.TrappedChestBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.TripwireBlock;
import net.minecraft.block.CobwebBlock;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.CartographyTableBlock;
import net.minecraft.block.GrindstoneBlock;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.SmithingTableBlock;
import net.minecraft.block.StonecutterBlock;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.registry.Registries;
import ua.mintantileak.spk.Compile;

/*
 * Duplicate member names - consider using --renamedupmembers true
 */
@ModuleInfo(name="No Interact", category=ModuleCategory.PLAYER)
public class NoInteract
extends Module {
    public BlockItemSetting blocks;
    private BooleanSetting onlyaura;
    private BooleanSetting onlyutilityitems;
    private BooleanSetting armorStend;

    public NoInteract() {
        this.initializeInteractionRules();
    }

    @Compile(obfuscation=4)
    private void initializeInteractionRules() {
        this.blocks = new BlockItemSetting(this, "modules.settings.no_interact.blocks").includeBlocks(Blocks.CRAFTING_TABLE, Blocks.ENCHANTING_TABLE, Blocks.RED_BED, Blocks.CHEST, Blocks.ENDER_CHEST, Blocks.TRAPPED_CHEST, Blocks.FURNACE, Blocks.BARREL, Blocks.SHULKER_BOX, Blocks.DROPPER, Blocks.DISPENSER, Blocks.HOPPER, Blocks.ANVIL, Blocks.CAULDRON, Blocks.OAK_SIGN, Blocks.BELL, Blocks.COMPOSTER, Blocks.BREWING_STAND, Blocks.JUKEBOX, Blocks.COMMAND_BLOCK, Blocks.BEACON, Blocks.RESPAWN_ANCHOR, Blocks.GRINDSTONE, Blocks.LECTERN, Blocks.CARTOGRAPHY_TABLE, Blocks.LOOM, Blocks.COBWEB, Blocks.TRIPWIRE, Blocks.SMITHING_TABLE, Blocks.STONECUTTER, Blocks.PLAYER_HEAD, Blocks.OAK_TRAPDOOR).includeItem(Items.ARMOR_STAND).includeItem(Items.MINECART).includeItem(Items.CHEST_MINECART).includeItem(Items.FURNACE_MINECART).includeItem(Items.TNT_MINECART).includeItem(Items.HOPPER_MINECART).includeItem(Items.COMMAND_BLOCK_MINECART);
        this.onlyaura = new BooleanSetting(this, "modules.settings.no_interact.onlyAura");
        this.onlyutilityitems = new BooleanSetting(this, "modules.settings.no_interact.onlyUtilityItems");
        this.armorStend = new InteractionListener(this, this, "\u0410\u0440\u043c\u043e\u0440 \u0441\u0442\u0435\u043d\u0434", () -> true);
    }

    public boolean isBlockInteractionAllowed(Block class_22482, ItemStack class_17992) {
        if (!this.isHeldItemAllowed(class_17992)) {
            return false;
        }
        if (class_22482 instanceof CraftingTableBlock && this.blocks.isBlockSelected(Blocks.CRAFTING_TABLE)) {
            return true;
        }
        if (class_22482 instanceof EnchantingTableBlock && this.blocks.isBlockSelected(Blocks.ENCHANTING_TABLE)) {
            return true;
        }
        if (class_22482 instanceof BedBlock && this.blocks.isBlockSelected(Blocks.RED_BED)) {
            return true;
        }
        if (class_22482 instanceof TrapdoorBlock && this.blocks.isBlockSelected(Blocks.OAK_TRAPDOOR)) {
            return true;
        }
        if (class_22482 instanceof ChestBlock && this.blocks.isBlockSelected(Blocks.CHEST)) {
            return true;
        }
        if (class_22482 instanceof EnderChestBlock && this.blocks.isBlockSelected(Blocks.ENDER_CHEST)) {
            return true;
        }
        if (class_22482 instanceof TrappedChestBlock && this.blocks.isBlockSelected(Blocks.TRAPPED_CHEST)) {
            return true;
        }
        if (class_22482 instanceof AbstractFurnaceBlock && this.blocks.isBlockSelected(Blocks.FURNACE)) {
            return true;
        }
        if (class_22482 instanceof BarrelBlock && this.blocks.isBlockSelected(Blocks.BARREL)) {
            return true;
        }
        if (class_22482 instanceof ShulkerBoxBlock && this.blocks.isBlockSelected(Blocks.SHULKER_BOX)) {
            return true;
        }
        if (class_22482 instanceof DropperBlock && this.blocks.isBlockSelected(Blocks.DROPPER)) {
            return true;
        }
        if (class_22482 instanceof DispenserBlock && this.blocks.isBlockSelected(Blocks.DISPENSER)) {
            return true;
        }
        if (class_22482 instanceof HopperBlock && this.blocks.isBlockSelected(Blocks.HOPPER)) {
            return true;
        }
        if (class_22482 instanceof AnvilBlock && this.blocks.isBlockSelected(Blocks.ANVIL)) {
            return true;
        }
        if (class_22482 instanceof AbstractCauldronBlock && this.blocks.isBlockSelected(Blocks.CAULDRON)) {
            return true;
        }
        if (class_22482 instanceof AbstractSignBlock && this.blocks.isBlockSelected(Blocks.OAK_SIGN)) {
            return true;
        }
        if (class_22482 instanceof BellBlock && this.blocks.isBlockSelected(Blocks.BELL)) {
            return true;
        }
        if (class_22482 instanceof ComposterBlock && this.blocks.isBlockSelected(Blocks.COMPOSTER)) {
            return true;
        }
        if (class_22482 instanceof BrewingStandBlock && this.blocks.isBlockSelected(Blocks.BREWING_STAND)) {
            return true;
        }
        if (class_22482 instanceof JukeboxBlock && this.blocks.isBlockSelected(Blocks.JUKEBOX)) {
            return true;
        }
        if (class_22482 instanceof CommandBlock && this.blocks.isBlockSelected(Blocks.COMMAND_BLOCK)) {
            return true;
        }
        if (class_22482 instanceof BeaconBlock && this.blocks.isBlockSelected(Blocks.BEACON)) {
            return true;
        }
        if (class_22482 instanceof RespawnAnchorBlock && this.blocks.isBlockSelected(Blocks.RESPAWN_ANCHOR)) {
            return true;
        }
        if (class_22482 instanceof GrindstoneBlock && this.blocks.isBlockSelected(Blocks.GRINDSTONE)) {
            return true;
        }
        if (class_22482 instanceof LecternBlock && this.blocks.isBlockSelected(Blocks.LECTERN)) {
            return true;
        }
        if (class_22482 instanceof CobwebBlock && this.blocks.isBlockSelected(Blocks.COBWEB)) {
            return true;
        }
        if (class_22482 instanceof TripwireBlock && this.blocks.isBlockSelected(Blocks.TRIPWIRE)) {
            return true;
        }
        if (class_22482 instanceof CartographyTableBlock && this.blocks.isBlockSelected(Blocks.CARTOGRAPHY_TABLE)) {
            return true;
        }
        if (class_22482 instanceof LoomBlock && this.blocks.isBlockSelected(Blocks.LOOM)) {
            return true;
        }
        if (class_22482 instanceof SmithingTableBlock && this.blocks.isBlockSelected(Blocks.SMITHING_TABLE)) {
            return true;
        }
        return class_22482 instanceof StonecutterBlock && this.blocks.isBlockSelected(Blocks.STONECUTTER);
    }

    public boolean isEntityInteractionAllowed(Entity class_12972, ItemStack class_17992) {
        if (!this.isHeldItemAllowed(class_17992)) {
            return false;
        }
        if (class_12972 instanceof ArmorStandEntity && this.blocks.isRegistryIdSelected(Registries.ITEM.getId(Items.ARMOR_STAND))) {
            return true;
        }
        if (class_12972 instanceof MinecartEntity && this.blocks.isRegistryIdSelected(Registries.ITEM.getId(Items.MINECART))) {
            return true;
        }
        if (class_12972 instanceof ChestMinecartEntity && this.blocks.isRegistryIdSelected(Registries.ITEM.getId(Items.CHEST_MINECART))) {
            return true;
        }
        if (class_12972 instanceof FurnaceMinecartEntity && this.blocks.isRegistryIdSelected(Registries.ITEM.getId(Items.FURNACE_MINECART))) {
            return true;
        }
        if (class_12972 instanceof TntMinecartEntity && this.blocks.isRegistryIdSelected(Registries.ITEM.getId(Items.TNT_MINECART))) {
            return true;
        }
        if (class_12972 instanceof HopperMinecartEntity && this.blocks.isRegistryIdSelected(Registries.ITEM.getId(Items.HOPPER_MINECART))) {
            return true;
        }
        return class_12972 instanceof CommandBlockMinecartEntity && this.blocks.isRegistryIdSelected(Registries.ITEM.getId(Items.COMMAND_BLOCK_MINECART));
    }

    public boolean isItemValid(ItemStack class_17992) {
        if (!this.isHeldItemAllowed(class_17992)) {
            return false;
        }
        Item class_17922 = class_17992.getItem();
        if (class_17922 instanceof BlockItem) {
            BlockItem class_17472 = (BlockItem)class_17922;
            if (class_17472.getBlock() instanceof SkullBlock && this.blocks.isBlockSelected(Blocks.PLAYER_HEAD)) {
                return true;
            }
            if (class_17472.getBlock() instanceof CobwebBlock && this.blocks.isBlockSelected(Blocks.COBWEB)) {
                return true;
            }
            if (class_17472.getBlock() instanceof TripwireBlock && this.blocks.isBlockSelected(Blocks.TRIPWIRE)) {
                return true;
            }
        }
        return class_17992.getItem() == Items.STRING && this.blocks.isBlockSelected(Blocks.TRIPWIRE);
    }

    private boolean isHeldItemAllowed(ItemStack class_17992) {
        if (this.onlyaura.isEnabled() && !RockstarClient.create().getModuleRegistry().getModule(Aura.class).isEnabled()) {
            return false;
        }
        return !this.onlyutilityitems.isEnabled() || this.isContainerItemAllowed(class_17992);
    }

    private boolean isContainerItemAllowed(ItemStack class_17992) {
        if (class_17992 == null || class_17992.isEmpty()) {
            return false;
        }
        return class_17992.isOf(Items.ENDER_PEARL) || class_17992.isOf(Items.CHORUS_FRUIT) || class_17992.isOf(Items.FIREWORK_ROCKET) || class_17992.isOf(Items.WIND_CHARGE) || class_17992.isOf(Items.EXPERIENCE_BOTTLE) || class_17992.isOf(Items.POTION) || class_17992.isOf(Items.SPLASH_POTION) || class_17992.isOf(Items.LINGERING_POTION) || class_17992.isOf(Items.SNOWBALL) || class_17992.isOf(Items.EGG);
    }

    @Generated
    public BlockItemSetting getBlockSettings() {
        return this.blocks;
    }

    @Generated
    public BooleanSetting getOnlyAuraSetting() {
        return this.onlyaura;
    }
}
