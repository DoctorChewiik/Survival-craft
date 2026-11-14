package com.doctorchewiik.survivalcraft.client;

import java.util.List;

import com.doctorchewiik.survivalcraft.SurvivalCraft;
import com.doctorchewiik.survivalcraft.block.MonotubState;
import com.doctorchewiik.survivalcraft.block.entity.MonotubBlockEntity;
import com.doctorchewiik.survivalcraft.menu.MonotubMenu;
import com.doctorchewiik.survivalcraft.mycology.MushroomSpecies;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/**
 * Écran du monotub avec quatre jauges :
 * humidité, température (la zone verte est la plage idéale de l'espèce), colonisation, prochaine récolte.
 */
public class MonotubScreen extends AbstractContainerScreen<MonotubMenu> {
    private static final ResourceLocation BACKGROUND = SurvivalCraft.id("textures/gui/container/monotub.png");
    private static final int GAUGE_X = 30;
    private static final int GAUGE_WIDTH = 96;
    private static final int GAUGE_HEIGHT = 5;
    private static final int[] ROWS = {18, 38, 58, 78};
    // Échelle de la jauge de température : 0 à 20 sur l'échelle du mod (0 à 40 °C)
    private static final float TEMP_MAX = 20f;

    public MonotubScreen(MonotubMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 200;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("gui.survivalcraft.monotub.empty"),
                        button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, MonotubMenu.BUTTON_EMPTY))
                .bounds(leftPos + 133, topPos + 62, 38, 16)
                .tooltip(Tooltip.create(
                        Component.translatable("gui.survivalcraft.monotub.empty.tooltip")))
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        // Aide sur les cases vides : ce qu'il faut y mettre
        if (menu.getCarried().isEmpty() && hoveredSlot != null && !hoveredSlot.hasItem()
                && hoveredSlot.index < MonotubBlockEntity.FIRST_OUTPUT) {
            String key = switch (hoveredSlot.index) {
                case MonotubBlockEntity.SLOT_BAG -> "gui.survivalcraft.monotub.slot.bag";
                case MonotubBlockEntity.SLOT_SUBSTRATE -> "gui.survivalcraft.monotub.slot.substrate";
                default -> "gui.survivalcraft.monotub.slot.water";
            };
            graphics.renderComponentTooltip(font, List.of(Component.translatable(key)), mouseX, mouseY);
            return;
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        MushroomSpecies species = menu.species();

        // Humidité : bleue, rouge si le bac est détrempé (risque de moisissure)
        float humidity = menu.humidity();
        gauge(graphics, ROWS[0], humidity / 100f, humidity > 97f ? 0xFFD04040 : 0xFF3F8FE0);

        // Température : zone idéale en vert, curseur blanc
        int y = topPos + ROWS[1] + 10;
        int x = leftPos + GAUGE_X;
        frame(graphics, y);
        graphics.fillGradient(x, y, x + GAUGE_WIDTH, y + GAUGE_HEIGHT, 0xFF3F6FD0, 0xFF3F6FD0);
        graphics.fillGradient(x + GAUGE_WIDTH / 2, y, x + GAUGE_WIDTH, y + GAUGE_HEIGHT, 0xFFD06A30, 0xFFD06A30);
        if (species != null) {
            int from = x + Math.round(GAUGE_WIDTH * Mth.clamp(species.idealMin() / TEMP_MAX, 0f, 1f));
            int to = x + Math.round(GAUGE_WIDTH * Mth.clamp(species.idealMax() / TEMP_MAX, 0f, 1f));
            graphics.fill(from, y, to, y + GAUGE_HEIGHT, 0xFF50C850);
        }
        int cursor = x + Math.round((GAUGE_WIDTH - 1) * Mth.clamp(menu.temperature() / TEMP_MAX, 0f, 1f));
        graphics.fill(cursor, y - 1, cursor + 1, y + GAUGE_HEIGHT + 1, 0xFFFFFFFF);

        // Colonisation : blanc mycélium
        gauge(graphics, ROWS[2], menu.colonization() / 100f, 0xFFF0F0F0);

        // Prochaine vague : brun champignon
        gauge(graphics, ROWS[3], menu.state() == MonotubState.FRUITING ? menu.flushProgress() / 100f : 0f, 0xFFB07840);
    }

    private void gauge(GuiGraphics graphics, int row, float fraction, int color) {
        int y = topPos + row + 10;
        int x = leftPos + GAUGE_X;
        frame(graphics, y);
        int width = Math.round(GAUGE_WIDTH * Mth.clamp(fraction, 0f, 1f));
        if (width > 0) {
            graphics.fill(x, y, x + width, y + GAUGE_HEIGHT, color);
        }
    }

    private void frame(GuiGraphics graphics, int y) {
        int x = leftPos + GAUGE_X;
        graphics.fill(x - 1, y - 1, x + GAUGE_WIDTH + 1, y + GAUGE_HEIGHT + 1, 0xFF373737);
        graphics.fill(x, y, x + GAUGE_WIDTH, y + GAUGE_HEIGHT, 0xFF1E1E1E);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);

        MushroomSpecies species = menu.species();
        label(graphics, ROWS[0], Component.translatable("gui.survivalcraft.monotub.humidity",
                Math.round(menu.humidity())));
        Component temperature = species == null
                ? Component.translatable("gui.survivalcraft.monotub.temperature", celsius(menu.temperature()))
                : Component.translatable("gui.survivalcraft.monotub.temperature_ideal", celsius(menu.temperature()),
                        celsius(species.idealMin()), celsius(species.idealMax()));
        label(graphics, ROWS[1], temperature);
        label(graphics, ROWS[2], Component.translatable("gui.survivalcraft.monotub.colonization",
                Math.round(menu.colonization())));
        label(graphics, ROWS[3], Component.translatable("gui.survivalcraft.monotub.flush",
                Math.min(menu.flushes() + 1, MonotubBlockEntity.MAX_FLUSHES), MonotubBlockEntity.MAX_FLUSHES));

        // État du bac et espèce, sous les jauges
        Component state = Component.translatable("gui.survivalcraft.monotub.state." + menu.state().getSerializedName());
        if (species != null) {
            state = Component.empty().append(state).append(" · ").append(species.displayName());
        }
        int color = menu.state() == MonotubState.CONTAMINATED ? 0x2E7D32 : 0x404040;
        graphics.drawString(font, state, 8, 96, color, false);
    }

    private void label(GuiGraphics graphics, int row, Component text) {
        graphics.drawString(font, text, GAUGE_X, row, 0x404040, false);
    }

    // Échelle du mod vers degrés Celsius : 10 = 20 °C
    private static String celsius(float value) {
        return String.valueOf(Math.round(value * 2f));
    }
}
