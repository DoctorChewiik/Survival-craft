package com.doctorchewiik.survivalcraft.network;

import com.doctorchewiik.survivalcraft.fatigue.Fatigue;
import com.doctorchewiik.survivalcraft.registry.ModAttachments;
import com.doctorchewiik.survivalcraft.temperature.Temperature;
import com.doctorchewiik.survivalcraft.thirst.Thirst;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

// Envoie les statistiques au client après toutes les mises à jour du tick, seulement si elles ont changé
public final class StatsSync {
    public static class State {
        int thirst = -1;
        float temperature = Float.NaN;
        float fatigue = Float.NaN;
    }

    private StatsSync() {
    }

    public static void register(IEventBus bus) {
        bus.addListener(EventPriority.LOWEST, StatsSync::onPlayerTick);
        bus.addListener(StatsSync::onChangedDimension);
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        State state = player.getData(ModAttachments.SYNC_STATE);
        int thirst = Thirst.get(player).getThirst();
        float temperature = Temperature.get(player).getBody();
        float fatigue = Fatigue.get(player).getFatigue();

        boolean changed = thirst != state.thirst
                || Float.isNaN(state.temperature) || Math.abs(temperature - state.temperature) >= 0.05f
                || Float.isNaN(state.fatigue) || Math.abs(fatigue - state.fatigue) >= 0.5f;
        if (changed) {
            state.thirst = thirst;
            state.temperature = temperature;
            state.fatigue = fatigue;
            PacketDistributor.sendToPlayer(player, new StatsSyncPayload(thirst, temperature, fatigue));
        }
    }

    // Le client recrée son joueur en changeant de dimension : on renvoie tout
    private static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        event.getEntity().setData(ModAttachments.SYNC_STATE, new State());
    }
}
