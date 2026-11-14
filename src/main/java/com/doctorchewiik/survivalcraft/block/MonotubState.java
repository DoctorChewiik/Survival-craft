package com.doctorchewiik.survivalcraft.block;

import net.minecraft.util.StringRepresentable;

// Ce qui se passe dans un monotub, visible sur le bloc
public enum MonotubState implements StringRepresentable {
    EMPTY("empty"),
    COLONIZING("colonizing"),
    FRUITING("fruiting"),
    CONTAMINATED("contaminated");

    private final String name;

    MonotubState(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
