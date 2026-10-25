package com.doctorchewiik.survivalcraft.fatigue;

import com.doctorchewiik.survivalcraft.registry.ModAttachments;

import net.minecraft.world.entity.player.Player;

public final class Fatigue {
    private Fatigue() {
    }

    public static FatigueData get(Player player) {
        return player.getData(ModAttachments.FATIGUE);
    }

    // Vitesse de minage selon la fatigue
    public static float breakSpeedMultiplier(float fatigue) {
        if (fatigue >= FatigueData.VERY_TIRED) {
            return 0.6f;
        }
        if (fatigue >= FatigueData.TIRED) {
            return 0.8f;
        }
        return 1f;
    }
}
