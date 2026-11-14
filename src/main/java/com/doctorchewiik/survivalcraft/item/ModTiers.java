package com.doctorchewiik.survivalcraft.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

// Matériaux d'outils du mod
public enum ModTiers implements Tier {
    // Silex : un peu moins solide que le bois, mais tranchant
    FLINT(48, 2.5f, 0.5f, 5);

    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantability;

    ModTiers(int uses, float speed, float damage, int enchantability) {
        this.uses = uses;
        this.speed = speed;
        this.damage = damage;
        this.enchantability = enchantability;
    }

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return damage;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_WOODEN_TOOL;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantability;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(Items.FLINT);
    }
}
