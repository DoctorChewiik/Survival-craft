package com.doctorchewiik.survivalcraft.thirst;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Mth;

// Soif d'un joueur, sur le même modèle que la faim vanilla : 0 à 20 points (10 gouttes).
public class ThirstData {
    public static final int MAX = 20;

    public static final Codec<ThirstData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("thirst").forGetter(ThirstData::getThirst),
            Codec.FLOAT.fieldOf("exhaustion").forGetter(ThirstData::getExhaustion)
    ).apply(instance, ThirstData::new));

    private int thirst;
    private float exhaustion;

    // Non sauvegardés
    int damageTimer;
    int lastSynced = -1;

    public ThirstData() {
        this(MAX, 0f);
    }

    public ThirstData(int thirst, float exhaustion) {
        this.thirst = Mth.clamp(thirst, 0, MAX);
        this.exhaustion = exhaustion;
    }

    public int getThirst() {
        return thirst;
    }

    public float getExhaustion() {
        return exhaustion;
    }

    public void setThirst(int thirst) {
        this.thirst = Mth.clamp(thirst, 0, MAX);
    }

    public void drink(int amount) {
        setThirst(thirst + amount);
    }

    public boolean isFull() {
        return thirst >= MAX;
    }

    // Ajoute de la fatigue de soif ; chaque 1.0 accumulé retire un point.
    public void addExhaustion(float amount) {
        exhaustion += amount;
        while (exhaustion >= 1f) {
            exhaustion -= 1f;
            setThirst(thirst - 1);
        }
    }

    // Force un nouvel envoi au client (changement de dimension, etc.)
    public void markDirty() {
        lastSynced = -1;
    }
}
