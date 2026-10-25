package com.doctorchewiik.survivalcraft.network;

import com.doctorchewiik.survivalcraft.fatigue.Fatigue;
import com.doctorchewiik.survivalcraft.temperature.Temperature;
import com.doctorchewiik.survivalcraft.thirst.Thirst;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {
    private static final String PROTOCOL_VERSION = "2";

    private ModNetworking() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        // Côté client, context.player() est le joueur local : pas besoin de classes client ici
        registrar.playToClient(StatsSyncPayload.TYPE, StatsSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    Player player = context.player();
                    Thirst.get(player).setThirst(payload.thirst());
                    Temperature.get(player).setBody(payload.temperature());
                    Fatigue.get(player).setFatigue(payload.fatigue());
                }));
    }
}
