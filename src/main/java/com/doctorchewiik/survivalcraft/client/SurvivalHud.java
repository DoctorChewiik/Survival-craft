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
 * - gouttes de soif juste au-dessus de la barre de faim (à droite) ;
 * - lunes d'énergie juste au-dessus de la barre de vie (à gauche) ;
 * - thermomètre et température en °C en bas à gauche de l'écran ;
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
    private static final int[] TEXT_COLORS = {0xFF4696FF, 0xFF82C8FF, 0xFFFFFFFF, 0xFFFFA028, 0xFFE6281E};

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
        drawRightRow(graphics, Thirst.get(player).getThirst(), THIRST_EMPTY, THIRST_HALF, THIRST_FULL);
    }

    public static void renderEnergy(GuiGraphics graphics, DeltaTracker deltaTracker) {
        LocalPlayer player = survivalPlayer();
        if (player == null || !Config.FATIGUE_ENABLED.get()) {
            return;
        }
        // 20 demi-lunes d'énergie = 100 de fatigue en moins
        int energy = Mth.ceil((FatigueData.MAX - Fatigue.get(player).getFatigue()) / 5f);
        drawLeftRow(graphics, energy, ENERGY_EMPTY, ENERGY_HALF, ENERGY_FULL);
    }

    // Rangée de 10 icônes alignée à droite (comme la faim), puis on remonte la pile de droite
    private static void drawRightRow(GuiGraphics graphics, int value, ResourceLocation empty, ResourceLocation half,
            ResourceLocation full) {
        Minecraft minecraft = Minecraft.getInstance();
        int right = graphics.guiWidth() / 2 + 91;
        int y = graphics.guiHeight() - minecraft.gui.rightHeight;
        for (int i = 0; i < 10; i++) {
            drawIcon(graphics, right - i * 8 - 9, y, i, value, empty, half, full);
        }
        minecraft.gui.rightHeight += 10;
    }

    // Rangée de 10 icônes alignée à gauche (comme la vie), puis on remonte la pile de gauche
    private static void drawLeftRow(GuiGraphics graphics, int value, ResourceLocation empty, ResourceLocation half,
            ResourceLocation full) {
        Minecraft minecraft = Minecraft.getInstance();
        int left = graphics.guiWidth() / 2 - 91;
        int y = graphics.guiHeight() - minecraft.gui.leftHeight;
        for (int i = 0; i < 10; i++) {
            drawIcon(graphics, left + i * 8, y, i, value, empty, half, full);
        }
        minecraft.gui.leftHeight += 10;
    }

    private static void drawIcon(GuiGraphics graphics, int x, int y, int index, int value, ResourceLocation empty,
            ResourceLocation half, ResourceLocation full) {
        graphics.blitSprite(empty, x, y, 9, 9);
        if (index * 2 + 1 < value) {
            graphics.blitSprite(full, x, y, 9, 9);
        } else if (index * 2 + 1 == value) {
            graphics.blitSprite(half, x, y, 9, 9);
        }
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
        // En bas à gauche : thermomètre agrandi et température en °C (10 sur l'échelle = 37 °C)
        int x = 4;
        int y = graphics.guiHeight() - 22;
        graphics.blitSprite(THERMOMETER[level], x, y, 18, 18);
        float celsius = 33f + body * 0.4f;
        String text = String.format("%.1f °C", celsius);
        graphics.drawString(Minecraft.getInstance().font, text, x + 21, y + 5, TEXT_COLORS[level]);
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
