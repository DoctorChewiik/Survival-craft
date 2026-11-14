package com.doctorchewiik.survivalcraft.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

// Champignon sauvage posé sur la terre ou l'herbe : se cueille d'un coup
public class WildMushroomBlock extends BushBlock {
    public static final MapCodec<WildMushroomBlock> CODEC = simpleCodec(WildMushroomBlock::new);
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 8, 12);

    public WildMushroomBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(level, pos);
        return SHAPE.move(offset.x, offset.y, offset.z);
    }
}
