package com.doctorchewiik.survivalcraft.block.entity;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.doctorchewiik.survivalcraft.Config;
import com.doctorchewiik.survivalcraft.SurvivalCraft;
import com.doctorchewiik.survivalcraft.block.MonotubBlock;
import com.doctorchewiik.survivalcraft.block.MonotubState;
import com.doctorchewiik.survivalcraft.item.CanteenItem;
import com.doctorchewiik.survivalcraft.menu.MonotubMenu;
import com.doctorchewiik.survivalcraft.mycology.GrainBag;
import com.doctorchewiik.survivalcraft.mycology.GrainBagItem;
import com.doctorchewiik.survivalcraft.mycology.MushroomSpecies;
import com.doctorchewiik.survivalcraft.registry.ModBlockEntities;
import com.doctorchewiik.survivalcraft.temperature.Temperature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Monotub :
 * - un sac de grains inoculé + du substrat lancent la colonisation (un sac pas fini = risque de contamination) ;
 * - l'humidité baisse avec le temps, on l'entretient avec un seau, une fiole d'eau ou une gourde ;
 * - la vitesse dépend de la température (plage idéale de l'espèce) et de l'humidité ;
 * - une fois colonisé, il donne 3 vagues de champignons de plus en plus petites, puis le substrat est épuisé ;
 * - trop d'humidité ou trop de chaleur peuvent le contaminer.
 */
public class MonotubBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_BAG = 0;
    public static final int SLOT_SUBSTRATE = 1;
    public static final int SLOT_WATER = 2;
    public static final int FIRST_OUTPUT = 3;
    public static final int SLOTS = 7;
    public static final int MAX_FLUSHES = 3;
    public static final int DATA_COUNT = 7;
    public static final TagKey<Item> SUBSTRATE = TagKey.create(Registries.ITEM, SurvivalCraft.id("mushroom_substrate"));

    private static final int[] FLUSH_YIELD = {5, 3, 2};
    private static final int COLONIZE_TICKS = 12000;
    private static final int FLUSH_TICKS = 6000;

    private final ItemStackHandler items = createHandler(this::setChanged);
    private MonotubState state = MonotubState.EMPTY;
    @Nullable
    private MushroomSpecies species;
    private float humidity;
    private float colonization;
    private float flushProgress;
    private int flushes;
    private float temperature = 10f;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> Math.round(humidity * 10);
                case 1 -> Math.round(colonization * 10);
                case 2 -> Math.round(flushProgress * 10);
                case 3 -> flushes;
                case 4 -> state.ordinal();
                case 5 -> Math.round(temperature * 10);
                case 6 -> species == null ? 0 : species.ordinal() + 1;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public MonotubBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.MONOTUB.get(), pos, blockState);
    }

    // Inventaire partagé par le bloc et l'écran (côté client), avec les mêmes règles
    public static ItemStackHandler createHandler(Runnable onChange) {
        return new ItemStackHandler(SLOTS) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return switch (slot) {
                    case SLOT_BAG -> stack.getItem() instanceof GrainBagItem
                            && GrainBagItem.get(stack).speciesValue().isPresent();
                    case SLOT_SUBSTRATE -> stack.is(SUBSTRATE);
                    case SLOT_WATER -> isWater(stack);
                    default -> false;
                };
            }

            @Override
            protected void onContentsChanged(int slot) {
                onChange.run();
            }
        };
    }

    private static boolean isWater(ItemStack stack) {
        return stack.is(Items.WATER_BUCKET)
                || stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)
                || stack.getItem() instanceof CanteenItem && CanteenItem.getSips(stack) > 0;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState blockState, MonotubBlockEntity tub) {
        long time = level.getGameTime();
        if (time % 20 == 0) {
            tub.temperature = Temperature.environment(level, pos);
        }
        tub.consumeWater();
        // L'humidité s'évapore, plus vite quand il fait chaud
        tub.humidity = Math.max(0f, tub.humidity - 0.003f * (1f + Math.max(0f, tub.temperature - 12f) * 0.15f));

        double speed = Config.MYCOLOGY_SPEED.get();
        switch (tub.state) {
            case EMPTY -> tub.tryStart(level);
            case COLONIZING -> {
                tub.colonization += (float) (100.0 / (COLONIZE_TICKS * tub.species.slowness())
                        * tub.species.temperatureFactor(tub.temperature) * colonizeHumidity(tub.humidity) * speed);
                if (tub.colonization >= 100f) {
                    tub.colonization = 100f;
                    tub.state = MonotubState.FRUITING;
                    tub.flushProgress = 0f;
                }
            }
            case FRUITING -> tub.fruit(level, pos, speed);
            case CONTAMINATED -> {
            }
        }
        if (time % 20 == 0) {
            tub.checkContamination(level);
            tub.setChanged();
        }
        if (blockState.getValue(MonotubBlock.STATE) != tub.state) {
            level.setBlock(pos, blockState.setValue(MonotubBlock.STATE, tub.state), 3);
        }
    }

    // Le mycélium supporte un substrat assez sec, mais pas desséché
    private static float colonizeHumidity(float humidity) {
        if (humidity < 30f) {
            return 0f;
        }
        if (humidity < 50f) {
            return 0.5f;
        }
        return humidity > 95f ? 0.8f : 1f;
    }

    // Les champignons ont besoin d'une forte humidité pour sortir
    private static float fruitHumidity(float humidity) {
        if (humidity < 50f) {
            return 0f;
        }
        if (humidity < 75f) {
            return 0.4f;
        }
        return humidity > 97f ? 0.6f : 1f;
    }

    private void consumeWater() {
        ItemStack water = items.getStackInSlot(SLOT_WATER);
        if (water.isEmpty()) {
            return;
        }
        if (water.is(Items.WATER_BUCKET) && humidity <= 60f) {
            humidity += 40f;
            items.setStackInSlot(SLOT_WATER, new ItemStack(Items.BUCKET));
        } else if (water.is(Items.POTION) && isWater(water) && humidity <= 85f) {
            humidity += 15f;
            items.setStackInSlot(SLOT_WATER, new ItemStack(Items.GLASS_BOTTLE));
        } else if (water.getItem() instanceof CanteenItem && humidity <= 90f && CanteenItem.takeSip(water)) {
            humidity += 10f;
            setChanged();
        }
    }

    private void tryStart(Level level) {
        ItemStack bagStack = items.getStackInSlot(SLOT_BAG);
        ItemStack substrate = items.getStackInSlot(SLOT_SUBSTRATE);
        if (!(bagStack.getItem() instanceof GrainBagItem) || substrate.isEmpty()) {
            return;
        }
        GrainBagItem.refresh(bagStack, level.getGameTime());
        GrainBag bag = GrainBagItem.get(bagStack);
        if (bag.speciesValue().isEmpty()) {
            return;
        }
        // Un sac pas entièrement colonisé laisse la place aux moisissures
        float risk = switch (bag.stage()) {
            case CONTAMINATED -> 1f;
            case COLONIZED -> 0.02f;
            default -> 1f - bag.percent() / 100f;
        };
        if (!Config.CONTAMINATION_ENABLED.get() && bag.stage() != GrainBag.Stage.CONTAMINATED) {
            risk = 0f;
        }
        species = bag.speciesValue().get();
        items.extractItem(SLOT_BAG, 1, false);
        items.extractItem(SLOT_SUBSTRATE, 1, false);
        colonization = 0f;
        flushProgress = 0f;
        flushes = 0;
        state = level.getRandom().nextFloat() < risk ? MonotubState.CONTAMINATED : MonotubState.COLONIZING;
        level.playSound(null, worldPosition, SoundEvents.ROOTED_DIRT_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    private void fruit(Level level, BlockPos pos, double speed) {
        if (flushProgress < 100f) {
            flushProgress = Math.min(100f, flushProgress + (float) (100.0 / (FLUSH_TICKS * species.slowness())
                    * species.temperatureFactor(temperature) * fruitHumidity(humidity) * speed));
            return;
        }
        // Récolte automatique dans les cases de sortie ; on attend s'il n'y a pas la place
        ItemStack harvest = new ItemStack(species.mushroom(), FLUSH_YIELD[Math.min(flushes, FLUSH_YIELD.length - 1)]);
        if (!addOutput(harvest)) {
            return;
        }
        level.playSound(null, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0f, 1.0f);
        flushes++;
        flushProgress = 0f;
        humidity = Math.max(0f, humidity - 15f);
        if (flushes >= MAX_FLUSHES) {
            // Substrat épuisé
            reset();
        }
    }

    private boolean addOutput(ItemStack stack) {
        int space = 0;
        for (int slot = FIRST_OUTPUT; slot < SLOTS; slot++) {
            ItemStack existing = items.getStackInSlot(slot);
            if (existing.isEmpty()) {
                space += stack.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
                space += existing.getMaxStackSize() - existing.getCount();
            }
        }
        if (space < stack.getCount()) {
            return false;
        }
        int remaining = stack.getCount();
        for (int slot = FIRST_OUTPUT; slot < SLOTS && remaining > 0; slot++) {
            ItemStack existing = items.getStackInSlot(slot);
            if (existing.isEmpty()) {
                int amount = Math.min(remaining, stack.getMaxStackSize());
                items.setStackInSlot(slot, stack.copyWithCount(amount));
                remaining -= amount;
            } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
                int amount = Math.min(remaining, existing.getMaxStackSize() - existing.getCount());
                items.setStackInSlot(slot, existing.copyWithCount(existing.getCount() + amount));
                remaining -= amount;
            }
        }
        return true;
    }

    private void checkContamination(Level level) {
        if (species == null || !Config.CONTAMINATION_ENABLED.get()
                || (state != MonotubState.COLONIZING && state != MonotubState.FRUITING)) {
            return;
        }
        boolean soaked = humidity > 97f && level.getRandom().nextFloat() < 0.01f;
        boolean overheated = temperature > species.idealMax() + 4f && level.getRandom().nextFloat() < 0.005f;
        if (soaked || overheated) {
            state = MonotubState.CONTAMINATED;
            level.playSound(null, worldPosition, SoundEvents.SLIME_SQUISH_SMALL, SoundSource.BLOCKS, 1.0f, 0.6f);
        }
    }

    // Bouton "Vider" de l'écran : jette le substrat (contaminé ou non) et remet le bac à zéro
    public void reset() {
        state = MonotubState.EMPTY;
        species = null;
        colonization = 0f;
        flushProgress = 0f;
        flushes = 0;
        setChanged();
    }

    public List<ItemStack> drops() {
        List<ItemStack> drops = new ArrayList<>();
        for (int slot = 0; slot < SLOTS; slot++) {
            drops.add(items.getStackInSlot(slot).copy());
        }
        return drops;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.survivalcraft.monotub");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MonotubMenu(containerId, inventory, items, data, ContainerLevelAccess.create(level, worldPosition),
                this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("items", items.serializeNBT(registries));
        tag.putString("state", state.getSerializedName());
        tag.putString("species", species == null ? "" : species.getSerializedName());
        tag.putFloat("humidity", humidity);
        tag.putFloat("colonization", colonization);
        tag.putFloat("flush_progress", flushProgress);
        tag.putInt("flushes", flushes);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.deserializeNBT(registries, tag.getCompound("items"));
        state = MonotubState.EMPTY;
        for (MonotubState value : MonotubState.values()) {
            if (value.getSerializedName().equals(tag.getString("state"))) {
                state = value;
            }
        }
        species = MushroomSpecies.byName(tag.getString("species")).orElse(null);
        if (species == null && state != MonotubState.EMPTY) {
            state = MonotubState.EMPTY;
        }
        humidity = tag.getFloat("humidity");
        colonization = tag.getFloat("colonization");
        flushProgress = tag.getFloat("flush_progress");
        flushes = tag.getInt("flushes");
    }
}
