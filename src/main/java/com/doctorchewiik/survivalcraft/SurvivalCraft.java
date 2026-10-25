package com.doctorchewiik.survivalcraft;

import org.slf4j.Logger;

import com.doctorchewiik.survivalcraft.command.ModCommands;
import com.doctorchewiik.survivalcraft.network.ModNetworking;
import com.doctorchewiik.survivalcraft.registry.ModAttachments;
import com.doctorchewiik.survivalcraft.registry.ModCreativeTabs;
import com.doctorchewiik.survivalcraft.registry.ModDataComponents;
import com.doctorchewiik.survivalcraft.registry.ModItems;
import com.doctorchewiik.survivalcraft.thirst.ThirstEvents;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(SurvivalCraft.MODID)
public class SurvivalCraft {
    public static final String MODID = "survivalcraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SurvivalCraft(IEventBus modEventBus, ModContainer modContainer) {
        // Registres (items, composants de données, données attachées au joueur, onglet créatif)
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        // Réseau (synchronisation serveur -> client)
        modEventBus.addListener(ModNetworking::register);

        // Événements de jeu
        ThirstEvents.register(NeoForge.EVENT_BUS);
        NeoForge.EVENT_BUS.addListener(ModCommands::register);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
