package com.doctorchewiik.survivalcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// Bloc qui contient une gourde (gourde posée, filtre à eau)
public abstract class CanteenHolderBlockEntity extends BlockEntity {
    private ItemStack canteen = ItemStack.EMPTY;

    protected CanteenHolderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ItemStack getCanteen() {
        return canteen;
    }

    public void setCanteen(ItemStack canteen) {
        this.canteen = canteen;
        setChanged();
    }

    // Retire la gourde et la renvoie
    public ItemStack takeCanteen() {
        ItemStack taken = canteen;
        setCanteen(ItemStack.EMPTY);
        return taken;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!canteen.isEmpty()) {
            tag.put("canteen", canteen.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        canteen = ItemStack.parseOptional(registries, tag.getCompound("canteen"));
    }
}
