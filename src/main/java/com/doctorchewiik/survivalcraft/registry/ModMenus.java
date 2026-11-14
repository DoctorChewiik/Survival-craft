package com.doctorchewiik.survivalcraft.registry;

import java.util.function.Supplier;

import com.doctorchewiik.survivalcraft.SurvivalCraft;
import com.doctorchewiik.survivalcraft.menu.MonotubMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, SurvivalCraft.MODID);

    public static final Supplier<MenuType<MonotubMenu>> MONOTUB = MENUS.register("monotub",
            () -> IMenuTypeExtension.create(MonotubMenu::new));
}
