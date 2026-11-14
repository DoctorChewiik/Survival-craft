package com.doctorchewiik.survivalcraft.block.entity;

import com.doctorchewiik.survivalcraft.SurvivalCraft;
import com.doctorchewiik.survivalcraft.item.JarContents;
import com.doctorchewiik.survivalcraft.registry.ModBlockEntities;
import com.doctorchewiik.survivalcraft.registry.ModDataComponents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// Bocal posé : garde son contenu, et le rend quand on le casse (voir la loot table)
public class JarBlockEntity extends BlockEntity {
    private JarContents contents = JarContents.EMPTY;

    public JarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.JAR.get(), pos, state);
    }

    public JarContents getContents() {
        return contents;
    }

    public void setContents(JarContents contents) {
        this.contents = contents;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!contents.isEmpty()) {
            JarContents.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), contents)
                    .resultOrPartial(SurvivalCraft.LOGGER::error)
                    .ifPresent(encoded -> tag.put("contents", encoded));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        contents = tag.contains("contents")
                ? JarContents.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("contents"))
                        .resultOrPartial(SurvivalCraft.LOGGER::error)
                        .orElse(JarContents.EMPTY)
                : JarContents.EMPTY;
    }

    // Item -> bloc à la pose, bloc -> item quand on le casse
    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        contents = input.getOrDefault(ModDataComponents.JAR_CONTENTS.get(), JarContents.EMPTY);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        if (!contents.isEmpty()) {
            builder.set(ModDataComponents.JAR_CONTENTS.get(), contents);
        }
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("contents");
    }
}
