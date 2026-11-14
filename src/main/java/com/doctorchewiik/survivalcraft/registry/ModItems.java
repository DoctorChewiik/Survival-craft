package com.doctorchewiik.survivalcraft.registry;

import com.doctorchewiik.survivalcraft.SurvivalCraft;
import com.doctorchewiik.survivalcraft.block.CanteenKind;
import com.doctorchewiik.survivalcraft.item.CanteenItem;
import com.doctorchewiik.survivalcraft.item.CoffeeItem;
import com.doctorchewiik.survivalcraft.item.JarItem;
import com.doctorchewiik.survivalcraft.item.ModTiers;

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
}
