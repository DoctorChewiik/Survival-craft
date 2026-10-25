package com.doctorchewiik.survivalcraft.registry;

import java.util.function.Supplier;

import com.doctorchewiik.survivalcraft.SurvivalCraft;
import com.doctorchewiik.survivalcraft.thirst.ThirstData;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

// Données attachées aux entités et sauvegardées avec elles
public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, SurvivalCraft.MODID);

    // Soif du joueur. Non copiée à la mort : on réapparaît avec la soif pleine.
    public static final Supplier<AttachmentType<ThirstData>> THIRST = ATTACHMENT_TYPES.register("thirst",
            () -> AttachmentType.builder(() -> new ThirstData())
                    .serialize(ThirstData.CODEC)
                    .build());
}
