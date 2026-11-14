package com.doctorchewiik.survivalcraft.mycology;

import java.util.Optional;

import com.doctorchewiik.survivalcraft.registry.ModItems;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;

/**
 * Espèces cultivables. La plage idéale est sur l'échelle de température du mod
 * (10 = 20 °C, environ 2 °C par point). "slowness" allonge les temps de colonisation et de pousse.
 */
public enum MushroomSpecies implements StringRepresentable {
    CUBENSIS("cubensis", 11f, 14f, 1.0f),
    COPELANDIA("copelandia", 12f, 15f, 1.2f),
    OYSTER("oyster", 6f, 10f, 0.7f),
    SHIITAKE("shiitake", 5f, 9f, 1.4f);

    private final String name;
    private final float idealMin;
    private final float idealMax;
    private final float slowness;

    MushroomSpecies(String name, float idealMin, float idealMax, float slowness) {
        this.name = name;
        this.idealMin = idealMin;
        this.idealMax = idealMax;
        this.slowness = slowness;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public float idealMin() {
        return idealMin;
    }

    public float idealMax() {
        return idealMax;
    }

    public float slowness() {
        return slowness;
    }

    public Component displayName() {
        return Component.translatable("species.survivalcraft." + name);
    }

    public Item mushroom() {
        return switch (this) {
            case CUBENSIS -> ModItems.CUBENSIS.get();
            case COPELANDIA -> ModItems.COPELANDIA.get();
            case OYSTER -> ModItems.OYSTER.get();
            case SHIITAKE -> ModItems.SHIITAKE.get();
        };
    }

    // 1 dans la plage idéale, ralenti à 2 points près, presque à l'arrêt au-delà
    public float temperatureFactor(float temperature) {
        if (temperature >= idealMin && temperature <= idealMax) {
            return 1f;
        }
        float distance = temperature < idealMin ? idealMin - temperature : temperature - idealMax;
        return distance <= 2f ? 0.4f : 0.05f;
    }

    public static Optional<MushroomSpecies> byName(String name) {
        for (MushroomSpecies species : values()) {
            if (species.name.equals(name)) {
                return Optional.of(species);
            }
        }
        return Optional.empty();
    }
}
