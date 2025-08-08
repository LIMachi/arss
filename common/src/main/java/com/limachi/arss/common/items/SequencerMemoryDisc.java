package com.limachi.arss.common.items;

import com.limachi.arss.Arss;

import com.limachi.arss.client.ClientDef;

import com.limachi.arss.common.ArssItemStackComponents;
import com.limachi.arss.common.block_entities.SequencerBlockEntity;
import com.limachi.arss.common.blocks.AnalogJukebox;

import com.limachi.lim_lib.common.annotations.RegisterItem;
import com.limachi.lim_lib.common.utils.Game;

import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.JukeboxBlock;

import java.util.List;

public class SequencerMemoryDisc extends Item {

    public static final ResourceLocation SOUND_LOCATION = ResourceLocation.fromNamespaceAndPath(Arss.INSTANCE.registries.mod_id, "static_10min");
    public static final ResourceKey<JukeboxSong> SONG = ResourceKey.create(Registries.JUKEBOX_SONG, SOUND_LOCATION);
    public static final SoundEvent SOUND = SoundEvent.createVariableRangeEvent(SOUND_LOCATION);

    @RegisterItem
    public static RegistrySupplier<Item> R_ITEM;

    public SequencerMemoryDisc(Properties props) { super(props.stacksTo(1).jukeboxPlayable(SONG)); }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> components, TooltipFlag flags) {
        super.appendHoverText(stack, ctx, components, flags);
        ClientDef.commonHoverText("sequencer_record", components);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.getItem() instanceof SequencerMemoryDisc && stack.has(ArssItemStackComponents.SEQUENCER_DATA.get());
    }

    public static void loadFromString(ItemStack stack, String base64) {
        var data = ArssItemStackComponents.SequencerData.fromBase64(base64);
        if (data != null)
            stack.set(ArssItemStackComponents.SEQUENCER_DATA.get(), data);
    }

    public static String saveToString(ItemStack stack) {
        var data = stack.get(ArssItemStackComponents.SEQUENCER_DATA.get());
        if (data == null)
            return "";
        return data.toBase64();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof SequencerMemoryDisc)
            Game.runLogical(()->()->{
                if (player.isShiftKeyDown())
                    loadFromString(stack, Minecraft.getInstance().keyboardHandler.getClipboard());
                else if (stack.has(ArssItemStackComponents.SEQUENCER_DATA.get()))
                    Minecraft.getInstance().keyboardHandler.setClipboard(saveToString(stack));
            }, null);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getLevel().getBlockEntity(ctx.getClickedPos()) instanceof SequencerBlockEntity be) {
            ItemStack stack = ctx.getItemInHand();
            if (ctx.getPlayer() instanceof ServerPlayer player) {
                if (!player.isShiftKeyDown())
                    stack.set(ArssItemStackComponents.SEQUENCER_DATA.get(), ArssItemStackComponents.SequencerData.fromCompoundTag(be.memoryItemData(new CompoundTag())));
                else if (stack.get(ArssItemStackComponents.SEQUENCER_DATA.get()) instanceof ArssItemStackComponents.SequencerData data) {
                    be.loadMemoryItem(data.toCompoundTag(new CompoundTag()));
                    be.setChanged();
                    if (ctx.getLevel() instanceof ServerLevel sl)
                        new SequencerBlockEntity.SyncManually(ctx.getClickedPos(), be.saveSyncData(new CompoundTag())).sendToClients(sl, ctx.getClickedPos());
                }
            }
            return InteractionResult.SUCCESS;
        }
        Block block = ctx.getLevel().getBlockState(ctx.getClickedPos()).getBlock();
        if (block instanceof JukeboxBlock || block instanceof AnalogJukebox)
            return InteractionResult.PASS;
        if (ctx.getPlayer() != null)
            return use(ctx.getLevel(), ctx.getPlayer(), ctx.getHand()).getResult();
        return InteractionResult.PASS;
    }
}
