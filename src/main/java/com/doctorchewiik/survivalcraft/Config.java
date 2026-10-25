package com.doctorchewiik.survivalcraft;

import net.neoforged.neoforge.common.ModConfigSpec;

// Tous les réglages d'équilibrage du mod. Chaque mécanique peut être désactivée.
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static {
        BUILDER.comment("Soif").push("thirst");
    }

    public static final ModConfigSpec.BooleanValue THIRST_ENABLED = BUILDER
            .comment("Active la mécanique de soif")
            .define("enabled", true);

    public static final ModConfigSpec.DoubleValue THIRST_BASE_DRAIN = BUILDER
            .comment("Fatigue de soif ajoutée à chaque tick. Un point de soif est perdu tous les 1.0.",
                    "0.0011 = environ 20 points perdus en 15 minutes sans rien faire")
            .defineInRange("baseDrainPerTick", 0.0011, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue THIRST_SPRINT_MULTIPLIER = BUILDER
            .comment("Multiplicateur quand le joueur court")
            .defineInRange("sprintMultiplier", 3.0, 1.0, 20.0);

    public static final ModConfigSpec.DoubleValue THIRST_HOT_BIOME_MULTIPLIER = BUILDER
            .comment("Multiplicateur dans les biomes chauds (température de base > 1.0, ex. désert)")
            .defineInRange("hotBiomeMultiplier", 1.5, 1.0, 20.0);

    public static final ModConfigSpec.DoubleValue THIRST_NETHER_MULTIPLIER = BUILDER
            .comment("Multiplicateur dans les dimensions brûlantes (Nether)")
            .defineInRange("netherMultiplier", 2.0, 1.0, 20.0);

    public static final ModConfigSpec.IntValue THIRST_SLOWNESS_THRESHOLD = BUILDER
            .comment("En dessous ou égal à ce niveau de soif, le joueur est ralenti")
            .defineInRange("slownessThreshold", 6, 0, 20);

    public static final ModConfigSpec.IntValue THIRST_DAMAGE_INTERVAL = BUILDER
            .comment("Ticks entre deux dégâts de déshydratation quand la soif est à 0")
            .defineInRange("damageIntervalTicks", 80, 1, 2400);

    public static final ModConfigSpec.DoubleValue DIRTY_WATER_SICKNESS_CHANCE = BUILDER
            .comment("Chance (0 à 1) de tomber malade en buvant de l'eau sale")
            .defineInRange("dirtyWaterSicknessChance", 0.4, 0.0, 1.0);

    static {
        BUILDER.pop();
    }

    static final ModConfigSpec SPEC = BUILDER.build();
}
