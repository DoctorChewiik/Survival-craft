package com.doctorchewiik.survivalcraft.command;

import com.doctorchewiik.survivalcraft.fatigue.Fatigue;
import com.doctorchewiik.survivalcraft.temperature.Temperature;
import com.doctorchewiik.survivalcraft.thirst.Thirst;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Commandes de test :
 * /survivalcraft thirst <0-20>
 * /survivalcraft temperature <0-20>   (10 = normal)
 * /survivalcraft fatigue <0-100>
 * /survivalcraft status
 */
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
                                })))
                .then(Commands.literal("temperature")
                        .then(Commands.argument("value", FloatArgumentType.floatArg(0, 20))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    float value = FloatArgumentType.getFloat(context, "value");
                                    Temperature.get(player).setBody(value);
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("commands.survivalcraft.temperature.set", value), false);
                                    return 1;
                                })))
                .then(Commands.literal("fatigue")
                        .then(Commands.argument("value", FloatArgumentType.floatArg(0, 100))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    float value = FloatArgumentType.getFloat(context, "value");
                                    Fatigue.get(player).setFatigue(value);
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("commands.survivalcraft.fatigue.set", value), false);
                                    return 1;
                                })))
                .then(Commands.literal("status")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            Component status = Component.translatable("commands.survivalcraft.status",
                                    Thirst.get(player).getThirst(),
                                    String.format("%.1f", Temperature.get(player).getBody()),
                                    String.format("%.1f", Temperature.get(player).getTarget()),
                                    String.format("%.0f", Fatigue.get(player).getFatigue()));
                            context.getSource().sendSuccess(() -> status, false);
                            return 1;
                        })));
    }
}
