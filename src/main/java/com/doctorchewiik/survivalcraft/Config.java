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
            .comment("Multiplicateur dans les biomes chauds, utilisé seulement si la température corporelle est désactivée")
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

    public static final ModConfigSpec.IntValue RAIN_FILL_INTERVAL = BUILDER
            .comment("Ticks pour qu'une gourde posée sous la pluie gagne une gorgée")
            .defineInRange("rainFillIntervalTicks", 200, 1, 24000);

    public static final ModConfigSpec.IntValue WATER_FILTER_TIME = BUILDER
            .comment("Ticks pour qu'un filtre à eau purifie une gourde")
            .defineInRange("waterFilterTicks", 600, 1, 24000);

    static {
        BUILDER.pop();
        BUILDER.comment("Température corporelle (0 = gelé, 10 = normal, 20 = brûlant)").push("temperature");
    }

    public static final ModConfigSpec.BooleanValue TEMPERATURE_ENABLED = BUILDER
            .comment("Active la température corporelle")
            .define("enabled", true);

    public static final ModConfigSpec.DoubleValue TEMPERATURE_ADAPT_RATE = BUILDER
            .comment("Vitesse à laquelle le corps se rapproche de la température ambiante (par tick)")
            .defineInRange("adaptRate", 0.002, 0.0001, 1.0);

    public static final ModConfigSpec.IntValue TEMPERATURE_DAMAGE_INTERVAL = BUILDER
            .comment("Ticks entre deux dégâts d'hypothermie ou de coup de chaleur")
            .defineInRange("damageIntervalTicks", 80, 1, 2400);

    static {
        BUILDER.pop();
        BUILDER.comment("Fatigue et sommeil (0 = reposé, 100 = épuisé)").push("fatigue");
    }

    public static final ModConfigSpec.BooleanValue FATIGUE_ENABLED = BUILDER
            .comment("Active la fatigue")
            .define("enabled", true);

    public static final ModConfigSpec.DoubleValue FATIGUE_PER_TICK = BUILDER
            .comment("Fatigue ajoutée à chaque tick éveillé. 0.0033 = épuisé après environ une journée et quart")
            .defineInRange("perTick", 0.0033, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue FATIGUE_PER_BLOCK_BROKEN = BUILDER
            .comment("Fatigue ajoutée à chaque bloc cassé")
            .defineInRange("perBlockBroken", 0.05, 0.0, 10.0);

    static {
        BUILDER.pop();
        BUILDER.comment("Début de partie primitif").push("primitive");
    }

    public static final ModConfigSpec.BooleanValue REQUIRE_AXE_FOR_LOGS = BUILDER
            .comment("Impossible de casser les bûches sans hache")
            .define("requireAxeForLogs", true);

    static {
        BUILDER.pop();
    }

    static final ModConfigSpec SPEC = BUILDER.build();
}
