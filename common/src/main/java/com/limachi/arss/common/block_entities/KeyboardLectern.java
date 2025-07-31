package com.limachi.arss.common.block_entities;

import com.limachi.arss.client.keyboardSystem.KeyboardHandler;

import com.limachi.arss.common.items.Keyboard;
import com.limachi.lim_lib.common.annotations.Config;
import com.limachi.lim_lib.common.annotations.RegisterBlockEntity;
import com.limachi.lim_lib.common.annotations.RegisterMsg;
import com.limachi.lim_lib.common.network.IS2CMsg;

import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class KeyboardLectern extends BlockEntity {
    @Config(cmt = "how far can a player be and still use a keyboard on a lectern", reload = true, path = "Keyboard", name = "LecternReach")
    public static double LECTERN_REACH = 8.;

    protected ItemStack keyboard = ItemStack.EMPTY;
    protected Player currentUser;

    @RegisterMsg
    public record ConnectionStatus(BlockPos pos, boolean connected) implements IS2CMsg<ConnectionStatus> {
        @Override
        public void run(NetworkManager.PacketContext ctx) {
            if (ctx.getPlayer() instanceof Player player && player.level().getBlockEntity(pos) instanceof KeyboardLectern be)
                KeyboardHandler.useLectern(be, connected);
        }
    }

    @RegisterBlockEntity
    public static RegistrySupplier<BlockEntityType<KeyboardLectern>> TYPE;

    public void setKeyboard(ItemStack keyboard) { this.keyboard = keyboard; }

    public ItemStack getKeyboard() { return keyboard; }

    public Player getCurrentUser() { return currentUser; }

    protected void addUser(Player player) {
        player.displayClientMessage(Component.translatable("display.arss.keyboard_item.toggle_keyboard.true"), true);
        if (currentUser == null && player != null && getBlockState().is(com.limachi.arss.common.blocks.KeyboardLectern.R_BLOCK.get()) && !getBlockState().getValue(com.limachi.arss.common.blocks.KeyboardLectern.POWERED))
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(com.limachi.arss.common.blocks.KeyboardLectern.POWERED, true));
        if (currentUser instanceof ServerPlayer p)
            new ConnectionStatus(worldPosition, true).sendToClient(p);
        Keyboard.setListening(keyboard, true);
        currentUser = player;
    }

    protected void removeUser() {
        if (currentUser != null) {
            currentUser.displayClientMessage(Component.translatable("display.arss.keyboard_item.toggle_keyboard.false"), true);
            if (level != null && getBlockState().is(com.limachi.arss.common.blocks.KeyboardLectern.R_BLOCK.get()) && getBlockState().getValue(com.limachi.arss.common.blocks.KeyboardLectern.POWERED))
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(com.limachi.arss.common.blocks.KeyboardLectern.POWERED, false));
            if (currentUser instanceof ServerPlayer p)
                new ConnectionStatus(worldPosition, false).sendToClient(p);
            Keyboard.setListening(keyboard, false);
            currentUser = null;
        }
    }

    public void setController(Player player) {
        if (player == null || currentUser == player)
            removeUser();
        else
            addUser(player);
    }

    public KeyboardLectern(BlockPos pos, BlockState state) { super(TYPE.get(), pos, state); }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithoutMetadata(provider);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (!keyboard.isEmpty())
            tag.put("keyboard", keyboard.save(provider));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("keyboard", Tag.TAG_COMPOUND))
            ItemStack.parse(provider, tag.getCompound("keyboard")).ifPresent(s->keyboard = s);
    }

    public void tick() {
        if (currentUser != null && !currentUser.position().closerThan(worldPosition.getCenter(), KeyboardLectern.LECTERN_REACH))
            removeUser();
    }
}
