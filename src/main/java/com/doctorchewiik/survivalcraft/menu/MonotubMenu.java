package com.doctorchewiik.survivalcraft.menu;

import org.jetbrains.annotations.Nullable;

import com.doctorchewiik.survivalcraft.block.MonotubState;
import com.doctorchewiik.survivalcraft.block.entity.MonotubBlockEntity;
import com.doctorchewiik.survivalcraft.mycology.MushroomSpecies;
import com.doctorchewiik.survivalcraft.registry.ModBlocks;
import com.doctorchewiik.survivalcraft.registry.ModMenus;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Écran du monotub : 3 cases d'entrée (sac, substrat, eau), 4 cases de récolte,
 * et les jauges envoyées par le serveur (humidité, température, colonisation, prochaine vague).
 */
public class MonotubMenu extends AbstractContainerMenu {
    public static final int BUTTON_EMPTY = 0;
    private static final int TUB_SLOTS = MonotubBlockEntity.SLOTS;

    private final ContainerData data;
    private final ContainerLevelAccess access;
    @Nullable
    private final MonotubBlockEntity tub;

    // Côté client : l'inventaire et les jauges sont synchronisés par le serveur
    public MonotubMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, MonotubBlockEntity.createHandler(() -> {
        }), new SimpleContainerData(MonotubBlockEntity.DATA_COUNT), ContainerLevelAccess.NULL, null);
    }

    public MonotubMenu(int containerId, Inventory inventory, IItemHandler items, ContainerData data,
            ContainerLevelAccess access, @Nullable MonotubBlockEntity tub) {
        super(ModMenus.MONOTUB.get(), containerId);
        checkContainerDataCount(data, MonotubBlockEntity.DATA_COUNT);
        this.data = data;
        this.access = access;
        this.tub = tub;

        addSlot(new SlotItemHandler(items, MonotubBlockEntity.SLOT_BAG, 8, 22));
        addSlot(new SlotItemHandler(items, MonotubBlockEntity.SLOT_SUBSTRATE, 8, 44));
        addSlot(new SlotItemHandler(items, MonotubBlockEntity.SLOT_WATER, 8, 66));
        for (int i = 0; i < 4; i++) {
            addSlot(new SlotItemHandler(items, MonotubBlockEntity.FIRST_OUTPUT + i, 134 + (i % 2) * 18, 22 + (i / 2) * 18));
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 118 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 176));
        }
        addDataSlots(data);
    }

    public float humidity() {
        return data.get(0) / 10f;
    }

    public float colonization() {
        return data.get(1) / 10f;
    }

    public float flushProgress() {
        return data.get(2) / 10f;
    }

    public int flushes() {
        return data.get(3);
    }

    public MonotubState state() {
        MonotubState[] values = MonotubState.values();
        int index = data.get(4);
        return index >= 0 && index < values.length ? values[index] : MonotubState.EMPTY;
    }

    public float temperature() {
        return data.get(5) / 10f;
    }

    @Nullable
    public MushroomSpecies species() {
        int index = data.get(6) - 1;
        MushroomSpecies[] values = MushroomSpecies.values();
        return index >= 0 && index < values.length ? values[index] : null;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_EMPTY && tub != null) {
            tub.reset();
            return true;
        }
        return id == BUTTON_EMPTY;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < TUB_SLOTS) {
            if (!moveItemStackTo(stack, TUB_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, MonotubBlockEntity.FIRST_OUTPUT, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.MONOTUB.get());
    }
}
