package com.doctorchewiik.survivalcraft.client;

import com.doctorchewiik.survivalcraft.SurvivalCraft;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import com.doctorchewiik.survivalcraft.mycology.GrainBagItem;
import com.doctorchewiik.survivalcraft.registry.ModItems;
import com.doctorchewiik.survivalcraft.registry.ModMenus;

import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

// Cette classe n'est jamais chargée sur un serveur dédié.
@Mod(value = SurvivalCraft.MODID, dist = Dist.CLIENT)
public class SurvivalCraftClient {
    public SurvivalCraftClient(IEventBus modEventBus, ModContainer container) {
        // Écran de configuration accessible depuis la liste des mods
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(SurvivalCraftClient::registerGuiLayers);
        modEventBus.addListener(SurvivalCraftClient::registerScreens);
        modEventBus.addListener(SurvivalCraftClient::clientSetup);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.MONOTUB.get(), MonotubScreen::new);
    }

    // L'apparence du sac de grains change selon son étape (0 = cru ... 4 = contaminé)
    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(ModItems.GRAIN_BAG.get(), SurvivalCraft.id("stage"),
                (stack, level, entity, seed) -> GrainBagItem.get(stack).stage().ordinal() / 4f));
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        // Soif juste au-dessus de la faim, énergie juste au-dessus de la vie, température en bas à gauche
        event.registerAbove(VanillaGuiLayers.FOOD_LEVEL, SurvivalCraft.id("thirst"), SurvivalHud::renderThirst);
        event.registerAbove(VanillaGuiLayers.PLAYER_HEALTH, SurvivalCraft.id("energy"), SurvivalHud::renderEnergy);
        event.registerAbove(VanillaGuiLayers.HOTBAR, SurvivalCraft.id("thermometer"), SurvivalHud::renderThermometer);
        event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, SurvivalCraft.id("fatigue_overlay"),
                SurvivalHud::renderFatigueOverlay);
    }
}
