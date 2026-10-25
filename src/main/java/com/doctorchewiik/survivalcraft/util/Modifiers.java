package com.doctorchewiik.survivalcraft.util;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class Modifiers {
    private Modifiers() {
    }

    // Ajoute ou retire un modificateur de vitesse temporaire selon la condition
    public static void setSpeedModifier(Player player, AttributeModifier modifier, boolean active) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        if (active && !speed.hasModifier(modifier.id())) {
            speed.addTransientModifier(modifier);
        } else if (!active && speed.hasModifier(modifier.id())) {
            speed.removeModifier(modifier.id());
        }
    }
}
