package com.doctorchewiik.survivalcraft.fatigue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Mth;

// Fatigue : 0 = bien reposé, 100 = épuisé.
public class FatigueData {
    public static final float MAX = 100f;

    // Seuils d'effets
    public static final float TIRED = 60f;
    public static final float VERY_TIRED = 80f;
    public static final float EXHAUSTED = 95f;

    public static final Codec<FatigueData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("fatigue").forGetter(FatigueData::getFatigue)
    ).apply(instance, FatigueData::new));

    private float fatigue;

    public FatigueData() {
        this(0f);
    }

    public FatigueData(float fatigue) {
        this.fatigue = Mth.clamp(fatigue, 0f, MAX);
    }

    public float getFatigue() {
        return fatigue;
    }

    public void setFatigue(float fatigue) {
        this.fatigue = Mth.clamp(fatigue, 0f, MAX);
    }

    public void add(float amount) {
        setFatigue(fatigue + amount);
    }
}
