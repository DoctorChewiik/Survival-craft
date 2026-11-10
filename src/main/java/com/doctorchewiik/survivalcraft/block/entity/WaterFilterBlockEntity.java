package com.doctorchewiik.survivalcraft.block.entity;

import com.doctorchewiik.survivalcraft.Config;
import com.doctorchewiik.survivalcraft.block.WaterFilterBlock;
import com.doctorchewiik.survivalcraft.item.CanteenItem;
import com.doctorchewiik.survivalcraft.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

// Le filtre (charbon et sable) purifie lentement l'eau d'une gourde, sans feu
public class WaterFilterBlockEntity extends CanteenHolderBlockEntity {
    private int progress;

    public WaterFilterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WATER_FILTER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WaterFilterBlockEntity entity) {
        boolean filtering = CanteenItem.getSips(entity.getCanteen()) > 0 && CanteenItem.isDirty(entity.getCanteen());
        if (state.getValue(WaterFilterBlock.FILTERING) != filtering) {
            level.setBlock(pos, state.setValue(WaterFilterBlock.FILTERING, filtering), 3);
        }
        if (!filtering) {
            entity.progress = 0;
            return;
        }
        if (++entity.progress >= Config.WATER_FILTER_TIME.get()) {
            entity.progress = 0;
            CanteenItem.purify(entity.getCanteen());
            entity.setChanged();
            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 0.6f, 1.2f);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        progress = tag.getInt("progress");
    }
}
