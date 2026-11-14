package com.doctorchewiik.survivalcraft.temperature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Mth;

// Température du corps : 0 = gelé, 10 = normal, 20 = brûlant.
public class TemperatureData {
    public static final float MIN = 0f;
    public static final float MAX = 20f;
    public static final float NORMAL = 10f;

    // Seuils d'effets
    public static final float HYPOTHERMIA = 3f;
    public static final float COLD = 6f;
    public static final float HOT = 14f;
    public static final float HEATSTROKE = 17f;

    public static final Codec<TemperatureData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("body").forGetter(TemperatureData::getBody)
    ).apply(instance, TemperatureData::new));

    private float body;

    // Non sauvegardés
    float target = NORMAL;
    int damageTimer;

    public TemperatureData() {
        this(NORMAL);
    }

    public TemperatureData(float body) {
        this.body = Mth.clamp(body, MIN, MAX);
    }

    public float getBody() {
        return body;
    }

    public void setBody(float body) {
        this.body = Mth.clamp(body, MIN, MAX);
    }

    public float getTarget() {
        return target;
    }
}
