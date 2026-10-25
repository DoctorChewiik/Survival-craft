package com.doctorchewiik.survivalcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;

final class Supports {
    private Supports() {
    }

    // Le bloc en dessous doit avoir une face supérieure solide
    static boolean hasSolidGround(LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }
}
