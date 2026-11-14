package com.doctorchewiik.survivalcraft.mycology;

import java.util.List;

import com.doctorchewiik.survivalcraft.Config;
import com.doctorchewiik.survivalcraft.mycology.GrainBag.Stage;
import com.doctorchewiik.survivalcraft.registry.ModDataComponents;
import com.doctorchewiik.survivalcraft.registry.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;

/**
 * Sac de grains :
 * 1. clic droit sur un feu de camp allumé : stérilisé ;
 * 2. clic droit avec une empreinte de spores sur le sac (dans l'inventaire) : inoculé ;
 * 3. le mycélium colonise le sac avec le temps (environ un jour de jeu) ;
 * 4. un sac mal stérilisé, ou inoculé trop longtemps après la stérilisation, peut être contaminé.
 */
public class GrainBagItem extends Item {
    // Durée de colonisation d'un sac pour une espèce de vitesse normale
    public static final int BAG_COLONIZE_TICKS = 24000;
    // Au-delà d'un jour après la stérilisation, le sac a eu le temps d'attraper des moisissures
    private static final int STERILE_WINDOW_TICKS = 24000;
    private static final float CONTAMINATION_FRESH = 0.1f;
    private static final float CONTAMINATION_STALE = 0.4f;

    public GrainBagItem(Properties properties) {
        super(properties);
    }

    public static GrainBag get(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.GRAIN_BAG.get(), GrainBag.RAW);
    }

    private static void set(ItemStack stack, GrainBag bag) {
        stack.set(ModDataComponents.GRAIN_BAG.get(), bag);
    }

    // Un sac déjà colonisé, pour l'onglet créatif
    public static ItemStack colonized(MushroomSpecies species) {
        ItemStack stack = new ItemStack(ModItems.GRAIN_BAG.get());
        set(stack, new GrainBag(Stage.COLONIZED, species.getSerializedName(), 0L, 0L, 100, false));
        return stack;
    }

    // Met à jour la colonisation d'après l'heure du monde. Le sac avance même rangé dans un coffre.
    public static void refresh(ItemStack stack, long now) {
        GrainBag bag = get(stack);
        if (bag.stage() != Stage.INOCULATED || bag.speciesValue().isEmpty()) {
            return;
        }
        double total = BAG_COLONIZE_TICKS * bag.speciesValue().get().slowness() / Config.MYCOLOGY_SPEED.get();
        int percent = (int) Mth.clamp((now - bag.inoculatedAt()) * 100.0 / total, 0, 100);
        if (bag.doomed() && percent >= 50) {
            set(stack, bag.withStage(Stage.CONTAMINATED, percent));
        } else if (percent >= 100) {
            set(stack, bag.withStage(Stage.COLONIZED, 100));
        } else if (percent != bag.percent()) {
            set(stack, bag.withStage(Stage.INOCULATED, percent));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide() && level.getGameTime() % 20 == 0) {
            refresh(stack, level.getGameTime());
        }
    }

    // Stérilisation sur un feu de camp allumé (toute la pile d'un coup)
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!CampfireBlock.isLitCampfire(level.getBlockState(pos)) || get(stack).stage() != Stage.RAW) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            set(stack, new GrainBag(Stage.STERILIZED, "", level.getGameTime(), 0L, 0, false));
            level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0f, 0.8f);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        8, 0.2, 0.1, 0.2, 0.01);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    // Inoculation : empreinte de spores déposée (clic droit) sur un sac stérilisé dans l'inventaire
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action,
            Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !(other.getItem() instanceof SporePrintItem print)
                || !slot.allowModification(player)) {
            return false;
        }
        GrainBag bag = get(stack);
        if (bag.stage() != Stage.STERILIZED) {
            return false;
        }
        if (player.level().isClientSide()) {
            return true;
        }
        long now = player.level().getGameTime();
        float chance = now - bag.sterilizedAt() > STERILE_WINDOW_TICKS ? CONTAMINATION_STALE : CONTAMINATION_FRESH;
        boolean doomed = Config.CONTAMINATION_ENABLED.get() && player.getRandom().nextFloat() < chance;
        GrainBag inoculated = new GrainBag(Stage.INOCULATED, print.getSpecies().getSerializedName(),
                bag.sterilizedAt(), now, 0, doomed);
        if (stack.getCount() == 1) {
            set(stack, inoculated);
        } else {
            // Un seul sac de la pile est inoculé, il va dans l'inventaire
            ItemStack single = stack.split(1);
            set(single, inoculated);
            if (!player.getInventory().add(single)) {
                player.drop(single, false);
            }
        }
        other.shrink(1);
        player.playSound(SoundEvents.BUNDLE_INSERT, 0.8f, 1.0f);
        return true;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return get(stack).stage() == Stage.INOCULATED;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Mth.clamp(Math.round(13.0f * get(stack).percent() / 100f), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xEDEDED;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        GrainBag bag = get(stack);
        ChatFormatting color = switch (bag.stage()) {
            case RAW -> ChatFormatting.GRAY;
            case STERILIZED -> ChatFormatting.AQUA;
            case INOCULATED -> ChatFormatting.WHITE;
            case COLONIZED -> ChatFormatting.GREEN;
            case CONTAMINATED -> ChatFormatting.DARK_GREEN;
        };
        tooltip.add(Component.translatable("tooltip.survivalcraft.grain_bag." + bag.stage().getSerializedName())
                .withStyle(color));
        bag.speciesValue().ifPresent(species -> tooltip.add(species.displayName().withStyle(ChatFormatting.GRAY)));
        if (bag.stage() == Stage.INOCULATED) {
            tooltip.add(Component.translatable("tooltip.survivalcraft.grain_bag.progress", bag.percent())
                    .withStyle(ChatFormatting.GRAY));
        }
        String hint = switch (bag.stage()) {
            case RAW -> "tooltip.survivalcraft.grain_bag.hint_raw";
            case STERILIZED -> "tooltip.survivalcraft.grain_bag.hint_sterilized";
            case INOCULATED, COLONIZED -> "tooltip.survivalcraft.grain_bag.hint_colonized";
            case CONTAMINATED -> "tooltip.survivalcraft.grain_bag.hint_contaminated";
        };
        tooltip.add(Component.translatable(hint).withStyle(ChatFormatting.DARK_GRAY));
    }
}
