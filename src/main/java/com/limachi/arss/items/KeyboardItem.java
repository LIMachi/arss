package com.limachi.arss.items;

import com.limachi.arss.Arss;
import com.limachi.arss.blocks.KeyboardLecternBlock;
import com.limachi.arss.menu.KeyboardMenu;
import com.limachi.lim_lib.Configs;
import com.limachi.lim_lib.network.IRecordMsg;
import com.limachi.lim_lib.network.NetworkManager;
import com.limachi.lim_lib.network.RegisterMsg;
import com.limachi.lim_lib.registries.ClientRegistries;
import com.limachi.lim_lib.registries.StaticInitClient;
import com.limachi.lim_lib.registries.annotations.RegisterItem;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = Arss.MOD_ID, value = Dist.CLIENT)
public class KeyboardItem extends Item {

    @Configs.Config(min = "2", max = "32", cmt = "how far a keyboard item can transmit redstone signal")
    public static int KEYBOARD_REACH = 6;

    @RegisterItem
    public static RegistryObject<Item> R_ITEM;

    @RegisterMsg
    public record KeyPressVisualFeedbackSlotMsg(int slot, int power) implements IRecordMsg {
        @Override
        public void serverWork(Player player) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof KeyboardItem)
                stack.getOrCreateTag().putInt("output", power);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static int getTint(int index, int power, boolean recording) {
        if (index == 15)
            return RedStoneWireBlock.getColorForPower(recording ? 15 : 0);
        if (index == power - 1)
            return 0xFF00FFFF;
        return -1;
    }

    @OnlyIn(Dist.CLIENT)
    public static int getTint(ItemStack stack, int index) {
        CompoundTag tag = stack.getTag();
        if (tag != null)
            return getTint(index, tag.getInt("output"), inputActive(tag));
        return -1;
    }

    @StaticInitClient
    public static void registerTint() {
        ClientRegistries.setColor(R_ITEM, KeyboardItem::getTint);
    }

    public KeyboardItem() { super(new Properties().stacksTo(1)); }

    public static boolean validBlock(BlockState state) {
        return !(state.getBlock() instanceof RedStoneWireBlock) && state.isSignalSource() && !state.hasBlockEntity() && state.hasProperty(BlockStateProperties.POWER);
    }

