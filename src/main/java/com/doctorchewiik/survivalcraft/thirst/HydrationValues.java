package com.doctorchewiik.survivalcraft.thirst;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

// Points de soif rendus par les items vanilla. Les gourdes du mod gèrent leur propre valeur.
public final class HydrationValues {
    public record Hydration(int amount, boolean dirty) {
    }

    private static final Map<Item, Hydration> VALUES = Map.of(
            Items.MILK_BUCKET, new Hydration(4, false),
            Items.MELON_SLICE, new Hydration(2, false),
            Items.APPLE, new Hydration(1, false),
            Items.SWEET_BERRIES, new Hydration(1, false),
            Items.GLOW_BERRIES, new Hydration(1, false),
            Items.HONEY_BOTTLE, new Hydration(1, false),
            Items.MUSHROOM_STEW, new Hydration(3, false),
            Items.BEETROOT_SOUP, new Hydration(3, false),
            Items.RABBIT_STEW, new Hydration(3, false),
            Items.SUSPICIOUS_STEW, new Hydration(3, false));

    // Une fiole d'eau vanilla est remplie à la rivière : l'eau est sale
    private static final Hydration WATER_BOTTLE = new Hydration(3, true);

    private HydrationValues() {
    }

    @Nullable
    public static Hydration get(ItemStack stack) {
        if (stack.is(Items.POTION)
                && stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)) {
            return WATER_BOTTLE;
        }
        return VALUES.get(stack.getItem());
    }
}
