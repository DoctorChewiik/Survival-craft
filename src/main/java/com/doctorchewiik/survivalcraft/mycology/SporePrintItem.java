package com.doctorchewiik.survivalcraft.mycology;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

// Empreinte de spores : clic droit avec elle sur un sac de grains stérilisé pour l'inoculer
public class SporePrintItem extends Item {
    private final MushroomSpecies species;

    public SporePrintItem(MushroomSpecies species, Properties properties) {
        super(properties);
        this.species = species;
    }

    public MushroomSpecies getSpecies() {
        return species;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.survivalcraft.spore_print.use").withStyle(ChatFormatting.DARK_GRAY));
    }
}
