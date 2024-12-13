package com.limachi.arss.items;

import com.limachi.arss.Arss;
import com.limachi.arss.blockEntities.SequencerBlockEntity;
import com.limachi.arss.blocks.AnalogJukeboxBlock;
import com.limachi.lim_lib.Sides;
import com.limachi.lim_lib.World;
import com.limachi.lim_lib.registries.annotations.RegisterItem;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.List;

public class SequencerMemoryItem extends RecordItem {

    public static final ResourceLocation SOUND_LOCATION = new ResourceLocation(Arss.MOD_ID, "static_10min");
    public static final SoundEvent SOUND = SoundEvent.createVariableRangeEvent(SOUND_LOCATION);

    @RegisterItem
    public static RegistryObject<Item> R_ITEM;

    public SequencerMemoryItem() { super(0, ()->SOUND, new Properties().stacksTo(1), SequencerBlockEntity.MAXIMUM_TICK_COUNT); }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flags) {
        super.appendHoverText(stack, level, components, flags);
        Arss.commonHoverText("sequencer_record", components);
    }

    @Override
    public int getAnalogOutput() {
        if (World.overworld() instanceof ServerLevel level)
            return level.getRandom().nextInt(15);
        return 0;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof SequencerBlockEntity be) {
            if (context.getPlayer() instanceof ServerPlayer player) {
                if (!player.isShiftKeyDown())
                    be.memoryItemData(stack.getOrCreateTag());
                else {
                    be.loadMemoryItem(stack.getOrCreateTag());
                    be.setChanged();
                }
            }
            return InteractionResult.SUCCESS;
        }
        Block block = context.getLevel().getBlockState(context.getClickedPos()).getBlock();
        if (block instanceof JukeboxBlock || block instanceof AnalogJukeboxBlock)
            return InteractionResult.PASS;
        if (context.getPlayer() != null)
            return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
        return InteractionResult.PASS;
    }

    public static boolean validTagData(CompoundTag tag) {
        return tag.contains("Ticks", Tag.TAG_BYTE_ARRAY) && tag.contains("Length", Tag.TAG_INT) && tag.contains("Head", Tag.TAG_INT) && tag.contains("Limits", Tag.TAG_INT_ARRAY);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        if (stack.getItem() instanceof SequencerMemoryItem && stack.getTag() != null)
            return validTagData(stack.getTag());
        return super.isFoil(stack);
    }

    public static void loadFromString(ItemStack stack, String ser) {
        CompoundTag tag = new CompoundTag();
        ByteBuffer bb;
        try {
            bb = ByteBuffer.wrap(Base64.getDecoder().decode(ser));
        } catch (IllegalArgumentException ignore) {
            return;
        }
        try {
            int al = bb.array().length - 5;
            if (al <= 0) return;
            tag.putInt("Length", bb.getShort());
            tag.putInt("Head", bb.getShort());
            int ll = bb.get();
            al -= ll * 2;
            if (al <= 0) return;
            int[] limits = new int[ll];
            for (int i = 0; i < ll; ++i)
                limits[i] = bb.getShort();
            tag.putIntArray("Limits", limits);
            byte[] ticks = new byte[al];
            bb.get(ticks);
            tag.putByteArray("Ticks", ticks);
            stack.setTag(tag);
        } catch (BufferUnderflowException ignore) {
            return;
        }
    }

    public static String saveToString(ItemStack stack) {
        if (stack.getTag() != null && validTagData(stack.getTag())) {
            CompoundTag tag = stack.getTag();
            byte[] ticksRLE = tag.getByteArray("Ticks");
            int[] limits = tag.getIntArray("Limits");
            ByteBuffer tmp = ByteBuffer.allocate(5 + ticksRLE.length + limits.length * 2);
            tmp.putShort((short)tag.getInt("Length"));
            tmp.putShort((short)tag.getInt("Head"));
            tmp.put((byte)limits.length);
            for (int l : limits)
                tmp.putShort((short)l);
            tmp.put(ticksRLE);
            return Base64.getEncoder().encodeToString(tmp.array());
        }
        return "";
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof SequencerMemoryItem)
            Sides.logicalSideRun(()->()->{
                if (player.isShiftKeyDown())
                    loadFromString(stack, Minecraft.getInstance().keyboardHandler.getClipboard());
                else if (stack.getTag() != null)
                    Minecraft.getInstance().keyboardHandler.setClipboard(saveToString(stack));
                return null;
            }, null);
        return InteractionResultHolder.success(stack);
    }
}
