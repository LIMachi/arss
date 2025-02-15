package com.limachi.arss.common.block_entities;

import com.limachi.arss.utils.annotations.RegisterBlockEntity;
import com.limachi.arss.utils.annotations.RegisterMsg;
import com.limachi.arss.utils.network.IC2SMsg;
import com.limachi.arss.utils.network.IS2CMsg;

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

import java.util.*;

public class KeyboardLectern extends BlockEntity {
    //WARN: TRANSITIVE HASHMAP!
    protected static final HashMap<UUID, HashSet<BlockPos>> CONTROLLED_LECTERNS = new HashMap<>();
    protected ItemStack keyboard = ItemStack.EMPTY;
    protected int userCount = 0;

    @RegisterBlockEntity
    public static RegistrySupplier<BlockEntityType<KeyboardLectern>> TYPE;

    public void setKeyboard(ItemStack keyboard) { this.keyboard = keyboard; }

    public ItemStack getKeyboard() { return keyboard; }

    public static HashSet<BlockPos> getKeyboardLecterns(Player player) {
        var out = CONTROLLED_LECTERNS.get(player.getUUID());
        if (out == null)
            out = new HashSet<>();
        else if (out.isEmpty())
            CONTROLLED_LECTERNS.remove(player.getUUID());
        return out;
    }

    public static void setKeyboardLecterns(Player player, HashSet<BlockPos> lecterns) {
        if (lecterns.isEmpty())
            CONTROLLED_LECTERNS.remove(player.getUUID());
        else
            CONTROLLED_LECTERNS.put(player.getUUID(), lecterns);
    }

    public static List<KeyboardLectern> getControlled(Player player) {
        ArrayList<KeyboardLectern> out = new ArrayList<>();
        if (player != null) {
            UUID id = player.getUUID();
            synchronized (CONTROLLED_LECTERNS) {
                var set = CONTROLLED_LECTERNS.get(id);
                if (set == null)
                    return out;
                HashSet<BlockPos> keep = new HashSet<>();
                boolean resync = false;
                for (var lectern : set)
                    if (player.blockPosition().distSqr(lectern) <= 36 && player.level().getBlockEntity(lectern) instanceof KeyboardLectern be) {
                        out.add(be);
                        keep.add(lectern);
                    } else
                        resync = true;
                if (resync) {
                    if (keep.isEmpty())
                        CONTROLLED_LECTERNS.remove(id);
                    else
                        CONTROLLED_LECTERNS.put(id, keep);
                    if (player.level().isClientSide)
                        new SyncLinkedLecterns(keep.toArray(new BlockPos[keep.size()]), true).sendToServer();
                }
            }
        }
        return out;
    }

    @RegisterMsg
    public record SyncLinkedLecterns(BlockPos[] lecterns, Boolean upstream) implements IC2SMsg<SyncLinkedLecterns>, IS2CMsg<SyncLinkedLecterns> {
        @Override
        public void run(NetworkManager.PacketContext ctx) {
            Player player = ctx.getPlayer();
            if (upstream) {
                HashSet<BlockPos> current = getKeyboardLecterns(player);//new ArrayList<>(Arrays.stream(player.getPersistentData().getLongArray("KeyboardLecternBlock")).boxed().toList());
                HashSet<BlockPos> valid = new HashSet<>();
                for (BlockPos l : lecterns) {
                    if (player.level().getBlockEntity(l) instanceof KeyboardLectern be) {
                        valid.add(l);
                        if (!current.contains(l))
                            be.addUser(player);
                    }
                    current.remove(l);
                }
                for (BlockPos l : current)
                    if (player.level().getBlockEntity(l) instanceof KeyboardLectern be)
                        be.removeUser(player);
                setKeyboardLecterns(player, valid);
            } else
                setKeyboardLecterns(player, new HashSet<>(Arrays.asList(lecterns)));
        }
    }

    protected void addUser(Player player) {
        player.displayClientMessage(Component.translatable("display.arss.keyboard_item.toggle_keyboard.true"), true);
        if (userCount == 0 && level != null && getBlockState().is(com.limachi.arss.common.blocks.KeyboardLectern.R_BLOCK.get()) && !getBlockState().getValue(com.limachi.arss.common.blocks.KeyboardLectern.POWERED))
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(com.limachi.arss.common.blocks.KeyboardLectern.POWERED, true));
        ++userCount;
        setChanged();
    }

    protected void removeUser(Player player) {
        player.displayClientMessage(Component.translatable("display.arss.keyboard_item.toggle_keyboard.false"), true);
        if (userCount > 0) {
            --userCount;
            if (userCount == 0 && level != null && getBlockState().is(com.limachi.arss.common.blocks.KeyboardLectern.R_BLOCK.get()) && getBlockState().getValue(com.limachi.arss.common.blocks.KeyboardLectern.POWERED))
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(com.limachi.arss.common.blocks.KeyboardLectern.POWERED, false));
            setChanged();
        }
    }

    public void setController(Player player) {
        if (player != null) {
            var keep = getKeyboardLecterns(player);
            if (keep.contains(worldPosition)) {
                removeUser(player);
                keep.remove(worldPosition);
            } else {
                addUser(player);
                keep.add(worldPosition);
            }
            setKeyboardLecterns(player, keep);
            if (player instanceof ServerPlayer serverPlayer)
                new SyncLinkedLecterns(keep.toArray(new BlockPos[keep.size()]), false).sendToClient(serverPlayer);
        }
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
}
