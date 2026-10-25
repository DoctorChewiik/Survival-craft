package com.doctorchewiik.survivalcraft.network;

import com.doctorchewiik.survivalcraft.SurvivalCraft;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

// Envoyé au client à chaque changement de soif, pour l'affichage du HUD
public record ThirstSyncPayload(int thirst) implements CustomPacketPayload {
    public static final Type<ThirstSyncPayload> TYPE = new Type<>(SurvivalCraft.id("thirst_sync"));

    public static final StreamCodec<ByteBuf, ThirstSyncPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, ThirstSyncPayload::thirst, ThirstSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
