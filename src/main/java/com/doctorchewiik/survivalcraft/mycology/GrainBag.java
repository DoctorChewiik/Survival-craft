package com.doctorchewiik.survivalcraft.mycology;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * État d'un sac de grains. Immuable, comme tout composant de données.
 * - sterilizedAt / inoculatedAt : heure du monde (ticks) de chaque étape
 * - percent : avancement de la colonisation, mis à jour pendant que le sac est dans l'inventaire
 * - doomed : contamination décidée à l'inoculation, elle se déclare à mi-chemin
 */
public record GrainBag(Stage stage, String species, long sterilizedAt, long inoculatedAt, int percent,
        boolean doomed) {
    public static final GrainBag RAW = new GrainBag(Stage.RAW, "", 0L, 0L, 0, false);

    public static final Codec<GrainBag> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Stage.CODEC.fieldOf("stage").forGetter(GrainBag::stage),
            Codec.STRING.optionalFieldOf("species", "").forGetter(GrainBag::species),
            Codec.LONG.optionalFieldOf("sterilized_at", 0L).forGetter(GrainBag::sterilizedAt),
            Codec.LONG.optionalFieldOf("inoculated_at", 0L).forGetter(GrainBag::inoculatedAt),
            Codec.INT.optionalFieldOf("percent", 0).forGetter(GrainBag::percent),
            Codec.BOOL.optionalFieldOf("doomed", false).forGetter(GrainBag::doomed)
    ).apply(instance, GrainBag::new));

    public static final StreamCodec<ByteBuf, GrainBag> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public Optional<MushroomSpecies> speciesValue() {
        return MushroomSpecies.byName(species);
    }

    public GrainBag withStage(Stage newStage, int newPercent) {
        return new GrainBag(newStage, species, sterilizedAt, inoculatedAt, newPercent, doomed);
    }

    public enum Stage implements StringRepresentable {
        RAW("raw"),
        STERILIZED("sterilized"),
        INOCULATED("inoculated"),
        COLONIZED("colonized"),
        CONTAMINATED("contaminated");

        public static final Codec<Stage> CODEC = StringRepresentable.fromEnum(Stage::values);

        private final String name;

        Stage(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
