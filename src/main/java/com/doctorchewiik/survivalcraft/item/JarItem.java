package com.doctorchewiik.survivalcraft.item;

import java.util.List;

import com.doctorchewiik.survivalcraft.registry.ModDataComponents;
import com.doctorchewiik.survivalcraft.thirst.HydrationValues;
import com.doctorchewiik.survivalcraft.thirst.Thirst;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Bocal hermétique. Dans l'inventaire, il se remplit comme un sac : clic droit avec un aliment sur le bocal
 * pour le ranger, clic droit main vide pour tout reprendre. Sur un feu de camp, il est stérilisé et scellé.
 * Clic droit dans le vide pour manger un aliment du bocal. Accroupi + clic droit pour le poser.
 */
public class JarItem extends BlockItem {
    public JarItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static JarContents getContents(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.JAR_CONTENTS.get(), JarContents.EMPTY);
    }

    private static void setContents(ItemStack stack, JarContents contents) {
        if (contents.isEmpty()) {
            stack.remove(ModDataComponents.JAR_CONTENTS.get());
        } else {
            stack.set(ModDataComponents.JAR_CONTENTS.get(), contents);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        JarContents contents = getContents(stack);

        // Stériliser : bocal plein, non scellé, sur un feu de camp allumé
        if (CampfireBlock.isLitCampfire(level.getBlockState(pos)) && !contents.isEmpty() && !contents.sealed()
                && stack.getCount() == 1) {
            setContents(stack, contents.seal());
            level.playSound(context.getPlayer(), pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        6, 0.2, 0.1, 0.2, 0.01);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (context.isSecondaryUseActive()) {
            return super.useOn(context);
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        JarContents contents = getContents(stack);
        FoodProperties food = contents.food().get(DataComponents.FOOD);
        if (stack.getCount() == 1 && food != null && player.canEat(food.canAlwaysEat())) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return getContents(stack).isEmpty() ? UseAnim.NONE : UseAnim.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        JarContents contents = getContents(stack);
        return contents.isEmpty() ? 0 : contents.food().getUseDuration(entity);
    }

    // On mange un aliment du bocal ; l'ouvrir casse le joint
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        JarContents contents = getContents(stack);
        if (contents.isEmpty()) {
            return stack;
        }
        ItemStack bite = contents.food().copyWithCount(1);
        ItemStack leftover = bite.finishUsingItem(level, entity);
        if (entity instanceof ServerPlayer player) {
            HydrationValues.Hydration hydration = HydrationValues.get(contents.food());
            if (hydration != null) {
                Thirst.drink(player, hydration.amount(), hydration.dirty());
            }
            // Bol de ragoût, etc.
            if (!leftover.isEmpty()) {
                ItemHandlerHelper.giveItemToPlayer(player, leftover);
            }
        }
        setContents(stack, contents.withRemoved(1));
        return stack;
    }

    // Bocal tenu par la souris, posé sur un aliment : on range l'aliment
    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY || stack.getCount() != 1) {
            return false;
        }
        ItemStack target = slot.getItem();
        JarContents contents = getContents(stack);
        int amount = Math.min(contents.spaceFor(target), target.getCount());
        if (amount <= 0) {
            return false;
        }
        ItemStack taken = slot.safeTake(amount, amount, player);
        setContents(stack, contents.withAdded(taken, taken.getCount()));
        player.playSound(SoundEvents.BUNDLE_INSERT, 0.8f, 1.0f);
        return true;
    }

    // Aliment tenu par la souris, posé sur le bocal : on le range. Main vide : on vide le bocal.
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action,
            Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player) || stack.getCount() != 1) {
            return false;
        }
        JarContents contents = getContents(stack);
        if (other.isEmpty()) {
            if (contents.isEmpty()) {
                return false;
            }
            access.set(contents.food().copy());
            setContents(stack, JarContents.EMPTY);
            player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8f, 1.0f);
            return true;
        }
        int amount = Math.min(contents.spaceFor(other), other.getCount());
        if (amount <= 0) {
            return false;
        }
        setContents(stack, contents.withAdded(other, amount));
        other.shrink(amount);
        player.playSound(SoundEvents.BUNDLE_INSERT, 0.8f, 1.0f);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        JarContents contents = getContents(stack);
        if (contents.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.survivalcraft.jar.empty").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.survivalcraft.jar.contents",
                    contents.count(), JarContents.CAPACITY, contents.food().getHoverName()).withStyle(ChatFormatting.GRAY));
            tooltip.add(contents.sealed()
                    ? Component.translatable("tooltip.survivalcraft.jar.sealed").withStyle(ChatFormatting.GREEN)
                    : Component.translatable("tooltip.survivalcraft.jar.open").withStyle(ChatFormatting.GOLD));
        }
        tooltip.add(Component.translatable("tooltip.survivalcraft.jar.help").withStyle(ChatFormatting.DARK_GRAY));
    }
}
