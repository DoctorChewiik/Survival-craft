package com.doctorchewiik.survivalcraft.registry;

import com.doctorchewiik.survivalcraft.SurvivalCraft;
import com.doctorchewiik.survivalcraft.item.CanteenItem;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SurvivalCraft.MODID);

    // Gourdes : nombre de gorgées, et si on peut les poser sur un feu pour faire bouillir l'eau
    public static final DeferredItem<CanteenItem> GOURD_CANTEEN = ITEMS.register("gourd_canteen",
            () -> new CanteenItem(3, false, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CanteenItem> LEATHER_CANTEEN = ITEMS.register("leather_canteen",
            () -> new CanteenItem(5, false, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<CanteenItem> METAL_CANTEEN = ITEMS.register("metal_canteen",
            () -> new CanteenItem(8, true, new Item.Properties().stacksTo(1)));
}
