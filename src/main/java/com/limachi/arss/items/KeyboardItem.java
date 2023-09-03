package com.limachi.arss.items;

import com.limachi.arss.Arss;
import com.limachi.arss.blocks.KeyboardLecternBlock;
import com.limachi.arss.client.MidiHandler;
import com.limachi.arss.menu.KeyboardMenu;
import com.limachi.lim_lib.network.IRecordMsg;
import com.limachi.lim_lib.network.RegisterMsg;
import com.limachi.lim_lib.registries.ClientRegistries;
import com.limachi.lim_lib.registries.StaticInitClient;
import com.limachi.lim_lib.registries.annotations.RegisterItem;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = Arss.MOD_ID, value = Dist.CLIENT)
public class KeyboardItem extends Item {

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
        if (stack.getTag() != null)
            return getTint(index, stack.getTag().getInt("output"), stack.getTag().getBoolean("catch_keyboard"));
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

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        Player player = context.getPlayer();
        if (player != null && player.mayBuild()) {
            if (state.is(Blocks.LECTERN) && !state.getValue(LecternBlock.HAS_BOOK)) {
                KeyboardLecternBlock.replaceLectern(context.getLevel(), context.getClickedPos(), state, stack.copy());
                if (!player.isCreative())
                    stack.setCount(0);
            } else if (validBlock(state)) {
                CompoundTag tag = stack.getOrCreateTag();
                tag.putLong("target", context.getClickedPos().asLong());
                MutableComponent name = state.getBlock().getName();
                tag.putString("target_name", Component.Serializer.toJson(name));
                player.displayClientMessage(Component.translatable("display.arss.keyboard_item.linked_to", name), true);
                return InteractionResult.SUCCESS;
            } else if (player.isCrouching()) {
                CompoundTag tag = stack.getOrCreateTag();
                tag.remove("target");
                tag.remove("target_name");
                player.displayClientMessage(Component.translatable("display.arss.keyboard_item.clear_link"), true);
                return InteractionResult.SUCCESS;
            } else if (!(state.getBlock() instanceof AirBlock)) {
                ItemStack nameGetter = state.getCloneItemStack(new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside()), context.getLevel(), context.getClickedPos(), context.getPlayer());
                player.displayClientMessage(Component.translatable("display.arss.keyboard_item.cant_link_to", nameGetter.getHoverName()), true);
                return InteractionResult.SUCCESS;
            }
        }
        return super.onItemUseFirst(stack, context);
    }

    @RegisterMsg
    public record DisableKeyboardCatchMsg() implements IRecordMsg {
        @Override
        public void serverWork(Player player) {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof KeyboardItem))
                stack = player.getOffhandItem();
            if (!(stack.getItem() instanceof KeyboardItem))
                return;
            stack.getOrCreateTag().putBoolean("catch_keyboard", false);
            player.displayClientMessage(Component.translatable("display.arss.keyboard_item.toggle_keyboard.false"), true);
        }
    }

    public static void initDefaultBindings(CompoundTag tag) {
        if (!tag.contains("bindings"))
            tag.putIntArray("bindings", Util.make(new int[15], a->{for (int i = 0; i < 15; ++i) a[i] = i + MidiHandler.NOTE_OFFSET; }));
    }

    @Override
    public void inventoryTick(ItemStack stack, @Nonnull Level level, @Nonnull Entity entity, int slot, boolean selected) {
        initDefaultBindings(stack.getOrCreateTag());
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, Player player, @Nonnull InteractionHand hand) {
        if (player.isShiftKeyDown())
            KeyboardMenu.open(player, hand);
        else {
            CompoundTag tag = player.getItemInHand(hand).getOrCreateTag();
            tag.putBoolean("catch_keyboard", !tag.getBoolean("catch_keyboard"));
            player.displayClientMessage(Component.translatable("display.arss.keyboard_item.toggle_keyboard." + tag.getBoolean("catch_keyboard")), true);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !(oldStack.is(R_ITEM.get()) && newStack.is(R_ITEM.get()));
    }
}
