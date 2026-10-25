package com.doctorchewiik.survivalcraft.network;

import com.doctorchewiik.survivalcraft.thirst.Thirst;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";

    private ModNetworking() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        // Côté client, context.player() est le joueur local : pas besoin de classes client ici
        registrar.playToClient(ThirstSyncPayload.TYPE, ThirstSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> Thirst.get(context.player()).setThirst(payload.thirst())));
    }
}
