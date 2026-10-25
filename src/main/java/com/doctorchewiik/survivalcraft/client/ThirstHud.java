package com.doctorchewiik.survivalcraft.client;

import com.doctorchewiik.survivalcraft.Config;
import com.doctorchewiik.survivalcraft.SurvivalCraft;
import com.doctorchewiik.survivalcraft.thirst.Thirst;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;

// Barre de 10 gouttes, alignée à droite comme la barre de faim
public final class ThirstHud {
    private static final ResourceLocation EMPTY = SurvivalCraft.id("hud/thirst_empty");
    private static final ResourceLocation HALF = SurvivalCraft.id("hud/thirst_half");
    private static final ResourceLocation FULL = SurvivalCraft.id("hud/thirst_full");

    private ThirstHud() {
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.gameMode == null || !minecraft.gameMode.canHurtPlayer()
                || !Config.THIRST_ENABLED.get()) {
            return;
        }

        int thirst = Thirst.get(player).getThirst();
        int right = graphics.guiWidth() / 2 + 91;
        int y = graphics.guiHeight() - minecraft.gui.rightHeight;

        for (int i = 0; i < 10; i++) {
            int x = right - i * 8 - 9;
            graphics.blitSprite(EMPTY, x, y, 9, 9);
            if (i * 2 + 1 < thirst) {
                graphics.blitSprite(FULL, x, y, 9, 9);
            } else if (i * 2 + 1 == thirst) {
                graphics.blitSprite(HALF, x, y, 9, 9);
            }
        }
        minecraft.gui.rightHeight += 10;
    }
}
