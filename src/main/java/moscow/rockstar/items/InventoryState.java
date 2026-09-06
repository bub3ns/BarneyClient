/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.EquipmentSlot
 *  net.minecraft.ItemStack
 *  net.minecraft.Vec3d
 *  net.minecraft.MathHelper
 */
package moscow.rockstar.items;

import java.util.List;
import lombok.Generated;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;

public class InventoryState {
    private double positionX;
    private double positionY;
    private double positionZ;
    private float yaw;
    private float pitch;
    private float health = 20.0f;
    private float maxHealth = 20.0f;
    private int foodLevel;
    private int selectedHotbarSlot;
    private boolean primaryActionActive = false;
    private boolean secondaryActionActive = false;
    private boolean onGround = true;
    private int playerEntityId;
    private ItemStack[] inventoryItems = new ItemStack[46];
    private ItemStack[] armorItems = new ItemStack[4];
    private ItemStack offhandStack = ItemStack.EMPTY;
    private ItemStack cursorStack = ItemStack.EMPTY;
    private ItemStack[] containerItems = new ItemStack[128];
    private int containerId = -1;
    private int containerRevision;
    private String containerTitle = "";
    private float experienceProgress;
    private int totalExperience;
    private int experienceLevel;
    private boolean invulnerable;
    private boolean flying;
    private boolean allowFlying;
    private boolean creativeMode;
    private float flySpeed = 0.05f;
    private float walkSpeed = 0.1f;
    private int entityStateId = -1;
    private long positionUpdateTimeMillis = 0L;
    private double previousPositionX;
    private double previousPositionY;
    private double previousPositionZ;
    private float previousYaw;
    private float previousPitch;

    public InventoryState() {
        int n;
        for (n = 0; n < this.inventoryItems.length; ++n) {
            this.inventoryItems[n] = ItemStack.EMPTY;
        }
        for (n = 0; n < this.armorItems.length; ++n) {
            this.armorItems[n] = ItemStack.EMPTY;
        }
        this.resetContainer();
    }

    public Vec3d getPosition() {
        return new Vec3d(this.positionX, this.positionY, this.positionZ);
    }

    public void setPosition(double d, double d2, double d3) {
        this.positionX = d;
        this.positionY = d2;
        this.positionZ = d3;
        this.positionUpdateTimeMillis = System.currentTimeMillis();
    }

    public void setRotation(float f, float f2) {
        this.yaw = MathHelper.wrapDegrees((float)f);
        this.pitch = MathHelper.clamp((float)f2, (float)-90.0f, (float)90.0f);
    }

    public ItemStack getSelectedHotbarItem() {
        if (this.selectedHotbarSlot >= 0 && this.selectedHotbarSlot < 9 && this.selectedHotbarSlot < this.inventoryItems.length) {
            return this.inventoryItems[this.selectedHotbarSlot];
        }
        return ItemStack.EMPTY;
    }

    public void setInventoryItem(int n, ItemStack class_17992) {
        if (n >= 0 && n < this.inventoryItems.length) {
            this.inventoryItems[n] = class_17992 != null ? class_17992 : ItemStack.EMPTY;
        }
    }

    public void updateInventorySlot(int n, ItemStack class_17992) {
        if (n >= 36 && n <= 44) {
            this.setInventoryItem(n - 36, class_17992);
            return;
        }
        if (n >= 9 && n <= 35) {
            this.setInventoryItem(n, class_17992);
            return;
        }
        if (n >= 5 && n <= 8) {
            this.setArmorItem(8 - n, class_17992);
            return;
        }
        if (n == 45) {
            this.offhandStack = class_17992 == null ? ItemStack.EMPTY : class_17992;
        }
    }

    public void setPlayerInventory(List<ItemStack> list) {
        if (list == null) {
            return;
        }
        for (int i = 0; i < list.size(); ++i) {
            this.updateInventorySlot(i, list.get(i));
        }
    }

    public void setArmorItem(int n, ItemStack class_17992) {
        if (n >= 0 && n < this.armorItems.length) {
            this.armorItems[n] = class_17992 != null ? class_17992 : ItemStack.EMPTY;
        }
    }

