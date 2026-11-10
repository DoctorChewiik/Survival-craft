package com.doctorchewiik.survivalcraft.registry;

import java.util.function.Supplier;

import com.doctorchewiik.survivalcraft.SurvivalCraft;
import com.doctorchewiik.survivalcraft.fatigue.FatigueData;
import com.doctorchewiik.survivalcraft.network.StatsSync;
import com.doctorchewiik.survivalcraft.temperature.TemperatureData;
import com.doctorchewiik.survivalcraft.thirst.ThirstData;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

// Données attachées aux entités et sauvegardées avec elles.
// Aucune n'est copiée à la mort : on réapparaît reposé, hydraté et à température normale.
public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, SurvivalCraft.MODID);

    public static final Supplier<AttachmentType<ThirstData>> THIRST = ATTACHMENT_TYPES.register("thirst",
            () -> AttachmentType.builder(() -> new ThirstData())
                    .serialize(ThirstData.CODEC)
                    .build());

    public static final Supplier<AttachmentType<TemperatureData>> TEMPERATURE = ATTACHMENT_TYPES.register("temperature",
            () -> AttachmentType.builder(() -> new TemperatureData())
                    .serialize(TemperatureData.CODEC)
                    .build());

    public static final Supplier<AttachmentType<FatigueData>> FATIGUE = ATTACHMENT_TYPES.register("fatigue",
            () -> AttachmentType.builder(() -> new FatigueData())
                    .serialize(FatigueData.CODEC)
                    .build());

    // Dernières valeurs envoyées au client (non sauvegardé)
    public static final Supplier<AttachmentType<StatsSync.State>> SYNC_STATE = ATTACHMENT_TYPES.register("sync_state",
            () -> AttachmentType.builder(() -> new StatsSync.State()).build());
}
