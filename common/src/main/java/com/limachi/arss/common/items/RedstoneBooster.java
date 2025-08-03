package com.limachi.arss.common.items;

import com.limachi.arss.client.ClientDef;
import com.limachi.arss.common.ArssBlockStateProperties;

import com.limachi.lim_lib.common.annotations.RegisterItem;

import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class RedstoneBooster extends Item {

    @RegisterItem
    public static RegistrySupplier<Item> R_ITEM;

    public RedstoneBooster(Properties props) { super(props.durability(16)); }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> components, TooltipFlag flags) {
        super.appendHoverText(stack, ctx, components, flags);
        ClientDef.commonHoverText("redstone_booster", components);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        ItemStack stack = ctx.getItemInHand();
        if (ctx.getLevel() instanceof ServerLevel level) {
            BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
            if (state.hasProperty(ArssBlockStateProperties.BOOSTED) && !state.getValue(ArssBlockStateProperties.BOOSTED)) {
                ctx.getLevel().setBlockAndUpdate(ctx.getClickedPos(), state.setValue(ArssBlockStateProperties.BOOSTED, true));
                if (ctx.getPlayer() instanceof ServerPlayer player) {
                    player.displayClientMessage(Component.translatable("display.arss.redstone_booster.used"), true);
                    if (!player.isCreative())
                        stack.hurtAndBreak(1, level, player, r->{});
                }
            }
            return InteractionResult.SUCCESS;
        } else
            return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        if (player.isShiftKeyDown() && state.hasProperty(ArssBlockStateProperties.BOOSTED) && state.getValue(ArssBlockStateProperties.BOOSTED)) {
            level.setBlockAndUpdate(pos, state.setValue(ArssBlockStateProperties.BOOSTED, false));
            if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
                serverPlayer.displayClientMessage(Component.translatable("display.arss.redstone_booster.reclaimed"), true);
                if (!player.isCreative()) {
                    ItemStack stack = player.getMainHandItem();
                    if (stack.isDamaged())
                        stack.hurtAndBreak(-1, serverLevel, serverPlayer, r->{});
                }
            }
        }
        return false;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (entity instanceof Player player && player.isShiftKeyDown() && state.hasProperty(ArssBlockStateProperties.BOOSTED) && state.getValue(ArssBlockStateProperties.BOOSTED)) {
            level.setBlockAndUpdate(pos, state.setValue(ArssBlockStateProperties.BOOSTED, false));
            if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
                serverPlayer.displayClientMessage(Component.translatable("display.arss.redstone_booster.reclaimed"), true);
                if (!player.isCreative()) {
                    if (stack.isDamaged())
                        stack.hurtAndBreak(-1, serverLevel, serverPlayer, r->{});
                }
            }
            return true;
        }
        return super.mineBlock(stack, level, state, pos, entity);
    }
}