    public void setEquipmentItem(EquipmentSlot class_13042, ItemStack class_17992) {
        if (class_13042 == null) {
            return;
        }
        ItemStack class_17993 = class_17992 == null ? ItemStack.EMPTY : class_17992;
        switch (class_13042) {
            case MAINHAND: {
                this.setInventoryItem(this.selectedHotbarSlot, class_17993);
                break;
            }
            case OFFHAND: {
                this.offhandStack = class_17993;
                break;
            }
            case FEET: {
                this.setArmorItem(0, class_17993);
                break;
            }
            case LEGS: {
                this.setArmorItem(1, class_17993);
                break;
            }
            case CHEST: {
                this.setArmorItem(2, class_17993);
                break;
            }
            case HEAD: {
                this.setArmorItem(3, class_17993);
                break;
            }
        }
    }

    public void updateExperience(float f, int n, int n2) {
        this.experienceProgress = MathHelper.clamp((float)f, (float)0.0f, (float)1.0f);
        this.experienceLevel = Math.max(0, n);
        this.totalExperience = Math.max(0, n2);
    }

    public void updatePlayerAbilities(boolean bl, boolean bl2, boolean bl3, boolean bl4, float f, float f2) {
        this.invulnerable = bl;
        this.flying = bl2;
        this.allowFlying = bl3;
        this.creativeMode = bl4;
        this.flySpeed = Math.max(0.0f, f);
        this.walkSpeed = Math.max(0.0f, f2);
    }

    public void openContainer(int n, String string) {
        this.containerId = n;
        this.containerRevision = 0;
        this.containerTitle = string == null ? "" : string;
        this.clearContainerItems();
    }

    public void closeContainerIfMatches(int n) {
        if (this.containerId == n || n < 0) {
            this.resetContainer();
        }
    }

    public void updateContainerContents(int n, int n2, List<ItemStack> list) {
        if (n == 0) {
            return;
        }
        if (this.containerId != n) {
            this.openContainer(n, this.containerTitle);
        }
        this.containerRevision = n2;
        this.clearContainerItems();
        if (list == null) {
            return;
        }
        this.ensureContainerCapacity(list.size());
        for (int i = 0; i < list.size(); ++i) {
            this.containerItems[i] = list.get(i) == null ? ItemStack.EMPTY : list.get(i);
        }
    }

    public void updateContainerSlot(int n, int n2, int n3, ItemStack class_17992) {
        if (n == 0) {
            this.updateInventorySlot(n3, class_17992);
            return;
        }
        if (n3 < 0) {
            return;
        }
        if (this.containerId != n) {
            this.openContainer(n, this.containerTitle);
        }
        this.containerRevision = n2;
        this.ensureContainerCapacity(n3 + 1);
        this.containerItems[n3] = class_17992 == null ? ItemStack.EMPTY : class_17992;
    }

    public ItemStack getContainerItem(int n) {
        if (n < 0 || n >= this.containerItems.length) {
            return ItemStack.EMPTY;
        }
        return this.containerItems[n] == null ? ItemStack.EMPTY : this.containerItems[n];
    }

    public boolean hasOpenContainer() {
        return this.containerId > 0;
    }

    public boolean isHealthDepleted() {
        return this.health <= 0.0f;
    }

    public boolean hasPositionChanged() {
        return this.positionX != this.previousPositionX || this.positionY != this.previousPositionY || this.positionZ != this.previousPositionZ;
    }

    public boolean hasRotationChanged() {
        return this.yaw != this.previousYaw || this.pitch != this.previousPitch;
    }

    public void savePositionSnapshot() {
        this.previousPositionX = this.positionX;
        this.previousPositionY = this.positionY;
        this.previousPositionZ = this.positionZ;
        this.previousYaw = this.yaw;
        this.previousPitch = this.pitch;
    }

