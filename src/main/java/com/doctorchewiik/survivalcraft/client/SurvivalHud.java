package com.doctorchewiik.survivalcraft.client;

import com.doctorchewiik.survivalcraft.Config;
import com.doctorchewiik.survivalcraft.SurvivalCraft;
import com.doctorchewiik.survivalcraft.fatigue.Fatigue;
import com.doctorchewiik.survivalcraft.fatigue.FatigueData;
import com.doctorchewiik.survivalcraft.temperature.Temperature;
import com.doctorchewiik.survivalcraft.temperature.TemperatureData;
import com.doctorchewiik.survivalcraft.thirst.Thirst;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Éléments du HUD :
 * - gouttes de soif et lunes d'énergie, empilées à droite au-dessus de la faim ;
 * - thermomètre entre la vie et la faim ;
 * - voile sombre quand le joueur est très fatigué.
 */
public final class SurvivalHud {
    private static final ResourceLocation THIRST_EMPTY = SurvivalCraft.id("hud/thirst_empty");
    private static final ResourceLocation THIRST_HALF = SurvivalCraft.id("hud/thirst_half");
    private static final ResourceLocation THIRST_FULL = SurvivalCraft.id("hud/thirst_full");
    private static final ResourceLocation ENERGY_EMPTY = SurvivalCraft.id("hud/energy_empty");
    private static final ResourceLocation ENERGY_HALF = SurvivalCraft.id("hud/energy_half");
    private static final ResourceLocation ENERGY_FULL = SurvivalCraft.id("hud/energy_full");
    private static final ResourceLocation[] THERMOMETER = {
            SurvivalCraft.id("hud/thermometer_freezing"),
            SurvivalCraft.id("hud/thermometer_cold"),
            SurvivalCraft.id("hud/thermometer_normal"),
            SurvivalCraft.id("hud/thermometer_hot"),
            SurvivalCraft.id("hud/thermometer_burning")};

    private SurvivalHud() {
    }

    private static LocalPlayer survivalPlayer() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.gameMode == null || !minecraft.gameMode.canHurtPlayer()) {
            return null;
        }
        return minecraft.player;
    }

    public static void renderThirst(GuiGraphics graphics, DeltaTracker deltaTracker) {
        LocalPlayer player = survivalPlayer();
        if (player == null || !Config.THIRST_ENABLED.get()) {
            return;
        }
        drawRow(graphics, Thirst.get(player).getThirst(), THIRST_EMPTY, THIRST_HALF, THIRST_FULL);
    }

    public static void renderEnergy(GuiGraphics graphics, DeltaTracker deltaTracker) {
        LocalPlayer player = survivalPlayer();
        if (player == null || !Config.FATIGUE_ENABLED.get()) {
            return;
        }
        // 20 demi-lunes d'énergie = 100 de fatigue en moins
        int energy = Mth.ceil((FatigueData.MAX - Fatigue.get(player).getFatigue()) / 5f);
        drawRow(graphics, energy, ENERGY_EMPTY, ENERGY_HALF, ENERGY_FULL);
    }

    // Une rangée de 10 icônes alignée à droite, comme la faim, puis on remonte la pile
    private static void drawRow(GuiGraphics graphics, int value, ResourceLocation empty, ResourceLocation half,
            ResourceLocation full) {
        Minecraft minecraft = Minecraft.getInstance();
        int right = graphics.guiWidth() / 2 + 91;
        int y = graphics.guiHeight() - minecraft.gui.rightHeight;
        for (int i = 0; i < 10; i++) {
            int x = right - i * 8 - 9;
            graphics.blitSprite(empty, x, y, 9, 9);
            if (i * 2 + 1 < value) {
                graphics.blitSprite(full, x, y, 9, 9);
            } else if (i * 2 + 1 == value) {
                graphics.blitSprite(half, x, y, 9, 9);
            }
        }
        minecraft.gui.rightHeight += 10;
    }

    public static void renderThermometer(GuiGraphics graphics, DeltaTracker deltaTracker) {
        LocalPlayer player = survivalPlayer();
        if (player == null || !Config.TEMPERATURE_ENABLED.get()) {
            return;
        }
        float body = Temperature.get(player).getBody();
        int level;
        if (body <= TemperatureData.HYPOTHERMIA) {
            level = 0;
        } else if (body <= TemperatureData.COLD) {
            level = 1;
        } else if (body < TemperatureData.HOT) {
            level = 2;
        } else if (body < TemperatureData.HEATSTROKE) {
            level = 3;
        } else {
            level = 4;
        }
        int x = graphics.guiWidth() / 2 - 4;
        int y = graphics.guiHeight() - 39;
        graphics.blitSprite(THERMOMETER[level], x, y, 9, 9);
    }

    // Voile sombre qui pulse quand on tombe de sommeil
    public static void renderFatigueOverlay(GuiGraphics graphics, DeltaTracker deltaTracker) {
        LocalPlayer player = survivalPlayer();
        if (player == null || !Config.FATIGUE_ENABLED.get()) {
            return;
        }
        float fatigue = Fatigue.get(player).getFatigue();
        if (fatigue < FatigueData.VERY_TIRED) {
            return;
        }
        float strength = (fatigue - FatigueData.VERY_TIRED) / (FatigueData.MAX - FatigueData.VERY_TIRED);
        float time = player.tickCount + deltaTracker.getGameTimeDeltaPartialTick(true);
        float pulse = 0.5f + 0.5f * Mth.sin(time * 0.05f);
        int alpha = (int) (strength * (0.25f + 0.35f * pulse) * 255);
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), alpha << 24);
    }
}
