/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.StatusEffect
 *  net.minecraft.StatusEffectInstance
 *  net.minecraft.StatusEffects
 *  net.minecraft.ItemStack
 *  net.minecraft.PotionItem
 *  net.minecraft.PotionContentsComponent
 *  net.minecraft.RegistryEntry
 *  net.minecraft.DataComponentTypes
 */
package moscow.rockstar.items.recipes;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.component.DataComponentTypes;

public final class RecipeItemResolver {
    public static boolean containsEffect(ItemStack class_17992, RegistryEntry<StatusEffect> class_68802) {
        if (class_17992 == null || class_17992.isEmpty()) {
            return false;
        }
        if (!(class_17992.getItem() instanceof PotionItem)) {
            return false;
        }
        PotionContentsComponent class_18442 = (PotionContentsComponent)class_17992.get(DataComponentTypes.POTION_CONTENTS);
        if (class_18442 == null) {
            return false;
        }
        for (StatusEffectInstance class_12932 : class_18442.getEffects()) {
            if (class_12932.getEffectType() != class_68802) continue;
            return true;
        }
        return false;
    }

    public static List<StatusEffectInstance> getEffects(ItemStack class_17992) {
        ArrayList<StatusEffectInstance> arrayList = new ArrayList<StatusEffectInstance>();
        if (class_17992 == null || class_17992.isEmpty()) {
            return arrayList;
        }
        if (!(class_17992.getItem() instanceof PotionItem)) {
            return arrayList;
        }
        PotionContentsComponent class_18442 = (PotionContentsComponent)class_17992.get(DataComponentTypes.POTION_CONTENTS);
        if (class_18442 == null) {
            return arrayList;
        }
        class_18442.getEffects().forEach(arrayList::add);
        return arrayList;
    }

    public static ItemStack withEffectAmplifier(ItemStack class_17992, int n) {
        if (!(class_17992.getItem() instanceof PotionItem)) {
            return class_17992;
        }
        PotionContentsComponent class_18442 = (PotionContentsComponent)class_17992.get(DataComponentTypes.POTION_CONTENTS);
        if (class_18442 == null) {
            return class_17992;
        }
        ArrayList<StatusEffectInstance> arrayList = new ArrayList<StatusEffectInstance>();
        for (StatusEffectInstance class_12932 : class_18442.getEffects()) {
            arrayList.add(new StatusEffectInstance(class_12932.getEffectType(), class_12932.getDuration(), n, class_12932.isAmbient(), class_12932.shouldShowParticles(), class_12932.shouldShowIcon()));
        }
        ItemStack class_17993 = class_17992.copy();
        class_17993.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(class_18442.potion(), class_18442.customColor(), arrayList, class_18442.customName()));
        return class_17993;
    }

    public static int getFirstEffectAmplifier(ItemStack class_17992) {
        if (!(class_17992.getItem() instanceof PotionItem)) {
            return 0;
        }
        PotionContentsComponent class_18442 = (PotionContentsComponent)class_17992.get(DataComponentTypes.POTION_CONTENTS);
        if (class_18442 == null) {
            return 0;
        }
        Iterator iterator = class_18442.getEffects().iterator();
        if (iterator.hasNext()) {
            StatusEffectInstance class_12932 = (StatusEffectInstance)iterator.next();
            return class_12932.getAmplifier();
        }
        return 0;
    }

    public static boolean containsAllEffects(ItemStack class_17992, ItemStack class_17993) {
        PotionContentsComponent class_18442 = (PotionContentsComponent)class_17992.get(DataComponentTypes.POTION_CONTENTS);
        PotionContentsComponent class_18443 = (PotionContentsComponent)class_17993.get(DataComponentTypes.POTION_CONTENTS);
        if (class_18442 == null || class_18443 == null) {
            return false;
        }
        Map<PotionEffectKey, Integer> map = RecipeItemResolver.countEffects(class_18442);
        Map<PotionEffectKey, Integer> map2 = RecipeItemResolver.countEffects(class_18443);
        for (Map.Entry<PotionEffectKey, Integer> entry : map.entrySet()) {
            if (map2.containsKey(entry.getKey())) continue;
            return false;
        }
        return true;
    }

    private static Map<PotionEffectKey, Integer> countEffects(PotionContentsComponent class_18442) {
        HashMap<PotionEffectKey, Integer> hashMap = new HashMap<PotionEffectKey, Integer>();
        for (StatusEffectInstance class_12932 : class_18442.getEffects()) {
            hashMap.merge(PotionEffectKey.fromStatusEffect(class_12932), 1, Integer::sum);
        }
        return hashMap;
    }

    public static boolean hasNonStandardEffect(ItemStack class_17992) {
        if (!(class_17992.getItem() instanceof PotionItem)) {
            return false;
        }
        PotionContentsComponent class_18442 = (PotionContentsComponent)class_17992.get(DataComponentTypes.POTION_CONTENTS);
        if (class_18442 == null) {
            return false;
        }
        for (StatusEffectInstance class_12932 : class_18442.getEffects()) {
            RegistryEntry<StatusEffect> class_68802 = class_12932.getEffectType();
            if (class_68802.equals((Object)StatusEffects.INVISIBILITY) || class_68802.equals((Object)StatusEffects.NIGHT_VISION) || class_68802.equals((Object)StatusEffects.WATER_BREATHING) || class_68802.equals((Object)StatusEffects.FIRE_RESISTANCE) || class_68802.equals((Object)StatusEffects.SLOW_FALLING) || class_68802.equals((Object)StatusEffects.LUCK)) continue;
            return true;
        }
        return false;
    }

    @Generated
    private RecipeItemResolver() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    static final class PotionEffectKey {
        private final RegistryEntry<StatusEffect> effectType;
        private final int amplifier;

        private PotionEffectKey(RegistryEntry<StatusEffect> class_68802, int n) {
            this.effectType = class_68802;
            this.amplifier = n;
        }

        static PotionEffectKey fromStatusEffect(StatusEffectInstance class_12932) {
            return new PotionEffectKey((RegistryEntry<StatusEffect>)class_12932.getEffectType(), class_12932.getAmplifier());
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "effectType", "amplifier");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "effectType", "amplifier");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "effectType", "amplifier");
        }

        public RegistryEntry<StatusEffect> getEffectType() {
            return this.effectType;
        }

        public int getAmplifier() {
            return this.amplifier;
        }
    }
}