    public void resetState() {
        int n;
        this.positionZ = 0.0;
        this.positionY = 0.0;
        this.positionX = 0.0;
        this.pitch = 0.0f;
        this.yaw = 0.0f;
        this.previousPositionZ = 0.0;
        this.previousPositionY = 0.0;
        this.previousPositionX = 0.0;
        this.previousPitch = 0.0f;
        this.previousYaw = 0.0f;
        this.health = 20.0f;
        this.maxHealth = 20.0f;
        this.foodLevel = 20;
        this.selectedHotbarSlot = 0;
        this.primaryActionActive = false;
        this.secondaryActionActive = false;
        this.onGround = true;
        this.playerEntityId = -1;
        this.experienceProgress = 0.0f;
        this.experienceLevel = 0;
        this.totalExperience = 0;
        this.invulnerable = false;
        this.flying = false;
        this.allowFlying = false;
        this.creativeMode = false;
        this.flySpeed = 0.05f;
        this.walkSpeed = 0.1f;
        this.entityStateId = -1;
        for (n = 0; n < this.inventoryItems.length; ++n) {
            this.inventoryItems[n] = ItemStack.EMPTY;
        }
        for (n = 0; n < this.armorItems.length; ++n) {
            this.armorItems[n] = ItemStack.EMPTY;
        }
        this.offhandStack = ItemStack.EMPTY;
        this.cursorStack = ItemStack.EMPTY;
        this.resetContainer();
    }

    private void resetContainer() {
        this.containerId = -1;
        this.containerRevision = 0;
        this.containerTitle = "";
        this.clearContainerItems();
    }

    private void clearContainerItems() {
        for (int i = 0; i < this.containerItems.length; ++i) {
            this.containerItems[i] = ItemStack.EMPTY;
        }
    }

    private void ensureContainerCapacity(int n) {
        if (n <= this.containerItems.length) {
            return;
        }
        ItemStack[] class_1799Array = new ItemStack[n];
        System.arraycopy(this.containerItems, 0, class_1799Array, 0, this.containerItems.length);
        for (int i = this.containerItems.length; i < class_1799Array.length; ++i) {
            class_1799Array[i] = ItemStack.EMPTY;
        }
        this.containerItems = class_1799Array;
    }

    @Generated
    public double getPositionX() {
        return this.positionX;
    }

    @Generated
    public double getPositionY() {
        return this.positionY;
    }

    @Generated
    public double getPositionZ() {
        return this.positionZ;
    }

    @Generated
    public float getYaw() {
        return this.yaw;
    }

    @Generated
    public float getPitch() {
        return this.pitch;
    }

    @Generated
    public float getHealth() {
        return this.health;
    }

    @Generated
    public float getMaxHealth() {
        return this.maxHealth;
    }

    @Generated
    public int getUpdateTick() {
        return this.foodLevel;
    }

    @Generated
    public int getSelectedHotbarSlot() {
        return this.selectedHotbarSlot;
    }

    @Generated
    public boolean isPrimaryActionActive() {
        return this.primaryActionActive;
    }

    @Generated
    public boolean isSecondaryActionActive() {
        return this.secondaryActionActive;
    }

    @Generated
    public boolean isOnGround() {
        return this.onGround;
    }

    @Generated
    public int getPlayerEntityId() {
        return this.playerEntityId;
    }

    @Generated
    public ItemStack[] getInventoryItems() {
        return this.inventoryItems;
    }

    @Generated
    public ItemStack[] getArmorItems() {
        return this.armorItems;
    }

    @Generated
    public ItemStack getOffhandStack() {
        return this.offhandStack;
    }

    @Generated
    public ItemStack getCursorStack() {
        return this.cursorStack;
    }

    @Generated
    public ItemStack[] getContainerItems() {
        return this.containerItems;
    }

    @Generated
    public int getContainerId() {
        return this.containerId;
    }

    @Generated
    public int getContainerRevision() {
        return this.containerRevision;
    }

    @Generated
    public String getContainerTitle() {
        return this.containerTitle;
    }

    @Generated
    public float getExperienceProgress() {
        return this.experienceProgress;
    }

    @Generated
    public int getTotalExperience() {
        return this.totalExperience;
    }

    @Generated
    public int getExperienceLevel() {
        return this.experienceLevel;
    }

    @Generated
    public boolean isInvulnerable() {
        return this.invulnerable;
    }

    @Generated
    public boolean isFlying() {
        return this.flying;
    }

    @Generated
    public boolean canFly() {
        return this.allowFlying;
    }

    @Generated
    public boolean isCreativeMode() {
        return this.creativeMode;
    }

    @Generated
    public float getFlySpeed() {
        return this.flySpeed;
    }

    @Generated
    public float getWalkSpeed() {
        return this.walkSpeed;
    }

    @Generated
    public int getEntityStateId() {
        return this.entityStateId;
    }

