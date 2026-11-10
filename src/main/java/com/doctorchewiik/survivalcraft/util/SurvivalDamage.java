package com.doctorchewiik.survivalcraft.util;

import com.doctorchewiik.survivalcraft.SurvivalCraft;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

// Types de dégâts du mod et règles communes, calquées sur la famine vanilla
public final class SurvivalDamage {
    public static final ResourceKey<DamageType> DEHYDRATION = key("dehydration");
    public static final ResourceKey<DamageType> HYPOTHERMIA = key("hypothermia");
    public static final ResourceKey<DamageType> HEATSTROKE = key("heatstroke");

    private SurvivalDamage() {
    }

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, SurvivalCraft.id(name));
    }

    public static DamageSource source(ServerPlayer player, ResourceKey<DamageType> type) {
        return new DamageSource(player.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(type));
    }

    // Facile : s'arrête à 5 coeurs. Normal : s'arrête à un demi-coeur. Difficile : peut tuer.
    public static boolean canHurt(ServerPlayer player) {
        Difficulty difficulty = player.level().getDifficulty();
        return player.getHealth() > 10f
                || difficulty == Difficulty.HARD
                || (player.getHealth() > 1f && difficulty == Difficulty.NORMAL);
    }

    public static void hurt(ServerPlayer player, ResourceKey<DamageType> type) {
        if (canHurt(player)) {
            player.hurt(source(player, type), 1f);
        }
    }
}
