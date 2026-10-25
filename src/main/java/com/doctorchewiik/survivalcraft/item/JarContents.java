package com.doctorchewiik.survivalcraft.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * Contenu d'un bocal hermétique : un seul type d'aliment, 8 au maximum.
 * Un bocal stérilisé (sealed) est fermé ; le rouvrir casse le joint.
 * Immuable, comme tout composant de données.
 */
public record JarContents(ItemStack food, boolean sealed) {
    public static final int CAPACITY = 8;
    public static final JarContents EMPTY = new JarContents(ItemStack.EMPTY, false);

    public static final Codec<JarContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.fieldOf("food").forGetter(JarContents::food),
            Codec.BOOL.fieldOf("sealed").forGetter(JarContents::sealed)
    ).apply(instance, JarContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, JarContents> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, JarContents::food,
            ByteBufCodecs.BOOL, JarContents::sealed,
            JarContents::new);

    public JarContents {
        food = food.copy();
    }

    public static boolean canHold(ItemStack stack) {
        return !stack.isEmpty() && stack.has(DataComponents.FOOD);
    }

    public boolean isEmpty() {
        return food.isEmpty();
    }

    public int count() {
        return food.getCount();
    }

    // Combien d'items de ce stack peuvent encore entrer
    public int spaceFor(ItemStack stack) {
        if (!canHold(stack) || sealed) {
            return 0;
        }
        if (isEmpty()) {
            return CAPACITY;
        }
        return ItemStack.isSameItemSameComponents(food, stack) ? CAPACITY - food.getCount() : 0;
    }

    public JarContents withAdded(ItemStack stack, int amount) {
        ItemStack result = isEmpty() ? stack.copyWithCount(amount) : food.copyWithCount(food.getCount() + amount);
        return new JarContents(result, false);
    }

    // Retire des items ; ouvrir le bocal casse le joint
    public JarContents withRemoved(int amount) {
        int left = food.getCount() - amount;
        return left <= 0 ? EMPTY : new JarContents(food.copyWithCount(left), false);
    }

    public JarContents seal() {
        return isEmpty() ? this : new JarContents(food, true);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof JarContents contents
                && sealed == contents.sealed
                && ItemStack.matches(food, contents.food);
    }

    @Override
    public int hashCode() {
        return 31 * ItemStack.hashItemAndComponents(food) + food.getCount() * 2 + (sealed ? 1 : 0);
    }
}