    @RegisterMsg
    public record KeyboardItemMsg(BlockPos pos, int power) implements IRecordMsg {
        @Override
        public void serverWork(Player player) {
            BlockState target = player.level().getBlockState(pos);
            if (validBlock(target) && target.getValue(BlockStateProperties.POWER) != power)
                player.level().setBlockAndUpdate(pos, target.setValue(BlockStateProperties.POWER, power));
        }
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> text, @Nonnull TooltipFlag flags) {
        super.appendHoverText(stack, level, text, flags);
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("target") && tag.contains("target_name")) {
            Component name = Component.Serializer.fromJson(tag.getString("target_name"));
            if (name != null) {
                BlockPos at = BlockPos.of(tag.getLong("target"));
                text.add(Component.translatable("tooltip.keyboard_item.link", name, at.getX(), at.getY(), at.getZ()));
            }
        } else
            text.add(Component.translatable("tooltip.keyboard_item.unlinked"));
        Arss.commonHoverText("keyboard_item", text);
    }

    @RegisterMsg
    public record ClearKeyboardTargetMsg() implements IRecordMsg {
        @Override
        public void serverWork(Player player) {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof KeyboardItem))
                return;
            removeTarget(stack.getOrCreateTag());
            player.displayClientMessage(Component.translatable("display.arss.keyboard_item.clear_link"), true);
        }
    }

    @SubscribeEvent
    public static void leftClickAir(PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getEntity().isCrouching() && event.getItemStack().getItem() instanceof KeyboardItem) {
            NetworkManager.toServer(new ClearKeyboardTargetMsg());
        }
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        if (player.isCrouching()) {
            if (!level.isClientSide) {
                ItemStack stack = player.getMainHandItem();
                removeTarget(stack.getOrCreateTag());
                player.displayClientMessage(Component.translatable("display.arss.keyboard_item.clear_link"), true);
            }
        }
        return false;
    }

    @Override
    public boolean mineBlock(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockPos pos, @Nonnull LivingEntity entity) {
        if (entity instanceof Player player && player.isCrouching()) {
            if (!level.isClientSide) {
                removeTarget(stack.getOrCreateTag());
                player.displayClientMessage(Component.translatable("display.arss.keyboard_item.clear_link"), true);
            }
            return true;
        }
        return super.mineBlock(stack, level, state, pos, entity);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        Player player = context.getPlayer();
        CompoundTag tag = stack.getOrCreateTag();
        if (player != null) {
            if (state.is(Blocks.LECTERN) && !state.getValue(LecternBlock.HAS_BOOK)) {
                KeyboardLecternBlock.replaceLectern(context.getLevel(), pos, state, stack.copy());
                if (!player.isCreative())
                    stack.setCount(0);
            } else if (player.isCrouching()) {
                if (validBlock(state) && !matchTargetPos(tag, pos)) {
                    setTarget(tag, pos, state);
                    player.displayClientMessage(Component.translatable("display.arss.keyboard_item.linked_to", state.getBlock().getName()), true);
                    return InteractionResult.SUCCESS;
                }
                KeyboardMenu.open(player, context.getHand());
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.translatable("display.arss.keyboard_item.toggle_keyboard." + toggleInputState(tag)), true);
                return InteractionResult.SUCCESS;
            }
        }
        return super.onItemUseFirst(stack, context);
    }

    public static int[] getBindings(CompoundTag tag) {
        if (!tag.contains("bindings"))
            tag.putIntArray("bindings", Util.make(new int[15], a->{for (int i = 0; i < 15; ++i) a[i] = -1; }));
        return tag.getIntArray("bindings");
    }

    public static void setBinding(CompoundTag tag, int power, int binding) {
        setBindings(tag, Util.make(() -> {
            int[] bindings = getBindings(tag);
            if (power >= 0 && power < 15)
                bindings[power] = binding;
            return bindings;
        }));
    }

    public static void setBindings(CompoundTag tag, int[] bindings) {
        if (bindings.length != 15) {
            final int[] prev = bindings;
            bindings = Util.make(new int[15], b->{for (int i = 0; i < 15; ++i) b[i] = i < prev.length ? prev[i] : -1;});
        }
        tag.putIntArray("bindings", bindings);
    }

    public static boolean inputActive(CompoundTag tag) {
        return tag.getBoolean("catch_keyboard");
    }

    public static void setInputState(CompoundTag tag, boolean active) {
        tag.putBoolean("catch_keyboard", active);
    }

    public static boolean toggleInputState(CompoundTag tag) {
        boolean state = !tag.getBoolean("catch_keyboard");
        tag.putBoolean("catch_keyboard", state);
        return state;
    }

    public static void setTarget(CompoundTag tag, BlockPos pos, Component name) {
        tag.putLong("target", pos.asLong());
        tag.putString("target_name", Component.Serializer.toJson(name));
    }

    public static void setTarget(CompoundTag tag, BlockPos pos, BlockState state) {
        setTarget(tag, pos, state.getBlock().getName());
    }

    public static void removeTarget(CompoundTag tag) {
        tag.remove("target");
        tag.remove("target_name");
    }

    public static boolean matchTargetPos(CompoundTag tag, BlockPos pos) {
        return tag.contains("target") && tag.getLong("target") == pos.asLong();
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, Player player, @Nonnull InteractionHand hand) {
        if (player.isShiftKeyDown())
            KeyboardMenu.open(player, hand);
        else
            player.displayClientMessage(Component.translatable("display.arss.keyboard_item.toggle_keyboard." + toggleInputState(player.getItemInHand(hand).getOrCreateTag())), true);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !(oldStack.is(R_ITEM.get()) && newStack.is(R_ITEM.get()));
    }
}
