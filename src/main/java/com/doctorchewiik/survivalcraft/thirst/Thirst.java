package com.doctorchewiik.survivalcraft.thirst;

import com.doctorchewiik.survivalcraft.Config;
import com.doctorchewiik.survivalcraft.registry.ModAttachments;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

// Point d'entrée pour toute action qui touche à la soif.
public final class Thirst {
    private Thirst() {
    }

    public static ThirstData get(Player player) {
        return player.getData(ModAttachments.THIRST);
    }

    // Fait boire le joueur. L'eau sale peut rendre malade (faim + nausée).
    public static void drink(ServerPlayer player, int amount, boolean dirty) {
        get(player).drink(amount);
        if (dirty && player.getRandom().nextDouble() < Config.DIRTY_WATER_SICKNESS_CHANCE.get()) {
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 0));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
        }
    }
}
