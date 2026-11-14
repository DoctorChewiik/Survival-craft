package com.doctorchewiik.survivalcraft.item;

import java.util.List;

import com.doctorchewiik.survivalcraft.block.CanteenKind;
import com.doctorchewiik.survivalcraft.block.PlacedCanteenBlock;
import com.doctorchewiik.survivalcraft.block.entity.PlacedCanteenBlockEntity;
import com.doctorchewiik.survivalcraft.registry.ModBlocks;
import com.doctorchewiik.survivalcraft.registry.ModDataComponents;
import com.doctorchewiik.survivalcraft.registry.ModItems;
import com.doctorchewiik.survivalcraft.thirst.Thirst;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Gourde : se remplit dans l'eau (eau sale), se boit gorgée par gorgée.
 * - Les gourdes "bouillables" (métal) se purifient en les utilisant sur un feu de camp allumé.
 * - Toutes se purifient avec une pastille (clic droit avec la pastille sur la gourde dans l'inventaire)
 *   ou dans un filtre à eau.
 * - Accroupi + clic droit sur le sol : la gourde est posée et se remplit sous la pluie.
 */
public class CanteenItem extends Item {
    private static final int SIP_THIRST_CLEAN = 4;
    private static final int SIP_THIRST_DIRTY = 3;
    private static final int COLOR_CLEAN = 0x3FA7FF;
    private static final int COLOR_DIRTY = 0x8B6B3D;

    private final int maxSips;
    private final boolean boilable;
    private final CanteenKind kind;

    public CanteenItem(int maxSips, boolean boilable, CanteenKind kind, Properties properties) {
        super(properties);
        this.maxSips = maxSips;
        this.boilable = boilable;
        this.kind = kind;
    }

    public int getMaxSips() {
        return maxSips;
    }

    public static int getSips(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.WATER_SIPS.get(), 0);
    }

    public static boolean isDirty(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.DIRTY_WATER.get(), false);
    }

    private static void setContent(ItemStack stack, int sips, boolean dirty) {
        if (sips <= 0) {
            stack.remove(ModDataComponents.WATER_SIPS.get());
            stack.remove(ModDataComponents.DIRTY_WATER.get());
        } else {
            stack.set(ModDataComponents.WATER_SIPS.get(), sips);
            stack.set(ModDataComponents.DIRTY_WATER.get(), dirty);
        }
    }

    // Ajoute des gorgées. Un mélange avec de l'eau sale reste sale. Renvoie false si déjà pleine.
    public boolean addSips(ItemStack stack, int amount, boolean dirty) {
        int sips = getSips(stack);
        if (sips >= maxSips) {
            return false;
        }
        setContent(stack, Math.min(maxSips, sips + amount), dirty || (sips > 0 && isDirty(stack)));
        return true;
    }

    // Rend l'eau potable. Renvoie false s'il n'y avait rien à purifier.
    public static boolean purify(ItemStack stack) {
        int sips = getSips(stack);
        if (sips <= 0 || !isDirty(stack)) {
            return false;
        }
        setContent(stack, sips, false);
        return true;
    }

    // Une gourde pleine, utilisée pour l'onglet créatif
    public ItemStack filled(boolean dirty) {
        ItemStack stack = new ItemStack(this);
        setContent(stack, maxSips, dirty);
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int sips = getSips(stack);

        // Viser de l'eau : remplir (l'eau de rivière ou de mer est sale)
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() == HitResult.Type.BLOCK && sips < maxSips) {
            BlockPos pos = hit.getBlockPos();
            if (level.mayInteract(player, pos) && level.getFluidState(pos).is(FluidTags.WATER)) {
                setContent(stack, maxSips, true);
                level.playSound(player, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0f, 1.0f);
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
        }

        // Sinon : boire s'il reste de l'eau
        if (sips > 0) {
            return ItemUtils.startUsingInstantly(level, player, hand);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (context.isSecondaryUseActive()) {
            return place(context);
        }
        if (!boilable || !CampfireBlock.isLitCampfire(state) || !purify(stack)) {
            return InteractionResult.PASS;
        }
        level.playSound(context.getPlayer(), pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0f, 1.0f);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    6, 0.2, 0.1, 0.2, 0.01);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    // Pose la gourde au sol, avec son contenu
    private InteractionResult place(UseOnContext context) {
        BlockPlaceContext placeContext = new BlockPlaceContext(context);
        Level level = context.getLevel();
        BlockPos pos = placeContext.getClickedPos();
        BlockState state = ModBlocks.PLACED_CANTEEN.get().defaultBlockState().setValue(PlacedCanteenBlock.KIND, kind);
        if (!placeContext.canPlace() || !state.canSurvive(level, pos)) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            ItemStack stack = context.getItemInHand();
            level.setBlock(pos, state, 3);
            if (level.getBlockEntity(pos) instanceof PlacedCanteenBlockEntity entity) {
                entity.setCanteen(stack.split(1));
            }
            level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    // Pastille de purification déposée sur la gourde dans l'inventaire (clic droit)
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action,
            Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !other.is(ModItems.PURIFICATION_TABLET.get())
                || !slot.allowModification(player) || stack.getCount() != 1) {
            return false;
        }
        if (purify(stack)) {
            other.shrink(1);
            player.playSound(SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, 0.8f, 1.2f);
        }
        return true;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        int sips = getSips(stack);
        if (sips <= 0) {
            return stack;
        }
        boolean dirty = isDirty(stack);
        if (entity instanceof ServerPlayer player) {
            Thirst.drink(player, dirty ? SIP_THIRST_DIRTY : SIP_THIRST_CLEAN, dirty);
        }
        if (!(entity instanceof Player player) || !player.getAbilities().instabuild) {
            setContent(stack, sips - 1, dirty);
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

    // La barre sous l'item montre le niveau d'eau : bleue si potable, brune si sale
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getSips(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Mth.clamp(Math.round(13.0f * getSips(stack) / maxSips), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return isDirty(stack) ? COLOR_DIRTY : COLOR_CLEAN;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int sips = getSips(stack);
        tooltip.add(Component.translatable("tooltip.survivalcraft.canteen.sips", sips, maxSips)
                .withStyle(ChatFormatting.GRAY));
        if (sips > 0) {
            tooltip.add(isDirty(stack)
                    ? Component.translatable("tooltip.survivalcraft.canteen.dirty").withStyle(ChatFormatting.GOLD)
                    : Component.translatable("tooltip.survivalcraft.canteen.clean").withStyle(ChatFormatting.AQUA));
        }
        if (boilable) {
            tooltip.add(Component.translatable("tooltip.survivalcraft.canteen.boilable").withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(Component.translatable("tooltip.survivalcraft.canteen.place").withStyle(ChatFormatting.DARK_GRAY));
    }
}
