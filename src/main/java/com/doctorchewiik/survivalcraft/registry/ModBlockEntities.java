package com.doctorchewiik.survivalcraft.registry;

import java.util.function.Supplier;

import com.doctorchewiik.survivalcraft.SurvivalCraft;
import com.doctorchewiik.survivalcraft.block.entity.JarBlockEntity;
import com.doctorchewiik.survivalcraft.block.entity.PlacedCanteenBlockEntity;
import com.doctorchewiik.survivalcraft.block.entity.WaterFilterBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SurvivalCraft.MODID);

    public static final Supplier<BlockEntityType<JarBlockEntity>> JAR = BLOCK_ENTITY_TYPES.register("hermetic_jar",
            () -> BlockEntityType.Builder.of(JarBlockEntity::new, ModBlocks.HERMETIC_JAR.get()).build(null));

    public static final Supplier<BlockEntityType<PlacedCanteenBlockEntity>> PLACED_CANTEEN =
            BLOCK_ENTITY_TYPES.register("placed_canteen",
                    () -> BlockEntityType.Builder.of(PlacedCanteenBlockEntity::new, ModBlocks.PLACED_CANTEEN.get())
                            .build(null));

    public static final Supplier<BlockEntityType<WaterFilterBlockEntity>> WATER_FILTER =
            BLOCK_ENTITY_TYPES.register("water_filter",
                    () -> BlockEntityType.Builder.of(WaterFilterBlockEntity::new, ModBlocks.WATER_FILTER.get())
                            .build(null));
}