    @Generated
    public long getPositionUpdateTimeMillis() {
        return this.positionUpdateTimeMillis;
    }

    @Generated
    public double getPreviousPositionX() {
        return this.previousPositionX;
    }

    @Generated
    public double getPreviousPositionY() {
        return this.previousPositionY;
    }

    @Generated
    public double getPreviousPositionZ() {
        return this.previousPositionZ;
    }

    @Generated
    public float getPreviousYaw() {
        return this.previousYaw;
    }

    @Generated
    public float getPreviousPitch() {
        return this.previousPitch;
    }

    @Generated
    public void setPositionX(double d) {
        this.positionX = d;
    }

    @Generated
    public void setPositionY(double d) {
        this.positionY = d;
    }

    @Generated
    public void setPositionZ(double d) {
        this.positionZ = d;
    }

    @Generated
    public void setYaw(float f) {
        this.yaw = f;
    }

    @Generated
    public void setPitch(float f) {
        this.pitch = f;
    }

    @Generated
    public void setHealth(float f) {
        this.health = f;
    }

    @Generated
    public void setMaxHealth(float f) {
        this.maxHealth = f;
    }

    @Generated
    public void setFoodLevel(int n) {
        this.foodLevel = n;
    }

    @Generated
    public void setSelectedHotbarSlot(int n) {
        this.selectedHotbarSlot = n;
    }

    @Generated
    public void setPrimaryActionActive(boolean bl) {
        this.primaryActionActive = bl;
    }

    @Generated
    public void setSecondaryActionActive(boolean bl) {
        this.secondaryActionActive = bl;
    }

    @Generated
    public void setOnGround(boolean bl) {
        this.onGround = bl;
    }

    @Generated
    public void setPlayerEntityId(int n) {
        this.playerEntityId = n;
    }

    @Generated
    public void setInventoryItems(ItemStack[] class_1799Array) {
        this.inventoryItems = class_1799Array;
    }

    @Generated
    public void setArmorItems(ItemStack[] class_1799Array) {
        this.armorItems = class_1799Array;
    }

    @Generated
    public void setOffhandStack(ItemStack class_17992) {
        this.offhandStack = class_17992;
    }

    @Generated
    public void setCursorStack(ItemStack class_17992) {
        this.cursorStack = class_17992;
    }

    @Generated
    public void setContainerItems(ItemStack[] class_1799Array) {
        this.containerItems = class_1799Array;
    }

    @Generated
    public void setContainerId(int n) {
        this.containerId = n;
    }

    @Generated
    public void setContainerRevision(int n) {
        this.containerRevision = n;
    }

    @Generated
    public void setContainerTitle(String string) {
        this.containerTitle = string;
    }

    @Generated
    public void setExperienceProgress(float f) {
        this.experienceProgress = f;
    }

    @Generated
    public void writeAmountPrevious(int n) {
        this.totalExperience = n;
    }

    @Generated
    public void writeAmountNext(int n) {
        this.experienceLevel = n;
    }

    @Generated
    public void setInvulnerable(boolean bl) {
        this.invulnerable = bl;
    }

    @Generated
    public void setFlying(boolean bl) {
        this.flying = bl;
    }

    @Generated
    public void setAllowFlying(boolean bl) {
        this.allowFlying = bl;
    }

    @Generated
    public void setCreativeMode(boolean bl) {
        this.creativeMode = bl;
    }

    @Generated
    public void setFlySpeed(float f) {
        this.flySpeed = f;
    }

    @Generated
    public void setWalkSpeed(float f) {
        this.walkSpeed = f;
    }

    @Generated
    public void setWorldTime(int n) {
        this.entityStateId = n;
    }

    @Generated
    public void setPositionUpdateTimeMillis(long l) {
        this.positionUpdateTimeMillis = l;
    }

    @Generated
    public void setPreviousPositionX(double d) {
        this.previousPositionX = d;
    }

    @Generated
    public void setPreviousPositionY(double d) {
        this.previousPositionY = d;
    }

    @Generated
    public void setPreviousPositionZ(double d) {
        this.previousPositionZ = d;
    }

    @Generated
    public void setPreviousYaw(float f) {
        this.previousYaw = f;
    }

    @Generated
    public void setPreviousPitch(float f) {
        this.previousPitch = f;
    }
}
