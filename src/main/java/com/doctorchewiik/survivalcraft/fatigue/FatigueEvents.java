package com.doctorchewiik.survivalcraft.fatigue;

import com.doctorchewiik.survivalcraft.Config;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class FatigueEvents {
    private FatigueEvents() {
    }

    public static void register(IEventBus bus) {
        bus.addListener(FatigueEvents::onPlayerTick);
        bus.addListener(FatigueEvents::onBlockBroken);
        bus.addListener(FatigueEvents::onBreakSpeed);
        bus.addListener(FatigueEvents::onWakeUp);
    }

    private static boolean isActive(Player player) {
        return Config.FATIGUE_ENABLED.get() && !player.isCreative() && !player.isSpectator();
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        FatigueData data = Fatigue.get(player);
        if (!isActive(player)) {
            data.setFatigue(0f);
            return;
        }
        if (player.isSleeping()) {
            // Dormir repose petit à petit, même sans attendre le matin
            data.add(-0.05f);
            return;
        }
        if (player.level().getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }

        float gain = Config.FATIGUE_PER_TICK.get().floatValue();
        if (player.isSprinting() || player.isSwimming()) {
            gain *= 2f;
        }
        data.add(gain);

        // Épuisement : le joueur s'évanouit quelques secondes et récupère un peu
        if (data.getFatigue() >= FatigueData.MAX) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
            data.add(-15f);
        } else if (data.getFatigue() >= FatigueData.EXHAUSTED && player.tickCount % 600 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
        }
    }

    private static void onBlockBroken(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player instanceof ServerPlayer && isActive(player)) {
            Fatigue.get(player).add(Config.FATIGUE_PER_BLOCK_BROKEN.get().floatValue());
        }
    }

    // Fatigué, on mine moins vite (appliqué des deux côtés, la fatigue est synchronisée)
    private static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (isActive(player)) {
            event.setNewSpeed(event.getNewSpeed() * Fatigue.breakSpeedMultiplier(Fatigue.get(player).getFatigue()));
        }
    }

    // Une nuit complète remet la fatigue à zéro
    private static void onWakeUp(PlayerWakeUpEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !event.wakeImmediately()) {
            Fatigue.get(player).setFatigue(0f);
        }
    }
}
