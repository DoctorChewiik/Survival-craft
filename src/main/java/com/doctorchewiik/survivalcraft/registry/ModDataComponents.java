package com.doctorchewiik.survivalcraft.registry;

import java.util.function.Supplier;

import com.doctorchewiik.survivalcraft.SurvivalCraft;
import com.doctorchewiik.survivalcraft.item.JarContents;
import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

// Données stockées directement sur les items (contenu d'une gourde, etc.)
public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, SurvivalCraft.MODID);

    // Nombre de gorgées restantes dans un contenant
    public static final Supplier<DataComponentType<Integer>> WATER_SIPS = DATA_COMPONENTS.register("water_sips",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    // true si l'eau contenue n'a pas été purifiée
    public static final Supplier<DataComponentType<Boolean>> DIRTY_WATER = DATA_COMPONENTS.register("dirty_water",
            () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

    // Aliments rangés dans un bocal hermétique
    public static final Supplier<DataComponentType<JarContents>> JAR_CONTENTS = DATA_COMPONENTS.register("jar_contents",
            () -> DataComponentType.<JarContents>builder()
                    .persistent(JarContents.CODEC)
                    .networkSynchronized(JarContents.STREAM_CODEC)
                    .build());
}
