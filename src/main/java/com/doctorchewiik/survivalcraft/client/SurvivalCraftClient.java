package com.doctorchewiik.survivalcraft.client;

import com.doctorchewiik.survivalcraft.SurvivalCraft;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
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
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        // Les gouttes de soif s'affichent juste au-dessus de la barre de faim
        event.registerAbove(VanillaGuiLayers.FOOD_LEVEL, SurvivalCraft.id("thirst"), ThirstHud::render);
    }
}
