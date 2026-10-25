package com.doctorchewiik.survivalcraft.command;

import com.doctorchewiik.survivalcraft.thirst.Thirst;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

// Commandes de test : /survivalcraft thirst <0-20>
public final class ModCommands {
    private ModCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("survivalcraft")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("thirst")
                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 20))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int value = IntegerArgumentType.getInteger(context, "value");
                                    Thirst.get(player).setThirst(value);
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("commands.survivalcraft.thirst.set", value), false);
                                    return value;
                                }))));
    }
}
