package com.doctorchewiik.survivalcraft.registry;

import com.doctorchewiik.survivalcraft.SurvivalCraft;
import com.doctorchewiik.survivalcraft.block.CanteenKind;
import com.doctorchewiik.survivalcraft.item.CanteenItem;
import com.doctorchewiik.survivalcraft.item.CoffeeItem;
import com.doctorchewiik.survivalcraft.item.JarItem;
import com.doctorchewiik.survivalcraft.item.ModTiers;
import com.doctorchewiik.survivalcraft.mycology.GrainBagItem;
import com.doctorchewiik.survivalcraft.mycology.MushroomSpecies;
import com.doctorchewiik.survivalcraft.mycology.SporePrintItem;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SurvivalCraft.MODID);

    // Gourdes : nombre de gorgées, et si on peut les poser sur un feu pour faire bouillir l'eau
    public static final DeferredItem<CanteenItem> GOURD_CANTEEN = ITEMS.register("gourd_canteen",
            () -> new CanteenItem(3, false, CanteenKind.GOURD, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CanteenItem> LEATHER_CANTEEN = ITEMS.register("leather_canteen",
            () -> new CanteenItem(5, false, CanteenKind.LEATHER, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CanteenItem> METAL_CANTEEN = ITEMS.register("metal_canteen",
            () -> new CanteenItem(8, true, CanteenKind.METAL, new Item.Properties().stacksTo(1)));

    // Purification de l'eau
    public static final DeferredItem<Item> PURIFICATION_TABLET = ITEMS.registerSimpleItem("purification_tablet",
            new Item.Properties().stacksTo(16));
    public static final DeferredItem<BlockItem> WATER_FILTER = ITEMS.registerSimpleBlockItem("water_filter", ModBlocks.WATER_FILTER);

    // Conservation
    public static final DeferredItem<JarItem> HERMETIC_JAR = ITEMS.register("hermetic_jar",
            () -> new JarItem(ModBlocks.HERMETIC_JAR.get(), new Item.Properties().stacksTo(16)));

    // Café
    public static final DeferredItem<Item> ROASTED_COFFEE_BEANS = ITEMS.registerSimpleItem("roasted_coffee_beans");
    public static final DeferredItem<CoffeeItem> COFFEE = ITEMS.register("coffee",
            () -> new CoffeeItem(new Item.Properties().stacksTo(1)));

    // Début de partie primitif
    public static final DeferredItem<BlockItem> PEBBLE = ITEMS.registerSimpleBlockItem("pebble", ModBlocks.PEBBLE);
    public static final DeferredItem<SwordItem> FLINT_KNIFE = ITEMS.register("flint_knife",
            () -> new SwordItem(ModTiers.FLINT, new Item.Properties()
                    .attributes(SwordItem.createAttributes(ModTiers.FLINT, 2, -2.0f))));
    public static final DeferredItem<AxeItem> FLINT_HATCHET = ITEMS.register("flint_hatchet",
            () -> new AxeItem(ModTiers.FLINT, new Item.Properties()
                    .attributes(AxeItem.createAttributes(ModTiers.FLINT, 5.0f, -3.2f))));

    // Champignons : se mangent, se posent, et servent à faire des empreintes de spores
    public static final DeferredItem<BlockItem> CUBENSIS = ITEMS.register("cubensis_mushroom",
            () -> new BlockItem(ModBlocks.CUBENSIS.get(), new Item.Properties().food(magicMushroom(600, 900))));
    public static final DeferredItem<BlockItem> COPELANDIA = ITEMS.register("copelandia_mushroom",
            () -> new BlockItem(ModBlocks.COPELANDIA.get(), new Item.Properties().food(magicMushroom(900, 1200))));
    public static final DeferredItem<BlockItem> OYSTER = ITEMS.register("oyster_mushroom",
            () -> new BlockItem(ModBlocks.OYSTER.get(), new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.4f).build())));
    public static final DeferredItem<BlockItem> SHIITAKE = ITEMS.register("shiitake_mushroom",
            () -> new BlockItem(ModBlocks.SHIITAKE.get(), new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.5f).build())));

    public static final DeferredItem<SporePrintItem> CUBENSIS_SPORE_PRINT = sporePrint(MushroomSpecies.CUBENSIS);
    public static final DeferredItem<SporePrintItem> COPELANDIA_SPORE_PRINT = sporePrint(MushroomSpecies.COPELANDIA);
    public static final DeferredItem<SporePrintItem> OYSTER_SPORE_PRINT = sporePrint(MushroomSpecies.OYSTER);
    public static final DeferredItem<SporePrintItem> SHIITAKE_SPORE_PRINT = sporePrint(MushroomSpecies.SHIITAKE);

    public static final DeferredItem<GrainBagItem> GRAIN_BAG = ITEMS.register("grain_bag",
            () -> new GrainBagItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<BlockItem> MONOTUB = ITEMS.registerSimpleBlockItem("monotub", ModBlocks.MONOTUB);

    private static DeferredItem<SporePrintItem> sporePrint(MushroomSpecies species) {
        return ITEMS.register(species.getSerializedName() + "_spore_print",
                () -> new SporePrintItem(species, new Item.Properties().stacksTo(16)));
    }

    // Nausée (vision déformée) et vision nocturne : les effets visuels dédiés viendront plus tard
    private static FoodProperties magicMushroom(int nauseaTicks, int nightVisionTicks) {
        return new FoodProperties.Builder().nutrition(1).saturationModifier(0.1f).alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, nauseaTicks), 1.0f)
                .effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, nightVisionTicks), 1.0f)
                .build();
    }
}
