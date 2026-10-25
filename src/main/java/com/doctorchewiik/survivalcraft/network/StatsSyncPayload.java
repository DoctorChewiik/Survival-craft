package com.doctorchewiik.survivalcraft.network;

import com.doctorchewiik.survivalcraft.SurvivalCraft;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

// Envoyé au client quand la soif, la température ou la fatigue changent, pour le HUD
public record StatsSyncPayload(int thirst, float temperature, float fatigue) implements CustomPacketPayload {
    public static final Type<StatsSyncPayload> TYPE = new Type<>(SurvivalCraft.id("stats_sync"));

    public static final StreamCodec<ByteBuf, StatsSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, StatsSyncPayload::thirst,
            ByteBufCodecs.FLOAT, StatsSyncPayload::temperature,
            ByteBufCodecs.FLOAT, StatsSyncPayload::fatigue,
            StatsSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
