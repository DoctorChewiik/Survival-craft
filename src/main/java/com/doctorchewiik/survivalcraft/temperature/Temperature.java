package com.doctorchewiik.survivalcraft.temperature;

import com.doctorchewiik.survivalcraft.registry.ModAttachments;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Calcule la température ambiante ressentie par le joueur, sur la même échelle que le corps
 * (0 = gelé, 10 = confortable, 20 = brûlant).
 */
public final class Temperature {
    private static final int HEAT_RADIUS = 3;
    private static final float MAX_HEAT_BONUS = 6f;
    private static final EquipmentSlot[] ARMOR_SLOTS =
            {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private Temperature() {
    }

    public static TemperatureData get(Player player) {
        return player.getData(ModAttachments.TEMPERATURE);
    }

    // Avoir chaud fait boire plus
    public static double thirstMultiplier(float body) {
        if (body >= TemperatureData.HEATSTROKE) {
            return 2.0;
        }
        if (body >= TemperatureData.HOT) {
            return 1.5;
        }
        return 1.0;
    }

    public static float computeAmbient(ServerPlayer player) {
        Level level = player.level();
        BlockPos pos = player.blockPosition();

        float ambient;
        if (level.dimensionType().ultraWarm()) {
            ambient = 18f;
        } else if (!level.dimensionType().hasSkyLight()) {
            // L'End et les dimensions sans ciel : frais et stable
            ambient = 7f;
        } else {
            // Plaine (0.8) = 10, désert (2.0) = 16, toundra (0.0) = 6
            float biome = level.getBiome(pos).value().getBaseTemperature();
            ambient = 10f + (biome - 0.8f) * 5f;

            // Plus froid en altitude
            if (pos.getY() > 90) {
                ambient -= (pos.getY() - 90) / 20f;
            }
            boolean exposed = level.canSeeSky(pos);
            if (!level.isDay()) {
                ambient -= 2f;
            } else if (exposed && ambient > 12f) {
                // Plein soleil dans un biome chaud
                ambient += 1f;
            }
            if (level.isRainingAt(pos.above())) {
                ambient -= 2f;
            }
            // Sous terre, la température se stabilise
            if (!exposed && pos.getY() < 50) {
                ambient = (ambient + 9f) / 2f;
            }
        }

        if (player.isInWater()) {
            ambient -= 3f;
        }
        if (player.isInPowderSnow) {
            ambient -= 4f;
        }
        if (player.isOnFire()) {
            ambient += 5f;
        }
        ambient += heatSources(level, pos);
        ambient += clothing(player, ambient);
        return ambient;
    }

    // Feux, lave, torches... à quelques blocs autour du joueur
    private static float heatSources(Level level, BlockPos center) {
        float heat = 0f;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-HEAT_RADIUS, -1, -HEAT_RADIUS),
                center.offset(HEAT_RADIUS, 2, HEAT_RADIUS))) {
            heat += heatOf(level.getBlockState(pos));
            if (heat >= MAX_HEAT_BONUS) {
                return MAX_HEAT_BONUS;
            }
        }
        return heat;
    }

    private static float heatOf(BlockState state) {
        if (state.is(Blocks.LAVA)) {
            return 4f;
        }
        if (CampfireBlock.isLitCampfire(state)) {
            return 3f;
        }
        if (state.is(BlockTags.FIRE)) {
            return 2f;
        }
        if (state.getBlock() instanceof AbstractFurnaceBlock && state.getValue(AbstractFurnaceBlock.LIT)) {
            return 1.5f;
        }
        if (state.is(Blocks.MAGMA_BLOCK)) {
            return 1f;
        }
        if (state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH) || state.is(Blocks.LANTERN)) {
            return 0.5f;
        }
        return 0f;
    }

    // Le cuir tient chaud ; une armure en métal chauffe en plein soleil
    private static float clothing(Player player, float ambient) {
        float bonus = 0f;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!(stack.getItem() instanceof ArmorItem armor)) {
                continue;
            }
            if (armor.getMaterial().value() == ArmorMaterials.LEATHER.value()) {
                bonus += ambient < 12f ? 1f : 0.5f;
            } else if (ambient > 14f) {
                bonus += 0.5f;
            }
        }
        return bonus;
    }
}
