package com.doctorchewiik.survivalcraft.thirst;

import com.doctorchewiik.survivalcraft.Config;
import com.doctorchewiik.survivalcraft.SurvivalCraft;
import com.doctorchewiik.survivalcraft.network.ThirstSyncPayload;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ThirstEvents {
    private static final ResourceLocation SLOWNESS_ID = SurvivalCraft.id("thirst_slowness");
    private static final AttributeModifier SLOWNESS =
            new AttributeModifier(SLOWNESS_ID, -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    private ThirstEvents() {
    }

    public static void register(IEventBus bus) {
        bus.addListener(ThirstEvents::onPlayerTick);
        bus.addListener(ThirstEvents::onChangedDimension);
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

        updateSlowness(player, active && data.getThirst() <= Config.THIRST_SLOWNESS_THRESHOLD.get());

        if (data.getThirst() != data.lastSynced) {
            data.lastSynced = data.getThirst();
            PacketDistributor.sendToPlayer(player, new ThirstSyncPayload(data.getThirst()));
        }
    }

    private static float computeDrain(ServerPlayer player) {
        double drain = Config.THIRST_BASE_DRAIN.get();
        if (player.isSprinting()) {
            drain *= Config.THIRST_SPRINT_MULTIPLIER.get();
        }
        if (player.level().dimensionType().ultraWarm()) {
            drain *= Config.THIRST_NETHER_MULTIPLIER.get();
        } else if (player.level().getBiome(player.blockPosition()).value().getBaseTemperature() > 1.0f) {
            drain *= Config.THIRST_HOT_BIOME_MULTIPLIER.get();
        }
        return (float) drain;
    }

    // Dégâts à soif nulle, avec les mêmes limites que la famine vanilla selon la difficulté
    private static void tickDehydration(ServerPlayer player, ThirstData data) {
        if (data.getThirst() > 0) {
            data.damageTimer = 0;
            return;
        }
        if (++data.damageTimer < Config.THIRST_DAMAGE_INTERVAL.get()) {
            return;
        }
        data.damageTimer = 0;
        Difficulty difficulty = player.level().getDifficulty();
        boolean canHurt = player.getHealth() > 10f
                || difficulty == Difficulty.HARD
                || (player.getHealth() > 1f && difficulty == Difficulty.NORMAL);
        if (canHurt) {
            player.hurt(Thirst.dehydrationSource(player), 1f);
        }
    }

    private static void updateSlowness(ServerPlayer player, boolean slowed) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        if (slowed && !speed.hasModifier(SLOWNESS_ID)) {
            speed.addTransientModifier(SLOWNESS);
        } else if (!slowed && speed.hasModifier(SLOWNESS_ID)) {
            speed.removeModifier(SLOWNESS_ID);
        }
    }

    // Le client recrée son joueur en changeant de dimension : on renvoie la soif
    private static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Thirst.get(event.getEntity()).markDirty();
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
