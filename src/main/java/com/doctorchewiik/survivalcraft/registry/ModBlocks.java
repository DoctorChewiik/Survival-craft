package com.doctorchewiik.survivalcraft.registry;

import com.doctorchewiik.survivalcraft.SurvivalCraft;
import com.doctorchewiik.survivalcraft.block.GroundCoverBlock;
import com.doctorchewiik.survivalcraft.block.JarBlock;
import com.doctorchewiik.survivalcraft.block.MonotubBlock;
import com.doctorchewiik.survivalcraft.block.PlacedCanteenBlock;
import com.doctorchewiik.survivalcraft.block.WaterFilterBlock;
import com.doctorchewiik.survivalcraft.block.WildMushroomBlock;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SurvivalCraft.MODID);

    // Cailloux et branches posés au sol, générés dans le monde
    public static final DeferredBlock<GroundCoverBlock> PEBBLE = BLOCKS.register("pebble",
            () -> new GroundCoverBlock(groundCover().mapColor(MapColor.STONE).sound(SoundType.STONE)));
    public static final DeferredBlock<GroundCoverBlock> GROUND_STICKS = BLOCKS.register("ground_sticks",
            () -> new GroundCoverBlock(groundCover().mapColor(MapColor.WOOD).sound(SoundType.WOOD)));

    public static final DeferredBlock<JarBlock> HERMETIC_JAR = BLOCKS.register("hermetic_jar",
            () -> new JarBlock(BlockBehaviour.Properties.of()
                    .strength(0.3f).sound(SoundType.GLASS).noOcclusion().pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<PlacedCanteenBlock> PLACED_CANTEEN = BLOCKS.register("placed_canteen",
            () -> new PlacedCanteenBlock(BlockBehaviour.Properties.of()
                    .strength(0.2f).sound(SoundType.WOOL).noOcclusion().pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<WaterFilterBlock> WATER_FILTER = BLOCKS.register("water_filter",
            () -> new WaterFilterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD).strength(1.5f).sound(SoundType.WOOD)));

    // Champignons sauvages et monotub
    public static final DeferredBlock<WildMushroomBlock> CUBENSIS = mushroom("cubensis_mushroom", MapColor.COLOR_BROWN);
    public static final DeferredBlock<WildMushroomBlock> COPELANDIA = mushroom("copelandia_mushroom", MapColor.TERRACOTTA_WHITE);
    public static final DeferredBlock<WildMushroomBlock> OYSTER = mushroom("oyster_mushroom", MapColor.COLOR_LIGHT_GRAY);
    public static final DeferredBlock<WildMushroomBlock> SHIITAKE = mushroom("shiitake_mushroom", MapColor.TERRACOTTA_BROWN);

    public static final DeferredBlock<MonotubBlock> MONOTUB = BLOCKS.register("monotub",
            () -> new MonotubBlock(BlockBehaviour.Properties.of()
                    .strength(0.5f).sound(SoundType.GLASS).noOcclusion()));

    private static DeferredBlock<WildMushroomBlock> mushroom(String name, MapColor color) {
        return BLOCKS.register(name, () -> new WildMushroomBlock(BlockBehaviour.Properties.of()
                .mapColor(color).noCollission().instabreak().sound(SoundType.GRASS)
                .offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));
    }

    private static BlockBehaviour.Properties groundCover() {
        return BlockBehaviour.Properties.of()
                .noCollission().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY);
    }
}
