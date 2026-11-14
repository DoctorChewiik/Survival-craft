package com.doctorchewiik.survivalcraft.temperature;

import com.doctorchewiik.survivalcraft.Config;
import com.doctorchewiik.survivalcraft.SurvivalCraft;
import com.doctorchewiik.survivalcraft.util.Modifiers;
import com.doctorchewiik.survivalcraft.util.SurvivalDamage;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class TemperatureEvents {
    private static final AttributeModifier COLD_SLOWNESS = new AttributeModifier(
            SurvivalCraft.id("hypothermia_slowness"), -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    private TemperatureEvents() {
    }

    public static void register(IEventBus bus) {
        bus.addListener(TemperatureEvents::onPlayerTick);
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        TemperatureData data = Temperature.get(player);
        boolean active = Config.TEMPERATURE_ENABLED.get() && !player.isCreative() && !player.isSpectator();
        if (!active) {
            data.setBody(TemperatureData.NORMAL);
            Modifiers.setSpeedModifier(player, COLD_SLOWNESS, false);
            return;
        }

        // L'environnement est recalculé chaque seconde (le scan des sources de chaleur coûte cher)
        if (player.tickCount % 20 == 0) {
            data.target = Temperature.computeAmbient(player);
        }
        float body = data.getBody();
        data.setBody(body + (data.target - body) * Config.TEMPERATURE_ADAPT_RATE.get().floatValue());
        body = data.getBody();

        boolean hypothermia = body <= TemperatureData.HYPOTHERMIA;
        boolean heatstroke = body >= TemperatureData.HEATSTROKE;
        Modifiers.setSpeedModifier(player, COLD_SLOWNESS, hypothermia);

        if (hypothermia) {
            // Givre sur l'écran, sans atteindre le gel complet vanilla
            int frost = (int) (player.getTicksRequiredToFreeze() * 0.9f);
            player.setTicksFrozen(Math.max(player.getTicksFrozen(), frost));
        }

        if (!hypothermia && !heatstroke) {
            data.damageTimer = 0;
            return;
        }
        if (++data.damageTimer >= Config.TEMPERATURE_DAMAGE_INTERVAL.get()) {
            data.damageTimer = 0;
            if (hypothermia) {
                SurvivalDamage.hurt(player, SurvivalDamage.HYPOTHERMIA);
            } else {
                SurvivalDamage.hurt(player, SurvivalDamage.HEATSTROKE);
                if (player.getRandom().nextInt(4) == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
                }
            }
        }
    }
}
