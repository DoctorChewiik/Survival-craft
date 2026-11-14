package com.doctorchewiik.survivalcraft.registry;

import com.doctorchewiik.survivalcraft.SurvivalCraft;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SurvivalCraft.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.survivalcraft"))
                    .icon(() -> ModItems.LEATHER_CANTEEN.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.GOURD_CANTEEN.get());
                        output.accept(ModItems.LEATHER_CANTEEN.get());
                        output.accept(ModItems.METAL_CANTEEN.get());
                        // Versions déjà remplies d'eau potable, pratiques pour tester
                        output.accept(ModItems.GOURD_CANTEEN.get().filled(false));
                        output.accept(ModItems.LEATHER_CANTEEN.get().filled(false));
                        output.accept(ModItems.METAL_CANTEEN.get().filled(false));
                        output.accept(ModItems.PURIFICATION_TABLET.get());
                        output.accept(ModItems.WATER_FILTER.get());
                        output.accept(ModItems.HERMETIC_JAR.get());
                        output.accept(ModItems.ROASTED_COFFEE_BEANS.get());
                        output.accept(ModItems.COFFEE.get());
                        output.accept(ModItems.PEBBLE.get());
                        output.accept(ModItems.FLINT_KNIFE.get());
                        output.accept(ModItems.FLINT_HATCHET.get());
                    })
                    .build());
}
