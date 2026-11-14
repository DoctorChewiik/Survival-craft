package com.doctorchewiik.survivalcraft.block.entity;

import com.doctorchewiik.survivalcraft.Config;
import com.doctorchewiik.survivalcraft.item.CanteenItem;
import com.doctorchewiik.survivalcraft.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class PlacedCanteenBlockEntity extends CanteenHolderBlockEntity {
    public PlacedCanteenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PLACED_CANTEEN.get(), pos, state);
    }

    // Sous la pluie, la gourde se remplit d'eau de pluie, qui est potable
    public static void serverTick(Level level, BlockPos pos, BlockState state, PlacedCanteenBlockEntity entity) {
        if (level.getGameTime() % Config.RAIN_FILL_INTERVAL.get() != 0 || !level.isRainingAt(pos.above())) {
            return;
        }
        if (entity.getCanteen().getItem() instanceof CanteenItem canteen && canteen.addSips(entity.getCanteen(), 1, false)) {
            entity.setChanged();
        }
    }
}
