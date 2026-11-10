package com.doctorchewiik.survivalcraft.block;

import net.minecraft.util.StringRepresentable;

// Type de gourde, pour choisir le modèle de la gourde posée
public enum CanteenKind implements StringRepresentable {
    GOURD("gourd"),
    LEATHER("leather"),
    METAL("metal");

    private final String name;

    CanteenKind(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
