package com.doctorchewiik.survivalcraft.thirst;

import com.doctorchewiik.survivalcraft.Config;
import com.doctorchewiik.survivalcraft.SurvivalCraft;
import com.doctorchewiik.survivalcraft.temperature.Temperature;
import com.doctorchewiik.survivalcraft.util.Modifiers;
import com.doctorchewiik.survivalcraft.util.SurvivalDamage;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class ThirstEvents {
    private static final ResourceLocation SLOWNESS_ID = SurvivalCraft.id("thirst_slowness");
    private static final AttributeModifier SLOWNESS =
            new AttributeModifier(SLOWNESS_ID, -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    private ThirstEvents() {
    }

    public static void register(IEventBus bus) {
        bus.addListener(ThirstEvents::onPlayerTick);
        bus.addListener(ThirstEvents::onItemUseFinish);
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ThirstData data = Thirst.get(player);
        boolean active = Config.THIRST_ENABLED.get() && !player.isCreative() && !player.isSpectator();

        if (active) {
            if (player.level().getDifficulty() == Difficulty.PEACEFUL) {
                // Comme la faim : en paisible, la soif remonte toute seule
                if (player.tickCount % 40 == 0) {
                    data.drink(1);
                }
            } else {
                data.addExhaustion(computeDrain(player));
                tickDehydration(player, data);
            }
        }

        Modifiers.setSpeedModifier(player, SLOWNESS,
                active && data.getThirst() <= Config.THIRST_SLOWNESS_THRESHOLD.get());
    }

    private static float computeDrain(ServerPlayer player) {
        double drain = Config.THIRST_BASE_DRAIN.get();
        if (player.isSprinting()) {
            drain *= Config.THIRST_SPRINT_MULTIPLIER.get();
        }
        if (player.level().dimensionType().ultraWarm()) {
            drain *= Config.THIRST_NETHER_MULTIPLIER.get();
        }
        if (Config.TEMPERATURE_ENABLED.get()) {
            // Avoir chaud donne soif
            drain *= Temperature.thirstMultiplier(Temperature.get(player).getBody());
        } else if (player.level().getBiome(player.blockPosition()).value().getBaseTemperature() > 1.0f) {
            drain *= Config.THIRST_HOT_BIOME_MULTIPLIER.get();
        }
        return (float) drain;
    }

    private static void tickDehydration(ServerPlayer player, ThirstData data) {
        if (data.getThirst() > 0) {
            data.damageTimer = 0;
            return;
        }
        if (++data.damageTimer >= Config.THIRST_DAMAGE_INTERVAL.get()) {
            data.damageTimer = 0;
            SurvivalDamage.hurt(player, SurvivalDamage.DEHYDRATION);
        }
    }

    // Boissons et aliments vanilla qui hydratent
    private static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player && Config.THIRST_ENABLED.get()) {
            HydrationValues.Hydration hydration = HydrationValues.get(event.getItem());
            if (hydration != null) {
                Thirst.drink(player, hydration.amount(), hydration.dirty());
            }
        }
    }
}
