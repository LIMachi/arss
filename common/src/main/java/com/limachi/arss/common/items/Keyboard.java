package com.limachi.arss.common.items;

import com.limachi.arss.client.ClientDef;
import com.limachi.arss.common.blocks.KeyboardLectern;
import com.limachi.arss.common.menus.KeyboardMenu;
import com.limachi.arss.utils.client.ClientEvents;
import com.limachi.arss.utils.IItemMixin;
import com.limachi.arss.utils.client.annotations.RegisterClientEventListener;
import com.limachi.arss.utils.network.IC2SMsg;
import com.limachi.arss.utils.annotations.*;
import com.limachi.arss.utils.client.annotations.ItemTinter;

import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.registries.RegistrySupplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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

import static com.limachi.arss.common.ArssItemStackComponents.*;

import java.util.List;

@SuppressWarnings("unused")
public class Keyboard extends Item implements IItemMixin {

    @Environment(EnvType.CLIENT)
    @RegisterClientEventListener(ClientEvents.LEFT_CLICK_AIR)
    public static void leftClickAir(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown() && player.getItemInHand(hand).getItem() instanceof Keyboard)
            new ClearKeyboardTargetMsg().sendToServer();
    }

    @Config(min = "2", max = "32", cmt = "how far a keyboard item can transmit redstone signal", reload = true, path = "RemoteKeyboard", name = "MaximumReach")
    public static int KEYBOARD_REACH = 6;

    @RegisterItem
    public static RegistrySupplier<Item> R_ITEM;

    @RegisterMsg
    public record KeyPressVisualFeedbackSlotMsg(int slot, int power) implements IC2SMsg<KeyPressVisualFeedbackSlotMsg> {
        @Override
        public void run(NetworkManager.PacketContext ctx) {
            ItemStack stack = ctx.getPlayer().getInventory().getItem(slot);
            if (stack.getItem() instanceof Keyboard)
                stack.set(OUTPUT.get(), power);
        }
    }

    public static int getTint(int index, int power, boolean recording) {
        if (index == 15)
            return 0xFF000000 | RedStoneWireBlock.getColorForPower(recording ? 15 : 0);
        if (index == power - 1)
            return 0xFF00FFFF;
        return -1;
    }

    @ItemTinter
    public static int getTint(ItemStack stack, int index) {
        return getTint(index, stack.getOrDefault(OUTPUT.get(), 0), stack.getOrDefault(CATCH.get(), false));
    }

    public Keyboard(Properties props) { super(props.stacksTo(1).component(OUTPUT.get(), 0).component(CATCH.get(), false).component(TARGET.get(), NamedPos.UNSET)); }

    public static boolean validBlock(BlockState state) {
        return !(state.getBlock() instanceof RedStoneWireBlock) && state.isSignalSource() && !state.hasBlockEntity() && state.hasProperty(BlockStateProperties.POWER);
    }

    @RegisterMsg
    public record KeyboardItemMsg(BlockPos pos, int power) implements IC2SMsg<KeyboardItemMsg> {
        @Override
        public void run(NetworkManager.PacketContext ctx) {
            BlockState target = ctx.getPlayer().level().getBlockState(pos);
            if (validBlock(target) && target.getValue(BlockStateProperties.POWER) != power)
                ctx.getPlayer().level().setBlockAndUpdate(pos, target.setValue(BlockStateProperties.POWER, power));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> text, TooltipFlag flags) {
        super.appendHoverText(stack, ctx, text, flags);
        NamedPos target = stack.get(TARGET.get());
        if (target != null && target.set()) {
            Component name = Component.Serializer.fromJson(target.name(), ctx.registries());
            if (name != null)
                text.add(Component.translatable("tooltip.keyboard_item.link", name, target.pos().getX(), target.pos().getY(), target.pos().getZ()));
        } else
            text.add(Component.translatable("tooltip.keyboard_item.unlinked"));
        ClientDef.commonHoverText("keyboard_item", text);
    }

    @RegisterMsg
    public record ClearKeyboardTargetMsg() implements IC2SMsg<ClearKeyboardTargetMsg> {
        @Override
        public void run(NetworkManager.PacketContext ctx) {
            ItemStack stack = ctx.getPlayer().getMainHandItem();
            if (!(stack.getItem() instanceof Keyboard))
                return;
            removeTarget(stack);
            ctx.getPlayer().displayClientMessage(Component.translatable("display.arss.keyboard_item.clear_link"), true);
        }
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                ItemStack stack = player.getMainHandItem();
                removeTarget(stack);
                player.displayClientMessage(Component.translatable("display.arss.keyboard_item.clear_link"), true);
            }
        }
        return false;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (entity instanceof Player player && player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                removeTarget(stack);
                player.displayClientMessage(Component.translatable("display.arss.keyboard_item.clear_link"), true);
            }
            return true;
        }
        return super.mineBlock(stack, level, state, pos, entity);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();
        if (player != null) {
            if (state.is(Blocks.LECTERN) && !state.getValue(LecternBlock.HAS_BOOK)) {
                KeyboardLectern.replaceLectern(ctx.getLevel(), pos, state, stack.copy());
                if (!player.isCreative())
                    stack.setCount(0);
            } else if (player.isShiftKeyDown()) {
                if (validBlock(state) && !matchTargetPos(stack, pos)) {
                    setTarget(stack, pos, state, ctx.getLevel().registryAccess());
                    player.displayClientMessage(Component.translatable("display.arss.keyboard_item.linked_to", state.getBlock().getName()), true);
                    return InteractionResult.SUCCESS;
                }
                KeyboardMenu.open(player, ctx.getHand());
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.translatable("display.arss.keyboard_item.toggle_keyboard." + toggleInputState(stack)), true);
                return InteractionResult.SUCCESS;
            }
        }
        return super.useOn(ctx);
    }

    public static Bindings getBindings(ItemStack stack) {
        var out = stack.get(BINDINGS.get());
        if (out == null)
            return Bindings.empty();
        return out;
    }

    public static void setBinding(ItemStack stack, int power, int binding) {
        setBindings(stack, Util.make(() -> {
            int[] bindings = getBindings(stack).raw();
            if (power >= 0 && power < 15)
                bindings[power] = binding;
            return bindings;
        }));
    }

    public static void setBindings(ItemStack stack, int[] raw) {
        if (raw.length != 15) {
            int[] prev = raw;
            raw = new int[15];
            for (int i = 0; i < 15; ++i)
                raw[i] = i < prev.length ? prev[i] : -1;
        }
        stack.set(BINDINGS.get(), new Bindings(raw));
    }

    public static boolean inputActive(ItemStack stack) {
        return stack.getOrDefault(CATCH.get(), false);
    }

    public static void setInputState(ItemStack stack, boolean active) {
        stack.set(CATCH.get(), active);
    }

    public static boolean toggleInputState(ItemStack stack) {
        boolean state = !stack.getOrDefault(CATCH.get(), false);
        stack.set(CATCH.get(), state);
        return state;
    }

    public static void setTarget(ItemStack stack, BlockPos pos, Component name, HolderLookup.Provider provider) {
        stack.set(TARGET.get(), new NamedPos(pos, Component.Serializer.toJson(name, provider)));
    }

    public static void setTarget(ItemStack stack, BlockPos pos, BlockState state, HolderLookup.Provider provider) {
        setTarget(stack, pos, state.getBlock().getName(), provider);
    }

    public static void removeTarget(ItemStack stack) {
        stack.set(TARGET.get(), NamedPos.UNSET);
    }

    public static boolean matchTargetPos(ItemStack stack, BlockPos pos) {
        var target = stack.get(TARGET.get());
        return target != null && target.set() && target.pos().equals(pos);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown())
            KeyboardMenu.open(player, hand);
        else
            player.displayClientMessage(Component.translatable("display.arss.keyboard_item.toggle_keyboard." + toggleInputState(player.getItemInHand(hand))), true);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack newStack, ItemStack oldStack, int slot) {
        return !(newStack.is(R_ITEM.get()) && oldStack.is(R_ITEM.get()));
    }
}
