package com.doctorchewiik.survivalcraft.primitive;

import com.doctorchewiik.survivalcraft.Config;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

// Début de partie primitif : il faut une hache (même en silex) pour couper du bois.
public final class PrimitiveEvents {
    private PrimitiveEvents() {
    }

    public static void register(IEventBus bus) {
        bus.addListener(PrimitiveEvents::onBreakSpeed);
    }

    private static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (!Config.REQUIRE_AXE_FOR_LOGS.get() || player.isCreative() || !event.getState().is(BlockTags.LOGS)) {
            return;
        }
        if (!player.getMainHandItem().isCorrectToolForDrops(event.getState())) {
            event.setNewSpeed(0f);
            if (player.level().isClientSide() && player.tickCount % 40 == 0) {
                player.displayClientMessage(Component.translatable("message.survivalcraft.need_axe"), true);
            }
        }
    }
}
