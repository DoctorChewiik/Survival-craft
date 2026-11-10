package com.doctorchewiik.survivalcraft.item;

import java.util.List;

import com.doctorchewiik.survivalcraft.fatigue.Fatigue;
import com.doctorchewiik.survivalcraft.thirst.Thirst;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

// Bol de café : réduit la fatigue et hydrate un peu. Rend le bol.
public class CoffeeItem extends Item {
    private static final float FATIGUE_RELIEF = 30f;
    private static final int THIRST = 2;

    public CoffeeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            Fatigue.get(player).add(-FATIGUE_RELIEF);
            Thirst.drink(player, THIRST, false);
        }
        if (entity instanceof Player player && player.getAbilities().instabuild) {
            return stack;
        }
        stack.shrink(1);
        if (stack.isEmpty()) {
            return new ItemStack(Items.BOWL);
        }
        if (entity instanceof Player player && !player.getInventory().add(new ItemStack(Items.BOWL))) {
            player.drop(new ItemStack(Items.BOWL), false);
        }
        return stack;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.survivalcraft.coffee").withStyle(ChatFormatting.GRAY));
    }
}
